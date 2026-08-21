package legacymodernizer.parser.parsing.evidence;

import java.util.List;

/** One canonical C type-name semantic-predicate decision at an identifier token. */
public record SymbolLookupEvidenceCandidate(
        SourceRangeCandidate range,
        String parserDecision,
        String resolutionStatus,
        String provenance,
        SourceRangeCandidate definitionRange,
        String configuredEvidenceId,
        List<String> predicateContexts,
        String grammarRule) {

    public SymbolLookupEvidenceCandidate {
        predicateContexts = List.copyOf(predicateContexts == null ? List.of() : predicateContexts);
        boolean resolved = "resolved".equals(resolutionStatus);
        if (range == null
                || !("type_name".equals(parserDecision)
                        || "ordinary_identifier".equals(parserDecision))
                || !(resolved || "unresolved".equals(resolutionStatus))
                || !("source_declaration".equals(provenance)
                        || "configured_preprocessing".equals(provenance)
                        || "grammar_context".equals(provenance)
                        || "unresolved_environment".equals(provenance))
                || predicateContexts.isEmpty()
                || predicateContexts.stream().anyMatch(value -> value == null || value.isBlank())
                || predicateContexts.stream().distinct().count() != predicateContexts.size()
                || grammarRule == null || grammarRule.isBlank()) {
            throw new IllegalArgumentException("invalid symbol lookup evidence");
        }
        boolean sourceResolved = resolved && "source_declaration".equals(provenance);
        boolean configuredResolved = resolved && "configured_preprocessing".equals(provenance);
        boolean grammarResolved = resolved && "grammar_context".equals(provenance);
        boolean validConfiguredId = configuredEvidenceId != null
                && configuredEvidenceId.matches("[0-9a-f]{64}");
        if ((sourceResolved && (definitionRange == null || configuredEvidenceId != null))
                || (configuredResolved && (definitionRange != null || !validConfiguredId))
                || (grammarResolved && (definitionRange != null || configuredEvidenceId != null))
                || (!sourceResolved && !configuredResolved && !grammarResolved && resolved)
                || (!resolved && (!"unresolved_environment".equals(provenance)
                        || definitionRange != null || configuredEvidenceId != null))) {
            throw new IllegalArgumentException("symbol lookup provenance contradicts resolution");
        }
    }
}
