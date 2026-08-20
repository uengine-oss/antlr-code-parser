package legacymodernizer.parser.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.intake.SourceIntakeClassifier;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.parsing.languages.c.CLanguageModule;
import legacymodernizer.parser.recovery.evidence.RecoveryOutcome;
import legacymodernizer.parser.recovery.quality.ParseQualityGate;
import legacymodernizer.parser.recovery.quality.QualityDecision;
import legacymodernizer.parser.recovery.quality.QualityStatus;
import legacymodernizer.parser.recovery.rules.RecoveryRuleRegistry;
import legacymodernizer.parser.recovery.workingcopy.Hashes;
import legacymodernizer.parser.service.ParseProgressTracker;

class ShopmallRecoverySemanticTest {

    @BeforeAll
    static void requireIsolatedDataRoot() {
        assertTrue(System.getProperty("parser.data.root", "").replace('\\', '/')
                .contains("/target/test-data"));
    }

    @Test
    void leavesUnknownConditionalPromotionPartialInsteadOfGuessingABranch() throws Exception {
        String sourceProperty = System.getProperty("parser.shopmall.source");
        Assumptions.assumeTrue(sourceProperty != null && !sourceProperty.isBlank());
        Path original = Path.of(sourceProperty).toAbsolutePath().normalize()
                .resolve("payment/promotion.c");
        Assumptions.assumeTrue(Files.isRegularFile(original));

        String source = Files.readString(original, StandardCharsets.UTF_8);
        String marker = "{\n    money_t disc = 0;";
        assertTrue(source.contains(marker));
        String baselineProbe = "{\n\n\n\n    int parser_probe = 0;\n\n    money_t disc = 0;";
        String malformedProbe = "{\n#ifdef PLATFORM_WITH_EXTERNAL_TYPE\n"
                + "    UNKNOWN_EXTERNAL_TYPE parser_probe;\n#else\n"
                + "    int parser_probe = 0;\n#endif\n    money_t disc = 0;";
        String baselineSource = source.replace(marker, baselineProbe);
        String malformedSource = source.replace(marker, malformedProbe);

        ParserWorkspace workspace = new ParserWorkspace(new SourceIntakeClassifier());
        Path baselineFile = workspace.sourceDir().resolve("shopmall-recovery/baseline/promotion.c");
        Path malformedFile = workspace.sourceDir().resolve("shopmall-recovery/malformed/promotion.c");
        Files.createDirectories(baselineFile.getParent());
        Files.createDirectories(malformedFile.getParent());
        Files.writeString(baselineFile, baselineSource, StandardCharsets.UTF_8);
        Files.writeString(malformedFile, malformedSource, StandardCharsets.UTF_8);
        String originalHash = Hashes.sha256(Files.readAllBytes(original));
        String malformedHash = Hashes.sha256(Files.readAllBytes(malformedFile));

        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        RawParseResult baseline = module.parseFile(baselineFile.toFile(),
                new ParseProgressTracker(null, baselineFile.getFileName().toString()));
        ParseQualityGate gate = new ParseQualityGate();
        assertEquals(QualityStatus.EXACT, gate.evaluateFirstPass(baseline).status());

        RawParseResult firstPass = module.parseFile(malformedFile.toFile(),
                new ParseProgressTracker(null, malformedFile.getFileName().toString()));
        QualityDecision firstDecision = gate.evaluateFirstPass(firstPass);
        assertFalse(firstDecision.accepted(), "The deliberately malformed first branch must fail");
        RecoveryOutcome outcome = new LayeredRecoveryPipeline(gate,
                new RecoveryRuleRegistry(List.of())).recover(module, malformedFile,
                workspace.sourceDir(), firstPass, firstDecision,
                new ParseProgressTracker(null, malformedFile.getFileName().toString()));

        assertEquals(QualityStatus.PARTIAL, outcome.decision().status(),
                outcome.units().toString());
        assertEquals(1, outcome.unresolvedUnits());
        assertFalse(outcome.units().stream().flatMap(unit -> unit.attempts().stream())
                .anyMatch(attempt -> "c.alternate-preprocessor-branches.v1"
                        .equals(attempt.ruleId())));
        assertEquals(malformedHash, Hashes.sha256(Files.readAllBytes(malformedFile)));
        assertEquals(originalHash, Hashes.sha256(Files.readAllBytes(original)));
    }

}
