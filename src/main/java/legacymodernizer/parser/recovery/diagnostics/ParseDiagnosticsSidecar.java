package legacymodernizer.parser.recovery.diagnostics;

import java.util.List;

import legacymodernizer.parser.recovery.quality.DeclarationCoverage;
import legacymodernizer.parser.recovery.diagnostics.ParseDiagnostic;
import legacymodernizer.parser.recovery.quality.QualityStatus;
import legacymodernizer.parser.recovery.evidence.UnitRecoveryEvidence;

public record ParseDiagnosticsSidecar(
        String schemaVersion,
        String sourcePath,
        String language,
        String sourceSha256,
        String grammarRevision,
        QualityStatus status,
        FirstPassEvidence firstPass,
        List<UnitRecoveryEvidence> units,
        Summary summary) {

    public record FirstPassEvidence(
            String entryRule,
            List<ParseDiagnostic> diagnostics,
            int antlrRecoveries,
            DeclarationCoverage coverage,
            long elapsedMillis,
            List<Integer> qualityTuple,
            List<String> qualityReasons) {
    }

    public record Summary(
            int lexerErrors,
            int parserErrors,
            int antlrRecoveries,
            int declarationsDiscovered,
            int declarationsEmitted,
            int agentAttempts,
            long elapsedMillis,
            long processingElapsedMillis) {
    }
}
