package legacymodernizer.parser.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.intake.SourceIntakeClassifier;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.parsing.languages.c.CLanguageModule;
import legacymodernizer.parser.service.ParseProgressTracker;

class CStructuralExpressionIr2ActualSliceTest {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Set<String> FORBIDDEN_SEMANTIC_FIELDS = Set.of(
            "binding", "type", "linkage", "meaning", "valueKind",
            "operatorMeaning", "resolvedTarget", "resolutionStatus");

    @Test
    void sealedSktFileHasClosedStructuralExpressionLedger() throws Exception {
        String configuredPath = System.getProperty("c081.actual.file", "");
        String expectedHash = System.getProperty("c081.actual.sha256", "");
        String outputPath = System.getProperty("c081.actual.output", "");
        Assumptions.assumeTrue(
                !configuredPath.isBlank() && !expectedHash.isBlank() && !outputPath.isBlank(),
                "run only for an explicitly sealed C-081 actual slice");

        Path source = Path.of(configuredPath);
        byte[] bytes = Files.readAllBytes(source);
        assertEquals(expectedHash, sha256(bytes));

        ParserWorkspace workspace = new ParserWorkspace(new SourceIntakeClassifier());
        Path canonical = workspace.sourceDir().resolve(source.getFileName().toString());
        Files.createDirectories(canonical.getParent());
        Files.copy(source, canonical, StandardCopyOption.REPLACE_EXISTING);
        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        RawParseResult result = module.parseFile(canonical.toFile(),
                new ParseProgressTracker(null, canonical.getFileName().toString()));
        JsonNode root = JSON.readTree(result.astJson());

        int facts = 0;
        Set<String> ids = new HashSet<>();
        for (JsonNode fact : root.path("evidence").path("facts")) {
            if (!"structural_expression".equals(fact.path("kind").asText())) continue;
            facts++;
            assertTrue(ids.add(fact.path("factId").asText()));
            JsonNode payload = fact.path("payload");
            assertTrue(payload.path("syntax").path("root").isObject());
            FORBIDDEN_SEMANTIC_FIELDS.forEach(field -> assertFalse(payload.has(field)));
        }
        JsonNode completeness = completeness(root, "structural_expression");
        assertEquals("complete", completeness.path("status").asText());
        assertEquals(facts, completeness.path("population").asInt());
        assertEquals(facts, completeness.path("emitted").asInt());
        assertEquals(0, completeness.path("explicitlyUnresolved").asInt());
        assertTrue(facts > 0);

        Path output = Path.of(outputPath);
        Files.createDirectories(output.getParent());
        Files.writeString(output, result.astJson());
        System.out.printf(
                "C081_SKT sha256=%s expressions=%d artifactSha256=%s%n",
                expectedHash, facts, sha256(Files.readAllBytes(output)));
    }

    private static JsonNode completeness(JsonNode root, String kind) {
        JsonNode match = null;
        for (JsonNode row : root.path("evidence").path("completeness")) {
            if (kind.equals(row.path("kind").asText())) {
                assertTrue(match == null, "duplicate completeness row for " + kind);
                match = row;
            }
        }
        assertTrue(match != null, "missing completeness row for " + kind);
        return match;
    }

    private static String sha256(byte[] bytes) throws Exception {
        return java.util.HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(bytes));
    }
}
