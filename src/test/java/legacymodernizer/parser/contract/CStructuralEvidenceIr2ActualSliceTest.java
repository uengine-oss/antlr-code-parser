package legacymodernizer.parser.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

class CStructuralEvidenceIr2ActualSliceTest {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Set<String> FORBIDDEN_FIELDS = Set.of(
            "binding", "adapterSchema", "bindingKey", "compatibilityKey",
            "compatibilityStatus", "compatibilityScope", "definitionStatus",
            "targetScope", "resolutionMode", "externalIdentityKey");

    @Test
    void sealedActualCFileHasClosedStructuralLedger() throws Exception {
        String configuredPath = System.getProperty("c080.actual.file", "");
        String expectedHash = System.getProperty("c080.actual.sha256", "");
        Assumptions.assumeTrue(!configuredPath.isBlank() && !expectedHash.isBlank(),
                "run only for an explicitly sealed C-080 actual slice");

        Path file = Path.of(configuredPath);
        byte[] bytes = Files.readAllBytes(file);
        assertEquals(expectedHash, sha256(bytes));
        String source = Files.readString(file);

        ParserWorkspace workspace = new ParserWorkspace(new SourceIntakeClassifier());
        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        RawParseResult result = module.parseFile(file.toFile(),
                new ParseProgressTracker(null, file.getFileName().toString()));
        JsonNode root = JSON.readTree(result.astJson());
        JsonNode evidence = root.path("evidence");

        assertEquals("2.0.0", evidence.path("version").asText());
        assertEquals("c", evidence.path("language").asText());
        assertEquals("antlr-c/v1", evidence.path("frontendSchema").asText());
        Set<String> factIds = new HashSet<>();
        List<JsonNode> calls = facts(root, "call");
        List<JsonNode> callables = facts(root, "callable");
        evidence.path("facts").forEach(fact -> assertTrue(
                factIds.add(fact.path("factId").asText()), "duplicate factId"));
        calls.forEach(fact -> {
            assertExactSlice(source, fact.path("range"));
            assertScopePathContains(fact.path("payload").path("scopePath"), fact.path("range"));
            FORBIDDEN_FIELDS.forEach(field -> assertFalse(fact.path("payload").has(field)));
        });
        callables.forEach(fact -> {
            assertExactSlice(source, fact.path("range"));
            assertScopePathContains(fact.path("payload").path("scopePath"), fact.path("range"));
            assertTrue(fact.path("payload").path("syntax").path("declarator").isObject());
            FORBIDDEN_FIELDS.forEach(field -> assertFalse(fact.path("payload").has(field)));
        });
        assertCompletePopulation(completeness(root, "call"), calls.size());
        assertCompletePopulation(completeness(root, "callable"), callables.size());
        assertFalse(hasCompleteness(root, "call_binding"));

        System.out.printf(
                "C080_ACTUAL sha256=%s calls=%d callables=%d firstCall=%s firstCallable=%s%n",
                expectedHash,
                calls.size(),
                callables.size(),
                coordinate(calls),
                coordinate(callables));
    }

    private static List<JsonNode> facts(JsonNode root, String kind) {
        List<JsonNode> result = new ArrayList<>();
        root.path("evidence").path("facts").forEach(fact -> {
            if (kind.equals(fact.path("kind").asText())) result.add(fact);
        });
        return result;
    }

    private static void assertScopePathContains(JsonNode scopePath, JsonNode factRange) {
        assertTrue(scopePath.isArray() && !scopePath.isEmpty());
        assertEquals("translation_unit", scopePath.path(0).path("kind").asText());
        JsonNode previous = null;
        for (JsonNode scope : scopePath) {
            assertTrue(scope.path("range").path("charLength").asInt() > 0);
            if (previous != null) assertContains(previous.path("range"), scope.path("range"));
            previous = scope;
        }
        assertContains(previous.path("range"), factRange);
    }

    private static void assertContains(JsonNode outer, JsonNode inner) {
        int outerStart = outer.path("charOffset").asInt();
        int outerEnd = outerStart + outer.path("charLength").asInt();
        int innerStart = inner.path("charOffset").asInt();
        int innerEnd = innerStart + inner.path("charLength").asInt();
        assertTrue(outerStart <= innerStart && innerEnd <= outerEnd);
    }

    private static void assertExactSlice(String source, JsonNode range) {
        int start = range.path("charOffset").asInt();
        int length = range.path("charLength").asInt();
        assertTrue(start >= 0 && length > 0 && start + length <= source.codePointCount(0, source.length()));
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

    private static boolean hasCompleteness(JsonNode root, String kind) {
        for (JsonNode row : root.path("evidence").path("completeness")) {
            if (kind.equals(row.path("kind").asText())) return true;
        }
        return false;
    }

    private static void assertCompletePopulation(JsonNode row, int population) {
        assertEquals("complete", row.path("status").asText());
        assertEquals(population, row.path("population").asInt());
        assertEquals(population, row.path("emitted").asInt());
        assertEquals(0, row.path("explicitlyUnresolved").asInt());
    }

    private static String coordinate(List<JsonNode> facts) {
        if (facts.isEmpty()) return "none";
        JsonNode range = facts.get(0).path("range");
        return range.path("charOffset").asInt() + ":" + range.path("charLength").asInt();
    }

    private static String sha256(byte[] bytes) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
        StringBuilder encoded = new StringBuilder(digest.length * 2);
        for (byte value : digest) encoded.append(String.format("%02x", value));
        return encoded.toString();
    }
}
