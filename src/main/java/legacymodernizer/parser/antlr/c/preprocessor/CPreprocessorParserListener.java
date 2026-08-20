// Generated from CPreprocessorParser.g4 by ANTLR 4.13.2

package legacymodernizer.parser.antlr.c.preprocessor;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link CPreprocessorParser}.
 */
public interface CPreprocessorParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#preprocessingFile}.
	 * @param ctx the parse tree
	 */
	void enterPreprocessingFile(CPreprocessorParser.PreprocessingFileContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#preprocessingFile}.
	 * @param ctx the parse tree
	 */
	void exitPreprocessingFile(CPreprocessorParser.PreprocessingFileContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#logicalLine}.
	 * @param ctx the parse tree
	 */
	void enterLogicalLine(CPreprocessorParser.LogicalLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#logicalLine}.
	 * @param ctx the parse tree
	 */
	void exitLogicalLine(CPreprocessorParser.LogicalLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#finalLogicalLine}.
	 * @param ctx the parse tree
	 */
	void enterFinalLogicalLine(CPreprocessorParser.FinalLogicalLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#finalLogicalLine}.
	 * @param ctx the parse tree
	 */
	void exitFinalLogicalLine(CPreprocessorParser.FinalLogicalLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#defineLine}.
	 * @param ctx the parse tree
	 */
	void enterDefineLine(CPreprocessorParser.DefineLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#defineLine}.
	 * @param ctx the parse tree
	 */
	void exitDefineLine(CPreprocessorParser.DefineLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#malformedDefineLine}.
	 * @param ctx the parse tree
	 */
	void enterMalformedDefineLine(CPreprocessorParser.MalformedDefineLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#malformedDefineLine}.
	 * @param ctx the parse tree
	 */
	void exitMalformedDefineLine(CPreprocessorParser.MalformedDefineLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#otherLine}.
	 * @param ctx the parse tree
	 */
	void enterOtherLine(CPreprocessorParser.OtherLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#otherLine}.
	 * @param ctx the parse tree
	 */
	void exitOtherLine(CPreprocessorParser.OtherLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#includeLine}.
	 * @param ctx the parse tree
	 */
	void enterIncludeLine(CPreprocessorParser.IncludeLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#includeLine}.
	 * @param ctx the parse tree
	 */
	void exitIncludeLine(CPreprocessorParser.IncludeLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#malformedIncludeLine}.
	 * @param ctx the parse tree
	 */
	void enterMalformedIncludeLine(CPreprocessorParser.MalformedIncludeLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#malformedIncludeLine}.
	 * @param ctx the parse tree
	 */
	void exitMalformedIncludeLine(CPreprocessorParser.MalformedIncludeLineContext ctx);
	/**
	 * Enter a parse tree produced by the {@code QuotedIncludeTarget}
	 * labeled alternative in {@link CPreprocessorParser#includeTarget}.
	 * @param ctx the parse tree
	 */
	void enterQuotedIncludeTarget(CPreprocessorParser.QuotedIncludeTargetContext ctx);
	/**
	 * Exit a parse tree produced by the {@code QuotedIncludeTarget}
	 * labeled alternative in {@link CPreprocessorParser#includeTarget}.
	 * @param ctx the parse tree
	 */
	void exitQuotedIncludeTarget(CPreprocessorParser.QuotedIncludeTargetContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AngleIncludeTarget}
	 * labeled alternative in {@link CPreprocessorParser#includeTarget}.
	 * @param ctx the parse tree
	 */
	void enterAngleIncludeTarget(CPreprocessorParser.AngleIncludeTargetContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AngleIncludeTarget}
	 * labeled alternative in {@link CPreprocessorParser#includeTarget}.
	 * @param ctx the parse tree
	 */
	void exitAngleIncludeTarget(CPreprocessorParser.AngleIncludeTargetContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ComputedIncludeTarget}
	 * labeled alternative in {@link CPreprocessorParser#includeTarget}.
	 * @param ctx the parse tree
	 */
	void enterComputedIncludeTarget(CPreprocessorParser.ComputedIncludeTargetContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ComputedIncludeTarget}
	 * labeled alternative in {@link CPreprocessorParser#includeTarget}.
	 * @param ctx the parse tree
	 */
	void exitComputedIncludeTarget(CPreprocessorParser.ComputedIncludeTargetContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#computedHeaderTokens}.
	 * @param ctx the parse tree
	 */
	void enterComputedHeaderTokens(CPreprocessorParser.ComputedHeaderTokensContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#computedHeaderTokens}.
	 * @param ctx the parse tree
	 */
	void exitComputedHeaderTokens(CPreprocessorParser.ComputedHeaderTokensContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#includeAngleToken}.
	 * @param ctx the parse tree
	 */
	void enterIncludeAngleToken(CPreprocessorParser.IncludeAngleTokenContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#includeAngleToken}.
	 * @param ctx the parse tree
	 */
	void exitIncludeAngleToken(CPreprocessorParser.IncludeAngleTokenContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#conditionalLine}.
	 * @param ctx the parse tree
	 */
	void enterConditionalLine(CPreprocessorParser.ConditionalLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#conditionalLine}.
	 * @param ctx the parse tree
	 */
	void exitConditionalLine(CPreprocessorParser.ConditionalLineContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IfDirective}
	 * labeled alternative in {@link CPreprocessorParser#conditionalDirective}.
	 * @param ctx the parse tree
	 */
	void enterIfDirective(CPreprocessorParser.IfDirectiveContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IfDirective}
	 * labeled alternative in {@link CPreprocessorParser#conditionalDirective}.
	 * @param ctx the parse tree
	 */
	void exitIfDirective(CPreprocessorParser.IfDirectiveContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IfdefDirective}
	 * labeled alternative in {@link CPreprocessorParser#conditionalDirective}.
	 * @param ctx the parse tree
	 */
	void enterIfdefDirective(CPreprocessorParser.IfdefDirectiveContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IfdefDirective}
	 * labeled alternative in {@link CPreprocessorParser#conditionalDirective}.
	 * @param ctx the parse tree
	 */
	void exitIfdefDirective(CPreprocessorParser.IfdefDirectiveContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IfndefDirective}
	 * labeled alternative in {@link CPreprocessorParser#conditionalDirective}.
	 * @param ctx the parse tree
	 */
	void enterIfndefDirective(CPreprocessorParser.IfndefDirectiveContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IfndefDirective}
	 * labeled alternative in {@link CPreprocessorParser#conditionalDirective}.
	 * @param ctx the parse tree
	 */
	void exitIfndefDirective(CPreprocessorParser.IfndefDirectiveContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ElifDirective}
	 * labeled alternative in {@link CPreprocessorParser#conditionalDirective}.
	 * @param ctx the parse tree
	 */
	void enterElifDirective(CPreprocessorParser.ElifDirectiveContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ElifDirective}
	 * labeled alternative in {@link CPreprocessorParser#conditionalDirective}.
	 * @param ctx the parse tree
	 */
	void exitElifDirective(CPreprocessorParser.ElifDirectiveContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ElseDirective}
	 * labeled alternative in {@link CPreprocessorParser#conditionalDirective}.
	 * @param ctx the parse tree
	 */
	void enterElseDirective(CPreprocessorParser.ElseDirectiveContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ElseDirective}
	 * labeled alternative in {@link CPreprocessorParser#conditionalDirective}.
	 * @param ctx the parse tree
	 */
	void exitElseDirective(CPreprocessorParser.ElseDirectiveContext ctx);
	/**
	 * Enter a parse tree produced by the {@code EndifDirective}
	 * labeled alternative in {@link CPreprocessorParser#conditionalDirective}.
	 * @param ctx the parse tree
	 */
	void enterEndifDirective(CPreprocessorParser.EndifDirectiveContext ctx);
	/**
	 * Exit a parse tree produced by the {@code EndifDirective}
	 * labeled alternative in {@link CPreprocessorParser#conditionalDirective}.
	 * @param ctx the parse tree
	 */
	void exitEndifDirective(CPreprocessorParser.EndifDirectiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#undefLine}.
	 * @param ctx the parse tree
	 */
	void enterUndefLine(CPreprocessorParser.UndefLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#undefLine}.
	 * @param ctx the parse tree
	 */
	void exitUndefLine(CPreprocessorParser.UndefLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#otherDirectiveLine}.
	 * @param ctx the parse tree
	 */
	void enterOtherDirectiveLine(CPreprocessorParser.OtherDirectiveLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#otherDirectiveLine}.
	 * @param ctx the parse tree
	 */
	void exitOtherDirectiveLine(CPreprocessorParser.OtherDirectiveLineContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FunctionDefine}
	 * labeled alternative in {@link CPreprocessorParser#defineDirective}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDefine(CPreprocessorParser.FunctionDefineContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FunctionDefine}
	 * labeled alternative in {@link CPreprocessorParser#defineDirective}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDefine(CPreprocessorParser.FunctionDefineContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ObjectDefine}
	 * labeled alternative in {@link CPreprocessorParser#defineDirective}.
	 * @param ctx the parse tree
	 */
	void enterObjectDefine(CPreprocessorParser.ObjectDefineContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ObjectDefine}
	 * labeled alternative in {@link CPreprocessorParser#defineDirective}.
	 * @param ctx the parse tree
	 */
	void exitObjectDefine(CPreprocessorParser.ObjectDefineContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#macroName}.
	 * @param ctx the parse tree
	 */
	void enterMacroName(CPreprocessorParser.MacroNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#macroName}.
	 * @param ctx the parse tree
	 */
	void exitMacroName(CPreprocessorParser.MacroNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#preprocessingIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterPreprocessingIdentifier(CPreprocessorParser.PreprocessingIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#preprocessingIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitPreprocessingIdentifier(CPreprocessorParser.PreprocessingIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#parameterList}.
	 * @param ctx the parse tree
	 */
	void enterParameterList(CPreprocessorParser.ParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#parameterList}.
	 * @param ctx the parse tree
	 */
	void exitParameterList(CPreprocessorParser.ParameterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#macroParameter}.
	 * @param ctx the parse tree
	 */
	void enterMacroParameter(CPreprocessorParser.MacroParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#macroParameter}.
	 * @param ctx the parse tree
	 */
	void exitMacroParameter(CPreprocessorParser.MacroParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#conditionalExpression}.
	 * @param ctx the parse tree
	 */
	void enterConditionalExpression(CPreprocessorParser.ConditionalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#conditionalExpression}.
	 * @param ctx the parse tree
	 */
	void exitConditionalExpression(CPreprocessorParser.ConditionalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#logicalOrExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalOrExpression(CPreprocessorParser.LogicalOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#logicalOrExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalOrExpression(CPreprocessorParser.LogicalOrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#logicalAndExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalAndExpression(CPreprocessorParser.LogicalAndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#logicalAndExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalAndExpression(CPreprocessorParser.LogicalAndExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#inclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void enterInclusiveOrExpression(CPreprocessorParser.InclusiveOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#inclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void exitInclusiveOrExpression(CPreprocessorParser.InclusiveOrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#exclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void enterExclusiveOrExpression(CPreprocessorParser.ExclusiveOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#exclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void exitExclusiveOrExpression(CPreprocessorParser.ExclusiveOrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#andExpression}.
	 * @param ctx the parse tree
	 */
	void enterAndExpression(CPreprocessorParser.AndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#andExpression}.
	 * @param ctx the parse tree
	 */
	void exitAndExpression(CPreprocessorParser.AndExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#equalityExpression}.
	 * @param ctx the parse tree
	 */
	void enterEqualityExpression(CPreprocessorParser.EqualityExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#equalityExpression}.
	 * @param ctx the parse tree
	 */
	void exitEqualityExpression(CPreprocessorParser.EqualityExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#relationalExpression}.
	 * @param ctx the parse tree
	 */
	void enterRelationalExpression(CPreprocessorParser.RelationalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#relationalExpression}.
	 * @param ctx the parse tree
	 */
	void exitRelationalExpression(CPreprocessorParser.RelationalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#shiftExpression}.
	 * @param ctx the parse tree
	 */
	void enterShiftExpression(CPreprocessorParser.ShiftExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#shiftExpression}.
	 * @param ctx the parse tree
	 */
	void exitShiftExpression(CPreprocessorParser.ShiftExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#additiveExpression}.
	 * @param ctx the parse tree
	 */
	void enterAdditiveExpression(CPreprocessorParser.AdditiveExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#additiveExpression}.
	 * @param ctx the parse tree
	 */
	void exitAdditiveExpression(CPreprocessorParser.AdditiveExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicativeExpression(CPreprocessorParser.MultiplicativeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicativeExpression(CPreprocessorParser.MultiplicativeExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpression(CPreprocessorParser.UnaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpression(CPreprocessorParser.UnaryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryExpression(CPreprocessorParser.PrimaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryExpression(CPreprocessorParser.PrimaryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#objectReplacement}.
	 * @param ctx the parse tree
	 */
	void enterObjectReplacement(CPreprocessorParser.ObjectReplacementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#objectReplacement}.
	 * @param ctx the parse tree
	 */
	void exitObjectReplacement(CPreprocessorParser.ObjectReplacementContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#replacementList}.
	 * @param ctx the parse tree
	 */
	void enterReplacementList(CPreprocessorParser.ReplacementListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#replacementList}.
	 * @param ctx the parse tree
	 */
	void exitReplacementList(CPreprocessorParser.ReplacementListContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#replacementTokens}.
	 * @param ctx the parse tree
	 */
	void enterReplacementTokens(CPreprocessorParser.ReplacementTokensContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#replacementTokens}.
	 * @param ctx the parse tree
	 */
	void exitReplacementTokens(CPreprocessorParser.ReplacementTokensContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#nonLparenReplacementToken}.
	 * @param ctx the parse tree
	 */
	void enterNonLparenReplacementToken(CPreprocessorParser.NonLparenReplacementTokenContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#nonLparenReplacementToken}.
	 * @param ctx the parse tree
	 */
	void exitNonLparenReplacementToken(CPreprocessorParser.NonLparenReplacementTokenContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#replacementToken}.
	 * @param ctx the parse tree
	 */
	void enterReplacementToken(CPreprocessorParser.ReplacementTokenContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#replacementToken}.
	 * @param ctx the parse tree
	 */
	void exitReplacementToken(CPreprocessorParser.ReplacementTokenContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#requiredHorizontal}.
	 * @param ctx the parse tree
	 */
	void enterRequiredHorizontal(CPreprocessorParser.RequiredHorizontalContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#requiredHorizontal}.
	 * @param ctx the parse tree
	 */
	void exitRequiredHorizontal(CPreprocessorParser.RequiredHorizontalContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#horizontal}.
	 * @param ctx the parse tree
	 */
	void enterHorizontal(CPreprocessorParser.HorizontalContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#horizontal}.
	 * @param ctx the parse tree
	 */
	void exitHorizontal(CPreprocessorParser.HorizontalContext ctx);
	/**
	 * Enter a parse tree produced by {@link CPreprocessorParser#ppToken}.
	 * @param ctx the parse tree
	 */
	void enterPpToken(CPreprocessorParser.PpTokenContext ctx);
	/**
	 * Exit a parse tree produced by {@link CPreprocessorParser#ppToken}.
	 * @param ctx the parse tree
	 */
	void exitPpToken(CPreprocessorParser.PpTokenContext ctx);
}