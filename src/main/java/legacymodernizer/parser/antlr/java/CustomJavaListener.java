package legacymodernizer.parser.antlr.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

import legacymodernizer.parser.antlr.Node;
import legacymodernizer.parser.antlr.ParserUtils;
import legacymodernizer.parser.service.ParseProgressTracker;

/**
 * Java 파일 분석을 위한 커스텀 리스너
 * - 패키지는 FILE 노드의 packageName 속성으로 저장
 * - import 문은 IMPORT 노드로 추출
 * - 클래스/인터페이스/메서드/필드/로컬변수 구조 추출
 * - new 인스턴스 생성은 NEW_INSTANCE 노드 (타입명, 라인만)
 * - 메서드 호출은 METHOD_CALL 노드; VARIABLE은 초기화식에 메서드호출/new 패턴이 있으면 플래그(속성)로 표시
 * - 어노테이션은 부모 노드의 annotations 속성에 포함
 * - 선행 주석(Javadoc 등)은 code 속성에 포함, startLine도 주석 시작 라인으로 보정
 * - 통일된 속성명 사용 (Node 클래스 참조)
 */
public class CustomJavaListener extends Java20ParserBaseListener {
    
    private CommonTokenStream tokens;
    private Stack<Node> nodeStack = new Stack<>();
    private Node root = new Node("FILE", 0, null);
    private ParseProgressTracker progressTracker;
    
    public Node getRoot() {
        return root;
    }
    
    /**
     * FILE 노드에 파일 정보 설정
     */
    public void setFileInfo(String fileName, String filePath) {
        root.fileName = fileName;
        root.filePath = filePath;
    }
    
    public CustomJavaListener(CommonTokenStream tokens) {
        this.tokens = tokens;
        nodeStack.push(root);
    }
    
    public CustomJavaListener(CommonTokenStream tokens, ParseProgressTracker tracker) {
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
                node.comment = ParserUtils.getLeadingComment(ctx, tokens);
            }
        }
    }
    
    // ========================================
    // 어노테이션/수정자 분리 추출
    // ========================================
    
    private void extractClassModifiers(Node node, List<Java20Parser.ClassModifierContext> modifiers) {
        if (modifiers == null || modifiers.isEmpty()) return;
        
        List<String> annotationList = new ArrayList<>();
        List<String> modifierList = new ArrayList<>();
        
        for (Java20Parser.ClassModifierContext m : modifiers) {
            if (m.annotation() != null) {
                annotationList.add(m.getText());
            } else {
                modifierList.add(m.getText());
            }
        }
        
        if (!annotationList.isEmpty()) {
            node.annotations = String.join(" ", annotationList);
        }
        if (!modifierList.isEmpty()) {
            node.modifiers = String.join(" ", modifierList);
        }
    }
    
    private void extractInterfaceModifiers(Node node, List<Java20Parser.InterfaceModifierContext> modifiers) {
        if (modifiers == null || modifiers.isEmpty()) return;
        
        List<String> annotationList = new ArrayList<>();
        List<String> modifierList = new ArrayList<>();
        
        for (Java20Parser.InterfaceModifierContext m : modifiers) {
            if (m.annotation() != null) {
                annotationList.add(m.getText());
            } else {
                modifierList.add(m.getText());
            }
        }
        
        if (!annotationList.isEmpty()) {
            node.annotations = String.join(" ", annotationList);
        }
        if (!modifierList.isEmpty()) {
            node.modifiers = String.join(" ", modifierList);
        }
    }
    
    private void extractMethodModifiers(Node node, List<Java20Parser.MethodModifierContext> modifiers) {
        if (modifiers == null || modifiers.isEmpty()) return;
        
        List<String> annotationList = new ArrayList<>();
        List<String> modifierList = new ArrayList<>();
        
        for (Java20Parser.MethodModifierContext m : modifiers) {
            if (m.annotation() != null) {
                annotationList.add(m.getText());
            } else {
                modifierList.add(m.getText());
            }
        }
        
        if (!annotationList.isEmpty()) {
            node.annotations = String.join(" ", annotationList);
        }
        if (!modifierList.isEmpty()) {
            node.modifiers = String.join(" ", modifierList);
        }
    }
    
    private void extractInterfaceMethodModifiers(Node node, List<Java20Parser.InterfaceMethodModifierContext> modifiers) {
        if (modifiers == null || modifiers.isEmpty()) return;
        
        List<String> annotationList = new ArrayList<>();
        List<String> modifierList = new ArrayList<>();
        
        for (Java20Parser.InterfaceMethodModifierContext m : modifiers) {
            if (m.annotation() != null) {
                annotationList.add(m.getText());
            } else {
                modifierList.add(m.getText());
            }
        }
        
        if (!annotationList.isEmpty()) {
            node.annotations = String.join(" ", annotationList);
        }
        if (!modifierList.isEmpty()) {
            node.modifiers = String.join(" ", modifierList);
        }
    }
    
    private void extractFieldModifiers(Node node, List<Java20Parser.FieldModifierContext> modifiers) {
        if (modifiers == null || modifiers.isEmpty()) return;
        
        List<String> annotationList = new ArrayList<>();
        List<String> modifierList = new ArrayList<>();
        
        for (Java20Parser.FieldModifierContext m : modifiers) {
            if (m.annotation() != null) {
                annotationList.add(m.getText());
            } else {
                modifierList.add(m.getText());
            }
        }
        
        if (!annotationList.isEmpty()) {
            node.annotations = String.join(" ", annotationList);
        }
        if (!modifierList.isEmpty()) {
            node.modifiers = String.join(" ", modifierList);
        }
    }
    
    // ========================================
    // 패키지 (FILE 노드 속성)
    // ========================================
    
    @Override
    public void enterPackageDeclaration(Java20Parser.PackageDeclarationContext ctx) {
        String text = ctx.getText();
        if (text != null) {
            root.packageName = text.replaceFirst("^.*?package\\s*", "").replaceAll("\\s*;\\s*$", "").trim();
        }
    }
    
    // ========================================
    // import 문
    // ========================================
    
    @Override
    public void enterImportDeclaration(Java20Parser.ImportDeclarationContext ctx) {
        String text = ctx.getText();
        String name = (text != null)
                ? text.replaceFirst("^import\\s*", "").replaceAll("\\s*;\\s*$", "").trim()
                : null;
        enterStatement("IMPORT", name, ctx.getStart().getLine());
    }
    
    @Override
    public void exitImportDeclaration(Java20Parser.ImportDeclarationContext ctx) {
        exitStatement("IMPORT", ctx.getStop().getLine(), ctx);
    }
    
    // ========================================
    // 모듈명 전파
    // ========================================
    
    /**
     * 모듈명을 모든 자식 노드에 재귀적으로 전파
     */
    private void propagateModuleName(Node node, String moduleName) {
        for (Node child : node.children) {
            child.moduleName = moduleName;
            propagateModuleName(child, moduleName);
    }
    }
    
    // ========================================
    // 클래스/인터페이스
    // ========================================
    
    @Override
    public void enterNormalClassDeclaration(Java20Parser.NormalClassDeclarationContext ctx) {
        String name = ctx.typeIdentifier() != null ? ctx.typeIdentifier().getText() : null;
        Node node = enterStatement("CLASS", name, ctx.getStart().getLine());
        
        node.signature = ParserUtils.extractSignature(ctx, tokens, "{");
        extractClassModifiers(node, ctx.classModifier());
        
        if (ctx.typeParameters() != null) {
            node.genericType = ctx.typeParameters().getText();
        }
        
        if (ctx.classExtends() != null && ctx.classExtends().classType() != null) {
            node.extendsType = ctx.classExtends().classType().getText();
        }
        
        if (ctx.classImplements() != null && ctx.classImplements().interfaceTypeList() != null) {
            node.implementsTypes = ctx.classImplements().interfaceTypeList().getText();
        }
    }
    
    @Override
    public void exitNormalClassDeclaration(Java20Parser.NormalClassDeclarationContext ctx) {
        if (!nodeStack.isEmpty() && nodeStack.peek().type.equals("CLASS")) {
            Node node = nodeStack.peek();
            // comment를 먼저 설정 (exitStatement가 pop하므로 미리 설정)
            if (ctx != null) {
                node.comment = ParserUtils.getLeadingComment(ctx, tokens);
            }
            // exitStatement 호출 (code, endLine 설정)
            exitStatement("CLASS", ctx.getStop().getLine(), ctx);
            // 모든 자식에 moduleName 전파
            propagateModuleName(node, node.name);
        } else {
            exitStatement("CLASS", ctx.getStop().getLine(), ctx);
        }
    }
    
    @Override
    public void enterNormalInterfaceDeclaration(Java20Parser.NormalInterfaceDeclarationContext ctx) {
        String name = ctx.typeIdentifier() != null ? ctx.typeIdentifier().getText() : null;
        Node node = enterStatement("INTERFACE", name, ctx.getStart().getLine());
        
        node.signature = ParserUtils.extractSignature(ctx, tokens, "{");
        extractInterfaceModifiers(node, ctx.interfaceModifier());
        
        if (ctx.typeParameters() != null) {
            node.genericType = ctx.typeParameters().getText();
        }
        
        if (ctx.interfaceExtends() != null && ctx.interfaceExtends().interfaceTypeList() != null) {
            node.extendsType = ctx.interfaceExtends().interfaceTypeList().getText();
        }
    }
    
    @Override
    public void exitNormalInterfaceDeclaration(Java20Parser.NormalInterfaceDeclarationContext ctx) {
        if (!nodeStack.isEmpty() && nodeStack.peek().type.equals("INTERFACE")) {
            Node node = nodeStack.peek();
            // comment를 먼저 설정 (exitStatement가 pop하므로 미리 설정)
            if (ctx != null) {
                node.comment = ParserUtils.getLeadingComment(ctx, tokens);
            }
            // exitStatement 호출 (code, endLine 설정)
            exitStatement("INTERFACE", ctx.getStop().getLine(), ctx);
            // 모든 자식에 moduleName 전파
            propagateModuleName(node, node.name);
        } else {
            exitStatement("INTERFACE", ctx.getStop().getLine(), ctx);
        }
    }
    
    // ========================================
    // 메서드
    // ========================================
    
    @Override
    public void enterMethodDeclaration(Java20Parser.MethodDeclarationContext ctx) {
        // CLASS/INTERFACE의 직접 자식일 때만 METHOD 노드 생성 (익명 클래스 내부 메서드 무시)
        if (nodeStack.isEmpty()) return;
        String parentType = nodeStack.peek().type;
        if (!parentType.equals("CLASS") && !parentType.equals("INTERFACE")) return;
        
        String name = null;
        if (ctx.methodHeader() != null && ctx.methodHeader().methodDeclarator() != null 
                && ctx.methodHeader().methodDeclarator().identifier() != null) {
            name = ctx.methodHeader().methodDeclarator().identifier().getText();
        }
        Node node = enterStatement("METHOD", name, ctx.getStart().getLine());
        
        node.signature = ParserUtils.extractSignature(ctx, tokens, "{");
        extractMethodModifiers(node, ctx.methodModifier());
        
        if (ctx.methodHeader() != null && ctx.methodHeader().result() != null) {
            node.returnType = ctx.methodHeader().result().getText();
        }
        
        if (ctx.methodHeader() != null && ctx.methodHeader().typeParameters() != null) {
            node.genericType = ctx.methodHeader().typeParameters().getText();
        }
        
        if (ctx.methodHeader() != null 
                && ctx.methodHeader().methodDeclarator() != null 
                && ctx.methodHeader().methodDeclarator().formalParameterList() != null) {
            node.parameters = ParserUtils.getOriginalText(
                    ctx.methodHeader().methodDeclarator().formalParameterList(), tokens);
        }
    }
    
    @Override
    public void exitMethodDeclaration(Java20Parser.MethodDeclarationContext ctx) {
        exitStatement("METHOD", ctx.getStop().getLine(), ctx);
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
        extractInterfaceMethodModifiers(node, ctx.interfaceMethodModifier());
        
        if (ctx.methodHeader() != null && ctx.methodHeader().result() != null) {
            node.returnType = ctx.methodHeader().result().getText();
        }
        
        if (ctx.methodHeader() != null && ctx.methodHeader().typeParameters() != null) {
            node.genericType = ctx.methodHeader().typeParameters().getText();
        }
        
        if (ctx.methodHeader() != null 
                && ctx.methodHeader().methodDeclarator() != null 
                && ctx.methodHeader().methodDeclarator().formalParameterList() != null) {
            node.parameters = ParserUtils.getOriginalText(
                    ctx.methodHeader().methodDeclarator().formalParameterList(), tokens);
        }
    }
    
    @Override
    public void exitInterfaceMethodDeclaration(Java20Parser.InterfaceMethodDeclarationContext ctx) {
        exitStatement("METHOD", ctx.getStop().getLine(), ctx);
    }
    
    // ========================================
    // 필드
    // ========================================
    
    @Override
    public void enterFieldDeclaration(Java20Parser.FieldDeclarationContext ctx) {
        Node node = enterStatement("FIELD", null, ctx.getStart().getLine());

        extractFieldModifiers(node, ctx.fieldModifier());

        // final 필드 → CONSTANT_FIELD
        if (node.modifiers != null && node.modifiers.contains("final")) {
            node.type = "CONSTANT_FIELD";
        }

        if (ctx.unannType() != null) {
            node.variableType = ctx.unannType().getText();
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
        // exitStatement의 타입도 현재 노드 타입에 맞춤
        Node current = nodeStack.peek();
        exitStatement(current != null ? current.type : "FIELD", ctx.getStop().getLine(), ctx);
    }
    
    // ========================================
    // 메서드 호출
    // ========================================
    
    @Override
    public void enterMethodInvocation(Java20Parser.MethodInvocationContext ctx) {
        String name = null;
        if (ctx.methodName() != null) {
            name = ctx.methodName().getText();
        } else if (ctx.identifier() != null) {
            name = ctx.identifier().getText();
        }
        enterStatement("METHOD_CALL", name, ctx.getStart().getLine());
    }
    
    @Override
    public void exitMethodInvocation(Java20Parser.MethodInvocationContext ctx) {
        exitStatement("METHOD_CALL", ctx.getStop().getLine(), ctx);
    }
    
    // ========================================
    // 변수 (초기화식 패턴은 플래그로 표시)
    // ========================================
    
    /**
     * 선언문 문자열에서 초기화부만 잘라서 반환 (첫 번째 '=' 뒤).
     * getText()는 공백이 빠질 수 있음 — 정규식 매칭용으로만 사용.
     */
    private static String getInitializerPart(String declaratorListText) {
        if (declaratorListText == null || !declaratorListText.contains("=")) return "";
        int eq = declaratorListText.indexOf('=');
        return declaratorListText.substring(eq + 1).trim();
    }
    
    /**
     * 초기화식 문자열에 메서드 호출 패턴이 있는지 정규식으로 판별.
     * 예: .getX( , getInstance( 등
     */
    private static boolean initializerMatchesMethodCall(String initializerText) {
        if (initializerText == null || initializerText.isEmpty()) return false;
        return initializerText.matches("(?s).*\\.\\w+\\s*\\(.*") || initializerText.matches("(?s).*\\b\\w+\\s*\\(.*");
    }
    
    /**
     * 초기화식 문자열에 new 타입 패턴이 있는지 정규식으로 판별.
     */
    private static boolean initializerMatchesNewInstance(String initializerText) {
        if (initializerText == null) return false;
        return initializerText.matches("(?s).*\\bnew\\s+\\w+.*");
    }
    
    @Override
    public void enterLocalVariableDeclaration(Java20Parser.LocalVariableDeclarationContext ctx) {
        Node node = enterStatement("VARIABLE", null, ctx.getStart().getLine());
        
        if (ctx.localVariableType() != null) {
            node.variableType = ctx.localVariableType().getText();
        }
        
        if (ctx.variableDeclaratorList() != null) {
            String raw = ctx.variableDeclaratorList().getText();
            if (raw != null && raw.contains("=")) {
                node.name = raw.split("=")[0].trim();
                String initPart = getInitializerPart(raw);
                node.initializerContainsMethodCall = initializerMatchesMethodCall(initPart);
                node.initializerContainsNewInstance = initializerMatchesNewInstance(initPart);
            } else {
                node.name = raw;
            }
        }
    }
    
    @Override
    public void exitLocalVariableDeclaration(Java20Parser.LocalVariableDeclarationContext ctx) {
        exitStatement("VARIABLE", ctx.getStop().getLine(), ctx);
    }
    
    // @Override
    // public void enterAssignment(Java20Parser.AssignmentContext ctx) {
    //     enterStatement("ASSIGNMENT", null, ctx.getStart().getLine());
    // }
    
    // @Override
    // public void exitAssignment(Java20Parser.AssignmentContext ctx) {
    //     exitStatement("ASSIGNMENT", ctx.getStop().getLine(), ctx);
    // }
    
    // ========================================
    // 객체 생성 (new 인스턴스 — 타입명·라인만)
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
    // return
    // ========================================
    
    // @Override
    // public void enterReturnStatement(Java20Parser.ReturnStatementContext ctx) {
    //     enterStatement("RETURN", null, ctx.getStart().getLine());
    // }
    
    // @Override
    // public void exitReturnStatement(Java20Parser.ReturnStatementContext ctx) {
    //     exitStatement("RETURN", ctx.getStop().getLine(), ctx);
    // }
    
    // ========================================
    // 디버깅용
    // ========================================
    
    public void printTree(Node node, String indent) {
        StringBuilder info = new StringBuilder();
        info.append(indent).append(node.type);
        if (node.name != null) info.append(" [").append(node.name).append("]");
        if (node.annotations != null) info.append(" @{").append(node.annotations).append("}");
        if (node.modifiers != null) info.append(" {").append(node.modifiers).append("}");
        if (node.extendsType != null) info.append(" extends:").append(node.extendsType);
        if (node.implementsTypes != null) info.append(" implements:").append(node.implementsTypes);
        if (node.variableType != null) info.append(" type:").append(node.variableType);
        if (node.returnType != null) info.append(" returns:").append(node.returnType);
        if (node.genericType != null) info.append(" generic:").append(node.genericType);
        if (node.packageName != null) info.append(" package:").append(node.packageName);
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
