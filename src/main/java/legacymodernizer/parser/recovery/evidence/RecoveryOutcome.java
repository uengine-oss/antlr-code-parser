package legacymodernizer.parser.recovery.evidence;

import java.util.List;

import legacymodernizer.parser.recovery.quality.QualityDecision;

public record RecoveryOutcome(
        String astJson,
        QualityDecision decision,
        List<UnitRecoveryEvidence> units,
        int exactReusedUnits,
        int recoveredUnits,
        int unresolvedUnits,
        String originalFileSha256,
        String repairedSource) {

    public RecoveryOutcome(String astJson, QualityDecision decision,
            List<UnitRecoveryEvidence> units, int exactReusedUnits,
            int recoveredUnits, int unresolvedUnits) {
        this(astJson, decision, units, exactReusedUnits, recoveredUnits, unresolvedUnits,
                null, null);
    }

    public boolean hasAcceptedAst() {
        return astJson != null && decision.accepted();
    }

    public boolean hasVerifiedSourceRepair() {
        return hasAcceptedAst() && originalFileSha256 != null && repairedSource != null;
    }
}
