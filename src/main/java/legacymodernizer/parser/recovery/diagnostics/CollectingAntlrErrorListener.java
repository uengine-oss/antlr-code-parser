package legacymodernizer.parser.recovery.diagnostics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import legacymodernizer.parser.recovery.diagnostics.DiagnosticPhase;
import legacymodernizer.parser.recovery.diagnostics.ParseDiagnostic;

public final class CollectingAntlrErrorListener extends BaseErrorListener {

    private static final int WINDOW_RADIUS = 60;
    private static final int MAX_MESSAGE_LENGTH = 768;
    private static final int MAX_TOKEN_LENGTH = 256;
    private static final int MAX_EXPECTED_LENGTH = 512;
    private static final int MAX_RULE_STACK_DEPTH = 32;

    private final DiagnosticPhase phase;
    private final String source;
    private final List<ParseDiagnostic> diagnostics = new ArrayList<>();

    public CollectingAntlrErrorListener(DiagnosticPhase phase, String source) {
        this.phase = phase;
        this.source = source == null ? "" : source;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int line, int charPositionInLine, String msg,
                            RecognitionException exception) {
        String offendingToken = offendingSymbol instanceof Token token
                ? bounded(token.getText(), MAX_TOKEN_LENGTH) : null;
        String expectedTokens = null;
        List<String> ruleStack = List.of();
        if (recognizer instanceof Parser parser) {
            expectedTokens = bounded(parser.getExpectedTokens().toString(parser.getVocabulary()),
                    MAX_EXPECTED_LENGTH);
            List<String> stack = new ArrayList<>(parser.getRuleInvocationStack());
            Collections.reverse(stack);
            if (stack.size() > MAX_RULE_STACK_DEPTH) {
                stack = new ArrayList<>(stack.subList(stack.size() - MAX_RULE_STACK_DEPTH, stack.size()));
            }
            stack.replaceAll(rule -> bounded(rule, MAX_TOKEN_LENGTH));
            ruleStack = List.copyOf(stack);
        }
        diagnostics.add(new ParseDiagnostic(
                phase,
                "ERROR",
                phase == DiagnosticPhase.LEXER ? "ANTLR_LEXER_SYNTAX" : "ANTLR_PARSER_SYNTAX",
                bounded(msg, MAX_MESSAGE_LENGTH),
                line,
                charPositionInLine,
                offendingToken,
                expectedTokens,
                ruleStack,
                sourceWindow(line, charPositionInLine)));
    }

    public List<ParseDiagnostic> diagnostics() {
        return List.copyOf(diagnostics);
    }

    private String sourceWindow(int line, int column) {
        if (source.isEmpty()) {
            return "";
        }
        int offset = 0;
        int currentLine = 1;
        while (offset < source.length() && currentLine < Math.max(1, line)) {
            if (source.charAt(offset++) == '\n') {
                currentLine++;
            }
        }
        offset = Math.min(source.length(), offset + Math.max(0, column));
        int start = Math.max(0, offset - WINDOW_RADIUS);
        int end = Math.min(source.length(), offset + WINDOW_RADIUS);
        return source.substring(start, end);
    }

    private static String bounded(String text, int maximumLength) {
        if (text == null || text.length() <= maximumLength) return text;
        return text.substring(0, maximumLength - 1) + "…";
    }
}
