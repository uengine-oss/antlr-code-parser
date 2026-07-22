package legacymodernizer.parser.recovery.boundaries;

public record UnitParseRequest(
        String sourceText,
        String fileName,
        String filePath,
        SourceUnit sourceUnit,
        int leadingContextLines) {

    public UnitParseRequest(String sourceText, String fileName, String filePath,
                            SourceUnit sourceUnit) {
        this(sourceText, fileName, filePath, sourceUnit, 0);
    }

    public UnitParseRequest {
        if (leadingContextLines < 0
                || (sourceUnit != null && leadingContextLines >= sourceUnit.startLine())) {
            throw new IllegalArgumentException("Invalid leading context line count");
        }
    }

    public int originalLineOffset() {
        return sourceUnit == null ? 0
                : Math.max(0, sourceUnit.startLine() - 1 - leadingContextLines);
    }
}
