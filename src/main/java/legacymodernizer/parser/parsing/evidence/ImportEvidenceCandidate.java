package legacymodernizer.parser.parsing.evidence;

import java.util.List;

/** Grammar-owned import/include statement and its ordered semantic binding entries. */
public record ImportEvidenceCandidate(
        String grammarRule,
        SourceRangeCandidate range,
        String directiveKind,
        List<ImportBindingCandidate> entries,
        List<ScopeEvidenceCandidate> scopePath) {

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
        scopePath = List.copyOf(scopePath == null ? List.of() : scopePath);
        if (entries.isEmpty()) {
            throw new IllegalArgumentException("import statement requires at least one binding entry");
        }
        if (scopePath.isEmpty() || !"translation_unit".equals(scopePath.get(0).kind())) {
            throw new IllegalArgumentException("import lexical scope path is required");
        }
        for (int index = 1; index < scopePath.size(); index++) {
            requireInside(scopePath.get(index - 1).range(),
                    scopePath.get(index).range(), "nested scope");
        }
        requireInside(scopePath.get(scopePath.size() - 1).range(), range, "statement");
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
