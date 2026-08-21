package legacymodernizer.parser.parsing.evidence;

import java.util.List;

/** Complete observed C definition/type-name lookup population for one parse. */
public record SymbolEvidenceExtraction(
        List<SymbolDefinitionEvidenceCandidate> definitions,
        List<SymbolLookupEvidenceCandidate> lookups) {

    public SymbolEvidenceExtraction {
        definitions = List.copyOf(definitions == null ? List.of() : definitions);
        lookups = List.copyOf(lookups == null ? List.of() : lookups);
    }

    public int unresolvedLookups() {
        return (int) lookups.stream()
                .filter(item -> "unresolved".equals(item.resolutionStatus()))
                .count();
    }
}
