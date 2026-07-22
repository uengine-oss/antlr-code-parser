package legacymodernizer.parser.recovery.workingcopy;

public record TextEdit(
        int startOffset,
        int endOffset,
        String replacement,
        String ruleId,
        String reason) {

    public TextEdit {
        if (startOffset < 0 || endOffset < startOffset) {
            throw new IllegalArgumentException("Invalid edit range: " + startOffset + ".." + endOffset);
        }
        replacement = replacement == null ? "" : replacement;
        ruleId = ruleId == null ? "unknown" : ruleId;
        reason = reason == null ? "" : reason;
    }

    public int changedOriginalLength() {
        return endOffset - startOffset;
    }
}
