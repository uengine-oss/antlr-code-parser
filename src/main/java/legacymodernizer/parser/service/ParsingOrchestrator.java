package legacymodernizer.parser.service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import legacymodernizer.parser.service.LanguageDetector.DetectionResult;
import legacymodernizer.parser.service.strategy.TargetParserStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 소스 파싱 오케스트레이션.
 *
 * <p>호출자가 target 을 던지지 않는다 — {@link LanguageDetector} 가 파일별로 파서 전략을
 * 자동 결정(확장자 + .sql 방언 마커)하고, 여기서 파일마다 알맞은 전략으로 파싱한다.
 * 결과는 analysis/ 에 source 구조 그대로 미러로 저장.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParsingOrchestrator {

    private final FileStorageService storageService;
    private final LanguageDetector languageDetector;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 소스를 자동 감지·라우팅해 파싱하며 진행 상황을 스트림으로 전달.
     *
     * @param sourcePath 분석할 로컬 폴더 경로(Electron 경로 모드). null/빈값이면 업로드된
     *                   {@code data/source} 를 파싱(브라우저 업로드 모드).
     */
    public void parse(String sourcePath, StreamCallback callback) {
        // 경로 모드(Electron): 로컬 폴더를 내용 분류해 data/{source, ddl} 로 반입(spec 006 입구 통일).
        // 업로드 모드: 이미 /fileUpload 가 data/ 를 채웠다. 두 모드 모두 이후 data/source 만 파싱.
        if (sourcePath != null && !sourcePath.isBlank()) {
            Path localRoot = Path.of(sourcePath.trim());
            if (!Files.exists(localRoot)) {
                callback.error("로컬 경로 없음: " + localRoot);
                throw new RuntimeException("로컬 경로 없음: " + localRoot);
            }
            callback.message("📁 로컬 경로 반입: " + localRoot);
            FileStorageService.IntakeResult intake = storageService.intakeFromPath(localRoot);
            callback.message(String.format("📦 반입 완료 — DDL %d개 · 소스 %d개%s",
                    intake.ddlCount(), intake.sourceCount(),
                    intake.skipped().isEmpty() ? "" : " · 건너뜀 " + intake.skipped().size() + "개"));
            for (String skipped : intake.skipped()) {
                callback.send("skipped", skipped);
            }
        }

        // 입구 통일: 입력 모드와 무관하게 항상 data/source 를 파싱한다.
        Path sourceBase = storageService.sourceDir();
        Path analysisBase = storageService.analysisDir();
        if (!Files.exists(sourceBase)) {
            callback.error("소스 디렉토리 없음: " + sourceBase);
            throw new RuntimeException("소스 디렉토리 없음: " + sourceBase);
        }

        // 파싱은 매 run 자기 출력(analysis/)을 새로 만든다(재파싱 시 stale AST 방지).
        storageService.clearAnalysisDir();

        DetectionResult detection = languageDetector.detect(sourceBase);
        Map<Path, TargetParserStrategy> fileStrategies = detection.fileStrategies();

        callback.message(String.format("🔎 언어 자동 감지: %s%s — 파싱 대상 %d개 파일",
                detection.detectedTargets(),
                detection.sqlDialect() != null ? " (SQL 방언: " + detection.sqlDialect() + ")" : "",
                fileStrategies.size()));

        // 프론트가 sourceType·strategy 를 자동 설정하도록 구조화된 감지 결과를 함께 전달.
        emitDetected(callback, detection);

        if (fileStrategies.isEmpty()) {
            callback.message("⚠️ 지원하는 확장자의 파싱 대상 파일이 없습니다.");
            return;
        }

        int total = fileStrategies.size();
        AtomicInteger index = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicInteger totalLines = new AtomicInteger(0);

        callback.message(String.format("🚀 파싱을 시작합니다. (총 %d개 파일)", total));

        // 전역 컨텍스트가 필요한 전략(예: C 매크로/타입 사전수집)을 전략별 1회 준비.
        fileStrategies.values().stream().distinct().forEach(TargetParserStrategy::prepare);

        for (Map.Entry<Path, TargetParserStrategy> entry : fileStrategies.entrySet()) {
            parseSingleFile(entry.getKey(), entry.getValue(), sourceBase, analysisBase, callback,
                    index.incrementAndGet(), total, successCount, errorCount, totalLines);
        }

        if (errorCount.get() > 0) {
            callback.message(String.format("⚠️ 파싱 완료 (일부 에러). 성공: %d개, 실패: %d개, 총 %,d라인",
                    successCount.get(), errorCount.get(), totalLines.get()));
        } else {
            callback.message(String.format("🎉 파싱 완료! 총 %d개 파일, %,d라인 처리됨",
                    successCount.get(), totalLines.get()));
        }
    }

    private void parseSingleFile(Path file, TargetParserStrategy strategy,
                                  Path sourceBase, Path analysisBase, StreamCallback callback,
                                  int fileIndex, int totalFiles,
                                  AtomicInteger successCount, AtomicInteger errorCount,
                                  AtomicInteger totalLines) {
        Path relative = sourceBase.relativize(file);
        String fileName = relative.toString();

        try {
            int lineCount = countLines(file);

            String relStr = relative.toString();
            int dot = relStr.lastIndexOf('.');
            String jsonPath = (dot > 0 ? relStr.substring(0, dot) : relStr) + ".json";
            Path output = analysisBase.resolve(jsonPath);
            Files.createDirectories(output.getParent());

            ParseProgressTracker tracker = new ParseProgressTracker(callback, fileName);

            callback.message(String.format("📄 [%d/%d] %s 파싱 시작... (%s, %,d라인)",
                    fileIndex, totalFiles, fileName, strategy.getSupportedTargetType(), lineCount));

            strategy.parseFileWithStream(file.toFile(), output.toString(), tracker);

            callback.message(String.format("✅ [%d/%d] %s 완료 (%,d라인)",
                    fileIndex, totalFiles, fileName, lineCount));

            successCount.incrementAndGet();
            totalLines.addAndGet(lineCount);
            log.info("  [PARSED] {} ({})", relative, strategy.getSupportedTargetType());

        } catch (Throwable e) {
            errorCount.incrementAndGet();
            callback.error(String.format("❌ [%d/%d] %s 파싱 실패: %s",
                    fileIndex, totalFiles, fileName, e.getMessage()));
            log.error("  [PARSE ERROR] {} - {}", relative, e.getMessage(), e);
        }
    }

    /** 구조화된 감지 결과({target, strategy, sqlDialect, targets})를 'detected' 이벤트로 스트림에 전달. */
    private void emitDetected(StreamCallback callback, DetectionResult detection) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("target", detection.primaryTarget());
        payload.put("strategy", detection.strategy());
        payload.put("sqlDialect", detection.sqlDialect());
        payload.put("targets", detection.detectedTargets());
        try {
            callback.send("detected", objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            log.warn("detected 이벤트 직렬화 실패: {}", e.getMessage());
        }
    }

    private int countLines(Path file) {
        try {
            return (int) Files.lines(file, StandardCharsets.UTF_8).count();
        } catch (Exception e) {
            try {
                return (int) Files.lines(file, Charset.forName("EUC-KR")).count();
            } catch (Exception e2) {
                return 0;
            }
        }
    }
}
