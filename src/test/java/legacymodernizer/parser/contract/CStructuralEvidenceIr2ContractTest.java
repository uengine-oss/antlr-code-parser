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

class CStructuralEvidenceIr2ContractTest {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Set<String> FORBIDDEN_SEMANTIC_FIELDS = Set.of(
            "binding",
            "adapterSchema",
            "bindingKey",
            "compatibilityKey",
            "compatibilityStatus",
            "compatibilityScope",
            "definitionStatus",
            "targetScope",
            "resolutionMode",
            "externalIdentityKey");

    @BeforeAll
    static void requireIsolatedDataRoot() {
        String configured = System.getProperty("parser.data.root", "").replace('\\', '/');
        assertTrue(configured.contains("/target/test-data"));
    }

    @Test
    void emitsIr2FrontendIdentityAndSyntaxOnlyCallFacts() throws Exception {
        String source = """
                static int helper_2a01(int value) { return value; }
                int caller_2a01(void) { return helper_2a01(1); }
                """;
        JsonNode root = parse("identity.c", source);
        JsonNode evidence = root.path("evidence");

        assertEquals("2.1.0", evidence.path("version").asText());
        assertEquals("c", evidence.path("language").asText());
        assertEquals("antlr-c/v1", evidence.path("frontendSchema").asText());

        JsonNode call = onlyCall(root, source, "helper_2a01");
        assertNoSemanticFields(call.path("payload"));
        assertScopeKinds(call.path("payload").path("scopePath"),
                "translation_unit", "function", "block");
        assertFalse(hasCompleteness(root, "call_binding"));
    }

    @Test
    void preservesExactLexicalScopePathAndDeclarationOrder() throws Exception {
        String source = """
                int caller_8c43(void) {
                    int target_8c43(int value);
                    {
                        return target_8c43(1);
                    }
                }
                """;
        JsonNode root = parse("scope.c", source);
        JsonNode declaration = onlyCallable(root, source, "declaration", "target_8c43");
        JsonNode call = onlyCall(root, source, "target_8c43");

        JsonNode declarationPayload = declaration.path("payload");
        JsonNode callPayload = call.path("payload");
        assertScopeKinds(declarationPayload.path("scopePath"),
                "translation_unit", "function", "block");
        assertScopeKinds(callPayload.path("scopePath"),
                "translation_unit", "function", "block", "block");
        assertEquals(declarationPayload.path("scopePath").path(2).path("range"),
                declarationPayload.path("scopeRange"));
        assertTrue(declarationPayload.path("declarationPoint").asInt()
                <= call.path("range").path("charOffset").asInt());
        assertNoSemanticFields(declarationPayload);
    }

    @Test
    void preservesTypedDeclaratorTreeAndDirectLexerTokens() throws Exception {
        String source = """
                static inline int typed_4d72(
                    int count,
                    int (*callback)(int),
                    ...);
                """;
        JsonNode root = parse("declarator.c", source);
        JsonNode callable = onlyCallable(root, source, "declaration", "typed_4d72");
        JsonNode syntax = callable.path("payload").path("syntax");

        assertEquals("c-callable-syntax/v1", syntax.path("schema").asText());
        assertTrue(syntax.path("declarationSpecifiers").isArray());
        assertTrue(syntax.path("declarationSpecifiers").size() >= 3);
        assertTrue(syntax.path("declarator").isObject());

        Set<String> grammarRules = new HashSet<>();
        Set<String> tokenKinds = new HashSet<>();
        Set<String> tokenCoordinates = new HashSet<>();
        syntax.path("declarationSpecifiers").forEach(component -> collectComponent(
                source, component, grammarRules, tokenKinds, tokenCoordinates));
        collectComponent(source, syntax.path("declarator"),
                grammarRules, tokenKinds, tokenCoordinates);
        syntax.path("attributes").forEach(component -> collectComponent(
                source, component, grammarRules, tokenKinds, tokenCoordinates));

        assertTrue(grammarRules.containsAll(Set.of(
                "storageClassSpecifier",
                "functionSpecifier",
                "typeSpecifier",
                "declarator",
                "pointer",
                "parameterDeclaration")), grammarRules::toString);
        assertTrue(tokenKinds.containsAll(Set.of(
                "Static", "Inline", "Int", "Identifier", "Star", "Ellipsis")),
                tokenKinds::toString);
    }

    @Test
    void accountsForCallAndCallableSyntaxExactlyOnce() throws Exception {
        String source = """
                int first_6f50(void) { return 1; }
                int second_6f50(void) { return first_6f50(); }
                """;
        JsonNode root = parse("completeness.c", source);
        int calls = countFacts(root, "call");
        int callables = countFacts(root, "callable");

        assertEquals(1, calls);
        assertEquals(2, callables);
        assertCompletePopulation(completeness(root, "call"), calls);
        assertCompletePopulation(completeness(root, "callable"), callables);
        assertFalse(hasCompleteness(root, "call_binding"));
    }

    @Test
    void accountsForRecoveredCallableWithoutPromotingSyntheticTokens() throws Exception {
        JsonNode root = parse("recovered-callable.c", """
                int broken_91d3(int value {
                    return value;
                }
                """);
        JsonNode row = completeness(root, "callable");

        assertEquals("partial", row.path("status").asText());
        assertEquals("insufficient_parser_recovery", row.path("reason").asText());
        assertEquals(1, row.path("population").asInt());
        assertEquals(0, row.path("emitted").asInt());
        assertEquals(1, row.path("explicitlyUnresolved").asInt());
    }

    private static JsonNode parse(String fileName, String source) throws Exception {
        ParserWorkspace workspace = new ParserWorkspace(new SourceIntakeClassifier());
        Path file = workspace.sourceDir().resolve("c-structural-ir2").resolve(fileName);
        Files.createDirectories(file.getParent());
        Files.writeString(file, source, StandardCharsets.UTF_8);
        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        RawParseResult result = module.parseFile(file.toFile(),
                new ParseProgressTracker(null, file.getFileName().toString()));
        return JSON.readTree(result.astJson());
    }

    private static JsonNode onlyCallable(
            JsonNode root, String source, String role, String name) {
        List<JsonNode> matches = new ArrayList<>();
        root.path("evidence").path("facts").forEach(fact -> {
            if ("callable".equals(fact.path("kind").asText())
                    && role.equals(fact.path("payload").path("role").asText())
                    && name.equals(slice(source, fact.path("payload").path("nameRange")))) {
                matches.add(fact);
            }
        });
        assertEquals(1, matches.size(), "callable must be exact-once for " + role + "/" + name);
        return matches.get(0);
    }

    private static JsonNode onlyCall(JsonNode root, String source, String name) {
        List<JsonNode> matches = new ArrayList<>();
        root.path("evidence").path("facts").forEach(fact -> {
            if ("call".equals(fact.path("kind").asText())
                    && name.equals(slice(source, fact.path("payload").path("calleeRange")))) {
                matches.add(fact);
            }
        });
        assertEquals(1, matches.size(), "call fact must be exact-once for " + name);
        return matches.get(0);
    }

    private static void assertScopeKinds(JsonNode scopePath, String... expectedKinds) {
        assertTrue(scopePath.isArray());
        assertEquals(expectedKinds.length, scopePath.size());
        for (int index = 0; index < expectedKinds.length; index++) {
            assertEquals(expectedKinds[index], scopePath.path(index).path("kind").asText());
            assertTrue(scopePath.path(index).path("range").path("charLength").asInt() > 0);
            if (index > 0) {
                assertContains(scopePath.path(index - 1).path("range"),
                        scopePath.path(index).path("range"));
            }
        }
    }

    private static void collectComponent(
            String source,
            JsonNode component,
            Set<String> grammarRules,
            Set<String> tokenKinds,
            Set<String> tokenCoordinates) {
        assertTrue(component.isObject());
        String grammarRule = component.path("grammarRule").asText();
        assertFalse(grammarRule.isBlank());
        grammarRules.add(grammarRule);
        JsonNode componentRange = component.path("range");
        assertFalse(slice(source, componentRange).isEmpty());

        assertTrue(component.path("directTokens").isArray());
        component.path("directTokens").forEach(token -> {
            String tokenKind = token.path("tokenKind").asText();
            assertFalse(tokenKind.isBlank());
            tokenKinds.add(tokenKind);
            assertContains(componentRange, token.path("range"));
            assertFalse(slice(source, token.path("range")).isEmpty());
            String coordinate = tokenKind + ":"
                    + token.path("range").path("charOffset").asInt() + ":"
                    + token.path("range").path("charLength").asInt();
            assertTrue(tokenCoordinates.add(coordinate), "duplicate direct token " + coordinate);
        });

        assertTrue(component.path("children").isArray());
        component.path("children").forEach(child -> {
            assertContains(componentRange, child.path("range"));
            collectComponent(source, child, grammarRules, tokenKinds, tokenCoordinates);
        });
    }

    private static void assertContains(JsonNode outer, JsonNode inner) {
        int outerStart = outer.path("charOffset").asInt();
        int outerEnd = outerStart + outer.path("charLength").asInt();
        int innerStart = inner.path("charOffset").asInt();
        int innerEnd = innerStart + inner.path("charLength").asInt();
        assertTrue(outerStart <= innerStart && innerEnd <= outerEnd,
                () -> outer + " does not contain " + inner);
    }

    private static void assertNoSemanticFields(JsonNode payload) {
        FORBIDDEN_SEMANTIC_FIELDS.forEach(field -> assertFalse(payload.has(field),
                () -> "Parser IR 2.0 must not emit " + field));
    }

    private static int countFacts(JsonNode root, String kind) {
        int count = 0;
        for (JsonNode fact : root.path("evidence").path("facts")) {
            if (kind.equals(fact.path("kind").asText())) {
                count++;
            }
        }
        return count;
    }

    private static boolean hasCompleteness(JsonNode root, String kind) {
        for (JsonNode row : root.path("evidence").path("completeness")) {
            if (kind.equals(row.path("kind").asText())) {
                return true;
            }
        }
        return false;
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
        assertEquals(0, row.path("unresolvedFactIds").size());
    }

    private static String slice(String source, JsonNode range) {
        int start = range.path("charOffset").asInt();
        int length = range.path("charLength").asInt();
        return new String(source.codePoints().toArray(), start, length);
    }
}
