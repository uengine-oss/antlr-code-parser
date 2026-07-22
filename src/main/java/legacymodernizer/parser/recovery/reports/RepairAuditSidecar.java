package legacymodernizer.parser.recovery.reports;

import java.util.List;

import legacymodernizer.parser.recovery.quality.QualityStatus;
import legacymodernizer.parser.recovery.evidence.UnitRecoveryEvidence;

public record RepairAuditSidecar(
        String schemaVersion,
        String sourcePath,
        String sourceSha256,
        String language,
        String grammarRevision,
        QualityStatus finalStatus,
        int exactReusedUnits,
        int recoveredUnits,
        int unresolvedUnits,
        int agentAttempts,
        List<UnitRecoveryEvidence> units) {
}
