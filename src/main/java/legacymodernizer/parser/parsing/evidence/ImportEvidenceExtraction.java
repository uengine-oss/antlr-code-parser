package legacymodernizer.parser.parsing.evidence;

import java.util.List;

/** Complete emitted/unresolved include accounting from the preprocessing grammar. */
public record ImportEvidenceExtraction(
        List<ImportEvidenceCandidate> candidates,
        int explicitlyUnresolved,
        List<String> reasons) {

    public ImportEvidenceExtraction {
        candidates = List.copyOf(candidates == null ? List.of() : candidates);
        reasons = List.copyOf(reasons == null ? List.of() : reasons);
        if (explicitlyUnresolved < 0) {
            throw new IllegalArgumentException("explicitlyUnresolved cannot be negative");
        }
        if (explicitlyUnresolved > 0 && reasons.isEmpty()) {
            throw new IllegalArgumentException("unresolved import population requires a reason");
        }
    }

    public int population() {
        return candidates.size() + explicitlyUnresolved;
    }
}
