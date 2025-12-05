package legacymodernizer.parser.antlr.java;

import java.util.Stack;

import org.antlr.v4.runtime.TokenStream;

import legacymodernizer.parser.antlr.Node;

/**
 * Java 파일 분석을 위한 커스텀 리스너
 * - 클래스/인터페이스/메서드/필드 구조 추출
 * - 상속/구현 관계 추출
 * - 메서드 호출, 객체 생성 추출
 * - 결과: type, startLine, endLine, children 트리 구조
 */
public class CustomJavaListener extends Java20ParserBaseListener {
    
    @SuppressWarnings("unused")
    private TokenStream tokens;
    private Stack<Node> nodeStack = new Stack<>();
    private Node root = new Node("FILE", 0, null);
    
    public Node getRoot() {
        return root;
    }
    
    public CustomJavaListener(TokenStream tokens) {
        this.tokens = tokens;
        nodeStack.push(root);
    }
    
    private void enterStatement(String statementType, int line) {
        Node currentNode = new Node(statementType, line, nodeStack.peek());
        nodeStack.push(currentNode);
    }
    
    private void exitStatement(String statementType, int line) {
        if (!nodeStack.isEmpty() && nodeStack.peek().type.equals(statementType)) {
            Node node = nodeStack.pop();
            node.endLine = line;
        }
    }
    
    // ========================================
    // 패키지/임포트
    // ========================================
    
    @Override
    public void enterPackageDeclaration(Java20Parser.PackageDeclarationContext ctx) {
        enterStatement("PACKAGE", ctx.getStart().getLine());
    }
    
    @Override
    public void exitPackageDeclaration(Java20Parser.PackageDeclarationContext ctx) {
        exitStatement("PACKAGE", ctx.getStop().getLine());
    }
    
    @Override
    public void enterImportDeclaration(Java20Parser.ImportDeclarationContext ctx) {
        enterStatement("IMPORT", ctx.getStart().getLine());
    }
    
    @Override
    public void exitImportDeclaration(Java20Parser.ImportDeclarationContext ctx) {
        exitStatement("IMPORT", ctx.getStop().getLine());
    }
    
    // ========================================
    // 클래스/인터페이스/열거형/레코드
    // ========================================
    
    @Override
    public void enterNormalClassDeclaration(Java20Parser.NormalClassDeclarationContext ctx) {
        enterStatement("CLASS", ctx.getStart().getLine());
    }
    
    @Override
    public void exitNormalClassDeclaration(Java20Parser.NormalClassDeclarationContext ctx) {
        exitStatement("CLASS", ctx.getStop().getLine());
    }
    
    @Override
    public void enterEnumDeclaration(Java20Parser.EnumDeclarationContext ctx) {
        enterStatement("ENUM", ctx.getStart().getLine());
    }
    
    @Override
    public void exitEnumDeclaration(Java20Parser.EnumDeclarationContext ctx) {
        exitStatement("ENUM", ctx.getStop().getLine());
    }
    
    @Override
    public void enterRecordDeclaration(Java20Parser.RecordDeclarationContext ctx) {
        enterStatement("RECORD", ctx.getStart().getLine());
    }
    
    @Override
    public void exitRecordDeclaration(Java20Parser.RecordDeclarationContext ctx) {
        exitStatement("RECORD", ctx.getStop().getLine());
    }
    
    @Override
    public void enterNormalInterfaceDeclaration(Java20Parser.NormalInterfaceDeclarationContext ctx) {
        enterStatement("INTERFACE", ctx.getStart().getLine());
    }
    
    @Override
    public void exitNormalInterfaceDeclaration(Java20Parser.NormalInterfaceDeclarationContext ctx) {
        exitStatement("INTERFACE", ctx.getStop().getLine());
    }
    
    @Override
    public void enterAnnotationInterfaceDeclaration(Java20Parser.AnnotationInterfaceDeclarationContext ctx) {
        enterStatement("ANNOTATION_TYPE", ctx.getStart().getLine());
    }
    
    @Override
    public void exitAnnotationInterfaceDeclaration(Java20Parser.AnnotationInterfaceDeclarationContext ctx) {
        exitStatement("ANNOTATION_TYPE", ctx.getStop().getLine());
    }
    
    // ========================================
    // 상속/구현 관계
    // ========================================
    
    @Override
    public void enterClassExtends(Java20Parser.ClassExtendsContext ctx) {
        enterStatement("EXTENDS", ctx.getStart().getLine());
    }
    
    @Override
    public void exitClassExtends(Java20Parser.ClassExtendsContext ctx) {
        exitStatement("EXTENDS", ctx.getStop().getLine());
    }
    
    @Override
    public void enterClassImplements(Java20Parser.ClassImplementsContext ctx) {
        enterStatement("IMPLEMENTS", ctx.getStart().getLine());
    }
    
    @Override
    public void exitClassImplements(Java20Parser.ClassImplementsContext ctx) {
        exitStatement("IMPLEMENTS", ctx.getStop().getLine());
    }
    
    @Override
    public void enterInterfaceExtends(Java20Parser.InterfaceExtendsContext ctx) {
        enterStatement("EXTENDS", ctx.getStart().getLine());
    }
    
    @Override
    public void exitInterfaceExtends(Java20Parser.InterfaceExtendsContext ctx) {
        exitStatement("EXTENDS", ctx.getStop().getLine());
    }
    
    // ========================================
    // 어노테이션 사용
    // ========================================
    
    @Override
    public void enterAnnotation(Java20Parser.AnnotationContext ctx) {
        enterStatement("ANNOTATION", ctx.getStart().getLine());
    }
    
    @Override
    public void exitAnnotation(Java20Parser.AnnotationContext ctx) {
        exitStatement("ANNOTATION", ctx.getStop().getLine());
    }
    
    // ========================================
    // 필드/변수
    // ========================================
    
    @Override
    public void enterFieldDeclaration(Java20Parser.FieldDeclarationContext ctx) {
        enterStatement("FIELD", ctx.getStart().getLine());
    }
    
    @Override
    public void exitFieldDeclaration(Java20Parser.FieldDeclarationContext ctx) {
        exitStatement("FIELD", ctx.getStop().getLine());
    }
    
    @Override
    public void enterLocalVariableDeclaration(Java20Parser.LocalVariableDeclarationContext ctx) {
        enterStatement("LOCAL_VARIABLE", ctx.getStart().getLine());
    }
    
    @Override
    public void exitLocalVariableDeclaration(Java20Parser.LocalVariableDeclarationContext ctx) {
        exitStatement("LOCAL_VARIABLE", ctx.getStop().getLine());
    }
    
    // ========================================
    // 메서드/생성자
    // ========================================
    
    @Override
    public void enterMethodDeclaration(Java20Parser.MethodDeclarationContext ctx) {
        enterStatement("METHOD", ctx.getStart().getLine());
    }
    
    @Override
    public void exitMethodDeclaration(Java20Parser.MethodDeclarationContext ctx) {
        exitStatement("METHOD", ctx.getStop().getLine());
    }
    
    @Override
    public void enterConstructorDeclaration(Java20Parser.ConstructorDeclarationContext ctx) {
        enterStatement("CONSTRUCTOR", ctx.getStart().getLine());
    }
    
    @Override
    public void exitConstructorDeclaration(Java20Parser.ConstructorDeclarationContext ctx) {
        exitStatement("CONSTRUCTOR", ctx.getStop().getLine());
    }
    
    @Override
    public void enterInterfaceMethodDeclaration(Java20Parser.InterfaceMethodDeclarationContext ctx) {
        enterStatement("METHOD", ctx.getStart().getLine());
    }
    
    @Override
    public void exitInterfaceMethodDeclaration(Java20Parser.InterfaceMethodDeclarationContext ctx) {
        exitStatement("METHOD", ctx.getStop().getLine());
    }
    
    // ========================================
    // Static/Instance 초기화 블록
    // ========================================
    
    @Override
    public void enterStaticInitializer(Java20Parser.StaticInitializerContext ctx) {
        enterStatement("STATIC_INIT", ctx.getStart().getLine());
    }
    
    @Override
    public void exitStaticInitializer(Java20Parser.StaticInitializerContext ctx) {
        exitStatement("STATIC_INIT", ctx.getStop().getLine());
    }
    
    @Override
    public void enterInstanceInitializer(Java20Parser.InstanceInitializerContext ctx) {
        enterStatement("INSTANCE_INIT", ctx.getStart().getLine());
    }
    
    @Override
    public void exitInstanceInitializer(Java20Parser.InstanceInitializerContext ctx) {
        exitStatement("INSTANCE_INIT", ctx.getStop().getLine());
    }
    
    // ========================================
    // 메서드 호출/참조
    // ========================================
    
    @Override
    public void enterMethodInvocation(Java20Parser.MethodInvocationContext ctx) {
        enterStatement("METHOD_CALL", ctx.getStart().getLine());
    }
    
    @Override
    public void exitMethodInvocation(Java20Parser.MethodInvocationContext ctx) {
        exitStatement("METHOD_CALL", ctx.getStop().getLine());
    }
    
    @Override
    public void enterMethodReference(Java20Parser.MethodReferenceContext ctx) {
        enterStatement("METHOD_REFERENCE", ctx.getStart().getLine());
    }
    
    @Override
    public void exitMethodReference(Java20Parser.MethodReferenceContext ctx) {
        exitStatement("METHOD_REFERENCE", ctx.getStop().getLine());
    }
    
    // ========================================
    // 객체/배열 생성
    // ========================================
    
    @Override
    public void enterClassInstanceCreationExpression(Java20Parser.ClassInstanceCreationExpressionContext ctx) {
        enterStatement("NEW_INSTANCE", ctx.getStart().getLine());
    }
    
    @Override
    public void exitClassInstanceCreationExpression(Java20Parser.ClassInstanceCreationExpressionContext ctx) {
        exitStatement("NEW_INSTANCE", ctx.getStop().getLine());
    }
    
    @Override
    public void enterArrayCreationExpression(Java20Parser.ArrayCreationExpressionContext ctx) {
        enterStatement("NEW_ARRAY", ctx.getStart().getLine());
    }
    
    @Override
    public void exitArrayCreationExpression(Java20Parser.ArrayCreationExpressionContext ctx) {
        exitStatement("NEW_ARRAY", ctx.getStop().getLine());
    }
    
    // ========================================
    // 제어 흐름
    // ========================================
    
    @Override
    public void enterIfThenStatement(Java20Parser.IfThenStatementContext ctx) {
        enterStatement("IF", ctx.getStart().getLine());
    }
    
    @Override
    public void exitIfThenStatement(Java20Parser.IfThenStatementContext ctx) {
        exitStatement("IF", ctx.getStop().getLine());
    }
    
    @Override
    public void enterIfThenElseStatement(Java20Parser.IfThenElseStatementContext ctx) {
        enterStatement("IF_ELSE", ctx.getStart().getLine());
    }
    
    @Override
    public void exitIfThenElseStatement(Java20Parser.IfThenElseStatementContext ctx) {
        exitStatement("IF_ELSE", ctx.getStop().getLine());
    }
    
    @Override
    public void enterForStatement(Java20Parser.ForStatementContext ctx) {
        enterStatement("FOR", ctx.getStart().getLine());
    }
    
    @Override
    public void exitForStatement(Java20Parser.ForStatementContext ctx) {
        exitStatement("FOR", ctx.getStop().getLine());
    }
    
    @Override
    public void enterWhileStatement(Java20Parser.WhileStatementContext ctx) {
        enterStatement("WHILE", ctx.getStart().getLine());
    }
    
    @Override
    public void exitWhileStatement(Java20Parser.WhileStatementContext ctx) {
        exitStatement("WHILE", ctx.getStop().getLine());
    }
    
    @Override
    public void enterDoStatement(Java20Parser.DoStatementContext ctx) {
        enterStatement("DO_WHILE", ctx.getStart().getLine());
    }
    
    @Override
    public void exitDoStatement(Java20Parser.DoStatementContext ctx) {
        exitStatement("DO_WHILE", ctx.getStop().getLine());
    }
    
    @Override
    public void enterSwitchStatement(Java20Parser.SwitchStatementContext ctx) {
        enterStatement("SWITCH", ctx.getStart().getLine());
    }
    
    @Override
    public void exitSwitchStatement(Java20Parser.SwitchStatementContext ctx) {
        exitStatement("SWITCH", ctx.getStop().getLine());
    }
    
    @Override
    public void enterSwitchExpression(Java20Parser.SwitchExpressionContext ctx) {
        enterStatement("SWITCH_EXPR", ctx.getStart().getLine());
    }
    
    @Override
    public void exitSwitchExpression(Java20Parser.SwitchExpressionContext ctx) {
        exitStatement("SWITCH_EXPR", ctx.getStop().getLine());
    }
    
    // ========================================
    // 예외 처리
    // ========================================
    
    @Override
    public void enterTryStatement(Java20Parser.TryStatementContext ctx) {
        enterStatement("TRY", ctx.getStart().getLine());
    }
    
    @Override
    public void exitTryStatement(Java20Parser.TryStatementContext ctx) {
        exitStatement("TRY", ctx.getStop().getLine());
    }
    
    @Override
    public void enterCatchClause(Java20Parser.CatchClauseContext ctx) {
        enterStatement("CATCH", ctx.getStart().getLine());
    }
    
    @Override
    public void exitCatchClause(Java20Parser.CatchClauseContext ctx) {
        exitStatement("CATCH", ctx.getStop().getLine());
    }
    
    @Override
    public void enterFinallyBlock(Java20Parser.FinallyBlockContext ctx) {
        enterStatement("FINALLY", ctx.getStart().getLine());
    }
    
    @Override
    public void exitFinallyBlock(Java20Parser.FinallyBlockContext ctx) {
        exitStatement("FINALLY", ctx.getStop().getLine());
    }
    
    @Override
    public void enterThrowStatement(Java20Parser.ThrowStatementContext ctx) {
        enterStatement("THROW", ctx.getStart().getLine());
    }
    
    @Override
    public void exitThrowStatement(Java20Parser.ThrowStatementContext ctx) {
        exitStatement("THROW", ctx.getStop().getLine());
    }
    
    // ========================================
    // 점프 문
    // ========================================
    
    @Override
    public void enterReturnStatement(Java20Parser.ReturnStatementContext ctx) {
        enterStatement("RETURN", ctx.getStart().getLine());
    }
    
    @Override
    public void exitReturnStatement(Java20Parser.ReturnStatementContext ctx) {
        exitStatement("RETURN", ctx.getStop().getLine());
    }
    
    @Override
    public void enterBreakStatement(Java20Parser.BreakStatementContext ctx) {
        enterStatement("BREAK", ctx.getStart().getLine());
    }
    
    @Override
    public void exitBreakStatement(Java20Parser.BreakStatementContext ctx) {
        exitStatement("BREAK", ctx.getStop().getLine());
    }
    
    @Override
    public void enterContinueStatement(Java20Parser.ContinueStatementContext ctx) {
        enterStatement("CONTINUE", ctx.getStart().getLine());
    }
    
    @Override
    public void exitContinueStatement(Java20Parser.ContinueStatementContext ctx) {
        exitStatement("CONTINUE", ctx.getStop().getLine());
    }
    
    @Override
    public void enterYieldStatement(Java20Parser.YieldStatementContext ctx) {
        enterStatement("YIELD", ctx.getStart().getLine());
    }
    
    @Override
    public void exitYieldStatement(Java20Parser.YieldStatementContext ctx) {
        exitStatement("YIELD", ctx.getStop().getLine());
    }
    
    // ========================================
    // 동기화/Assert
    // ========================================
    
    @Override
    public void enterSynchronizedStatement(Java20Parser.SynchronizedStatementContext ctx) {
        enterStatement("SYNCHRONIZED", ctx.getStart().getLine());
    }
    
    @Override
    public void exitSynchronizedStatement(Java20Parser.SynchronizedStatementContext ctx) {
        exitStatement("SYNCHRONIZED", ctx.getStop().getLine());
    }
    
    @Override
    public void enterAssertStatement(Java20Parser.AssertStatementContext ctx) {
        enterStatement("ASSERT", ctx.getStart().getLine());
    }
    
    @Override
    public void exitAssertStatement(Java20Parser.AssertStatementContext ctx) {
        exitStatement("ASSERT", ctx.getStop().getLine());
    }
    
    // ========================================
    // 람다/익명 클래스
    // ========================================
    
    @Override
    public void enterLambdaExpression(Java20Parser.LambdaExpressionContext ctx) {
        enterStatement("LAMBDA", ctx.getStart().getLine());
    }
    
    @Override
    public void exitLambdaExpression(Java20Parser.LambdaExpressionContext ctx) {
        exitStatement("LAMBDA", ctx.getStop().getLine());
    }
    
    // ========================================
    // 디버깅용
    // ========================================
    
    public void printTree(Node node, String indent) {
        System.out.println(indent + node.type + " (" + node.startLine + "-" + node.endLine + ")");
        for (Node child : node.children) {
            printTree(child, indent + "  ");
        }
    }
    
    public void printStructure() {
        printTree(root, "");
    }
}
