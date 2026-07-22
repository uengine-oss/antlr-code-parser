package legacymodernizer.parser.parsing.languages.java;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import legacymodernizer.parser.recovery.boundaries.SourceUnit;
import legacymodernizer.parser.recovery.boundaries.UnitBoundaries;
import legacymodernizer.parser.recovery.boundaries.UnitKind;
import legacymodernizer.parser.recovery.workingcopy.Hashes;
import legacymodernizer.parser.parsing.boundaries.CStyleStructuralMasker;

public final class JavaSourceUnitLocator {

    private static final Pattern TYPE = Pattern.compile(
            "\\b(class|interface)[\\t ]+([A-Za-z_$][A-Za-z0-9_$]*)\\b");

    public List<SourceUnit> locate(String source) {
        String text = source == null ? "" : source;
        String masked = CStyleStructuralMasker.mask(text);
        int[] depth = braceDepth(masked);
        List<Start> starts = new ArrayList<>();
        Matcher matcher = TYPE.matcher(masked);
        while (matcher.find()) {
            if (depth[matcher.start()] != 0) continue;
            int open = masked.indexOf('{', matcher.end());
            if (open < 0 || depth[open] != 0) continue;
            starts.add(new Start(declarationStart(text, matcher.start()), open, matcher.group(2)));
        }
        if (starts.isEmpty()) return List.of(UnitBoundaries.fileUnit("java", text));

        List<SourceUnit> units = new ArrayList<>();
        for (int index = 0; index < starts.size(); index++) {
            Start start = starts.get(index);
            int close = matchingClose(masked, start.openBrace());
            int fallback = index + 1 < starts.size() ? starts.get(index + 1).startOffset() : text.length();
            int end = close >= 0 ? close + 1 : fallback;
            String confidence = close >= 0 ? "EXACT" : "CONSERVATIVE";
            units.add(unit(text, start.startOffset(), end, index, start.name(), confidence));
        }
        return List.copyOf(units);
    }

    private static int[] braceDepth(String source) {
        int[] depths = new int[source.length() + 1];
        int depth = 0;
        for (int index = 0; index < source.length(); index++) {
            depths[index] = depth;
            if (source.charAt(index) == '{') depth++;
            else if (source.charAt(index) == '}') depth = Math.max(0, depth - 1);
        }
        depths[source.length()] = depth;
        return depths;
    }

    private static int matchingClose(String source, int open) {
        int depth = 0;
        for (int index = open; index < source.length(); index++) {
            if (source.charAt(index) == '{') depth++;
            else if (source.charAt(index) == '}' && --depth == 0) return index;
        }
        return -1;
    }

    private static int declarationStart(String source, int keywordOffset) {
        int start = lineStart(source, keywordOffset);
        int cursor = start;
        while (cursor > 0) {
            int previousEnd = cursor;
            int previousStart = lineStart(source, Math.max(0, cursor - 2));
            String previous = source.substring(previousStart, previousEnd).trim();
            if (!previous.startsWith("@")) break;
            start = previousStart;
            cursor = previousStart;
        }
        return start;
    }

    private static SourceUnit unit(String source, int start, int end, int ordinal,
                                   String name, String confidence) {
        String identity = "java\nCLASS\n" + start + "\n" + end + "\n" + source.substring(start, end);
        return new SourceUnit(Hashes.sha256(identity.getBytes(StandardCharsets.UTF_8)),
                UnitKind.CLASS, name, null, start, end, UnitBoundaries.lineOf(source, start),
                UnitBoundaries.lineOf(source, Math.max(start, end - 1)), ordinal, confidence);
    }

    private static int lineStart(String source, int offset) {
        int cursor = Math.min(offset, source.length());
        while (cursor > 0 && source.charAt(cursor - 1) != '\n' && source.charAt(cursor - 1) != '\r') cursor--;
        return cursor;
    }

    private record Start(int startOffset, int openBrace, String name) { }
}
