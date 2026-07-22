package legacymodernizer.parser.recovery.boundaries;

public record UnitParseContext(
        String contextId,
        String sourceText,
        int leadingContextLines) {

    public UnitParseContext {
        if (contextId == null || contextId.isBlank()) {
            throw new IllegalArgumentException("Context id is required");
        }
        if (sourceText == null) {
            throw new IllegalArgumentException("Context source is required");
        }
        if (leadingContextLines < 0) {
            throw new IllegalArgumentException("Leading context line count cannot be negative");
        }
    }
}
