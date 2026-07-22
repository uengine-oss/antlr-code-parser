package legacymodernizer.parser.parsing.languages.oracle;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import legacymodernizer.parser.recovery.boundaries.SourceUnit;
import legacymodernizer.parser.recovery.boundaries.UnitKind;
import legacymodernizer.parser.recovery.workingcopy.Hashes;

@Component
public final class OracleSourceUnitLocator {

    private static final Pattern DECLARATION = Pattern.compile(
            "(?im)^[\\t ]*CREATE[\\t ]+(?:OR[\\t ]+REPLACE[\\t ]+)?"
            + "(?:(?:EDITIONABLE|NONEDITIONABLE)[\\t ]+)?"
            + "(PACKAGE[\\t ]+BODY|PACKAGE|PROCEDURE|FUNCTION|TRIGGER)\\b");
    private static final Pattern TERMINATOR = Pattern.compile("(?m)^[\\t ]*/[\\t ]*(?:\\r?\\n|$)");
    private static final Pattern NAME = Pattern.compile(
            "(?is)\\b(?:PACKAGE\\s+BODY|PACKAGE|PROCEDURE|FUNCTION|TRIGGER)\\s+([^\\s(]+)");

    public List<SourceUnit> locate(String source) {
        String text = source == null ? "" : source;
        String sanitized = sanitizeForStructure(text);
        List<DeclarationStart> declarations = new ArrayList<>();
        Matcher declarationMatcher = DECLARATION.matcher(sanitized);
        while (declarationMatcher.find()) {
            String kindText = declarationMatcher.group(1).toUpperCase(Locale.ROOT)
                    .replaceAll("\\s+", " ");
            UnitKind kind = switch (kindText) {
                case "PROCEDURE" -> UnitKind.PROCEDURE;
                case "FUNCTION" -> UnitKind.FUNCTION;
                case "TRIGGER" -> UnitKind.TRIGGER;
                default -> UnitKind.PACKAGE;
            };
            int headerEnd = Math.min(text.length(), lineEnd(text, declarationMatcher.start()));
            Matcher nameMatcher = NAME.matcher(text.substring(declarationMatcher.start(), headerEnd));
            String name = nameMatcher.find() ? nameMatcher.group(1) : null;
            declarations.add(new DeclarationStart(declarationMatcher.start(), kind, name));
        }
        if (declarations.isEmpty()) {
            return List.of(fileUnit(text));
        }

        List<Integer> terminatorEnds = new ArrayList<>();
        Matcher terminatorMatcher = TERMINATOR.matcher(sanitized);
        while (terminatorMatcher.find()) terminatorEnds.add(terminatorMatcher.end());

        List<SourceUnit> units = new ArrayList<>();
        for (int index = 0; index < declarations.size(); index++) {
            DeclarationStart declaration = declarations.get(index);
            int nextStart = index + 1 < declarations.size()
                    ? declarations.get(index + 1).offset() : text.length();
            int end = nextStart;
            boolean exact = false;
            for (int terminatorEnd : terminatorEnds) {
                if (terminatorEnd > declaration.offset() && terminatorEnd <= nextStart) {
                    end = terminatorEnd;
                    exact = true;
                    break;
                }
            }
            while (end < text.length() && (text.charAt(end) == '\r' || text.charAt(end) == '\n')) end++;
            String identity = "oracle\n" + declaration.kind() + "\n" + declaration.offset()
                    + "\n" + end + "\n" + text.substring(declaration.offset(), end);
            units.add(new SourceUnit(
                    Hashes.sha256(identity.getBytes(StandardCharsets.UTF_8)),
                    declaration.kind(), declaration.name(), null,
                    declaration.offset(), end,
                    lineOf(text, declaration.offset()), lineOf(text, Math.max(declaration.offset(), end - 1)),
                    index, exact ? "EXACT" : "CONSERVATIVE"));
        }
        return List.copyOf(units);
    }

    private static SourceUnit fileUnit(String text) {
        String identity = "oracle\nFILE\n" + text;
        return new SourceUnit(Hashes.sha256(identity.getBytes(StandardCharsets.UTF_8)),
                UnitKind.FILE, null, null, 0, text.length(), text.isEmpty() ? 0 : 1,
                text.isEmpty() ? 0 : lineOf(text, Math.max(0, text.length() - 1)), 0, "CONSERVATIVE");
    }

    private static int lineEnd(String text, int offset) {
        int newline = text.indexOf('\n', offset);
        return newline < 0 ? text.length() : newline;
    }

    private static int lineOf(String text, int offset) {
        int line = 1;
        for (int index = 0; index < Math.min(offset, text.length()); index++) {
            if (text.charAt(index) == '\n') line++;
        }
        return line;
    }

    public static String sanitizeForStructure(String source) {
        char[] output = source.toCharArray();
        State state = State.NORMAL;
        char qClose = 0;
        for (int index = 0; index < source.length(); index++) {
            char current = source.charAt(index);
            char next = index + 1 < source.length() ? source.charAt(index + 1) : 0;
            if (current == '/' && isStandaloneSlash(source, index)
                    && shouldResynchronize(source, index, state, qClose)) {
                state = State.NORMAL;
            }
            if (state == State.LINE_COMMENT) {
                if (current == '\n') state = State.NORMAL;
                else output[index] = ' ';
                continue;
            }
            if (state == State.BLOCK_COMMENT) {
                if (current == '*' && next == '/') {
                    output[index] = output[index + 1] = ' ';
                    index++;
                    state = State.NORMAL;
                } else if (current != '\n' && current != '\r') output[index] = ' ';
                continue;
            }
            if (state == State.SINGLE_QUOTE) {
                output[index] = current == '\n' || current == '\r' ? current : ' ';
                if (current == '\'' && next == '\'') {
                    output[index + 1] = ' ';
                    index++;
                } else if (current == '\'') state = State.NORMAL;
                continue;
            }
            if (state == State.DOUBLE_QUOTE) {
                output[index] = current == '\n' || current == '\r' ? current : ' ';
                if (current == '"' && next == '"') {
                    output[index + 1] = ' ';
                    index++;
                } else if (current == '"') state = State.NORMAL;
                continue;
            }
            if (state == State.Q_QUOTE) {
                output[index] = current == '\n' || current == '\r' ? current : ' ';
                if (current == qClose && next == '\'') {
                    output[index + 1] = ' ';
                    index++;
                    state = State.NORMAL;
                }
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
            } else if ((current == 'q' || current == 'Q') && next == '\'' && index + 2 < source.length()) {
                char open = source.charAt(index + 2);
                qClose = switch (open) {
                    case '[' -> ']';
                    case '{' -> '}';
                    case '(' -> ')';
                    case '<' -> '>';
                    default -> open;
                };
                output[index] = output[index + 1] = output[index + 2] = ' ';
                index += 2;
                state = State.Q_QUOTE;
            } else if (current == '\'') {
                output[index] = ' ';
                state = State.SINGLE_QUOTE;
            } else if (current == '"') {
                output[index] = ' ';
                state = State.DOUBLE_QUOTE;
            }
        }
        return new String(output);
    }

    private static boolean isStandaloneSlash(String source, int offset) {
        int lineStart = offset;
        while (lineStart > 0 && source.charAt(lineStart - 1) != '\n'
                && source.charAt(lineStart - 1) != '\r') lineStart--;
        int lineEnd = offset + 1;
        while (lineEnd < source.length() && source.charAt(lineEnd) != '\n'
                && source.charAt(lineEnd) != '\r') lineEnd++;
        return source.substring(lineStart, lineEnd).trim().equals("/");
    }

    private static boolean shouldResynchronize(String source, int slashOffset,
                                               State state, char qClose) {
        if (state == State.NORMAL) return false;
        if (state == State.LINE_COMMENT || state == State.SINGLE_QUOTE
                || state == State.DOUBLE_QUOTE) return true;
        Matcher nextDeclaration = DECLARATION.matcher(source);
        if (!nextDeclaration.find(slashOffset + 1)) return false;
        int declarationOffset = nextDeclaration.start();
        int closeOffset = switch (state) {
            case BLOCK_COMMENT -> source.indexOf("*/", slashOffset + 1);
            case Q_QUOTE -> source.indexOf(String.valueOf(qClose) + '\'', slashOffset + 1);
            default -> -1;
        };
        return closeOffset < 0 || declarationOffset < closeOffset;
    }

    private enum State { NORMAL, SINGLE_QUOTE, DOUBLE_QUOTE, Q_QUOTE, LINE_COMMENT, BLOCK_COMMENT }
    private record DeclarationStart(int offset, UnitKind kind, String name) { }
}
