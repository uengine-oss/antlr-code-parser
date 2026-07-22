package legacymodernizer.parser.recovery.repair;

import java.util.List;

import legacymodernizer.parser.recovery.workingcopy.TextEdit;

public record PriorAttempt(
        String stage,
        int attemptNumber,
        String workingSha256,
        List<TextEdit> edits,
        List<DiagnosticEvidence> diagnostics,
        List<Integer> qualityTuple,
        List<String> validationReasons) {
}
