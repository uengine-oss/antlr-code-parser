package legacymodernizer.parser.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import legacymodernizer.parser.recovery.repair.StructuredRepairAgent;
import legacymodernizer.parser.recovery.repair.AgentTextEdit;
import legacymodernizer.parser.recovery.repair.FailureEnvelope;
import legacymodernizer.parser.recovery.repair.PatchProposal;
import legacymodernizer.parser.recovery.repair.PatchProposalValidator;
import legacymodernizer.parser.recovery.repair.RepairAgent;
import legacymodernizer.parser.recovery.repair.RepairAgentException;
import legacymodernizer.parser.recovery.reports.RepairAuditWriter;
import legacymodernizer.parser.recovery.quality.QualityDecision;
import legacymodernizer.parser.recovery.quality.QualityStatus;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.recovery.evidence.RecoveryOutcome;
import legacymodernizer.parser.recovery.quality.ParseQualityGate;
import legacymodernizer.parser.recovery.rules.RecoveryRuleRegistry;
import legacymodernizer.parser.recovery.workingcopy.Hashes;
import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.intake.SourceIntakeClassifier;
import legacymodernizer.parser.service.ParseProgressTracker;
import legacymodernizer.parser.parsing.languages.oracle.OracleLanguageModule;

class RepairAgentRecoveryTest {

    @BeforeAll
    static void requireIsolatedDataRoot() {
        assertTrue(System.getProperty("parser.data.root", "").replace('\\', '/')
                .contains("/target/test-data"));
    }

    @Test
    void adoptsOnlyAReparsedQualityImprovingBoundedPatch() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        RepairAgent agent = fake(envelope -> {
            calls.incrementAndGet();
            int garbage = envelope.sourceExcerpt().indexOf("@@@@@@ ");
            assertTrue(envelope.diagnosticWindowTokens().stream().anyMatch(token ->
                    "@".equals(token.text())));
            return proposal(envelope, List.of(new AgentTextEdit(garbage, garbage + 7,
                    "@@@@@@ ", "       ", "Remove stray punctuation noise")), List.of());
        });

        Result result = recover(agent, "valid");

        assertEquals(1, calls.get());
        assertEquals(QualityStatus.RECOVERED_VALIDATED, result.outcome().decision().status());
        assertEquals(1, result.outcome().recoveredUnits());
        assertTrue(result.outcome().hasVerifiedSourceRepair());
        assertFalse(result.outcome().repairedSource().contains("@@@@@@"));
        assertEquals(result.originalSha256(), Hashes.sha256(Files.readAllBytes(result.file())));
        assertTrue(result.outcome().units().get(0).attempts().stream().anyMatch(attempt ->
                "REPAIR_AGENT".equals(attempt.stage()) && attempt.diff() != null));
    }

    @Test
    void rejectsAmbiguousAndOutOfRangeProposalsWithoutTouchingOriginal() throws Exception {
        Result ambiguous = recover(fake(envelope -> proposal(envelope, List.of(),
                List.of("Could be a table alias or malformed expression"))), "ambiguous");
        assertEquals(QualityStatus.REVIEW_REQUIRED,
                ambiguous.outcome().units().get(0).status());
        assertEquals(ambiguous.originalSha256(), Hashes.sha256(Files.readAllBytes(ambiguous.file())));

        Result escaped = recover(fake(envelope -> proposal(envelope,
                List.of(new AgentTextEdit(0, envelope.sourceExcerpt().length() + 1,
                        "", "", "rewrite")),
                List.of())), "escaped");
        assertEquals(QualityStatus.REVIEW_REQUIRED, escaped.outcome().units().get(0).status());
        assertFalse(escaped.outcome().units().get(0).accepted());
        assertEquals(escaped.originalSha256(), Hashes.sha256(Files.readAllBytes(escaped.file())));
    }

    @Test
    void limitsNonImprovingAgentCallsToThreeAndRecordsProviderFailure() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        Result exhausted = recover(fake(envelope -> {
            int call = calls.incrementAndGet();
            if (call > 1) {
                assertTrue(envelope.priorAttempts().stream()
                        .anyMatch(prior -> !prior.edits().isEmpty()
                                && !prior.diagnostics().isEmpty()));
            }
            int whitespace = envelope.sourceExcerpt().indexOf('\n');
            return proposal(envelope,
                    List.of(new AgentTextEdit(whitespace, whitespace,
                            "", "", "no-op proposal")),
                    List.of());
        }), "exhausted");
        assertEquals(3, calls.get());
        assertEquals(QualityStatus.REVIEW_REQUIRED, exhausted.outcome().units().get(0).status());

        Result unavailable = recover(new RepairAgent() {
            @Override public boolean enabled() { return true; }
            @Override public PatchProposal propose(FailureEnvelope envelope) {
                throw new RepairAgentException("REPAIR_AGENT_TIMEOUT");
            }
        }, "timeout");
        assertTrue(unavailable.outcome().units().get(0).attempts().stream().anyMatch(attempt ->
                "REPAIR_AGENT_TIMEOUT".equals(attempt.ruleId())));
    }

    @Test
    void retriesRejectedProposalsWithExactValidationFeedback() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        Result recovered = recover(fake(envelope -> {
            int call = calls.incrementAndGet();
            if (call == 1) {
                return new PatchProposal("1.0.0", "0".repeat(64),
                        List.of(new AgentTextEdit(0, 0, "", " ",
                                "first invalid proposal")),
                        "wrong envelope binding", 0.9, List.of());
            }
            if (call == 2) {
                assertTrue(envelope.priorAttempts().stream()
                        .flatMap(prior -> prior.validationReasons().stream())
                        .anyMatch("AGENT_ENVELOPE_HASH_MISMATCH"::equals));
                return proposal(envelope, List.of(), List.of("More than one repair is plausible"));
            }
            assertTrue(envelope.priorAttempts().stream()
                    .flatMap(prior -> prior.validationReasons().stream())
                    .anyMatch("AGENT_AMBIGUOUS_PROPOSAL"::equals));
            int garbage = envelope.sourceExcerpt().indexOf("@@@@@@ ");
            return proposal(envelope, List.of(new AgentTextEdit(garbage, garbage + 7,
                    "@@@@@@ ", "       ", "Remove stray punctuation noise")), List.of());
        }), "retry-feedback");

        assertEquals(3, calls.get());
        assertEquals(QualityStatus.RECOVERED_VALIDATED,
                recovered.outcome().decision().status());
        assertEquals(recovered.originalSha256(),
                Hashes.sha256(Files.readAllBytes(recovered.file())));
    }

    @Test
    void proposalValidatorRejectsEnvelopeHashMismatch() throws Exception {
        Result capture = recover(fake(envelope -> new PatchProposal("1.0.0", "0".repeat(64),
                List.of(new AgentTextEdit(0, 0, "", ";", "insert")), "bad binding", 1.0,
                List.of())), "hash");
        assertEquals(QualityStatus.REVIEW_REQUIRED, capture.outcome().units().get(0).status());
        assertThrows(IllegalArgumentException.class, () -> new PatchProposalValidator().validate(
                null, null));
    }

    @Test
    void liveParserOwnedAgentProposalIsStillReparsedByParser() throws Exception {
        String apiBase = System.getProperty("parser.live.agent.api.base");
        String model = System.getProperty("parser.live.agent.model");
        String apiKey = System.getProperty("parser.live.agent.api.key", "");
        Assumptions.assumeTrue(apiBase != null && !apiBase.isBlank()
                && model != null && !model.isBlank());
        String previousEnabled = System.getProperty("parser.repair.agent.enabled");
        String previousApiBase = System.getProperty("parser.repair.agent.api.base");
        String previousModel = System.getProperty("parser.repair.agent.model");
        String previousApiKey = System.getProperty("parser.repair.agent.api.key");
        String previousReasoningEffort = System.getProperty(
                "parser.repair.agent.reasoning.effort");
        String previousThinking = System.getProperty("parser.repair.agent.thinking.enabled");
        String previousTopK = System.getProperty("parser.repair.agent.top.k");
        try {
            System.setProperty("parser.repair.agent.enabled", "true");
            System.setProperty("parser.repair.agent.api.base", apiBase);
            System.setProperty("parser.repair.agent.model", model);
            System.setProperty("parser.repair.agent.api.key", apiKey);
            copyOptionalProperty("parser.live.agent.reasoning.effort",
                    "parser.repair.agent.reasoning.effort");
            copyOptionalProperty("parser.live.agent.thinking.enabled",
                    "parser.repair.agent.thinking.enabled");
            copyOptionalProperty("parser.live.agent.top.k",
                    "parser.repair.agent.top.k");
            Result result = recover(new StructuredRepairAgent(), "live");
            ParserWorkspace storage = new ParserWorkspace(new SourceIntakeClassifier());
            Path audit = new RepairAuditWriter(storage).write(
                    result.file(), storage.sourceDir(), result.firstPass(), result.outcome());
            assertTrue(Files.isRegularFile(audit));
            assertEquals(QualityStatus.RECOVERED_VALIDATED,
                    result.outcome().decision().status(), result.outcome().units().toString());
            assertTrue(result.outcome().hasVerifiedSourceRepair());
            assertFalse(result.outcome().repairedSource().contains("@@@@@@"));
            assertTrue(result.outcome().units().get(0).attempts().stream().anyMatch(attempt ->
                    "REPAIR_AGENT".equals(attempt.stage())
                            && "REPAIR_AGENT".equals(attempt.ruleId())
                            && attempt.diff() != null
                            && !attempt.edits().isEmpty()
                            && attempt.agentRequest() != null));
            assertEquals(result.originalSha256(), Hashes.sha256(Files.readAllBytes(result.file())));
        } finally {
            restoreProperty("parser.repair.agent.enabled", previousEnabled);
            restoreProperty("parser.repair.agent.api.base", previousApiBase);
            restoreProperty("parser.repair.agent.model", previousModel);
            restoreProperty("parser.repair.agent.api.key", previousApiKey);
            restoreProperty("parser.repair.agent.reasoning.effort", previousReasoningEffort);
            restoreProperty("parser.repair.agent.thinking.enabled", previousThinking);
            restoreProperty("parser.repair.agent.top.k", previousTopK);
        }
    }

    private static void copyOptionalProperty(String source, String target) {
        String value = System.getProperty(source);
        if (value == null || value.isBlank()) {
            System.clearProperty(target);
        } else {
            System.setProperty(target, value);
        }
    }

    private static Result recover(RepairAgent agent, String name) throws Exception {
        // Garbage punctuation the deterministic engine cannot claim (lexer-level noise, no
        // profile keyword) and whose removal is neutral under the FR-040 token gate.
        String source = "CREATE OR REPLACE PROCEDURE alias_proc AS\n"
                + "  v_id NUMBER;\nBEGIN\n"
                + "  @@@@@@ SELECT A.ID INTO v_id FROM APP_TABLE A;\n"
                + "END;\n/\n";
        ParserWorkspace storage = new ParserWorkspace(new SourceIntakeClassifier());
        Path file = storage.sourceDir().resolve("agent/" + name + ".prc");
        Files.createDirectories(file.getParent());
        Files.writeString(file, source, StandardCharsets.UTF_8);
        String originalSha = Hashes.sha256(Files.readAllBytes(file));
        // Empty RepairProfile keeps the deterministic engine out of the way: these tests
        // exercise the Agent path, which only runs when no deterministic candidate survives.
        OracleLanguageModule module = new OracleLanguageModule(storage) {
            @Override
            public legacymodernizer.parser.recovery.candidates.RepairProfile repairProfile() {
                return legacymodernizer.parser.recovery.candidates.RepairProfile.empty();
            }
        };
        ParseProgressTracker tracker = new ParseProgressTracker(null, file.getFileName().toString());
        RawParseResult first = module.parseFile(file.toFile(), tracker);
        ParseQualityGate gate = new ParseQualityGate();
        QualityDecision decision = gate.evaluateFirstPass(first);
        assertFalse(decision.accepted());
        RecoveryOutcome outcome = new LayeredRecoveryPipeline(gate,
                new RecoveryRuleRegistry(List.of()), agent)
                .recover(module, file, storage.sourceDir(), first, decision, tracker);
        return new Result(file, originalSha, first, outcome);
    }

    private static RepairAgent fake(Function<FailureEnvelope, PatchProposal> behavior) {
        return new RepairAgent() {
            @Override public boolean enabled() { return true; }
            @Override public PatchProposal propose(FailureEnvelope envelope) {
                // These tests exercise the per-unit retry contract; the whole-file repair
                // pass (which runs first) is declined so unit ladders behave as before.
                if ("FILE".equals(envelope.unitKind())) {
                    return new PatchProposal("1.0.0", envelope.failureEnvelopeHash(), List.of(),
                            "unit-path test declines file-level repair", 0.0,
                            List.of("file-level repair not exercised by this test"));
                }
                return behavior.apply(envelope);
            }
        };
    }

    private static PatchProposal proposal(FailureEnvelope envelope, List<AgentTextEdit> edits,
                                          List<String> ambiguities) {
        return new PatchProposal("1.0.0", envelope.failureEnvelopeHash(), edits,
                "bounded syntax repair", 0.95, ambiguities);
    }

    private static void restoreProperty(String key, String value) {
        if (value == null) System.clearProperty(key);
        else System.setProperty(key, value);
    }

    private record Result(Path file, String originalSha256, RawParseResult firstPass,
                          RecoveryOutcome outcome) { }
}
