package legacymodernizer.parser.recovery.evidence;

import java.util.List;

import legacymodernizer.parser.recovery.diagnostics.ParseDiagnostic;
import legacymodernizer.parser.recovery.quality.DeclarationCoverage;
import legacymodernizer.parser.recovery.workingcopy.SourceMapSummary;
import legacymodernizer.parser.recovery.workingcopy.TextEdit;

public record RecoveryAttemptEvidence(
        String stage,
        int attemptNumber,
        String sourceSha256,
        List<ParseDiagnostic> diagnostics,
        int antlrRecoveries,
        DeclarationCoverage coverage,
        long elapsedMillis,
        List<String> qualityReasons,
        List<Integer> qualityTuple,
        String workingSha256,
        String ruleId,
        String diff,
        List<TextEdit> edits,
        SourceMapSummary sourceMap,
        AgentRequestEvidence agentRequest) {

    public RecoveryAttemptEvidence(
            String stage,
            int attemptNumber,
            String sourceSha256,
            List<ParseDiagnostic> diagnostics,
            int antlrRecoveries,
            DeclarationCoverage coverage,
            long elapsedMillis,
            List<String> qualityReasons,
            List<Integer> qualityTuple,
            String workingSha256,
            String ruleId,
            String diff) {
        this(stage, attemptNumber, sourceSha256, diagnostics, antlrRecoveries, coverage,
                elapsedMillis, qualityReasons, qualityTuple, workingSha256, ruleId, diff,
                List.of(), null, null);
    }

    public RecoveryAttemptEvidence(
            String stage,
            int attemptNumber,
            String sourceSha256,
            List<ParseDiagnostic> diagnostics,
            int antlrRecoveries,
            DeclarationCoverage coverage,
            long elapsedMillis,
            List<String> qualityReasons,
            List<Integer> qualityTuple,
            String workingSha256,
            String ruleId,
            String diff,
            List<TextEdit> edits,
            SourceMapSummary sourceMap) {
        this(stage, attemptNumber, sourceSha256, diagnostics, antlrRecoveries, coverage,
                elapsedMillis, qualityReasons, qualityTuple, workingSha256, ruleId, diff,
                edits, sourceMap, null);
    }

    public RecoveryAttemptEvidence {
        edits = edits == null ? List.of() : List.copyOf(edits);
    }
}
