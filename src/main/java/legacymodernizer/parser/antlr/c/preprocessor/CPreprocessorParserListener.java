// Generated from antlr-grammars/CPreprocessorParser.g4 by ANTLR 4.13.2

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