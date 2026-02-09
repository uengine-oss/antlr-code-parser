package legacymodernizer.parser.antlr.java;

import java.util.Stack;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;

import legacymodernizer.parser.antlr.Node;
import legacymodernizer.parser.antlr.ParserUtils;
import legacymodernizer.parser.service.ParseProgressTracker;

/**
 * Java 파일 분석을 위한 커스텀 리스너
 * - 클래스/인터페이스/메서드/필드 구조 추출
 * - 상속/구현 관계 추출
 * - 메서드 호출, 객체 생성 추출
 * - 통일된 속성명 사용 (Node 클래스 참조)
 */
public class CustomJavaListener extends Java20ParserBaseListener {
    
    private TokenStream tokens;
    private Stack<Node> nodeStack = new Stack<>();
    private Node root = new Node("FILE", 0, null);
    private ParseProgressTracker progressTracker;
    
    public Node getRoot() {
        return root;
    }
    
    public CustomJavaListener(TokenStream tokens) {
        this.tokens = tokens;
        nodeStack.push(root);
    }
    
    public CustomJavaListener(TokenStream tokens, ParseProgressTracker tracker) {
        this(tokens);
        this.progressTracker = tracker;
    }
    
    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        if (progressTracker != null && ctx.getStart() != null) {
            progressTracker.checkLine(ctx.getStart().getLine());
        }
    }
    
    // ========================================
    // 노드 생성/종료
    // ========================================
    
    private Node enterStatement(String type, int line) {
        return enterStatement(type, null, line);
    }
    
    private Node enterStatement(String type, String name, int line) {
        Node node = new Node(type, name, line, nodeStack.peek());
        nodeStack.push(node);
        return node;
    }
    
    private void exitStatement(String type, int line, ParserRuleContext ctx) {
        if (!nodeStack.isEmpty() && nodeStack.peek().type.equals(type)) {
            Node node = nodeStack.pop();
            node.endLine = line;
            if (ctx != null) {
                node.code = ParserUtils.getCodeWithLineNumbers(ctx);
            }
        }
    }
    
    // ========================================
    // 패키지/임포트
    // ========================================
    
    @Override
    public void enterPackageDeclaration(Java20Parser.PackageDeclarationContext ctx) {
        String name = ctx.identifier() != null && !ctx.identifier().isEmpty()
            ? ctx.identifier().stream().map(id -> id.getText()).collect(Collectors.joining("."))
            : null;
        enterStatement("PACKAGE", name, ctx.getStart().getLine());
    }
    
    @Override
    public void exitPackageDeclaration(Java20Parser.PackageDeclarationContext ctx) {
        exitStatement("PACKAGE", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterImportDeclaration(Java20Parser.ImportDeclarationContext ctx) {
        enterStatement("IMPORT", ctx.getStart().getLine());
    }
    
    @Override
    public void exitImportDeclaration(Java20Parser.ImportDeclarationContext ctx) {
        exitStatement("IMPORT", ctx.getStop().getLine(), ctx);
    }
    
    // ========================================
    // 클래스/인터페이스/열거형/레코드
    // ========================================
    
    @Override
    public void enterNormalClassDeclaration(Java20Parser.NormalClassDeclarationContext ctx) {
        String name = ctx.typeIdentifier() != null ? ctx.typeIdentifier().getText() : null;
        Node node = enterStatement("CLASS", name, ctx.getStart().getLine());
        
        // 시그니처 추출 (클래스 선언부, { 이전까지)
        node.signature = ParserUtils.extractSignature(ctx, tokens, "{");
        
        // 수정자
        if (ctx.classModifier() != null && !ctx.classModifier().isEmpty()) {
            node.modifiers = ctx.classModifier().stream().map(m -> m.getText()).collect(Collectors.joining(" "));
        }
        
        // 제네릭 타입
        if (ctx.typeParameters() != null) {
            node.genericType = ctx.typeParameters().getText();
        }
        
        // 상속 (통일된 속성명: extendsType)
        if (ctx.classExtends() != null && ctx.classExtends().classType() != null) {
            node.extendsType = ctx.classExtends().classType().getText();
        }
        
        // 구현 (통일된 속성명: implementsTypes)
        if (ctx.classImplements() != null && ctx.classImplements().interfaceTypeList() != null) {
            node.implementsTypes = ctx.classImplements().interfaceTypeList().getText();
        }
    }
    
    @Override
    public void exitNormalClassDeclaration(Java20Parser.NormalClassDeclarationContext ctx) {
        exitStatement("CLASS", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterEnumDeclaration(Java20Parser.EnumDeclarationContext ctx) {
        String name = ctx.typeIdentifier() != null ? ctx.typeIdentifier().getText() : null;
        Node node = enterStatement("ENUM", name, ctx.getStart().getLine());
        
        node.signature = ParserUtils.extractSignature(ctx, tokens, "{");
        
        if (ctx.classModifier() != null && !ctx.classModifier().isEmpty()) {
            node.modifiers = ctx.classModifier().stream().map(m -> m.getText()).collect(Collectors.joining(" "));
        }
        
        if (ctx.classImplements() != null && ctx.classImplements().interfaceTypeList() != null) {
            node.implementsTypes = ctx.classImplements().interfaceTypeList().getText();
        }
    }
    
    @Override
    public void exitEnumDeclaration(Java20Parser.EnumDeclarationContext ctx) {
        exitStatement("ENUM", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterRecordDeclaration(Java20Parser.RecordDeclarationContext ctx) {
        String name = ctx.typeIdentifier() != null ? ctx.typeIdentifier().getText() : null;
        Node node = enterStatement("RECORD", name, ctx.getStart().getLine());
        
        node.signature = ParserUtils.extractSignature(ctx, tokens, "{");
        
        if (ctx.classModifier() != null && !ctx.classModifier().isEmpty()) {
            node.modifiers = ctx.classModifier().stream().map(m -> m.getText()).collect(Collectors.joining(" "));
        }
        
        if (ctx.typeParameters() != null) {
            node.genericType = ctx.typeParameters().getText();
        }
        
        if (ctx.recordHeader() != null && ctx.recordHeader().recordComponentList() != null) {
            node.parameters = ctx.recordHeader().recordComponentList().getText();
        }
        
        if (ctx.classImplements() != null && ctx.classImplements().interfaceTypeList() != null) {
            node.implementsTypes = ctx.classImplements().interfaceTypeList().getText();
        }
    }
    
    @Override
    public void exitRecordDeclaration(Java20Parser.RecordDeclarationContext ctx) {
        exitStatement("RECORD", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterNormalInterfaceDeclaration(Java20Parser.NormalInterfaceDeclarationContext ctx) {
        String name = ctx.typeIdentifier() != null ? ctx.typeIdentifier().getText() : null;
        Node node = enterStatement("INTERFACE", name, ctx.getStart().getLine());
        
        node.signature = ParserUtils.extractSignature(ctx, tokens, "{");
        
        if (ctx.interfaceModifier() != null && !ctx.interfaceModifier().isEmpty()) {
            node.modifiers = ctx.interfaceModifier().stream().map(m -> m.getText()).collect(Collectors.joining(" "));
        }
        
        if (ctx.typeParameters() != null) {
            node.genericType = ctx.typeParameters().getText();
        }
        
        if (ctx.interfaceExtends() != null && ctx.interfaceExtends().interfaceTypeList() != null) {
            node.extendsType = ctx.interfaceExtends().interfaceTypeList().getText();
        }
    }
    
    @Override
    public void exitNormalInterfaceDeclaration(Java20Parser.NormalInterfaceDeclarationContext ctx) {
        exitStatement("INTERFACE", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterAnnotationInterfaceDeclaration(Java20Parser.AnnotationInterfaceDeclarationContext ctx) {
        String name = ctx.typeIdentifier() != null ? ctx.typeIdentifier().getText() : null;
        Node node = enterStatement("ANNOTATION_TYPE", name, ctx.getStart().getLine());
        
        node.signature = ParserUtils.extractSignature(ctx, tokens, "{");
        
        if (ctx.interfaceModifier() != null && !ctx.interfaceModifier().isEmpty()) {
            node.modifiers = ctx.interfaceModifier().stream().map(m -> m.getText()).collect(Collectors.joining(" "));
        }
    }
    
    @Override
    public void exitAnnotationInterfaceDeclaration(Java20Parser.AnnotationInterfaceDeclarationContext ctx) {
        exitStatement("ANNOTATION_TYPE", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterAnnotation(Java20Parser.AnnotationContext ctx) {
        String name = ctx.getText();
        if (name != null && name.contains("(")) {
            name = name.substring(0, name.indexOf("("));
        }
        enterStatement("ANNOTATION", name, ctx.getStart().getLine());
    }
    
    @Override
    public void exitAnnotation(Java20Parser.AnnotationContext ctx) {
        exitStatement("ANNOTATION", ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // 상속/구현 관계
    // ========================================
    
    // @Override
    // public void enterClassExtends(Java20Parser.ClassExtendsContext ctx) {
    //     String name = ctx.classType() != null ? ctx.classType().getText() : null;
    //     enterStatement("EXTENDS", name, ctx.getStart().getLine());
    // }
    
    // @Override
    // public void exitClassExtends(Java20Parser.ClassExtendsContext ctx) {
    //     exitStatement("EXTENDS", ctx.getStop().getLine(), ctx);
    // }
    
    // @Override
    // public void enterClassImplements(Java20Parser.ClassImplementsContext ctx) {
    //     String name = ctx.interfaceTypeList() != null ? ctx.interfaceTypeList().getText() : null;
    //     enterStatement("IMPLEMENTS", name, ctx.getStart().getLine());
    // }
    
    // @Override
    // public void exitClassImplements(Java20Parser.ClassImplementsContext ctx) {
    //     exitStatement("IMPLEMENTS", ctx.getStop().getLine(), ctx);
    // }
    
    // @Override
    // public void enterInterfaceExtends(Java20Parser.InterfaceExtendsContext ctx) {
    //     String name = ctx.interfaceTypeList() != null ? ctx.interfaceTypeList().getText() : null;
    //     enterStatement("EXTENDS", name, ctx.getStart().getLine());
    // }
    
    // @Override
    // public void exitInterfaceExtends(Java20Parser.InterfaceExtendsContext ctx) {
    //     exitStatement("EXTENDS", ctx.getStop().getLine(), ctx);
    // }
    
    // ========================================
    // 필드/변수
    // ========================================
    
    @Override
    public void enterFieldDeclaration(Java20Parser.FieldDeclarationContext ctx) {
        Node node = enterStatement("FIELD", ctx.getStart().getLine());
        
        if (ctx.fieldModifier() != null && !ctx.fieldModifier().isEmpty()) {
            node.modifiers = ctx.fieldModifier().stream().map(m -> m.getText()).collect(Collectors.joining(" "));
        }
        
        if (ctx.unannType() != null) {
            node.fieldType = ctx.unannType().getText();
        }
        
        if (ctx.variableDeclaratorList() != null) {
            node.name = ctx.variableDeclaratorList().getText();
            if (node.name != null && node.name.contains("=")) {
                node.name = node.name.split("=")[0];
            }
        }
    }
    
    @Override
    public void exitFieldDeclaration(Java20Parser.FieldDeclarationContext ctx) {
        exitStatement("FIELD", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterLocalVariableDeclaration(Java20Parser.LocalVariableDeclarationContext ctx) {
        Node node = enterStatement("VARIABLE", ctx.getStart().getLine());
        
        if (ctx.localVariableType() != null) {
            node.fieldType = ctx.localVariableType().getText();
        }
        
        if (ctx.variableDeclaratorList() != null) {
            node.name = ctx.variableDeclaratorList().getText();
            if (node.name != null && node.name.contains("=")) {
                node.name = node.name.split("=")[0];
            }
        }
    }
    
    @Override
    public void exitLocalVariableDeclaration(Java20Parser.LocalVariableDeclarationContext ctx) {
        exitStatement("VARIABLE", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterAssignment(Java20Parser.AssignmentContext ctx) {
        enterStatement("ASSIGNMENT", ctx.getStart().getLine());
    }
    
    @Override
    public void exitAssignment(Java20Parser.AssignmentContext ctx) {
        exitStatement("ASSIGNMENT", ctx.getStop().getLine(), ctx);
    }
    
    // ========================================
    // 초기화 블록
    // ========================================
    
    @Override
    public void enterStaticInitializer(Java20Parser.StaticInitializerContext ctx) {
        Node node = enterStatement("STATIC_INITIALIZER", ctx.getStart().getLine());
        node.modifiers = "static";
    }
    
    @Override
    public void exitStaticInitializer(Java20Parser.StaticInitializerContext ctx) {
        exitStatement("STATIC_INITIALIZER", ctx.getStop().getLine(), ctx);
    }
    
    // ========================================
    // 메서드/생성자
    // ========================================
    
    @Override
    public void enterMethodDeclaration(Java20Parser.MethodDeclarationContext ctx) {
        String name = null;
        if (ctx.methodHeader() != null && ctx.methodHeader().methodDeclarator() != null 
                && ctx.methodHeader().methodDeclarator().identifier() != null) {
            name = ctx.methodHeader().methodDeclarator().identifier().getText();
        }
        Node node = enterStatement("METHOD", name, ctx.getStart().getLine());
        
        // 시그니처 추출 ({ 이전까지)
        node.signature = ParserUtils.extractSignature(ctx, tokens, "{");
        
        if (ctx.methodModifier() != null && !ctx.methodModifier().isEmpty()) {
            node.modifiers = ctx.methodModifier().stream().map(m -> m.getText()).collect(Collectors.joining(" "));
        }
        
        if (ctx.methodHeader() != null && ctx.methodHeader().result() != null) {
            node.returnType = ctx.methodHeader().result().getText();
        }
        
        if (ctx.methodHeader() != null && ctx.methodHeader().typeParameters() != null) {
            node.genericType = ctx.methodHeader().typeParameters().getText();
        }
        
        if (ctx.methodHeader() != null && ctx.methodHeader().methodDeclarator() != null 
                && ctx.methodHeader().methodDeclarator().formalParameterList() != null) {
            node.parameters = ctx.methodHeader().methodDeclarator().formalParameterList().getText();
        }
    }
    
    @Override
    public void exitMethodDeclaration(Java20Parser.MethodDeclarationContext ctx) {
        exitStatement("METHOD", ctx.getStop().getLine(), ctx);
    }
    
    // @Override
    // public void enterMethodHeader(Java20Parser.MethodHeaderContext ctx) {
    //     enterStatement("METHOD_SIGNATURE", ctx.getStart().getLine());
    // }
    
    // @Override
    // public void exitMethodHeader(Java20Parser.MethodHeaderContext ctx) {
    //     exitStatement("METHOD_SIGNATURE", ctx.getStop().getLine(), ctx);
    // }
    
    @Override
    public void enterConstructorDeclaration(Java20Parser.ConstructorDeclarationContext ctx) {
        String name = null;
        if (ctx.constructorDeclarator() != null && ctx.constructorDeclarator().simpleTypeName() != null) {
            name = ctx.constructorDeclarator().simpleTypeName().getText();
        }
        Node node = enterStatement("CONSTRUCTOR", name, ctx.getStart().getLine());
        
        node.signature = ParserUtils.extractSignature(ctx, tokens, "{");
        
        if (ctx.constructorModifier() != null && !ctx.constructorModifier().isEmpty()) {
            node.modifiers = ctx.constructorModifier().stream().map(m -> m.getText()).collect(Collectors.joining(" "));
        }
        
        if (ctx.constructorDeclarator() != null && ctx.constructorDeclarator().typeParameters() != null) {
            node.genericType = ctx.constructorDeclarator().typeParameters().getText();
        }
        
        if (ctx.constructorDeclarator() != null && ctx.constructorDeclarator().formalParameterList() != null) {
            node.parameters = ctx.constructorDeclarator().formalParameterList().getText();
        }
    }
    
    @Override
    public void exitConstructorDeclaration(Java20Parser.ConstructorDeclarationContext ctx) {
        exitStatement("CONSTRUCTOR", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterInterfaceMethodDeclaration(Java20Parser.InterfaceMethodDeclarationContext ctx) {
        String name = null;
        if (ctx.methodHeader() != null && ctx.methodHeader().methodDeclarator() != null 
                && ctx.methodHeader().methodDeclarator().identifier() != null) {
            name = ctx.methodHeader().methodDeclarator().identifier().getText();
        }
        Node node = enterStatement("METHOD", name, ctx.getStart().getLine());
        
        node.signature = ParserUtils.extractSignature(ctx, tokens, ";");
        
        if (ctx.interfaceMethodModifier() != null && !ctx.interfaceMethodModifier().isEmpty()) {
            node.modifiers = ctx.interfaceMethodModifier().stream().map(m -> m.getText()).collect(Collectors.joining(" "));
        }
        
        if (ctx.methodHeader() != null && ctx.methodHeader().result() != null) {
            node.returnType = ctx.methodHeader().result().getText();
        }
        
        if (ctx.methodHeader() != null && ctx.methodHeader().typeParameters() != null) {
            node.genericType = ctx.methodHeader().typeParameters().getText();
        }
        
        if (ctx.methodHeader() != null && ctx.methodHeader().methodDeclarator() != null 
                && ctx.methodHeader().methodDeclarator().formalParameterList() != null) {
            node.parameters = ctx.methodHeader().methodDeclarator().formalParameterList().getText();
        }
    }
    
    @Override
    public void exitInterfaceMethodDeclaration(Java20Parser.InterfaceMethodDeclarationContext ctx) {
        exitStatement("METHOD", ctx.getStop().getLine(), ctx);
    }
    
    // ========================================
    // 메서드 호출/참조
    // ========================================
    
    @Override
    public void enterMethodInvocation(Java20Parser.MethodInvocationContext ctx) {
        String name = null;
        if (ctx.identifier() != null) {
            name = ctx.identifier().getText();
        } else if (ctx.methodName() != null) {
            name = ctx.methodName().getText();
        }
        enterStatement("METHOD_CALL", name, ctx.getStart().getLine());
    }
    
    @Override
    public void exitMethodInvocation(Java20Parser.MethodInvocationContext ctx) {
        exitStatement("METHOD_CALL", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterMethodReference(Java20Parser.MethodReferenceContext ctx) {
        String name = ctx.identifier() != null ? ctx.identifier().getText() : null;
        enterStatement("METHOD_CALL", name, ctx.getStart().getLine());
    }
    
    @Override
    public void exitMethodReference(Java20Parser.MethodReferenceContext ctx) {
        exitStatement("METHOD_CALL", ctx.getStop().getLine(), ctx);
    }
    
    // ========================================
    // 객체 생성
    // ========================================
    
    @Override
    public void enterClassInstanceCreationExpression(Java20Parser.ClassInstanceCreationExpressionContext ctx) {
        String name = null;
        if (ctx.unqualifiedClassInstanceCreationExpression() != null 
                && ctx.unqualifiedClassInstanceCreationExpression().classOrInterfaceTypeToInstantiate() != null) {
            name = ctx.unqualifiedClassInstanceCreationExpression().classOrInterfaceTypeToInstantiate().getText();
        }
        enterStatement("NEW_INSTANCE", name, ctx.getStart().getLine());
    }
    
    @Override
    public void exitClassInstanceCreationExpression(Java20Parser.ClassInstanceCreationExpressionContext ctx) {
        exitStatement("NEW_INSTANCE", ctx.getStop().getLine(), ctx);
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
        exitStatement("IF", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterIfThenElseStatement(Java20Parser.IfThenElseStatementContext ctx) {
        enterStatement("ELSE", ctx.getStart().getLine());
    }
    
    @Override
    public void exitIfThenElseStatement(Java20Parser.IfThenElseStatementContext ctx) {
        exitStatement("ELSE", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterForStatement(Java20Parser.ForStatementContext ctx) {
        enterStatement("FOR", ctx.getStart().getLine());
    }
    
    @Override
    public void exitForStatement(Java20Parser.ForStatementContext ctx) {
        exitStatement("FOR", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterWhileStatement(Java20Parser.WhileStatementContext ctx) {
        enterStatement("WHILE", ctx.getStart().getLine());
    }
    
    @Override
    public void exitWhileStatement(Java20Parser.WhileStatementContext ctx) {
        exitStatement("WHILE", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterDoStatement(Java20Parser.DoStatementContext ctx) {
        enterStatement("DO_WHILE", ctx.getStart().getLine());
    }
    
    @Override
    public void exitDoStatement(Java20Parser.DoStatementContext ctx) {
        exitStatement("DO_WHILE", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterSwitchStatement(Java20Parser.SwitchStatementContext ctx) {
        enterStatement("SWITCH", ctx.getStart().getLine());
    }
    
    @Override
    public void exitSwitchStatement(Java20Parser.SwitchStatementContext ctx) {
        exitStatement("SWITCH", ctx.getStop().getLine(), ctx);
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
        exitStatement("TRY", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterCatchClause(Java20Parser.CatchClauseContext ctx) {
        enterStatement("CATCH", ctx.getStart().getLine());
    }
    
    @Override
    public void exitCatchClause(Java20Parser.CatchClauseContext ctx) {
        exitStatement("CATCH", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterFinallyBlock(Java20Parser.FinallyBlockContext ctx) {
        enterStatement("FINALLY", ctx.getStart().getLine());
    }
    
    @Override
    public void exitFinallyBlock(Java20Parser.FinallyBlockContext ctx) {
        exitStatement("FINALLY", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterThrowStatement(Java20Parser.ThrowStatementContext ctx) {
        enterStatement("THROW", ctx.getStart().getLine());
    }
    
    @Override
    public void exitThrowStatement(Java20Parser.ThrowStatementContext ctx) {
        exitStatement("THROW", ctx.getStop().getLine(), ctx);
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
        exitStatement("RETURN", ctx.getStop().getLine(), ctx);
    }
    
    // ========================================
    // 디버깅용
    // ========================================
    
    public void printTree(Node node, String indent) {
        StringBuilder info = new StringBuilder();
        info.append(indent).append(node.type);
        if (node.name != null) info.append(" [").append(node.name).append("]");
        if (node.modifiers != null) info.append(" {").append(node.modifiers).append("}");
        if (node.extendsType != null) info.append(" extends:").append(node.extendsType);
        if (node.implementsTypes != null) info.append(" implements:").append(node.implementsTypes);
        if (node.fieldType != null) info.append(" type:").append(node.fieldType);
        if (node.returnType != null) info.append(" returns:").append(node.returnType);
        if (node.genericType != null) info.append(" generic:").append(node.genericType);
        info.append(" (").append(node.startLine).append("-").append(node.endLine).append(")");
        
        System.out.println(info.toString());
        for (Node child : node.children) {
            printTree(child, indent + "  ");
        }
    }
    
    public void printStructure() {
        printTree(root, "");
    }
}
