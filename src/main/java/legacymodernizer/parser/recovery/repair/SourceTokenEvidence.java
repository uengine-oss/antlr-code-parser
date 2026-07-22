package legacymodernizer.parser.recovery.repair;

/** One exact source token with excerpt-relative, end-exclusive coordinates. */
public record SourceTokenEvidence(
        int startOffset,
        int endOffset,
        String text) {
}
