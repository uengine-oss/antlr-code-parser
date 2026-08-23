package legacymodernizer.parser.parsing.evidence;

import java.util.List;

/** One ANTLR parser context with direct terminals and direct child contexts. */
public record SyntaxComponentCandidate(
        String grammarRule,
        SourceRangeCandidate range,
        List<SyntaxTokenCandidate> directTokens,
        List<SyntaxComponentCandidate> children) {

    public SyntaxComponentCandidate {
        directTokens = List.copyOf(directTokens == null ? List.of() : directTokens);
        children = List.copyOf(children == null ? List.of() : children);
        if (grammarRule == null || grammarRule.isBlank() || range == null
                || range.endOffset() <= range.startOffset()) {
            throw new IllegalArgumentException("invalid syntax component evidence");
        }
        directTokens.forEach(token -> requireContained(range, token.range(), "token"));
        children.forEach(child -> requireContained(range, child.range(), "child"));
    }

    private static void requireContained(
            SourceRangeCandidate outer, SourceRangeCandidate inner, String kind) {
        if (inner.startOffset() < outer.startOffset()
                || inner.endOffset() > outer.endOffset()) {
            throw new IllegalArgumentException(kind + " is outside syntax component");
        }
    }
}
