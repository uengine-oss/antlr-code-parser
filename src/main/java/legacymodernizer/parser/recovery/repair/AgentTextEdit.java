package legacymodernizer.parser.recovery.repair;

public record AgentTextEdit(
        int startOffset,
        int endOffset,
        String expectedText,
        String replacement,
        String reason) {
}
