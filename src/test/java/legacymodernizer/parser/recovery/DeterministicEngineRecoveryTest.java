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

import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.intake.SourceIntakeClassifier;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.parsing.languages.oracle.OracleLanguageModule;
import legacymodernizer.parser.recovery.evidence.RecoveryOutcome;
import legacymodernizer.parser.recovery.quality.ParseQualityGate;
import legacymodernizer.parser.recovery.quality.QualityDecision;
import legacymodernizer.parser.recovery.quality.QualityStatus;
import legacymodernizer.parser.recovery.repair.RepairAgent;
import legacymodernizer.parser.recovery.rules.RecoveryRuleRegistry;
import legacymodernizer.parser.recovery.workingcopy.Hashes;
import legacymodernizer.parser.service.ParseProgressTracker;

/**
 * Spec 012 FR-033/044: with the Agent disabled, the deterministic grammar-guided engine alone
 * repairs the dialect-keyword class of failures, and the original file stays byte-identical.
 */
class DeterministicEngineRecoveryTest {

    @BeforeAll
    static void requireIsolatedDataRoot() {
        assertTrue(System.getProperty("parser.data.root", "").replace('\\', '/')
                .contains("/target/test-data"));
    }

    @Test
    void schemaQualifiedNamesDoNotBlockWholeFileRepair() throws Exception {
        // Audit finding: the unit-name preservation gate compared locator names
        // ("SCH"."PROC") against listener names (PROC) and silently discarded valid
        // whole-file repairs for the most common Oracle shapes.
        String source = "CREATE OR REPLACE PROCEDURE \"SCH\".\"ALIAS_PROC\" AS\n"
                + "  v_id NUMBER;\nBEGIN\n"
                + "  SELECT A.ID INTO v_id FROM APP_TABLE AS A;\n"
                + "END;\n/\n";
        ParserWorkspace storage = new ParserWorkspace(new SourceIntakeClassifier());
        Path file = storage.sourceDir().resolve("engine/schema-qualified.prc");
        Files.createDirectories(file.getParent());
        Files.writeString(file, source, StandardCharsets.UTF_8);

        OracleLanguageModule module = new OracleLanguageModule(storage);
        ParseProgressTracker tracker = new ParseProgressTracker(null, "schema-qualified.prc");
        RawParseResult first = module.parseFile(file.toFile(), tracker);
        ParseQualityGate gate = new ParseQualityGate();
        QualityDecision decision = gate.evaluateFirstPass(first);
        assertFalse(decision.accepted());

        RecoveryOutcome outcome = new LayeredRecoveryPipeline(gate,
                new RecoveryRuleRegistry(List.of()), RepairAgent.disabled())
                .recover(module, file, storage.sourceDir(), first, decision, tracker);

        assertEquals(QualityStatus.RECOVERED_VALIDATED, outcome.decision().status(),
                outcome.decision().reasons().toString());
        assertTrue(outcome.decision().reasons().contains("WHOLE_FILE_RECOVERY"),
                "whole-file repair must not be discarded over name framing: "
                        + outcome.decision().reasons());
    }

    @Test
    void reusedOnlyAssembliesNeverUpgradeARejectedFirstPass() throws Exception {
        // Audit finding: a file rejected for coverage-only reasons (no diagnostics) had all
        // units reused untouched, then was relabelled RECOVERED_VALIDATED with a hardcoded
        // quality tuple — identical content graded better than the gate allowed.
        String source = "class A {}\nclass B {}\n";
        String ast = "{\"type\":\"FILE\",\"name\":\"toy\",\"startLine\":1,\"endLine\":2,"
                + "\"children\":[{\"type\":\"CLASS\",\"name\":\"A\",\"startLine\":1,"
                + "\"endLine\":1,\"children\":[]},{\"type\":\"CLASS\",\"name\":\"B\","
                + "\"startLine\":2,\"endLine\":2,\"children\":[]}]}";
        legacymodernizer.parser.parsing.languages.LanguageModule module =
                new legacymodernizer.parser.parsing.languages.LanguageModule() {
                    @Override
                    public RawParseResult parseFile(java.io.File file,
                            ParseProgressTracker tracker) {
                        throw new UnsupportedOperationException("not needed");
                    }

                    @Override
                    public String languageId() { return "toy"; }

                    @Override
                    public java.util.Set<String> parseExtensions() {
                        return java.util.Set.of(".toy");
                    }

                    @Override
                    public boolean supportsUnitParsing() { return true; }

                    @Override
                    public List<legacymodernizer.parser.recovery.boundaries.SourceUnit>
                            locateUnits(String text) {
                        return List.of(
                                new legacymodernizer.parser.recovery.boundaries.SourceUnit(
                                        "u1", legacymodernizer.parser.recovery.boundaries
                                                .UnitKind.CLASS, "A", null, 0, 11, 1, 1, 0,
                                        "EXACT"),
                                new legacymodernizer.parser.recovery.boundaries.SourceUnit(
                                        "u2", legacymodernizer.parser.recovery.boundaries
                                                .UnitKind.CLASS, "B", null, 11, 22, 2, 2, 1,
                                        "EXACT"));
                    }
                };
        ParserWorkspace storage = new ParserWorkspace(new SourceIntakeClassifier());
        Path file = storage.sourceDir().resolve("toy/laundering.toy");
        Files.createDirectories(file.getParent());
        Files.writeString(file, source, StandardCharsets.UTF_8);

        RawParseResult first = new RawParseResult("toy", "toy-1", "file", "0".repeat(64),
                ast, List.of(), 0,
                legacymodernizer.parser.recovery.quality.DeclarationCoverage.unknown(), 1);
        QualityDecision rejected = new QualityDecision(QualityStatus.REVIEW_REQUIRED, false,
                List.of(0, 0, 0, 0, 0, 1, 0), List.of("COVERAGE_INCOMPLETE_OR_UNKNOWN"));

        RecoveryOutcome outcome = new LayeredRecoveryPipeline(new ParseQualityGate(),
                new RecoveryRuleRegistry(List.of()), RepairAgent.disabled())
                .recover(module, file, storage.sourceDir(), first, rejected,
                        new ParseProgressTracker(null, "laundering.toy"));

        assertTrue(outcome.decision().status() != QualityStatus.RECOVERED_VALIDATED
                        && outcome.decision().status() != QualityStatus.RECOVERED_SAFE,
                "unchanged content must not be upgraded: " + outcome.decision());
        assertTrue(outcome.decision().reasons()
                        .contains("CONTENT_UNCHANGED_FROM_REJECTED_FIRST_PASS"),
                outcome.decision().reasons().toString());
        assertTrue(outcome.decision().reasons().contains("COVERAGE_INCOMPLETE_OR_UNKNOWN"),
                "original rejection reasons must be carried: " + outcome.decision().reasons());
    }

    @Test
    void recoveryEmitsUserFriendlyStreamEventsOnTheExistingWireContract() throws Exception {
        String source = "CREATE OR REPLACE PROCEDURE alias_proc AS\n"
                + "  v_id NUMBER;\nBEGIN\n"
                + "  SELECT A.ID INTO v_id FROM APP_TABLE AS A;\n"
                + "END;\n/\n";
        ParserWorkspace storage = new ParserWorkspace(new SourceIntakeClassifier());
        Path file = storage.sourceDir().resolve("engine/streamed.prc");
        Files.createDirectories(file.getParent());
        Files.writeString(file, source, StandardCharsets.UTF_8);

        OracleLanguageModule module = new OracleLanguageModule(storage);
        List<legacymodernizer.parser.api.stream.ParseStreamEvent> events =
                new java.util.ArrayList<>();
        ParseProgressTracker tracker = new ParseProgressTracker(events::add, "streamed.prc");
        RawParseResult first = module.parseFile(file.toFile(), tracker);
        ParseQualityGate gate = new ParseQualityGate();
        QualityDecision decision = gate.evaluateFirstPass(first);
        assertFalse(decision.accepted());

        new LayeredRecoveryPipeline(gate, new RecoveryRuleRegistry(List.of()),
                RepairAgent.disabled())
                .recover(module, file, storage.sourceDir(), first, decision, tracker);

        assertTrue(events.stream().anyMatch(event ->
                        "message".equals(event.type()) && "repair_started".equals(event.event())),
                "repair start must be announced on the legacy type=message contract");
        assertTrue(events.stream().anyMatch(event ->
                        "message".equals(event.type()) && event.event() != null
                                && event.event().endsWith("_adopted")
                                && "RECOVERY".equals(event.phase())),
                "successful repair must be announced: " + events);
    }

    @Test
    void twoIndependentDialectErrorsConvergeAcrossWaves() throws Exception {
        String source = "CREATE OR REPLACE PROCEDURE two_alias AS\n"
                + "  v_id NUMBER;\n  v_nm VARCHAR2(10);\nBEGIN\n"
                + "  SELECT A.ID INTO v_id FROM APP_TABLE AS A;\n"
                + "  SELECT B.NM INTO v_nm FROM APP_NAMES AS B;\n"
                + "END;\n/\n";
        ParserWorkspace storage = new ParserWorkspace(new SourceIntakeClassifier());
        Path file = storage.sourceDir().resolve("engine/two-alias.prc");
        Files.createDirectories(file.getParent());
        Files.writeString(file, source, StandardCharsets.UTF_8);
        String originalSha = Hashes.sha256(Files.readAllBytes(file));

        OracleLanguageModule module = new OracleLanguageModule(storage);
        ParseProgressTracker tracker = new ParseProgressTracker(null, "two-alias.prc");
        RawParseResult first = module.parseFile(file.toFile(), tracker);
        ParseQualityGate gate = new ParseQualityGate();
        QualityDecision decision = gate.evaluateFirstPass(first);
        assertFalse(decision.accepted());

        RecoveryOutcome outcome = new LayeredRecoveryPipeline(gate,
                new RecoveryRuleRegistry(List.of()), RepairAgent.disabled())
                .recover(module, file, storage.sourceDir(), first, decision, tracker);

        assertEquals(QualityStatus.RECOVERED_VALIDATED, outcome.decision().status(),
                outcome.units().toString());
        long engineAttempts = outcome.units().stream()
                .flatMap(unit -> unit.attempts().stream())
                .filter(attempt -> "GRAMMAR_GUIDED".equals(attempt.stage()))
                .count();
        assertTrue(engineAttempts >= 2, "both errors need their own wave: " + engineAttempts);
        assertEquals(originalSha, Hashes.sha256(Files.readAllBytes(file)));
    }

    @Test
    void oracleAliasAsIsRepairedWithoutAnyAgent() throws Exception {
        String source = "CREATE OR REPLACE PROCEDURE alias_proc AS\n"
                + "  v_id NUMBER;\nBEGIN\n"
                + "  SELECT A.ID INTO v_id FROM APP_TABLE AS A;\n"
                + "END;\n/\n";
        ParserWorkspace storage = new ParserWorkspace(new SourceIntakeClassifier());
        Path file = storage.sourceDir().resolve("engine/deterministic.prc");
        Files.createDirectories(file.getParent());
        Files.writeString(file, source, StandardCharsets.UTF_8);
        String originalSha = Hashes.sha256(Files.readAllBytes(file));

        OracleLanguageModule module = new OracleLanguageModule(storage);
        ParseProgressTracker tracker = new ParseProgressTracker(null, "deterministic.prc");
        RawParseResult first = module.parseFile(file.toFile(), tracker);
        ParseQualityGate gate = new ParseQualityGate();
        QualityDecision decision = gate.evaluateFirstPass(first);
        assertFalse(decision.accepted(), "fixture must fail strict parse first");

        RecoveryOutcome outcome = new LayeredRecoveryPipeline(gate,
                new RecoveryRuleRegistry(List.of()), RepairAgent.disabled())
                .recover(module, file, storage.sourceDir(), first, decision, tracker);

        assertEquals(QualityStatus.RECOVERED_VALIDATED, outcome.decision().status(),
                outcome.units().toString());
        assertEquals(1, outcome.recoveredUnits());
        assertTrue(outcome.units().stream().flatMap(unit -> unit.attempts().stream())
                        .anyMatch(attempt -> "GRAMMAR_GUIDED".equals(attempt.stage())),
                "the deterministic engine must own this repair");
        assertTrue(outcome.units().stream().flatMap(unit -> unit.attempts().stream())
                        .noneMatch(attempt -> "REPAIR_AGENT".equals(attempt.stage())),
                "no Agent call may occur in deterministic-only mode");
        assertEquals(originalSha, Hashes.sha256(Files.readAllBytes(file)),
                "original file must stay byte-identical");
    }
}
