package legacymodernizer.parser.parsing.languages.c;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import legacymodernizer.parser.recovery.boundaries.SourceUnit;
import legacymodernizer.parser.recovery.boundaries.UnitBoundaries;
import legacymodernizer.parser.recovery.boundaries.UnitKind;
import legacymodernizer.parser.recovery.workingcopy.Hashes;
import legacymodernizer.parser.parsing.boundaries.CStyleStructuralMasker;

public final class CSourceUnitLocator {

    public List<SourceUnit> locate(String source) {
        String text = source == null ? "" : source;
        String masked = CStyleStructuralMasker.mask(text);
        List<SourceUnit> units = new ArrayList<>();
        int braceDepth = 0;
        int ordinal = 0;
        for (int index = 0; index < masked.length(); index++) {
            char current = masked.charAt(index);
            if (current == '{' && braceDepth == 0) {
                int closeParen = previousNonWhitespace(masked, index - 1);
                if (closeParen >= 0 && masked.charAt(closeParen) == ')') {
                    int openParen = matchingOpen(masked, closeParen, '(', ')');
                    int nameEnd = previousNonWhitespace(masked, openParen - 1);
                    int nameStart = identifierStart(masked, nameEnd);
                    if (openParen >= 0 && nameStart >= 0) {
                        String name = masked.substring(nameStart, nameEnd + 1);
                        int closeBrace = matchingClose(masked, index);
                        boolean exact = closeBrace >= 0;
                        if (!exact) closeBrace = masked.length() - 1;
                        int start = declarationStart(masked, nameStart);
                        int end = Math.min(text.length(), closeBrace + 1);
                        String identity = "c\nFUNCTION\n" + start + "\n" + end + "\n"
                                + text.substring(start, end);
                        units.add(new SourceUnit(Hashes.sha256(identity.getBytes(StandardCharsets.UTF_8)),
                                UnitKind.FUNCTION, name, null, start, end,
                                UnitBoundaries.lineOf(text, start),
                                UnitBoundaries.lineOf(text, Math.max(start, end - 1)), ordinal++,
                                exact ? "EXACT" : "CONSERVATIVE"));
                        index = closeBrace;
                        continue;
                    }
                }
                braceDepth++;
            } else if (current == '{') {
                braceDepth++;
            } else if (current == '}') {
                braceDepth = Math.max(0, braceDepth - 1);
            }
        }
        return units.isEmpty() ? List.of(UnitBoundaries.fileUnit("c", text)) : List.copyOf(units);
    }

    private static int declarationStart(String source, int nameStart) {
        int cursor = nameStart;
        while (cursor > 0) {
            char previous = source.charAt(cursor - 1);
            if (previous == ';' || previous == '}') break;
            cursor--;
        }
        while (cursor < nameStart && Character.isWhitespace(source.charAt(cursor))) cursor++;
        return cursor;
    }

    private static int matchingOpen(String source, int close, char openChar, char closeChar) {
        int depth = 0;
        for (int index = close; index >= 0; index--) {
            if (source.charAt(index) == closeChar) depth++;
            else if (source.charAt(index) == openChar && --depth == 0) return index;
        }
        return -1;
    }

    private static int matchingClose(String source, int open) {
        int depth = 0;
        for (int index = open; index < source.length(); index++) {
            if (source.charAt(index) == '{') depth++;
            else if (source.charAt(index) == '}' && --depth == 0) return index;
        }
        return -1;
    }

    private static int previousNonWhitespace(String source, int offset) {
        int cursor = offset;
        while (cursor >= 0 && Character.isWhitespace(source.charAt(cursor))) cursor--;
        return cursor;
    }

    private static int identifierStart(String source, int end) {
        if (end < 0 || !(Character.isLetterOrDigit(source.charAt(end)) || source.charAt(end) == '_')) return -1;
        int start = end;
        while (start > 0 && (Character.isLetterOrDigit(source.charAt(start - 1))
                || source.charAt(start - 1) == '_')) start--;
        return start;
    }

}
