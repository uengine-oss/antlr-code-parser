package legacymodernizer.parser.parsing.evidence;

import java.util.List;

/** Grammar-owned lexical scope kind and exact source range. */
public record ScopeEvidenceCandidate(
        String kind,
        SourceRangeCandidate range) {

    private static final List<String> KINDS = List.of(
            "translation_unit", "package", "class", "function", "lambda", "comprehension", "block",
            "function_prototype");

    public ScopeEvidenceCandidate {
        if (!KINDS.contains(kind) || range == null || range.endOffset() <= range.startOffset()) {
            throw new IllegalArgumentException("invalid lexical scope evidence");
        }
    }
}
