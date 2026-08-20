package legacymodernizer.parser.parsing.evidence;

import java.nio.charset.StandardCharsets;
import java.util.List;

import legacymodernizer.parser.parsing.build.CompilationUnitBuildContext;
import legacymodernizer.parser.parsing.build.CompilationUnitBuildStatus;
import legacymodernizer.parser.recovery.workingcopy.Hashes;

/** Build and trace closure for configuration-specific preprocessing. */
public record ConfiguredPreprocessingEvidence(
        String version,
        String status,
        String trust,
        CompilationUnitBuildContext build,
        TraceCompleteness trace) {

    private static final String TRACE_ID_DOMAIN = "robo-preprocessing-trace-v1";

    public ConfiguredPreprocessingEvidence {
        if (!"1.0.0".equals(version) || build == null || trace == null) {
            throw new IllegalArgumentException("configured preprocessing contract is incomplete");
        }
        if (!List.of("authoritative", "compatible_replay", "unresolved").contains(trust)
                || !List.of("exact", "partial", "unresolved").contains(status)) {
            throw new IllegalArgumentException("invalid preprocessing status or trust");
        }
        if ("exact".equals(status)) {
            throw new IllegalArgumentException(
                    "configured preprocessing v1 cannot authorize raw-source semantic occurrences");
        }
        String expectedStatus;
        String expectedTrust;
        if (build.status() != CompilationUnitBuildStatus.EXACT
                || "unresolved".equals(trace.status())) {
            expectedStatus = "unresolved";
            expectedTrust = "unresolved";
        } else if ("compatible_replay".equals(trust)) {
            expectedStatus = "partial";
            expectedTrust = "compatible_replay";
        } else if ("exact".equals(trace.status())) {
            expectedStatus = "exact";
            expectedTrust = "authoritative";
        } else {
            expectedStatus = "partial";
            expectedTrust = "authoritative";
        }
        if (!expectedStatus.equals(status) || !expectedTrust.equals(trust)) {
            throw new IllegalArgumentException(
                    "configured preprocessing status/trust contradict build and trace closure");
        }
    }

    public static ConfiguredPreprocessingEvidence withoutTrace(
            String sourceId, CompilationUnitBuildContext build) {
        if (sourceId == null || sourceId.isBlank()) {
            throw new IllegalArgumentException("sourceId is required for trace evidence identity");
        }
        String reason = build.status() == CompilationUnitBuildStatus.EXACT
                ? "insufficient_preprocessing_trace_provider"
                : "insufficient_preprocessing_build_context";
        String buildIdentity = String.join("\0", build.commandOccurrenceIds()) + "\0"
                + String.join("\0", build.unresolvedEvidenceIds());
        String traceEvidenceId = Hashes.sha256((TRACE_ID_DOMAIN + "\0" + sourceId + "\0"
                + buildIdentity + "\0" + reason).getBytes(StandardCharsets.UTF_8));
        return new ConfiguredPreprocessingEvidence("1.0.0", "unresolved", "unresolved", build,
                new TraceCompleteness("unresolved", 1, 0, 1,
                        List.of(traceEvidenceId), List.of(), List.of(traceEvidenceId),
                        List.of(reason)));
    }

    public record TraceCompleteness(
            String status,
            int population,
            int emitted,
            int explicitlyUnresolved,
            List<String> evidenceIds,
            List<String> emittedEvidenceIds,
            List<String> unresolvedEvidenceIds,
            List<String> reasons) {

        public TraceCompleteness {
            evidenceIds = List.copyOf(evidenceIds == null ? List.of() : evidenceIds);
            emittedEvidenceIds = List.copyOf(
                    emittedEvidenceIds == null ? List.of() : emittedEvidenceIds);
            unresolvedEvidenceIds = List.copyOf(
                    unresolvedEvidenceIds == null ? List.of() : unresolvedEvidenceIds);
            reasons = List.copyOf(reasons == null ? List.of() : reasons);
            if (!List.of("exact", "partial", "unresolved").contains(status)
                    || population < 0 || emitted < 0 || explicitlyUnresolved < 0
                    || population != emitted + explicitlyUnresolved
                    || population != evidenceIds.size()
                    || emitted != emittedEvidenceIds.size()
                    || explicitlyUnresolved != unresolvedEvidenceIds.size()
                    || !validIds(evidenceIds) || !validIds(emittedEvidenceIds)
                    || !validIds(unresolvedEvidenceIds)
                    || !evidenceIds.stream().collect(java.util.stream.Collectors.toSet())
                            .equals(java.util.stream.Stream.concat(
                                    emittedEvidenceIds.stream(), unresolvedEvidenceIds.stream())
                                    .collect(java.util.stream.Collectors.toSet()))) {
                throw new IllegalArgumentException("invalid preprocessing trace completeness");
            }
            if ("exact".equals(status)
                    && (explicitlyUnresolved != 0 || !reasons.isEmpty())) {
                throw new IllegalArgumentException("exact trace cannot contain unresolved evidence");
            }
            if (!"exact".equals(status) && reasons.isEmpty()) {
                throw new IllegalArgumentException("non-exact trace requires an explicit reason");
            }
        }

        private static boolean validIds(List<String> values) {
            return values.stream().allMatch(value -> value != null
                    && value.matches("[0-9a-f]{64}"))
                    && values.stream().distinct().count() == values.size();
        }
    }
}
