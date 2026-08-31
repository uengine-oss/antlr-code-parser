package legacymodernizer.parser.parsing.evidence;

import java.util.List;
import java.util.Set;

/** Complete grammar-owned Python global/nonlocal name population. */
public record ScopeDirectiveEvidenceExtraction(
        List<ScopeDirectiveCandidate> directives,
        int explicitlyUnresolved,
        List<String> reasons) {

    public ScopeDirectiveEvidenceExtraction {
        directives = List.copyOf(directives == null ? List.of() : directives);
        reasons = List.copyOf(reasons == null ? List.of() : reasons);
        if (explicitlyUnresolved < 0 || (explicitlyUnresolved > 0 && reasons.isEmpty())) {
            throw new IllegalArgumentException("scope directive evidence accounting is incomplete");
        }
    }

    /** One name explicitly listed by a Python global or nonlocal statement. */
    public record ScopeDirectiveCandidate(
            String grammarRule,
            SourceRangeCandidate range,
            String directiveKind,
            List<ScopeEvidenceCandidate> scopePath) {

        private static final Set<String> KINDS = Set.of("global", "nonlocal");

        public ScopeDirectiveCandidate {
            scopePath = List.copyOf(scopePath == null ? List.of() : scopePath);
            if (grammarRule == null || grammarRule.isBlank() || range == null
                    || !KINDS.contains(directiveKind) || scopePath.isEmpty()
                    || !"translation_unit".equals(scopePath.get(0).kind())) {
                throw new IllegalArgumentException("invalid scope directive evidence candidate");
            }
        }
    }
}
