package legacymodernizer.parser.parsing.evidence;

import java.util.List;

/** Grammar-owned import/include statement and its ordered semantic binding entries. */
public record ImportEvidenceCandidate(
        String grammarRule,
        SourceRangeCandidate range,
        String directiveKind,
        List<ImportBindingCandidate> entries) {

    public ImportEvidenceCandidate {
        if (grammarRule == null || grammarRule.isBlank()) {
            throw new IllegalArgumentException("import grammarRule is required");
        }
        if (range == null) {
            throw new IllegalArgumentException("import range is required");
        }
        if (!List.of("include", "import").contains(directiveKind)) {
            throw new IllegalArgumentException("unsupported directiveKind: " + directiveKind);
        }
        entries = List.copyOf(entries == null ? List.of() : entries);
        if (entries.isEmpty()) {
            throw new IllegalArgumentException("import statement requires at least one binding entry");
        }
        for (ImportBindingCandidate entry : entries) {
            requireInside(range, entry.targetRange(), "target");
            for (SourceRangeCandidate component : entry.pathComponentRanges()) {
                requireInside(range, component, "path component");
            }
            if (entry.memberRange() != null) {
                requireInside(range, entry.memberRange(), "member");
            }
            if (entry.aliasRange() != null) {
                requireInside(range, entry.aliasRange(), "alias");
            }
        }
    }

    private static void requireInside(
            SourceRangeCandidate outer, SourceRangeCandidate inner, String label) {
        if (inner.startOffset() < outer.startOffset()
                || inner.endOffset() > outer.endOffset()) {
            throw new IllegalArgumentException("import " + label + " must be inside statement range");
        }
    }
}
