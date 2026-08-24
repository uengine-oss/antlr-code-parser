package legacymodernizer.parser.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.intake.SourceIntakeClassifier;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.parsing.languages.c.CLanguageModule;
import legacymodernizer.parser.service.ParseProgressTracker;

class CStructuralExpressionIr2ContractTest {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Set<String> FORBIDDEN_SEMANTIC_FIELDS = Set.of(
            "binding", "type", "linkage", "meaning", "valueKind",
            "operatorMeaning", "resolvedTarget", "resolutionStatus");

    @BeforeAll
    static void requireIsolatedDataRoot() {
        String configured = System.getProperty("parser.data.root", "").replace('\\', '/');
        assertTrue(configured.contains("/target/test-data"));
    }

    @Test
    void emitsExactStatementExpressionRootsWithoutSemanticDecisions() throws Exception {
        String source = """
                int sample_42af(int value) {
                    if (value > 0) value = value - 1;
                    return value;
                }
                """;
        JsonNode root = parse("expression-roots.c", source);
        List<JsonNode> facts = facts(root, "structural_expression");

        assertEquals(List.of(
                "condition:value > 0",
                "assignment_target:value",
                "assignment_value:value - 1",
                "return_value:value"), facts.stream().map(fact ->
                    fact.path("payload").path("role").asText() + ":"
                            + slice(source, fact.path("range"))).toList());

        Set<String> ids = new HashSet<>();
        for (JsonNode fact : facts) {
            assertTrue(ids.add(fact.path("factId").asText()));
            JsonNode payload = fact.path("payload");
            assertContains(payload.path("ownerRange"), fact.path("range"));
            assertScopePathContains(payload.path("scopePath"), fact.path("range"));
            assertEquals("c-expression-syntax/v1",
                    payload.path("syntax").path("schema").asText());
            assertTrue(payload.path("syntax").path("root").isObject());
            FORBIDDEN_SEMANTIC_FIELDS.forEach(field -> assertFalse(payload.has(field)));
        }
        assertCompletePopulation(completeness(root, "structural_expression"), 4);
    }

    @Test
    void preservesRecursiveGrammarAndDirectLexerCoordinates() throws Exception {
        String source = """
                int sample_a983(int *items, int index) {
                    return items[index + 1] != 0;
                }
                """;
        JsonNode root = parse("expression-tree.c", source);
        JsonNode fact = onlyRole(root, "return_value");
        JsonNode syntaxRoot = fact.path("payload").path("syntax").path("root");
        Set<String> grammarRules = new HashSet<>();
        Set<String> tokenKinds = new HashSet<>();
        Set<String> tokenCoordinates = new HashSet<>();

        collectComponent(source, syntaxRoot, grammarRules, tokenKinds, tokenCoordinates);

        assertTrue(grammarRules.containsAll(Set.of(
                "expression", "postfixExpression", "additiveExpression",
                "equalityExpression")), grammarRules::toString);
        assertTrue(tokenKinds.containsAll(Set.of(
                "Identifier", "LeftBracket", "RightBracket", "Plus", "NotEqual")),
                tokenKinds::toString);
    }

    @Test
    void distinguishesDeclarationInitializerAndUpdateSyntaxFromAssignments() throws Exception {
        String source = """
                int sample_77c1(void) {
                    int value = 1;
                    value++;
                    value += 2;
                    return value;
                }
                """;
        JsonNode root = parse("expression-origins.c", source);
        List<JsonNode> facts = facts(root, "structural_expression");

        assertEquals(List.of(
                "initializer_value:1",
                "update_expression:value++",
                "assignment_target:value",
                "assignment_value:2",
                "return_value:value"), facts.stream().map(fact ->
                    fact.path("payload").path("role").asText() + ":"
                            + slice(source, fact.path("range"))).toList());
        assertCompletePopulation(completeness(root, "structural_expression"), 5);

        JsonNode function = root.path("children").path(0);
        List<String> origins = new ArrayList<>();
        function.path("children").forEach(node -> {
            if ("ASSIGNMENT".equals(node.path("type").asText())) {
                origins.add(node.path("statementOrigin").asText());
            }
        });
        assertEquals(List.of(
                "declaration_initializer", "postfix_update", "assignment_expression"),
                origins);
    }

    private static JsonNode parse(String fileName, String source) throws Exception {
        ParserWorkspace workspace = new ParserWorkspace(new SourceIntakeClassifier());
        Path file = workspace.sourceDir().resolve("c-structural-expression-ir2").resolve(fileName);
        Files.createDirectories(file.getParent());
        Files.writeString(file, source, StandardCharsets.UTF_8);
        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        RawParseResult result = module.parseFile(file.toFile(),
                new ParseProgressTracker(null, file.getFileName().toString()));
        return JSON.readTree(result.astJson());
    }

    private static List<JsonNode> facts(JsonNode root, String kind) {
        List<JsonNode> result = new ArrayList<>();
        root.path("evidence").path("facts").forEach(fact -> {
            if (kind.equals(fact.path("kind").asText())) result.add(fact);
        });
        return result;
    }

    private static JsonNode onlyRole(JsonNode root, String role) {
        List<JsonNode> matches = facts(root, "structural_expression").stream()
                .filter(fact -> role.equals(fact.path("payload").path("role").asText()))
                .toList();
        assertEquals(1, matches.size());
        return matches.get(0);
    }

    private static JsonNode completeness(JsonNode root, String kind) {
        JsonNode match = null;
        for (JsonNode row : root.path("evidence").path("completeness")) {
            if (kind.equals(row.path("kind").asText())) {
                assertTrue(match == null, "duplicate completeness row for " + kind);
                match = row;
            }
        }
        assertNotNull(match, "missing completeness row for " + kind);
        return match;
    }

    private static void assertCompletePopulation(JsonNode row, int population) {
        assertEquals("complete", row.path("status").asText());
        assertEquals(population, row.path("population").asInt());
        assertEquals(population, row.path("emitted").asInt());
        assertEquals(0, row.path("explicitlyUnresolved").asInt());
    }

    private static void collectComponent(
            String source, JsonNode component, Set<String> grammarRules,
            Set<String> tokenKinds, Set<String> tokenCoordinates) {
        assertTrue(component.isObject());
        grammarRules.add(component.path("grammarRule").asText());
        JsonNode componentRange = component.path("range");
        assertFalse(slice(source, componentRange).isEmpty());
        component.path("directTokens").forEach(token -> {
            tokenKinds.add(token.path("tokenKind").asText());
            assertContains(componentRange, token.path("range"));
            String coordinate = token.path("tokenKind").asText() + ":"
                    + token.path("range").path("charOffset").asInt() + ":"
                    + token.path("range").path("charLength").asInt();
            assertTrue(tokenCoordinates.add(coordinate), "duplicate direct token " + coordinate);
        });
        component.path("children").forEach(child -> {
            assertContains(componentRange, child.path("range"));
            collectComponent(source, child, grammarRules, tokenKinds, tokenCoordinates);
        });
    }

    private static void assertScopePathContains(JsonNode scopePath, JsonNode range) {
        assertTrue(scopePath.isArray() && !scopePath.isEmpty());
        assertEquals("translation_unit", scopePath.path(0).path("kind").asText());
        JsonNode previous = null;
        for (JsonNode scope : scopePath) {
            if (previous != null) assertContains(previous.path("range"), scope.path("range"));
            previous = scope;
        }
        assertContains(previous.path("range"), range);
    }

    private static void assertContains(JsonNode outer, JsonNode inner) {
        int outerStart = outer.path("charOffset").asInt();
        int outerEnd = outerStart + outer.path("charLength").asInt();
        int innerStart = inner.path("charOffset").asInt();
        int innerEnd = innerStart + inner.path("charLength").asInt();
        assertTrue(outerStart <= innerStart && innerEnd <= outerEnd,
                () -> outer + " does not contain " + inner);
    }

    private static String slice(String source, JsonNode range) {
        int start = range.path("charOffset").asInt();
        int length = range.path("charLength").asInt();
        return new String(source.codePoints().toArray(), start, length);
    }
}
