package legacymodernizer.parser.parsing.evidence;

import java.util.List;

/** Frontend identity and grammar-owned callable syntax. */
public record CallableEvidenceExtraction(
        String language,
        String frontendSchema,
        List<CallableCandidate> callables,
        int explicitlyUnresolved,
        List<String> reasons) {

    public CallableEvidenceExtraction {
        callables = List.copyOf(callables == null ? List.of() : callables);
        reasons = List.copyOf(reasons == null ? List.of() : reasons);
        if (language == null || language.isBlank()
                || frontendSchema == null || frontendSchema.isBlank()
                || explicitlyUnresolved < 0
                || (explicitlyUnresolved > 0 && reasons.isEmpty())) {
            throw new IllegalArgumentException("frontend identity is required");
        }
    }

    public record CallableSyntaxCandidate(
            String schema,
            List<SyntaxComponentCandidate> declarationSpecifiers,
            SyntaxComponentCandidate declarator,
            List<SyntaxComponentCandidate> attributes) {

        public CallableSyntaxCandidate {
            declarationSpecifiers = List.copyOf(
                    declarationSpecifiers == null ? List.of() : declarationSpecifiers);
            attributes = List.copyOf(attributes == null ? List.of() : attributes);
            if (schema == null || schema.isBlank() || declarator == null) {
                throw new IllegalArgumentException("callable syntax is incomplete");
            }
        }
    }

    public record CallableCandidate(
            String grammarRule,
            SourceRangeCandidate range,
            SourceRangeCandidate nameRange,
            String role,
            SourceRangeCandidate astNodeRange,
            List<ScopeEvidenceCandidate> scopePath,
            SourceRangeCandidate scopeRange,
            int declarationPoint,
            CallableSyntaxCandidate syntax) {

        public CallableCandidate {
            scopePath = List.copyOf(scopePath == null ? List.of() : scopePath);
            if (grammarRule == null || grammarRule.isBlank()
                    || range == null || nameRange == null
                    || !("declaration".equals(role) || "definition".equals(role))
                    || scopePath.isEmpty() || scopeRange == null || syntax == null
                    || declarationPoint < nameRange.endOffset()
                    || declarationPoint > scopeRange.endOffset()) {
                throw new IllegalArgumentException("invalid callable evidence candidate");
            }
            if ("definition".equals(role) != (astNodeRange != null)) {
                throw new IllegalArgumentException(
                        "only callable definitions require astNodeRange");
            }
            if (!scopePath.get(0).kind().equals("translation_unit")
                    || !scopePath.get(scopePath.size() - 1).range().equals(scopeRange)) {
                throw new IllegalArgumentException("callable scope path is not canonical");
            }
            for (int index = 1; index < scopePath.size(); index++) {
                requireContained(scopePath.get(index - 1).range(),
                        scopePath.get(index).range(), "nested scope");
            }
            requireContained(scopeRange, nameRange, "callable name");
        }

        private static void requireContained(
                SourceRangeCandidate outer, SourceRangeCandidate inner, String kind) {
            if (inner.startOffset() < outer.startOffset()
                    || inner.endOffset() > outer.endOffset()) {
                throw new IllegalArgumentException(kind + " is outside callable scope");
            }
        }
    }
}
