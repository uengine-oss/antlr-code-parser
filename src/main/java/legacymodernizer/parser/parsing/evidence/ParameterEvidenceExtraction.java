package legacymodernizer.parser.parsing.evidence;

import java.util.List;

/** Complete grammar-owned parameter binding population for one source parse. */
public record ParameterEvidenceExtraction(
        List<ParameterCandidate> parameters,
        int explicitlyUnresolved,
        List<String> reasons) {

    public ParameterEvidenceExtraction {
        parameters = List.copyOf(parameters == null ? List.of() : parameters);
        reasons = List.copyOf(reasons == null ? List.of() : reasons);
        if (explicitlyUnresolved < 0 || (explicitlyUnresolved > 0 && reasons.isEmpty())) {
            throw new IllegalArgumentException("parameter evidence accounting is incomplete");
        }
    }

    /** One source-backed parameter name and the lexical scope it binds. */
    public record ParameterCandidate(
            String grammarRule,
            SourceRangeCandidate range,
            List<ScopeEvidenceCandidate> scopePath) {

        public ParameterCandidate {
            scopePath = List.copyOf(scopePath == null ? List.of() : scopePath);
            if (grammarRule == null || grammarRule.isBlank() || range == null
                    || scopePath.isEmpty()
                    || !"translation_unit".equals(scopePath.get(0).kind())) {
                throw new IllegalArgumentException("invalid parameter evidence candidate");
            }
            ScopeEvidenceCandidate owner = scopePath.get(scopePath.size() - 1);
            if (!("function".equals(owner.kind()) || "lambda".equals(owner.kind()))
                    || range.startOffset() < owner.range().startOffset()
                    || range.endOffset() > owner.range().endOffset()) {
                throw new IllegalArgumentException("parameter must belong to a function or lambda");
            }
        }
    }
}
