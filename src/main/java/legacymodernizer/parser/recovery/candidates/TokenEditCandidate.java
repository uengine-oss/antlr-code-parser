package legacymodernizer.parser.recovery.candidates;

import legacymodernizer.parser.recovery.workingcopy.TextEdit;

/**
 * One deterministic single-token repair candidate in the unit frame. {@code cost} orders
 * candidates for evaluation; ties that both survive strict reparse are an ambiguity and are
 * never auto-adopted.
 */
public record TokenEditCandidate(
        Kind kind,
        int unitStartOffset,
        int unitEndOffset,
        String expectedText,
        String replacement,
        EditClassification classification,
        int cost,
        String provenance) {

    public enum Kind { DELETE, INSERT, REPLACE }

    public boolean autoAdoptable() {
        return classification.autoAdoptable();
    }

    public TextEdit toTextEdit() {
        return new TextEdit(unitStartOffset, unitEndOffset, replacement,
                "GRAMMAR_GUIDED", provenance);
    }
}
