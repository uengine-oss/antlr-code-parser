package legacymodernizer.parser.recovery.rules;

import java.util.List;

import legacymodernizer.parser.recovery.workingcopy.TextEdit;

public record RecoveryRuleProposal(
        String ruleId,
        boolean safe,
        boolean ambiguous,
        List<TextEdit> edits,
        String rationale) {

    public RecoveryRuleProposal {
        edits = edits == null ? List.of() : List.copyOf(edits);
    }

    public static RecoveryRuleProposal none(String ruleId) {
        return new RecoveryRuleProposal(ruleId, false, false, List.of(), "not applicable");
    }
}
