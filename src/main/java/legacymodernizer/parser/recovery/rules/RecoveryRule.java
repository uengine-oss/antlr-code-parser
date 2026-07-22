package legacymodernizer.parser.recovery.rules;

import java.util.Set;

import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.recovery.boundaries.SourceUnit;

public interface RecoveryRule {

    String id();

    default String ruleSetId() {
        return "common-safe";
    }

    default Set<String> languages() {
        return Set.of();
    }

    RecoveryRuleProposal propose(String source, SourceUnit unit, RawParseResult failedAttempt);
}
