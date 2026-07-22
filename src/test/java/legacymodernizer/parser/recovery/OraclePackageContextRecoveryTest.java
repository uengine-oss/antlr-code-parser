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

import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.intake.SourceIntakeClassifier;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.parsing.languages.oracle.OracleLanguageModule;
import legacymodernizer.parser.recovery.diagnostics.DiagnosticPhase;
import legacymodernizer.parser.recovery.diagnostics.ParseDiagnostic;
import legacymodernizer.parser.recovery.evidence.RecoveryOutcome;
import legacymodernizer.parser.recovery.quality.ParseQualityGate;
import legacymodernizer.parser.recovery.quality.QualityDecision;
import legacymodernizer.parser.recovery.quality.QualityStatus;
import legacymodernizer.parser.recovery.rules.RecoveryRuleRegistry;
import legacymodernizer.parser.service.ParseProgressTracker;

class OraclePackageContextRecoveryTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @BeforeAll
    static void requireIsolatedDataRoot() {
        assertTrue(System.getProperty("parser.data.root", "").replace('\\', '/')
                .contains("/target/test-data"));
    }

    @Test
    void reparsesOnlyFailedPackageMemberWithSyntheticPackageContext() throws Exception {
        String source = "CREATE OR REPLACE PACKAGE BODY sample_pkg AS\n"
                + "  PROCEDURE first_proc AS BEGIN NULL; END first_proc;\n"
                + "  FUNCTION second_fn RETURN NUMBER AS BEGIN RETURN 2; END second_fn;\n"
                + "END sample_pkg;\n/\n";
        ParserWorkspace workspace = new ParserWorkspace(new SourceIntakeClassifier());
        Path file = workspace.sourceDir().resolve("oracle/package/sample_pkg.pkb");
        Files.createDirectories(file.getParent());
        Files.writeString(file, source, StandardCharsets.UTF_8);

        OracleLanguageModule module = new OracleLanguageModule(workspace);
        ParseProgressTracker tracker = new ParseProgressTracker(null, file.getFileName().toString());
        RawParseResult exact = module.parseFile(file.toFile(), tracker);
        JsonNode exactRoot = JSON.readTree(exact.astJson());
        assertEquals(2, exactRoot.path("children").size());

        ParseDiagnostic memberFailure = new ParseDiagnostic(
                DiagnosticPhase.PARSER, "ERROR", "PACKAGE_MEMBER_CONTEXT_REQUIRED",
                "synthetic package-member isolation trigger", 2, 2,
                "PROCEDURE", null, List.of("package_obj_body"), "PROCEDURE first_proc");
        RawParseResult failed = new RawParseResult(
                exact.language(), exact.grammarRevision(), exact.entryRule(), exact.sourceSha256(),
                exact.astJson(), List.of(memberFailure), 1, exact.coverage(), exact.elapsedMillis());
        QualityDecision failedDecision = new QualityDecision(
                QualityStatus.UNRESOLVED, false, List.of(0, 1, 1, 0, 0, 0, 0),
                List.of("PACKAGE_MEMBER_CONTEXT_REQUIRED"));

        RecoveryOutcome outcome = new LayeredRecoveryPipeline(
                new ParseQualityGate(), new RecoveryRuleRegistry(List.of())).recover(
                module, file, workspace.sourceDir(), failed, failedDecision, tracker);

        assertEquals(QualityStatus.RECOVERED_VALIDATED, outcome.decision().status(),
                outcome.units().toString());
        assertEquals(2, outcome.units().size());
        assertEquals(1, outcome.recoveredUnits());
        assertEquals(1, outcome.exactReusedUnits());
        assertTrue(outcome.units().stream().allMatch(evidence ->
                evidence.unit().parentUnitId() != null));
        assertTrue(outcome.units().get(0).attempts().stream().anyMatch(attempt ->
                "CONTEXT_RECONSTRUCTION".equals(attempt.stage())
                        && "oracle.package-member-wrapper.v1".equals(attempt.ruleId())));

        JsonNode recoveredRoot = JSON.readTree(outcome.astJson());
        assertEquals(List.of("first_proc", "second_fn"),
                java.util.stream.StreamSupport.stream(recoveredRoot.path("children").spliterator(), false)
                        .map(node -> node.path("name").asText()).toList());
        assertEquals(List.of(2, 3),
                java.util.stream.StreamSupport.stream(recoveredRoot.path("children").spliterator(), false)
                        .map(node -> node.path("startLine").asInt()).toList());
        assertFalse(outcome.astJson().contains("\"type\":\"PACKAGE\""));
    }

}
