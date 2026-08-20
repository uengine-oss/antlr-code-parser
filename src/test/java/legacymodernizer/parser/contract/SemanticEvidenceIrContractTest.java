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
            assertEquals("1.0.0", evidence.path("version").asText());
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
        }
    }

    @Test
    void callFactsUseFullGrammarExpressionsAndUnicodeCodePointRanges() throws Exception {
        ParserWorkspace workspace = workspace();
        String javaSource = "class Sample { void run() { String marker = \"😀\";\n"
                + "  service.repo().find(\n    nested(1), 2);\n} }\n";
        JsonNode javaRoot = parse(workspace, new Fixture(new JavaLanguageModule(workspace),
                "ranges/Sample.java", javaSource));
        JsonNode javaCall = callByCallee(javaRoot, javaSource, "service.repo().find");
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
        assertEquals("new Runnable()", slice(anonymousJava, constructor.path("range")),
                "anonymous class body is not part of the constructor call range");
        assertExactSlice(anonymousJava, constructor);

        String pythonSource = "def run():\n    marker = '😀'\n    service.repo().find(\n"
                + "        nested(1), 2)\n";
        JsonNode pythonRoot = parse(workspace, new Fixture(new PythonLanguageModule(workspace),
                "ranges/sample.py", pythonSource));
        JsonNode pythonCall = callByCallee(pythonRoot, pythonSource, "service.repo().find");
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
        assertEquals("(lambda x: x)(1)",
                slice(anonymousPython, anonymousCall.path("range")));
        assertExactSlice(anonymousPython, anonymousCall);
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
