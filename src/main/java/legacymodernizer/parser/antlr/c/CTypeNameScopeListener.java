package legacymodernizer.parser.antlr.c;

/**
 * Parse-time scope events that generated grammar actions do not expose.
 *
 * <p>This listener runs while ANTLR parses, before the AST listener walk. It only updates the
 * C parser's symbol environment; it does not inspect source text.</p>
 */
public final class CTypeNameScopeListener extends CParserBaseListener {
    private final CParserBase parser;

    public CTypeNameScopeListener(CParserBase parser) {
        this.parser = parser;
    }

    @Override
    public void enterParameterTypeList(CParser.ParameterTypeListContext context) {
        parser.EnterParameterTypeList(context);
    }

    @Override
    public void exitParameterTypeList(CParser.ParameterTypeListContext context) {
        parser.ExitParameterTypeList(context);
    }

    @Override
    public void exitDeclarator(CParser.DeclaratorContext context) {
        parser.EnterDeclarator(context);
    }

    @Override
    public void exitEnumerator(CParser.EnumeratorContext context) {
        parser.EnterEnumerator(context);
    }

    @Override
    public void exitExpressionStatement(CParser.ExpressionStatementContext context) {
        parser.RecordExpressionStatement(context);
    }
}
