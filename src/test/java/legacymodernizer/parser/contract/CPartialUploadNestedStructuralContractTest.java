package legacymodernizer.parser.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.intake.SourceIntakeClassifier;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.parsing.languages.c.CLanguageModule;
import legacymodernizer.parser.service.ParseProgressTracker;

class CPartialUploadNestedStructuralContractTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @BeforeAll
    static void requireIsolatedDataRoot() {
        String configured = System.getProperty("parser.data.root", "").replace('\\', '/');
        assertTrue(configured.contains("/target/test-data"));
    }

    @Test
    void externalDeclarationProbeDoesNotRejectUnknownLocalDeclarationTypes()
            throws Exception {
        String source = """
                money_t cart_calc_total(member_id_t member_id)
                {
                    money_t total;
                    total = (money_t)member_id;
                    return total;
                }
                """;
        ParserWorkspace workspace = new ParserWorkspace(new SourceIntakeClassifier());
        Path file = workspace.sourceDir().resolve("c-partial-upload/nested-cart.c");
        Files.createDirectories(file.getParent());
        Files.writeString(file, source, StandardCharsets.UTF_8);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        RawParseResult result = module.parseFile(file.toFile(),
                new ParseProgressTracker(null, file.getFileName().toString()));
        JsonNode root = JSON.readTree(result.astJson());

        assertEquals(0, result.antlrRecoveries(), () -> result.diagnostics().toString());
        assertTrue(result.diagnostics().isEmpty(), result.diagnostics()::toString);
        assertEquals(List.of("cart_calc_total"), functionNames(root));
        assertEquals(1, facts(root, "callable").size());
        assertEquals("complete", completeness(root, "callable").path("status").asText());
    }

    private static List<String> functionNames(JsonNode root) {
        List<String> result = new ArrayList<>();
        collectFunctions(root, result);
        return result;
    }

    private static void collectFunctions(JsonNode node, List<String> result) {
        if ("FUNCTION".equals(node.path("type").asText())) {
            result.add(node.path("name").asText());
        }
        node.path("children").forEach(child -> collectFunctions(child, result));
    }

    private static List<JsonNode> facts(JsonNode root, String kind) {
        List<JsonNode> result = new ArrayList<>();
        root.path("evidence").path("facts").forEach(fact -> {
            if (kind.equals(fact.path("kind").asText())) result.add(fact);
        });
        return result;
    }

    private static JsonNode completeness(JsonNode root, String kind) {
        for (JsonNode row : root.path("evidence").path("completeness")) {
            if (kind.equals(row.path("kind").asText())) return row;
        }
        throw new AssertionError("missing completeness row for " + kind);
    }
}
