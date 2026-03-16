// Generated from antlr-grammars/CParser.g4 by ANTLR 4.13.2
package legacymodernizer.parser.antlr.c;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link CParser}.
 */
public interface CParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link CParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void enterCompilationUnit(CParser.CompilationUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void exitCompilationUnit(CParser.CompilationUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterConstant(CParser.ConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitConstant(CParser.ConstantContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#enumerationConstant}.
	 * @param ctx the parse tree
	 */
	void enterEnumerationConstant(CParser.EnumerationConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#enumerationConstant}.
	 * @param ctx the parse tree
	 */
	void exitEnumerationConstant(CParser.EnumerationConstantContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#predefinedConstant}.
	 * @param ctx the parse tree
	 */
	void enterPredefinedConstant(CParser.PredefinedConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#predefinedConstant}.
	 * @param ctx the parse tree
	 */
	void exitPredefinedConstant(CParser.PredefinedConstantContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryExpression(CParser.PrimaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryExpression(CParser.PrimaryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#exprList}.
	 * @param ctx the parse tree
	 */
	void enterExprList(CParser.ExprListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#exprList}.
	 * @param ctx the parse tree
	 */
	void exitExprList(CParser.ExprListContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#genericSelection}.
	 * @param ctx the parse tree
	 */
	void enterGenericSelection(CParser.GenericSelectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#genericSelection}.
	 * @param ctx the parse tree
	 */
	void exitGenericSelection(CParser.GenericSelectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#genericAssocList}.
	 * @param ctx the parse tree
	 */
	void enterGenericAssocList(CParser.GenericAssocListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#genericAssocList}.
	 * @param ctx the parse tree
	 */
	void exitGenericAssocList(CParser.GenericAssocListContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#genericAssociation}.
	 * @param ctx the parse tree
	 */
	void enterGenericAssociation(CParser.GenericAssociationContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#genericAssociation}.
	 * @param ctx the parse tree
	 */
	void exitGenericAssociation(CParser.GenericAssociationContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#postfixExpression}.
	 * @param ctx the parse tree
	 */
	void enterPostfixExpression(CParser.PostfixExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#postfixExpression}.
	 * @param ctx the parse tree
	 */
	void exitPostfixExpression(CParser.PostfixExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#argumentExpressionList}.
	 * @param ctx the parse tree
	 */
	void enterArgumentExpressionList(CParser.ArgumentExpressionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#argumentExpressionList}.
	 * @param ctx the parse tree
	 */
	void exitArgumentExpressionList(CParser.ArgumentExpressionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpression(CParser.UnaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpression(CParser.UnaryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#castExpression}.
	 * @param ctx the parse tree
	 */
	void enterCastExpression(CParser.CastExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#castExpression}.
	 * @param ctx the parse tree
	 */
	void exitCastExpression(CParser.CastExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicativeExpression(CParser.MultiplicativeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicativeExpression(CParser.MultiplicativeExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#additiveExpression}.
	 * @param ctx the parse tree
	 */
	void enterAdditiveExpression(CParser.AdditiveExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#additiveExpression}.
	 * @param ctx the parse tree
	 */
	void exitAdditiveExpression(CParser.AdditiveExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#shiftExpression}.
	 * @param ctx the parse tree
	 */
	void enterShiftExpression(CParser.ShiftExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#shiftExpression}.
	 * @param ctx the parse tree
	 */
	void exitShiftExpression(CParser.ShiftExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#relationalExpression}.
	 * @param ctx the parse tree
	 */
	void enterRelationalExpression(CParser.RelationalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#relationalExpression}.
	 * @param ctx the parse tree
	 */
	void exitRelationalExpression(CParser.RelationalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#equalityExpression}.
	 * @param ctx the parse tree
	 */
	void enterEqualityExpression(CParser.EqualityExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#equalityExpression}.
	 * @param ctx the parse tree
	 */
	void exitEqualityExpression(CParser.EqualityExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#andExpression}.
	 * @param ctx the parse tree
	 */
	void enterAndExpression(CParser.AndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#andExpression}.
	 * @param ctx the parse tree
	 */
	void exitAndExpression(CParser.AndExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#exclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void enterExclusiveOrExpression(CParser.ExclusiveOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#exclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void exitExclusiveOrExpression(CParser.ExclusiveOrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#inclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void enterInclusiveOrExpression(CParser.InclusiveOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#inclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void exitInclusiveOrExpression(CParser.InclusiveOrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#logicalAndExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalAndExpression(CParser.LogicalAndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#logicalAndExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalAndExpression(CParser.LogicalAndExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#logicalOrExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalOrExpression(CParser.LogicalOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#logicalOrExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalOrExpression(CParser.LogicalOrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#conditionalExpression}.
	 * @param ctx the parse tree
	 */
	void enterConditionalExpression(CParser.ConditionalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#conditionalExpression}.
	 * @param ctx the parse tree
	 */
	void exitConditionalExpression(CParser.ConditionalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentExpression(CParser.AssignmentExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentExpression(CParser.AssignmentExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(CParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(CParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#constantExpression}.
	 * @param ctx the parse tree
	 */
	void enterConstantExpression(CParser.ConstantExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#constantExpression}.
	 * @param ctx the parse tree
	 */
	void exitConstantExpression(CParser.ConstantExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#declaration}.
	 * @param ctx the parse tree
	 */
	void enterDeclaration(CParser.DeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#declaration}.
	 * @param ctx the parse tree
	 */
	void exitDeclaration(CParser.DeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#declarationSpecifiers}.
	 * @param ctx the parse tree
	 */
	void enterDeclarationSpecifiers(CParser.DeclarationSpecifiersContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#declarationSpecifiers}.
	 * @param ctx the parse tree
	 */
	void exitDeclarationSpecifiers(CParser.DeclarationSpecifiersContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#declarationSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterDeclarationSpecifier(CParser.DeclarationSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#declarationSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitDeclarationSpecifier(CParser.DeclarationSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#initDeclaratorList}.
	 * @param ctx the parse tree
	 */
	void enterInitDeclaratorList(CParser.InitDeclaratorListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#initDeclaratorList}.
	 * @param ctx the parse tree
	 */
	void exitInitDeclaratorList(CParser.InitDeclaratorListContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#initDeclarator}.
	 * @param ctx the parse tree
	 */
	void enterInitDeclarator(CParser.InitDeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#initDeclarator}.
	 * @param ctx the parse tree
	 */
	void exitInitDeclarator(CParser.InitDeclaratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#attributeDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterAttributeDeclaration(CParser.AttributeDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#attributeDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitAttributeDeclaration(CParser.AttributeDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#storageClassSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterStorageClassSpecifier(CParser.StorageClassSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#storageClassSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitStorageClassSpecifier(CParser.StorageClassSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#typeSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterTypeSpecifier(CParser.TypeSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#typeSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitTypeSpecifier(CParser.TypeSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#structOrUnionSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterStructOrUnionSpecifier(CParser.StructOrUnionSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#structOrUnionSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitStructOrUnionSpecifier(CParser.StructOrUnionSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#structOrUnion}.
	 * @param ctx the parse tree
	 */
	void enterStructOrUnion(CParser.StructOrUnionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#structOrUnion}.
	 * @param ctx the parse tree
	 */
	void exitStructOrUnion(CParser.StructOrUnionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#memberDeclarationList}.
	 * @param ctx the parse tree
	 */
	void enterMemberDeclarationList(CParser.MemberDeclarationListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#memberDeclarationList}.
	 * @param ctx the parse tree
	 */
	void exitMemberDeclarationList(CParser.MemberDeclarationListContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#memberDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterMemberDeclaration(CParser.MemberDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#memberDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitMemberDeclaration(CParser.MemberDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#specifierQualifierList}.
	 * @param ctx the parse tree
	 */
	void enterSpecifierQualifierList(CParser.SpecifierQualifierListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#specifierQualifierList}.
	 * @param ctx the parse tree
	 */
	void exitSpecifierQualifierList(CParser.SpecifierQualifierListContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#typeSpecifierQualifier}.
	 * @param ctx the parse tree
	 */
	void enterTypeSpecifierQualifier(CParser.TypeSpecifierQualifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#typeSpecifierQualifier}.
	 * @param ctx the parse tree
	 */
	void exitTypeSpecifierQualifier(CParser.TypeSpecifierQualifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#memberDeclaratorList}.
	 * @param ctx the parse tree
	 */
	void enterMemberDeclaratorList(CParser.MemberDeclaratorListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#memberDeclaratorList}.
	 * @param ctx the parse tree
	 */
	void exitMemberDeclaratorList(CParser.MemberDeclaratorListContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#memberDeclarator}.
	 * @param ctx the parse tree
	 */
	void enterMemberDeclarator(CParser.MemberDeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#memberDeclarator}.
	 * @param ctx the parse tree
	 */
	void exitMemberDeclarator(CParser.MemberDeclaratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#enumSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterEnumSpecifier(CParser.EnumSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#enumSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitEnumSpecifier(CParser.EnumSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#enumeratorList}.
	 * @param ctx the parse tree
	 */
	void enterEnumeratorList(CParser.EnumeratorListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#enumeratorList}.
	 * @param ctx the parse tree
	 */
	void exitEnumeratorList(CParser.EnumeratorListContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#enumerator}.
	 * @param ctx the parse tree
	 */
	void enterEnumerator(CParser.EnumeratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#enumerator}.
	 * @param ctx the parse tree
	 */
	void exitEnumerator(CParser.EnumeratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#enumTypeSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterEnumTypeSpecifier(CParser.EnumTypeSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#enumTypeSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitEnumTypeSpecifier(CParser.EnumTypeSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#atomicTypeSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterAtomicTypeSpecifier(CParser.AtomicTypeSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#atomicTypeSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitAtomicTypeSpecifier(CParser.AtomicTypeSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#typeofSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterTypeofSpecifier(CParser.TypeofSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#typeofSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitTypeofSpecifier(CParser.TypeofSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#typeofSpecifierArgument}.
	 * @param ctx the parse tree
	 */
	void enterTypeofSpecifierArgument(CParser.TypeofSpecifierArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#typeofSpecifierArgument}.
	 * @param ctx the parse tree
	 */
	void exitTypeofSpecifierArgument(CParser.TypeofSpecifierArgumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#typeQualifier}.
	 * @param ctx the parse tree
	 */
	void enterTypeQualifier(CParser.TypeQualifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#typeQualifier}.
	 * @param ctx the parse tree
	 */
	void exitTypeQualifier(CParser.TypeQualifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#functionSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterFunctionSpecifier(CParser.FunctionSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#functionSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitFunctionSpecifier(CParser.FunctionSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#alignmentSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterAlignmentSpecifier(CParser.AlignmentSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#alignmentSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitAlignmentSpecifier(CParser.AlignmentSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#declarator}.
	 * @param ctx the parse tree
	 */
	void enterDeclarator(CParser.DeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#declarator}.
	 * @param ctx the parse tree
	 */
	void exitDeclarator(CParser.DeclaratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#directDeclarator}.
	 * @param ctx the parse tree
	 */
	void enterDirectDeclarator(CParser.DirectDeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#directDeclarator}.
	 * @param ctx the parse tree
	 */
	void exitDirectDeclarator(CParser.DirectDeclaratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#pointer}.
	 * @param ctx the parse tree
	 */
	void enterPointer(CParser.PointerContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#pointer}.
	 * @param ctx the parse tree
	 */
	void exitPointer(CParser.PointerContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#typeQualifierList}.
	 * @param ctx the parse tree
	 */
	void enterTypeQualifierList(CParser.TypeQualifierListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#typeQualifierList}.
	 * @param ctx the parse tree
	 */
	void exitTypeQualifierList(CParser.TypeQualifierListContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#parameterTypeList}.
	 * @param ctx the parse tree
	 */
	void enterParameterTypeList(CParser.ParameterTypeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#parameterTypeList}.
	 * @param ctx the parse tree
	 */
	void exitParameterTypeList(CParser.ParameterTypeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#parameterList}.
	 * @param ctx the parse tree
	 */
	void enterParameterList(CParser.ParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#parameterList}.
	 * @param ctx the parse tree
	 */
	void exitParameterList(CParser.ParameterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#parameterDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterParameterDeclaration(CParser.ParameterDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#parameterDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitParameterDeclaration(CParser.ParameterDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#typeName}.
	 * @param ctx the parse tree
	 */
	void enterTypeName(CParser.TypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#typeName}.
	 * @param ctx the parse tree
	 */
	void exitTypeName(CParser.TypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#abstractDeclarator}.
	 * @param ctx the parse tree
	 */
	void enterAbstractDeclarator(CParser.AbstractDeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#abstractDeclarator}.
	 * @param ctx the parse tree
	 */
	void exitAbstractDeclarator(CParser.AbstractDeclaratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#directAbstractDeclarator}.
	 * @param ctx the parse tree
	 */
	void enterDirectAbstractDeclarator(CParser.DirectAbstractDeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#directAbstractDeclarator}.
	 * @param ctx the parse tree
	 */
	void exitDirectAbstractDeclarator(CParser.DirectAbstractDeclaratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#typedefName}.
	 * @param ctx the parse tree
	 */
	void enterTypedefName(CParser.TypedefNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#typedefName}.
	 * @param ctx the parse tree
	 */
	void exitTypedefName(CParser.TypedefNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#initializer}.
	 * @param ctx the parse tree
	 */
	void enterInitializer(CParser.InitializerContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#initializer}.
	 * @param ctx the parse tree
	 */
	void exitInitializer(CParser.InitializerContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#initializerList}.
	 * @param ctx the parse tree
	 */
	void enterInitializerList(CParser.InitializerListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#initializerList}.
	 * @param ctx the parse tree
	 */
	void exitInitializerList(CParser.InitializerListContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#designation}.
	 * @param ctx the parse tree
	 */
	void enterDesignation(CParser.DesignationContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#designation}.
	 * @param ctx the parse tree
	 */
	void exitDesignation(CParser.DesignationContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#designatorList}.
	 * @param ctx the parse tree
	 */
	void enterDesignatorList(CParser.DesignatorListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#designatorList}.
	 * @param ctx the parse tree
	 */
	void exitDesignatorList(CParser.DesignatorListContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#designator}.
	 * @param ctx the parse tree
	 */
	void enterDesignator(CParser.DesignatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#designator}.
	 * @param ctx the parse tree
	 */
	void exitDesignator(CParser.DesignatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#staticAssertDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterStaticAssertDeclaration(CParser.StaticAssertDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#staticAssertDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitStaticAssertDeclaration(CParser.StaticAssertDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#attributeSpecifierSequence}.
	 * @param ctx the parse tree
	 */
	void enterAttributeSpecifierSequence(CParser.AttributeSpecifierSequenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#attributeSpecifierSequence}.
	 * @param ctx the parse tree
	 */
	void exitAttributeSpecifierSequence(CParser.AttributeSpecifierSequenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#attributeSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterAttributeSpecifier(CParser.AttributeSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#attributeSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitAttributeSpecifier(CParser.AttributeSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#attributeList}.
	 * @param ctx the parse tree
	 */
	void enterAttributeList(CParser.AttributeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#attributeList}.
	 * @param ctx the parse tree
	 */
	void exitAttributeList(CParser.AttributeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#attribute}.
	 * @param ctx the parse tree
	 */
	void enterAttribute(CParser.AttributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#attribute}.
	 * @param ctx the parse tree
	 */
	void exitAttribute(CParser.AttributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#attributeToken}.
	 * @param ctx the parse tree
	 */
	void enterAttributeToken(CParser.AttributeTokenContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#attributeToken}.
	 * @param ctx the parse tree
	 */
	void exitAttributeToken(CParser.AttributeTokenContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#attributeArgumentClause}.
	 * @param ctx the parse tree
	 */
	void enterAttributeArgumentClause(CParser.AttributeArgumentClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#attributeArgumentClause}.
	 * @param ctx the parse tree
	 */
	void exitAttributeArgumentClause(CParser.AttributeArgumentClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#balancedTokenSequence}.
	 * @param ctx the parse tree
	 */
	void enterBalancedTokenSequence(CParser.BalancedTokenSequenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#balancedTokenSequence}.
	 * @param ctx the parse tree
	 */
	void exitBalancedTokenSequence(CParser.BalancedTokenSequenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#balancedToken}.
	 * @param ctx the parse tree
	 */
	void enterBalancedToken(CParser.BalancedTokenContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#balancedToken}.
	 * @param ctx the parse tree
	 */
	void exitBalancedToken(CParser.BalancedTokenContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(CParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(CParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#labeledStatement}.
	 * @param ctx the parse tree
	 */
	void enterLabeledStatement(CParser.LabeledStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#labeledStatement}.
	 * @param ctx the parse tree
	 */
	void exitLabeledStatement(CParser.LabeledStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#compoundStatement}.
	 * @param ctx the parse tree
	 */
	void enterCompoundStatement(CParser.CompoundStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#compoundStatement}.
	 * @param ctx the parse tree
	 */
	void exitCompoundStatement(CParser.CompoundStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#blockItemList}.
	 * @param ctx the parse tree
	 */
	void enterBlockItemList(CParser.BlockItemListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#blockItemList}.
	 * @param ctx the parse tree
	 */
	void exitBlockItemList(CParser.BlockItemListContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#blockItem}.
	 * @param ctx the parse tree
	 */
	void enterBlockItem(CParser.BlockItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#blockItem}.
	 * @param ctx the parse tree
	 */
	void exitBlockItem(CParser.BlockItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#expressionStatement}.
	 * @param ctx the parse tree
	 */
	void enterExpressionStatement(CParser.ExpressionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#expressionStatement}.
	 * @param ctx the parse tree
	 */
	void exitExpressionStatement(CParser.ExpressionStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#selectionStatement}.
	 * @param ctx the parse tree
	 */
	void enterSelectionStatement(CParser.SelectionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#selectionStatement}.
	 * @param ctx the parse tree
	 */
	void exitSelectionStatement(CParser.SelectionStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#iterationStatement}.
	 * @param ctx the parse tree
	 */
	void enterIterationStatement(CParser.IterationStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#iterationStatement}.
	 * @param ctx the parse tree
	 */
	void exitIterationStatement(CParser.IterationStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#forCondition}.
	 * @param ctx the parse tree
	 */
	void enterForCondition(CParser.ForConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#forCondition}.
	 * @param ctx the parse tree
	 */
	void exitForCondition(CParser.ForConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#forDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterForDeclaration(CParser.ForDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#forDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitForDeclaration(CParser.ForDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#forExpression}.
	 * @param ctx the parse tree
	 */
	void enterForExpression(CParser.ForExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#forExpression}.
	 * @param ctx the parse tree
	 */
	void exitForExpression(CParser.ForExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#jumpStatement}.
	 * @param ctx the parse tree
	 */
	void enterJumpStatement(CParser.JumpStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#jumpStatement}.
	 * @param ctx the parse tree
	 */
	void exitJumpStatement(CParser.JumpStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#translationUnit}.
	 * @param ctx the parse tree
	 */
	void enterTranslationUnit(CParser.TranslationUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#translationUnit}.
	 * @param ctx the parse tree
	 */
	void exitTranslationUnit(CParser.TranslationUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#externalDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterExternalDeclaration(CParser.ExternalDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#externalDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitExternalDeclaration(CParser.ExternalDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#functionDefinition}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDefinition(CParser.FunctionDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#functionDefinition}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDefinition(CParser.FunctionDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#declarationList}.
	 * @param ctx the parse tree
	 */
	void enterDeclarationList(CParser.DeclarationListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#declarationList}.
	 * @param ctx the parse tree
	 */
	void exitDeclarationList(CParser.DeclarationListContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#functionBody}.
	 * @param ctx the parse tree
	 */
	void enterFunctionBody(CParser.FunctionBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#functionBody}.
	 * @param ctx the parse tree
	 */
	void exitFunctionBody(CParser.FunctionBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#identifierList}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierList(CParser.IdentifierListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#identifierList}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierList(CParser.IdentifierListContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#gnuArrayDesignator}.
	 * @param ctx the parse tree
	 */
	void enterGnuArrayDesignator(CParser.GnuArrayDesignatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#gnuArrayDesignator}.
	 * @param ctx the parse tree
	 */
	void exitGnuArrayDesignator(CParser.GnuArrayDesignatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#gnuIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterGnuIdentifier(CParser.GnuIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#gnuIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitGnuIdentifier(CParser.GnuIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#asmArgument}.
	 * @param ctx the parse tree
	 */
	void enterAsmArgument(CParser.AsmArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#asmArgument}.
	 * @param ctx the parse tree
	 */
	void exitAsmArgument(CParser.AsmArgumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#asmClobbers}.
	 * @param ctx the parse tree
	 */
	void enterAsmClobbers(CParser.AsmClobbersContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#asmClobbers}.
	 * @param ctx the parse tree
	 */
	void exitAsmClobbers(CParser.AsmClobbersContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#asmDefinition}.
	 * @param ctx the parse tree
	 */
	void enterAsmDefinition(CParser.AsmDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#asmDefinition}.
	 * @param ctx the parse tree
	 */
	void exitAsmDefinition(CParser.AsmDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#toplevelAsmArgument}.
	 * @param ctx the parse tree
	 */
	void enterToplevelAsmArgument(CParser.ToplevelAsmArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#toplevelAsmArgument}.
	 * @param ctx the parse tree
	 */
	void exitToplevelAsmArgument(CParser.ToplevelAsmArgumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#asmOperand}.
	 * @param ctx the parse tree
	 */
	void enterAsmOperand(CParser.AsmOperandContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#asmOperand}.
	 * @param ctx the parse tree
	 */
	void exitAsmOperand(CParser.AsmOperandContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#asmOperands}.
	 * @param ctx the parse tree
	 */
	void enterAsmOperands(CParser.AsmOperandsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#asmOperands}.
	 * @param ctx the parse tree
	 */
	void exitAsmOperands(CParser.AsmOperandsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#asmQualifier}.
	 * @param ctx the parse tree
	 */
	void enterAsmQualifier(CParser.AsmQualifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#asmQualifier}.
	 * @param ctx the parse tree
	 */
	void exitAsmQualifier(CParser.AsmQualifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#asmQualifierList}.
	 * @param ctx the parse tree
	 */
	void enterAsmQualifierList(CParser.AsmQualifierListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#asmQualifierList}.
	 * @param ctx the parse tree
	 */
	void exitAsmQualifierList(CParser.AsmQualifierListContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#asmStatement}.
	 * @param ctx the parse tree
	 */
	void enterAsmStatement(CParser.AsmStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#asmStatement}.
	 * @param ctx the parse tree
	 */
	void exitAsmStatement(CParser.AsmStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#asmStringLiteral}.
	 * @param ctx the parse tree
	 */
	void enterAsmStringLiteral(CParser.AsmStringLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#asmStringLiteral}.
	 * @param ctx the parse tree
	 */
	void exitAsmStringLiteral(CParser.AsmStringLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#gccDeclaratorExtension}.
	 * @param ctx the parse tree
	 */
	void enterGccDeclaratorExtension(CParser.GccDeclaratorExtensionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#gccDeclaratorExtension}.
	 * @param ctx the parse tree
	 */
	void exitGccDeclaratorExtension(CParser.GccDeclaratorExtensionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#gnuAttribute}.
	 * @param ctx the parse tree
	 */
	void enterGnuAttribute(CParser.GnuAttributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#gnuAttribute}.
	 * @param ctx the parse tree
	 */
	void exitGnuAttribute(CParser.GnuAttributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#gnuAttributeList}.
	 * @param ctx the parse tree
	 */
	void enterGnuAttributeList(CParser.GnuAttributeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#gnuAttributeList}.
	 * @param ctx the parse tree
	 */
	void exitGnuAttributeList(CParser.GnuAttributeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#gnuAttributes}.
	 * @param ctx the parse tree
	 */
	void enterGnuAttributes(CParser.GnuAttributesContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#gnuAttributes}.
	 * @param ctx the parse tree
	 */
	void exitGnuAttributes(CParser.GnuAttributesContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#gnuSingleAttribute}.
	 * @param ctx the parse tree
	 */
	void enterGnuSingleAttribute(CParser.GnuSingleAttributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#gnuSingleAttribute}.
	 * @param ctx the parse tree
	 */
	void exitGnuSingleAttribute(CParser.GnuSingleAttributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#simpleAsmExpr}.
	 * @param ctx the parse tree
	 */
	void enterSimpleAsmExpr(CParser.SimpleAsmExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#simpleAsmExpr}.
	 * @param ctx the parse tree
	 */
	void exitSimpleAsmExpr(CParser.SimpleAsmExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link CParser#vcSpecificModifer}.
	 * @param ctx the parse tree
	 */
	void enterVcSpecificModifer(CParser.VcSpecificModiferContext ctx);
	/**
	 * Exit a parse tree produced by {@link CParser#vcSpecificModifer}.
	 * @param ctx the parse tree
	 */
	void exitVcSpecificModifer(CParser.VcSpecificModiferContext ctx);
}