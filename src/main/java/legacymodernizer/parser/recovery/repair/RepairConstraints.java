package legacymodernizer.parser.recovery.repair;

import java.util.List;

public record RepairConstraints(
        int allowedStartOffset,
        int allowedEndOffset,
        int maxChangedCharacters,
        int maxChangedLines,
        int remainingAttempts,
        List<String> forbiddenOutputs) {
}
