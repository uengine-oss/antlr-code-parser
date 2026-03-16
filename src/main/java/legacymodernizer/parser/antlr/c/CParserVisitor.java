// Generated from antlr-grammars/CParser.g4 by ANTLR 4.13.2
package legacymodernizer.parser.antlr.c;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link CParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface CParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link CParser#compilationUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompilationUnit(CParser.CompilationUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant(CParser.ConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#enumerationConstant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumerationConstant(CParser.EnumerationConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#predefinedConstant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredefinedConstant(CParser.PredefinedConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryExpression(CParser.PrimaryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#exprList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprList(CParser.ExprListContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#genericSelection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericSelection(CParser.GenericSelectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#genericAssocList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericAssocList(CParser.GenericAssocListContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#genericAssociation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericAssociation(CParser.GenericAssociationContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#postfixExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostfixExpression(CParser.PostfixExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#argumentExpressionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgumentExpressionList(CParser.ArgumentExpressionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#unaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryExpression(CParser.UnaryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#castExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCastExpression(CParser.CastExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplicativeExpression(CParser.MultiplicativeExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#additiveExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditiveExpression(CParser.AdditiveExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#shiftExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShiftExpression(CParser.ShiftExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#relationalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationalExpression(CParser.RelationalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#equalityExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqualityExpression(CParser.EqualityExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#andExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndExpression(CParser.AndExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#exclusiveOrExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExclusiveOrExpression(CParser.ExclusiveOrExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#inclusiveOrExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInclusiveOrExpression(CParser.InclusiveOrExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#logicalAndExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalAndExpression(CParser.LogicalAndExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#logicalOrExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOrExpression(CParser.LogicalOrExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#conditionalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditionalExpression(CParser.ConditionalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#assignmentExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentExpression(CParser.AssignmentExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(CParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#constantExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantExpression(CParser.ConstantExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#declaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclaration(CParser.DeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#declarationSpecifiers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclarationSpecifiers(CParser.DeclarationSpecifiersContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#declarationSpecifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclarationSpecifier(CParser.DeclarationSpecifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#initDeclaratorList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInitDeclaratorList(CParser.InitDeclaratorListContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#initDeclarator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInitDeclarator(CParser.InitDeclaratorContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#attributeDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributeDeclaration(CParser.AttributeDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#storageClassSpecifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStorageClassSpecifier(CParser.StorageClassSpecifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#typeSpecifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeSpecifier(CParser.TypeSpecifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#structOrUnionSpecifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructOrUnionSpecifier(CParser.StructOrUnionSpecifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#structOrUnion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructOrUnion(CParser.StructOrUnionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#memberDeclarationList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemberDeclarationList(CParser.MemberDeclarationListContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#memberDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemberDeclaration(CParser.MemberDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#specifierQualifierList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecifierQualifierList(CParser.SpecifierQualifierListContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#typeSpecifierQualifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeSpecifierQualifier(CParser.TypeSpecifierQualifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#memberDeclaratorList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemberDeclaratorList(CParser.MemberDeclaratorListContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#memberDeclarator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemberDeclarator(CParser.MemberDeclaratorContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#enumSpecifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumSpecifier(CParser.EnumSpecifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#enumeratorList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumeratorList(CParser.EnumeratorListContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#enumerator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumerator(CParser.EnumeratorContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#enumTypeSpecifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumTypeSpecifier(CParser.EnumTypeSpecifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#atomicTypeSpecifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtomicTypeSpecifier(CParser.AtomicTypeSpecifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#typeofSpecifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeofSpecifier(CParser.TypeofSpecifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#typeofSpecifierArgument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeofSpecifierArgument(CParser.TypeofSpecifierArgumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#typeQualifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeQualifier(CParser.TypeQualifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#functionSpecifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionSpecifier(CParser.FunctionSpecifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#alignmentSpecifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlignmentSpecifier(CParser.AlignmentSpecifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#declarator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclarator(CParser.DeclaratorContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#directDeclarator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDirectDeclarator(CParser.DirectDeclaratorContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#pointer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPointer(CParser.PointerContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#typeQualifierList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeQualifierList(CParser.TypeQualifierListContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#parameterTypeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterTypeList(CParser.ParameterTypeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#parameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterList(CParser.ParameterListContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#parameterDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterDeclaration(CParser.ParameterDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#typeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeName(CParser.TypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#abstractDeclarator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAbstractDeclarator(CParser.AbstractDeclaratorContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#directAbstractDeclarator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDirectAbstractDeclarator(CParser.DirectAbstractDeclaratorContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#typedefName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypedefName(CParser.TypedefNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#initializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInitializer(CParser.InitializerContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#initializerList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInitializerList(CParser.InitializerListContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#designation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDesignation(CParser.DesignationContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#designatorList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDesignatorList(CParser.DesignatorListContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#designator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDesignator(CParser.DesignatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#staticAssertDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStaticAssertDeclaration(CParser.StaticAssertDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#attributeSpecifierSequence}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributeSpecifierSequence(CParser.AttributeSpecifierSequenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#attributeSpecifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributeSpecifier(CParser.AttributeSpecifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#attributeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributeList(CParser.AttributeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#attribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttribute(CParser.AttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#attributeToken}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributeToken(CParser.AttributeTokenContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#attributeArgumentClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributeArgumentClause(CParser.AttributeArgumentClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#balancedTokenSequence}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBalancedTokenSequence(CParser.BalancedTokenSequenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#balancedToken}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBalancedToken(CParser.BalancedTokenContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(CParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#labeledStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLabeledStatement(CParser.LabeledStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#compoundStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompoundStatement(CParser.CompoundStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#blockItemList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlockItemList(CParser.BlockItemListContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#blockItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlockItem(CParser.BlockItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#expressionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionStatement(CParser.ExpressionStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#selectionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectionStatement(CParser.SelectionStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#iterationStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIterationStatement(CParser.IterationStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#forCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForCondition(CParser.ForConditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#forDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForDeclaration(CParser.ForDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#forExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForExpression(CParser.ForExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#jumpStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJumpStatement(CParser.JumpStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#translationUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTranslationUnit(CParser.TranslationUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#externalDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExternalDeclaration(CParser.ExternalDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#functionDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionDefinition(CParser.FunctionDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#declarationList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclarationList(CParser.DeclarationListContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#functionBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionBody(CParser.FunctionBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#identifierList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierList(CParser.IdentifierListContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#gnuArrayDesignator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGnuArrayDesignator(CParser.GnuArrayDesignatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#gnuIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGnuIdentifier(CParser.GnuIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#asmArgument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAsmArgument(CParser.AsmArgumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#asmClobbers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAsmClobbers(CParser.AsmClobbersContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#asmDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAsmDefinition(CParser.AsmDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#toplevelAsmArgument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitToplevelAsmArgument(CParser.ToplevelAsmArgumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#asmOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAsmOperand(CParser.AsmOperandContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#asmOperands}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAsmOperands(CParser.AsmOperandsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#asmQualifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAsmQualifier(CParser.AsmQualifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#asmQualifierList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAsmQualifierList(CParser.AsmQualifierListContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#asmStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAsmStatement(CParser.AsmStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#asmStringLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAsmStringLiteral(CParser.AsmStringLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#gccDeclaratorExtension}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGccDeclaratorExtension(CParser.GccDeclaratorExtensionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#gnuAttribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGnuAttribute(CParser.GnuAttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#gnuAttributeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGnuAttributeList(CParser.GnuAttributeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#gnuAttributes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGnuAttributes(CParser.GnuAttributesContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#gnuSingleAttribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGnuSingleAttribute(CParser.GnuSingleAttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#simpleAsmExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleAsmExpr(CParser.SimpleAsmExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link CParser#vcSpecificModifer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVcSpecificModifer(CParser.VcSpecificModiferContext ctx);
}