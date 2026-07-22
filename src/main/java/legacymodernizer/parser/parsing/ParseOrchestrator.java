package legacymodernizer.parser.parsing;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import legacymodernizer.parser.api.stream.ParseEventSink;
import legacymodernizer.parser.api.stream.ParseStreamEvent;
import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.recovery.diagnostics.ParseDiagnosticsWriter;
import legacymodernizer.parser.recovery.reports.RepairAuditWriter;
import legacymodernizer.parser.recovery.reports.RepairPromotionReporter;
import legacymodernizer.parser.recovery.quality.QualityDecision;
import legacymodernizer.parser.recovery.quality.QualityStatus;
import legacymodernizer.parser.recovery.quality.ParseQualityGate;
import legacymodernizer.parser.parsing.languages.LanguageCatalogValidator;
import legacymodernizer.parser.parsing.languages.LanguageModule;
import legacymodernizer.parser.recovery.LayeredRecoveryPipeline;
import legacymodernizer.parser.recovery.evidence.RecoveryOutcome;
import legacymodernizer.parser.service.ParseProgressTracker;
import legacymodernizer.parser.parsing.ParserSelection.DetectionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 소스 파싱 오케스트레이션.
 *
 * <p>호출자가 target 을 던지지 않는다 — {@link ParserSelection} 이 파일별로 파서 모듈을
 * 자동 결정(확장자 + .sql 방언 마커)하고, 여기서 파일마다 알맞은 전략으로 파싱한다.
 * 결과는 analysis/ 에 source 구조 그대로 미러로 저장.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParseOrchestrator {

    private final ParserWorkspace parserWorkspace;
    private final ParserSelection parserSelection;
    private final ParseQualityGate qualityGate;
    private final ParseDiagnosticsWriter diagnosticsWriter;
    private final RepairAuditWriter repairAuditWriter;
    private final RepairPromotionReporter repairPromotionReporter;
    private final LanguageCatalogValidator languageCatalogValidator;
    private final LayeredRecoveryPipeline recoveryPipeline;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 소스를 자동 감지·라우팅해 파싱하며 진행 상황을 스트림으로 전달.
     *
     * @param sourcePath 분석할 로컬 폴더 경로(Electron 경로 모드). null/빈값이면 업로드된
     *                   {@code data/source} 를 파싱(브라우저 업로드 모드).
     */
    public void parse(String sourcePath, ParseEventSink eventSink) {
        eventSink.message("run_started", "🧭 코드 구조 파악을 준비하고 있어요", "PREPARING", "RUNNING");
        // 경로 모드(Electron): 로컬 폴더를 내용 분류해 data/{source, ddl} 로 반입(spec 006 입구 통일).
        // 업로드 모드: 이미 /fileUpload 가 data/ 를 채웠다. 두 모드 모두 이후 data/source 만 파싱.
        if (sourcePath != null && !sourcePath.isBlank()) {
            Path localRoot = Path.of(sourcePath.trim());
            if (!Files.exists(localRoot)) {
                eventSink.error("run_failed", "로컬 경로를 찾을 수 없어요: " + localRoot, "INTAKE");
                throw new RuntimeException("로컬 경로 없음: " + localRoot);
            }
            eventSink.message("intake_started", "📁 선택한 폴더에서 분석 파일을 가져오고 있어요: "
                    + localRoot, "INTAKE", "RUNNING");
            ParserWorkspace.IntakeResult intake = parserWorkspace.intakeFromPath(localRoot);
            eventSink.emit(ParseStreamEvent.builder("message", "intake_completed")
                    .content(String.format("📦 파일 준비 완료 — 소스 %d개 · DDL %d개%s",
                            intake.sourceCount(), intake.ddlCount(),
                            intake.skipped().isEmpty() ? "" : " · 제외 " + intake.skipped().size() + "개"))
                    .phase("INTAKE").status("COMPLETED")
                    .counts(counts("sourceFiles", intake.sourceCount(), "ddlFiles", intake.ddlCount(),
                            "skippedFiles", intake.skipped().size())).build());
            for (String skipped : intake.skipped()) {
                eventSink.emit(ParseStreamEvent.builder("warning", "file_skipped")
                        .content("⏭️ 분석 대상에서 제외했어요: " + skipped)
                        .phase("INTAKE").status("WARNING").file(skipped).build());
            }
        }

        // 입구 통일: 입력 모드와 무관하게 항상 data/source 를 파싱한다.
        Path sourceBase = parserWorkspace.sourceDir();
        Path analysisBase = parserWorkspace.analysisDir();
        if (!Files.exists(sourceBase)) {
            eventSink.error("run_failed", "파싱할 소스 디렉터리를 찾을 수 없어요: "
                    + sourceBase, "PREPARING");
            throw new RuntimeException("소스 디렉토리 없음: " + sourceBase);
        }

        // 파싱은 매 run 자기 출력(analysis/)을 새로 만든다(재파싱 시 stale AST 방지).
        parserWorkspace.clearAnalysisDir();
        parserWorkspace.clearRecoveryArtifacts();
        languageCatalogValidator.validateModules(parserSelection.modules());

        DetectionResult detection = parserSelection.detect(sourceBase);
        Map<Path, LanguageModule> modulesByFile = detection.modulesByFile();

        eventSink.emit(ParseStreamEvent.builder("message", "language_detection_completed")
                .content(String.format("🔎 언어를 확인했어요 — %s%s · 파싱 대상 %d개",
                detection.detectedTargets(),
                detection.sqlDialect() != null ? " (SQL 방언: " + detection.sqlDialect() + ")" : "",
                modulesByFile.size()))
                .phase("DETECTION").status("COMPLETED").total(modulesByFile.size())
                .language(detection.primaryTarget()).build());

        // 프론트가 sourceType·strategy 를 자동 설정하도록 구조화된 감지 결과를 함께 전달.
        emitDetected(eventSink, detection);

        if (modulesByFile.isEmpty()) {
            eventSink.warning("run_completed", "⚠️ 지원하는 언어의 파싱 대상 파일이 없어요",
                    "COMPLETED");
            return;
        }

        int total = modulesByFile.size();
        AtomicInteger index = new AtomicInteger(0);
        RunCounters counters = new RunCounters();

        eventSink.emit(ParseStreamEvent.builder("message", "parsing_started")
                .content(String.format("🚀 파싱을 시작합니다 — 코드 파일 %,d개의 구조를 차례로 읽을게요", total))
                .phase("PARSING").status("RUNNING").current(0).total(total).percent(0).build());

        // 전역 컨텍스트가 필요한 전략(예: C 매크로/타입 사전수집)을 전략별 1회 준비.
        modulesByFile.values().stream().distinct().forEach(LanguageModule::prepareProjectContext);

        for (Map.Entry<Path, LanguageModule> entry : modulesByFile.entrySet()) {
            parseSingleFile(entry.getKey(), entry.getValue(), sourceBase, analysisBase, eventSink,
                    index.incrementAndGet(), total, counters);
        }

        try {
            Optional<Path> promotionReport = repairPromotionReporter.writeIfCandidates();
            promotionReport.ifPresent(path -> eventSink.emit(ParseStreamEvent
                    .builder("message", "repair_promotion_candidates")
                    .content("🧩 반복 복구 패턴을 검토 후보로 정리했어요: "
                            + path.toString().replace('\\', '/'))
                    .phase("FINALIZING").status("COMPLETED").build()));
        } catch (Exception error) {
            eventSink.warning("repair_promotion_failed",
                    "⚠️ 반복 복구 패턴 보고서를 만들지 못했어요: " + error.getMessage(),
                    "FINALIZING");
            log.error("Repair promotion report failed", error);
        }

        Map<String, Integer> finalCounts = counts(
                "exact", counters.exact.get(),
                "recovered", counters.recovered.get(),
                "partial", counters.partial.get(),
                "reviewRequired", counters.reviewRequired.get(),
                "unresolved", counters.unresolved.get(),
                "failed", counters.failed.get(),
                "unresolvedOrFailed", counters.unresolved.get() + counters.failed.get(),
                "astFiles", counters.success.get(),
                "lines", counters.totalLines.get());
        eventSink.emit(ParseStreamEvent.builder("quality-summary", "quality_summary")
                .content(toLegacyQualityJson(finalCounts)).phase("FINALIZING")
                .status(counters.error.get() > 0 ? "WARNING" : "COMPLETED")
                .current(total).total(total).percent(100).counts(finalCounts).build());
        String summaryPrefix = counters.error.get() > 0 ? "⚠️" : "🎉";
        eventSink.emit(ParseStreamEvent.builder("message", "run_completed")
                .content(String.format(
                        "%s 파싱 완료 — 정확 %d · 복구 %d · 부분 %d · 검토 필요 %d · 미해결 %d · 실패 %d · AST %d개 · %,d라인",
                        summaryPrefix, counters.exact.get(), counters.recovered.get(),
                        counters.partial.get(), counters.reviewRequired.get(),
                        counters.unresolved.get(), counters.failed.get(),
                        counters.success.get(), counters.totalLines.get()))
                .phase("COMPLETED").status(counters.error.get() > 0 ? "WARNING" : "COMPLETED")
                .current(total).total(total).percent(100).counts(finalCounts).build());
    }

    /** run 전체 집계 카운터 — parseSingleFile 의 AtomicInteger 파라미터 폭발을 대체한다. */
    private static final class RunCounters {
        final AtomicInteger success = new AtomicInteger(0);
        final AtomicInteger exact = new AtomicInteger(0);
        final AtomicInteger recovered = new AtomicInteger(0);
        final AtomicInteger partial = new AtomicInteger(0);
        final AtomicInteger reviewRequired = new AtomicInteger(0);
        final AtomicInteger unresolved = new AtomicInteger(0);
        final AtomicInteger failed = new AtomicInteger(0);
        final AtomicInteger error = new AtomicInteger(0);
        final AtomicInteger totalLines = new AtomicInteger(0);
    }

    private void parseSingleFile(Path file, LanguageModule module,
                                  Path sourceBase, Path analysisBase, ParseEventSink eventSink,
                                  int fileIndex, int totalFiles, RunCounters counters) {
        Path relative = sourceBase.relativize(file);
        String fileName = relative.toString();

        try {
            int lineCount = countLines(file);

            String relStr = relative.toString();
            int dot = relStr.lastIndexOf('.');
            String jsonPath = (dot > 0 ? relStr.substring(0, dot) : relStr) + ".json";
            Path output = analysisBase.resolve(jsonPath);
            Files.createDirectories(output.getParent());

            ParseProgressTracker tracker = new ParseProgressTracker(eventSink, fileName,
                    module.languageId(), fileIndex, totalFiles, lineCount);

            eventSink.emit(ParseStreamEvent.builder("message", "file_started")
                    .content(String.format("📄 [%d/%d] %s — %s %,d라인을 읽고 있어요",
                            fileIndex, totalFiles, fileName, module.languageId().toUpperCase(), lineCount))
                    .phase("PARSING").status("RUNNING")
                    .current(fileIndex).total(totalFiles)
                    .percent((int) Math.floor(((double) (fileIndex - 1) / totalFiles) * 100.0))
                    .file(fileName).language(module.languageId()).build());

            long processingStarted = System.nanoTime();
            RawParseResult firstPass = module.parseFile(file.toFile(), tracker);
            QualityDecision decision = qualityGate.evaluateFirstPass(firstPass);
            RecoveryOutcome recovery = recoveryPipeline.recover(
                    module, file, sourceBase, firstPass, decision, tracker);
            long processingElapsedMillis = Math.max(firstPass.elapsedMillis(),
                    (System.nanoTime() - processingStarted) / 1_000_000L);
            diagnosticsWriter.write(file, sourceBase, firstPass, recovery,
                    processingElapsedMillis);
            repairAuditWriter.write(file, sourceBase, firstPass, recovery);

            if (!recovery.hasAcceptedAst()) {
                Files.deleteIfExists(output);
                counters.error.incrementAndGet();
                switch (recovery.decision().status()) {
                    case REVIEW_REQUIRED -> counters.reviewRequired.incrementAndGet();
                    case UNRESOLVED -> counters.unresolved.incrementAndGet();
                    default -> counters.failed.incrementAndGet();
                }
                eventSink.emit(ParseStreamEvent.builder("error", "file_result")
                        .content(String.format("❌ [%d/%d] %s — 결과를 만들지 않았어요 (%s: %s)",
                                fileIndex, totalFiles, fileName,
                                friendlyQuality(recovery.decision().status()),
                                recovery.decision().reasons()))
                        .phase("RECOVERY").status("FAILED")
                        .current(fileIndex).total(totalFiles)
                        .percent(fileIndex * 100 / totalFiles).file(fileName)
                        .language(module.languageId())
                        .quality(recovery.decision().status().name()).build());
                log.warn("  [QUALITY REJECTED] {} ({}) - {}", relative,
                        module.languageId(), recovery.decision().reasons());
                return;
            }

            Files.writeString(output, recovery.astJson(), StandardCharsets.UTF_8);

            counters.success.incrementAndGet();
            switch (recovery.decision().status()) {
                case EXACT -> counters.exact.incrementAndGet();
                case PARTIAL -> counters.partial.incrementAndGet();
                case RECOVERED_SAFE, RECOVERED_VALIDATED -> counters.recovered.incrementAndGet();
                default -> { }
            }
            counters.totalLines.addAndGet(lineCount);
            QualityStatus quality = recovery.decision().status();
            String wireType = quality == QualityStatus.PARTIAL ? "warning" : "message";
            String symbol = quality == QualityStatus.EXACT ? "✅"
                    : quality == QualityStatus.PARTIAL ? "⚠️" : "🛠️";
            eventSink.emit(ParseStreamEvent.builder(wireType, "file_result")
                    .content(String.format("%s [%d/%d] %s — %s (%s · %,d라인 · %,dms)",
                            symbol, fileIndex, totalFiles, fileName, friendlyQuality(quality),
                            module.languageId().toUpperCase(), lineCount, processingElapsedMillis))
                    .phase(quality == QualityStatus.EXACT ? "PARSING" : "RECOVERY")
                    .status(quality == QualityStatus.PARTIAL ? "WARNING" : "COMPLETED")
                    .current(fileIndex).total(totalFiles).percent(fileIndex * 100 / totalFiles)
                    .file(fileName).language(module.languageId()).quality(quality.name()).build());
            log.info("  [PARSED] {} ({})", relative, module.languageId());

        } catch (Throwable e) {
            counters.error.incrementAndGet();
            counters.failed.incrementAndGet();
            eventSink.emit(ParseStreamEvent.builder("error", "file_result")
                    .content(String.format("❌ [%d/%d] %s — 파싱하지 못했어요: %s",
                            fileIndex, totalFiles, fileName, e.getMessage()))
                    .phase("PARSING").status("FAILED")
                    .current(fileIndex).total(totalFiles).percent(fileIndex * 100 / totalFiles)
                    .file(fileName).language(module.languageId()).quality("FAILED").build());
            log.error("  [PARSE ERROR] {} - {}", relative, e.getMessage(), e);
        }
    }

    /** 구조화된 감지 결과({target, strategy, sqlDialect, targets})를 'detected' 이벤트로 스트림에 전달. */
    private void emitDetected(ParseEventSink eventSink, DetectionResult detection) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("target", detection.primaryTarget());
        payload.put("strategy", detection.analysisStrategy());
        payload.put("sqlDialect", detection.sqlDialect());
        payload.put("targets", detection.detectedTargets());
        try {
            eventSink.emit(ParseStreamEvent.builder("detected", "language_detected")
                    .content(objectMapper.writeValueAsString(payload))
                    .phase("DETECTION").status("COMPLETED")
                    .total(detection.modulesByFile().size())
                    .language(detection.primaryTarget()).build());
        } catch (Exception e) {
            log.warn("detected 이벤트 직렬화 실패: {}", e.getMessage());
        }
    }

    private static String friendlyQuality(QualityStatus quality) {
        return switch (quality) {
            case EXACT -> "문법 오류 없이 정확히 파싱됐어요";
            case RECOVERED_SAFE -> "안전 규칙으로 복구해 파싱했어요";
            case RECOVERED_VALIDATED -> "복구 후 재검증까지 통과했어요";
            case PARTIAL -> "안전한 부분만 파싱했고 일부는 남겼어요";
            case REVIEW_REQUIRED -> "자동 판단이 위험해 사람 검토가 필요해요";
            case UNRESOLVED -> "자동 복구하지 못했어요";
            case FAILED -> "파싱에 실패했어요";
        };
    }

    private static Map<String, Integer> counts(Object... entries) {
        Map<String, Integer> values = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            values.put((String) entries[index], (Integer) entries[index + 1]);
        }
        return values;
    }

    private static String toLegacyQualityJson(Map<String, Integer> counts) {
        return String.format(
                "{\"exact\":%d,\"recovered\":%d,\"partial\":%d,\"unresolvedOrFailed\":%d}",
                counts.get("exact"), counts.get("recovered"), counts.get("partial"),
                counts.get("unresolvedOrFailed"));
    }

    private int countLines(Path file) {
        try {
            return (int) SourceTextCodec.decode(Files.readAllBytes(file)).text().lines().count();
        } catch (Exception readFailure) {
            log.warn("라인 수 계산 실패: {} - {}", file, readFailure.getMessage());
            return 0;
        }
    }
}
