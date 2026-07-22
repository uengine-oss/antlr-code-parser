package legacymodernizer.parser.recovery.candidates;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Small, declarative per-language repair knowledge (spec 012 FR-031). This is deliberately
 * data, not code: languages never add candidate-generation logic, only name which alphabetic
 * keywords are safe to delete when the grammar reports them as extraneous.
 */
public record RepairProfile(Set<String> deletableStructuralKeywords) {

    public RepairProfile {
        deletableStructuralKeywords = deletableStructuralKeywords.stream()
                .map(keyword -> keyword.toUpperCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    public static RepairProfile empty() {
        return new RepairProfile(Set.of());
    }
}
