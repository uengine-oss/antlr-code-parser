package legacymodernizer.parser.parsing.evidence;

/** Grammar-owned C include occurrence and its exact target token range. */
public record ImportEvidenceCandidate(
        String grammarRule,
        SourceRangeCandidate range,
        SourceRangeCandidate targetRange,
        String targetKind) {

    public ImportEvidenceCandidate {
        if (grammarRule == null || grammarRule.isBlank()) {
            throw new IllegalArgumentException("import grammarRule is required");
        }
        if (range == null || targetRange == null
                || targetRange.startOffset() < range.startOffset()
                || targetRange.endOffset() > range.endOffset()) {
            throw new IllegalArgumentException("import target must be inside directive range");
        }
        if (!java.util.List.of("quoted", "angle", "computed").contains(targetKind)) {
            throw new IllegalArgumentException("unsupported import targetKind: " + targetKind);
        }
    }
}
