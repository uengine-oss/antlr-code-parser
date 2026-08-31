package legacymodernizer.parser.antlr.plpgsql;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import legacymodernizer.parser.parsing.evidence.CallArgumentEvidenceCandidate;
import legacymodernizer.parser.parsing.evidence.CallEvidenceCandidate;
import legacymodernizer.parser.parsing.evidence.ScopeEvidenceCandidate;
import legacymodernizer.parser.parsing.evidence.SourceRangeCandidate;

/**
 * Converts nested PL/pgSQL grammar contexts into source-file call ranges.
 * Binding and library classification remain Analyzer responsibilities.
 */
public final class PlpgsqlStructuralEvidenceListener extends PlpgsqlParserBaseListener {

    private final CommonTokenStream tokens;
    private final int nestedSourceOffset;
    private final int sourceLength;
    private final List<ScopeEvidenceCandidate> outerScopePath;
    private final List<CallEvidenceCandidate> callEvidence = new ArrayList<>();

    public PlpgsqlStructuralEvidenceListener(
            CommonTokenStream tokens,
            int nestedSourceOffset,
            int sourceLength,
            List<ScopeEvidenceCandidate> outerScopePath) {
        if (tokens == null || nestedSourceOffset < 0 || sourceLength <= 0
                || nestedSourceOffset > sourceLength
                || outerScopePath == null || outerScopePath.isEmpty()) {
            throw new IllegalArgumentException("nested PL/pgSQL evidence coordinates are required");
        }
        this.tokens = tokens;
        this.nestedSourceOffset = nestedSourceOffset;
        this.sourceLength = sourceLength;
        this.outerScopePath = List.copyOf(outerScopePath);
    }

    public List<CallEvidenceCandidate> callEvidenceCandidates() {
        return List.copyOf(callEvidence);
    }

    @Override
    public void enterFunctionCall(PlpgsqlParser.FunctionCallContext ctx) {
        List<PlpgsqlParser.CallableNamePartContext> path =
                ctx.callableName().callableNamePart();
        Token firstName = path.get(0).getStart();
        Token terminalName = path.get(path.size() - 1).getStart();
        SourceRangeCandidate calleeRange = sourceRange(firstName, terminalName);
        SourceRangeCandidate receiverRange = path.size() == 1 ? null
                : sourceRange(firstName, path.get(path.size() - 2).getStop());
        List<SourceRangeCandidate> argumentRanges = functionArguments(ctx).stream()
                .map(this::sourceRange)
                .toList();
        callEvidence.add(new CallEvidenceCandidate(
                "functionCall",
                sourceRange(ctx),
                calleeRange,
                receiverRange,
                "named",
                terminalName.getText(),
                argumentRanges.stream()
                        .map(CallArgumentEvidenceCandidate::expression).toList(),
                scopePath(ctx),
                path.stream().map(this::sourceRange).toList(),
                null));
    }

    @Override
    public void enterSqlGenericStmt(PlpgsqlParser.SqlGenericStmtContext ctx) {
        if (ctx.CALL() == null) return;
        List<Token> statementTokens = defaultChannelTokens(ctx);
        int openParenthesis = tokenIndex(statementTokens, PlpgsqlLexer.LPAREN, 1);
        int closeParenthesis = matchingCloseParenthesis(statementTokens, openParenthesis);
        if (openParenthesis <= 1 || closeParenthesis < 0) return;

        List<Token> nameComponents = qualifiedNameComponents(
                statementTokens.subList(1, openParenthesis));
        if (nameComponents.isEmpty()) return;

        Token firstName = nameComponents.get(0);
        Token terminalName = nameComponents.get(nameComponents.size() - 1);
        SourceRangeCandidate calleeRange = sourceRange(firstName, terminalName);
        SourceRangeCandidate receiverRange = nameComponents.size() == 1 ? null
                : sourceRange(firstName, nameComponents.get(nameComponents.size() - 2));
        callEvidence.add(new CallEvidenceCandidate(
                "sqlGenericStmt",
                sourceRange(firstName, statementTokens.get(closeParenthesis)),
                calleeRange,
                receiverRange,
                "named",
                terminalName.getText(),
                argumentRanges(statementTokens, openParenthesis, closeParenthesis).stream()
                        .map(CallArgumentEvidenceCandidate::expression).toList(),
                scopePath(ctx),
                nameComponents.stream().map(token -> sourceRange(token, token)).toList(),
                null));
    }

    private static List<ParserRuleContext> functionArguments(
            PlpgsqlParser.FunctionCallContext call) {
        PlpgsqlParser.FunctionCallArgsContext arguments = call.functionCallArgs();
        if (arguments == null) return List.of();
        if (arguments.expressionList() != null) {
            return new ArrayList<>(arguments.expressionList().expression());
        }
        return new ArrayList<>(arguments.expression());
    }

    private List<ScopeEvidenceCandidate> scopePath(ParserRuleContext context) {
        List<PlpgsqlParser.PlpgsqlBlockContext> blocks = new ArrayList<>();
        ParseTree cursor = context;
        while (cursor != null) {
            if (cursor instanceof PlpgsqlParser.PlpgsqlBlockContext block) {
                blocks.add(0, block);
            }
            cursor = cursor.getParent();
        }
        List<ScopeEvidenceCandidate> result = new ArrayList<>(outerScopePath);
        for (PlpgsqlParser.PlpgsqlBlockContext block : blocks) {
            SourceRangeCandidate range = sourceRange(block);
            if (result.stream().noneMatch(scope -> scope.range().equals(range))) {
                result.add(new ScopeEvidenceCandidate("block", range));
            }
        }
        return List.copyOf(result);
    }

    private List<Token> defaultChannelTokens(ParserRuleContext context) {
        List<Token> result = new ArrayList<>();
        int start = context.getStart().getTokenIndex();
        int stop = context.getStop().getTokenIndex();
        for (int index = start; index <= stop; index++) {
            Token token = tokens.get(index);
            if (token.getChannel() == Token.DEFAULT_CHANNEL) result.add(token);
        }
        return result;
    }

    private static int tokenIndex(List<Token> values, int tokenType, int startIndex) {
        for (int index = Math.max(0, startIndex); index < values.size(); index++) {
            if (values.get(index).getType() == tokenType) return index;
        }
        return -1;
    }

    private static int matchingCloseParenthesis(List<Token> values, int openIndex) {
        if (openIndex < 0) return -1;
        int depth = 0;
        for (int index = openIndex; index < values.size(); index++) {
            int type = values.get(index).getType();
            if (type == PlpgsqlLexer.LPAREN) depth++;
            if (type == PlpgsqlLexer.RPAREN && --depth == 0) return index;
        }
        return -1;
    }

    private static List<Token> qualifiedNameComponents(List<Token> nameTokens) {
        if (nameTokens.isEmpty()) return List.of();
        List<Token> result = new ArrayList<>();
        boolean expectName = true;
        for (Token token : nameTokens) {
            if (expectName) {
                if (token.getType() == PlpgsqlLexer.DOT) return List.of();
                result.add(token);
            } else if (token.getType() != PlpgsqlLexer.DOT) {
                return List.of();
            }
            expectName = !expectName;
        }
        return expectName ? List.of() : result;
    }

    private List<SourceRangeCandidate> argumentRanges(
            List<Token> values, int openIndex, int closeIndex) {
        List<SourceRangeCandidate> result = new ArrayList<>();
        int depth = 1;
        Token argumentStart = null;
        Token argumentStop = null;
        for (int index = openIndex + 1; index < closeIndex; index++) {
            Token token = values.get(index);
            int type = token.getType();
            if (type == PlpgsqlLexer.COMMA && depth == 1) {
                if (argumentStart != null) result.add(sourceRange(argumentStart, argumentStop));
                argumentStart = null;
                argumentStop = null;
                continue;
            }
            if (argumentStart == null) argumentStart = token;
            argumentStop = token;
            if (type == PlpgsqlLexer.LPAREN) depth++;
            if (type == PlpgsqlLexer.RPAREN) depth--;
        }
        if (argumentStart != null) result.add(sourceRange(argumentStart, argumentStop));
        return List.copyOf(result);
    }

    private SourceRangeCandidate sourceRange(ParserRuleContext context) {
        return sourceRange(context.getStart(), context.getStop());
    }

    private SourceRangeCandidate sourceRange(Token start, Token stop) {
        int startOffset = nestedSourceOffset + start.getStartIndex();
        int endOffset = nestedSourceOffset + stop.getStopIndex() + 1;
        if (startOffset < nestedSourceOffset || endOffset < startOffset
                || endOffset > sourceLength) {
            throw new IllegalArgumentException("nested PL/pgSQL range is outside source file");
        }
        return new SourceRangeCandidate(startOffset, endOffset);
    }
}
