package legacymodernizer.parser.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import legacymodernizer.parser.recovery.quality.QualityDecision;
import legacymodernizer.parser.recovery.quality.QualityStatus;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.recovery.evidence.RecoveryOutcome;
import legacymodernizer.parser.recovery.quality.ParseQualityGate;
import legacymodernizer.parser.parsing.languages.oracle.OracleTableAliasAsRule;
import legacymodernizer.parser.recovery.rules.RecoveryRuleRegistry;
import legacymodernizer.parser.recovery.rules.Utf8BomRule;
import legacymodernizer.parser.recovery.workingcopy.Hashes;
import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.intake.SourceIntakeClassifier;
import legacymodernizer.parser.service.ParseProgressTracker;
import legacymodernizer.parser.parsing.languages.oracle.OracleLanguageModule;

class OracleMinimalUnitRecoveryTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @BeforeAll
    static void requireIsolatedDataRoot() {
        assertTrue(System.getProperty("parser.data.root", "").replace('\\', '/')
                .contains("/target/test-data"));
    }

    @Test
    void keepsValidSiblingUnitsAndExcludesOnlyUnresolvedUnit() throws Exception {
        String source = "CREATE OR REPLACE PROCEDURE good_proc AS\n"
                + "BEGIN NULL; END;\n/\n"
                + "CREATE OR REPLACE PROCEDURE broken_proc(p_id IN NUMBER AS\n"
                + "BEGIN NULL; END;\n/\n"
                + "CREATE OR REPLACE FUNCTION good_fn RETURN NUMBER AS\n"
                + "BEGIN RETURN 1; END;\n/\n";
        Path file = writeSource("partial/three_units.prc", source);
        String before = Hashes.sha256(Files.readAllBytes(file));

        ParserWorkspace storage = new ParserWorkspace(new SourceIntakeClassifier());
        OracleLanguageModule module = new OracleLanguageModule(storage);
        ParseQualityGate gate = new ParseQualityGate();
        RawParseResult first = module.parseFile(file.toFile(), new ParseProgressTracker(null, file.getFileName().toString()));
        QualityDecision firstDecision = gate.evaluateFirstPass(first);
        assertFalse(firstDecision.accepted());

        RecoveryOutcome outcome = coordinator(gate).recover(
                module, file, storage.sourceDir(), first, firstDecision,
                new ParseProgressTracker(null, file.getFileName().toString()));
        assertTrue(outcome.hasAcceptedAst());
        assertEquals(QualityStatus.PARTIAL, outcome.decision().status());
        assertEquals(1, outcome.unresolvedUnits());
        assertEquals(3, outcome.units().size());

        JsonNode root = JSON.readTree(outcome.astJson());
        assertEquals(2, root.path("children").size());
        assertEquals("good_proc", root.path("children").get(0).path("name").asText());
        assertEquals("good_fn", root.path("children").get(1).path("name").asText());
        assertFalse(outcome.astJson().contains("UNRESOLVED"));
        assertEquals(before, Hashes.sha256(Files.readAllBytes(file)), "Original source changed");
    }

    @Test
    void recoversAllUnitsWhenOnlyOutOfUnitNoiseBreaksWholeFile() throws Exception {
        String source = "💥\n"
                + "CREATE OR REPLACE PROCEDURE first_proc AS\nBEGIN NULL; END;\n/\n"
                + "💥\n"
                + "CREATE OR REPLACE FUNCTION second_fn RETURN NUMBER AS\nBEGIN RETURN 2; END;\n/\n";
        Path file = writeSource("recovered/noisy_units.prc", source);
        ParserWorkspace storage = new ParserWorkspace(new SourceIntakeClassifier());
        OracleLanguageModule module = new OracleLanguageModule(storage);
        ParseQualityGate gate = new ParseQualityGate();
        RawParseResult first = module.parseFile(file.toFile(), new ParseProgressTracker(null, file.getFileName().toString()));
        QualityDecision firstDecision = gate.evaluateFirstPass(first);
        assertFalse(firstDecision.accepted());

        RecoveryOutcome outcome = coordinator(gate).recover(
                module, file, storage.sourceDir(), first, firstDecision,
                new ParseProgressTracker(null, file.getFileName().toString()));
        assertNotNull(outcome.astJson());
        assertEquals(QualityStatus.RECOVERED_VALIDATED, outcome.decision().status());
        assertEquals(0, outcome.unresolvedUnits());
        assertEquals(2, JSON.readTree(outcome.astJson()).path("children").size());
    }

    @Test
    void validatesSafeTableAliasRuleOnWorkingCopyOnly() throws Exception {
        String source = "CREATE OR REPLACE PROCEDURE alias_proc AS\n"
                + "  v_id NUMBER;\n"
                + "BEGIN\n"
                + "  SELECT A.ID INTO v_id FROM APP_TABLE AS A;\n"
                + "END;\n/\n";
        Path file = writeSource("rules/table_alias.prc", source);
        String before = Hashes.sha256(Files.readAllBytes(file));
        ParserWorkspace storage = new ParserWorkspace(new SourceIntakeClassifier());
        // Empty RepairProfile keeps the grammar-guided engine (which would repair this at
        // file level first) out of the way: this test exercises the safe-rule path.
        OracleLanguageModule module = new OracleLanguageModule(storage) {
            @Override
            public legacymodernizer.parser.recovery.candidates.RepairProfile repairProfile() {
                return legacymodernizer.parser.recovery.candidates.RepairProfile.empty();
            }
        };
        ParseQualityGate gate = new ParseQualityGate();
        RawParseResult first = module.parseFile(file.toFile(),
                new ParseProgressTracker(null, file.getFileName().toString()));
        QualityDecision firstDecision = gate.evaluateFirstPass(first);
        assertFalse(firstDecision.accepted());

        RecoveryOutcome outcome = coordinator(gate).recover(
                module, file, storage.sourceDir(), first, firstDecision,
                new ParseProgressTracker(null, file.getFileName().toString()));

        assertEquals(QualityStatus.RECOVERED_SAFE, outcome.decision().status());
        assertEquals(1, outcome.recoveredUnits());
        assertEquals("alias_proc", JSON.readTree(outcome.astJson())
                .path("children").get(0).path("name").asText());
        assertTrue(outcome.units().get(0).attempts().stream().anyMatch(attempt ->
                "oracle.remove-table-alias-as.v1".equals(attempt.ruleId())
                        && attempt.diff().contains(" AS ")));
        assertEquals(before, Hashes.sha256(Files.readAllBytes(file)), "Original source changed");
    }

    private static Path writeSource(String relative, String source) throws Exception {
        ParserWorkspace storage = new ParserWorkspace(new SourceIntakeClassifier());
        Path file = storage.sourceDir().resolve(relative);
        Files.createDirectories(file.getParent());
        Files.writeString(file, source, StandardCharsets.UTF_8);
        return file;
    }

    private static LayeredRecoveryPipeline coordinator(ParseQualityGate gate) {
        return new LayeredRecoveryPipeline(gate, new RecoveryRuleRegistry(
                java.util.List.of(new Utf8BomRule(), new OracleTableAliasAsRule())));
    }
}
