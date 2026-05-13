package legacymodernizer.parser.antlr.java;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

import legacymodernizer.parser.model.Node;
import legacymodernizer.parser.antlr.ListenerHelper;
import legacymodernizer.parser.antlr.ParserUtils;
import legacymodernizer.parser.service.ParseProgressTracker;

/**
 * Java 파일 → AST 변환 리스너.
 * 패키지/import/클래스/인터페이스/메서드/필드/변수/호출 구조를 추출한다.
 */
public class JavaAstListener extends Java20ParserBaseListener {

    private final ListenerHelper h;

    public JavaAstListener(CommonTokenStream tokens, ParseProgressTracker tracker) {
        this.h = new ListenerHelper(tokens, tracker);
    }

    public Node getRoot() { return h.getRoot(); }
    public void setFileInfo(String fileName, String filePath) { h.setFileInfo(fileName, filePath); }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) { h.checkProgress(ctx); }

    
    // ========================================
    // 어노테이션/수정자 분리 추출
    // ========================================
    
    /**
     * 어노테이션과 수정자를 분리하여 Node에 설정 (모든 modifier 컨텍스트 공용)
     *
     * @param node          대상 노드
     * @param modifiers     modifier 컨텍스트 리스트 (ClassModifier, MethodModifier 등)
     * @param isAnnotation  해당 컨텍스트가 어노테이션인지 판별하는 조건
     */
    private <T extends ParserRuleContext> void extractModifiers(
            Node node, List<T> modifiers, Predicate<T> isAnnotation) {
        if (modifiers == null || modifiers.isEmpty()) return;

        List<String> annotationList = new ArrayList<>();
        List<String> modifierList = new ArrayList<>();

        for (T m : modifiers) {
            if (isAnnotation.test(m)) {
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
            h.getRoot().packageName = text.replaceFirst("^.*?package\\s*", "").replaceAll("\\s*;\\s*$", "").trim();
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
        h.enterStatement("IMPORT", name, ctx.getStart().getLine());
    }
    
    @Override
    public void exitImportDeclaration(Java20Parser.ImportDeclarationContext ctx) {
        h.exitStatement("IMPORT", ctx.getStop().getLine(), ctx);
    }
    
    // ========================================
    // 클래스/인터페이스
    // ========================================
    
    @Override
    public void enterNormalClassDeclaration(Java20Parser.NormalClassDeclarationContext ctx) {
        String name = ctx.typeIdentifier() != null ? ctx.typeIdentifier().getText() : null;
        Node node = h.enterStatement("CLASS", name, ctx.getStart().getLine());
        
        node.signature = ParserUtils.extractSignature(ctx, h.getTokens(), "{");
        extractModifiers(node, ctx.classModifier(), m -> m.annotation() != null);
        
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
        if (!h.getNodeStack().isEmpty() && h.getNodeStack().peek().type.equals("CLASS")) {
            Node node = h.getNodeStack().peek();
            // comment를 먼저 설정 (exitStatement가 pop하므로 미리 설정)
            if (ctx != null) {
                node.comment = ParserUtils.getLeadingComment(ctx, h.getTokens());
            }
            // exitStatement 호출 (code, endLine 설정)
            h.exitStatement("CLASS", ctx.getStop().getLine(), ctx);
            // 모든 자식에 moduleName 전파
            ListenerHelper.propagateModuleName(node, node.name);
        } else {
            h.exitStatement("CLASS", ctx.getStop().getLine(), ctx);
        }
    }
    
    @Override
    public void enterNormalInterfaceDeclaration(Java20Parser.NormalInterfaceDeclarationContext ctx) {
        String name = ctx.typeIdentifier() != null ? ctx.typeIdentifier().getText() : null;
        Node node = h.enterStatement("INTERFACE", name, ctx.getStart().getLine());
        
        node.signature = ParserUtils.extractSignature(ctx, h.getTokens(), "{");
        extractModifiers(node, ctx.interfaceModifier(), m -> m.annotation() != null);
        
        if (ctx.typeParameters() != null) {
            node.genericType = ctx.typeParameters().getText();
        }
        
        if (ctx.interfaceExtends() != null && ctx.interfaceExtends().interfaceTypeList() != null) {
            node.extendsType = ctx.interfaceExtends().interfaceTypeList().getText();
        }
    }
    
    @Override
    public void exitNormalInterfaceDeclaration(Java20Parser.NormalInterfaceDeclarationContext ctx) {
        if (!h.getNodeStack().isEmpty() && h.getNodeStack().peek().type.equals("INTERFACE")) {
            Node node = h.getNodeStack().peek();
            // comment를 먼저 설정 (exitStatement가 pop하므로 미리 설정)
            if (ctx != null) {
                node.comment = ParserUtils.getLeadingComment(ctx, h.getTokens());
            }
            // exitStatement 호출 (code, endLine 설정)
            h.exitStatement("INTERFACE", ctx.getStop().getLine(), ctx);
            // 모든 자식에 moduleName 전파
            ListenerHelper.propagateModuleName(node, node.name);
        } else {
            h.exitStatement("INTERFACE", ctx.getStop().getLine(), ctx);
        }
    }
    
    // ========================================
    // 메서드
    // ========================================
    
    @Override
    public void enterMethodDeclaration(Java20Parser.MethodDeclarationContext ctx) {
        // CLASS/INTERFACE의 직접 자식일 때만 METHOD 노드 생성 (익명 클래스 내부 메서드 무시)
        if (h.getNodeStack().isEmpty()) return;
        String parentType = h.getNodeStack().peek().type;
        if (!parentType.equals("CLASS") && !parentType.equals("INTERFACE")) return;
        
        String name = null;
        if (ctx.methodHeader() != null && ctx.methodHeader().methodDeclarator() != null 
                && ctx.methodHeader().methodDeclarator().identifier() != null) {
            name = ctx.methodHeader().methodDeclarator().identifier().getText();
        }
        Node node = h.enterStatement("METHOD", name, ctx.getStart().getLine());
        
        node.signature = ParserUtils.extractSignature(ctx, h.getTokens(), "{");
        extractModifiers(node, ctx.methodModifier(), m -> m.annotation() != null);
        
        if (ctx.methodHeader() != null && ctx.methodHeader().result() != null) {
            node.returnType = ctx.methodHeader().result().getText();
        }
        
        if (ctx.methodHeader() != null && ctx.methodHeader().typeParameters() != null) {
            node.genericType = ctx.methodHeader().typeParameters().getText();
        }
        
        if (ctx.methodHeader() != null
                && ctx.methodHeader().methodDeclarator() != null
                && ctx.methodHeader().methodDeclarator().formalParameterList() != null) {
            Java20Parser.FormalParameterListContext paramList =
                    ctx.methodHeader().methodDeclarator().formalParameterList();
            node.parameters = ParserUtils.getOriginalText(paramList, h.getTokens());
        }
    }

    @Override
    public void exitMethodDeclaration(Java20Parser.MethodDeclarationContext ctx) {
        h.exitStatement("METHOD", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterInterfaceMethodDeclaration(Java20Parser.InterfaceMethodDeclarationContext ctx) {
        String name = null;
        if (ctx.methodHeader() != null && ctx.methodHeader().methodDeclarator() != null 
                && ctx.methodHeader().methodDeclarator().identifier() != null) {
            name = ctx.methodHeader().methodDeclarator().identifier().getText();
        }
        Node node = h.enterStatement("METHOD", name, ctx.getStart().getLine());
        
        node.signature = ParserUtils.extractSignature(ctx, h.getTokens(), ";");
        extractModifiers(node, ctx.interfaceMethodModifier(), m -> m.annotation() != null);
        
        if (ctx.methodHeader() != null && ctx.methodHeader().result() != null) {
            node.returnType = ctx.methodHeader().result().getText();
        }
        
        if (ctx.methodHeader() != null && ctx.methodHeader().typeParameters() != null) {
            node.genericType = ctx.methodHeader().typeParameters().getText();
        }
        
        if (ctx.methodHeader() != null
                && ctx.methodHeader().methodDeclarator() != null
                && ctx.methodHeader().methodDeclarator().formalParameterList() != null) {
            Java20Parser.FormalParameterListContext paramList =
                    ctx.methodHeader().methodDeclarator().formalParameterList();
            node.parameters = ParserUtils.getOriginalText(paramList, h.getTokens());
        }
    }

    @Override
    public void exitInterfaceMethodDeclaration(Java20Parser.InterfaceMethodDeclarationContext ctx) {
        h.exitStatement("METHOD", ctx.getStop().getLine(), ctx);
    }
    
    // ========================================
    // 필드
    // ========================================
    
    @Override
    public void enterFieldDeclaration(Java20Parser.FieldDeclarationContext ctx) {
        Node node = h.enterStatement("FIELD", null, ctx.getStart().getLine());

        extractModifiers(node, ctx.fieldModifier(), m -> m.annotation() != null);

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
        Node current = h.getNodeStack().peek();
        h.exitStatement(current != null ? current.type : "FIELD", ctx.getStop().getLine(), ctx);
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
        h.enterStatement("FUNCTION_CALL", name, ctx.getStart().getLine());
    }
    
    @Override
    public void exitMethodInvocation(Java20Parser.MethodInvocationContext ctx) {
        h.exitStatement("FUNCTION_CALL", ctx.getStop().getLine(), ctx);
    }
    
    // ========================================
    // 지역변수 선언 — VARIABLE 노드는 생성하지 않고, 초기화식에 호출/생성이 있으면 해당 타입으로 emit
    // ========================================

    @Override
    public void enterLocalVariableDeclaration(Java20Parser.LocalVariableDeclarationContext ctx) {
        if (ctx.variableDeclaratorList() == null) return;
        String raw = ctx.variableDeclaratorList().getText();
        if (raw == null || !raw.contains("=")) return;

        String initPart = raw.substring(raw.indexOf('=') + 1).trim();
        if (ParserUtils.matchesNewInstance(initPart)) {
            String className = ParserUtils.extractNewInstanceName(initPart);
            Node node = h.enterStatement("NEW_INSTANCE", className, ctx.getStart().getLine());
            node.endLine = ctx.getStop().getLine();
            h.getNodeStack().pop();
        } else if (ParserUtils.matchesMethodCall(initPart)) {
            String callName = ParserUtils.extractCallName(initPart);
            Node node = h.enterStatement("FUNCTION_CALL", callName, ctx.getStart().getLine());
            node.endLine = ctx.getStop().getLine();
            h.getNodeStack().pop();
        }
    }

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
        h.enterStatement("NEW_INSTANCE", name, ctx.getStart().getLine());
    }
    
    @Override
    public void exitClassInstanceCreationExpression(Java20Parser.ClassInstanceCreationExpressionContext ctx) {
        h.exitStatement("NEW_INSTANCE", ctx.getStop().getLine(), ctx);
    }
    
}
