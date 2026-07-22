package legacymodernizer.parser.recovery.rules;

import java.util.List;

import org.springframework.stereotype.Component;

import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.recovery.boundaries.SourceUnit;
import legacymodernizer.parser.recovery.workingcopy.TextEdit;

@Component
public final class Utf8BomRule implements RecoveryRule {

    @Override
    public String id() {
        return "common.remove-leading-bom.v1";
    }

    @Override
    public RecoveryRuleProposal propose(String source, SourceUnit unit, RawParseResult failedAttempt) {
        if (source == null || source.isEmpty() || source.charAt(0) != '\uFEFF') {
            return RecoveryRuleProposal.none(id());
        }
        return new RecoveryRuleProposal(id(), true, false,
                List.of(new TextEdit(0, 1, "", id(), "Remove Unicode BOM before grammar input")),
                "A leading BOM is transport metadata, not source semantics.");
    }
}
