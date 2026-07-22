package legacymodernizer.parser.recovery;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.antlr.v4.runtime.CommonToken;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import legacymodernizer.parser.recovery.diagnostics.CollectingAntlrErrorListener;
import legacymodernizer.parser.recovery.diagnostics.ParseDiagnosticsWriter;
import legacymodernizer.parser.recovery.diagnostics.DiagnosticPhase;
import legacymodernizer.parser.recovery.quality.QualityDecision;
import legacymodernizer.parser.recovery.quality.QualityStatus;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.recovery.quality.ParseQualityGate;
import legacymodernizer.parser.recovery.evidence.RecoveryOutcome;
import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.intake.SourceIntakeClassifier;
import legacymodernizer.parser.service.ParseProgressTracker;
import legacymodernizer.parser.parsing.languages.c.CLanguageModule;
import legacymodernizer.parser.parsing.languages.java.JavaLanguageModule;
import legacymodernizer.parser.parsing.languages.oracle.OracleLanguageModule;
import legacymodernizer.parser.parsing.languages.postgresql.PostgreSqlLanguageModule;
import legacymodernizer.parser.parsing.languages.python.PythonLanguageModule;
import legacymodernizer.parser.parsing.languages.LanguageModule;

class ParseDiagnosticsQualityTest {

    private static final Map<String, String> EXACT_FILES = Map.of(
            "java", "Sample.java", "python", "sample.py", "c", "sample.c",
            "oracle", "sample.prc", "postgresql", "sample.sql");
    private static final Map<String, String> BROKEN_FILES = Map.of(
            "java", "Broken.java", "python", "broken.py", "c", "broken.c",
            "oracle", "broken.prc", "postgresql", "broken.sql");

    @BeforeAll
    static void requireIsolatedDataRoot() {
        String configured = System.getProperty("parser.data.root", "").replace('\\', '/');
        assertTrue(configured.contains("/target/test-data"),
                "Diagnostics tests must run only under target/test-data: " + configured);
    }

    @Test
    void exactFixturesPassAndMalformedFixturesCannotBeFalseExact() throws Exception {
        ParserWorkspace storage = new ParserWorkspace(new SourceIntakeClassifier());
        Map<String, LanguageModule> modules = modules(storage);
        ParseQualityGate gate = new ParseQualityGate();

        for (Map.Entry<String, LanguageModule> entry : modules.entrySet()) {
            String language = entry.getKey();
            LanguageModule module = entry.getValue();

            Path exact = copy(storage, "exact", language, EXACT_FILES.get(language));
            module.prepareProjectContext();
            QualityDecision exactDecision = gate.evaluateFirstPass(
                    module.parseFile(exact.toFile(), new ParseProgressTracker(null, exact.getFileName().toString())));
            assertTrue(exactDecision.accepted(), language + " exact fixture rejected: " + exactDecision.reasons());
            assertTrue(exactDecision.status() == QualityStatus.EXACT);

            Path broken = copy(storage, "malformed", language, BROKEN_FILES.get(language));
            RawParseResult brokenResult = module.parseFile(
                    broken.toFile(), new ParseProgressTracker(null, broken.getFileName().toString()));
            QualityDecision brokenDecision = gate.evaluateFirstPass(brokenResult);
            assertFalse(brokenDecision.accepted(), language + " malformed fixture was false exact");
            assertFalse(brokenResult.diagnostics().isEmpty(), language + " did not collect ANTLR errors");
        }
    }

    @Test
    void sidecarIsSeparateFromAstAndContainsNoAstPayload() throws Exception {
        ParserWorkspace storage = new ParserWorkspace(new SourceIntakeClassifier());
        LanguageModule module = modules(storage).get("java");
        Path broken = copy(storage, "malformed", "java", "Broken.java");
        RawParseResult parseAttempt = module.parseFile(
                broken.toFile(), new ParseProgressTracker(null, broken.getFileName().toString()));
        QualityDecision decision = new ParseQualityGate().evaluateFirstPass(parseAttempt);

        Path sidecar = new ParseDiagnosticsWriter(storage).write(
                broken, storage.sourceDir(), parseAttempt,
                new RecoveryOutcome(null, decision, java.util.List.of(), 0, 0, 1),
                parseAttempt.elapsedMillis());
        String json = Files.readString(sidecar);
        assertTrue(sidecar.startsWith(storage.diagnosticsDir()));
        assertFalse(sidecar.startsWith(storage.analysisDir()));
        assertTrue(json.contains("ANTLR_PARSER_SYNTAX"));
        assertTrue(json.contains("UNRESOLVED"));
        assertFalse(json.contains("astJson"));
        assertFalse(json.contains(parseAttempt.astJson()));
        JsonNode sidecarJson = new ObjectMapper().readTree(json);
        assertEquals("1.1.0", sidecarJson.path("schemaVersion").asText());
        assertTrue(sidecarJson.path("summary").has("processingElapsedMillis"));
        assertTrue(sidecarJson.path("summary").path("processingElapsedMillis").asLong()
                >= sidecarJson.path("summary").path("elapsedMillis").asLong());
    }

    @Test
    void nestedPlpgsqlParserErrorsAreNotSilentlyDiscarded() throws Exception {
        ParserWorkspace storage = new ParserWorkspace(new SourceIntakeClassifier());
        LanguageModule module = modules(storage).get("postgresql");
        Path broken = copy(storage, "malformed", "postgresql", "broken_body.sql");
        RawParseResult parseAttempt = module.parseFile(
                broken.toFile(), new ParseProgressTracker(null, broken.getFileName().toString()));
        assertFalse(parseAttempt.diagnostics().isEmpty());
        assertTrue(parseAttempt.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code().equals("ANTLR_PARSER_SYNTAX") && diagnostic.line() >= 5));
        assertFalse(new ParseQualityGate().evaluateFirstPass(parseAttempt).accepted());
    }

    @Test
    void javaConstructorsFollowTheExistingListenerContractWithoutFalseRecovery() throws Exception {
        ParserWorkspace storage = new ParserWorkspace(new SourceIntakeClassifier());
        Path source = storage.sourceDir().resolve("diagnostics/exact/java/WithConstructors.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, "public class WithConstructors {\n"
                + "  public WithConstructors() {}\n"
                + "  public WithConstructors(int value) {}\n"
                + "  public int value() { return 1; }\n"
                + "}\n");

        RawParseResult parseAttempt = modules(storage).get("java").parseFile(
                source.toFile(), new ParseProgressTracker(null, source.getFileName().toString()));
        QualityDecision decision = new ParseQualityGate().evaluateFirstPass(parseAttempt);

        assertTrue(decision.accepted(), decision.reasons().toString());
        assertEquals(QualityStatus.EXACT, decision.status());
        assertEquals(2, parseAttempt.coverage().declarationsDiscovered());
        assertTrue(parseAttempt.coverage().missingDeclarations().isEmpty());
    }

    @Test
    void javaNestedEnumAndAnonymousMethodsDoNotCreateFalseCoverageFailures() throws Exception {
        ParserWorkspace storage = new ParserWorkspace(new SourceIntakeClassifier());
        Path source = storage.sourceDir().resolve("diagnostics/exact/java/ListenerCoverage.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, "public class ListenerCoverage {\n"
                + "  enum Code { A; void enumMethod() {} }\n"
                + "  Runnable task = new Runnable() { public void run() {} };\n"
                + "  public int emittedMethod() { return 1; }\n"
                + "}\n");

        RawParseResult parseAttempt = modules(storage).get("java").parseFile(
                source.toFile(), new ParseProgressTracker(null, source.getFileName().toString()));
        QualityDecision decision = new ParseQualityGate().evaluateFirstPass(parseAttempt);

        assertTrue(decision.accepted(), parseAttempt.coverage().toString());
        assertEquals(QualityStatus.EXACT, decision.status());
        assertEquals(3, parseAttempt.coverage().declarationsDiscovered());
        assertEquals(3, parseAttempt.coverage().declarationsEmitted());
    }

    @Test
    void boundsUntrustedAntlrMessageAndTokenPayloads() {
        CollectingAntlrErrorListener listener = new CollectingAntlrErrorListener(
                DiagnosticPhase.PARSER, "SELECT 1");
        listener.syntaxError(null, new CommonToken(1, "x".repeat(2_000)),
                1, 0, "m".repeat(5_000), null);

        assertEquals(1, listener.diagnostics().size());
        assertTrue(listener.diagnostics().get(0).message().length() <= 768);
        assertTrue(listener.diagnostics().get(0).offendingToken().length() <= 256);
        assertTrue(listener.diagnostics().get(0).tokenWindow().length() <= 120);
    }

    private static Map<String, LanguageModule> modules(ParserWorkspace storage) {
        Map<String, LanguageModule> modules = new LinkedHashMap<>();
        modules.put("java", new JavaLanguageModule(storage));
        modules.put("python", new PythonLanguageModule(storage));
        modules.put("c", new CLanguageModule(storage));
        modules.put("oracle", new OracleLanguageModule(storage));
        modules.put("postgresql", new PostgreSqlLanguageModule(storage));
        return modules;
    }

    private static Path copy(ParserWorkspace storage, String group,
                             String language, String fileName) throws IOException {
        Path destination = storage.sourceDir().resolve(Path.of("diagnostics", group, language, fileName));
        Files.createDirectories(destination.getParent());
        String resource = "/recovery/" + group + "/" + language + "/" + fileName;
        try (InputStream input = ParseDiagnosticsQualityTest.class.getResourceAsStream(resource)) {
            if (input == null) throw new IOException("Missing resource: " + resource);
            Files.copy(input, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return destination;
    }
}
