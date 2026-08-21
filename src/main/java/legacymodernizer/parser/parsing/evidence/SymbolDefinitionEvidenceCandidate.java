package legacymodernizer.parser.parsing.evidence;

/** Grammar/listener-owned C identifier definition and lexical scope. */
public record SymbolDefinitionEvidenceCandidate(
        SourceRangeCandidate range,
        String symbolKind,
        String scopeKind,
        SourceRangeCandidate scopeRange,
        int visibilityStartOffset,
        String grammarRule) {

    public SymbolDefinitionEvidenceCandidate {
        if (range == null || scopeRange == null
                || !("typedef_name".equals(symbolKind)
                        || "ordinary_identifier".equals(symbolKind))
                || !("file".equals(scopeKind) || "block".equals(scopeKind)
                        || "function_prototype".equals(scopeKind))
                || grammarRule == null || grammarRule.isBlank()
                || visibilityStartOffset < range.endOffset()
                || visibilityStartOffset > scopeRange.endOffset()
                || range.startOffset() < scopeRange.startOffset()
                || range.endOffset() > scopeRange.endOffset()) {
            throw new IllegalArgumentException("invalid symbol definition evidence");
        }
    }
}
