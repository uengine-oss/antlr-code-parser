package legacymodernizer.parser.parsing.evidence;

/** Zero-based, half-open Unicode code-point range supplied by an ANTLR frontend. */
public record SourceRangeCandidate(int startOffset, int endOffset) {
    public SourceRangeCandidate {
        if (startOffset < 0 || endOffset < startOffset) {
            throw new IllegalArgumentException("invalid source range: " + startOffset + ".." + endOffset);
        }
    }
}
