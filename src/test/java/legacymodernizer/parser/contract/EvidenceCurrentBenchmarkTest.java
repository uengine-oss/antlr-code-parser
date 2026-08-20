package legacymodernizer.parser.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.intake.SourceIntakeClassifier;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.parsing.languages.c.CLanguageModule;
import legacymodernizer.parser.service.ParseProgressTracker;

/** Opt-in like-for-like benchmark against the isolated pre-evidence baseline. */
class EvidenceCurrentBenchmarkTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    void measureCurrentParserOnActualCorpus() throws Exception {
        String configuredCorpus = System.getProperty("parser.evidence.corpus", "");
        String configuredReport = System.getProperty("parser.evidence.report", "");
        Assumptions.assumeTrue(!configuredCorpus.isBlank() && !configuredReport.isBlank(),
                "Set parser.evidence.corpus and parser.evidence.report for actual benchmarking");

        Path corpus = Path.of(configuredCorpus).toAbsolutePath().normalize();
        Path reportPath = Path.of(configuredReport).toAbsolutePath().normalize();
        assertTrue(reportPath.toString().replace('\\', '/').contains(
                "/specs/131-cross-node-semantic-grounding/_runs/framework/"));

        ParserWorkspace workspace = new ParserWorkspace(new SourceIntakeClassifier());
        workspace.intakeFromPath(corpus);
        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        List<Path> sources;
        try (var walk = Files.walk(workspace.sourceDir())) {
            sources = walk.filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".c")
                            || path.toString().toLowerCase().endsWith(".h"))
                    .sorted().toList();
        }
        assertEquals(23, sources.size());

        ManagementFactory.getMemoryPoolMXBeans().forEach(pool -> {
            if (pool.isValid()) pool.resetPeakUsage();
        });
        long started = System.nanoTime();
        long firstElapsed = 0;
        long repeatedElapsed = 0;
        long legacyCalls = 0;
        long evidenceCalls = 0;
        long outputBytes = 0;
        ArrayNode files = JSON.createArrayNode();
        for (Path source : sources) {
            RawParseResult first = module.parseFile(source.toFile(),
                    new ParseProgressTracker(null, source.getFileName().toString()));
            RawParseResult repeated = module.parseFile(source.toFile(),
                    new ParseProgressTracker(null, source.getFileName().toString()));
            assertEquals(first.astJson(), repeated.astJson());
            JsonNode root = JSON.readTree(first.astJson());
            long fileLegacyCalls = countLegacy(root, "FUNCTION_CALL");
            long fileEvidenceCalls = countEvidence(root, "call");
            legacyCalls += fileLegacyCalls;
            evidenceCalls += fileEvidenceCalls;
            outputBytes += first.astJson().getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
            firstElapsed += first.elapsedMillis();
            repeatedElapsed += repeated.elapsedMillis();
            ObjectNode row = files.addObject();
            row.put("source", workspace.sourceDir().relativize(source)
                    .toString().replace('\\', '/'));
            row.put("legacyCalls", fileLegacyCalls);
            row.put("evidenceCalls", fileEvidenceCalls);
            row.put("outputBytes", first.astJson()
                    .getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
            row.put("firstParseElapsedMillis", first.elapsedMillis());
            row.put("repeatedParseElapsedMillis", repeated.elapsedMillis());
        }
        ObjectNode report = JSON.createObjectNode();
        report.put("sourceFiles", sources.size());
        report.put("legacyFunctionCallNodes", legacyCalls);
        report.put("callFacts", evidenceCalls);
        report.put("outputBytes", outputBytes);
        report.put("firstPassSumMillis", firstElapsed);
        report.put("repeatedPassSumMillis", repeatedElapsed);
        report.put("wallMillisTwoPass", (System.nanoTime() - started) / 1_000_000L);
        report.put("peakHeapBytes", ManagementFactory.getMemoryPoolMXBeans().stream()
                .filter(pool -> pool.getType() == MemoryType.HEAP)
                .mapToLong(pool -> pool.getPeakUsage().getUsed()).sum());
        report.set("files", files);
        Files.createDirectories(reportPath.getParent());
        new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                .writeValue(reportPath.toFile(), report);
    }

    private static long countLegacy(JsonNode node, String type) {
        long result = type.equals(node.path("type").asText()) ? 1 : 0;
        for (JsonNode child : node.path("children")) result += countLegacy(child, type);
        return result;
    }

    private static long countEvidence(JsonNode root, String kind) {
        long result = 0;
        for (JsonNode fact : root.path("evidence").path("facts")) {
            if (kind.equals(fact.path("kind").asText())) result++;
        }
        return result;
    }
}
