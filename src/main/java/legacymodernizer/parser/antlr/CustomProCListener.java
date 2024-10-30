package legacymodernizer.parser.antlr;

import java.util.Stack;

import org.antlr.v4.runtime.TokenStream;

import legacymodernizer.parser.antlr.proc.ProCBaseListener;
import legacymodernizer.parser.antlr.proc.ProCParser;

public class CustomProCListener extends ProCBaseListener {
    @SuppressWarnings("unused")
    private TokenStream tokens;
    private Stack<Node> nodeStack = new Stack<>();
    private Node root = new Node("ROOT", 0, null); // 루트 노드

    public Node getRoot() {
        return root;
    }

    public CustomProCListener(TokenStream tokens) {
        this.tokens = tokens;
        nodeStack.push(root); // 초기 상태에서 루트 노드를 스택에 푸시
    }

    private void enterStatement(String statementType, int line) {
        Node currentNode = new Node(statementType, line, nodeStack.peek());
        nodeStack.push(currentNode);
        // System.out.println("Enter " + statementType + " Statement Line: " + line);
    }

    private void exitStatement(String statementType, int line) {
        Node node = nodeStack.pop();
        node.endLine = line;
        // System.out.println("Exit " + statementType + " Statement Line: " + line);
    }

    @Override
    public void enterValueList(ProCParser.ValueListContext ctx) {
        enterStatement("VALUE_LIST", ctx.getStart().getLine());
    }

    @Override
    public void exitValueList(ProCParser.ValueListContext ctx) {
        exitStatement("VALUE_LIST", ctx.getStop().getLine());
    }

    @Override
    public void enterSpecifierQualifierList(ProCParser.SpecifierQualifierListContext ctx) {
        enterStatement("SPECIFIER_QUALIFIER_LIST", ctx.getStart().getLine());
    }

    @Override
    public void exitSpecifierQualifierList(ProCParser.SpecifierQualifierListContext ctx) {
        exitStatement("SPECIFIER_QUALIFIER_LIST", ctx.getStop().getLine());
    }

    @Override
    public void enterStructDeclaratorList(ProCParser.StructDeclaratorListContext ctx) {
        enterStatement("STRUCT_DECLARATOR_LIST", ctx.getStart().getLine());
    }

    @Override
    public void exitStructDeclaratorList(ProCParser.StructDeclaratorListContext ctx) {
        exitStatement("STRUCT_DECLARATOR_LIST", ctx.getStop().getLine());
    }

    @Override
    public void enterAlignmentSpecifier(ProCParser.AlignmentSpecifierContext ctx) {
        enterStatement("ALIGNMENT_SPECIFIER", ctx.getStart().getLine());
    }

    @Override
    public void exitAlignmentSpecifier(ProCParser.AlignmentSpecifierContext ctx) {
        exitStatement("ALIGNMENT_SPECIFIER", ctx.getStop().getLine());
    }

    @Override
    public void enterDeclarator(ProCParser.DeclaratorContext ctx) {
        enterStatement("DECLARATOR", ctx.getStart().getLine());
    }

    @Override
    public void exitDeclarator(ProCParser.DeclaratorContext ctx) {
        exitStatement("DECLARATOR", ctx.getStop().getLine());
    }

    @Override
    public void enterDirectDeclarator(ProCParser.DirectDeclaratorContext ctx) {
        enterStatement("DIRECT_DECLARATOR", ctx.getStart().getLine());
    }

    @Override
    public void exitDirectDeclarator(ProCParser.DirectDeclaratorContext ctx) {
        exitStatement("DIRECT_DECLARATOR", ctx.getStop().getLine());
    }

    @Override
    public void enterVcSpecificModifer(ProCParser.VcSpecificModiferContext ctx) {
        enterStatement("VC_SPECIFIC_MODIFIER", ctx.getStart().getLine());
    }

    @Override
    public void exitVcSpecificModifer(ProCParser.VcSpecificModiferContext ctx) {
        exitStatement("VC_SPECIFIC_MODIFIER", ctx.getStop().getLine());
    }

    @Override
    public void enterParameterTypeList(ProCParser.ParameterTypeListContext ctx) {
        enterStatement("PARAMETER_TYPE_LIST", ctx.getStart().getLine());
    }

    @Override
    public void exitParameterTypeList(ProCParser.ParameterTypeListContext ctx) {
        exitStatement("PARAMETER_TYPE_LIST", ctx.getStop().getLine());
    }

    @Override
    public void enterParameterList(ProCParser.ParameterListContext ctx) {
        enterStatement("PARAMETER_LIST", ctx.getStart().getLine());
    }

    @Override
    public void exitParameterList(ProCParser.ParameterListContext ctx) {
        exitStatement("PARAMETER_LIST", ctx.getStop().getLine());
    }

    @Override
    public void enterParameterDeclaration(ProCParser.ParameterDeclarationContext ctx) {
        enterStatement("PARAMETER_DECLARATION", ctx.getStart().getLine());
    }

    @Override
    public void exitParameterDeclaration(ProCParser.ParameterDeclarationContext ctx) {
        exitStatement("PARAMETER_DECLARATION", ctx.getStop().getLine());
    }

    @Override
    public void enterIdentifierList(ProCParser.IdentifierListContext ctx) {
        enterStatement("IDENTIFIER_LIST", ctx.getStart().getLine());
    }

    @Override
    public void exitIdentifierList(ProCParser.IdentifierListContext ctx) {
        exitStatement("IDENTIFIER_LIST", ctx.getStop().getLine());
    }

    @Override
    public void enterTypeName(ProCParser.TypeNameContext ctx) {
        enterStatement("TYPE_NAME", ctx.getStart().getLine());
    }

    @Override
    public void exitTypeName(ProCParser.TypeNameContext ctx) {
        exitStatement("TYPE_NAME", ctx.getStop().getLine());
    }

    @Override
    public void enterDesignatorList(ProCParser.DesignatorListContext ctx) {
        enterStatement("DESIGNATOR_LIST", ctx.getStart().getLine());
    }

    @Override
    public void exitDesignatorList(ProCParser.DesignatorListContext ctx) {
        exitStatement("DESIGNATOR_LIST", ctx.getStop().getLine());
    }

    @Override
    public void enterDesignator(ProCParser.DesignatorContext ctx) {
        enterStatement("DESIGNATOR", ctx.getStart().getLine());
    }

    @Override
    public void exitDesignator(ProCParser.DesignatorContext ctx) {
        exitStatement("DESIGNATOR", ctx.getStop().getLine());
    }

    @Override
    public void enterStaticAssertDeclaration(ProCParser.StaticAssertDeclarationContext ctx) {
        enterStatement("STATIC_ASSERT_DECLARATION", ctx.getStart().getLine());
    }

    @Override
    public void exitStaticAssertDeclaration(ProCParser.StaticAssertDeclarationContext ctx) {
        exitStatement("STATIC_ASSERT_DECLARATION", ctx.getStop().getLine());
    }

    @Override
    public void enterStatement(ProCParser.StatementContext ctx) {
        enterStatement("STATEMENT", ctx.getStart().getLine());
    }

    @Override
    public void exitStatement(ProCParser.StatementContext ctx) {
        exitStatement("STATEMENT", ctx.getStop().getLine());
    }

    @Override
    public void enterLabeledStatement(ProCParser.LabeledStatementContext ctx) {
        enterStatement("LABELED_STATEMENT", ctx.getStart().getLine());
    }

    @Override
    public void exitLabeledStatement(ProCParser.LabeledStatementContext ctx) {
        exitStatement("LABELED_STATEMENT", ctx.getStop().getLine());
    }

    @Override
    public void enterCompoundStatement(ProCParser.CompoundStatementContext ctx) {
        enterStatement("COMPOUND_STATEMENT", ctx.getStart().getLine());
    }

    @Override
    public void exitCompoundStatement(ProCParser.CompoundStatementContext ctx) {
        exitStatement("COMPOUND_STATEMENT", ctx.getStop().getLine());
    }

    @Override
    public void enterBlockItemList(ProCParser.BlockItemListContext ctx) {
        enterStatement("BLOCK_ITEM_LIST", ctx.getStart().getLine());
    }

    @Override
    public void exitBlockItemList(ProCParser.BlockItemListContext ctx) {
        exitStatement("BLOCK_ITEM_LIST", ctx.getStop().getLine());
    }

    @Override
    public void enterIterationStatement(ProCParser.IterationStatementContext ctx) {
        enterStatement("ITERATION_STATEMENT", ctx.getStart().getLine());
    }

    @Override
    public void exitIterationStatement(ProCParser.IterationStatementContext ctx) {
        exitStatement("ITERATION_STATEMENT", ctx.getStop().getLine());
    }

    @Override
    public void enterForCondition(ProCParser.ForConditionContext ctx) {
        enterStatement("FOR_CONDITION", ctx.getStart().getLine());
    }

    @Override
    public void exitForCondition(ProCParser.ForConditionContext ctx) {
        exitStatement("FOR_CONDITION", ctx.getStop().getLine());
    }

    @Override
    public void enterForDeclaration(ProCParser.ForDeclarationContext ctx) {
        enterStatement("FOR_DECLARATION", ctx.getStart().getLine());
    }

    @Override
    public void exitForDeclaration(ProCParser.ForDeclarationContext ctx) {
        exitStatement("FOR_DECLARATION", ctx.getStop().getLine());
    }

    @Override
    public void enterForExpression(ProCParser.ForExpressionContext ctx) {
        enterStatement("FOR_EXPRESSION", ctx.getStart().getLine());
    }

    @Override
    public void exitForExpression(ProCParser.ForExpressionContext ctx) {
        exitStatement("FOR_EXPRESSION", ctx.getStop().getLine());
    }

    @Override
    public void enterJumpStatement(ProCParser.JumpStatementContext ctx) {
        enterStatement("JUMP_STATEMENT", ctx.getStart().getLine());
    }

    @Override
    public void exitJumpStatement(ProCParser.JumpStatementContext ctx) {
        exitStatement("JUMP_STATEMENT", ctx.getStop().getLine());
    }

    @Override
    public void enterCompilationUnit(ProCParser.CompilationUnitContext ctx) {
        enterStatement("COMPILATION_UNIT", ctx.getStart().getLine());
    }

    @Override
    public void exitCompilationUnit(ProCParser.CompilationUnitContext ctx) {
        exitStatement("COMPILATION_UNIT", ctx.getStop().getLine());
    }

    @Override
    public void enterTranslationUnit(ProCParser.TranslationUnitContext ctx) {
        enterStatement("TRANSLATION_UNIT", ctx.getStart().getLine());
    }

    @Override
    public void exitTranslationUnit(ProCParser.TranslationUnitContext ctx) {
        exitStatement("TRANSLATION_UNIT", ctx.getStop().getLine());
    }

    @Override
    public void enterExternalDeclaration(ProCParser.ExternalDeclarationContext ctx) {
        enterStatement("EXTERNAL_DECLARATION", ctx.getStart().getLine());
    }

    @Override
    public void exitExternalDeclaration(ProCParser.ExternalDeclarationContext ctx) {
        exitStatement("EXTERNAL_DECLARATION", ctx.getStop().getLine());
    }

    @Override
    public void enterFunctionDefinition(ProCParser.FunctionDefinitionContext ctx) {
        enterStatement("FUNCTION_DEFINITION", ctx.getStart().getLine());
    }

    @Override
    public void exitFunctionDefinition(ProCParser.FunctionDefinitionContext ctx) {
        exitStatement("FUNCTION_DEFINITION", ctx.getStop().getLine());
    }

    @Override
    public void enterDeclarationList(ProCParser.DeclarationListContext ctx) {
        enterStatement("DECLARATION_LIST", ctx.getStart().getLine());
    }

    @Override
    public void exitDeclarationList(ProCParser.DeclarationListContext ctx) {
        exitStatement("DECLARATION_LIST", ctx.getStop().getLine());
    }

    // 트리 구조를 출력하는 메서드 (디버깅 목적)
    public void printTree(Node node, String indent) {
        System.out.println(indent + node.type + " (" + node.startLine + ", " + node.endLine + ")");
        for (Node child : node.children) {
            printTree(child, indent + "  ");
        }
    }

    // 트리 구조 출력을 위한 메서드 호출 예시
    public void printStructure() {
        printTree(root, "");
    }
}

