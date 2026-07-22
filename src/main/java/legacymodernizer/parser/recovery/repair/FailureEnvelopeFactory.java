package legacymodernizer.parser.recovery.repair;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import legacymodernizer.parser.recovery.quality.DeclarationCoverage;
import legacymodernizer.parser.recovery.diagnostics.ParseDiagnostic;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.recovery.evidence.RecoveryAttemptEvidence;
import legacymodernizer.parser.recovery.localization.ContextSlice;
import legacymodernizer.parser.recovery.localization.ErrorSpanLocator;
import legacymodernizer.parser.recovery.boundaries.SourceUnit;
import legacymodernizer.parser.recovery.workingcopy.Hashes;

import java.nio.charset.StandardCharsets;

/**
 * Builds the Agent request from a Parser-chosen {@link ContextSlice}. The excerpt is always the
 * slice text — full-unit transmission was removed with envelope schema 2.0.0 (spec 012).
 */
public final class FailureEnvelopeFactory {

    public static final String SCHEMA_VERSION = "2.0.0";
    private static final List<String> FORBIDDEN = List.of(
            "AST", "PARSE_TREE", "NODE_JSON", "FULL_FILE_REWRITE", "FULL_UNIT_REWRITE");
    private static final Pattern SOURCE_TOKEN =
            legacymodernizer.parser.recovery.SourceTokens.PATTERN;
    // 수정 상한 = excerpt의 1/4: 수리는 국소 편집이라는 계약을 수치로 강제하되(전면 재작성
    // 차단), 짧은 슬라이스에서도 토큰 몇 개는 고칠 수 있게 하한을 둔다.
    private static final int MAX_CHANGED_CHARACTERS_CAP = 16_384;
    private static final int MIN_CHANGED_CHARACTERS = 64;
    private static final int MAX_CHANGED_LINES_CAP = 256;
    private static final int MIN_CHANGED_LINES = 8;
    // 진단 주변 ±96자·토큰 256개 창: 모델이 offset을 세지 않고 좌표를 베끼게 하는 근거
    // 자료의 상한 — 프롬프트 크기와 유용성의 절충값(spec 012 실측 기반).
    private static final int DIAGNOSTIC_WINDOW_RADIUS = 96;
    private static final int DIAGNOSTIC_WINDOW_TOKEN_CAP = 256;

    private final ObjectMapper canonicalMapper = new ObjectMapper()
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
    private final ErrorSpanLocator locator = new ErrorSpanLocator();

    public FailureEnvelope create(String language, String fileSha256, SourceUnit unit,
                                  String unitText, ContextSlice slice, RawParseResult parse,
                                  List<RecoveryAttemptEvidence> attempts,
                                  int remainingAttempts) {
        if (unitText == null || unitText.isEmpty()) {
            throw new IllegalArgumentException("REPAIR_UNIT_EMPTY");
        }
        if (slice == null || slice.text().isEmpty()) {
            throw new IllegalArgumentException("REPAIR_SLICE_EMPTY");
        }
        List<DiagnosticEvidence> diagnostics = diagnosticEvidence(
                unitText, slice, unit.startLine(), parse.diagnostics());
        DeclarationCoverage coverage = parse.coverage();
        if (diagnostics.isEmpty()) {
            diagnostics = new ArrayList<>();
            diagnostics.add(new DiagnosticEvidence(legacymodernizer.parser.recovery.diagnostics.DiagnosticPhase.COVERAGE.name(), "INCOMPLETE_DECLARATION_COVERAGE",
                    "Declaration coverage did not pass the Parser quality gate", 0, 0,
                    0, 0, null, null, List.of(), ""));
        }
        CoverageEvidence coverageEvidence = new CoverageEvidence(
                Math.max(0, coverage.declarationsDiscovered()),
                Math.max(0, coverage.declarationsEmitted()),
                List.copyOf(coverage.missingDeclarations()), coverage.isKnownAndComplete());
        List<PriorAttempt> priorAttempts = attempts.stream()
                .filter(attempt -> "REPAIR_AGENT".equals(attempt.stage())
                        || !attempt.edits().isEmpty())
                .map(attempt -> new PriorAttempt(attempt.stage(), attempt.attemptNumber(),
                        attempt.workingSha256(), List.copyOf(attempt.edits()),
                        attempt.edits().isEmpty() ? List.of()
                                : diagnosticEvidence(unitText, slice, unit.startLine(),
                                        attempt.diagnostics()),
                        attempt.qualityTuple(),
                        List.copyOf(attempt.qualityReasons())))
                .toList();
        String excerpt = slice.text();
        RepairConstraints constraints = new RepairConstraints(0, excerpt.length(),
                Math.max(1, Math.min(MAX_CHANGED_CHARACTERS_CAP,
                        Math.max(MIN_CHANGED_CHARACTERS, excerpt.length() / 4))),
                Math.max(1, Math.min(MAX_CHANGED_LINES_CAP,
                        Math.max(MIN_CHANGED_LINES, countLines(excerpt) / 4))),
                Math.max(1, Math.min(3, remainingAttempts)), FORBIDDEN);
        FailureEnvelope unhashed = new FailureEnvelope(SCHEMA_VERSION, "", language,
                parse.grammarRevision(), fileSha256,
                Hashes.sha256(unitText.getBytes(StandardCharsets.UTF_8)), unit.unitId(),
                unit.kind().name(), unit.startOffset(), unit.endOffset(), unit.startLine(),
                unit.endLine(), slice.level().name(), slice.unitStartOffset(),
                slice.unitEndOffset(), unit.startOffset() + slice.unitStartOffset(),
                unit.startOffset() + slice.unitEndOffset(), slice.sliceSha256(),
                slice.headerText(), List.copyOf(diagnostics), coverageEvidence, priorAttempts,
                contextHeader(unit), excerpt, lineStartOffsets(excerpt),
                diagnosticWindowTokens(excerpt, diagnostics), constraints);
        return unhashed.withHash(checksum(unhashed));
    }

    /**
     * Projects diagnostics into excerpt-relative offsets, keeping only those that fall inside
     * the slice — out-of-slice diagnostics belong to another repair group.
     */
    private List<DiagnosticEvidence> diagnosticEvidence(
            String unitText, ContextSlice slice, int unitStartLine,
            List<ParseDiagnostic> parseDiagnostics) {
        List<DiagnosticEvidence> diagnostics = new ArrayList<>();
        for (ParseDiagnostic diagnostic : parseDiagnostics) {
            int unitOffset = locator.anchorOffset(unitText, unitStartLine,
                    diagnostic.line(), diagnostic.column());
            if (unitOffset < slice.unitStartOffset() || unitOffset >= slice.unitEndOffset()) {
                continue;
            }
            int excerptStart = unitOffset - slice.unitStartOffset();
            int excerptEnd = Math.min(slice.length(), excerptStart
                    + Math.max(1, diagnostic.offendingToken() == null
                            ? 1 : diagnostic.offendingToken().length()));
            diagnostics.add(new DiagnosticEvidence(diagnostic.phase().name(), diagnostic.code(),
                    diagnostic.message(), Math.max(0, diagnostic.line()),
                    Math.max(0, diagnostic.column()), excerptStart, excerptEnd,
                    diagnostic.offendingToken(), diagnostic.expectedTokens(),
                    diagnostic.ruleStack(), diagnostic.tokenWindow()));
        }
        return List.copyOf(diagnostics);
    }

    private static List<Integer> lineStartOffsets(String text) {
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);
        for (int index = 0; index < text.length(); index++) {
            if (text.charAt(index) == '\n' && index + 1 < text.length()) offsets.add(index + 1);
        }
        return List.copyOf(offsets);
    }

    private static List<SourceTokenEvidence> diagnosticWindowTokens(
            String text, List<DiagnosticEvidence> diagnostics) {
        Map<Integer, SourceTokenEvidence> tokens = new LinkedHashMap<>();
        Matcher matcher = SOURCE_TOKEN.matcher(text);
        for (DiagnosticEvidence diagnostic : diagnostics) {
            int start = Math.max(0, diagnostic.excerptStartOffset() - DIAGNOSTIC_WINDOW_RADIUS);
            int end = Math.min(text.length(),
                    diagnostic.excerptEndOffset() + DIAGNOSTIC_WINDOW_RADIUS);
            matcher.region(start, end);
            while (matcher.find() && tokens.size() < DIAGNOSTIC_WINDOW_TOKEN_CAP) {
                tokens.putIfAbsent(matcher.start(), new SourceTokenEvidence(
                        matcher.start(), matcher.end(), matcher.group()));
            }
        }
        return List.copyOf(tokens.values());
    }


    public String checksum(FailureEnvelope envelope) {
        try {
            ObjectNode payload = canonicalMapper.valueToTree(envelope);
            payload.remove("failureEnvelopeHash");
            return Hashes.sha256(canonicalMapper.writeValueAsBytes(payload));
        } catch (Exception error) {
            throw new IllegalStateException("FAILURE_ENVELOPE_SERIALIZATION_FAILED", error);
        }
    }

    private static String contextHeader(SourceUnit unit) {
        String parent = unit.parentUnitId() == null ? "" : "; parent=" + unit.parentUnitId();
        return "unit=" + unit.unitId() + "; kind=" + unit.kind().name()
                + "; name=" + unit.name() + parent;
    }

    private static int countLines(String text) {
        return (int) text.chars().filter(character -> character == '\n').count() + 1;
    }
}
