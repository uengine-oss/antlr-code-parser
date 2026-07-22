package legacymodernizer.parser.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryType;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import legacymodernizer.parser.recovery.workingcopy.Hashes;
import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.parsing.ParseOrchestrator;

@SpringBootTest
class FullCorpusRecoveryTest {

    @Autowired private ParseOrchestrator orchestrator;
    @Autowired private ParserWorkspace storage;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void parsesConfiguredCorpusWithoutChangingOriginal() throws Exception {
        String configured = System.getProperty("parser.full.corpus");
        Assumptions.assumeTrue(configured != null && !configured.isBlank());
        Path corpus = Path.of(configured).toAbsolutePath().normalize();
        Assumptions.assumeTrue(Files.isDirectory(corpus));
        assertTrue(storage.analysisDir().toString().replace('\\', '/').contains("/target/test-data/"));

        String originalBefore = inventoryHash(corpus);
        ManagementFactory.getMemoryPoolMXBeans().stream()
                .filter(pool -> pool.getType() == MemoryType.HEAP)
                .forEach(pool -> {
                    try {
                        pool.resetPeakUsage();
                    } catch (UnsupportedOperationException ignored) {
                        // A JVM may expose a read-only peak counter.
                    }
                });
        Instant started = Instant.now();
        ParserWorkspace.IntakeResult intake = storage.intakeFromPath(corpus);
        List<String> errors = new ArrayList<>();
        orchestrator.parse(null, event -> {
            if ("error".equals(event.type())) errors.add(event.content());
        });
        long elapsedMillis = Duration.between(started, Instant.now()).toMillis();
        String originalAfter = inventoryHash(corpus);
        assertEquals(originalBefore, originalAfter, "The original corpus changed");
        long peakHeapBytes = ManagementFactory.getMemoryPoolMXBeans().stream()
                .filter(pool -> pool.getType() == MemoryType.HEAP)
                .mapToLong(pool -> Math.max(0, pool.getPeakUsage().getUsed()))
                .sum();

        Map<String, Map<String, Object>> languages = new LinkedHashMap<>();
        for (String language : List.of("java", "python", "c", "oracle", "postgresql")) {
            languages.put(language, newLanguageMetrics());
        }
        int diagnosticsFiles = 0;
        int actualAgentCalls = 0;
        int agentSkips = 0;
        int sourceLines = 0;
        int lexerErrors = 0;
        int parserErrors = 0;
        int antlrRecoveries = 0;
        long firstPassElapsedMillis = 0;
        long processingElapsedMillis = 0;
        long exactFirstPassElapsedMillis = 0;
        long exactProcessingElapsedMillis = 0;
        Map<String, Integer> unitStatuses = new LinkedHashMap<>();
        List<Map<String, Object>> unresolvedUnits = new ArrayList<>();
        if (Files.isDirectory(storage.diagnosticsDir())) {
            try (var files = Files.walk(storage.diagnosticsDir())) {
                for (Path path : files.filter(Files::isRegularFile)
                        .filter(value -> value.toString().endsWith(".parse.json")).toList()) {
                    diagnosticsFiles++;
                    JsonNode sidecar = mapper.readTree(path.toFile());
                    String language = sidecar.path("language").asText("unknown");
                    String status = sidecar.path("status").asText("UNKNOWN");
                    Map<String, Object> metrics = languages.computeIfAbsent(language,
                            ignored -> newLanguageMetrics());
                    merge(metrics, "files", 1);
                    statuses(metrics).merge(status, 1, Integer::sum);

                    Path source = storage.sourceDir().resolve(sidecar.path("sourcePath").asText());
                    int lines = Files.isRegularFile(source) ? countLines(source) : 0;
                    int fileLexerErrors = sidecar.path("summary").path("lexerErrors").asInt();
                    int fileParserErrors = sidecar.path("summary").path("parserErrors").asInt();
                    int fileRecoveries = sidecar.path("summary").path("antlrRecoveries").asInt();
                    long fileFirstPassElapsedMillis = sidecar.path("summary")
                            .path("elapsedMillis").asLong();
                    long fileProcessingElapsedMillis = sidecar.path("summary")
                            .path("processingElapsedMillis").asLong(fileFirstPassElapsedMillis);
                    sourceLines += lines;
                    lexerErrors += fileLexerErrors;
                    parserErrors += fileParserErrors;
                    antlrRecoveries += fileRecoveries;
                    firstPassElapsedMillis += fileFirstPassElapsedMillis;
                    processingElapsedMillis += fileProcessingElapsedMillis;
                    if ("EXACT".equals(status)) {
                        exactFirstPassElapsedMillis += fileFirstPassElapsedMillis;
                        exactProcessingElapsedMillis += fileProcessingElapsedMillis;
                    }
                    merge(metrics, "lines", lines);
                    merge(metrics, "lexerErrors", fileLexerErrors);
                    merge(metrics, "parserErrors", fileParserErrors);
                    merge(metrics, "antlrRecoveries", fileRecoveries);
                    merge(metrics, "declarationsDiscovered",
                            sidecar.path("summary").path("declarationsDiscovered").asInt());
                    merge(metrics, "declarationsEmitted",
                            sidecar.path("summary").path("declarationsEmitted").asInt());
                    mergeLong(metrics, "firstPassElapsedMillis", fileFirstPassElapsedMillis);
                    mergeLong(metrics, "processingElapsedMillis", fileProcessingElapsedMillis);
                    if ("EXACT".equals(status)) {
                        mergeLong(metrics, "exactFirstPassElapsedMillis",
                                fileFirstPassElapsedMillis);
                        mergeLong(metrics, "exactProcessingElapsedMillis",
                                fileProcessingElapsedMillis);
                    }

                    for (JsonNode unit : sidecar.path("units")) {
                        String unitStatus = unit.path("status").asText("UNKNOWN");
                        unitStatuses.merge(unitStatus, 1, Integer::sum);
                        unitStatuses(metrics).merge(unitStatus, 1, Integer::sum);
                        for (JsonNode attempt : unit.path("attempts")) {
                            String stage = attempt.path("stage").asText();
                            if ("REPAIR_AGENT".equals(stage)) {
                                actualAgentCalls++;
                                merge(metrics, "actualAgentCalls", 1);
                            } else if ("REPAIR_AGENT_SKIPPED".equals(stage)) {
                                agentSkips++;
                                merge(metrics, "agentSkips", 1);
                            }
                        }
                        if (!unit.path("accepted").asBoolean()) {
                            unresolvedUnits.add(unresolved(sidecar, unit));
                        }
                    }
                }
            }
        }
        long astFiles = countJson(storage.analysisDir());
        long repairsJsonFiles = countJson(storage.repairsDir());
        long repairFiles = countJsonWithSuffix(storage.repairsDir(), ".repair.json");
        long promotionReportFiles = Files.isRegularFile(storage.repairsDir()
                .resolve("review/promotion-candidates.json")) ? 1 : 0;
        long exactPathOverheadMillis = Math.max(0,
                exactProcessingElapsedMillis - exactFirstPassElapsedMillis);
        Double exactPathOverheadPercent = overheadPercent(
                exactPathOverheadMillis, exactFirstPassElapsedMillis);
        for (Map<String, Object> metrics : languages.values()) {
            addExactPathPerformance(metrics);
        }
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("schemaVersion", "2.1.0");
        report.put("corpus", corpus.toString());
        report.put("originalInventorySha256Before", originalBefore);
        report.put("originalInventorySha256After", originalAfter);
        report.put("originalCorpusUnchanged", originalBefore.equals(originalAfter));
        report.put("intakeDdlFiles", intake.ddlCount());
        report.put("intakeSourceFiles", intake.sourceCount());
        report.put("skippedFiles", intake.skipped().size());
        report.put("detectedParserFiles", diagnosticsFiles);
        report.put("unsupportedIntakeFiles",
                intake.ddlCount() + intake.sourceCount() - diagnosticsFiles);
        report.put("sourceLines", sourceLines);
        report.put("astFiles", astFiles);
        report.put("diagnosticsFiles", diagnosticsFiles);
        report.put("repairFiles", repairFiles);
        report.put("promotionReportFiles", promotionReportFiles);
        report.put("repairsJsonFiles", repairsJsonFiles);
        report.put("lexerErrors", lexerErrors);
        report.put("parserErrors", parserErrors);
        report.put("antlrRecoveries", antlrRecoveries);
        report.put("actualAgentCalls", actualAgentCalls);
        report.put("agentSkips", agentSkips);
        report.put("unitStatuses", unitStatuses);
        report.put("unresolvedUnits", unresolvedUnits);
        report.put("callbackErrors", errors.size());
        report.put("callbackErrorMessages", errors);
        report.put("elapsedMillis", elapsedMillis);
        report.put("firstPassElapsedMillis", firstPassElapsedMillis);
        report.put("processingElapsedMillis", processingElapsedMillis);
        report.put("exactFirstPassElapsedMillis", exactFirstPassElapsedMillis);
        report.put("exactProcessingElapsedMillis", exactProcessingElapsedMillis);
        report.put("exactPathOverheadMillis", exactPathOverheadMillis);
        report.put("exactPathOverheadPercent", exactPathOverheadPercent);
        report.put("exactPathOverheadTargetPercent", 20.0);
        report.put("exactPathOverheadWithinTarget",
                exactPathOverheadPercent == null ? null : exactPathOverheadPercent < 20.0);
        report.put("peakHeapBytes", peakHeapBytes);
        report.put("sourceInventorySha256", inventoryHash(storage.sourceDir()));
        report.put("astInventorySha256", inventoryHash(storage.analysisDir()));
        report.put("diagnosticsSemanticInventorySha256",
                semanticJsonInventoryHash(storage.diagnosticsDir()));
        report.put("repairsSemanticInventorySha256",
                semanticJsonInventoryHash(storage.repairsDir()));
        report.put("languages", languages);
        String reportName = System.getProperty("parser.full.report.name", "full-corpus-summary.json");
        Path output = Path.of("target", "corpus-reports", reportName);
        Files.createDirectories(output.getParent());
        Files.writeString(output, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(report),
                StandardCharsets.UTF_8);
        assertTrue(diagnosticsFiles > 0);
        assertTrue(astFiles > 0);
    }

    private Map<String, Object> unresolved(JsonNode sidecar, JsonNode unit) {
        JsonNode sourceUnit = unit.path("unit");
        JsonNode attempts = unit.path("attempts");
        JsonNode lastAttempt = attempts.isArray() && !attempts.isEmpty()
                ? attempts.get(attempts.size() - 1) : mapper.createObjectNode();
        List<Map<String, Object>> diagnostics = new ArrayList<>();
        for (JsonNode diagnostic : lastAttempt.path("diagnostics")) {
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("phase", diagnostic.path("phase").asText());
            value.put("code", diagnostic.path("code").asText());
            value.put("line", diagnostic.path("line").asInt());
            value.put("column", diagnostic.path("column").asInt());
            value.put("message", diagnostic.path("message").asText());
            diagnostics.add(value);
        }
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("sourcePath", sidecar.path("sourcePath").asText());
        value.put("language", sidecar.path("language").asText());
        value.put("unitId", sourceUnit.path("unitId").asText());
        value.put("kind", sourceUnit.path("kind").asText());
        value.put("name", sourceUnit.path("name").isMissingNode()
                ? null : sourceUnit.path("name").asText(null));
        value.put("startLine", sourceUnit.path("startLine").asInt());
        value.put("endLine", sourceUnit.path("endLine").asInt());
        value.put("status", unit.path("status").asText());
        value.put("finalStage", lastAttempt.path("stage").asText());
        value.put("reasons", mapper.convertValue(lastAttempt.path("qualityReasons"), List.class));
        value.put("diagnostics", diagnostics);
        return value;
    }

    private static Map<String, Object> newLanguageMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        for (String field : List.of("files", "lines", "lexerErrors", "parserErrors",
                "antlrRecoveries", "declarationsDiscovered", "declarationsEmitted",
                "actualAgentCalls", "agentSkips")) {
            metrics.put(field, 0);
        }
        for (String field : List.of("firstPassElapsedMillis", "processingElapsedMillis",
                "exactFirstPassElapsedMillis", "exactProcessingElapsedMillis")) {
            metrics.put(field, 0L);
        }
        metrics.put("statuses", new LinkedHashMap<String, Integer>());
        metrics.put("unitStatuses", new LinkedHashMap<String, Integer>());
        return metrics;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Integer> statuses(Map<String, Object> metrics) {
        return (Map<String, Integer>) metrics.get("statuses");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Integer> unitStatuses(Map<String, Object> metrics) {
        return (Map<String, Integer>) metrics.get("unitStatuses");
    }

    private static void merge(Map<String, Object> metrics, String field, int value) {
        metrics.put(field, ((Number) metrics.getOrDefault(field, 0)).intValue() + value);
    }

    private static void mergeLong(Map<String, Object> metrics, String field, long value) {
        metrics.put(field, ((Number) metrics.getOrDefault(field, 0L)).longValue() + value);
    }

    private static void addExactPathPerformance(Map<String, Object> metrics) {
        long firstPass = ((Number) metrics.get("exactFirstPassElapsedMillis")).longValue();
        long processing = ((Number) metrics.get("exactProcessingElapsedMillis")).longValue();
        long overhead = Math.max(0, processing - firstPass);
        Double percent = overheadPercent(overhead, firstPass);
        metrics.put("exactPathOverheadMillis", overhead);
        metrics.put("exactPathOverheadPercent", percent);
        metrics.put("exactPathOverheadWithinTarget", percent == null ? null : percent < 20.0);
    }

    private static Double overheadPercent(long overheadMillis, long baselineMillis) {
        if (baselineMillis <= 0) return null;
        return Math.round((overheadMillis * 100_000.0) / baselineMillis) / 1_000.0;
    }

    private static int countLines(Path file) throws Exception {
        byte[] bytes = Files.readAllBytes(file);
        String text = null;
        for (String charset : List.of("UTF-8", "EUC-KR", "MS949")) {
            try {
                text = Charset.forName(charset).newDecoder()
                        .decode(java.nio.ByteBuffer.wrap(bytes)).toString();
                break;
            } catch (Exception ignored) {
                // Try the next legacy charset.
            }
        }
        if (text == null) text = new String(bytes, StandardCharsets.UTF_8);
        if (text.isEmpty()) return 0;
        int lines = 1;
        for (int index = 0; index < text.length(); index++) {
            if (text.charAt(index) == '\n') lines++;
        }
        return lines;
    }

    private static long countJson(Path root) throws Exception {
        if (!Files.isDirectory(root)) return 0;
        try (var files = Files.walk(root)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json")).count();
        }
    }

    private static long countJsonWithSuffix(Path root, String suffix) throws Exception {
        if (!Files.isDirectory(root)) return 0;
        try (var files = Files.walk(root)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(suffix)).count();
        }
    }

    private static String inventoryHash(Path root) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (var files = Files.walk(root)) {
            for (Path path : files.filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(value -> root.relativize(value).toString())).toList()) {
                digest.update(root.relativize(path).toString().replace('\\', '/').getBytes(StandardCharsets.UTF_8));
                digest.update((byte) 0);
                digest.update(Hashes.sha256(Files.readAllBytes(path)).getBytes(StandardCharsets.US_ASCII));
                digest.update((byte) '\n');
            }
        }
        return java.util.HexFormat.of().formatHex(digest.digest());
    }

    private String semanticJsonInventoryHash(Path root) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        if (!Files.isDirectory(root)) return java.util.HexFormat.of().formatHex(digest.digest());
        try (var files = Files.walk(root)) {
            for (Path path : files.filter(Files::isRegularFile)
                    .filter(value -> value.toString().endsWith(".json"))
                    .sorted(Comparator.comparing(value -> root.relativize(value).toString())).toList()) {
                digest.update(root.relativize(path).toString().replace('\\', '/')
                        .getBytes(StandardCharsets.UTF_8));
                digest.update((byte) 0);
                JsonNode json = mapper.readTree(path.toFile());
                removeElapsedMillis(json);
                digest.update(mapper.writeValueAsBytes(json));
                digest.update((byte) '\n');
            }
        }
        return java.util.HexFormat.of().formatHex(digest.digest());
    }

    private static void removeElapsedMillis(JsonNode node) {
        if (node == null) return;
        if (node.isObject()) {
            ((com.fasterxml.jackson.databind.node.ObjectNode) node).remove("elapsedMillis");
            ((com.fasterxml.jackson.databind.node.ObjectNode) node)
                    .remove("processingElapsedMillis");
            node.elements().forEachRemaining(FullCorpusRecoveryTest::removeElapsedMillis);
        } else if (node.isArray()) {
            node.forEach(FullCorpusRecoveryTest::removeElapsedMillis);
        }
    }
}
