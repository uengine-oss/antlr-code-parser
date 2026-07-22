package legacymodernizer.parser.recovery.evidence;

import java.util.List;

import legacymodernizer.parser.recovery.boundaries.SourceUnit;
import legacymodernizer.parser.recovery.quality.QualityStatus;

public record UnitRecoveryEvidence(
        SourceUnit unit,
        QualityStatus status,
        boolean accepted,
        int emittedTopLevelNodes,
        List<RecoveryAttemptEvidence> attempts) {
}
