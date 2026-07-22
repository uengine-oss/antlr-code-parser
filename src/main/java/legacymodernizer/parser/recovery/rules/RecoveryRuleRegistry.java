package legacymodernizer.parser.recovery.rules;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import legacymodernizer.parser.parsing.languages.LanguageModule;

@Component
public final class RecoveryRuleRegistry {

    private final List<RecoveryRule> rules;

    public RecoveryRuleRegistry(List<RecoveryRule> rules) {
        this.rules = rules.stream().sorted(Comparator.comparing(RecoveryRule::id)).toList();
    }

    public List<RecoveryRule> forModule(LanguageModule module) {
        return rules.stream()
                .filter(rule -> module.recoveryRuleSets().contains(rule.ruleSetId()))
                .filter(rule -> rule.languages().isEmpty()
                        || rule.languages().contains(module.languageId()))
                .toList();
    }
}
