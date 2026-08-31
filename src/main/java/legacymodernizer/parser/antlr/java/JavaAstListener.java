package legacymodernizer.parser.antlr.java;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import legacymodernizer.parser.parsing.AntlrParseHarness;
import legacymodernizer.parser.parsing.evidence.CallEvidenceCandidate;
import legacymodernizer.parser.parsing.evidence.CallableEvidenceExtraction;
import legacymodernizer.parser.parsing.evidence.CallableEvidenceExtraction.CallableCandidate;
import legacymodernizer.parser.parsing.evidence.CallableEvidenceExtraction.CallableSyntaxCandidate;
import legacymodernizer.parser.parsing.evidence.GrammarStructureEvidence;
import legacymodernizer.parser.parsing.evidence.GrammarStructureEvidence.IncompleteGrammarEvidence;
import legacymodernizer.parser.parsing.evidence.ImportBindingCandidate;
import legacymodernizer.parser.parsing.evidence.ImportEvidenceCandidate;
import legacymodernizer.parser.parsing.evidence.ImportEvidenceExtraction;
import legacymodernizer.parser.parsing.evidence.SourceRangeCandidate;
import legacymodernizer.parser.parsing.evidence.SyntaxComponentCandidate;
import legacymodernizer.parser.model.Node;
import legacymodernizer.parser.antlr.ListenerHelper;
import legacymodernizer.parser.antlr.ParserUtils;
import legacymodernizer.parser.service.ParseProgressTracker;

/**
 * Java 파일 → AST 변환 리스너.
 * 패키지/import/클래스/인터페이스/메서드/필드/변수/호출 구조를 추출한다.
 */
public class JavaAstListener extends Java20ParserBaseListener
        implements AntlrParseHarness.AstListener {

    private final ListenerHelper h;
    private final GrammarStructureEvidence structuralEvidence;
    private final List<CallEvidenceCandidate> callEvidence = new ArrayList<>();
    private final List<CallableCandidate> callableEvidence = new ArrayList<>();
    private int unresolvedCallableEvidence;
    private final List<ImportEvidenceCandidate> importEvidence = new ArrayList<>();

    public JavaAstListener(CommonTokenStream tokens, ParseProgressTracker tracker) {
        this.h = new ListenerHelper(tokens, tracker);
        this.structuralEvidence = new GrammarStructureEvidence(
                tokens, Java20Parser.VOCABULARY, Java20Parser.ruleNames);
    }

    public Node getRoot() { return h.getRoot(); }
    public void setFileInfo(String fileName, String filePath) { h.setFileInfo(fileName, filePath); }
    @Override public List<CallEvidenceCandidate> callEvidenceCandidates() { return List.copyOf(callEvidence); }
    @Override public ImportEvidenceExtraction importEvidenceExtraction() {
        return new ImportEvidenceExtraction(importEvidence, 0, List.of());
    }
    public CallableEvidenceExtraction callableEvidenceExtraction() {
        return new CallableEvidenceExtraction(
                "java", "antlr-java/v1", callableEvidence, unresolvedCallableEvidence,
                unresolvedCallableEvidence == 0
                        ? List.of() : List.of("insufficient_parser_recovery"));
    }

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
        ImportBindingCandidate entry;
        String grammarRule;
        String legacyName;
        if (ctx.singleTypeImportDeclaration() != null) {
            var declaration = ctx.singleTypeImportDeclaration();
            var path = declaration.typeName();
            grammarRule = "singleTypeImportDeclaration";
            legacyName = path.getText();
            entry = new ImportBindingCandidate(
                    "type", "qualified", range(path), componentRanges(path),
                    null, null, 0, false, "unspecified");
        } else if (ctx.typeImportOnDemandDeclaration() != null) {
            var declaration = ctx.typeImportOnDemandDeclaration();
            var path = declaration.packageOrTypeName();
            grammarRule = "typeImportOnDemandDeclaration";
            legacyName = path.getText() + ".*";
            entry = new ImportBindingCandidate(
                    "namespace", "qualified",
                    range(path.getStart(), declaration.MUL().getSymbol()),
                    componentRanges(path), null, null, 0, true, "unspecified");
        } else if (ctx.singleStaticImportDeclaration() != null) {
            var declaration = ctx.singleStaticImportDeclaration();
            var path = declaration.typeName();
            grammarRule = "singleStaticImportDeclaration";
            legacyName = "static " + path.getText() + "." + declaration.identifier().getText();
            entry = new ImportBindingCandidate(
                    "static_member", "qualified",
                    range(path.getStart(), declaration.identifier().getStop()),
                    componentRanges(path), range(declaration.identifier()), null,
                    0, false, "unspecified");
        } else if (ctx.staticImportOnDemandDeclaration() != null) {
            var declaration = ctx.staticImportOnDemandDeclaration();
            var path = declaration.typeName();
            grammarRule = "staticImportOnDemandDeclaration";
            legacyName = "static " + path.getText() + ".*";
            entry = new ImportBindingCandidate(
                    "static_member", "qualified",
                    range(path.getStart(), declaration.MUL().getSymbol()),
                    componentRanges(path), null, null, 0, true, "unspecified");
        } else {
            throw new IllegalStateException("importDeclaration has no grammar alternative");
        }
        h.enterStatement("IMPORT", legacyName, ctx.getStart().getLine());
        importEvidence.add(new ImportEvidenceCandidate(
                grammarRule, range(ctx), "import", List.of(entry),
                structuralEvidence.scopePath(ctx, this::scopeKind)));
    }
    
    @Override
    public void exitImportDeclaration(Java20Parser.ImportDeclarationContext ctx) {
        h.exitStatement("IMPORT", ctx.getStop().getLine(), ctx);
    }

    private static SourceRangeCandidate range(ParserRuleContext context) {
        return range(context.getStart(), context.getStop());
    }

    private static SourceRangeCandidate range(Token start, Token stop) {
        if (start == null || stop == null) {
            throw new IllegalArgumentException("ANTLR token boundary is required");
        }
        return new SourceRangeCandidate(start.getStartIndex(), stop.getStopIndex() + 1);
    }

    private static List<SourceRangeCandidate> componentRanges(ParserRuleContext path) {
        List<SourceRangeCandidate> result = new ArrayList<>();
        collectComponentRanges(path, result);
        return List.copyOf(result);
    }

    private static void collectComponentRanges(
            ParseTree tree, List<SourceRangeCandidate> result) {
        if (tree instanceof TerminalNode terminal) {
            if (!".".equals(terminal.getText())) {
                result.add(range(terminal.getSymbol(), terminal.getSymbol()));
            }
            return;
        }
        for (int index = 0; index < tree.getChildCount(); index++) {
            collectComponentRanges(tree.getChild(index), result);
        }
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
            h.exitStatement("CLASS", ctx.getStop().getLine(), ctx);
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
            h.exitStatement("INTERFACE", ctx.getStop().getLine(), ctx);
            ListenerHelper.propagateModuleName(node, node.name);
        } else {
            h.exitStatement("INTERFACE", ctx.getStop().getLine(), ctx);
        }
    }

    @Override
    public void enterRecordDeclaration(Java20Parser.RecordDeclarationContext ctx) {
        String name = ctx.typeIdentifier() != null ? ctx.typeIdentifier().getText() : null;
        Node node = h.enterStatement("CLASS", name, ctx.getStart().getLine());
        node.signature = ParserUtils.extractSignature(ctx, h.getTokens(), "{");
        extractModifiers(node, ctx.classModifier(), modifier -> modifier.annotation() != null);
        if (ctx.typeParameters() != null) {
            node.genericType = ctx.typeParameters().getText();
        }
        if (ctx.classImplements() != null
                && ctx.classImplements().interfaceTypeList() != null) {
            node.implementsTypes = ctx.classImplements().interfaceTypeList().getText();
        }
    }

    @Override
    public void exitRecordDeclaration(Java20Parser.RecordDeclarationContext ctx) {
        if (!h.getNodeStack().isEmpty() && "CLASS".equals(h.getNodeStack().peek().type)) {
            Node node = h.getNodeStack().peek();
            node.comment = ParserUtils.getLeadingComment(ctx, h.getTokens());
            h.exitStatement("CLASS", ctx.getStop().getLine(), ctx);
            ListenerHelper.propagateModuleName(node, node.name);
        } else {
            h.exitStatement("CLASS", ctx.getStop().getLine(), ctx);
        }
    }
    
    // ========================================
    // 메서드
    // ========================================
    
    @Override
    public void enterMethodDeclaration(Java20Parser.MethodDeclarationContext ctx) {
        addMethodCallable(ctx, ctx.methodHeader(), ctx.methodBody(), ctx.methodModifier());
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
        addMethodCallable(ctx, ctx.methodHeader(), ctx.methodBody(),
                ctx.interfaceMethodModifier());
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

    @Override
    public void enterConstructorDeclaration(Java20Parser.ConstructorDeclarationContext ctx) {
        var declarator = ctx.constructorDeclarator();
        if (declarator == null || declarator.simpleTypeName() == null) {
            unresolvedCallableEvidence++;
            return;
        }
        addCallable("definition", "constructorDeclaration", ctx,
                declarator.simpleTypeName(), declarator, ctx.constructorModifier(),
                ctx.throwsT() == null ? List.of() : List.of(ctx.throwsT()));

        if (h.getNodeStack().isEmpty()
                || !"CLASS".equals(h.getNodeStack().peek().type)) {
            return;
        }
        Node node = h.enterStatement(
                "METHOD", declarator.simpleTypeName().getText(), ctx.getStart().getLine());
        node.signature = ParserUtils.extractSignature(ctx, h.getTokens(), "{");
        extractModifiers(node, ctx.constructorModifier(), m -> m.annotation() != null);
        if (declarator.typeParameters() != null) {
            node.genericType = declarator.typeParameters().getText();
        }
        if (declarator.formalParameterList() != null) {
            node.parameters = ParserUtils.getOriginalText(
                    declarator.formalParameterList(), h.getTokens());
        }
    }

    @Override
    public void exitConstructorDeclaration(Java20Parser.ConstructorDeclarationContext ctx) {
        h.exitStatement("METHOD", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCompactConstructorDeclaration(
            Java20Parser.CompactConstructorDeclarationContext ctx) {
        if (ctx.simpleTypeName() == null) {
            unresolvedCallableEvidence++;
            return;
        }
        Java20Parser.RecordDeclarationContext record = enclosingRecord(ctx);
        addCallable("definition", "compactConstructorDeclaration", ctx,
                ctx.simpleTypeName(), ctx.simpleTypeName(), ctx.constructorModifier(), List.of());

        if (h.getNodeStack().isEmpty()
                || !"CLASS".equals(h.getNodeStack().peek().type)) {
            return;
        }
        Node node = h.enterStatement(
                "METHOD", ctx.simpleTypeName().getText(), ctx.getStart().getLine());
        node.signature = ParserUtils.extractSignature(ctx, h.getTokens(), "{");
        extractModifiers(node, ctx.constructorModifier(), modifier -> modifier.annotation() != null);
        if (record != null && record.recordHeader() != null
                && record.recordHeader().recordComponentList() != null) {
            node.parameters = ParserUtils.getOriginalText(
                    record.recordHeader().recordComponentList(), h.getTokens());
        }
    }

    @Override
    public void exitCompactConstructorDeclaration(
            Java20Parser.CompactConstructorDeclarationContext ctx) {
        if (!h.getNodeStack().isEmpty()
                && "METHOD".equals(h.getNodeStack().peek().type)) {
            h.exitStatement("METHOD", ctx.getStop().getLine(), ctx);
        }
    }

    private static Java20Parser.RecordDeclarationContext enclosingRecord(
            ParserRuleContext context) {
        for (ParseTree parent = context.getParent(); parent != null;
                parent = parent.getParent()) {
            if (parent instanceof Java20Parser.RecordDeclarationContext record) {
                return record;
            }
        }
        return null;
    }

    @Override
    public void enterAnnotationInterfaceElementDeclaration(
            Java20Parser.AnnotationInterfaceElementDeclarationContext ctx) {
        if (ctx.identifier() == null) {
            unresolvedCallableEvidence++;
            return;
        }
        addCallable("declaration", "annotationInterfaceElementDeclaration", ctx,
                ctx.identifier(), ctx, ctx.annotationInterfaceElementModifier(), List.of());
    }

    private void addMethodCallable(
            ParserRuleContext factContext,
            Java20Parser.MethodHeaderContext header,
            Java20Parser.MethodBodyContext body,
            List<? extends ParserRuleContext> modifiers) {
        if (header == null || header.methodDeclarator() == null
                || header.methodDeclarator().identifier() == null || body == null) {
            unresolvedCallableEvidence++;
            return;
        }
        addCallable(body.block() == null ? "declaration" : "definition",
                factContext instanceof Java20Parser.InterfaceMethodDeclarationContext
                        ? "interfaceMethodDeclaration" : "methodDeclaration",
                factContext, header.methodDeclarator().identifier(), header,
                modifiers, List.of());
    }

    private void addCallable(
            String role,
            String grammarRule,
            ParserRuleContext factContext,
            ParserRuleContext nameContext,
            ParserRuleContext declaratorContext,
            List<? extends ParserRuleContext> specifierContexts,
            List<? extends ParserRuleContext> attributeContexts) {
        try {
            var scopePath = structuralEvidence.scopePath(
                    (ParserRuleContext) factContext.getParent(), this::scopeKind);
            var factRange = structuralEvidence.range(factContext);
            var scopeRange = scopePath.get(scopePath.size() - 1).range();
            callableEvidence.add(new CallableCandidate(
                    grammarRule,
                    factRange,
                    structuralEvidence.range(nameContext),
                    role,
                    "definition".equals(role) ? factRange : null,
                    scopePath,
                    scopeRange,
                    declaratorContext.getStop().getStopIndex() + 1,
                    new CallableSyntaxCandidate(
                            "java-callable-syntax/v1",
                            syntaxComponents(specifierContexts),
                            structuralEvidence.component(declaratorContext),
                            syntaxComponents(attributeContexts))));
        } catch (IncompleteGrammarEvidence ignored) {
            unresolvedCallableEvidence++;
        }
    }

    private List<SyntaxComponentCandidate> syntaxComponents(
            List<? extends ParserRuleContext> contexts) {
        return contexts == null ? List.of() : contexts.stream()
                .map(structuralEvidence::component)
                .toList();
    }

    private String scopeKind(ParserRuleContext context) {
        if (context instanceof Java20Parser.NormalClassDeclarationContext
                || context instanceof Java20Parser.NormalInterfaceDeclarationContext
                || context instanceof Java20Parser.EnumDeclarationContext
                || context instanceof Java20Parser.RecordDeclarationContext
                || context instanceof Java20Parser.AnnotationInterfaceDeclarationContext) {
            return "class";
        }
        if (context instanceof Java20Parser.ClassBodyContext
                && context.getParent()
                        instanceof Java20Parser.UnqualifiedClassInstanceCreationExpressionContext) {
            return "class";
        }
        if (context instanceof Java20Parser.MethodDeclarationContext
                || context instanceof Java20Parser.InterfaceMethodDeclarationContext
                || context instanceof Java20Parser.ConstructorDeclarationContext
                || context instanceof Java20Parser.CompactConstructorDeclarationContext) {
            return "function";
        }
        if (context instanceof Java20Parser.BlockContext
                || context instanceof Java20Parser.ConstructorBodyContext) {
            return "block";
        }
        return null;
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
            setSingleInitValue(node, ctx.variableDeclaratorList());
        }
    }

    /**
     * 선언자가 하나일 때만 초기화식을 {@code initValue} 로 싣는다 — C/Python/PL/SQL 리스너와 동일 계약.
     * 한 줄에 여러 선언자가 있으면 이름과 값의 짝을 노드 하나로 표현할 수 없으므로 비운다(정직한 미기재).
     */
    private void setSingleInitValue(Node node, Java20Parser.VariableDeclaratorListContext list) {
        if (list.variableDeclarator().size() != 1) {
            return;
        }
        Java20Parser.VariableDeclaratorContext declarator = list.variableDeclarator(0);
        if (declarator.variableInitializer() == null) {
            return;
        }
        node.initValue = declarator.variableInitializer().getText();
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
        Token calleeStop = childBeforeDirectTerminal(ctx, "(");
        CallEvidenceCandidate candidate = CallEvidenceCandidate.fromTokens("methodInvocation",
                ctx.getStart(), ctx.getStop(), ctx.getStart(), calleeStop,
                name == null ? "expression" : "named", name,
                ctx.argumentList() == null ? List.of() : ctx.argumentList().expression());
        callEvidence.add(candidate.withStructuralContext(
                methodReceiverRange(ctx), structuralEvidence.scopePath(ctx, this::scopeKind)));
    }
    
    @Override
    public void exitMethodInvocation(Java20Parser.MethodInvocationContext ctx) {
        h.exitStatement("FUNCTION_CALL", ctx.getStop().getLine(), ctx);
    }
    
    // ========================================
    // 객체 생성 (new) — NEW_INSTANCE + FUNCTION_CALL 둘 다 emit (역할 분리)
    // 지역변수 선언은 노드로 만들지 않음 — 초기화식의 호출/생성은
    // enterMethodInvocation / enterClassInstanceCreationExpression 이 직접 잡는다.
    // ========================================

    @Override
    public void enterClassInstanceCreationExpression(Java20Parser.ClassInstanceCreationExpressionContext ctx) {
        String name = null;
        if (ctx.unqualifiedClassInstanceCreationExpression() != null
                && ctx.unqualifiedClassInstanceCreationExpression().classOrInterfaceTypeToInstantiate() != null) {
            name = ctx.unqualifiedClassInstanceCreationExpression().classOrInterfaceTypeToInstantiate().getText();
        }
        // 구현체 추적용 NEW_INSTANCE + 생성자 호출 추적용 FUNCTION_CALL (형제 노드)
        Node ni = h.enterStatement("NEW_INSTANCE", name, ctx.getStart().getLine());
        Node call = new Node("FUNCTION_CALL", name, ctx.getStart().getLine(), ni.parent);
        call.endLine = ctx.getStop().getLine();
    }

    @Override
    public void enterUnqualifiedClassInstanceCreationExpression(
            Java20Parser.UnqualifiedClassInstanceCreationExpressionContext context) {
        Token calleeStop = childBeforeDirectTerminal(context, "(");
        List<Java20Parser.IdentifierContext> typeNames =
                context.classOrInterfaceTypeToInstantiate() == null ? List.of()
                        : context.classOrInterfaceTypeToInstantiate().identifier();
        String terminalName = typeNames.isEmpty()
                ? null : typeNames.get(typeNames.size() - 1).getText();
        CallEvidenceCandidate candidate = CallEvidenceCandidate.fromTokens(
                "unqualifiedClassInstanceCreationExpression",
                context.getStart(), directTerminal(context, ")"),
                context.getStart(), calleeStop,
                terminalName == null ? "expression" : "constructor", terminalName,
                context.argumentList() == null ? List.of() : context.argumentList().expression());
        candidate = candidate.withCalleeStructure(
                typeNames.stream().map(structuralEvidence::range).toList(), null);
        callEvidence.add(candidate.withStructuralContext(
                null, structuralEvidence.scopePath(context, this::scopeKind)));
    }

    private static Token childBeforeDirectTerminal(ParserRuleContext context, String text) {
        List<ParseTree> children = context.children == null ? List.of() : context.children;
        for (int index = 1; index < children.size(); index++) {
            ParseTree child = children.get(index);
            if (child instanceof TerminalNode terminal && text.equals(terminal.getText())) {
                ParseTree previous = children.get(index - 1);
                if (previous instanceof TerminalNode previousTerminal) return previousTerminal.getSymbol();
                if (previous instanceof ParserRuleContext previousContext) return previousContext.getStop();
            }
        }
        throw new IllegalStateException("missing direct terminal " + text + " in "
                + context.getClass().getSimpleName());
    }

    private SourceRangeCandidate methodReceiverRange(
            Java20Parser.MethodInvocationContext context) {
        List<ParseTree> children = context.children == null ? List.of() : context.children;
        Token receiverStop = null;
        for (int index = 1; index < children.size(); index++) {
            ParseTree child = children.get(index);
            if (!(child instanceof TerminalNode terminal)
                    || !".".equals(terminal.getText())) {
                continue;
            }
            ParseTree previous = children.get(index - 1);
            receiverStop = previous instanceof TerminalNode previousTerminal
                    ? previousTerminal.getSymbol()
                    : previous instanceof ParserRuleContext previousContext
                            ? previousContext.getStop() : null;
        }
        return receiverStop == null ? null
                : structuralEvidence.range(context.getStart(), receiverStop);
    }

    private static Token directTerminal(ParserRuleContext context, String text) {
        List<ParseTree> children = context.children == null ? List.of() : context.children;
        for (ParseTree child : children) {
            if (child instanceof TerminalNode terminal && text.equals(terminal.getText())) {
                return terminal.getSymbol();
            }
        }
        throw new IllegalStateException("missing direct terminal " + text + " in "
                + context.getClass().getSimpleName());
    }
    
    @Override
    public void exitClassInstanceCreationExpression(Java20Parser.ClassInstanceCreationExpressionContext ctx) {
        h.exitStatement("NEW_INSTANCE", ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // 제어 흐름 의미 AST (spec 007 계약의 Java 구현): IF / ELSE / LOOP / SWITCH / CASE / TRY / CATCH
    //
    // C 리스너와 동일 원칙: 메서드 안에서만 emit, for/while/do 는 구분 없이 LOOP,
    // else-if 는 ELSE 아래 중첩 IF 로 자연 표현(인위적 평탄화 없음), finally 는 TRY 본문에 포함.
    // 기존 FUNCTION_CALL/NEW_INSTANCE emit 은 불변 — 제어문 아래로 자연 중첩된다.
    // switch expression(값으로 쓰는 switch)은 이번 범위 밖(계약 v2.0 SWITCH=문장) — 미지원 명시.
    // ========================================

    private boolean isInsideRoutine() {
        for (int i = h.getNodeStack().size() - 1; i >= 0; i--) {
            String t = h.getNodeStack().get(i).type;
            if ("METHOD".equals(t) || "FUNCTION".equals(t)) return true;
        }
        return false;
    }

    @Override
    public void enterIfThenStatement(Java20Parser.IfThenStatementContext ctx) {
        if (!isInsideRoutine()) return;
        // 조건식 원문 보존 (spec 016 FR-003) — 이하 IF/LOOP/SWITCH/CASE 동일.
        h.enterStatement("IF", ctx.getStart().getLine())
                .expression = ParserUtils.getExactSourceText(ctx.expression());
    }

    @Override
    public void exitIfThenStatement(Java20Parser.IfThenStatementContext ctx) {
        if (isInsideRoutine()) h.exitStatementWithFullComment("IF", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterIfThenElseStatement(Java20Parser.IfThenElseStatementContext ctx) {
        if (!isInsideRoutine()) return;
        h.enterStatement("IF", ctx.getStart().getLine())
                .expression = ParserUtils.getExactSourceText(ctx.expression());
    }

    @Override
    public void exitIfThenElseStatement(Java20Parser.IfThenElseStatementContext ctx) {
        if (isInsideRoutine()) h.exitStatementWithFullComment("IF", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterIfThenElseStatementNoShortIf(Java20Parser.IfThenElseStatementNoShortIfContext ctx) {
        if (!isInsideRoutine()) return;
        h.enterStatement("IF", ctx.getStart().getLine())
                .expression = ParserUtils.getExactSourceText(ctx.expression());
    }

    @Override
    public void exitIfThenElseStatementNoShortIf(Java20Parser.IfThenElseStatementNoShortIfContext ctx) {
        if (isInsideRoutine()) h.exitStatementWithFullComment("IF", ctx.getStop().getLine(), ctx);
    }

    /** ctx 가 if-then-else 의 else 분기인지 — then 분기·중첩 statement 와 구분한다. */
    private static boolean isElseBranch(org.antlr.v4.runtime.ParserRuleContext ctx) {
        org.antlr.v4.runtime.ParserRuleContext p = ctx.getParent();
        if (p instanceof Java20Parser.IfThenElseStatementContext) {
            return ((Java20Parser.IfThenElseStatementContext) p).statement() == ctx;
        }
        if (p instanceof Java20Parser.IfThenElseStatementNoShortIfContext) {
            Java20Parser.IfThenElseStatementNoShortIfContext sel =
                    (Java20Parser.IfThenElseStatementNoShortIfContext) p;
            return sel.statementNoShortIf().size() >= 2 && sel.statementNoShortIf(1) == ctx;
        }
        return false;
    }

    @Override
    public void enterStatement(Java20Parser.StatementContext ctx) {
        if (isInsideRoutine() && isElseBranch(ctx)) h.enterStatement("ELSE", ctx.getStart().getLine());
    }

    @Override
    public void exitStatement(Java20Parser.StatementContext ctx) {
        if (isInsideRoutine() && isElseBranch(ctx)) h.exitStatementWithFullComment("ELSE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterStatementNoShortIf(Java20Parser.StatementNoShortIfContext ctx) {
        if (isInsideRoutine() && isElseBranch(ctx)) h.enterStatement("ELSE", ctx.getStart().getLine());
    }

    @Override
    public void exitStatementNoShortIf(Java20Parser.StatementNoShortIfContext ctx) {
        if (isInsideRoutine() && isElseBranch(ctx)) h.exitStatementWithFullComment("ELSE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterWhileStatement(Java20Parser.WhileStatementContext ctx) {
        if (!isInsideRoutine()) return;
        h.enterStatement("LOOP", ctx.getStart().getLine())
                .expression = ParserUtils.getExactSourceText(ctx.expression());
    }

    @Override
    public void exitWhileStatement(Java20Parser.WhileStatementContext ctx) {
        if (isInsideRoutine()) h.exitStatementWithFullComment("LOOP", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterWhileStatementNoShortIf(Java20Parser.WhileStatementNoShortIfContext ctx) {
        if (!isInsideRoutine()) return;
        h.enterStatement("LOOP", ctx.getStart().getLine())
                .expression = ParserUtils.getExactSourceText(ctx.expression());
    }

    @Override
    public void exitWhileStatementNoShortIf(Java20Parser.WhileStatementNoShortIfContext ctx) {
        if (isInsideRoutine()) h.exitStatementWithFullComment("LOOP", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterDoStatement(Java20Parser.DoStatementContext ctx) {
        if (!isInsideRoutine()) return;
        Node node = h.enterStatement("LOOP", ctx.getStart().getLine());
        node.expression = ParserUtils.getExactSourceText(ctx.expression());
        node.conditionTiming = "post";
    }

    @Override
    public void exitDoStatement(Java20Parser.DoStatementContext ctx) {
        if (isInsideRoutine()) h.exitStatementWithFullComment("LOOP", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterForStatement(Java20Parser.ForStatementContext ctx) {
        // basic/enhanced for 의 공통 부모 — 여기 한 곳만 hook (자식까지 걸면 이중 LOOP)
        if (!isInsideRoutine()) return;
        Node node = h.enterStatement("LOOP", ctx.getStart().getLine());
        // basic for 는 grammar 가 ';' 로 구분한 test 절만, enhanced for 는 반복 대상 식.
        // 초기화/증감은 도달 기계장치라 조건이 아니다(TA-102).
        if (ctx.basicForStatement() != null) {
            node.expression = ParserUtils.getExactSourceText(ctx.basicForStatement().expression());
        } else if (ctx.enhancedForStatement() != null) {
            node.expression = ParserUtils.getExactSourceText(ctx.enhancedForStatement().expression());
        }
    }

    @Override
    public void exitForStatement(Java20Parser.ForStatementContext ctx) {
        if (isInsideRoutine()) h.exitStatementWithFullComment("LOOP", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterForStatementNoShortIf(Java20Parser.ForStatementNoShortIfContext ctx) {
        if (!isInsideRoutine()) return;
        Node node = h.enterStatement("LOOP", ctx.getStart().getLine());
        if (ctx.basicForStatementNoShortIf() != null) {
            node.expression = ParserUtils.getExactSourceText(
                    ctx.basicForStatementNoShortIf().expression());
        } else if (ctx.enhancedForStatementNoShortIf() != null) {
            node.expression = ParserUtils.getExactSourceText(
                    ctx.enhancedForStatementNoShortIf().expression());
        }
    }

    @Override
    public void exitForStatementNoShortIf(Java20Parser.ForStatementNoShortIfContext ctx) {
        if (isInsideRoutine()) h.exitStatementWithFullComment("LOOP", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterSwitchStatement(Java20Parser.SwitchStatementContext ctx) {
        if (!isInsideRoutine()) return;
        h.enterStatement("SWITCH", ctx.getStart().getLine())
                .expression = ParserUtils.getExactSourceText(ctx.expression());
    }

    @Override
    public void exitSwitchStatement(Java20Parser.SwitchStatementContext ctx) {
        if (isInsideRoutine()) h.exitStatementWithFullComment("SWITCH", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterSwitchBlockStatementGroup(Java20Parser.SwitchBlockStatementGroupContext ctx) {
        // case/default 라벨 + 본문 묶음 = CASE (C 의 case/default 와 동일 의미 단위)
        if (!isInsideRoutine()) return;
        Node node = h.enterStatement("CASE", ctx.getStart().getLine());
        // 라벨 상수 원문 보존 — 첫 라벨이 case 상수를 갖는 경우만 (default 는 null).
        Java20Parser.SwitchLabelContext label = ctx.switchLabel(0);
        if (label != null && !label.caseConstant().isEmpty()) {
            node.expression = ParserUtils.getExactSourceText(
                    label.caseConstant(0), label.caseConstant(label.caseConstant().size() - 1));
        }
    }

    @Override
    public void exitSwitchBlockStatementGroup(Java20Parser.SwitchBlockStatementGroupContext ctx) {
        if (isInsideRoutine()) h.exitStatementWithFullComment("CASE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterTryStatement(Java20Parser.TryStatementContext ctx) {
        // try-with-resources 도 이 규칙의 대안으로 파싱되므로 여기 한 곳으로 충분.
        if (isInsideRoutine()) h.enterStatement("TRY", ctx.getStart().getLine());
    }

    @Override
    public void exitTryStatement(Java20Parser.TryStatementContext ctx) {
        if (isInsideRoutine()) h.exitStatementWithFullComment("TRY", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCatchClause(Java20Parser.CatchClauseContext ctx) {
        if (isInsideRoutine()) h.enterStatement("CATCH", ctx.getStart().getLine());
    }

    @Override
    public void exitCatchClause(Java20Parser.CatchClauseContext ctx) {
        if (isInsideRoutine()) h.exitStatementWithFullComment("CATCH", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterFinallyBlock(Java20Parser.FinallyBlockContext ctx) {
        // 무조건 실행 arm — 자원 정리 의미가 함수 내부 분석에서 소실되지 않도록 emit.
        // analyzer 소비(분기 역할)는 첫 Java corpus 의 red 테스트와 함께 확정한다.
        if (isInsideRoutine()) h.enterStatement("FINALLY", ctx.getStart().getLine());
    }

    @Override
    public void exitFinallyBlock(Java20Parser.FinallyBlockContext ctx) {
        if (isInsideRoutine()) h.exitStatementWithFullComment("FINALLY", ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // 구조 statement — return/throw/break/continue (spec 016)
    // ========================================
    // leaf(스택 비진입) emit — 표현식 안의 METHOD 호출 등 기존 자식은 종전 부모에
    // 그대로 붙어 기존 노드 수·부모가 보존된다(FR-009). 표현식 원문은 expression
    // 필드로 보존한다(FR-003). throw 는 return 과 다른 노드다(FR-006).

    @Override
    public void enterReturnStatement(Java20Parser.ReturnStatementContext ctx) {
        if (!isInsideRoutine()) return;
        Node node = h.addLeafStatement(
                "RETURN", null, ctx.getStart().getLine(), ctx.getStop().getLine());
        node.expression = ParserUtils.getExactSourceText(ctx.expression());
    }

    @Override
    public void enterThrowStatement(Java20Parser.ThrowStatementContext ctx) {
        if (!isInsideRoutine()) return;
        Node node = h.addLeafStatement(
                "THROW", null, ctx.getStart().getLine(), ctx.getStop().getLine());
        node.expression = ParserUtils.getExactSourceText(ctx.expression());
    }

    @Override
    public void enterBreakStatement(Java20Parser.BreakStatementContext ctx) {
        if (!isInsideRoutine()) return;
        // 'break' Identifier? ';' — 라벨이 있으면 child(1)이 라벨이다.
        String label = ctx.getChildCount() >= 3 ? ctx.getChild(1).getText() : null;
        h.addLeafStatement("BREAK", label, ctx.getStart().getLine(), ctx.getStop().getLine());
    }

    @Override
    public void enterContinueStatement(Java20Parser.ContinueStatementContext ctx) {
        if (!isInsideRoutine()) return;
        String label = ctx.getChildCount() >= 3 ? ctx.getChild(1).getText() : null;
        h.addLeafStatement("CONTINUE", label, ctx.getStart().getLine(), ctx.getStop().getLine());
    }

    /**
     * 지역 선언 초기화(`int rc = f();`)는 실행 효과다 (spec 016) — 선언 자체는 노드가
     * 아니지만 초기화는 ASSIGNMENT leaf 로 보존한다. for 머리(forInit)의 선언은
     * localVariableDeclarationStatement 문맥이 아니라서 제외된다 (TA-102 정합).
     */
    @Override
    public void enterLocalVariableDeclaration(Java20Parser.LocalVariableDeclarationContext ctx) {
        if (!isInsideRoutine()) return;
        if (!(ctx.getParent() instanceof Java20Parser.LocalVariableDeclarationStatementContext)) {
            return;
        }
        for (Java20Parser.VariableDeclaratorContext declarator
                : ctx.variableDeclaratorList().variableDeclarator()) {
            if (declarator.variableInitializer() == null) continue;
            Node node = h.addLeafStatement(
                    "ASSIGNMENT", null, ctx.getStart().getLine(), ctx.getStop().getLine());
            node.target = declarator.variableDeclaratorId().getText();
            node.operator = "=";
            node.expression = ParserUtils.getExactSourceText(declarator.variableInitializer());
        }
    }

    /**
     * statement-level 대입만 ASSIGNMENT leaf 로 emit 한다 (spec 016).
     * for 머리(forInit/forUpdate)의 대입은 statementExpressionList 문맥이라 제외된다 —
     * 도달 기계장치이지 문장 효과가 아니다 (TA-102 정합).
     */
    @Override
    public void enterAssignment(Java20Parser.AssignmentContext ctx) {
        if (!isInsideRoutine()) return;
        if (!(ctx.getParent() instanceof Java20Parser.StatementExpressionContext)) return;
        if (!(ctx.getParent().getParent() instanceof Java20Parser.ExpressionStatementContext)) {
            return;
        }
        Node node = h.addLeafStatement(
                "ASSIGNMENT", null, ctx.getStart().getLine(), ctx.getStop().getLine());
        node.target = ParserUtils.getExactSourceText(ctx.leftHandSide());
        node.operator = ctx.assignmentOperator().getText();
        node.expression = ParserUtils.getExactSourceText(ctx.expression());
    }

}
