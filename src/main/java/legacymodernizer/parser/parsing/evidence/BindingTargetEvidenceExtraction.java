package legacymodernizer.parser.parsing.evidence;

import java.util.List;
import java.util.Set;

/** Complete grammar-owned binding-target population for one Python source parse. */
public record BindingTargetEvidenceExtraction(
        List<BindingTargetCandidate> targets,
        int explicitlyUnresolved,
        List<String> reasons) {

    public BindingTargetEvidenceExtraction {
        targets = List.copyOf(targets == null ? List.of() : targets);
        reasons = List.copyOf(reasons == null ? List.of() : reasons);
        if (explicitlyUnresolved < 0 || (explicitlyUnresolved > 0 && reasons.isEmpty())) {
            throw new IllegalArgumentException("binding target evidence accounting is incomplete");
        }
    }

    /** One grammar target; Analyzer decides whether it binds a local name or mutates state. */
    public record BindingTargetCandidate(
            String grammarRule,
            SourceRangeCandidate range,
            String bindingContext,
            List<ScopeEvidenceCandidate> scopePath,
            SyntaxComponentCandidate syntax) {

        private static final Set<String> CONTEXTS = Set.of(
                "assignment", "annotated_assignment", "augmented_assignment",
                "for_target", "with_target", "except_target", "comprehension_target",
                "function_definition", "class_definition", "delete_target");

        public BindingTargetCandidate {
            scopePath = List.copyOf(scopePath == null ? List.of() : scopePath);
            if (grammarRule == null || grammarRule.isBlank() || range == null
                    || !CONTEXTS.contains(bindingContext) || scopePath.isEmpty()
                    || !"translation_unit".equals(scopePath.get(0).kind())
                    || syntax == null || !range.equals(syntax.range())) {
                throw new IllegalArgumentException("invalid binding target evidence candidate");
            }
            ScopeEvidenceCandidate owner = scopePath.get(scopePath.size() - 1);
            if (range.startOffset() < owner.range().startOffset()
                    || range.endOffset() > owner.range().endOffset()) {
                throw new IllegalArgumentException("binding target is outside lexical scope");
            }
        }
    }
}
