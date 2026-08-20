package legacymodernizer.parser.parsing.languages.c;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import legacymodernizer.parser.antlr.c.CLexer;
import legacymodernizer.parser.parsing.evidence.ConditionalCompilationEvidence;
import legacymodernizer.parser.parsing.evidence.ConditionalCompilationEvidence.ConditionalRegionCandidate;
import legacymodernizer.parser.parsing.evidence.ConditionalCompilationEvidence.Presence;
import legacymodernizer.parser.parsing.evidence.ConditionalCompilationEvidence.PresenceSpan;
import legacymodernizer.parser.parsing.evidence.SourceRangeCandidate;

/** Builds a presence map from grammar lexer Directive tokens without selecting a fake build. */
public final class CConditionalCompilationAnalyzer {

    private CConditionalCompilationAnalyzer() {
    }

    public static ConditionalCompilationEvidence analyze(CommonTokenStream tokens) {
        tokens.fill();
        int sourceLength = tokens.getTokenSource().getInputStream().size();
        List<PresenceSpan> spans = new ArrayList<>();
        List<ConditionalRegionCandidate> regions = new ArrayList<>();
        ArrayDeque<Frame> stack = new ArrayDeque<>();
        int cursor = 0;

        for (Token token : tokens.getTokens()) {
            if (token.getType() != CLexer.Directive && token.getType() != CLexer.MultiLineMacro) continue;
            int start = token.getStartIndex();
            int end = token.getStopIndex() + 1;
            addSpan(spans, cursor, start, currentPresence(stack));

            Directive directive = Directive.parse(token.getText());
            switch (directive.kind()) {
                case IF, IFDEF, IFNDEF -> {
                    Reachability parent = currentReachability(stack);
                    Evaluation evaluation = directive.kind() == Kind.IF
                            ? ConstantExpressionEvaluator.evaluate(directive.operand())
                            : Evaluation.unknown();
                    Reachability branch = fromEvaluation(evaluation);
                    Frame frame = new Frame(start, directive.canonicalText(), parent,
                            combine(parent, branch), evaluation.mayBeTrue(), evaluation.isDefinitelyTrue(),
                            evaluation.isUnknown());
                    stack.push(frame);
                }
                case ELIF -> {
                    if (!stack.isEmpty()) stack.peek().enterElif(directive);
                    else regions.add(malformedDirective(start, end, directive));
                }
                case ELSE -> {
                    if (!stack.isEmpty()) stack.peek().enterElse();
                    else regions.add(malformedDirective(start, end, directive));
                }
                case ENDIF -> {
                    if (!stack.isEmpty()) {
                        Frame completed = stack.pop();
                        Presence regionPresence = effectiveRegionPresence(completed);
                        regions.add(new ConditionalRegionCandidate(
                                new SourceRangeCandidate(completed.groupStart, end),
                                completed.firstCondition, regionPresence));
                    } else regions.add(malformedDirective(start, end, directive));
                }
                case OTHER -> {
                    // Non-conditional directives do not change branch presence.
                }
            }
            cursor = end;
        }
        addSpan(spans, cursor, sourceLength, currentPresence(stack));
        while (!stack.isEmpty()) {
            Frame unterminated = stack.removeLast();
            regions.add(new ConditionalRegionCandidate(
                    new SourceRangeCandidate(unterminated.groupStart, sourceLength),
                    unterminated.firstCondition,
                    new Presence("unknown", unterminated.firstCondition,
                            "malformed_directive_structure")));
        }
        return new ConditionalCompilationEvidence(spans, regions);
    }

    private static Presence effectiveRegionPresence(Frame completed) {
        if (completed.parent == Reachability.INACTIVE) {
            return new Presence("inactive", completed.firstCondition,
                    "enclosing_inactive");
        }
        if (completed.parent == Reachability.CONDITIONAL) {
            return new Presence("conditional", completed.firstCondition,
                    "inherited_missing_build_configuration");
        }
        if (completed.unknownEncountered) {
            return new Presence("conditional", completed.firstCondition,
                    "missing_build_configuration");
        }
        return new Presence("active", completed.firstCondition, "constant_evaluated");
    }

    private static ConditionalRegionCandidate malformedDirective(
            int start, int end, Directive directive) {
        return new ConditionalRegionCandidate(new SourceRangeCandidate(start, end),
                directive.canonicalText(), new Presence("unknown", directive.canonicalText(),
                        "malformed_directive_structure"));
    }

    private static void addSpan(List<PresenceSpan> spans, int start, int end, Presence presence) {
        if (end > start) spans.add(new PresenceSpan(new SourceRangeCandidate(start, end), presence));
    }

    private static Reachability currentReachability(ArrayDeque<Frame> stack) {
        return stack.isEmpty() ? Reachability.ACTIVE : stack.peek().current;
    }

    private static Presence currentPresence(ArrayDeque<Frame> stack) {
        if (stack.isEmpty()) return Presence.active();
        Frame frame = stack.peek();
        return switch (frame.current) {
            case ACTIVE -> new Presence("active", frame.currentCondition, "constant_evaluated");
            case INACTIVE -> new Presence("inactive", frame.currentCondition, "constant_evaluated");
            case CONDITIONAL -> new Presence("conditional", frame.currentCondition,
                    "missing_build_configuration");
        };
    }

    private static Reachability fromEvaluation(Evaluation evaluation) {
        if (evaluation.isDefinitelyTrue()) return Reachability.ACTIVE;
        if (!evaluation.mayBeTrue()) return Reachability.INACTIVE;
        return Reachability.CONDITIONAL;
    }

    private static Reachability combine(Reachability parent, Reachability branch) {
        if (parent == Reachability.INACTIVE || branch == Reachability.INACTIVE) {
            return Reachability.INACTIVE;
        }
        if (parent == Reachability.ACTIVE && branch == Reachability.ACTIVE) {
            return Reachability.ACTIVE;
        }
        return Reachability.CONDITIONAL;
    }

    private enum Reachability { ACTIVE, INACTIVE, CONDITIONAL }

    private enum Kind { IF, IFDEF, IFNDEF, ELIF, ELSE, ENDIF, OTHER }

    private record Directive(Kind kind, String operand, String canonicalText) {
        private static Directive parse(String source) {
            int cursor = 0;
            while (cursor < source.length() && Character.isWhitespace(source.charAt(cursor))) cursor++;
            if (cursor >= source.length() || source.charAt(cursor) != '#') {
                return new Directive(Kind.OTHER, "", source.trim());
            }
            cursor++;
            while (cursor < source.length() && Character.isWhitespace(source.charAt(cursor))) cursor++;
            int wordStart = cursor;
            while (cursor < source.length()
                    && isAsciiIdentifierPart(source.charAt(cursor))) cursor++;
            String word = source.substring(wordStart, cursor);
            while (cursor < source.length() && Character.isWhitespace(source.charAt(cursor))) cursor++;
            String operand = source.substring(cursor).trim();
            Kind kind = switch (word) {
                case "if" -> Kind.IF;
                case "ifdef" -> Kind.IFDEF;
                case "ifndef" -> Kind.IFNDEF;
                case "elif" -> Kind.ELIF;
                case "else" -> Kind.ELSE;
                case "endif" -> Kind.ENDIF;
                default -> Kind.OTHER;
            };
            return new Directive(kind, operand, source.trim());
        }
    }

    private static final class Frame {
        private final int groupStart;
        private final String firstCondition;
        private final Reachability parent;
        private boolean priorMayMatch;
        private boolean priorDefinitelyMatched;
        private boolean unknownEncountered;
        private Reachability current;
        private String currentCondition;

        private Frame(int groupStart, String firstCondition, Reachability parent,
                      Reachability current, boolean priorMayMatch,
                      boolean priorDefinitelyMatched, boolean unknownEncountered) {
            this.groupStart = groupStart;
            this.firstCondition = firstCondition;
            this.parent = parent;
            this.current = current;
            this.priorMayMatch = priorMayMatch;
            this.priorDefinitelyMatched = priorDefinitelyMatched;
            this.unknownEncountered = unknownEncountered;
            this.currentCondition = firstCondition;
        }

        private void enterElif(Directive directive) {
            Evaluation evaluation = ConstantExpressionEvaluator.evaluate(directive.operand());
            Reachability local;
            if (priorDefinitelyMatched || !evaluation.mayBeTrue()) local = Reachability.INACTIVE;
            else if (priorMayMatch || evaluation.isUnknown()) local = Reachability.CONDITIONAL;
            else local = Reachability.ACTIVE;
            current = combine(parent, local);
            priorDefinitelyMatched = priorDefinitelyMatched
                    || (!priorMayMatch && evaluation.isDefinitelyTrue());
            priorMayMatch = priorMayMatch || evaluation.mayBeTrue();
            unknownEncountered |= evaluation.isUnknown();
            currentCondition = directive.canonicalText();
        }

        private void enterElse() {
            Reachability local = priorDefinitelyMatched ? Reachability.INACTIVE
                    : priorMayMatch ? Reachability.CONDITIONAL : Reachability.ACTIVE;
            current = combine(parent, local);
            currentCondition = "#else of (" + firstCondition + ")";
        }
    }

    private record Evaluation(boolean known, long value) {
        private static Evaluation known(long value) { return new Evaluation(true, value); }
        private static Evaluation unknown() { return new Evaluation(false, 0); }
        private boolean isDefinitelyTrue() { return known && value != 0; }
        private boolean mayBeTrue() { return !known || value != 0; }
        private boolean isUnknown() { return !known; }
    }

    /** Partial evaluator: unsupported/macro-dependent expressions remain unknown, never guessed. */
    private static final class ConstantExpressionEvaluator {
        private final Lexer lexer;
        private Lexeme current;

        private ConstantExpressionEvaluator(String source) {
            lexer = new Lexer(source);
            current = lexer.next();
        }

        private static Evaluation evaluate(String source) {
            try {
                ConstantExpressionEvaluator parser = new ConstantExpressionEvaluator(source);
                Evaluation value = parser.logicalOr();
                return parser.current.kind == LexemeKind.END ? value : Evaluation.unknown();
            } catch (RuntimeException unsupported) {
                return Evaluation.unknown();
            }
        }

        private Evaluation logicalOr() {
            Evaluation left = logicalAnd();
            while (accept("||")) {
                Evaluation right = logicalAnd();
                if (left.known && left.value != 0 || right.known && right.value != 0) left = Evaluation.known(1);
                else if (left.known && right.known) left = Evaluation.known(0);
                else left = Evaluation.unknown();
            }
            return left;
        }

        private Evaluation logicalAnd() {
            Evaluation left = bitwiseOr();
            while (accept("&&")) {
                Evaluation right = bitwiseOr();
                if (left.known && left.value == 0 || right.known && right.value == 0) left = Evaluation.known(0);
                else if (left.known && right.known) left = Evaluation.known(1);
                else left = Evaluation.unknown();
            }
            return left;
        }

        private Evaluation bitwiseOr() { return binary(this::bitwiseXor, "|"); }
        private Evaluation bitwiseXor() { return binary(this::bitwiseAnd, "^"); }
        private Evaluation bitwiseAnd() { return binary(this::equality, "&"); }

        private Evaluation equality() {
            Evaluation left = relational();
            while (is("==") || is("!=")) {
                String operator = consume().text;
                Evaluation right = relational();
                left = both(left, right, (a, b) -> operator.equals("==") == (a == b) ? 1 : 0);
            }
            return left;
        }

        private Evaluation relational() {
            Evaluation left = shift();
            while (is("<") || is("<=") || is(">") || is(">=")) {
                String operator = consume().text;
                Evaluation right = shift();
                left = both(left, right, (a, b) -> switch (operator) {
                    case "<" -> a < b ? 1 : 0;
                    case "<=" -> a <= b ? 1 : 0;
                    case ">" -> a > b ? 1 : 0;
                    default -> a >= b ? 1 : 0;
                });
            }
            return left;
        }

        private Evaluation shift() {
            Evaluation left = additive();
            while (is("<<") || is(">>")) {
                String operator = consume().text;
                Evaluation right = additive();
                left = both(left, right, (a, b) -> operator.equals("<<") ? a << b : a >> b);
            }
            return left;
        }

        private Evaluation additive() {
            Evaluation left = multiplicative();
            while (is("+") || is("-")) {
                String operator = consume().text;
                Evaluation right = multiplicative();
                left = both(left, right, (a, b) -> operator.equals("+") ? a + b : a - b);
            }
            return left;
        }

        private Evaluation multiplicative() {
            Evaluation left = unary();
            while (is("*") || is("/") || is("%")) {
                String operator = consume().text;
                Evaluation right = unary();
                if (right.known && right.value == 0 && !operator.equals("*")) return Evaluation.unknown();
                left = both(left, right, (a, b) -> switch (operator) {
                    case "*" -> a * b;
                    case "/" -> a / b;
                    default -> a % b;
                });
            }
            return left;
        }

        private Evaluation unary() {
            if (accept("!")) {
                Evaluation value = unary();
                return value.known ? Evaluation.known(value.value == 0 ? 1 : 0) : value;
            }
            if (accept("~")) {
                Evaluation value = unary();
                return value.known ? Evaluation.known(~value.value) : value;
            }
            if (accept("+")) return unary();
            if (accept("-")) {
                Evaluation value = unary();
                return value.known ? Evaluation.known(-value.value) : value;
            }
            if (accept("(")) {
                Evaluation value = logicalOr();
                require(")");
                return value;
            }
            if (current.kind == LexemeKind.NUMBER) {
                String text = stripIntegerSuffix(consume().text);
                int radix = text.startsWith("0x") || text.startsWith("0X") ? 16
                        : text.length() > 1 && text.startsWith("0") ? 8 : 10;
                String digits = radix == 16 ? text.substring(2) : text;
                return Evaluation.known(Long.parseUnsignedLong(digits, radix));
            }
            if (current.kind == LexemeKind.IDENTIFIER) {
                consume();
                return Evaluation.unknown();
            }
            throw new IllegalArgumentException("unsupported preprocessor expression");
        }

        private Evaluation binary(java.util.function.Supplier<Evaluation> next, String operator) {
            Evaluation left = next.get();
            while (accept(operator)) {
                Evaluation right = next.get();
                left = both(left, right, (a, b) -> switch (operator) {
                    case "|" -> a | b;
                    case "^" -> a ^ b;
                    default -> a & b;
                });
            }
            return left;
        }

        private Evaluation both(Evaluation left, Evaluation right, LongBinary operation) {
            return left.known && right.known
                    ? Evaluation.known(operation.apply(left.value, right.value))
                    : Evaluation.unknown();
        }

        private boolean is(String text) { return text.equals(current.text); }
        private boolean accept(String text) { if (!is(text)) return false; consume(); return true; }
        private Lexeme consume() { Lexeme result = current; current = lexer.next(); return result; }
        private void require(String text) { if (!accept(text)) throw new IllegalArgumentException("expected " + text); }

        private static String stripIntegerSuffix(String source) {
            int end = source.length();
            while (end > 0) {
                char value = source.charAt(end - 1);
                if (value != 'u' && value != 'U' && value != 'l' && value != 'L') break;
                end--;
            }
            return source.substring(0, end);
        }
    }

    @FunctionalInterface
    private interface LongBinary { long apply(long left, long right); }

    private enum LexemeKind { NUMBER, IDENTIFIER, OPERATOR, END }
    private record Lexeme(LexemeKind kind, String text) { }

    private static final class Lexer {
        private final String source;
        private int cursor;

        private Lexer(String source) { this.source = source == null ? "" : source; }

        private Lexeme next() {
            skipTrivia();
            if (cursor >= source.length()) return new Lexeme(LexemeKind.END, "");
            int start = cursor;
            char first = source.charAt(cursor++);
            if (Character.isDigit(first)) {
                while (cursor < source.length() && Character.isLetterOrDigit(source.charAt(cursor))) cursor++;
                return new Lexeme(LexemeKind.NUMBER, source.substring(start, cursor));
            }
            if (isAsciiIdentifierStart(first)) {
                while (cursor < source.length()
                        && isAsciiIdentifierPart(source.charAt(cursor))) cursor++;
                return new Lexeme(LexemeKind.IDENTIFIER, source.substring(start, cursor));
            }
            if (cursor < source.length()) {
                String pair = source.substring(start, cursor + 1);
                if (List.of("&&", "||", "<<", ">>", "<=", ">=", "==", "!=").contains(pair)) {
                    cursor++;
                    return new Lexeme(LexemeKind.OPERATOR, pair);
                }
            }
            if ("()!~+-*/%<>&^|".indexOf(first) >= 0) {
                return new Lexeme(LexemeKind.OPERATOR, Character.toString(first));
            }
            throw new IllegalArgumentException("unsupported preprocessor token");
        }

        private void skipTrivia() {
            boolean advanced;
            do {
                advanced = false;
                while (cursor < source.length() && Character.isWhitespace(source.charAt(cursor))) {
                    cursor++;
                    advanced = true;
                }
                if (cursor + 1 < source.length() && source.charAt(cursor) == '/'
                        && source.charAt(cursor + 1) == '*') {
                    int end = source.indexOf("*/", cursor + 2);
                    if (end < 0) throw new IllegalArgumentException("unterminated directive comment");
                    cursor = end + 2;
                    advanced = true;
                } else if (cursor + 1 < source.length() && source.charAt(cursor) == '/'
                        && source.charAt(cursor + 1) == '/') {
                    cursor = source.length();
                    advanced = true;
                }
            } while (advanced);
        }
    }

    private static boolean isAsciiIdentifierStart(char value) {
        return value == '_' || value >= 'A' && value <= 'Z' || value >= 'a' && value <= 'z';
    }

    private static boolean isAsciiIdentifierPart(char value) {
        return isAsciiIdentifierStart(value) || value >= '0' && value <= '9';
    }
}
