package legacymodernizer.parser.recovery.boundaries;

public record SourceUnit(
        String unitId,
        UnitKind kind,
        String name,
        String parentUnitId,
        int startOffset,
        int endOffset,
        int startLine,
        int endLine,
        int ordinal,
        String boundaryConfidence) {
}
