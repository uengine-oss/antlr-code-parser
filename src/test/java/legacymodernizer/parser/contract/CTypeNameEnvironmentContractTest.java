package legacymodernizer.parser.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

/** Formal red contract for spec 131 C-062 type-name syntax ownership. */
class CTypeNameEnvironmentContractTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @BeforeAll
    static void requireIsolatedDataRoot() {
        String configured = System.getProperty("parser.data.root", "").replace('\\', '/');
        assertTrue(configured.contains("/target/test-data"));
    }

    @Test
    void unrelatedTranslationUnitsAndLexicalShapesCannotSeedTypeNames() throws Exception {
        ParserWorkspace workspace = workspace();
        Path fixtureRoot = workspace.sourceDir().resolve("c-type-environment/cross-tu");
        write(fixtureRoot.resolve("definitions.c"), """
                typedef int CrossTuOnly_7f31;
                /*
                typedef int CommentOnly_7f31;
                */
                struct Shape_7f31 { int value; } OrdinaryInstance_7f31;
                """);
        String consumerSource = """
                void run_7f31(void) {
                    CrossTuOnly_7f31 *first;
                    CommentOnly_7f31 *second;
                    OrdinaryInstance_7f31 *third;
                }
                """;
        Path consumer = fixtureRoot.resolve("consumer.c");
        write(consumer, consumerSource);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, consumer);

        assertEquals("1.3.0", root.path("evidence").path("version").asText());
        for (String name : List.of(
                "CrossTuOnly_7f31", "CommentOnly_7f31", "OrdinaryInstance_7f31")) {
            JsonNode lookup = onlyLookup(root, consumerSource, name);
            assertEquals("ordinary_identifier", lookup.path("payload")
                    .path("parserDecision").asText());
            assertEquals("unresolved", lookup.path("payload")
                    .path("resolutionStatus").asText());
            assertEquals("unresolved_environment", lookup.path("payload")
                    .path("provenance").asText());
            assertTrue(lookup.path("payload").path("definitionFactId").isNull());
        }
        assertPartialSymbolClosure(root, 3);
    }

    @Test
    void headerAndPlatformNamesAreNotLanguageBuiltinTypes() throws Exception {
        ParserWorkspace workspace = workspace();
        Path sourceFile = workspace.sourceDir()
                .resolve("c-type-environment/platform/platform_names.c");
        String source = """
                void run_29ac(void) {
                    HANDLE *handle_29ac;
                    size_t count_29ac;
                    NULL *value_29ac;
                }
                """;
        write(sourceFile, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, sourceFile);

        for (String name : List.of("HANDLE", "size_t", "NULL")) {
            JsonNode lookup = onlyLookup(root, source, name);
            assertEquals("unresolved", lookup.path("payload")
                    .path("resolutionStatus").asText());
            assertEquals("unresolved_environment", lookup.path("payload")
                    .path("provenance").asText());
        }
        List<JsonNode> countDefinitions = symbolFacts(
                root, "definition", source, "count_29ac");
        assertEquals(1, countDefinitions.size(),
                () -> root.path("evidence").path("facts").toPrettyString());
        assertEquals("ordinary_identifier", countDefinitions.get(0).path("payload")
                .path("symbolKind").asText());
        assertEquals("partial", root.path("evidence").path("parseStatus").asText());
        assertPartialSymbolClosure(root, 3);
    }

    @Test
    void sameTranslationUnitTypedefAndOrdinaryShadowUseGrammarScopeAndOrder()
            throws Exception {
        ParserWorkspace workspace = workspace();
        Path sourceFile = workspace.sourceDir()
                .resolve("c-type-environment/scope/local_scope.c");
        String source = """
                typedef int ScopedAlias_4c82;
                ScopedAlias_4c82 file_value_4c82;
                void run_4c82(void) {
                    int ScopedAlias_4c82;
                    ScopedAlias_4c82 *product_4c82;
                }
                """;
        write(sourceFile, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, sourceFile);

        List<JsonNode> definitions = symbolFacts(root, "definition", source, "ScopedAlias_4c82");
        assertEquals(2, definitions.size(),
                () -> "typedef and inner ordinary definition must both exist; facts="
                        + root.path("evidence").path("facts").toPrettyString());
        assertEquals("typedef_name", definitions.get(0).path("payload")
                .path("symbolKind").asText());
        assertEquals("ordinary_identifier", definitions.get(1).path("payload")
                .path("symbolKind").asText());

        List<JsonNode> lookups = symbolFacts(root, "lookup", source, "ScopedAlias_4c82");
        assertEquals(2, lookups.size(), "one file-scope and one shadowed lookup are required");
        JsonNode outer = lookups.get(0);
        assertEquals("type_name", outer.path("payload").path("parserDecision").asText());
        assertEquals("resolved", outer.path("payload").path("resolutionStatus").asText());
        assertEquals(definitions.get(0).path("factId").asText(),
                outer.path("payload").path("definitionFactId").asText());

        JsonNode shadowed = lookups.get(1);
        assertEquals("ordinary_identifier", shadowed.path("payload")
                .path("parserDecision").asText());
        assertEquals("resolved", shadowed.path("payload").path("resolutionStatus").asText());
        assertEquals(definitions.get(1).path("factId").asText(),
                shadowed.path("payload").path("definitionFactId").asText());
        assertEquals("complete", completeness(root, "symbol").path("status").asText());
        assertEquals(0, completeness(root, "symbol")
                .path("explicitlyUnresolved").asInt(-1));
    }

    @Test
    void lookupLedgerIsCanonicalExactOnceAndReplayStable() throws Exception {
        ParserWorkspace workspace = workspace();
        Path sourceFile = workspace.sourceDir()
                .resolve("c-type-environment/replay/replay.c");
        String source = "void run_b11e(void) { MissingType_b11e *value_b11e; }\n";
        write(sourceFile, source);

        CLanguageModule firstModule = new CLanguageModule(workspace);
        firstModule.prepareProjectContext();
        JsonNode first = parse(firstModule, sourceFile);
        CLanguageModule secondModule = new CLanguageModule(workspace);
        secondModule.prepareProjectContext();
        JsonNode second = parse(secondModule, sourceFile);

        JsonNode firstLookup = onlyLookup(first, source, "MissingType_b11e");
        JsonNode secondLookup = onlyLookup(second, source, "MissingType_b11e");
        assertEquals(firstLookup.path("factId").asText(),
                secondLookup.path("factId").asText());
        assertEquals(64, firstLookup.path("factId").asText().length());
        assertEquals(List.of(firstLookup.path("factId").asText()),
                strings(completeness(first, "symbol").path("unresolvedFactIds")));
    }

    @Test
    void typedefVisibilityBeginsAtItsDeclarationPoint() throws Exception {
        ParserWorkspace workspace = workspace();
        Path sourceFile = workspace.sourceDir()
                .resolve("c-type-environment/order/declaration_point.c");
        String source = """
                void run_d50a(void) {
                    OrderedAlias_d50a *before_d50a;
                    typedef int OrderedAlias_d50a;
                    OrderedAlias_d50a *after_d50a;
                }
                """;
        write(sourceFile, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, sourceFile);

        List<JsonNode> definitions = symbolFacts(
                root, "definition", source, "OrderedAlias_d50a");
        assertEquals(1, definitions.size());
        List<JsonNode> lookups = symbolFacts(root, "lookup", source, "OrderedAlias_d50a");
        assertEquals(2, lookups.size());
        assertEquals("unresolved", lookups.get(0).path("payload")
                .path("resolutionStatus").asText());
        assertEquals("ordinary_identifier", lookups.get(0).path("payload")
                .path("parserDecision").asText());
        assertEquals("resolved", lookups.get(1).path("payload")
                .path("resolutionStatus").asText());
        assertEquals("type_name", lookups.get(1).path("payload")
                .path("parserDecision").asText());
        assertEquals(definitions.get(0).path("factId").asText(),
                lookups.get(1).path("payload").path("definitionFactId").asText());
    }

    @Test
    void parameterOrdinaryIdentifierShadowsTypedefInFunctionBody() throws Exception {
        ParserWorkspace workspace = workspace();
        Path sourceFile = workspace.sourceDir()
                .resolve("c-type-environment/scope/parameter_body.c");
        String source = """
                typedef int ParamAlias_f43b;
                void run_f43b(int ParamAlias_f43b) {
                    ParamAlias_f43b *value_f43b;
                }
                """;
        write(sourceFile, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, sourceFile);

        List<JsonNode> definitions = symbolFacts(
                root, "definition", source, "ParamAlias_f43b");
        assertEquals(2, definitions.size(),
                () -> root.path("evidence").path("facts").toPrettyString());
        assertEquals("typedef_name", definitions.get(0).path("payload")
                .path("symbolKind").asText());
        assertEquals("ordinary_identifier", definitions.get(1).path("payload")
                .path("symbolKind").asText());
        assertEquals("block", definitions.get(1).path("payload")
                .path("scopeKind").asText());

        JsonNode lookup = onlyLookup(root, source, "ParamAlias_f43b");
        assertEquals("ordinary_identifier", lookup.path("payload")
                .path("parserDecision").asText());
        assertEquals("resolved", lookup.path("payload")
                .path("resolutionStatus").asText());
        assertEquals(definitions.get(1).path("factId").asText(),
                lookup.path("payload").path("definitionFactId").asText());
    }

    @Test
    void prototypeParameterScopeDoesNotLeakPastFunctionDeclarator() throws Exception {
        ParserWorkspace workspace = workspace();
        Path sourceFile = workspace.sourceDir()
                .resolve("c-type-environment/scope/prototype_scope.c");
        String source = """
                typedef int ProtoAlias_0bce;
                void declared_0bce(int ProtoAlias_0bce,
                        int values_0bce[sizeof(ProtoAlias_0bce)]);
                ProtoAlias_0bce after_0bce;
                """;
        write(sourceFile, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, sourceFile);

        List<JsonNode> definitions = symbolFacts(
                root, "definition", source, "ProtoAlias_0bce");
        assertEquals(2, definitions.size(),
                () -> root.path("evidence").path("facts").toPrettyString());
        assertEquals("function_prototype", definitions.get(1).path("payload")
                .path("scopeKind").asText());
        List<JsonNode> lookups = symbolFacts(root, "lookup", source, "ProtoAlias_0bce");
        assertEquals(2, lookups.size(),
                () -> root.path("evidence").path("facts").toPrettyString());
        assertEquals("ordinary_identifier", lookups.get(0).path("payload")
                .path("parserDecision").asText());
        assertEquals(definitions.get(1).path("factId").asText(),
                lookups.get(0).path("payload").path("definitionFactId").asText());
        assertEquals("type_name", lookups.get(1).path("payload")
                .path("parserDecision").asText());
        assertEquals(definitions.get(0).path("factId").asText(),
                lookups.get(1).path("payload").path("definitionFactId").asText());
    }

    @Test
    void enumeratorOrdinaryIdentifierShadowsTypedefOnlyInsideItsBlock() throws Exception {
        ParserWorkspace workspace = workspace();
        Path sourceFile = workspace.sourceDir()
                .resolve("c-type-environment/scope/enumerator_scope.c");
        String source = """
                typedef int EnumAlias_6e21;
                void run_6e21(void) {
                    {
                        enum { EnumAlias_6e21 = 1 };
                        EnumAlias_6e21 *shadowed_6e21;
                    }
                    EnumAlias_6e21 *outer_6e21;
                }
                """;
        write(sourceFile, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, sourceFile);

        List<JsonNode> definitions = symbolFacts(
                root, "definition", source, "EnumAlias_6e21");
        assertEquals(2, definitions.size(),
                () -> root.path("evidence").path("facts").toPrettyString());
        assertEquals("ordinary_identifier", definitions.get(1).path("payload")
                .path("symbolKind").asText());
        List<JsonNode> lookups = symbolFacts(root, "lookup", source, "EnumAlias_6e21");
        assertEquals(2, lookups.size(),
                () -> root.path("evidence").path("facts").toPrettyString());
        assertEquals("ordinary_identifier", lookups.get(0).path("payload")
                .path("parserDecision").asText());
        assertEquals(definitions.get(1).path("factId").asText(),
                lookups.get(0).path("payload").path("definitionFactId").asText());
        assertEquals("type_name", lookups.get(1).path("payload")
                .path("parserDecision").asText());
        assertEquals(definitions.get(0).path("factId").asText(),
                lookups.get(1).path("payload").path("definitionFactId").asText());
    }

    @Test
    void priorGrammarTypeSpecifierMakesOrdinaryDeclaratorEnvironmentIndependent()
            throws Exception {
        ParserWorkspace workspace = workspace();
        Path sourceFile = workspace.sourceDir()
                .resolve("c-type-environment/grammar/prior_type_specifier.c");
        String source = """
                struct Record_7a91 {
                    int member_7a91;
                };
                """;
        write(sourceFile, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, sourceFile);

        JsonNode lookup = onlyLookup(root, source, "member_7a91");
        assertEquals("ordinary_identifier", lookup.path("payload")
                .path("parserDecision").asText());
        assertEquals("resolved", lookup.path("payload")
                .path("resolutionStatus").asText());
        assertEquals("grammar_context", lookup.path("payload")
                .path("provenance").asText());
        assertTrue(lookup.path("payload").path("definitionFactId").isNull());
        assertTrue(lookup.path("payload").path("configuredEvidenceId").isNull());
        assertEquals("complete", completeness(root, "symbol").path("status").asText());
    }

    @Test
    void grammarConfirmedCallTargetDoesNotBecomeTypeNameUnresolved() throws Exception {
        ParserWorkspace workspace = workspace();
        Path sourceFile = workspace.sourceDir()
                .resolve("c-type-environment/grammar/external_call.c");
        String source = "void run_62f1(void) { target_62f1(1); }\n";
        write(sourceFile, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, sourceFile);

        assertTrue(symbolFacts(root, "lookup", source, "target_62f1").isEmpty(),
                () -> root.path("evidence").path("facts").toPrettyString());
        assertEquals("exact", root.path("evidence").path("parseStatus").asText());
        assertEquals("complete", completeness(root, "symbol").path("status").asText());
        assertTrue(root.path("evidence").path("facts").findValuesAsText("terminalName")
                .contains("target_62f1"));
    }

    @Test
    void declarationOnlyUnknownParameterTypePreservesFunctionOwnerAndBody()
            throws Exception {
        ParserWorkspace workspace = workspace();
        Path sourceFile = workspace.sourceDir()
                .resolve("c-type-environment/grammar/declaration_only_parameter.c");
        String source = """
                static long declared_83d1(MissingParamType_83d1 *itf_83d1);
                static long implemented_83d1(MissingParamType_83d1 *itf_83d1) {
                    target_83d1();
                    return 0;
                }
                """;
        write(sourceFile, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, sourceFile);

        assertTrue(astNodes(root, "FUNCTION", "itf_83d1").isEmpty(),
                () -> root.toPrettyString());
        JsonNode function = onlyAstNode(root, "FUNCTION", "implemented_83d1");
        assertFalse(astNodes(function, "FUNCTION_CALL", "target_83d1").isEmpty(),
                () -> function.toPrettyString());
        assertFalse(astNodes(function, "RETURN", "").isEmpty(),
                () -> function.toPrettyString());

        List<JsonNode> lookups = symbolFacts(
                root, "lookup", source, "MissingParamType_83d1");
        assertEquals(2, lookups.size(), () -> root.path("evidence")
                .path("facts").toPrettyString());
        lookups.forEach(lookup -> {
            assertEquals("type_name", lookup.path("payload")
                    .path("parserDecision").asText());
            assertEquals("unresolved", lookup.path("payload")
                    .path("resolutionStatus").asText());
            assertTrue(strings(lookup.path("payload").path("predicateContexts"))
                    .contains("declaration_only_context"));
        });
        assertEquals("partial", root.path("evidence").path("parseStatus").asText());
    }

    @Test
    void declarationOnlyUnknownReturnTypePreservesSignatureAndBody() throws Exception {
        ParserWorkspace workspace = workspace();
        Path sourceFile = workspace.sourceDir()
                .resolve("c-type-environment/grammar/declaration_only_return_type.c");
        String source = """
                MissingReturnType_61df *run_61df(void) {
                    target_61df();
                    return 0;
                }
                """;
        write(sourceFile, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, sourceFile);

        JsonNode function = onlyAstNode(root, "FUNCTION", "run_61df");
        assertEquals("MissingReturnType_61df *", function.path("returnType").asText(),
                () -> root.toPrettyString());
        assertFalse(astNodes(function, "FUNCTION_CALL", "target_61df").isEmpty(),
                () -> function.toPrettyString());
        assertFalse(astNodes(function, "RETURN", "").isEmpty(),
                () -> function.toPrettyString());

        JsonNode lookup = onlyLookup(root, source, "MissingReturnType_61df");
        assertEquals("type_name", lookup.path("payload")
                .path("parserDecision").asText());
        assertTrue(strings(lookup.path("payload").path("predicateContexts"))
                .contains("external_declaration_only_parse"),
                () -> lookup.toPrettyString());
    }

    @Test
    void grammarConfirmedAssignmentDoesNotBecomeTypeNameLookup() throws Exception {
        ParserWorkspace workspace = workspace();
        Path sourceFile = workspace.sourceDir()
                .resolve("c-type-environment/grammar/expression_statement.c");
        String source = """
                struct Record_5ac2 { int member_5ac2; };
                void run_5ac2(void) {
                    struct Record_5ac2 value_5ac2;
                    value_5ac2.member_5ac2 = 1;
                    target_5ac2(1);
                }
                """;
        write(sourceFile, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, sourceFile);

        assertTrue(symbolFacts(root, "lookup", source, "value_5ac2").isEmpty(),
                () -> root.path("evidence").path("facts").toPrettyString());
        assertTrue(symbolFacts(root, "lookup", source, "target_5ac2").isEmpty(),
                () -> root.path("evidence").path("facts").toPrettyString());
        assertEquals("exact", root.path("evidence").path("parseStatus").asText());
        assertEquals("complete", completeness(root, "symbol").path("status").asText());
    }

    @Test
    void castOnlyUnknownTypePreservesControlFlowAndFollowingFunctions() throws Exception {
        ParserWorkspace workspace = workspace();
        Path sourceFile = workspace.sourceDir()
                .resolve("c-type-environment/grammar/cast_only_type.c");
        String source = """
                void run_91b7(int input_91b7) {
                    if (input_91b7 == (MissingCastType_91b7)1) {
                        first_91b7();
                    } else {
                        second_91b7();
                    }
                    return;
                }
                void after_91b7(void) { third_91b7(); }
                """;
        write(sourceFile, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, sourceFile);

        JsonNode function = onlyAstNode(root, "FUNCTION", "run_91b7");
        assertFalse(astNodes(function, "ELSE", "").isEmpty(),
                () -> function.toPrettyString());
        assertFalse(astNodes(function, "FUNCTION_CALL", "second_91b7").isEmpty(),
                () -> function.toPrettyString());
        assertFalse(astNodes(function, "RETURN", "").isEmpty(),
                () -> function.toPrettyString());
        assertFalse(astNodes(onlyAstNode(root, "FUNCTION", "after_91b7"),
                "FUNCTION_CALL", "third_91b7").isEmpty(), () -> root.toPrettyString());

        JsonNode lookup = onlyLookup(root, source, "MissingCastType_91b7");
        assertEquals("type_name", lookup.path("payload")
                .path("parserDecision").asText());
        assertTrue(strings(lookup.path("payload").path("predicateContexts"))
                .contains("cast_only_parse"));
    }

    @Test
    void syntacticallyAmbiguousCastOrExpressionIsNotInventedAsType() throws Exception {
        ParserWorkspace workspace = workspace();
        Path sourceFile = workspace.sourceDir()
                .resolve("c-type-environment/grammar/ambiguous_cast.c");
        String source = """
                int run_a42c(void) {
                    return (MaybeTypeOrValue_a42c) + 1;
                }
                """;
        write(sourceFile, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, sourceFile);

        JsonNode lookup = onlyLookup(root, source, "MaybeTypeOrValue_a42c");
        assertEquals("ordinary_identifier", lookup.path("payload")
                .path("parserDecision").asText());
        assertTrue(strings(lookup.path("payload").path("predicateContexts"))
                .contains("ambiguous_cast_expression"));
        assertFalse(astNodes(onlyAstNode(root, "FUNCTION", "run_a42c"),
                "RETURN", "").isEmpty(), () -> root.toPrettyString());
    }

    @Test
    void trueBlockItemAmbiguityIsDiagnosedWithoutCascadingPastIt() throws Exception {
        ParserWorkspace workspace = workspace();
        Path sourceFile = workspace.sourceDir()
                .resolve("c-type-environment/grammar/ambiguous_block_item.c");
        String source = """
                void run_c71e(void) {
                    MaybeTypeOrValue_c71e *value_c71e;
                    target_c71e();
                    return;
                }
                """;
        write(sourceFile, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, sourceFile);

        JsonNode lookup = onlyLookup(root, source, "MaybeTypeOrValue_c71e");
        assertEquals("ordinary_identifier", lookup.path("payload")
                .path("parserDecision").asText());
        assertEquals("unresolved", lookup.path("payload")
                .path("resolutionStatus").asText());
        assertTrue(strings(lookup.path("payload").path("predicateContexts"))
                .contains("ambiguous_block_item"));
        JsonNode function = onlyAstNode(root, "FUNCTION", "run_c71e");
        assertFalse(astNodes(function, "FUNCTION_CALL", "target_c71e").isEmpty(),
                () -> function.toPrettyString());
        assertFalse(astNodes(function, "RETURN", "").isEmpty(),
                () -> function.toPrettyString());
        assertEquals("partial", root.path("evidence").path("parseStatus").asText());
    }

    @Test
    void declarationShapedUnknownLocalDoesNotConsumeFollowingStatements()
            throws Exception {
        ParserWorkspace workspace = workspace();
        Path sourceFile = workspace.sourceDir()
                .resolve("c-type-environment/grammar/declaration_shaped_local.c");
        String source = """
                void run_f219(void) {
                    MissingLocalType_f219 value_f219;
                    target_f219();
                    return;
                }
                """;
        write(sourceFile, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, sourceFile);

        JsonNode lookup = onlyLookup(root, source, "MissingLocalType_f219");
        assertEquals("type_name", lookup.path("payload")
                .path("parserDecision").asText());
        assertTrue(strings(lookup.path("payload").path("predicateContexts"))
                .contains("declaration_only_parse"));
        JsonNode function = onlyAstNode(root, "FUNCTION", "run_f219");
        assertFalse(astNodes(function, "FUNCTION_CALL", "target_f219").isEmpty(),
                () -> function.toPrettyString());
        assertFalse(astNodes(function, "RETURN", "").isEmpty(),
                () -> function.toPrettyString());
        assertTrue(astNodes(root, "FUNCTION", "value_f219").isEmpty(),
                () -> root.toPrettyString());
    }

    @Test
    void implicitIntFunctionDeclaratorIsNotInventedAsATypeName() throws Exception {
        ParserWorkspace workspace = workspace();
        Path sourceFile = workspace.sourceDir()
                .resolve("c-type-environment/grammar/implicit_int_function.c");
        String source = """
                legacy_entry_84e1(void) {
                    target_84e1();
                    return 0;
                }
                """;
        write(sourceFile, source);

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        JsonNode root = parse(module, sourceFile);

        JsonNode function = onlyAstNode(root, "FUNCTION", "legacy_entry_84e1");
        assertFalse(astNodes(function, "FUNCTION_CALL", "target_84e1").isEmpty(),
                () -> function.toPrettyString());
        assertFalse(astNodes(function, "RETURN", "").isEmpty(),
                () -> function.toPrettyString());
        assertTrue(symbolFacts(root, "lookup", source, "legacy_entry_84e1").isEmpty(),
                () -> root.path("evidence").path("facts").toPrettyString());
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

    private static JsonNode onlyLookup(JsonNode root, String source, String name) {
        List<JsonNode> matches = symbolFacts(root, "lookup", source, name);
        assertEquals(1, matches.size(), "lookup must be canonical exact-once for " + name);
        return matches.get(0);
    }

    private static JsonNode onlyAstNode(JsonNode root, String type, String name) {
        List<JsonNode> matches = astNodes(root, type, name);
        assertEquals(1, matches.size(), "AST node must be canonical exact-once: "
                + type + "/" + name);
        return matches.get(0);
    }

    private static List<JsonNode> astNodes(JsonNode root, String type, String name) {
        List<JsonNode> result = new ArrayList<>();
        collectAstNodes(root, type, name, result);
        return result;
    }

    private static void collectAstNodes(
            JsonNode node, String type, String name, List<JsonNode> result) {
        if (type.equals(node.path("type").asText())
                && (name.isEmpty() || name.equals(node.path("name").asText()))) {
            result.add(node);
        }
        node.path("children").forEach(child -> collectAstNodes(child, type, name, result));
    }

    private static List<JsonNode> symbolFacts(
            JsonNode root, String role, String source, String name) {
        List<JsonNode> result = new ArrayList<>();
        root.path("evidence").path("facts").forEach(fact -> {
            if ("symbol".equals(fact.path("kind").asText())
                    && role.equals(fact.path("payload").path("role").asText())
                    && name.equals(slice(source, fact.path("range")))) {
                result.add(fact);
            }
        });
        return result;
    }

    private static void assertPartialSymbolClosure(JsonNode root, int unresolved) {
        JsonNode completeness = completeness(root, "symbol");
        assertEquals("partial", completeness.path("status").asText());
        assertEquals("insufficient_type_name_environment",
                completeness.path("reason").asText());
        assertEquals(unresolved, completeness.path("unresolvedFactIds").size(),
                () -> "unexpected unresolved symbol ledger: " + completeness.toPrettyString()
                        + "; facts=" + root.path("evidence").path("facts").toPrettyString());
        assertEquals(completeness.path("population").asInt(),
                completeness.path("emitted").asInt());
        assertEquals(0, completeness.path("explicitlyUnresolved").asInt(-1));
        assertFalse(strings(completeness.path("unresolvedFactIds")).contains(""));
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

    private static List<String> strings(JsonNode values) {
        List<String> result = new ArrayList<>();
        values.forEach(value -> result.add(value.asText()));
        return result;
    }

    private static String slice(String source, JsonNode range) {
        int start = range.path("charOffset").asInt();
        int length = range.path("charLength").asInt();
        return new String(source.codePoints().toArray(), start, length);
    }
}
