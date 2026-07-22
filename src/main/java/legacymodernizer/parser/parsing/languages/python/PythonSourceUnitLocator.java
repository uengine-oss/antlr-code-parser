package legacymodernizer.parser.parsing.languages.python;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import legacymodernizer.parser.recovery.boundaries.SourceUnit;
import legacymodernizer.parser.recovery.boundaries.UnitKind;
import legacymodernizer.parser.recovery.workingcopy.Hashes;

public final class PythonSourceUnitLocator {

    private static final Pattern DECLARATION = Pattern.compile(
            "^(?:async[\\t ]+)?(def|class)[\\t ]+([A-Za-z_][A-Za-z0-9_]*)\\b");

    public List<SourceUnit> locate(String source) {
        String text = source == null ? "" : source;
        String masked = maskStringsAndComments(text);
        List<Line> lines = lines(masked);
        List<SourceUnit> units = new ArrayList<>();
        int ordinal = 0;
        for (int index = 0; index < lines.size(); index++) {
            Line line = lines.get(index);
            String content = masked.substring(line.start(), line.contentEnd());
            if (indent(content) != 0) continue;
            Matcher matcher = DECLARATION.matcher(content);
            if (!matcher.find()) continue;

            int startLineIndex = decoratorStart(lines, masked, index);
            int endLineIndex = declarationEnd(lines, masked, index);
            int start = lines.get(startLineIndex).start();
            int end = endLineIndex < lines.size() ? lines.get(endLineIndex).start() : text.length();
            while (end > start && (text.charAt(end - 1) == '\r' || text.charAt(end - 1) == '\n')) end--;
            UnitKind kind = matcher.group(1).equals("class") ? UnitKind.CLASS : UnitKind.FUNCTION;
            String identity = "python\n" + kind + "\n" + start + "\n" + end + "\n"
                    + text.substring(start, end);
            units.add(new SourceUnit(Hashes.sha256(identity.getBytes(StandardCharsets.UTF_8)),
                    kind, matcher.group(2), null, start, end, startLineIndex + 1,
                    Math.max(startLineIndex + 1, endLineIndex), ordinal++, "EXACT"));
            index = Math.max(index, endLineIndex - 1);
        }
        return units.isEmpty() ? List.of(fileUnit(text)) : List.copyOf(units);
    }

    private static int declarationEnd(List<Line> lines, String source, int declarationLine) {
        int headerDepth = 0;
        boolean suiteStarted = false;
        for (int index = declarationLine; index < lines.size(); index++) {
            String content = source.substring(lines.get(index).start(), lines.get(index).contentEnd());
            if (!suiteStarted) {
                headerDepth += delimiterDelta(content);
                if (headerDepth <= 0 && hasSuiteColon(content)) suiteStarted = true;
                continue;
            }
            String trimmed = content.trim();
            if (trimmed.isEmpty()) continue;
            if (indent(content) == 0) return index;
        }
        return lines.size();
    }

    private static int decoratorStart(List<Line> lines, String source, int declarationLine) {
        int start = declarationLine;
        for (int index = declarationLine - 1; index >= 0; index--) {
            String content = source.substring(lines.get(index).start(), lines.get(index).contentEnd());
            String trimmed = content.trim();
            if (trimmed.isEmpty()) continue;
            if (indent(content) == 0 && trimmed.startsWith("@")) start = index;
            else break;
        }
        return start;
    }

    private static int delimiterDelta(String line) {
        int delta = 0;
        for (int index = 0; index < line.length(); index++) {
            char current = line.charAt(index);
            if (current == '(' || current == '[' || current == '{') delta++;
            else if (current == ')' || current == ']' || current == '}') delta--;
        }
        return delta;
    }

    private static boolean hasSuiteColon(String line) {
        int depth = 0;
        for (int index = 0; index < line.length(); index++) {
            char current = line.charAt(index);
            if (current == '(' || current == '[' || current == '{') depth++;
            else if (current == ')' || current == ']' || current == '}') depth--;
            else if (current == ':' && depth == 0) return true;
        }
        return false;
    }

    private static int indent(String line) {
        int count = 0;
        while (count < line.length() && (line.charAt(count) == ' ' || line.charAt(count) == '\t')) count++;
        return count;
    }

    private static List<Line> lines(String source) {
        List<Line> lines = new ArrayList<>();
        int start = 0;
        for (int index = 0; index < source.length(); index++) {
            if (source.charAt(index) == '\n') {
                int contentEnd = index > start && source.charAt(index - 1) == '\r' ? index - 1 : index;
                lines.add(new Line(start, contentEnd));
                start = index + 1;
            }
        }
        lines.add(new Line(start, source.length()));
        return lines;
    }

    private static String maskStringsAndComments(String source) {
        char[] output = source.toCharArray();
        Quote quote = Quote.NONE;
        boolean escaped = false;
        for (int index = 0; index < source.length(); index++) {
            char current = source.charAt(index);
            boolean newline = current == '\n' || current == '\r';
            if (quote != Quote.NONE) {
                if (!newline) output[index] = ' ';
                if (quote.triple) {
                    if (current == quote.character && index + 2 < source.length()
                            && source.charAt(index + 1) == quote.character
                            && source.charAt(index + 2) == quote.character) {
                        output[index] = output[index + 1] = output[index + 2] = ' ';
                        index += 2;
                        quote = Quote.NONE;
                    }
                } else if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == quote.character) {
                    quote = Quote.NONE;
                }
                continue;
            }
            if (current == '#') {
                while (index < source.length() && source.charAt(index) != '\n') output[index++] = ' ';
                index--;
            } else if (current == '\'' || current == '"') {
                boolean triple = index + 2 < source.length()
                        && source.charAt(index + 1) == current && source.charAt(index + 2) == current;
                output[index] = ' ';
                if (triple) {
                    output[index + 1] = output[index + 2] = ' ';
                    index += 2;
                }
                quote = current == '\''
                        ? triple ? Quote.TRIPLE_SINGLE : Quote.SINGLE
                        : triple ? Quote.TRIPLE_DOUBLE : Quote.DOUBLE;
            }
        }
        return new String(output);
    }

    private static SourceUnit fileUnit(String source) {
        String identity = "python\nFILE\n" + source;
        return new SourceUnit(Hashes.sha256(identity.getBytes(StandardCharsets.UTF_8)),
                UnitKind.FILE, null, null, 0, source.length(), source.isEmpty() ? 0 : 1,
                source.isEmpty() ? 0 : lines(source).size(), 0, "CONSERVATIVE");
    }

    private enum Quote {
        NONE('\0', false), SINGLE('\'', false), DOUBLE('"', false),
        TRIPLE_SINGLE('\'', true), TRIPLE_DOUBLE('"', true);
        private final char character;
        private final boolean triple;
        Quote(char character, boolean triple) { this.character = character; this.triple = triple; }
    }

    private record Line(int start, int contentEnd) { }
}
