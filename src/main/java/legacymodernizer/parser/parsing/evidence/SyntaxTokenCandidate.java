package legacymodernizer.parser.parsing.evidence;

/** Direct ANTLR terminal kind and exact source range. */
public record SyntaxTokenCandidate(
        String tokenKind,
        SourceRangeCandidate range) {

    public SyntaxTokenCandidate {
        if (tokenKind == null || tokenKind.isBlank() || range == null
                || range.endOffset() <= range.startOffset()) {
            throw new IllegalArgumentException("invalid syntax token evidence");
        }
    }
}
