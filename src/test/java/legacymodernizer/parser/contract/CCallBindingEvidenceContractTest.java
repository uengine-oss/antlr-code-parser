package legacymodernizer.parser.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

/** Formal red contract for spec 131 C-067 language-owned C call binding. */
class CCallBindingEvidenceContractTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @BeforeAll
    static void requireIsolatedDataRoot() {
        String configured = System.getProperty("parser.data.root", "").replace('\\', '/');
        assertTrue(configured.contains("/target/test-data"));
    }

    @Test
    void visiblePrototypeAndCrossTranslationUnitDefinitionShareOpaqueKeys()
            throws Exception {
        ParserWorkspace workspace = workspace();
        Path root = workspace.sourceDir().resolve("c-call-binding/cross-tu");
        String callerSource = """
                int shared_7a31(int declared_value);
                int caller_7a31(void) { return shared_7a31(1); }
                """;
        String targetSource = """
                int shared_7a31(int implemented_value) { return implemented_value; }
                """;
        Path callerFile = root.resolve("caller.c");
        Path targetFile = root.resolve("target.c");
        write(callerFile, callerSource);
        write(targetFile, targetSource);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode caller = parse(module, callerFile);
        JsonNode target = parse(module, targetFile);

        assertEquals("1.3.0", caller.path("evidence").path("version").asText());
        JsonNode declaration = onlyCallable(caller, callerSource, "declaration", "shared_7a31");
        JsonNode definition = onlyCallable(target, targetSource, "definition", "shared_7a31");
        JsonNode binding = onlyCall(caller, callerSource, "shared_7a31")
                .path("payload").path("binding");

        assertEquals("declaration_bound", binding.path("status").asText());
        assertEquals("compatible_definition", binding.path("resolutionMode").asText());
        assertEquals(declaration.path("factId").asText(),
                binding.path("declarationFactId").asText());
        assertEquals(declaration.path("payload").path("bindingKey").asText(),
                definition.path("payload").path("bindingKey").asText());
        assertEquals(declaration.path("payload").path("compatibilityKey").asText(),
                definition.path("payload").path("compatibilityKey").asText(),
                "parameter identifiers do not change a C function type");
        assertEquals("corpus", binding.path("compatibilityScope").asText());
        assertEquals("corpus", binding.path("targetScope").asText());
        assertEquals("exact", binding.path("dispatch").asText());
        assertEquals("complete", completeness(caller, "call_binding")
                .path("status").asText());
    }

    @Test
    void staticDefinitionsAreTranslationUnitScoped() throws Exception {
        ParserWorkspace workspace = workspace();
        Path root = workspace.sourceDir().resolve("c-call-binding/static");
        String firstSource = "static int local_83bf(void) { return 1; }\n";
        String secondSource = "static int local_83bf(void) { return 2; }\n";
        Path firstFile = root.resolve("first.c");
        Path secondFile = root.resolve("second.c");
        write(firstFile, firstSource);
        write(secondFile, secondSource);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode first = onlyCallable(parse(module, firstFile), firstSource,
                "definition", "local_83bf");
        JsonNode second = onlyCallable(parse(module, secondFile), secondSource,
                "definition", "local_83bf");

        assertEquals("source_file", first.path("payload").path("targetScope").asText());
        assertEquals("source_file", second.path("payload").path("targetScope").asText());
        assertFalse(first.path("payload").path("bindingKey").asText().equals(
                second.path("payload").path("bindingKey").asText()));
    }

    @Test
    void missingVisibleDeclarationIsProductExternalNotInventedInternal()
            throws Exception {
        ParserWorkspace workspace = workspace();
        Path file = workspace.sourceDir().resolve("c-call-binding/external/caller.c");
        String source = "int caller_b921(void) { return absent_b921(1); }\n";
        write(file, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, file);
        JsonNode binding = onlyCall(root, source, "absent_b921")
                .path("payload").path("binding");

        assertEquals("external", binding.path("status").asText());
        assertEquals("none", binding.path("resolutionMode").asText());
        assertTrue(binding.path("declarationFactId").isNull());
        assertTrue(binding.path("bindingKey").isNull());
        assertEquals("missing_visible_declaration", binding.path("reason").asText());
        assertEquals("complete", completeness(root, "call_binding")
                .path("status").asText());
    }

    @Test
    void incompatibleFunctionTypesDoNotShareCompatibilityKey() throws Exception {
        ParserWorkspace workspace = workspace();
        Path root = workspace.sourceDir().resolve("c-call-binding/incompatible");
        String callerSource = """
                int changed_04ae(int value);
                int caller_04ae(void) { return changed_04ae(1); }
                """;
        String targetSource = "long changed_04ae(long value) { return value; }\n";
        Path callerFile = root.resolve("caller.c");
        Path targetFile = root.resolve("target.c");
        write(callerFile, callerSource);
        write(targetFile, targetSource);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode declaration = onlyCallable(parse(module, callerFile), callerSource,
                "declaration", "changed_04ae");
        JsonNode definition = onlyCallable(parse(module, targetFile), targetSource,
                "definition", "changed_04ae");

        assertEquals(declaration.path("payload").path("bindingKey").asText(),
                definition.path("payload").path("bindingKey").asText());
        assertFalse(declaration.path("payload").path("compatibilityKey").asText().equals(
                definition.path("payload").path("compatibilityKey").asText()));
    }

    @Test
    void suppliedHeaderDefinitionIsPreservedInsteadOfDiscarded() throws Exception {
        ParserWorkspace workspace = workspace();
        Path file = workspace.sourceDir().resolve("c-call-binding/header/helper.h");
        String source = "static inline int helper_1df2(int value) { return value; }\n";
        write(file, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, file);

        JsonNode definition = onlyCallable(root, source, "definition", "helper_1df2");
        assertEquals("source_file", definition.path("payload")
                .path("targetScope").asText());
        assertEquals(1, astNodes(root, "FUNCTION", "helper_1df2").size(),
                () -> root.toPrettyString());
        assertEquals("complete", completeness(root, "callable").path("status").asText());
    }

    @Test
    void functionLikeMacroCallIsConfigurationDependentNotAFunctionGuess()
            throws Exception {
        ParserWorkspace workspace = workspace();
        Path file = workspace.sourceDir().resolve("c-call-binding/macro/caller.c");
        String source = """
                #define APPLY_f01d(value) ((value) + 1)
                int caller_f01d(void) { return APPLY_f01d(1); }
                """;
        write(file, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, file);
        JsonNode binding = onlyCall(root, source, "APPLY_f01d")
                .path("payload").path("binding");

        assertEquals("configuration_dependent", binding.path("status").asText());
        assertEquals("insufficient_preprocessing_expansion",
                binding.path("reason").asText());
        assertEquals(1, binding.path("candidateFactIds").size(),
                () -> binding.toPrettyString());
        assertEquals("partial", completeness(root, "call_binding")
                .path("status").asText());
    }

    @Test
    void memberFunctionPointerCallIsDynamicNotANameOnlyFunctionTarget()
            throws Exception {
        ParserWorkspace workspace = workspace();
        Path file = workspace.sourceDir().resolve("c-call-binding/member/caller.c");
        String source = """
                struct Ops_921a { int (*apply_921a)(int); };
                int caller_921a(struct Ops_921a *ops) { return ops->apply_921a(1); }
                """;
        write(file, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, file);
        JsonNode call = onlyCall(root, source, "ops->apply_921a");

        assertEquals("expression", call.path("payload").path("calleeKind").asText());
        assertTrue(call.path("payload").path("terminalName").isNull());
        assertEquals("dynamic", call.path("payload").path("binding")
                .path("status").asText());
    }

    @Test
    void namedFunctionPointerObjectIsDynamicAndNotACallableDefinition()
            throws Exception {
        ParserWorkspace workspace = workspace();
        Path file = workspace.sourceDir().resolve("c-call-binding/pointer/caller.c");
        String source = """
                int (*callback_772e)(int);
                int caller_772e(void) { return callback_772e(1); }
                """;
        write(file, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, file);
        JsonNode binding = onlyCall(root, source, "callback_772e")
                .path("payload").path("binding");

        assertEquals(0, callableFacts(root, source, "callback_772e").size());
        assertEquals("dynamic", binding.path("status").asText());
        assertEquals("function_pointer_object", binding.path("reason").asText());
    }

    @Test
    void blockScopePrototypeDoesNotLeakIntoSiblingBlock() throws Exception {
        ParserWorkspace workspace = workspace();
        Path file = workspace.sourceDir().resolve("c-call-binding/block/caller.c");
        String source = """
                int caller_18f0(int branch) {
                    if (branch) {
                        int local_decl_18f0(int value);
                        return local_decl_18f0(1);
                    }
                    return local_decl_18f0(2);
                }
                """;
        write(file, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, file);
        List<JsonNode> calls = callFacts(root, source, "local_decl_18f0");

        assertEquals(2, calls.size());
        assertEquals("declaration_bound", calls.get(0).path("payload")
                .path("binding").path("status").asText());
        assertEquals("external", calls.get(1).path("payload")
                .path("binding").path("status").asText());
    }

    @Test
    void translationUnitLocalTypeSpellingIsNotCrossFileCompatibilityProof()
            throws Exception {
        ParserWorkspace workspace = workspace();
        Path root = workspace.sourceDir().resolve("c-call-binding/type-scope");
        String callerSource = """
                typedef int LocalType_4e2a;
                int typed_4e2a(LocalType_4e2a value);
                int caller_4e2a(void) { return typed_4e2a(1); }
                """;
        String targetSource = """
                typedef long LocalType_4e2a;
                int typed_4e2a(LocalType_4e2a value) { return (int)value; }
                """;
        Path callerFile = root.resolve("caller.c");
        Path targetFile = root.resolve("target.c");
        write(callerFile, callerSource);
        write(targetFile, targetSource);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode declaration = onlyCallable(parse(module, callerFile), callerSource,
                "declaration", "typed_4e2a");
        JsonNode definition = onlyCallable(parse(module, targetFile), targetSource,
                "definition", "typed_4e2a");

        assertEquals("unavailable", declaration.path("payload")
                .path("compatibilityStatus").asText());
        assertEquals("unavailable", definition.path("payload")
                .path("compatibilityStatus").asText());
        assertTrue(declaration.path("payload").path("compatibilityKey").isNull());
        assertTrue(definition.path("payload").path("compatibilityKey").isNull());
    }

    @Test
    void blockLocalTypePrototypeSurfacesUnsupportedCompatibility() throws Exception {
        ParserWorkspace workspace = workspace();
        Path file = workspace.sourceDir().resolve("c-call-binding/block-type/caller.c");
        String source = """
                int caller_b176(void) {
                    typedef int BlockType_b176;
                    int typed_b176(BlockType_b176 value);
                    return typed_b176(1);
                }
                """;
        write(file, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, file);
        JsonNode declaration = onlyCallable(
                root, source, "declaration", "typed_b176");
        JsonNode binding = onlyCall(root, source, "typed_b176")
                .path("payload").path("binding");

        assertEquals("unavailable", declaration.path("payload")
                .path("compatibilityStatus").asText());
        assertEquals("unsupported", binding.path("status").asText());
        assertEquals(1, binding.path("candidateFactIds").size());
        assertEquals(declaration.path("factId"), binding.path("candidateFactIds").get(0));
        assertEquals("partial", completeness(root, "callable").path("status").asText());
        assertEquals("partial", completeness(root, "call_binding")
                .path("status").asText());
    }

    @Test
    void nonStaticInlineDefinitionRequiresDialectConfiguration() throws Exception {
        ParserWorkspace workspace = workspace();
        Path file = workspace.sourceDir().resolve("c-call-binding/inline/caller.c");
        String source = """
                inline int inline_99c4(int value) { return value; }
                int caller_99c4(void) { return inline_99c4(1); }
                """;
        write(file, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, file);
        JsonNode definition = onlyCallable(
                root, source, "definition", "inline_99c4");
        JsonNode binding = onlyCall(root, source, "inline_99c4")
                .path("payload").path("binding");

        assertEquals("configuration_dependent", definition.path("payload")
                .path("definitionStatus").asText());
        assertEquals("configuration_dependent", binding.path("status").asText());
        assertEquals(1, binding.path("candidateFactIds").size());
        assertEquals(definition.path("factId"), binding.path("candidateFactIds").get(0));
        assertEquals("partial", completeness(root, "callable").path("status").asText());
        assertEquals("partial", completeness(root, "call_binding")
                .path("status").asText());
    }

    @Test
    void unspecifiedParameterDeclarationsDoNotClaimExactCompatibility()
            throws Exception {
        ParserWorkspace workspace = workspace();
        Path file = workspace.sourceDir().resolve("c-call-binding/unspecified/caller.c");
        String source = """
                static long initialize_669a();
                int caller_669a(void) { return initialize_669a(); }
                static long initialize_669a() { return 0; }
                """;
        write(file, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, file);
        List<JsonNode> callable = callableFacts(root, source, "initialize_669a");
        JsonNode binding = onlyCall(root, source, "initialize_669a")
                .path("payload").path("binding");

        assertEquals(2, callable.size());
        assertTrue(callable.stream().allMatch(fact -> "unavailable".equals(
                fact.path("payload").path("compatibilityStatus").asText())));
        assertEquals("unsupported", binding.path("status").asText());
        assertEquals("partial", completeness(root, "call_binding")
                .path("status").asText());
    }

    @Test
    void compatibleBuiltinSpellingsShareCanonicalFunctionType() throws Exception {
        ParserWorkspace workspace = workspace();
        Path root = workspace.sourceDir().resolve("c-call-binding/builtin-canonical");
        String callerSource = """
                signed int normalized_6f20(const short int value);
                int caller_6f20(void) { return normalized_6f20(1); }
                """;
        String targetSource = """
                int normalized_6f20(signed short value) { return value; }
                """;
        Path callerFile = root.resolve("caller.c");
        Path targetFile = root.resolve("target.c");
        write(callerFile, callerSource);
        write(targetFile, targetSource);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode declaration = onlyCallable(parse(module, callerFile), callerSource,
                "declaration", "normalized_6f20");
        JsonNode definition = onlyCallable(parse(module, targetFile), targetSource,
                "definition", "normalized_6f20");

        assertEquals("exact", declaration.path("payload")
                .path("compatibilityStatus").asText());
        assertEquals("corpus", declaration.path("payload")
                .path("compatibilityScope").asText());
        assertEquals(declaration.path("payload").path("compatibilityKey"),
                definition.path("payload").path("compatibilityKey"));
    }

    @Test
    void visibleDefinitionIsDirectEvenWhenNamedTypeCompatibilityIsUnavailable()
            throws Exception {
        ParserWorkspace workspace = workspace();
        Path file = workspace.sourceDir().resolve("c-call-binding/direct/caller.c");
        String source = """
                typedef int Local_3ac1;
                static int direct_3ac1(Local_3ac1 value) { return value; }
                int caller_3ac1(void) { return direct_3ac1(1); }
                """;
        write(file, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, file);
        JsonNode definition = onlyCallable(root, source, "definition", "direct_3ac1");
        JsonNode binding = onlyCall(root, source, "direct_3ac1")
                .path("payload").path("binding");

        assertEquals("unavailable", definition.path("payload")
                .path("compatibilityStatus").asText());
        assertEquals("declaration_bound", binding.path("status").asText());
        assertEquals("direct_definition", binding.path("resolutionMode").asText());
        assertEquals(definition.path("factId"), binding.path("declarationFactId"));
        assertTrue(binding.path("compatibilityKey").isNull());
    }

    @Test
    void inactivePrototypeCannotBindAnActiveCall() throws Exception {
        ParserWorkspace workspace = workspace();
        Path file = workspace.sourceDir().resolve("c-call-binding/inactive/caller.c");
        String source = """
                #if 0
                int hidden_b389(int value);
                #endif
                int caller_b389(void) { return hidden_b389(1); }
                """;
        write(file, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, file);
        JsonNode binding = onlyCall(root, source, "hidden_b389")
                .path("payload").path("binding");

        assertEquals("external", binding.path("status").asText());
        assertEquals("missing_visible_declaration", binding.path("reason").asText());
    }

    @Test
    void conditionalPrototypeProducesConfigurationDependentBinding() throws Exception {
        ParserWorkspace workspace = workspace();
        Path file = workspace.sourceDir().resolve("c-call-binding/conditional/caller.c");
        String source = """
                #if FEATURE_B824
                int conditional_b824(int value);
                #endif
                int caller_b824(void) { return conditional_b824(1); }
                """;
        write(file, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, file);
        JsonNode declaration = onlyCallable(
                root, source, "declaration", "conditional_b824");
        JsonNode binding = onlyCall(root, source, "conditional_b824")
                .path("payload").path("binding");

        assertEquals("configuration_dependent", binding.path("status").asText());
        assertEquals("conditional_callable_visibility", binding.path("reason").asText());
        assertEquals(1, binding.path("candidateFactIds").size());
        assertEquals(declaration.path("factId"), binding.path("candidateFactIds").get(0));
        assertEquals("partial", completeness(root, "call_binding")
                .path("status").asText());
    }

    @Test
    void inactiveStaticDeclarationCannotChangeActiveDefinitionLinkage()
            throws Exception {
        ParserWorkspace workspace = workspace();
        Path file = workspace.sourceDir().resolve("c-call-binding/inactive-linkage/caller.c");
        String source = """
                #if 0
                static int linkage_728d(int value);
                #endif
                int linkage_728d(int value) { return value; }
                """;
        write(file, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode definition = onlyCallable(
                parse(module, file), source, "definition", "linkage_728d");

        assertEquals("corpus", definition.path("payload")
                .path("targetScope").asText());
        assertEquals("exact", definition.path("payload")
                .path("definitionStatus").asText());
    }

    @Test
    void tagSpellingWithoutCanonicalTagIdentityIsUnavailableEvenInOneSource()
            throws Exception {
        ParserWorkspace workspace = workspace();
        Path file = workspace.sourceDir().resolve("c-call-binding/tag-scope/caller.c");
        String source = """
                int tagged_e420(struct Local_e420 *value);
                struct Local_e420 { int value; };
                int tagged_e420(struct Local_e420 *value) { return value->value; }
                """;
        write(file, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, file);
        List<JsonNode> callable = callableFacts(root, source, "tagged_e420");

        assertEquals(2, callable.size());
        assertTrue(callable.stream().allMatch(fact -> "unavailable".equals(
                fact.path("payload").path("compatibilityStatus").asText())));
        assertTrue(callable.stream().allMatch(fact -> fact.path("payload")
                .path("compatibilityKey").isNull()));
    }

    @Test
    void parenthesizedFunctionDeclaratorKeepsOneAstAndCallableIdentity()
            throws Exception {
        ParserWorkspace workspace = workspace();
        Path file = workspace.sourceDir().resolve(
                "c-call-binding/parenthesized-definition/caller.c");
        String source = """
                int (wrapped_3ef2)(int value) { return value; }
                int caller_3ef2(void) { return wrapped_3ef2(1); }
                """;
        write(file, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, file);
        JsonNode definition = onlyCallable(
                root, source, "definition", "wrapped_3ef2");
        JsonNode binding = onlyCall(root, source, "wrapped_3ef2")
                .path("payload").path("binding");

        assertEquals(1, astNodes(root, "FUNCTION", "wrapped_3ef2").size(),
                () -> root.toPrettyString());
        assertEquals(definition.path("range"), definition.path("payload")
                .path("astNodeRange"));
        assertEquals("declaration_bound", binding.path("status").asText());
        assertEquals("direct_definition", binding.path("resolutionMode").asText());
        assertEquals(definition.path("factId"), binding.path("declarationFactId"));
    }

    private static ParserWorkspace workspace() {
        return new ParserWorkspace(new SourceIntakeClassifier());
    }

    private static void write(Path file, String source) throws Exception {
        Files.createDirectories(file.getParent());
        Files.writeString(file, source, StandardCharsets.UTF_8);
    }

    private static JsonNode parse(CLanguageModule module, Path file) throws Exception {
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
        assertEquals(1, matches.size(), () -> "callable must be exact-once: " + role
                + "/" + name + "; facts="
                + root.path("evidence").path("facts").toPrettyString());
        return matches.get(0);
    }

    private static JsonNode onlyCall(JsonNode root, String source, String name) {
        List<JsonNode> matches = callFacts(root, source, name);
        assertEquals(1, matches.size(), "call fact must be exact-once for " + name);
        return matches.get(0);
    }

    private static List<JsonNode> callFacts(JsonNode root, String source, String name) {
        List<JsonNode> matches = new ArrayList<>();
        root.path("evidence").path("facts").forEach(fact -> {
            if ("call".equals(fact.path("kind").asText())
                    && name.equals(slice(source,
                            fact.path("payload").path("calleeRange")))) {
                matches.add(fact);
            }
        });
        return matches;
    }

    private static List<JsonNode> callableFacts(
            JsonNode root, String source, String name) {
        List<JsonNode> matches = new ArrayList<>();
        root.path("evidence").path("facts").forEach(fact -> {
            if ("callable".equals(fact.path("kind").asText())
                    && name.equals(slice(source,
                            fact.path("payload").path("nameRange")))) {
                matches.add(fact);
            }
        });
        return matches;
    }

    private static JsonNode completeness(JsonNode root, String kind) {
        JsonNode match = null;
        for (JsonNode row : root.path("evidence").path("completeness")) {
            if (kind.equals(row.path("kind").asText())) {
                assertNull(match, "duplicate completeness row for " + kind);
                match = row;
            }
        }
        assertNotNull(match, "missing completeness row for " + kind);
        return match;
    }

    private static List<JsonNode> astNodes(JsonNode root, String type, String name) {
        List<JsonNode> result = new ArrayList<>();
        collectAstNodes(root, type, name, result);
        return result;
    }

    private static void collectAstNodes(
            JsonNode node, String type, String name, List<JsonNode> result) {
        if (type.equals(node.path("type").asText())
                && name.equals(node.path("name").asText())) {
            result.add(node);
        }
        node.path("children").forEach(child -> collectAstNodes(child, type, name, result));
    }

    private static String slice(String source, JsonNode range) {
        int start = range.path("charOffset").asInt();
        int length = range.path("charLength").asInt();
        return new String(source.codePoints().toArray(), start, length);
    }
}
