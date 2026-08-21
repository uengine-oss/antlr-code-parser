package legacymodernizer.parser.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import legacymodernizer.parser.parsing.languages.LanguageModule;
import legacymodernizer.parser.recovery.quality.QualityDecision;
import legacymodernizer.parser.recovery.quality.QualityStatus;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.recovery.evidence.RecoveryOutcome;
import legacymodernizer.parser.recovery.quality.ParseQualityGate;
import legacymodernizer.parser.recovery.rules.RecoveryRuleRegistry;
import legacymodernizer.parser.recovery.workingcopy.Hashes;
import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.intake.SourceIntakeClassifier;
import legacymodernizer.parser.service.ParseProgressTracker;
import legacymodernizer.parser.parsing.languages.c.CLanguageModule;
import legacymodernizer.parser.parsing.languages.java.JavaLanguageModule;
import legacymodernizer.parser.parsing.languages.postgresql.PostgreSqlLanguageModule;
import legacymodernizer.parser.parsing.languages.python.PythonLanguageModule;

class CrossLanguageMinimalUnitRecoveryTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @BeforeAll
    static void requireIsolatedDataRoot() {
        assertTrue(System.getProperty("parser.data.root", "").replace('\\', '/')
                .contains("/target/test-data"));
    }

    @Test
    void isolatesJavaTopLevelTypes() throws Exception {
        String source = "import java.util.List;\n"
                + "class GoodOne { int value() { return 1; } }\n"
                + "class Broken { int value() { return ; + ; } }\n"
                + "class GoodTwo { int value() { return 2; } }\n";
        assertRecovered("units/java/Three.java", source,
                new JavaLanguageModule(storage()), "CLASS", List.of("GoodOne", "GoodTwo"), true);
    }

    @Test
    void isolatesPythonTopLevelDeclarations() throws Exception {
        String source = "import os\n\n"
                + "def good_one():\n    return 1\n\n"
                + "def broken():\n    return (\n\n"
                + "def good_two():\n    return 2\n";
        assertRecovered("units/python/three.py", source,
                new PythonLanguageModule(storage()), "FUNCTION", List.of("good_one", "good_two"), true);
    }

    @Test
    void isolatesCFunctions() throws Exception {
        String source = "#include <stdio.h>\n"
                + "int good_one(void) { return 1; }\n"
                + "int broken(void) { return ; + ; }\n"
                + "int good_two(void) { return 2; }\n";
        CLanguageModule module = new CLanguageModule(storage());
        assertRecovered("units/c/three.c", source, module,
                "FUNCTION", List.of("good_one", "good_two"), false);
    }

    @Test
    void preservesCFunctionAndMarksConditionalTypeEvidencePartialWithoutGuessingBranch()
            throws Exception {
        String source = "int helper(void) { return 1; }\n"
                + "int main(void) {\n"
                + "#ifdef PLATFORM_WITH_EXTERNAL_TYPE\n"
                + "    UNKNOWN_EXTERNAL_TYPE value;\n"
                + "#else\n"
                + "    int value = 0;\n"
                + "#endif\n"
                + "    return value;\n"
                + "}\n";
        ParserWorkspace workspace = storage();
        Path file = workspace.sourceDir().resolve("units/c/conditional.c");
        Files.createDirectories(file.getParent());
        Files.writeString(file, source, StandardCharsets.UTF_8);
        String originalSha256 = Hashes.sha256(Files.readAllBytes(file));
        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        ParseProgressTracker tracker = new ParseProgressTracker(null, file.getFileName().toString());
        RawParseResult firstPass = module.parseFile(file.toFile(), tracker);
        ParseQualityGate gate = new ParseQualityGate();
        QualityDecision firstDecision = gate.evaluateFirstPass(firstPass);
        assertTrue(firstDecision.accepted(), firstPass::astJson);

        JsonNode root = JSON.readTree(firstPass.astJson());
        assertEquals(List.of("helper", "main"), children(firstPass.astJson()).stream()
                .filter(child -> "FUNCTION".equals(child.path("type").asText()))
                .map(child -> child.path("name").asText()).toList());
        assertEquals("partial", root.path("evidence").path("parseStatus").asText());
        assertEquals("unresolved", root.path("evidence")
                .path("configuredPreprocessing").path("status").asText());

        JsonNode unresolvedType = null;
        int conditionalRegions = 0;
        for (JsonNode fact : root.path("evidence").path("facts")) {
            if ("conditional_region".equals(fact.path("kind").asText())) {
                conditionalRegions++;
            }
            if ("symbol".equals(fact.path("kind").asText())
                    && "lookup".equals(fact.path("payload").path("role").asText())
                    && "UNKNOWN_EXTERNAL_TYPE".equals(slice(source, fact.path("range")))) {
                unresolvedType = fact;
            }
        }
        assertEquals(1, conditionalRegions);
        assertTrue(unresolvedType != null, root.path("evidence").path("facts")::toPrettyString);
        assertEquals("type_name", unresolvedType.path("payload")
                .path("parserDecision").asText());
        assertEquals("unresolved", unresolvedType.path("payload")
                .path("resolutionStatus").asText());
        int presenceRef = unresolvedType.path("presenceRef").asInt(-1);
        assertEquals("conditional", root.path("evidence").path("presences")
                .path(presenceRef).path("status").asText());
        assertEquals(originalSha256, Hashes.sha256(Files.readAllBytes(file)));
    }

    private static String slice(String source, JsonNode range) {
        int start = range.path("charOffset").asInt();
        int length = range.path("charLength").asInt();
        return new String(source.codePoints().toArray(), start, length);
    }

    @Test
    void isolatesPostgresqlRoutinesWithoutRelabelingFunction() throws Exception {
        String source = "CREATE FUNCTION good_one() RETURNS integer AS $$\n"
                + "BEGIN RETURN 1; END;\n$$ LANGUAGE plpgsql;\n"
                + "CREATE FUNCTION broken() RETURNS integer AS $$\n"
                + "BEGIN RETURN ; + ; END;\n$$ LANGUAGE plpgsql;\n"
                + "CREATE FUNCTION good_two() RETURNS integer AS $$\n"
                + "BEGIN RETURN 2; END;\n$$ LANGUAGE plpgsql;\n";
        assertRecovered("units/postgresql/three.sql", source,
                new PostgreSqlLanguageModule(storage()), "PROCEDURE",
                List.of("good_one", "good_two"), false);
    }

    private static void assertRecovered(String relative, String source, LanguageModule module,
                                        String nodeType, List<String> expectedNames,
                                        boolean expectOutsideNode) throws Exception {
        ParserWorkspace storage = storage();
        Path file = storage.sourceDir().resolve(relative);
        Files.createDirectories(file.getParent());
        Files.writeString(file, source, StandardCharsets.UTF_8);
        String before = Hashes.sha256(Files.readAllBytes(file));
        module.prepareProjectContext();
        ParseProgressTracker tracker = new ParseProgressTracker(null, file.getFileName().toString());
        RawParseResult first = module.parseFile(file.toFile(), tracker);
        ParseQualityGate gate = new ParseQualityGate();
        QualityDecision decision = gate.evaluateFirstPass(first);
        assertFalse(decision.accepted(), module.languageId());
        RecoveryOutcome outcome = new LayeredRecoveryPipeline(gate, new RecoveryRuleRegistry(List.of()))
                .recover(module, file, storage.sourceDir(), first, decision, tracker);

        assertEquals(QualityStatus.PARTIAL, outcome.decision().status());
        assertEquals(1, outcome.unresolvedUnits());
        List<String> names = children(outcome.astJson()).stream()
                .filter(child -> nodeType.equals(child.path("type").asText()))
                .map(child -> child.path("name").asText()).toList();
        assertEquals(expectedNames, names);
        if (expectOutsideNode) {
            assertTrue(children(outcome.astJson()).stream().anyMatch(child ->
                    "IMPORT".equals(child.path("type").asText())));
        }
        assertEquals(before, Hashes.sha256(Files.readAllBytes(file)), "Original source changed");
    }

    private static List<JsonNode> children(String astJson) throws Exception {
        java.util.ArrayList<JsonNode> result = new java.util.ArrayList<>();
        JSON.readTree(astJson).path("children").forEach(result::add);
        return result;
    }

    private static ParserWorkspace storage() {
        return new ParserWorkspace(new SourceIntakeClassifier());
    }
}
