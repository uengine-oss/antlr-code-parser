package legacymodernizer.parser.recovery.workingcopy;

import java.util.Arrays;

public final class SourceMap {

    private final String original;
    private final String working;
    private final int[] workingToOriginalOffset;
    private final int[] originalLineStarts;
    private final int[] workingLineStarts;

    SourceMap(String original, String working, int[] workingToOriginalOffset) {
        this.original = original;
        this.working = working;
        this.workingToOriginalOffset = Arrays.copyOf(workingToOriginalOffset, workingToOriginalOffset.length);
        this.originalLineStarts = lineStarts(original);
        this.workingLineStarts = lineStarts(working);
    }

    public int originalOffset(int workingOffset) {
        if (workingOffset < 0 || workingOffset >= workingToOriginalOffset.length) {
            throw new IllegalArgumentException("Working offset out of range: " + workingOffset);
        }
        return workingToOriginalOffset[workingOffset];
    }

    public int originalLine(int workingLine) {
        if (workingLine <= 0 || workingLine > workingLineStarts.length) {
            throw new IllegalArgumentException("Working line out of range: " + workingLine);
        }
        return lineOfOriginalOffset(originalOffset(workingLineStarts[workingLine - 1]));
    }

    public boolean preservesLineCount() {
        return originalLineStarts.length == workingLineStarts.length;
    }

    public String original() {
        return original;
    }

    public String working() {
        return working;
    }

    public SourceMapSummary summary() {
        return new SourceMapSummary("ORIGINAL_OFFSET", original.length(), working.length(),
                originalLineStarts.length, workingLineStarts.length, preservesLineCount());
    }

    private int lineOfOriginalOffset(int offset) {
        int index = Arrays.binarySearch(originalLineStarts, Math.min(offset, original.length()));
        if (index >= 0) return index + 1;
        return -index - 1;
    }

    private static int[] lineStarts(String text) {
        int count = 1;
        for (int index = 0; index < text.length(); index++) {
            if (text.charAt(index) == '\n') count++;
        }
        int[] starts = new int[count];
        int line = 1;
        for (int index = 0; index < text.length(); index++) {
            if (text.charAt(index) == '\n') starts[line++] = index + 1;
        }
        return starts;
    }
}
