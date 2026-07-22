package legacymodernizer.parser.recovery.workingcopy;

public record SourceMapSummary(
        String mappingMode,
        int originalLength,
        int workingLength,
        int originalLineCount,
        int workingLineCount,
        boolean preservesLineCount) {
}
