package legacymodernizer.parser.parsing.evidence;

import java.util.List;

/** Language-adapter-owned callable declarations and one binding decision per call. */
public record CallableEvidenceExtraction(
        List<CallableCandidate> callables,
        List<CallBindingCandidate> bindings) {

    public static final String C_SOURCE_ADAPTER = "c-source-binding/v1";

    public CallableEvidenceExtraction {
        callables = List.copyOf(callables == null ? List.of() : callables);
        bindings = List.copyOf(bindings == null ? List.of() : bindings);
    }

    public record CallableCandidate(
            String grammarRule,
            SourceRangeCandidate range,
            SourceRangeCandidate nameRange,
            String role,
            String adapterSchema,
            String terminalName,
            String targetScope,
            String compatibilityMaterial,
            String compatibilityStatus,
            String compatibilityScope,
            String definitionStatus,
            String configurationId,
            SourceRangeCandidate astNodeRange,
            SourceRangeCandidate visibilityRange,
            int visibilityStartOffset) {

        public CallableCandidate {
            if (grammarRule == null || grammarRule.isBlank()
                    || range == null || nameRange == null
                    || !("declaration".equals(role) || "definition".equals(role))
                    || adapterSchema == null || adapterSchema.isBlank()
                    || terminalName == null || terminalName.isBlank()
                    || !("source_file".equals(targetScope) || "corpus".equals(targetScope))
                    || !("exact".equals(compatibilityStatus)
                            || "configuration_bound".equals(compatibilityStatus)
                            || "unavailable".equals(compatibilityStatus))
                    || !("source_file".equals(compatibilityScope)
                            || "corpus".equals(compatibilityScope)
                            || "configuration".equals(compatibilityScope)
                            || "unavailable".equals(compatibilityScope))
                    || !("exact".equals(definitionStatus)
                            || "configuration_dependent".equals(definitionStatus)
                            || "not_applicable".equals(definitionStatus))
                    || visibilityRange == null
                    || visibilityStartOffset < nameRange.endOffset()
                    || visibilityStartOffset > visibilityRange.endOffset()) {
                throw new IllegalArgumentException("invalid callable evidence candidate");
            }
            if ("definition".equals(role) != (astNodeRange != null)) {
                throw new IllegalArgumentException(
                        "only callable definitions require astNodeRange");
            }
            if ("definition".equals(role)
                    == "not_applicable".equals(definitionStatus)) {
                throw new IllegalArgumentException(
                        "callable role/definition status contradict each other");
            }
            if ("unavailable".equals(compatibilityStatus)
                    != (compatibilityMaterial == null)) {
                throw new IllegalArgumentException(
                        "compatibility material/status contradict each other");
            }
            if ("unavailable".equals(compatibilityStatus)
                    != "unavailable".equals(compatibilityScope)) {
                throw new IllegalArgumentException(
                        "compatibility status/scope contradict each other");
            }
        }
    }

    public record CallBindingCandidate(
            SourceRangeCandidate callRange,
            String adapterSchema,
            String status,
            String resolutionMode,
            SourceRangeCandidate declarationNameRange,
            String targetScope,
            String dispatch,
            String configurationId,
            String provenance,
            String reason,
            List<SourceRangeCandidate> candidateMacroRanges,
            List<SourceRangeCandidate> candidateCallableRanges) {

        public CallBindingCandidate {
            candidateMacroRanges = List.copyOf(
                    candidateMacroRanges == null ? List.of() : candidateMacroRanges);
            candidateCallableRanges = List.copyOf(
                    candidateCallableRanges == null ? List.of() : candidateCallableRanges);
            if (callRange == null || adapterSchema == null || adapterSchema.isBlank()
                    || !List.of("declaration_bound", "external",
                            "configuration_dependent", "dynamic", "ambiguous",
                            "unsupported").contains(status)
                    || !List.of("direct_definition", "compatible_definition", "none")
                            .contains(resolutionMode)
                    || !List.of("source_file", "corpus", "runtime").contains(targetScope)
                    || !List.of("exact", "virtual", "dynamic").contains(dispatch)
                    || !List.of("grammar_scope", "configured_semantic_trace",
                            "runtime_semantics", "unsupported_adapter").contains(provenance)) {
                throw new IllegalArgumentException("invalid call binding evidence candidate");
            }
            if ("declaration_bound".equals(status) != (declarationNameRange != null)) {
                throw new IllegalArgumentException(
                        "declaration_bound requires exactly one declaration reference");
            }
            if ("declaration_bound".equals(status)
                    != !"none".equals(resolutionMode)) {
                throw new IllegalArgumentException(
                        "binding status/resolution mode contradict each other");
            }
            boolean hasCandidateEvidence = !(candidateMacroRanges.isEmpty()
                    && candidateCallableRanges.isEmpty());
            if ("configuration_dependent".equals(status) && !hasCandidateEvidence) {
                throw new IllegalArgumentException(
                        "configuration-dependent binding requires candidate evidence");
            }
            if (List.of("declaration_bound", "external", "dynamic").contains(status)
                    && hasCandidateEvidence) {
                throw new IllegalArgumentException(
                        "resolved/external/dynamic binding cannot retain candidates");
            }
            if ("ambiguous".equals(status)
                    && (!candidateMacroRanges.isEmpty()
                            || candidateCallableRanges.size() < 2)) {
                throw new IllegalArgumentException(
                        "ambiguous binding requires multiple callable candidates");
            }
            if ("unsupported".equals(status) && !candidateMacroRanges.isEmpty()) {
                throw new IllegalArgumentException(
                        "unsupported binding candidates must be callable facts");
            }
            if ("declaration_bound".equals(status) && reason != null) {
                throw new IllegalArgumentException("bound call cannot have a reason");
            }
            if (!"declaration_bound".equals(status)
                    && (reason == null || reason.isBlank())) {
                throw new IllegalArgumentException("non-bound call requires a reason");
            }
        }
    }
}
