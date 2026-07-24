package legacymodernizer.parser.recovery.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.intake.SourceIntakeClassifier;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.parsing.languages.oracle.OracleLanguageModule;
import legacymodernizer.parser.recovery.LayeredRecoveryPipeline;
import legacymodernizer.parser.recovery.evidence.RecoveryOutcome;
import legacymodernizer.parser.recovery.quality.ParseQualityGate;
import legacymodernizer.parser.recovery.quality.QualityDecision;
import legacymodernizer.parser.recovery.quality.QualityStatus;
import legacymodernizer.parser.recovery.repair.RepairAgent;
import legacymodernizer.parser.recovery.rules.RecoveryRuleRegistry;
import legacymodernizer.parser.service.ParseProgressTracker;

class VerifiedSourceRepairIntegrationTest {

    @TempDir
    Path externalRoot;

    @Test
    void deterministicRepairIsReparsedThenAppliedOnlyToSelectedOriginal() throws Exception {
        String malformed = "CREATE OR REPLACE PROCEDURE alias_proc AS\n"
                + "  v_id NUMBER;\nBEGIN\n"
                + "  SELECT A.ID INTO v_id FROM APP_TABLE AS A;\n"
                + "END;\n/\n";
        Path original = externalRoot.resolve("alias_proc.prc");
        Files.writeString(original, malformed, StandardCharsets.UTF_8);
        ParserWorkspace workspace = new ParserWorkspace(new SourceIntakeClassifier());
        workspace.intakeFromPath(externalRoot);
        Path workspaceFile = workspace.sourceDir().resolve("alias_proc.prc");

        OracleLanguageModule module = new OracleLanguageModule(workspace);
        ParseProgressTracker tracker = new ParseProgressTracker(null, "alias_proc.prc");
        RawParseResult first = module.parseFile(workspaceFile.toFile(), tracker);
        ParseQualityGate gate = new ParseQualityGate();
        QualityDecision firstDecision = gate.evaluateFirstPass(first);
        assertFalse(firstDecision.accepted());

        RecoveryOutcome recovery = new LayeredRecoveryPipeline(gate,
                new RecoveryRuleRegistry(List.of()), RepairAgent.disabled())
                .recover(module, workspaceFile, workspace.sourceDir(),
                        first, firstDecision, tracker);
        assertTrue(recovery.hasVerifiedSourceRepair());

        SourceApplicationResult application = new VerifiedSourceRepairApplier(true)
                .apply(workspace.sourceOrigin(workspaceFile), recovery);

        assertEquals(SourceApplicationStatus.APPLIED, application.status());
        String repaired = Files.readString(original, StandardCharsets.UTF_8);
        assertFalse(repaired.contains("APP_TABLE AS A"));
        RawParseResult reparsed = module.parseFile(original.toFile(), tracker);
        assertEquals(QualityStatus.EXACT, gate.evaluateFirstPass(reparsed).status());
    }
}
