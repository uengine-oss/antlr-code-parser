package legacymodernizer.parser.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.intake.SourceIntakeClassifier;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.parsing.languages.LanguageModule;
import legacymodernizer.parser.parsing.languages.c.CLanguageModule;
import legacymodernizer.parser.parsing.languages.java.JavaLanguageModule;
import legacymodernizer.parser.parsing.languages.oracle.OracleLanguageModule;
import legacymodernizer.parser.parsing.languages.postgresql.PostgreSqlLanguageModule;
import legacymodernizer.parser.parsing.languages.python.PythonLanguageModule;
import legacymodernizer.parser.service.ParseProgressTracker;

/** Red contract for spec 131 common evidence IR. */
class SemanticEvidenceIrContractTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @BeforeAll
    static void requireIsolatedDataRoot() {
        String configured = System.getProperty("parser.data.root", "").replace('\\', '/');
        assertTrue(configured.contains("/target/test-data"),
                "Evidence tests must run only under target/test-data: " + configured);
    }

    @Test
    void everyFrontendPublishesAnExactVersionedSourceEnvelope() throws Exception {
        ParserWorkspace workspace = workspace();
        Map<String, Fixture> fixtures = new LinkedHashMap<>();
        fixtures.put("c", new Fixture(new CLanguageModule(workspace), "binding/sample.c",
                "void run(void) { target(1); }\n"));
        fixtures.put("java", new Fixture(new JavaLanguageModule(workspace), "binding/Sample.java",
                "class Sample { void run() { target(1); } }\n"));
        fixtures.put("python", new Fixture(new PythonLanguageModule(workspace), "binding/sample.py",
                "def run():\n    target(1)\n"));
        fixtures.put("oracle", new Fixture(new OracleLanguageModule(workspace), "binding/sample.prc",
                "CREATE OR REPLACE PROCEDURE run AS BEGIN NULL; END;\n/\n"));
        fixtures.put("postgresql", new Fixture(new PostgreSqlLanguageModule(workspace),
                "binding/sample.sql",
                "CREATE OR REPLACE FUNCTION run() RETURNS void LANGUAGE plpgsql "
                        + "AS $$ BEGIN NULL; END; $$;\n"));

        for (Map.Entry<String, Fixture> entry : fixtures.entrySet()) {
            JsonNode evidence = parse(workspace, entry.getValue()).path("evidence");
            assertFalse(evidence.isMissingNode(), entry.getKey() + " omitted evidence envelope");
            assertEquals("1.1.0", evidence.path("version").asText());
            assertEquals(entry.getValue().relativePath(), evidence.path("sourceId").asText());
            assertEquals(64, evidence.path("rawSourceSha256").asText().length());
            assertEquals(64, evidence.path("decodedTextSha256").asText().length());
            assertEquals("UTF-8", evidence.path("sourceEncoding").asText());
            assertEquals("exact", evidence.path("decodeStatus").asText());
            assertEquals(entry.getValue().source(), evidence.path("decodedText").asText(),
                    "the exact decoded source must be sealed once in the envelope");
            assertEquals("unicode-code-point", evidence.path("positionEncoding").asText());
            assertEquals("half-open", evidence.path("rangeConvention").asText());
            assertEquals("char-offset-length", evidence.path("rangeEncoding").asText());
            assertEquals("exact", evidence.path("parseStatus").asText());
            assertTrue(evidence.path("facts").isArray());
            assertTrue(evidence.path("grammarRules").isArray());
            assertTrue(evidence.path("presences").isArray());
            assertTrue(evidence.path("completeness").isArray());
            if ("c".equals(entry.getKey())) {
                JsonNode configured = evidence.path("configuredPreprocessing");
                assertEquals("1.0.0", configured.path("version").asText());
                assertEquals("unresolved", configured.path("status").asText());
                assertEquals("unresolved", configured.path("trust").asText());
                assertEquals("unresolved", configured.path("build").path("status").asText());
                assertEquals(1, configured.path("build").path("population").asInt());
                assertEquals(0, configured.path("build").path("emitted").asInt());
                assertEquals(1, configured.path("build")
                        .path("explicitlyUnresolved").asInt());
                assertEquals(1, configured.path("build")
                        .path("unresolvedEvidenceIds").size());
                assertEquals(List.of("insufficient_compilation_database"),
                        stringValues(configured.path("build").path("reasons")));
                assertEquals("unresolved", configured.path("trace").path("status").asText());
                assertEquals(1, configured.path("trace").path("evidenceIds").size());
                assertEquals(0, configured.path("trace").path("emittedEvidenceIds").size());
                assertEquals(1, configured.path("trace").path("unresolvedEvidenceIds").size());
                assertEquals(List.of("insufficient_preprocessing_build_context"),
                        stringValues(configured.path("trace").path("reasons")));
            } else {
                assertTrue(evidence.path("configuredPreprocessing").isMissingNode(),
                        entry.getKey() + " must not inherit C preprocessing semantics");
            }
        }
    }

    @Test
    void callFactsUseFullGrammarExpressionsAndUnicodeCodePointRanges() throws Exception {
        ParserWorkspace workspace = workspace();
        String cSource = "void run(void) { service->handler(1); (*callback)(2); }\n";
        JsonNode cRoot = parse(workspace, new Fixture(new CLanguageModule(workspace),
                "ranges/sample.c", cSource));
        JsonNode cNamed = callByCallee(cRoot, cSource, "service->handler");
        assertEquals("named", cNamed.path("payload").path("calleeKind").asText());
        assertEquals("handler", cNamed.path("payload").path("terminalName").asText());
        JsonNode cExpression = callByCallee(cRoot, cSource, "(*callback)");
        assertEquals("expression", cExpression.path("payload").path("calleeKind").asText());
        assertTrue(cExpression.path("payload").path("terminalName").isNull());

        String javaSource = "class Sample { void run() { String marker = \"😀\";\n"
                + "  service.repo().find(\n    nested(1), 2);\n} }\n";
        JsonNode javaRoot = parse(workspace, new Fixture(new JavaLanguageModule(workspace),
                "ranges/Sample.java", javaSource));
        JsonNode javaCall = callByCallee(javaRoot, javaSource, "service.repo().find");
        assertEquals("named", javaCall.path("payload").path("calleeKind").asText());
        assertEquals("find", javaCall.path("payload").path("terminalName").asText());
        assertEquals("service.repo().find(\n    nested(1), 2)",
                slice(javaSource, javaCall.path("range")));
        assertEquals(List.of("nested(1)", "2"), argumentExpressions(javaSource, javaCall));
        assertNoDuplicatedSourceText(javaCall);
        assertExactSlice(javaSource, javaCall);

        String anonymousJava = "class Sample { void make() { Object value = new Runnable() "
                + "{ public void run() {} }; } }\n";
        JsonNode anonymousJavaRoot = parse(workspace, new Fixture(new JavaLanguageModule(workspace),
                "ranges/Anonymous.java", anonymousJava));
        JsonNode constructor = callByCallee(anonymousJavaRoot, anonymousJava, "new Runnable");
        assertEquals("constructor", constructor.path("payload").path("calleeKind").asText());
        assertEquals("Runnable", constructor.path("payload").path("terminalName").asText());
        assertEquals("new Runnable()", slice(anonymousJava, constructor.path("range")),
                "anonymous class body is not part of the constructor call range");
        assertExactSlice(anonymousJava, constructor);

        String pythonSource = "def run():\n    marker = '😀'\n    service.repo().find(\n"
                + "        nested(1), 2)\n";
        JsonNode pythonRoot = parse(workspace, new Fixture(new PythonLanguageModule(workspace),
                "ranges/sample.py", pythonSource));
        JsonNode pythonCall = callByCallee(pythonRoot, pythonSource, "service.repo().find");
        assertEquals("named", pythonCall.path("payload").path("calleeKind").asText());
        assertEquals("find", pythonCall.path("payload").path("terminalName").asText());
        assertEquals("service.repo().find(\n        nested(1), 2)",
                slice(pythonSource, pythonCall.path("range")));
        assertEquals(List.of("nested(1)", "2"), argumentExpressions(pythonSource, pythonCall));
        assertNoDuplicatedSourceText(pythonCall);
        assertExactSlice(pythonSource, pythonCall);

        String anonymousPython = "value = (lambda x: x)(1)\n";
        JsonNode anonymousPythonRoot = parse(workspace,
                new Fixture(new PythonLanguageModule(workspace),
                        "ranges/anonymous.py", anonymousPython));
        JsonNode anonymousCall = callByCallee(
                anonymousPythonRoot, anonymousPython, "(lambda x: x)");
        assertEquals("expression", anonymousCall.path("payload").path("calleeKind").asText());
        assertTrue(anonymousCall.path("payload").path("terminalName").isNull());
        assertEquals("(lambda x: x)(1)",
                slice(anonymousPython, anonymousCall.path("range")));
        assertExactSlice(anonymousPython, anonymousCall);
    }

    @Test
    void cMacroFactsComeFromPreprocessorGrammarNotSourceRegex() throws Exception {
        ParserWorkspace workspace = workspace();
        String source = "#define OBJECT (1)\n"
                + "#define EMPTY()\n"
                + "#define SUM(left, right) ((left) + (right))\n"
                + "#define LOG(format, ...) emit(format, __VA_ARGS__)\n"
                + "#define CONTINUED(value) \\\n  consume(value)\n"
                + "#if 0\n#define HIDDEN(value) consume(value)\n#endif\n"
                + "void run(void) { SUM(1, 2); }\n";
        JsonNode root = parse(workspace, new Fixture(new CLanguageModule(workspace),
                "macro/sample.c", source));

        List<JsonNode> macros = facts(root, "macro");
        assertEquals(6, macros.size());
        assertEquals(List.of("OBJECT", "EMPTY", "SUM", "LOG", "CONTINUED", "HIDDEN"),
                macros.stream().map(fact -> fact.path("payload").path("terminalName").asText())
                        .toList());
        assertEquals(List.of("object", "function", "function", "function", "function",
                        "function"),
                macros.stream().map(fact -> fact.path("payload").path("macroKind").asText())
                        .toList());
        JsonNode log = macros.get(3);
        List<String> parameters = new ArrayList<>();
        log.path("payload").path("parameterRanges").forEach(range ->
                parameters.add(slice(source, range)));
        assertEquals(List.of("format"), parameters);
        assertTrue(log.path("payload").path("variadic").asBoolean());
        assertEquals("inactive", presence(root, macros.get(5)).path("status").asText());
        macros.forEach(fact -> assertExactSlice(source, fact));

        JsonNode completeness = completeness(root, "macro");
        assertEquals("complete", completeness.path("status").asText());
        assertEquals(6, completeness.path("population").asInt());
        assertEquals(6, completeness.path("emitted").asInt());
        assertEquals(0, completeness.path("explicitlyUnresolved").asInt());

        String commentSeparated = "#/**/define COMMENT_SEPARATED (7)\n";
        JsonNode commentRoot = parse(workspace, new Fixture(new CLanguageModule(workspace),
                "macro/comment-separated.c", commentSeparated));
        JsonNode legacyDefine = children(commentRoot, "DEFINE").stream()
                .filter(node -> "COMMENT_SEPARATED".equals(node.path("name").asText()))
                .findFirst().orElseThrow();
        assertEquals("(7)", legacyDefine.path("initValue").asText(),
                "legacy projection must consume grammar evidence, not directive text regex");
    }

    @Test
    void cMacroGrammarPreservesWhitespaceSplicingExtensionsAndMalformedAccounting()
            throws Exception {
        ParserWorkspace workspace = workspace();
        String source = "#define OBJECT_WITH_PAREN (value)\r\n"
                + "#define COMMENT_GAP/**/(value)\r\n"
                + "#define FUNCTION(value) value\r\n"
                + "#define GNU_VARIADIC(arguments...) emit(arguments)\r\n"
                + "#define CRLF_SPLICE(value) \\\r\n  emit(value)\r\n"
                + "#define CR_SPLICE(value) \\\r  emit(value)\r"
                + "#define EMPTY_OBJECT\r\n"
                + "#define BLOCK_COMMENT /* first\r\n second */ replacement\r\n"
                + "#define BEFORE_COMMENT/**/value\r\n"
                + "%:define DIGRAPH(value) value\r\n"
                + "#define 123 invalid\r\n"
                + "#define UNTERMINATED(value\r\n"
                + "#define UNTERMINATED_COMMENT /* no end";
        JsonNode root = parse(workspace, new Fixture(new CLanguageModule(workspace),
                "macro/edge-cases.c", source));

        List<JsonNode> macros = facts(root, "macro");
        assertEquals(List.of("OBJECT_WITH_PAREN", "COMMENT_GAP", "FUNCTION",
                        "GNU_VARIADIC", "CRLF_SPLICE", "CR_SPLICE", "EMPTY_OBJECT",
                        "BLOCK_COMMENT", "BEFORE_COMMENT", "DIGRAPH"),
                macros.stream().map(fact -> fact.path("payload").path("terminalName").asText())
                        .toList());
        assertEquals(List.of("object", "object", "function", "function", "function",
                        "function", "object", "object", "object", "function"),
                macros.stream().map(fact -> fact.path("payload").path("macroKind").asText())
                        .toList());
        assertTrue(macros.get(3).path("payload").path("variadic").asBoolean());
        assertEquals(List.of("arguments"), rangeSlices(source,
                macros.get(3).path("payload").path("parameterRanges")));
        assertEquals("emit(value)", slice(source,
                macros.get(5).path("payload").path("replacementRange")));
        assertTrue(macros.get(7).path("payload").path("replacementRange").isNull(),
                "a retained newline inside a block comment ends the directive");
        assertEquals("value", slice(source,
                macros.get(8).path("payload").path("replacementRange")));
        macros.forEach(fact -> assertExactSlice(source, fact));

        JsonNode completeness = completeness(root, "macro");
        assertEquals("partial", completeness.path("status").asText());
        assertEquals("insufficient_preprocessor_directive_syntax",
                completeness.path("reason").asText());
        assertEquals(List.of("insufficient_preprocessor_directive_syntax",
                        "insufficient_unterminated_preprocessor_comment"),
                stringValues(completeness.path("reasons")));
        assertEquals(13, completeness.path("population").asInt());
        assertEquals(10, completeness.path("emitted").asInt());
        assertEquals(3, completeness.path("explicitlyUnresolved").asInt());
    }

    @Test
    void cIncludeAndConditionalSyntaxComeFromThePreprocessorGrammar() throws Exception {
        ParserWorkspace workspace = workspace();
        String source = "#/**/include <local/config.h>\n"
                + "%:include \"digraph.h\"\n"
                + "#inc\\\nlude \"spliced.h\"\n"
                + "void run(void) {\n"
                + "#/**/if 0\n"
                + "  hidden_call();\n"
                + "#/**/else\n"
                + "  visible_call();\n"
                + "#/**/endif\n"
                + "%:if 0\n"
                + "  digraph_hidden();\n"
                + "%:else\n"
                + "  digraph_visible();\n"
                + "%:endif\n"
                + "}\n";
        JsonNode root = parse(workspace, new Fixture(new CLanguageModule(workspace),
                "preprocessor/directives.c", source));

        assertEquals(List.of("<local/config.h>", "\"digraph.h\"", "\"spliced.h\""),
                children(root, "INCLUDE").stream()
                        .map(node -> node.path("name").asText())
                        .toList(),
                "legacy INCLUDE projection must be sourced from grammar facts");

        Map<String, JsonNode> callsByName = new LinkedHashMap<>();
        for (JsonNode call : facts(root, "call")) {
            callsByName.put(call.path("payload").path("terminalName").asText(), call);
        }
        assertEquals("inactive", presence(root, callsByName.get("hidden_call"))
                .path("status").asText());
        assertEquals("active", presence(root, callsByName.get("visible_call"))
                .path("status").asText());
        assertEquals("inactive", presence(root, callsByName.get("digraph_hidden"))
                .path("status").asText());
        assertEquals("active", presence(root, callsByName.get("digraph_visible"))
                .path("status").asText());
    }

    @Test
    void malformedCIncludeIsExplicitlyUnresolvedInsteadOfSilentlyComplete() throws Exception {
        ParserWorkspace workspace = workspace();
        String source = "#include <valid.h>\n#include\nvoid run(void) {}\n";
        JsonNode root = parse(workspace, new Fixture(new CLanguageModule(workspace),
                "preprocessor/malformed-include.c", source));

        assertEquals(1, facts(root, "import").size());
        assertEquals(List.of("<valid.h>"), children(root, "INCLUDE").stream()
                .map(node -> node.path("name").asText()).toList());
        JsonNode completeness = completeness(root, "import");
        assertEquals("partial", completeness.path("status").asText());
        assertEquals(2, completeness.path("population").asInt());
        assertEquals(1, completeness.path("emitted").asInt());
        assertEquals(1, completeness.path("explicitlyUnresolved").asInt());
        assertEquals("insufficient_preprocessor_directive_syntax",
                completeness.path("reason").asText());
    }

    @Test
    void importsPublishGrammarOwnedOrderedBindingEntriesAcrossLanguages() throws Exception {
        ParserWorkspace workspace = workspace();

        String cSource = "#include <system/api.h>\n"
                + "#include \"local/config.h\"\n"
                + "#include HEADER_MACRO\n";
        JsonNode cRoot = parse(workspace, new Fixture(new CLanguageModule(workspace),
                "imports/sample.c", cSource));
        List<JsonNode> cImports = facts(cRoot, "import");
        assertEquals(3, cImports.size());
        assertImportEntry(cRoot, cSource, cImports.get(0), 0,
                "source_file", "angle", "<system/api.h>",
                List.of("system/api.h"), null, null, 0, false, "system");
        assertImportEntry(cRoot, cSource, cImports.get(1), 0,
                "source_file", "quoted", "\"local/config.h\"",
                List.of("local/config.h"), null, null, 0, false, "local");
        assertImportEntry(cRoot, cSource, cImports.get(2), 0,
                "computed", "computed", "HEADER_MACRO",
                List.of(), null, null, 0, false, "unspecified");

        String javaSource = "package use;\n"
                + "import pkg.Type;\n"
                + "import pkg.types.*;\n"
                + "import static pkg.Constants.VALUE;\n"
                + "import static pkg.Constants.*;\n"
                + "class Sample {}\n";
        JsonNode javaRoot = parse(workspace, new Fixture(new JavaLanguageModule(workspace),
                "imports/Sample.java", javaSource));
        List<JsonNode> javaImports = facts(javaRoot, "import");
        assertEquals(4, javaImports.size());
        assertImportEntry(javaRoot, javaSource, javaImports.get(0), 0,
                "type", "qualified", "pkg.Type", List.of("pkg", "Type"),
                null, null, 0, false, "unspecified");
        assertImportEntry(javaRoot, javaSource, javaImports.get(1), 0,
                "namespace", "qualified", "pkg.types.*", List.of("pkg", "types"),
                null, null, 0, true, "unspecified");
        assertImportEntry(javaRoot, javaSource, javaImports.get(2), 0,
                "static_member", "qualified", "pkg.Constants.VALUE",
                List.of("pkg", "Constants"), "VALUE", null, 0, false, "unspecified");
        assertImportEntry(javaRoot, javaSource, javaImports.get(3), 0,
                "static_member", "qualified", "pkg.Constants.*",
                List.of("pkg", "Constants"), null, null, 0, true, "unspecified");
        assertEquals("complete", completeness(javaRoot, "import").path("status").asText());

        String pythonSource = "import pkg.mod, other as renamed\n"
                + "from ..base.tools import first as one, second\n"
                + "from . import child\n"
                + "from pkg.public import *\n";
        JsonNode pythonRoot = parse(workspace, new Fixture(new PythonLanguageModule(workspace),
                "imports/sample.py", pythonSource));
        List<JsonNode> pythonImports = facts(pythonRoot, "import");
        assertEquals(4, pythonImports.size(), "facts are import statements, not flattened names");
        assertEquals(2, pythonImports.get(0).path("payload").path("entries").size());
        assertImportEntry(pythonRoot, pythonSource, pythonImports.get(0), 0,
                "module", "qualified", "pkg.mod", List.of("pkg", "mod"),
                null, null, 0, false, "unspecified");
        assertImportEntry(pythonRoot, pythonSource, pythonImports.get(0), 1,
                "module", "qualified", "other as renamed", List.of("other"),
                null, "renamed", 0, false, "unspecified");
        assertImportEntry(pythonRoot, pythonSource, pythonImports.get(1), 0,
                "module_member", "qualified", "first as one", List.of("base", "tools"),
                "first", "one", 2, false, "unspecified");
        assertImportEntry(pythonRoot, pythonSource, pythonImports.get(1), 1,
                "module_member", "qualified", "second", List.of("base", "tools"),
                "second", null, 2, false, "unspecified");
        assertImportEntry(pythonRoot, pythonSource, pythonImports.get(2), 0,
                "module_member", "qualified", "child", List.of(),
                "child", null, 1, false, "unspecified");
        assertImportEntry(pythonRoot, pythonSource, pythonImports.get(3), 0,
                "module_member", "qualified", "*", List.of("pkg", "public"),
                null, null, 0, true, "unspecified");
        assertEquals("complete", completeness(pythonRoot, "import").path("status").asText());
    }

    @Test
    void unknownConditionalCompilationNeverBecomesAFalseConcreteBuild() throws Exception {
        ParserWorkspace workspace = workspace();
        String source = "void run(void) {\n"
                + "#if 0 /* comments are preprocessing whitespace */\n"
                + "  inactive_call();\n#else\n  active_call();\n#endif\n"
                + "#if FEATURE\n  left_call();\n#else\n  right_call();\n#endif\n"
                + "#if (2 * 3) == 6\n  expression_true();\n#else\n  expression_false();\n#endif\n"
                + "}\n";
        JsonNode root = parse(workspace, new Fixture(new CLanguageModule(workspace),
                "conditional/sample.c", source));

        assertFalse(hasActiveCall(root, source, "inactive_call"), "#if 0 branch became active");
        assertTrue(hasActiveCall(root, source, "active_call"), "#if 0 else branch was lost");
        assertFalse(hasActiveCall(root, source, "left_call"), "unknown FEATURE became true");
        assertFalse(hasActiveCall(root, source, "right_call"), "unknown FEATURE became false");
        assertTrue(hasActiveCall(root, source, "expression_true"),
                "general constant-expression evaluation lost the true branch");
        assertFalse(hasActiveCall(root, source, "expression_false"),
                "general constant-expression evaluation activated the false branch");

        List<JsonNode> regions = facts(root, "conditional_region");
        assertEquals(3, regions.size(), "every conditional group needs one exact region fact");
        JsonNode unknown = regions.stream()
                .filter(fact -> fact.path("payload").path("condition").asText().contains("FEATURE"))
                .findFirst().orElseThrow();
        assertEquals("conditional", presence(root, unknown).path("status").asText());
        assertExactSlice(source, unknown);
        assertTrue(slice(source, unknown.path("range")).contains("#if FEATURE"));

        JsonNode callsCompleteness = completeness(root, "call");
        assertEquals("partial", callsCompleteness.path("status").asText());
        assertEquals("insufficient_missing_build_configuration",
                callsCompleteness.path("reason").asText());
        assertEquals(0, callsCompleteness.path("explicitlyUnresolved").asInt(),
                "a region is not an invented unresolved call occurrence");
        assertEquals(List.of(unknown.path("factId").asText()),
                stringValues(callsCompleteness.path("unresolvedScopeFactIds")));
        assertEquals(callsCompleteness.path("population").asInt(),
                callsCompleteness.path("emitted").asInt()
                + callsCompleteness.path("explicitlyUnresolved").asInt());
    }

    @Test
    void nestedConditionalRegionsUseEffectiveParentPresence() throws Exception {
        ParserWorkspace workspace = workspace();
        String source = "void run(void) {\n"
                + "#if 0\n#if HIDDEN_FEATURE\n  unreachable_call();\n#endif\n#endif\n"
                + "#if FEATURE\n#if 1\n  conditional_call();\n#endif\n#endif\n"
                + "}\n";
        JsonNode root = parse(workspace, new Fixture(new CLanguageModule(workspace),
                "conditional/nested.c", source));

        JsonNode hidden = facts(root, "conditional_region").stream()
                .filter(fact -> fact.path("payload").path("condition").asText()
                        .contains("HIDDEN_FEATURE"))
                .findFirst().orElseThrow();
        assertEquals("inactive", presence(root, hidden).path("status").asText(),
                "an unknown child of an inactive parent cannot degrade the active view");

        JsonNode knownChild = facts(root, "conditional_region").stream()
                .filter(fact -> "#if 1".equals(
                        fact.path("payload").path("condition").asText()))
                .findFirst().orElseThrow();
        assertEquals("conditional", presence(root, knownChild).path("status").asText(),
                "a known child cannot become concrete under an unknown parent");

        JsonNode calls = completeness(root, "call");
        assertEquals("partial", calls.path("status").asText());
        assertFalse(stringValues(calls.path("unresolvedScopeFactIds"))
                .contains(hidden.path("factId").asText()));
        assertTrue(stringValues(calls.path("unresolvedScopeFactIds"))
                .contains(knownChild.path("factId").asText()));
    }

    @Test
    void canonicalFactIdsAreReplayStableAndSourceBound() throws Exception {
        ParserWorkspace workspace = workspace();
        LanguageModule module = new JavaLanguageModule(workspace);
        String source = "class Sample { void run() { target(1); target(1); } }\n";
        Fixture fixture = new Fixture(module, "identity/Sample.java", source);
        List<String> first = factIds(parse(workspace, fixture));
        List<String> repeated = factIds(parse(workspace, fixture));
        assertEquals(first, repeated, "same source replay changed canonical IDs");
        assertEquals(first.size(), first.stream().distinct().count(),
                "same-range or repeated occurrences collided");

        List<String> changedSource = factIds(parse(workspace, new Fixture(module,
                "identity/Sample.java", source.replace("target(1)", "target(2)"))));
        assertNotEquals(first, changedSource, "source revision did not change fact IDs");

        List<String> changedPath = factIds(parse(workspace, new Fixture(module,
                "identity/Renamed.java", source)));
        assertNotEquals(first, changedPath, "sourceId did not bind fact IDs");
    }

    @Test
    void parserRecoveryNeverClaimsCompleteCallEvidence() throws Exception {
        ParserWorkspace workspace = workspace();
        String malformed = "class Broken { void run( { target(1); } }\n";
        JsonNode root = parse(workspace, new Fixture(new JavaLanguageModule(workspace),
                "degraded/Broken.java", malformed));

        JsonNode evidence = root.path("evidence");
        assertEquals("partial", evidence.path("parseStatus").asText());
        JsonNode calls = completeness(root, "call");
        assertEquals("partial", calls.path("status").asText(),
                "recovered parser facts cannot be advertised as a complete population");
        assertEquals("insufficient_parser_recovery", calls.path("reason").asText());
        assertEquals(0, calls.path("explicitlyUnresolved").asInt(),
                "unknown occurrence count must not be invented");
        assertEquals(calls.path("population").asInt(), calls.path("emitted").asInt());
    }

    @Test
    void fullFileParsingOutsideTheWorkspaceUsesAnExplicitContentAddressedSourceId()
            throws Exception {
        ParserWorkspace workspace = workspace();
        String source = "class External { void run() { target(1); } }\n";
        Path external = Files.createTempFile("robo-evidence-external-", ".java");
        try {
            Files.writeString(external, source, StandardCharsets.UTF_8);
            RawParseResult result = new JavaLanguageModule(workspace).parseFile(external.toFile(),
                    new ParseProgressTracker(null, external.getFileName().toString()));
            JsonNode evidence = JSON.readTree(result.astJson()).path("evidence");
            assertFalse(evidence.isMissingNode(),
                    "a public full-file parse must not silently omit its evidence envelope");
            assertTrue(evidence.path("sourceId").asText().matches(
                    "unscoped/[0-9a-f]{64}/[^/]+\\.java"));
            assertEquals(source, evidence.path("decodedText").asText());
            assertEquals(1, facts(JSON.readTree(result.astJson()), "call").size());
        } finally {
            Files.deleteIfExists(external);
        }
    }

    private static ParserWorkspace workspace() {
        return new ParserWorkspace(new SourceIntakeClassifier());
    }

    private static JsonNode parse(ParserWorkspace workspace, Fixture fixture) throws Exception {
        Path file = workspace.sourceDir().resolve(fixture.relativePath());
        Files.createDirectories(file.getParent());
        Files.writeString(file, fixture.source(), StandardCharsets.UTF_8);
        RawParseResult result = fixture.module().parseFile(file.toFile(),
                new ParseProgressTracker(null, file.getFileName().toString()));
        return JSON.readTree(result.astJson());
    }

    private static JsonNode callByCallee(JsonNode root, String source, String callee) {
        List<JsonNode> calls = facts(root, "call");
        return calls.stream()
                .filter(fact -> callee.equals(slice(source,
                        fact.path("payload").path("calleeRange"))))
                .findFirst().orElseThrow(() -> new AssertionError("missing call fact: " + callee
                        + "; available=" + calls.stream().map(fact -> slice(source,
                                fact.path("payload").path("calleeRange"))).toList()));
    }

    private static boolean hasActiveCall(JsonNode root, String source, String callee) {
        return facts(root, "call").stream().anyMatch(fact ->
                callee.equals(slice(source, fact.path("payload").path("calleeRange")))
                        && "active".equals(presence(root, fact).path("status").asText()));
    }

    private static List<JsonNode> facts(JsonNode root, String kind) {
        List<JsonNode> result = new ArrayList<>();
        root.path("evidence").path("facts").forEach(fact -> {
            if (kind.equals(fact.path("kind").asText())) result.add(fact);
        });
        return result;
    }

    private static List<JsonNode> children(JsonNode root, String type) {
        List<JsonNode> result = new ArrayList<>();
        root.path("children").forEach(child -> {
            if (type.equals(child.path("type").asText())) result.add(child);
        });
        return result;
    }

    private static JsonNode completeness(JsonNode root, String kind) {
        List<JsonNode> matches = new ArrayList<>();
        root.path("evidence").path("completeness").forEach(item -> {
            if (kind.equals(item.path("kind").asText())) matches.add(item);
        });
        assertEquals(1, matches.size(), "missing or duplicate completeness row for " + kind);
        return matches.get(0);
    }

    private static List<String> argumentExpressions(String source, JsonNode call) {
        List<String> result = new ArrayList<>();
        call.path("payload").path("argumentRanges").forEach(range ->
                result.add(slice(source, range)));
        return result;
    }

    private static List<String> rangeSlices(String source, JsonNode ranges) {
        List<String> result = new ArrayList<>();
        ranges.forEach(range -> result.add(slice(source, range)));
        return result;
    }

    private static void assertImportEntry(
            JsonNode root, String source, JsonNode fact, int ordinal,
            String importKind, String targetKind, String target,
            List<String> pathComponents, String member, String alias,
            int relativeLevel, boolean wildcard, String locality) {
        JsonNode entries = fact.path("payload").path("entries");
        assertTrue(ordinal >= 0 && ordinal < entries.size(), "missing import binding entry");
        JsonNode entry = entries.get(ordinal);
        assertEquals(importKind, entry.path("importKind").asText());
        assertEquals(targetKind, entry.path("targetKind").asText());
        assertEquals(target, slice(source, entry.path("targetRange")));
        assertEquals(pathComponents,
                rangeSlices(source, entry.path("pathComponentRanges")));
        if (member == null) assertTrue(entry.path("memberRange").isNull());
        else assertEquals(member, slice(source, entry.path("memberRange")));
        if (alias == null) assertTrue(entry.path("aliasRange").isNull());
        else assertEquals(alias, slice(source, entry.path("aliasRange")));
        assertEquals(relativeLevel, entry.path("relativeLevel").asInt(-1));
        assertEquals(wildcard, entry.path("wildcard").asBoolean());
        assertEquals(locality, entry.path("locality").asText());
        assertEquals("active", presence(root, fact).path("status").asText());
        assertExactSlice(source, fact);
    }

    private static void assertNoDuplicatedSourceText(JsonNode call) {
        assertFalse(call.path("payload").has("callExpression"));
        assertFalse(call.path("payload").has("calleeExpression"));
        assertFalse(call.path("payload").has("arguments"));
        assertFalse(call.has("sourceSliceSha256"));
        assertFalse(call.has("syntaxOwnerFactId"));
        assertTrue(call.path("grammarRuleRef").isIntegralNumber());
        assertTrue(call.path("presenceRef").isIntegralNumber());
    }

    private static List<String> factIds(JsonNode root) {
        List<String> result = new ArrayList<>();
        root.path("evidence").path("facts").forEach(fact ->
                result.add(fact.path("factId").asText()));
        assertFalse(result.isEmpty(), "evidence facts must not be empty");
        result.forEach(id -> assertEquals(64, id.length(), "factId must be SHA-256"));
        return result;
    }

    private static void assertExactSlice(String source, JsonNode fact) {
        int start = fact.path("range").path("charOffset").asInt(-1);
        int end = start + fact.path("range").path("charLength").asInt(-1);
        assertTrue(start >= 0 && end >= start, "invalid half-open code-point range");
        int[] codePoints = source.codePoints().toArray();
        new String(codePoints, start, end - start);
    }

    private static String slice(String source, JsonNode range) {
        int start = range.path("charOffset").asInt();
        int end = start + range.path("charLength").asInt();
        int[] codePoints = source.codePoints().toArray();
        return new String(codePoints, start, end - start);
    }

    private static JsonNode presence(JsonNode root, JsonNode fact) {
        int reference = fact.path("presenceRef").asInt(-1);
        JsonNode presences = root.path("evidence").path("presences");
        assertTrue(reference >= 0 && reference < presences.size());
        return presences.get(reference);
    }

    private static List<String> stringValues(JsonNode values) {
        List<String> result = new ArrayList<>();
        values.forEach(value -> result.add(value.asText()));
        return result;
    }

    private record Fixture(LanguageModule module, String relativePath, String source) {
    }
}
