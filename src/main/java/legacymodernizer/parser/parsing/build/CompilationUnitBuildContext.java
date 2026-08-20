package legacymodernizer.parser.parsing.build;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Exact source-to-command resolution without path/name guessing. */
public record CompilationUnitBuildContext(
        String version,
        CompilationUnitBuildStatus status,
        int population,
        int emitted,
        int explicitlyUnresolved,
        List<String> commandOccurrenceIds,
        List<String> emittedCommandOccurrenceIds,
        List<String> unresolvedCommandOccurrenceIds,
        List<String> unresolvedEvidenceIds,
        List<String> unresolvedReasons) {

    public CompilationUnitBuildContext {
        if (!"1.0.0".equals(version) || status == null) {
            throw new IllegalArgumentException("unsupported build context version or status");
        }
        commandOccurrenceIds = immutableUnique(commandOccurrenceIds, "commandOccurrenceIds");
        emittedCommandOccurrenceIds = immutableUnique(
                emittedCommandOccurrenceIds, "emittedCommandOccurrenceIds");
        unresolvedCommandOccurrenceIds = immutableUnique(
                unresolvedCommandOccurrenceIds, "unresolvedCommandOccurrenceIds");
        unresolvedEvidenceIds = immutableUnique(
                unresolvedEvidenceIds, "unresolvedEvidenceIds");
        unresolvedReasons = List.copyOf(unresolvedReasons == null ? List.of() : unresolvedReasons);
        if (population < 0 || emitted < 0 || explicitlyUnresolved < 0
                || population != emitted + explicitlyUnresolved) {
            throw new IllegalArgumentException(
                    "population must equal emitted + explicitlyUnresolved");
        }
        if (emitted != emittedCommandOccurrenceIds.size()) {
            throw new IllegalArgumentException("emitted command IDs do not match emitted count");
        }
        if (explicitlyUnresolved != unresolvedEvidenceIds.size()) {
            throw new IllegalArgumentException(
                    "unresolved evidence IDs do not match explicitlyUnresolved count");
        }
        Set<String> allIds = new HashSet<>(commandOccurrenceIds);
        if (!allIds.containsAll(emittedCommandOccurrenceIds)
                || !allIds.containsAll(unresolvedCommandOccurrenceIds)) {
            throw new IllegalArgumentException("partition command IDs must belong to the context");
        }
        if (!new HashSet<>(unresolvedEvidenceIds).containsAll(
                unresolvedCommandOccurrenceIds)) {
            throw new IllegalArgumentException(
                    "unresolved command IDs must belong to unresolved evidence IDs");
        }
        if (status == CompilationUnitBuildStatus.EXACT
                && (explicitlyUnresolved != 0 || emitted == 0 || !unresolvedReasons.isEmpty())) {
            throw new IllegalArgumentException("exact build context cannot be empty or unresolved");
        }
        if (status == CompilationUnitBuildStatus.UNRESOLVED && emitted != 0) {
            throw new IllegalArgumentException("unresolved build context cannot emit commands");
        }
    }

    private static List<String> immutableUnique(List<String> values, String field) {
        List<String> result = List.copyOf(values == null ? List.of() : values);
        if (result.size() != new HashSet<>(result).size()
                || result.stream().anyMatch(value -> value == null
                        || !value.matches("[0-9a-f]{64}"))) {
            throw new IllegalArgumentException(field + " must contain unique SHA-256 IDs");
        }
        return result;
    }
}
