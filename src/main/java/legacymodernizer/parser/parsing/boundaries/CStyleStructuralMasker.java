package legacymodernizer.parser.parsing.boundaries;

public final class CStyleStructuralMasker {

    private CStyleStructuralMasker() {
    }

    public static String mask(String source) {
        String text = source == null ? "" : source;
        char[] output = text.toCharArray();
        State state = State.NORMAL;
        boolean escaped = false;
        for (int index = 0; index < text.length(); index++) {
            char current = text.charAt(index);
            char next = index + 1 < text.length() ? text.charAt(index + 1) : 0;
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
            if (state == State.TEXT_BLOCK) {
                if (current == '"' && index + 2 < text.length()
                        && text.charAt(index + 1) == '"' && text.charAt(index + 2) == '"') {
                    output[index] = output[index + 1] = output[index + 2] = ' ';
                    index += 2;
                    state = State.NORMAL;
                } else if (!newline) output[index] = ' ';
                continue;
            }
            if (state == State.SINGLE_QUOTE || state == State.DOUBLE_QUOTE) {
                if (!newline) output[index] = ' ';
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if ((state == State.SINGLE_QUOTE && current == '\'')
                        || (state == State.DOUBLE_QUOTE && current == '"')) {
                    state = State.NORMAL;
                }
                continue;
            }

            if (current == '/' && next == '/') {
                output[index] = output[index + 1] = ' ';
                index++;
                state = State.LINE_COMMENT;
            } else if (current == '/' && next == '*') {
                output[index] = output[index + 1] = ' ';
                index++;
                state = State.BLOCK_COMMENT;
            } else if (current == '"' && index + 2 < text.length()
                    && text.charAt(index + 1) == '"' && text.charAt(index + 2) == '"') {
                output[index] = output[index + 1] = output[index + 2] = ' ';
                index += 2;
                state = State.TEXT_BLOCK;
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

    private enum State { NORMAL, LINE_COMMENT, BLOCK_COMMENT, SINGLE_QUOTE, DOUBLE_QUOTE, TEXT_BLOCK }
}
