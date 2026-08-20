package legacymodernizer.parser.parsing.languages.c;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import legacymodernizer.parser.antlr.c.preprocessor.CPreprocessorLexer;
import legacymodernizer.parser.antlr.c.preprocessor.CPreprocessorParser;
import legacymodernizer.parser.antlr.c.preprocessor.CPreprocessorParserBaseListener;
import legacymodernizer.parser.parsing.evidence.ImportEvidenceCandidate;
import legacymodernizer.parser.parsing.evidence.ImportEvidenceExtraction;
import legacymodernizer.parser.parsing.evidence.MacroEvidenceCandidate;
import legacymodernizer.parser.parsing.evidence.MacroEvidenceExtraction;
import legacymodernizer.parser.parsing.evidence.SourceRangeCandidate;
import legacymodernizer.parser.parsing.languages.c.CConditionalCompilationAnalyzer.Directive;
import legacymodernizer.parser.parsing.languages.c.CConditionalCompilationAnalyzer.Kind;

/** Dedicated preprocessing grammar consumer; never searches directive source text. */
public final class CPreprocessorEvidenceExtractor {

    private static final String MALFORMED_REASON =
            "insufficient_preprocessor_directive_syntax";
    private static final String NONCONTIGUOUS_REASON =
            "insufficient_noncontiguous_preprocessing_token";
    private static final String UNTERMINATED_COMMENT_REASON =
            "insufficient_unterminated_preprocessor_comment";

    private CPreprocessorEvidenceExtractor() {
    }

    public static CPreprocessorSyntax extract(String source) {
        String physicalSource = source == null ? "" : source;
        SplicedSource spliced = SplicedSource.from(physicalSource);
        ErrorCounter lexerErrors = new ErrorCounter();
        CPreprocessorLexer lexer = new CPreprocessorLexer(CharStreams.fromString(spliced.text()));
        lexer.removeErrorListeners();
        lexer.addErrorListener(lexerErrors);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CPreprocessorParser parser = new CPreprocessorParser(tokens);
        ErrorCounter parserErrors = new ErrorCounter();
        parser.removeErrorListeners();
        parser.addErrorListener(parserErrors);
        CPreprocessorParser.PreprocessingFileContext tree = parser.preprocessingFile();
        if (lexerErrors.count > 0 || parserErrors.count > 0) {
            throw new IllegalStateException(
                    "C preprocessor grammar failed: lexer=" + lexerErrors.count
                            + ", parser=" + parserErrors.count);
        }
        tokens.fill();
        EvidenceListener listener = new EvidenceListener(
                tokens, spliced, lexer.hasUnterminatedBlockComment());
        new ParseTreeWalker().walk(listener, tree);
        return listener.result(physicalSource.codePointCount(0, physicalSource.length()));
    }

    private static final class EvidenceListener extends CPreprocessorParserBaseListener {
        private final CommonTokenStream tokens;
        private final SplicedSource spliced;
        private final int unterminatedCommentStartTokenIndex;
        private final List<MacroEvidenceCandidate> macros = new ArrayList<>();
        private final Set<String> macroReasons = new LinkedHashSet<>();
        private final List<ImportEvidenceCandidate> imports = new ArrayList<>();
        private final Set<String> importReasons = new LinkedHashSet<>();
        private final List<Directive> conditionals = new ArrayList<>();
        private final List<SourceRangeCandidate> directiveRanges = new ArrayList<>();
        private int unresolvedMacros;
        private int unresolvedImports;

        private EvidenceListener(CommonTokenStream tokens, SplicedSource spliced,
                                 boolean hasUnterminatedBlockComment) {
            this.tokens = tokens;
            this.spliced = spliced;
            this.unterminatedCommentStartTokenIndex = unterminatedCommentStart(
                    tokens, hasUnterminatedBlockComment);
        }

        @Override
        public void enterDefineLine(CPreprocessorParser.DefineLineContext context) {
            addDirective(context.HASH().getSymbol(), context.getStop());
        }

        @Override
        public void enterMalformedDefineLine(
                CPreprocessorParser.MalformedDefineLineContext context) {
            addDirective(context.HASH().getSymbol(), context.getStop());
            unresolvedMacros++;
            macroReasons.add(MALFORMED_REASON);
        }

        @Override
        public void enterIncludeLine(CPreprocessorParser.IncludeLineContext context) {
            addDirective(context.HASH().getSymbol(), context.getStop());
            try {
                CPreprocessorParser.IncludeTargetContext target = context.includeTarget();
                String targetKind = target instanceof CPreprocessorParser.QuotedIncludeTargetContext
                        ? "quoted"
                        : target instanceof CPreprocessorParser.AngleIncludeTargetContext
                                ? "angle" : "computed";
                imports.add(new ImportEvidenceCandidate(
                        "includeLine",
                        spliced.outerRange(context.HASH().getSymbol(), context.getStop()),
                        spliced.semanticRange(target.getStart(), target.getStop()),
                        targetKind));
            } catch (IllegalArgumentException noncontiguous) {
                unresolvedImports++;
                importReasons.add(NONCONTIGUOUS_REASON);
            }
        }

        @Override
        public void enterMalformedIncludeLine(
                CPreprocessorParser.MalformedIncludeLineContext context) {
            addDirective(context.HASH().getSymbol(), context.getStop());
            unresolvedImports++;
            importReasons.add(MALFORMED_REASON);
        }

        @Override
        public void enterConditionalLine(CPreprocessorParser.ConditionalLineContext context) {
            addDirective(context.HASH().getSymbol(), context.getStop());
        }

        @Override
        public void enterUndefLine(CPreprocessorParser.UndefLineContext context) {
            addDirective(context.HASH().getSymbol(), context.getStop());
        }

        @Override
        public void enterOtherDirectiveLine(
                CPreprocessorParser.OtherDirectiveLineContext context) {
            addDirective(context.HASH().getSymbol(), context.getStop());
        }

        @Override
        public void enterIfDirective(CPreprocessorParser.IfDirectiveContext context) {
            addConditional(Kind.IF, context, evaluate(context.conditionalExpression()));
        }

        @Override
        public void enterIfdefDirective(CPreprocessorParser.IfdefDirectiveContext context) {
            addConditional(Kind.IFDEF, context, null);
        }

        @Override
        public void enterIfndefDirective(CPreprocessorParser.IfndefDirectiveContext context) {
            addConditional(Kind.IFNDEF, context, null);
        }

        @Override
        public void enterElifDirective(CPreprocessorParser.ElifDirectiveContext context) {
            addConditional(Kind.ELIF, context, evaluate(context.conditionalExpression()));
        }

        @Override
        public void enterElseDirective(CPreprocessorParser.ElseDirectiveContext context) {
            addConditional(Kind.ELSE, context, null);
        }

        @Override
        public void enterEndifDirective(CPreprocessorParser.EndifDirectiveContext context) {
            addConditional(Kind.ENDIF, context, null);
        }

        @Override
        public void enterFunctionDefine(CPreprocessorParser.FunctionDefineContext context) {
            if (containsUnterminatedComment(context)) {
                unresolvedMacros++;
                macroReasons.add(UNTERMINATED_COMMENT_REASON);
                return;
            }
            try {
                List<SourceRangeCandidate> parameters = context.parameterList() == null
                        ? List.of()
                        : context.parameterList().macroParameter().stream()
                                .map(parameter -> spliced.semanticRange(
                                        parameter.getStart(), parameter.getStop()))
                                .toList();
                addMacro(context, "function", parameters,
                        context.parameterList() != null
                                && context.parameterList().ELLIPSIS() != null,
                        context.RPAREN().getSymbol());
            } catch (IllegalArgumentException noncontiguous) {
                unresolvedMacros++;
                macroReasons.add(NONCONTIGUOUS_REASON);
            }
        }

        @Override
        public void enterObjectDefine(CPreprocessorParser.ObjectDefineContext context) {
            if (containsUnterminatedComment(context)) {
                unresolvedMacros++;
                macroReasons.add(UNTERMINATED_COMMENT_REASON);
                return;
            }
            try {
                addMacro(context, "object", List.of(), false,
                        context.macroName().getStop());
            } catch (IllegalArgumentException noncontiguous) {
                unresolvedMacros++;
                macroReasons.add(NONCONTIGUOUS_REASON);
            }
        }

        private void addMacro(CPreprocessorParser.DefineDirectiveContext context,
                              String kind,
                              List<SourceRangeCandidate> parameters,
                              boolean variadic,
                              Token replacementBoundary) {
            CPreprocessorParser.DefineLineContext line =
                    (CPreprocessorParser.DefineLineContext) context.getParent();
            Token name = context instanceof CPreprocessorParser.FunctionDefineContext function
                    ? function.macroName().getStart()
                    : ((CPreprocessorParser.ObjectDefineContext) context).macroName().getStart();
            SourceRangeCandidate replacement = replacementRange(
                    replacementBoundary.getTokenIndex() + 1,
                    context.getStop().getTokenIndex());
            macros.add(new MacroEvidenceCandidate(
                    "function".equals(kind) ? "functionDefine" : "objectDefine",
                    spliced.outerRange(line.HASH().getSymbol(), context.getStop()),
                    spliced.semanticRange(name, name),
                    kind,
                    name.getText(),
                    parameters,
                    variadic,
                    replacement));
        }

        private void addConditional(Kind kind, ParserRuleContext context, Long value) {
            CPreprocessorParser.ConditionalLineContext line =
                    (CPreprocessorParser.ConditionalLineContext) context.getParent();
            SourceRangeCandidate range = spliced.outerRange(
                    line.HASH().getSymbol(), line.getStop());
            conditionals.add(new Directive(kind, range,
                    tokens.getText(line.getSourceInterval()).trim(), value));
        }

        private void addDirective(Token start, Token stop) {
            directiveRanges.add(spliced.outerRange(start, stop));
        }

        private SourceRangeCandidate replacementRange(int firstIndex, int lastIndex) {
            Token first = null;
            Token last = null;
            for (int index = firstIndex; index <= lastIndex; index++) {
                Token token = tokens.get(index);
                if (isHorizontal(token.getType())) continue;
                if (first == null) first = token;
                last = token;
            }
            return first == null ? null : spliced.outerRange(first, last);
        }

        private boolean containsUnterminatedComment(
                CPreprocessorParser.DefineDirectiveContext context) {
            return unterminatedCommentStartTokenIndex >= context.getStart().getTokenIndex()
                    && unterminatedCommentStartTokenIndex <= context.getStop().getTokenIndex();
        }

        private CPreprocessorSyntax result(int sourceLength) {
            return new CPreprocessorSyntax(
                    new MacroEvidenceExtraction(macros, unresolvedMacros,
                            List.copyOf(macroReasons)),
                    new ImportEvidenceExtraction(imports, unresolvedImports,
                            List.copyOf(importReasons)),
                    CConditionalCompilationAnalyzer.analyze(sourceLength, conditionals),
                    directiveRanges);
        }
    }

    private static Long evaluate(ParseTree tree) {
        if (tree == null) return null;
        if (tree instanceof CPreprocessorParser.ConditionalExpressionContext context) {
            Long condition = evaluate(context.logicalOrExpression());
            if (context.QUESTION() == null) return condition;
            if (condition == null) return null;
            return evaluate(context.conditionalExpression(condition == 0 ? 1 : 0));
        }
        if (tree instanceof CPreprocessorParser.LogicalOrExpressionContext
                || tree instanceof CPreprocessorParser.LogicalAndExpressionContext
                || tree instanceof CPreprocessorParser.InclusiveOrExpressionContext
                || tree instanceof CPreprocessorParser.ExclusiveOrExpressionContext
                || tree instanceof CPreprocessorParser.AndExpressionContext
                || tree instanceof CPreprocessorParser.EqualityExpressionContext
                || tree instanceof CPreprocessorParser.RelationalExpressionContext
                || tree instanceof CPreprocessorParser.ShiftExpressionContext
                || tree instanceof CPreprocessorParser.AdditiveExpressionContext
                || tree instanceof CPreprocessorParser.MultiplicativeExpressionContext) {
            return foldBinary((ParserRuleContext) tree);
        }
        if (tree instanceof CPreprocessorParser.UnaryExpressionContext context) {
            if (context.primaryExpression() != null) return evaluate(context.primaryExpression());
            Long value = evaluate(context.unaryExpression());
            if (value == null) return null;
            String operator = significant(context).get(0).getText();
            return switch (operator) {
                case "+" -> value;
                case "-" -> -value;
                case "!" -> value == 0 ? 1L : 0L;
                case "~" -> ~value;
                default -> null;
            };
        }
        if (tree instanceof CPreprocessorParser.PrimaryExpressionContext context) {
            if (context.PP_NUMBER() != null) return integer(context.PP_NUMBER().getText());
            if (context.CHARACTER_CONSTANT() != null) {
                return character(context.CHARACTER_CONSTANT().getText());
            }
            if (context.conditionalExpression() != null) {
                return evaluate(context.conditionalExpression());
            }
            return null;
        }
        return null;
    }

    private static Long foldBinary(ParserRuleContext context) {
        List<ParseTree> parts = significant(context);
        Long result = evaluate(parts.get(0));
        for (int index = 1; index + 1 < parts.size(); index += 2) {
            String operator = parts.get(index).getText();
            Long right = evaluate(parts.get(index + 1));
            result = binary(result, operator, right);
        }
        return result;
    }

    private static List<ParseTree> significant(ParserRuleContext context) {
        List<ParseTree> result = new ArrayList<>();
        for (int index = 0; index < context.getChildCount(); index++) {
            ParseTree child = context.getChild(index);
            if (!(child instanceof CPreprocessorParser.HorizontalContext)) result.add(child);
        }
        return result;
    }

    private static Long binary(Long left, String operator, Long right) {
        if ("||".equals(operator)) {
            if (left != null && left != 0 || right != null && right != 0) return 1L;
            return left != null && right != null ? 0L : null;
        }
        if ("&&".equals(operator)) {
            if (left != null && left == 0 || right != null && right == 0) return 0L;
            return left != null && right != null ? 1L : null;
        }
        if (left == null || right == null) return null;
        if (("/".equals(operator) || "%".equals(operator)) && right == 0) return null;
        return switch (operator) {
            case "|" -> left | right;
            case "^" -> left ^ right;
            case "&" -> left & right;
            case "==" -> left.longValue() == right.longValue() ? 1L : 0L;
            case "!=" -> left.longValue() != right.longValue() ? 1L : 0L;
            case "<" -> left < right ? 1L : 0L;
            case "<=" -> left <= right ? 1L : 0L;
            case ">" -> left > right ? 1L : 0L;
            case ">=" -> left >= right ? 1L : 0L;
            case "<<" -> left << right;
            case ">>" -> left >> right;
            case "+" -> left + right;
            case "-" -> left - right;
            case "*" -> left * right;
            case "/" -> left / right;
            case "%" -> left % right;
            default -> null;
        };
    }

    private static Long integer(String source) {
        try {
            int end = source.length();
            while (end > 0 && "uUlL".indexOf(source.charAt(end - 1)) >= 0) end--;
            String text = source.substring(0, end);
            int radix = text.startsWith("0x") || text.startsWith("0X") ? 16
                    : text.startsWith("0b") || text.startsWith("0B") ? 2
                    : text.length() > 1 && text.startsWith("0") ? 8 : 10;
            String digits = radix == 16 || radix == 2 ? text.substring(2) : text;
            return Long.parseLong(digits, radix);
        } catch (RuntimeException unsupported) {
            return null;
        }
    }

    private static Long character(String source) {
        int quote = source.indexOf('\'');
        if (quote < 0 || !source.endsWith("'") || source.length() <= quote + 1) return null;
        String body = source.substring(quote + 1, source.length() - 1);
        if (body.codePointCount(0, body.length()) == 1 && body.charAt(0) != '\\') {
            return (long) body.codePointAt(0);
        }
        return switch (body) {
            case "\\0" -> 0L;
            case "\\n" -> 10L;
            case "\\r" -> 13L;
            case "\\t" -> 9L;
            case "\\\\" -> 92L;
            case "\\'" -> 39L;
            default -> null;
        };
    }

    private static boolean isHorizontal(int tokenType) {
        return tokenType == CPreprocessorLexer.WS
                || tokenType == CPreprocessorLexer.LINE_COMMENT
                || tokenType == CPreprocessorLexer.BLOCK_COMMENT_START
                || tokenType == CPreprocessorLexer.BLOCK_COMMENT_END;
    }

    private static int unterminatedCommentStart(
            CommonTokenStream tokens, boolean unterminated) {
        if (!unterminated) return -1;
        for (int index = tokens.size() - 1; index >= 0; index--) {
            if (tokens.get(index).getType() == CPreprocessorLexer.BLOCK_COMMENT_START) {
                return index;
            }
        }
        throw new IllegalStateException("unterminated preprocessor comment has no opening token");
    }

    private static final class ErrorCounter extends BaseErrorListener {
        private int count;

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine, String message,
                                RecognitionException error) {
            count++;
        }
    }

    /** Translation phase 2 view with exact logical-code-point to physical mapping. */
    private record SplicedSource(String text, int[] logicalToPhysical) {

        private static SplicedSource from(String source) {
            int[] physical = source.codePoints().toArray();
            StringBuilder logical = new StringBuilder(source.length());
            List<Integer> mapping = new ArrayList<>();
            for (int index = 0; index < physical.length; index++) {
                if (physical[index] == '\\' && index + 1 < physical.length) {
                    if (physical[index + 1] == '\n') {
                        index += 1;
                        continue;
                    }
                    if (physical[index + 1] == '\r' && index + 2 < physical.length
                            && physical[index + 2] == '\n') {
                        index += 2;
                        continue;
                    }
                    if (physical[index + 1] == '\r') {
                        index += 1;
                        continue;
                    }
                }
                mapping.add(index);
                logical.appendCodePoint(physical[index]);
            }
            return new SplicedSource(logical.toString(), mapping.stream()
                    .mapToInt(Integer::intValue).toArray());
        }

        private SourceRangeCandidate semanticRange(Token start, Token stop) {
            int logicalStart = start.getStartIndex();
            int logicalStop = stop.getStopIndex();
            requireMapped(logicalStart, logicalStop);
            for (int index = logicalStart; index < logicalStop; index++) {
                if (logicalToPhysical[index + 1] != logicalToPhysical[index] + 1) {
                    throw new IllegalArgumentException(
                            "semantic preprocessing token is physically noncontiguous");
                }
            }
            return new SourceRangeCandidate(logicalToPhysical[logicalStart],
                    logicalToPhysical[logicalStop] + 1);
        }

        private SourceRangeCandidate outerRange(Token start, Token stop) {
            int logicalStart = start.getStartIndex();
            int logicalStop = stop.getStopIndex();
            requireMapped(logicalStart, logicalStop);
            return new SourceRangeCandidate(logicalToPhysical[logicalStart],
                    logicalToPhysical[logicalStop] + 1);
        }

        private void requireMapped(int start, int stop) {
            if (start < 0 || stop < start || stop >= logicalToPhysical.length) {
                throw new IllegalArgumentException(
                        "preprocessor token range is outside the mapped source");
            }
        }
    }
}
