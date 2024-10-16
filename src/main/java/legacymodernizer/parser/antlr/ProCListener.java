package legacymodernizer.parser.antlr;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ProCParser}.
 */
public interface ProCListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ProCParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryExpression(ProCParser.PrimaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryExpression(ProCParser.PrimaryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#genericSelection}.
	 * @param ctx the parse tree
	 */
	void enterGenericSelection(ProCParser.GenericSelectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#genericSelection}.
	 * @param ctx the parse tree
	 */
	void exitGenericSelection(ProCParser.GenericSelectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#genericAssocList}.
	 * @param ctx the parse tree
	 */
	void enterGenericAssocList(ProCParser.GenericAssocListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#genericAssocList}.
	 * @param ctx the parse tree
	 */
	void exitGenericAssocList(ProCParser.GenericAssocListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#genericAssociation}.
	 * @param ctx the parse tree
	 */
	void enterGenericAssociation(ProCParser.GenericAssociationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#genericAssociation}.
	 * @param ctx the parse tree
	 */
	void exitGenericAssociation(ProCParser.GenericAssociationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#postfixExpression}.
	 * @param ctx the parse tree
	 */
	void enterPostfixExpression(ProCParser.PostfixExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#postfixExpression}.
	 * @param ctx the parse tree
	 */
	void exitPostfixExpression(ProCParser.PostfixExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#argumentExpressionList}.
	 * @param ctx the parse tree
	 */
	void enterArgumentExpressionList(ProCParser.ArgumentExpressionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#argumentExpressionList}.
	 * @param ctx the parse tree
	 */
	void exitArgumentExpressionList(ProCParser.ArgumentExpressionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpression(ProCParser.UnaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpression(ProCParser.UnaryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#unaryOperator}.
	 * @param ctx the parse tree
	 */
	void enterUnaryOperator(ProCParser.UnaryOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#unaryOperator}.
	 * @param ctx the parse tree
	 */
	void exitUnaryOperator(ProCParser.UnaryOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#castExpression}.
	 * @param ctx the parse tree
	 */
	void enterCastExpression(ProCParser.CastExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#castExpression}.
	 * @param ctx the parse tree
	 */
	void exitCastExpression(ProCParser.CastExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicativeExpression(ProCParser.MultiplicativeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicativeExpression(ProCParser.MultiplicativeExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#additiveExpression}.
	 * @param ctx the parse tree
	 */
	void enterAdditiveExpression(ProCParser.AdditiveExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#additiveExpression}.
	 * @param ctx the parse tree
	 */
	void exitAdditiveExpression(ProCParser.AdditiveExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#shiftExpression}.
	 * @param ctx the parse tree
	 */
	void enterShiftExpression(ProCParser.ShiftExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#shiftExpression}.
	 * @param ctx the parse tree
	 */
	void exitShiftExpression(ProCParser.ShiftExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#relationalExpression}.
	 * @param ctx the parse tree
	 */
	void enterRelationalExpression(ProCParser.RelationalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#relationalExpression}.
	 * @param ctx the parse tree
	 */
	void exitRelationalExpression(ProCParser.RelationalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#equalityExpression}.
	 * @param ctx the parse tree
	 */
	void enterEqualityExpression(ProCParser.EqualityExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#equalityExpression}.
	 * @param ctx the parse tree
	 */
	void exitEqualityExpression(ProCParser.EqualityExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#andExpression}.
	 * @param ctx the parse tree
	 */
	void enterAndExpression(ProCParser.AndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#andExpression}.
	 * @param ctx the parse tree
	 */
	void exitAndExpression(ProCParser.AndExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#exclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void enterExclusiveOrExpression(ProCParser.ExclusiveOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#exclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void exitExclusiveOrExpression(ProCParser.ExclusiveOrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#inclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void enterInclusiveOrExpression(ProCParser.InclusiveOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#inclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void exitInclusiveOrExpression(ProCParser.InclusiveOrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#logicalAndExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalAndExpression(ProCParser.LogicalAndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#logicalAndExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalAndExpression(ProCParser.LogicalAndExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#logicalOrExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalOrExpression(ProCParser.LogicalOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#logicalOrExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalOrExpression(ProCParser.LogicalOrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#conditionalExpression}.
	 * @param ctx the parse tree
	 */
	void enterConditionalExpression(ProCParser.ConditionalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#conditionalExpression}.
	 * @param ctx the parse tree
	 */
	void exitConditionalExpression(ProCParser.ConditionalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentExpression(ProCParser.AssignmentExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentExpression(ProCParser.AssignmentExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#assignmentOperator}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentOperator(ProCParser.AssignmentOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#assignmentOperator}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentOperator(ProCParser.AssignmentOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(ProCParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(ProCParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#constantExpression}.
	 * @param ctx the parse tree
	 */
	void enterConstantExpression(ProCParser.ConstantExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#constantExpression}.
	 * @param ctx the parse tree
	 */
	void exitConstantExpression(ProCParser.ConstantExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#declaration}.
	 * @param ctx the parse tree
	 */
	void enterDeclaration(ProCParser.DeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#declaration}.
	 * @param ctx the parse tree
	 */
	void exitDeclaration(ProCParser.DeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#declarationSpecifiers}.
	 * @param ctx the parse tree
	 */
	void enterDeclarationSpecifiers(ProCParser.DeclarationSpecifiersContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#declarationSpecifiers}.
	 * @param ctx the parse tree
	 */
	void exitDeclarationSpecifiers(ProCParser.DeclarationSpecifiersContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#declarationSpecifiers2}.
	 * @param ctx the parse tree
	 */
	void enterDeclarationSpecifiers2(ProCParser.DeclarationSpecifiers2Context ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#declarationSpecifiers2}.
	 * @param ctx the parse tree
	 */
	void exitDeclarationSpecifiers2(ProCParser.DeclarationSpecifiers2Context ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#declarationSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterDeclarationSpecifier(ProCParser.DeclarationSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#declarationSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitDeclarationSpecifier(ProCParser.DeclarationSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#initDeclaratorList}.
	 * @param ctx the parse tree
	 */
	void enterInitDeclaratorList(ProCParser.InitDeclaratorListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#initDeclaratorList}.
	 * @param ctx the parse tree
	 */
	void exitInitDeclaratorList(ProCParser.InitDeclaratorListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#initDeclarator}.
	 * @param ctx the parse tree
	 */
	void enterInitDeclarator(ProCParser.InitDeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#initDeclarator}.
	 * @param ctx the parse tree
	 */
	void exitInitDeclarator(ProCParser.InitDeclaratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#storageClassSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterStorageClassSpecifier(ProCParser.StorageClassSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#storageClassSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitStorageClassSpecifier(ProCParser.StorageClassSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#typeSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterTypeSpecifier(ProCParser.TypeSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#typeSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitTypeSpecifier(ProCParser.TypeSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#structOrUnionSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterStructOrUnionSpecifier(ProCParser.StructOrUnionSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#structOrUnionSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitStructOrUnionSpecifier(ProCParser.StructOrUnionSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#structOrUnion}.
	 * @param ctx the parse tree
	 */
	void enterStructOrUnion(ProCParser.StructOrUnionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#structOrUnion}.
	 * @param ctx the parse tree
	 */
	void exitStructOrUnion(ProCParser.StructOrUnionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#structDeclarationList}.
	 * @param ctx the parse tree
	 */
	void enterStructDeclarationList(ProCParser.StructDeclarationListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#structDeclarationList}.
	 * @param ctx the parse tree
	 */
	void exitStructDeclarationList(ProCParser.StructDeclarationListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#structDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterStructDeclaration(ProCParser.StructDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#structDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitStructDeclaration(ProCParser.StructDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#specifierQualifierList}.
	 * @param ctx the parse tree
	 */
	void enterSpecifierQualifierList(ProCParser.SpecifierQualifierListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#specifierQualifierList}.
	 * @param ctx the parse tree
	 */
	void exitSpecifierQualifierList(ProCParser.SpecifierQualifierListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#structDeclaratorList}.
	 * @param ctx the parse tree
	 */
	void enterStructDeclaratorList(ProCParser.StructDeclaratorListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#structDeclaratorList}.
	 * @param ctx the parse tree
	 */
	void exitStructDeclaratorList(ProCParser.StructDeclaratorListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#structDeclarator}.
	 * @param ctx the parse tree
	 */
	void enterStructDeclarator(ProCParser.StructDeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#structDeclarator}.
	 * @param ctx the parse tree
	 */
	void exitStructDeclarator(ProCParser.StructDeclaratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#enumSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterEnumSpecifier(ProCParser.EnumSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#enumSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitEnumSpecifier(ProCParser.EnumSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#enumeratorList}.
	 * @param ctx the parse tree
	 */
	void enterEnumeratorList(ProCParser.EnumeratorListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#enumeratorList}.
	 * @param ctx the parse tree
	 */
	void exitEnumeratorList(ProCParser.EnumeratorListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#enumerator}.
	 * @param ctx the parse tree
	 */
	void enterEnumerator(ProCParser.EnumeratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#enumerator}.
	 * @param ctx the parse tree
	 */
	void exitEnumerator(ProCParser.EnumeratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#enumerationConstant}.
	 * @param ctx the parse tree
	 */
	void enterEnumerationConstant(ProCParser.EnumerationConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#enumerationConstant}.
	 * @param ctx the parse tree
	 */
	void exitEnumerationConstant(ProCParser.EnumerationConstantContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#atomicTypeSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterAtomicTypeSpecifier(ProCParser.AtomicTypeSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#atomicTypeSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitAtomicTypeSpecifier(ProCParser.AtomicTypeSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#typeQualifier}.
	 * @param ctx the parse tree
	 */
	void enterTypeQualifier(ProCParser.TypeQualifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#typeQualifier}.
	 * @param ctx the parse tree
	 */
	void exitTypeQualifier(ProCParser.TypeQualifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#functionSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterFunctionSpecifier(ProCParser.FunctionSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#functionSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitFunctionSpecifier(ProCParser.FunctionSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#alignmentSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterAlignmentSpecifier(ProCParser.AlignmentSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#alignmentSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitAlignmentSpecifier(ProCParser.AlignmentSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#declarator}.
	 * @param ctx the parse tree
	 */
	void enterDeclarator(ProCParser.DeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#declarator}.
	 * @param ctx the parse tree
	 */
	void exitDeclarator(ProCParser.DeclaratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#directDeclarator}.
	 * @param ctx the parse tree
	 */
	void enterDirectDeclarator(ProCParser.DirectDeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#directDeclarator}.
	 * @param ctx the parse tree
	 */
	void exitDirectDeclarator(ProCParser.DirectDeclaratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#vcSpecificModifer}.
	 * @param ctx the parse tree
	 */
	void enterVcSpecificModifer(ProCParser.VcSpecificModiferContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#vcSpecificModifer}.
	 * @param ctx the parse tree
	 */
	void exitVcSpecificModifer(ProCParser.VcSpecificModiferContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#gccDeclaratorExtension}.
	 * @param ctx the parse tree
	 */
	void enterGccDeclaratorExtension(ProCParser.GccDeclaratorExtensionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#gccDeclaratorExtension}.
	 * @param ctx the parse tree
	 */
	void exitGccDeclaratorExtension(ProCParser.GccDeclaratorExtensionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#gccAttributeSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterGccAttributeSpecifier(ProCParser.GccAttributeSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#gccAttributeSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitGccAttributeSpecifier(ProCParser.GccAttributeSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#gccAttributeList}.
	 * @param ctx the parse tree
	 */
	void enterGccAttributeList(ProCParser.GccAttributeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#gccAttributeList}.
	 * @param ctx the parse tree
	 */
	void exitGccAttributeList(ProCParser.GccAttributeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#gccAttribute}.
	 * @param ctx the parse tree
	 */
	void enterGccAttribute(ProCParser.GccAttributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#gccAttribute}.
	 * @param ctx the parse tree
	 */
	void exitGccAttribute(ProCParser.GccAttributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#nestedParenthesesBlock}.
	 * @param ctx the parse tree
	 */
	void enterNestedParenthesesBlock(ProCParser.NestedParenthesesBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#nestedParenthesesBlock}.
	 * @param ctx the parse tree
	 */
	void exitNestedParenthesesBlock(ProCParser.NestedParenthesesBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#pointer}.
	 * @param ctx the parse tree
	 */
	void enterPointer(ProCParser.PointerContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#pointer}.
	 * @param ctx the parse tree
	 */
	void exitPointer(ProCParser.PointerContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#typeQualifierList}.
	 * @param ctx the parse tree
	 */
	void enterTypeQualifierList(ProCParser.TypeQualifierListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#typeQualifierList}.
	 * @param ctx the parse tree
	 */
	void exitTypeQualifierList(ProCParser.TypeQualifierListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#parameterTypeList}.
	 * @param ctx the parse tree
	 */
	void enterParameterTypeList(ProCParser.ParameterTypeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#parameterTypeList}.
	 * @param ctx the parse tree
	 */
	void exitParameterTypeList(ProCParser.ParameterTypeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#parameterList}.
	 * @param ctx the parse tree
	 */
	void enterParameterList(ProCParser.ParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#parameterList}.
	 * @param ctx the parse tree
	 */
	void exitParameterList(ProCParser.ParameterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#parameterDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterParameterDeclaration(ProCParser.ParameterDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#parameterDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitParameterDeclaration(ProCParser.ParameterDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#identifierList}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierList(ProCParser.IdentifierListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#identifierList}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierList(ProCParser.IdentifierListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#typeName}.
	 * @param ctx the parse tree
	 */
	void enterTypeName(ProCParser.TypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#typeName}.
	 * @param ctx the parse tree
	 */
	void exitTypeName(ProCParser.TypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#abstractDeclarator}.
	 * @param ctx the parse tree
	 */
	void enterAbstractDeclarator(ProCParser.AbstractDeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#abstractDeclarator}.
	 * @param ctx the parse tree
	 */
	void exitAbstractDeclarator(ProCParser.AbstractDeclaratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#directAbstractDeclarator}.
	 * @param ctx the parse tree
	 */
	void enterDirectAbstractDeclarator(ProCParser.DirectAbstractDeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#directAbstractDeclarator}.
	 * @param ctx the parse tree
	 */
	void exitDirectAbstractDeclarator(ProCParser.DirectAbstractDeclaratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#typedefName}.
	 * @param ctx the parse tree
	 */
	void enterTypedefName(ProCParser.TypedefNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#typedefName}.
	 * @param ctx the parse tree
	 */
	void exitTypedefName(ProCParser.TypedefNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#initializer}.
	 * @param ctx the parse tree
	 */
	void enterInitializer(ProCParser.InitializerContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#initializer}.
	 * @param ctx the parse tree
	 */
	void exitInitializer(ProCParser.InitializerContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#initializerList}.
	 * @param ctx the parse tree
	 */
	void enterInitializerList(ProCParser.InitializerListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#initializerList}.
	 * @param ctx the parse tree
	 */
	void exitInitializerList(ProCParser.InitializerListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#designation}.
	 * @param ctx the parse tree
	 */
	void enterDesignation(ProCParser.DesignationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#designation}.
	 * @param ctx the parse tree
	 */
	void exitDesignation(ProCParser.DesignationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#designatorList}.
	 * @param ctx the parse tree
	 */
	void enterDesignatorList(ProCParser.DesignatorListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#designatorList}.
	 * @param ctx the parse tree
	 */
	void exitDesignatorList(ProCParser.DesignatorListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#designator}.
	 * @param ctx the parse tree
	 */
	void enterDesignator(ProCParser.DesignatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#designator}.
	 * @param ctx the parse tree
	 */
	void exitDesignator(ProCParser.DesignatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#staticAssertDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterStaticAssertDeclaration(ProCParser.StaticAssertDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#staticAssertDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitStaticAssertDeclaration(ProCParser.StaticAssertDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(ProCParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(ProCParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#labeledStatement}.
	 * @param ctx the parse tree
	 */
	void enterLabeledStatement(ProCParser.LabeledStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#labeledStatement}.
	 * @param ctx the parse tree
	 */
	void exitLabeledStatement(ProCParser.LabeledStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#compoundStatement}.
	 * @param ctx the parse tree
	 */
	void enterCompoundStatement(ProCParser.CompoundStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#compoundStatement}.
	 * @param ctx the parse tree
	 */
	void exitCompoundStatement(ProCParser.CompoundStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#blockItemList}.
	 * @param ctx the parse tree
	 */
	void enterBlockItemList(ProCParser.BlockItemListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#blockItemList}.
	 * @param ctx the parse tree
	 */
	void exitBlockItemList(ProCParser.BlockItemListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#blockItem}.
	 * @param ctx the parse tree
	 */
	void enterBlockItem(ProCParser.BlockItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#blockItem}.
	 * @param ctx the parse tree
	 */
	void exitBlockItem(ProCParser.BlockItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#expressionStatement}.
	 * @param ctx the parse tree
	 */
	void enterExpressionStatement(ProCParser.ExpressionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#expressionStatement}.
	 * @param ctx the parse tree
	 */
	void exitExpressionStatement(ProCParser.ExpressionStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#selectionStatement}.
	 * @param ctx the parse tree
	 */
	void enterSelectionStatement(ProCParser.SelectionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#selectionStatement}.
	 * @param ctx the parse tree
	 */
	void exitSelectionStatement(ProCParser.SelectionStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#iterationStatement}.
	 * @param ctx the parse tree
	 */
	void enterIterationStatement(ProCParser.IterationStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#iterationStatement}.
	 * @param ctx the parse tree
	 */
	void exitIterationStatement(ProCParser.IterationStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#forCondition}.
	 * @param ctx the parse tree
	 */
	void enterForCondition(ProCParser.ForConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#forCondition}.
	 * @param ctx the parse tree
	 */
	void exitForCondition(ProCParser.ForConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#forDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterForDeclaration(ProCParser.ForDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#forDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitForDeclaration(ProCParser.ForDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#forExpression}.
	 * @param ctx the parse tree
	 */
	void enterForExpression(ProCParser.ForExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#forExpression}.
	 * @param ctx the parse tree
	 */
	void exitForExpression(ProCParser.ForExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#jumpStatement}.
	 * @param ctx the parse tree
	 */
	void enterJumpStatement(ProCParser.JumpStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#jumpStatement}.
	 * @param ctx the parse tree
	 */
	void exitJumpStatement(ProCParser.JumpStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void enterCompilationUnit(ProCParser.CompilationUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void exitCompilationUnit(ProCParser.CompilationUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#translationUnit}.
	 * @param ctx the parse tree
	 */
	void enterTranslationUnit(ProCParser.TranslationUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#translationUnit}.
	 * @param ctx the parse tree
	 */
	void exitTranslationUnit(ProCParser.TranslationUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#externalDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterExternalDeclaration(ProCParser.ExternalDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#externalDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitExternalDeclaration(ProCParser.ExternalDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#functionDefinition}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDefinition(ProCParser.FunctionDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#functionDefinition}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDefinition(ProCParser.FunctionDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#declarationList}.
	 * @param ctx the parse tree
	 */
	void enterDeclarationList(ProCParser.DeclarationListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#declarationList}.
	 * @param ctx the parse tree
	 */
	void exitDeclarationList(ProCParser.DeclarationListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#proCStatement}.
	 * @param ctx the parse tree
	 */
	void enterProCStatement(ProCParser.ProCStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#proCStatement}.
	 * @param ctx the parse tree
	 */
	void exitProCStatement(ProCParser.ProCStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#execSqlStatement}.
	 * @param ctx the parse tree
	 */
	void enterExecSqlStatement(ProCParser.ExecSqlStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#execSqlStatement}.
	 * @param ctx the parse tree
	 */
	void exitExecSqlStatement(ProCParser.ExecSqlStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#sqlOperation}.
	 * @param ctx the parse tree
	 */
	void enterSqlOperation(ProCParser.SqlOperationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#sqlOperation}.
	 * @param ctx the parse tree
	 */
	void exitSqlOperation(ProCParser.SqlOperationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#declareCursor}.
	 * @param ctx the parse tree
	 */
	void enterDeclareCursor(ProCParser.DeclareCursorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#declareCursor}.
	 * @param ctx the parse tree
	 */
	void exitDeclareCursor(ProCParser.DeclareCursorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#openCursor}.
	 * @param ctx the parse tree
	 */
	void enterOpenCursor(ProCParser.OpenCursorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#openCursor}.
	 * @param ctx the parse tree
	 */
	void exitOpenCursor(ProCParser.OpenCursorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#fetchCursor}.
	 * @param ctx the parse tree
	 */
	void enterFetchCursor(ProCParser.FetchCursorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#fetchCursor}.
	 * @param ctx the parse tree
	 */
	void exitFetchCursor(ProCParser.FetchCursorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#closeCursor}.
	 * @param ctx the parse tree
	 */
	void enterCloseCursor(ProCParser.CloseCursorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#closeCursor}.
	 * @param ctx the parse tree
	 */
	void exitCloseCursor(ProCParser.CloseCursorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#sqlInsert}.
	 * @param ctx the parse tree
	 */
	void enterSqlInsert(ProCParser.SqlInsertContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#sqlInsert}.
	 * @param ctx the parse tree
	 */
	void exitSqlInsert(ProCParser.SqlInsertContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#sqlUpdate}.
	 * @param ctx the parse tree
	 */
	void enterSqlUpdate(ProCParser.SqlUpdateContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#sqlUpdate}.
	 * @param ctx the parse tree
	 */
	void exitSqlUpdate(ProCParser.SqlUpdateContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#sqlDelete}.
	 * @param ctx the parse tree
	 */
	void enterSqlDelete(ProCParser.SqlDeleteContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#sqlDelete}.
	 * @param ctx the parse tree
	 */
	void exitSqlDelete(ProCParser.SqlDeleteContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#sqlSelect}.
	 * @param ctx the parse tree
	 */
	void enterSqlSelect(ProCParser.SqlSelectContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#sqlSelect}.
	 * @param ctx the parse tree
	 */
	void exitSqlSelect(ProCParser.SqlSelectContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#hostVariableDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterHostVariableDeclaration(ProCParser.HostVariableDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#hostVariableDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitHostVariableDeclaration(ProCParser.HostVariableDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#hostVariableList}.
	 * @param ctx the parse tree
	 */
	void enterHostVariableList(ProCParser.HostVariableListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#hostVariableList}.
	 * @param ctx the parse tree
	 */
	void exitHostVariableList(ProCParser.HostVariableListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#tableName}.
	 * @param ctx the parse tree
	 */
	void enterTableName(ProCParser.TableNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#tableName}.
	 * @param ctx the parse tree
	 */
	void exitTableName(ProCParser.TableNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#columnList}.
	 * @param ctx the parse tree
	 */
	void enterColumnList(ProCParser.ColumnListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#columnList}.
	 * @param ctx the parse tree
	 */
	void exitColumnList(ProCParser.ColumnListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#valueList}.
	 * @param ctx the parse tree
	 */
	void enterValueList(ProCParser.ValueListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#valueList}.
	 * @param ctx the parse tree
	 */
	void exitValueList(ProCParser.ValueListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#updateList}.
	 * @param ctx the parse tree
	 */
	void enterUpdateList(ProCParser.UpdateListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#updateList}.
	 * @param ctx the parse tree
	 */
	void exitUpdateList(ProCParser.UpdateListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#selectList}.
	 * @param ctx the parse tree
	 */
	void enterSelectList(ProCParser.SelectListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#selectList}.
	 * @param ctx the parse tree
	 */
	void exitSelectList(ProCParser.SelectListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#condition}.
	 * @param ctx the parse tree
	 */
	void enterCondition(ProCParser.ConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#condition}.
	 * @param ctx the parse tree
	 */
	void exitCondition(ProCParser.ConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProCParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(ProCParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProCParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(ProCParser.ValueContext ctx);
}