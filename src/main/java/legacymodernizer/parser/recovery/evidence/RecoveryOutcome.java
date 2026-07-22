package legacymodernizer.parser.recovery.evidence;

import java.util.List;

import legacymodernizer.parser.recovery.quality.QualityDecision;

public record RecoveryOutcome(
        String astJson,
        QualityDecision decision,
        List<UnitRecoveryEvidence> units,
        int exactReusedUnits,
        int recoveredUnits,
        int unresolvedUnits) {

    public boolean hasAcceptedAst() {
        return astJson != null && decision.accepted();
    }
}
