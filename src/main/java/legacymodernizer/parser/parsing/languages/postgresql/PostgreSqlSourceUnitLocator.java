package legacymodernizer.parser.parsing.languages.postgresql;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import legacymodernizer.parser.recovery.boundaries.SourceUnit;
import legacymodernizer.parser.recovery.boundaries.UnitKind;
import legacymodernizer.parser.recovery.workingcopy.Hashes;

public final class PostgreSqlSourceUnitLocator {

    private static final Pattern DECLARATION = Pattern.compile(
            "(?im)^[\\t ]*CREATE[\\t ]+(?:OR[\\t ]+REPLACE[\\t ]+)?"
            + "(FUNCTION|PROCEDURE|TRIGGER)\\b");
    private static final Pattern NAME = Pattern.compile(
            "(?is)\\b(?:FUNCTION|PROCEDURE|TRIGGER)\\s+([^\\s(]+)");

    public List<SourceUnit> locate(String source) {
        String text = source == null ? "" : source;
        String masked = mask(text);
        List<Start> starts = new ArrayList<>();
        Matcher matcher = DECLARATION.matcher(masked);
        while (matcher.find()) {
            UnitKind kind = UnitKind.valueOf(matcher.group(1).toUpperCase(Locale.ROOT));
            int headerEnd = masked.indexOf('\n', matcher.start());
            if (headerEnd < 0) headerEnd = masked.length();
            Matcher name = NAME.matcher(text.substring(matcher.start(), headerEnd));
            starts.add(new Start(matcher.start(), kind, name.find() ? name.group(1) : null));
        }
        if (starts.isEmpty()) return List.of(fileUnit(text));

        List<SourceUnit> units = new ArrayList<>();
        for (int index = 0; index < starts.size(); index++) {
            Start start = starts.get(index);
            int next = index + 1 < starts.size() ? starts.get(index + 1).offset() : text.length();
            int semicolon = masked.indexOf(';', start.offset());
            boolean exact = semicolon >= 0 && semicolon < next;
            int end = exact ? semicolon + 1 : next;
            while (end < text.length() && (text.charAt(end) == '\r' || text.charAt(end) == '\n')) end++;
            String identity = "postgresql\n" + start.kind() + "\n" + start.offset() + "\n" + end
                    + "\n" + text.substring(start.offset(), end);
            units.add(new SourceUnit(Hashes.sha256(identity.getBytes(StandardCharsets.UTF_8)),
                    start.kind(), start.name(), null, start.offset(), end,
                    lineOf(text, start.offset()), lineOf(text, Math.max(start.offset(), end - 1)),
                    index, exact ? "EXACT" : "CONSERVATIVE"));
        }
        return List.copyOf(units);
    }

    private static String mask(String source) {
        char[] output = source.toCharArray();
        State state = State.NORMAL;
        String dollarTag = null;
        for (int index = 0; index < source.length(); index++) {
            char current = source.charAt(index);
            char next = index + 1 < source.length() ? source.charAt(index + 1) : 0;
            boolean newline = current == '\n' || current == '\r';
            if (state == State.LINE_COMMENT) {
                if (current == '\n') state = State.NORMAL;
                else if (!newline) output[index] = ' ';
                continue;
            }
            if (state == State.BLOCK_COMMENT) {
                if (current == '*' && next == '/') {
                    output[index] = output[index + 1] = ' ';
                    index++;
                    state = State.NORMAL;
                } else if (!newline) output[index] = ' ';
                continue;
            }
            if (state == State.SINGLE_QUOTE || state == State.DOUBLE_QUOTE) {
                if (!newline) output[index] = ' ';
                char quote = state == State.SINGLE_QUOTE ? '\'' : '"';
                if (current == quote && next == quote) {
                    output[index + 1] = ' ';
                    index++;
                } else if (current == quote) state = State.NORMAL;
                continue;
            }
            if (state == State.DOLLAR_QUOTE) {
                if (source.startsWith(dollarTag, index)) {
                    for (int tagIndex = 0; tagIndex < dollarTag.length(); tagIndex++) output[index + tagIndex] = ' ';
                    index += dollarTag.length() - 1;
                    state = State.NORMAL;
                } else if (!newline) output[index] = ' ';
                continue;
            }

            if (current == '-' && next == '-') {
                output[index] = output[index + 1] = ' ';
                index++;
                state = State.LINE_COMMENT;
            } else if (current == '/' && next == '*') {
                output[index] = output[index + 1] = ' ';
                index++;
                state = State.BLOCK_COMMENT;
            } else if (current == '\'') {
                output[index] = ' ';
                state = State.SINGLE_QUOTE;
            } else if (current == '"') {
                output[index] = ' ';
                state = State.DOUBLE_QUOTE;
            } else if (current == '$') {
                int tagEnd = source.indexOf('$', index + 1);
                if (tagEnd > index && source.substring(index + 1, tagEnd).matches("[A-Za-z_][A-Za-z0-9_]*|")) {
                    dollarTag = source.substring(index, tagEnd + 1);
                    for (int tagIndex = index; tagIndex <= tagEnd; tagIndex++) output[tagIndex] = ' ';
                    index = tagEnd;
                    state = State.DOLLAR_QUOTE;
                }
            }
        }
        return new String(output);
    }

    private static SourceUnit fileUnit(String source) {
        String identity = "postgresql\nFILE\n" + source;
        return new SourceUnit(Hashes.sha256(identity.getBytes(StandardCharsets.UTF_8)), UnitKind.FILE,
                null, null, 0, source.length(), source.isEmpty() ? 0 : 1,
                source.isEmpty() ? 0 : lineOf(source, source.length() - 1), 0, "CONSERVATIVE");
    }

    private static int lineOf(String source, int offset) {
        int line = 1;
        for (int index = 0; index < Math.min(offset, source.length()); index++) if (source.charAt(index) == '\n') line++;
        return line;
    }

    private enum State { NORMAL, LINE_COMMENT, BLOCK_COMMENT, SINGLE_QUOTE, DOUBLE_QUOTE, DOLLAR_QUOTE }
    private record Start(int offset, UnitKind kind, String name) { }
}
