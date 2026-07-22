package legacymodernizer.parser.recovery.repair;

import java.util.List;

/**
 * One parser diagnostic projected into the Agent's editable excerpt. Offsets are relative to
 * {@link FailureEnvelope#sourceExcerpt()} (the Parser-chosen slice), never to the whole unit.
 */
public record DiagnosticEvidence(
        String phase,
        String code,
        String message,
        int line,
        int column,
        int excerptStartOffset,
        int excerptEndOffset,
        String offendingToken,
        String expectedTokens,
        List<String> ruleStack,
        String tokenWindow) {
}
