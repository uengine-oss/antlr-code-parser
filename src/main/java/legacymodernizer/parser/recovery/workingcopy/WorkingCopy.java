package legacymodernizer.parser.recovery.workingcopy;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class WorkingCopy {

    private final String originalText;
    private final String workingText;
    private final String originalSha256;
    private final String workingSha256;
    private final List<TextEdit> edits;
    private final SourceMap sourceMap;

    private WorkingCopy(String originalText, String workingText, List<TextEdit> edits,
                        SourceMap sourceMap) {
        this.originalText = originalText;
        this.workingText = workingText;
        this.originalSha256 = hash(originalText);
        this.workingSha256 = hash(workingText);
        this.edits = List.copyOf(edits);
        this.sourceMap = sourceMap;
    }

    public static WorkingCopy exact(String originalText) {
        String text = originalText == null ? "" : originalText;
        int[] identity = new int[text.length() + 1];
        for (int index = 0; index < identity.length; index++) identity[index] = index;
        return new WorkingCopy(text, text, List.of(), new SourceMap(text, text, identity));
    }

    public WorkingCopy applyOriginalEdits(List<TextEdit> proposed) {
        List<TextEdit> ordered = new ArrayList<>(proposed == null ? List.of() : proposed);
        ordered.sort(Comparator.comparingInt(TextEdit::startOffset)
                .thenComparingInt(TextEdit::endOffset));
        validate(ordered);

        StringBuilder output = new StringBuilder(originalText.length());
        List<Integer> mapping = new ArrayList<>(originalText.length() + 1);
        int cursor = 0;
        for (TextEdit edit : ordered) {
            appendUnchanged(output, mapping, cursor, edit.startOffset());
            for (int index = 0; index < edit.replacement().length(); index++) {
                output.append(edit.replacement().charAt(index));
                int mapped = edit.replacement().length() == edit.changedOriginalLength()
                        ? edit.startOffset() + index : edit.startOffset();
                mapping.add(Math.min(mapped, edit.endOffset()));
            }
            cursor = edit.endOffset();
        }
        appendUnchanged(output, mapping, cursor, originalText.length());
        mapping.add(originalText.length());

        int[] offsets = mapping.stream().mapToInt(Integer::intValue).toArray();
        String working = output.toString();
        return new WorkingCopy(originalText, working, ordered,
                new SourceMap(originalText, working, offsets));
    }

    public String unifiedDiff(String path) {
        if (edits.isEmpty()) return "";
        String label = path == null ? "source" : path.replace('\\', '/');
        StringBuilder diff = new StringBuilder("--- a/").append(label)
                .append("\n+++ b/").append(label).append('\n');
        for (TextEdit edit : edits) {
            diff.append("@@ offset ").append(edit.startOffset()).append(',')
                    .append(edit.endOffset()).append(" rule ").append(edit.ruleId()).append(" @@\n")
                    .append('-').append(escaped(originalText.substring(edit.startOffset(), edit.endOffset())))
                    .append('\n').append('+').append(escaped(edit.replacement())).append('\n');
        }
        return diff.toString();
    }

    public String originalText() { return originalText; }
    public String workingText() { return workingText; }
    public String originalSha256() { return originalSha256; }
    public String workingSha256() { return workingSha256; }
    public List<TextEdit> edits() { return edits; }
    public SourceMap sourceMap() { return sourceMap; }

    private void validate(List<TextEdit> ordered) {
        int previousEnd = 0;
        for (TextEdit edit : ordered) {
            if (edit.endOffset() > originalText.length()) {
                throw new IllegalArgumentException("Edit outside original: " + edit);
            }
            if (edit.startOffset() < previousEnd) {
                throw new IllegalArgumentException("Overlapping edits are forbidden: " + edit);
            }
            previousEnd = edit.endOffset();
        }
    }

    private void appendUnchanged(StringBuilder output, List<Integer> mapping, int start, int end) {
        for (int index = start; index < end; index++) {
            output.append(originalText.charAt(index));
            mapping.add(index);
        }
    }

    private static String hash(String text) {
        return Hashes.sha256(text.getBytes(StandardCharsets.UTF_8));
    }

    private static String escaped(String text) {
        return text.replace("\\", "\\\\").replace("\r", "\\r").replace("\n", "\\n");
    }
}
