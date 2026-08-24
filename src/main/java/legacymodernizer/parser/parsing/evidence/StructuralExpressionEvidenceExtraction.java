package legacymodernizer.parser.parsing.evidence;

import java.util.List;
import java.util.Set;

/** Grammar-owned roots for expression-bearing statement fields. */
public record StructuralExpressionEvidenceExtraction(
        List<ExpressionCandidate> expressions,
        int explicitlyUnresolved,
        List<String> reasons) {

    public StructuralExpressionEvidenceExtraction {
        expressions = List.copyOf(expressions == null ? List.of() : expressions);
        reasons = List.copyOf(reasons == null ? List.of() : reasons);
        if (explicitlyUnresolved < 0
                || (explicitlyUnresolved > 0 && reasons.isEmpty())) {
            throw new IllegalArgumentException("invalid structural expression extraction");
        }
    }

    public record ExpressionCandidate(
            String role,
            SourceRangeCandidate range,
            SourceRangeCandidate ownerRange,
            List<ScopeEvidenceCandidate> scopePath,
            SyntaxComponentCandidate syntax) {

        private static final Set<String> ROLES = Set.of(
                "condition", "case_value", "return_value",
                "assignment_target", "assignment_value", "initializer_value",
                "update_expression");

        public ExpressionCandidate {
            scopePath = List.copyOf(scopePath == null ? List.of() : scopePath);
            if (!ROLES.contains(role) || range == null || ownerRange == null
                    || scopePath.isEmpty() || syntax == null
                    || !range.equals(syntax.range())) {
                throw new IllegalArgumentException("invalid structural expression candidate");
            }
            requireContained(ownerRange, range, "expression");
            if (!"translation_unit".equals(scopePath.get(0).kind())) {
                throw new IllegalArgumentException("expression scope path is not canonical");
            }
            for (int index = 1; index < scopePath.size(); index++) {
                requireContained(scopePath.get(index - 1).range(),
                        scopePath.get(index).range(), "nested scope");
            }
            requireContained(scopePath.get(scopePath.size() - 1).range(),
                    range, "expression scope");
        }

        private static void requireContained(
                SourceRangeCandidate outer, SourceRangeCandidate inner, String kind) {
            if (inner.startOffset() < outer.startOffset()
                    || inner.endOffset() > outer.endOffset()) {
                throw new IllegalArgumentException(kind + " is outside its owner range");
            }
        }
    }
}
