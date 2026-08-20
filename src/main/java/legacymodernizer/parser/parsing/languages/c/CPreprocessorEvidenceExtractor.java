package legacymodernizer.parser.parsing.languages.c;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import legacymodernizer.parser.antlr.c.preprocessor.CPreprocessorLexer;
import legacymodernizer.parser.antlr.c.preprocessor.CPreprocessorParser;
import legacymodernizer.parser.antlr.c.preprocessor.CPreprocessorParserBaseListener;
import legacymodernizer.parser.parsing.evidence.MacroEvidenceCandidate;
import legacymodernizer.parser.parsing.evidence.MacroEvidenceExtraction;
import legacymodernizer.parser.parsing.evidence.SourceRangeCandidate;

/** Dedicated preprocessing grammar consumer; never searches directive text. */
public final class CPreprocessorEvidenceExtractor {

    private static final String MALFORMED_REASON =
            "insufficient_preprocessor_directive_syntax";
    private static final String NONCONTIGUOUS_REASON =
            "insufficient_noncontiguous_preprocessing_token";
    private static final String UNTERMINATED_COMMENT_REASON =
            "insufficient_unterminated_preprocessor_comment";

    private CPreprocessorEvidenceExtractor() {
    }

    public static MacroEvidenceExtraction extract(String source) {
        SplicedSource spliced = SplicedSource.from(source == null ? "" : source);
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
        return listener.result();
    }

    private static final class EvidenceListener extends CPreprocessorParserBaseListener {
        private final CommonTokenStream tokens;
        private final SplicedSource spliced;
        private final int unterminatedCommentStartTokenIndex;
        private final List<MacroEvidenceCandidate> candidates = new ArrayList<>();
        private final Set<String> reasons = new LinkedHashSet<>();
        private int unresolved;

        private EvidenceListener(CommonTokenStream tokens, SplicedSource spliced,
                                 boolean hasUnterminatedBlockComment) {
            this.tokens = tokens;
            this.spliced = spliced;
            this.unterminatedCommentStartTokenIndex = unterminatedCommentStart(
                    tokens, hasUnterminatedBlockComment);
        }

        @Override
        public void enterFunctionDefine(CPreprocessorParser.FunctionDefineContext context) {
            if (containsUnterminatedComment(context)) {
                unresolved++;
                reasons.add(UNTERMINATED_COMMENT_REASON);
                return;
            }
            try {
                List<SourceRangeCandidate> parameters = context.parameterList() == null
                        ? List.of()
                        : context.parameterList().macroParameter().stream()
                                .map(parameter -> spliced.semanticRange(
                                        parameter.getStart(), parameter.getStop()))
                                .toList();
                addCandidate(context, "function", parameters,
                        context.parameterList() != null
                                && context.parameterList().ELLIPSIS() != null,
                        context.RPAREN().getSymbol());
            } catch (IllegalArgumentException noncontiguous) {
                unresolved++;
                reasons.add(NONCONTIGUOUS_REASON);
            }
        }

        @Override
        public void enterObjectDefine(CPreprocessorParser.ObjectDefineContext context) {
            if (containsUnterminatedComment(context)) {
                unresolved++;
                reasons.add(UNTERMINATED_COMMENT_REASON);
                return;
            }
            try {
                addCandidate(context, "object", List.of(), false,
                        context.macroName().getStop());
            } catch (IllegalArgumentException noncontiguous) {
                unresolved++;
                reasons.add(NONCONTIGUOUS_REASON);
            }
        }

        @Override
        public void enterMalformedDefineLine(
                CPreprocessorParser.MalformedDefineLineContext context) {
            unresolved++;
            reasons.add(MALFORMED_REASON);
        }

        private void addCandidate(CPreprocessorParser.DefineDirectiveContext context,
                                  String kind,
                                  List<SourceRangeCandidate> parameters,
                                  boolean variadic,
                                  Token replacementBoundary) {
            CPreprocessorParser.DefineLineContext line =
                    (CPreprocessorParser.DefineLineContext) context.getParent();
            Token name = context instanceof CPreprocessorParser.FunctionDefineContext function
                    ? function.macroName().IDENTIFIER().getSymbol()
                    : ((CPreprocessorParser.ObjectDefineContext) context)
                            .macroName().IDENTIFIER().getSymbol();
            SourceRangeCandidate replacement = replacementRange(
                    replacementBoundary.getTokenIndex() + 1,
                    context.getStop().getTokenIndex());
            candidates.add(new MacroEvidenceCandidate(
                    "function".equals(kind) ? "functionDefine" : "objectDefine",
                    spliced.outerRange(line.HASH().getSymbol(), context.getStop()),
                    spliced.semanticRange(name, name),
                    kind,
                    name.getText(),
                    parameters,
                    variadic,
                    replacement));
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

        private static boolean isHorizontal(int tokenType) {
            return tokenType == CPreprocessorLexer.WS
                    || tokenType == CPreprocessorLexer.LINE_COMMENT
                    || tokenType == CPreprocessorLexer.BLOCK_COMMENT_START
                    || tokenType == CPreprocessorLexer.BLOCK_COMMENT_END;
        }

        private boolean containsUnterminatedComment(
                CPreprocessorParser.DefineDirectiveContext context) {
            return unterminatedCommentStartTokenIndex >= context.getStart().getTokenIndex()
                    && unterminatedCommentStartTokenIndex <= context.getStop().getTokenIndex();
        }

        private static int unterminatedCommentStart(CommonTokenStream tokens,
                                                    boolean unterminated) {
            if (!unterminated) return -1;
            for (int index = tokens.size() - 1; index >= 0; index--) {
                if (tokens.get(index).getType() == CPreprocessorLexer.BLOCK_COMMENT_START) {
                    return index;
                }
            }
            throw new IllegalStateException(
                    "unterminated preprocessor comment has no opening token");
        }

        private MacroEvidenceExtraction result() {
            return new MacroEvidenceExtraction(candidates, unresolved, List.copyOf(reasons));
        }
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
