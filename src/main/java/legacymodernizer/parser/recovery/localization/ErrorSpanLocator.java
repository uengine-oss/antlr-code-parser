package legacymodernizer.parser.recovery.localization;

/**
 * Deterministic, Parser-owned localization of a diagnostic into a bounded editable slice.
 *
 * ANTLR may report an error later than the true root cause, so slices grow through the
 * {@link SliceLevel} ladder instead of trusting the reported position alone. All scanning is
 * plain text with {@link SliceSyntax} lexical facts: statement terminators and bracket balance
 * are only honoured outside strings and comments, so a slice never starts or ends inside either.
 */
public final class ErrorSpanLocator {

    private static final byte CODE = 0;
    private static final byte STRING = 1;
    private static final byte COMMENT = 2;
    private static final int HEADER_MAX_CHARS = 200;

    /** Maps a diagnostic's file line/column onto a unit-relative character offset. */
    public int anchorOffset(String unitText, int unitStartLine, int diagnosticLine, int column) {
        int relativeLine = Math.max(0, diagnosticLine - unitStartLine);
        int offset = 0;
        for (int line = 0; line < relativeLine && offset < unitText.length(); line++) {
            int newline = unitText.indexOf('\n', offset);
            if (newline < 0) return unitText.length();
            offset = newline + 1;
        }
        return Math.min(unitText.length(), offset + Math.max(0, column));
    }

    public ContextSlice slice(String unitText, SliceSyntax syntax, int anchor, SliceLevel level) {
        int bounded = Math.max(0, Math.min(anchor, Math.max(0, unitText.length() - 1)));
        byte[] states = scan(unitText, syntax);
        int start;
        int end;
        switch (level) {
            case L0 -> {
                start = lineStart(unitText, bounded, 1);
                end = lineEnd(unitText, bounded, 1);
            }
            case L1 -> {
                int[] statement = statementSpan(unitText, syntax, states, bounded);
                start = statement[0];
                end = statement[1];
            }
            case L2 -> {
                int[] statement = statementSpan(unitText, syntax, states, bounded);
                start = previousStatementStart(unitText, syntax, states, statement[0]);
                end = statement[1];
            }
            case L3 -> {
                start = 0;
                end = unitText.length();
            }
            default -> throw new IllegalStateException("SLICE_LEVEL_UNKNOWN");
        }
        int[] fitted = fitBudget(unitText, bounded, start, end, level.maxChars());
        String header = fitted[0] > 0 && (level == SliceLevel.L2 || level == SliceLevel.L3)
                ? unitHeader(unitText) : "";
        return ContextSlice.of(level, unitText, fitted[0], fitted[1], header);
    }

    /** The unit's declaration line(s), read-only context for L2/L3 slices. */
    private static String unitHeader(String unitText) {
        int cursor = 0;
        while (cursor < unitText.length() && Character.isWhitespace(unitText.charAt(cursor))) {
            cursor++;
        }
        int lineEnd = unitText.indexOf('\n', cursor);
        if (lineEnd < 0) lineEnd = unitText.length();
        int end = Math.min(lineEnd, cursor + HEADER_MAX_CHARS);
        return unitText.substring(cursor, end);
    }

    private int[] statementSpan(String unitText, SliceSyntax syntax, byte[] states, int anchor) {
        if (syntax.statementTerminators().isEmpty()) {
            return new int[]{lineStart(unitText, anchor, 2), lineEnd(unitText, anchor, 2)};
        }
        int start = previousTerminator(unitText, syntax, states, anchor - 1) + 1;
        int end = nextTerminatorEnd(unitText, syntax, states, anchor);
        while (bracketBalance(unitText, states, start, end) > 0 && end < unitText.length()) {
            end = nextTerminatorEnd(unitText, syntax, states, end);
        }
        return new int[]{lineStart(unitText, start, 0), lineEnd(unitText, Math.max(start, end - 1), 0)};
    }

    private int previousStatementStart(String unitText, SliceSyntax syntax, byte[] states,
                                       int statementStart) {
        if (syntax.statementTerminators().isEmpty()) {
            return lineStart(unitText, statementStart, 4);
        }
        int previous = previousTerminator(unitText, syntax, states,
                previousTerminator(unitText, syntax, states, statementStart - 1) - 1) + 1;
        return lineStart(unitText, Math.max(0, previous), 0);
    }

    private static int previousTerminator(String unitText, SliceSyntax syntax, byte[] states,
                                          int from) {
        for (int index = Math.min(from, unitText.length() - 1); index >= 0; index--) {
            if (states[index] == CODE
                    && syntax.statementTerminators().contains(unitText.charAt(index))) {
                return index;
            }
        }
        return -1;
    }

    private static int nextTerminatorEnd(String unitText, SliceSyntax syntax, byte[] states,
                                         int from) {
        for (int index = Math.max(0, from); index < unitText.length(); index++) {
            if (states[index] == CODE
                    && syntax.statementTerminators().contains(unitText.charAt(index))) {
                return index + 1;
            }
        }
        return unitText.length();
    }

    private static int bracketBalance(String unitText, byte[] states, int start, int end) {
        int balance = 0;
        for (int index = start; index < Math.min(end, unitText.length()); index++) {
            if (states[index] != CODE) continue;
            char character = unitText.charAt(index);
            if (character == '(' || character == '[' || character == '{') balance++;
            if (character == ')' || character == ']' || character == '}') balance--;
        }
        return balance;
    }

    private static int[] fitBudget(String unitText, int anchor, int start, int end, int maxChars) {
        start = Math.max(0, Math.min(start, unitText.length()));
        end = Math.max(start, Math.min(end, unitText.length()));
        if (end - start <= maxChars) return new int[]{start, end};
        int half = maxChars / 2;
        int fittedStart = Math.max(start, anchor - half);
        int fittedEnd = Math.min(end, fittedStart + maxChars);
        fittedStart = Math.max(start, fittedEnd - maxChars);
        // Snap only inward (shrinking) so the budget is a hard ceiling.
        int snappedStart = unitText.indexOf('\n', fittedStart);
        if (snappedStart >= 0 && snappedStart + 1 <= Math.min(fittedEnd, fittedStart + 80)
                && snappedStart + 1 > fittedStart && fittedStart > start) {
            fittedStart = snappedStart + 1;
        }
        int snappedEnd = unitText.lastIndexOf('\n', Math.max(fittedStart, fittedEnd - 1));
        if (snappedEnd + 1 >= Math.max(fittedStart, fittedEnd - 80)
                && snappedEnd + 1 < fittedEnd && fittedEnd < end) {
            fittedEnd = snappedEnd + 1;
        }
        return new int[]{fittedStart, Math.max(fittedStart, fittedEnd)};
    }

    private static int lineStart(String unitText, int offset, int extraLinesUp) {
        int cursor = Math.max(0, Math.min(offset, unitText.length()));
        for (int line = 0; line <= extraLinesUp; line++) {
            int newline = unitText.lastIndexOf('\n', cursor - 1);
            if (newline < 0) return 0;
            cursor = newline;
        }
        return cursor + 1;
    }

    private static int lineEnd(String unitText, int offset, int extraLinesDown) {
        int cursor = Math.max(0, Math.min(offset, Math.max(0, unitText.length() - 1)));
        for (int line = 0; line <= extraLinesDown; line++) {
            int newline = unitText.indexOf('\n', cursor);
            if (newline < 0) return unitText.length();
            cursor = newline + 1;
        }
        return cursor;
    }

    /** Single-pass lexical scan marking every character as code, string, or comment. */
    static byte[] scan(String text, SliceSyntax syntax) {
        byte[] states = new byte[text.length()];
        int index = 0;
        while (index < text.length()) {
            String lineComment = matchPrefix(text, index, syntax.lineCommentPrefixes());
            if (lineComment != null) {
                index = markUntil(states, text, index, "\n", false);
                continue;
            }
            int blockComment = matchBlockOpener(text, index, syntax);
            if (blockComment >= 0) {
                index = markBlock(states, text, index,
                        syntax.blockCommentOpeners().get(blockComment),
                        syntax.blockCommentClosers().get(blockComment));
                continue;
            }
            char character = text.charAt(index);
            if (syntax.stringQuotes().contains(character)) {
                index = markString(states, text, index, character, syntax);
                continue;
            }
            states[index] = CODE;
            index++;
        }
        return states;
    }

    private static String matchPrefix(String text, int index, java.util.List<String> prefixes) {
        for (String prefix : prefixes) {
            if (text.startsWith(prefix, index)) return prefix;
        }
        return null;
    }

    private static int matchBlockOpener(String text, int index, SliceSyntax syntax) {
        for (int pair = 0; pair < syntax.blockCommentOpeners().size(); pair++) {
            if (text.startsWith(syntax.blockCommentOpeners().get(pair), index)) return pair;
        }
        return -1;
    }

    private static int markUntil(byte[] states, String text, int from, String closer,
                                 boolean includeCloser) {
        int end = text.indexOf(closer, from);
        int stop = end < 0 ? text.length() : includeCloser ? end + closer.length() : end;
        for (int index = from; index < stop; index++) states[index] = COMMENT;
        return stop;
    }

    private static int markBlock(byte[] states, String text, int from, String opener,
                                 String closer) {
        int end = text.indexOf(closer, from + opener.length());
        int stop = end < 0 ? text.length() : end + closer.length();
        for (int index = from; index < stop; index++) states[index] = COMMENT;
        return stop;
    }

    private static int markString(byte[] states, String text, int from, char quote,
                                  SliceSyntax syntax) {
        boolean triple = syntax.tripleQuotedStrings()
                && text.startsWith(String.valueOf(quote).repeat(3), from);
        int cursor = from + (triple ? 3 : 1);
        while (cursor < text.length()) {
            char character = text.charAt(cursor);
            if (syntax.backslashEscapesInStrings() && character == '\\') {
                cursor += 2;
                continue;
            }
            if (character == quote) {
                if (triple) {
                    if (text.startsWith(String.valueOf(quote).repeat(3), cursor)) {
                        cursor += 3;
                        break;
                    }
                } else if (syntax.doubledQuoteEscapesInStrings()
                        && cursor + 1 < text.length() && text.charAt(cursor + 1) == quote) {
                    cursor += 2;
                    continue;
                } else {
                    cursor++;
                    break;
                }
            }
            cursor++;
        }
        int stop = Math.min(cursor, text.length());
        for (int index = from; index < stop; index++) states[index] = STRING;
        return stop;
    }
}
