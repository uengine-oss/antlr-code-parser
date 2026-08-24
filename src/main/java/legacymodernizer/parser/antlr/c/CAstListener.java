package legacymodernizer.parser.antlr.c;

import java.util.ArrayList;
import java.util.List;

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
import legacymodernizer.parser.parsing.evidence.ConditionalCompilationEvidence;
import legacymodernizer.parser.parsing.evidence.ScopeEvidenceCandidate;
import legacymodernizer.parser.parsing.evidence.SourceRangeCandidate;
import legacymodernizer.parser.parsing.evidence.SyntaxComponentCandidate;
import legacymodernizer.parser.parsing.evidence.SyntaxTokenCandidate;
import legacymodernizer.parser.parsing.evidence.StructuralExpressionEvidenceExtraction;
import legacymodernizer.parser.parsing.evidence.StructuralExpressionEvidenceExtraction.ExpressionCandidate;
import legacymodernizer.parser.parsing.languages.c.CPreprocessorEvidenceExtractor;
import legacymodernizer.parser.parsing.languages.c.CPreprocessorLegacyAstAdapter;
import legacymodernizer.parser.parsing.languages.c.CPreprocessorSyntax;
import legacymodernizer.parser.model.Node;
import legacymodernizer.parser.antlr.ListenerHelper;
import legacymodernizer.parser.antlr.ParserUtils;
import legacymodernizer.parser.service.ParseProgressTracker;

/**
 * C 파일 분석을 위한 커스텀 리스너
 * - #include는 전처리기 지시문(HIDDEN 채널)이므로 별도 추출
 * - struct/union → STRUCT 노드
 * - enum → ENUM 노드
 * - functionDefinition → FUNCTION 노드
 * - 전역 declaration → GLOBAL_VARIABLE 또는 TYPEDEF 노드
 * - 지역 declaration (블록 내) → VARIABLE 노드
 * - 함수 호출 → FUNCTION_CALL 노드 (postfixExpression 기반)
 */
public class CAstListener extends CParserBaseListener
        implements AntlrParseHarness.AstListener {

    private final ListenerHelper h;
    private final List<CallEvidenceCandidate> callEvidence = new ArrayList<>();
    private final List<CallableCandidate> callableEvidence = new ArrayList<>();
    private int unresolvedCallableEvidence;
    private final List<ExpressionCandidate> structuralExpressionEvidence = new ArrayList<>();
    private int unresolvedStructuralExpressionEvidence;
    private final ConditionalCompilationEvidence conditionalEvidence;
    private final int sourceLength;

    /**
     * typedef struct { ... } Name; 패턴에서 Name을 임시 보관.
     * enterDeclaration이 먼저 실행되어 이름을 저장하면,
     * 이후 enterStructOrUnionSpecifier / enterEnumSpecifier 에서 사용한다.
     */
    private String pendingTypedefName = null;

    public Node getRoot() {
        return h.getRoot();
    }

    @Override
    public List<CallEvidenceCandidate> callEvidenceCandidates() {
        return List.copyOf(callEvidence);
    }

    public CallableEvidenceExtraction callableEvidenceExtraction() {
        return new CallableEvidenceExtraction(
                "c", "antlr-c/v1", callableEvidence, unresolvedCallableEvidence,
                unresolvedCallableEvidence == 0
                        ? List.of() : List.of("insufficient_parser_recovery"));
    }

    public StructuralExpressionEvidenceExtraction structuralExpressionEvidenceExtraction() {
        return new StructuralExpressionEvidenceExtraction(
                structuralExpressionEvidence,
                unresolvedStructuralExpressionEvidence,
                unresolvedStructuralExpressionEvidence == 0
                        ? List.of() : List.of("insufficient_parser_recovery"));
    }

    @Override
    public ConditionalCompilationEvidence conditionalCompilationEvidence() {
        return conditionalEvidence;
    }

    @Override
    public void finalizeAst() {
        normalizeSwitchCases(h.getRoot());
    }

    public void setFileInfo(String fileName, String filePath) {
        h.setFileInfo(fileName, filePath);
    }

    public CAstListener(CommonTokenStream tokens, ParseProgressTracker tracker) {
        this(tokens, tracker, tokens.getTokenSource().getInputStream().toString(),
                CPreprocessorEvidenceExtractor.extract(
                        tokens.getTokenSource().getInputStream().toString()));
    }

    public CAstListener(CommonTokenStream tokens, ParseProgressTracker tracker,
                        String source, CPreprocessorSyntax preprocessorSyntax) {
        this.h = new ListenerHelper(tokens, tracker);
        this.conditionalEvidence = preprocessorSyntax.conditional();
        this.sourceLength = source.codePointCount(0, source.length());
        extractFileHeaderComment();
        CPreprocessorLegacyAstAdapter.appendIncludes(
                h.getRoot(), source, preprocessorSyntax.imports());
        CPreprocessorLegacyAstAdapter.appendDefines(
                h.getRoot(), source, preprocessorSyntax.macros());
    }

    /**
     * 파일 최상단 주석 블록을 FILE 노드의 comment로 저장한다.
     *
     * C 파일은 클래스 개념이 없어 파일 자체가 모듈이며, 첫 주석은 통상
     * 모듈 목적·작성자·변경이력 등을 담은 모듈 차원 문서이다.
     * 이 정보는 자식 함수들의 부모 컨텍스트로 활용된다.
     *
     * 동작: 코드 토큰이 등장하기 전까지 BlockComment / LineComment를
     * 줄 단위로 모아 첫 연속 주석 블록만 root.comment에 저장한다.
     */
    private void extractFileHeaderComment() {
        h.getTokens().fill();
        StringBuilder header = new StringBuilder();
        int prevCommentLine = -2;
        for (Token token : h.getTokens().getTokens()) {
            int type = token.getType();
            if (type == CLexer.BlockComment || type == CLexer.LineComment) {
                int line = token.getLine();
                if (header.length() == 0 || line - prevCommentLine <= 1) {
                    header.append(token.getText()).append("\n");
                    prevCommentLine = line;
                } else {
                    break; // 코드 사이에 끼어든 두 번째 주석 블록은 무시
                }
            } else if (type != CLexer.Directive
                    && type != Token.EOF
                    && !token.getText().trim().isEmpty()) {
                break; // 코드 토큰 등장 → 헤더 종료
            }
        }
        if (header.length() > 0) {
            h.getRoot().comment = header.toString().trim();
        }
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        h.checkProgress(ctx);
    }

    // ========================================
    // struct / union
    // ========================================

    @Override
    public void enterStructOrUnionSpecifier(CParser.StructOrUnionSpecifierContext ctx) {
        // 본문이 없으면 (forward declaration 또는 타입 참조) 무시
        if (ctx.memberDeclarationList() == null) return;

        String structOrUnion = ctx.structOrUnion().getText(); // "struct" or "union"
        String type = structOrUnion.equals("union") ? "UNION" : "STRUCT";

        String name;
        if (ctx.Identifier() != null) {
            // Named struct: struct Subscriber { ... };
            name = ctx.Identifier().getText();
        } else if (pendingTypedefName != null) {
            // Anonymous typedef struct: typedef struct { ... } Subscriber;
            name = pendingTypedefName;
        } else {
            return; // 이름 없는 익명 struct (typedef도 아님) → 무시
        }

        Node node = h.enterStatement(type, name, ctx.getStart().getLine());
        node.signature = ParserUtils.extractSignature(ctx, h.getTokens(), "{");
    }

    @Override
    public void exitStructOrUnionSpecifier(CParser.StructOrUnionSpecifierContext ctx) {
        if (ctx.memberDeclarationList() == null) return;

        String structOrUnion = ctx.structOrUnion().getText();
        String type = structOrUnion.equals("union") ? "UNION" : "STRUCT";

        if (!h.getNodeStack().isEmpty() && h.getNodeStack().peek().type.equals(type)) {
            Node node = h.getNodeStack().peek();
            node.comment = ParserUtils.getComment(ctx, h.getTokens());
            h.exitStatementWithFullComment(type, ctx.getStop().getLine(), ctx);
            ListenerHelper.propagateModuleName(node, node.name);
        }
        // typedef struct 처리 완료 시 pending 이름 초기화
        pendingTypedefName = null;
    }

    // ========================================
    // enum
    // ========================================

    @Override
    public void enterEnumSpecifier(CParser.EnumSpecifierContext ctx) {
        if (ctx.enumeratorList() == null) return; // body 없으면 무시

        String name;
        if (ctx.Identifier() != null) {
            // Named enum: enum UsageType { ... };
            name = ctx.Identifier().getText();
        } else if (pendingTypedefName != null) {
            // Anonymous typedef enum: typedef enum { ... } UsageType;
            name = pendingTypedefName;
        } else {
            return; // 이름 없는 익명 enum → 무시
        }

        Node node = h.enterStatement("ENUM", name, ctx.getStart().getLine());
        node.signature = ParserUtils.extractSignature(ctx, h.getTokens(), "{");
    }

    @Override
    public void exitEnumSpecifier(CParser.EnumSpecifierContext ctx) {
        if (ctx.enumeratorList() == null) return;

        h.exitStatementWithFullComment("ENUM", ctx.getStop().getLine(), ctx);
        // typedef enum 처리 완료 시 pending 이름 초기화
        pendingTypedefName = null;
    }

    // ========================================
    // 함수 정의
    // ========================================

    @Override
    public void enterFunctionDefinition(CParser.FunctionDefinitionContext ctx) {
        Token nameToken = extractDeclaratorNameToken(ctx.declarator());
        String name = nameToken == null ? null : nameToken.getText();

        // returnType, modifiers 분리 추출
        String returnType = extractReturnType(ctx.declarationSpecifiers());
        String modifiers = extractModifiers(ctx.declarationSpecifiers());

        // 포인터 * 확인 (declarator에 pointer가 있으면 리턴 타입에 포함)
        if (ctx.declarator() != null && !ctx.declarator().pointer().isEmpty()) {
            returnType = returnType + " *";
        }

        Node node = h.enterStatement("FUNCTION", name, ctx.getStart().getLine());
        node.signature = ParserUtils.extractSignature(ctx, h.getTokens(), "{");
        node.returnType = returnType != null ? returnType.trim() : null;
        if (modifiers != null) {
            node.modifiers = modifiers;
        }
        if (nameToken != null) {
            addCallable("definition", "functionDefinition", ctx, nameToken,
                    ctx.declarationSpecifiers(), ctx.declarator(), true,
                    enclosingCompoundStatement(ctx.getParent() instanceof ParserRuleContext
                            ? (ParserRuleContext) ctx.getParent() : null));
        }

        // 파라미터 텍스트 추출 (시그니처 표시용 — 별도 PARAMETER 노드는 생성하지 않음)
        if (ctx.declarator() != null && ctx.declarator().directDeclarator() != null) {
            CParser.DirectDeclaratorContext dd = ctx.declarator().directDeclarator();
            if (dd.parameterTypeList() != null && !dd.parameterTypeList().isEmpty()) {
                CParser.ParameterTypeListContext params = dd.parameterTypeList(0);
                node.parameters = ParserUtils.getOriginalText(params, h.getTokens());
            }
        }
    }

    @Override
    public void exitFunctionDefinition(CParser.FunctionDefinitionContext ctx) {
        h.exitStatementWithFullComment("FUNCTION", ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // 전역 선언 (변수, typedef)
    // ========================================

    @Override
    public void enterDeclaration(CParser.DeclarationContext ctx) {
        if (ctx.declarationSpecifiers() == null) return;

        // 블록 내부의 declaration은 VARIABLE, 전역은 GLOBAL_VARIABLE 또는 TYPEDEF
        boolean isGlobal = isGlobalScope();
        TypedefShape shape = classifyTypedef(ctx.declarationSpecifiers());
        String variableType = extractReturnType(ctx.declarationSpecifiers());
        // typedef는 modifiers에서 제외 (별도 처리)
        String modifierStr = withoutTypedefModifier(extractModifiers(ctx.declarationSpecifiers()));

        // typedef struct { ... } Name; / typedef enum { ... } Name; 패턴:
        // TYPEDEF 노드를 만들지 않고, 이름만 저장하여 struct/enum 핸들러에서 사용
        if (shape.isTypedef() && shape.hasStructOrEnumBody()) {
            pendingTypedefName = locateTypedefName(ctx);
            if (pendingTypedefName != null) {
                return; // TYPEDEF 노드 생성 안 함 → struct/enum 핸들러가 처리
            }
        }

        if (ctx.initDeclaratorList() == null) return;
        for (CParser.InitDeclaratorContext initDecl : ctx.initDeclaratorList().initDeclarator()) {
            emitDeclarator(ctx, initDecl, shape.isTypedef(), isGlobal,
                    variableType, modifierStr,
                    isGlobal ? null : enclosingCompoundStatement(ctx));
        }
    }

    /** typedef 여부와 struct/enum 본문 동반 여부 — typedef struct {…} Name; 분기 판별용. */
    private record TypedefShape(boolean isTypedef, boolean hasStructOrEnumBody) { }

    private static TypedefShape classifyTypedef(CParser.DeclarationSpecifiersContext specifiers) {
        boolean isTypedef = false;
        boolean hasStructOrEnumBody = false;
        for (CParser.DeclarationSpecifierContext ds : specifiers.declarationSpecifier()) {
            if (ds.storageClassSpecifier() != null && ds.storageClassSpecifier().getText().equals("typedef")) {
                isTypedef = true;
            }
            if (ds.typeSpecifier() != null) {
                CParser.TypeSpecifierContext ts = ds.typeSpecifier();
                if (ts.structOrUnionSpecifier() != null
                        && ts.structOrUnionSpecifier().memberDeclarationList() != null) {
                    hasStructOrEnumBody = true;
                }
                if (ts.enumSpecifier() != null
                        && ts.enumSpecifier().enumeratorList() != null) {
                    hasStructOrEnumBody = true;
                }
            }
        }
        return new TypedefShape(isTypedef, hasStructOrEnumBody);
    }

    private static String withoutTypedefModifier(String modifiers) {
        if (modifiers == null) return null;
        String cleaned = modifiers.replace("typedef", "").trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    /** typedef struct/enum 의 별칭 이름 — 선언자 우선, 없으면 마지막 typedefName 스펙에서. */
    private String locateTypedefName(CParser.DeclarationContext ctx) {
        if (ctx.initDeclaratorList() != null) {
            for (CParser.InitDeclaratorContext initDecl : ctx.initDeclaratorList().initDeclarator()) {
                return extractDeclaratorName(initDecl.declarator());
            }
            return null;
        }
        // ANTLR이 typedef 이름을 typeSpecifier(typedefName)로 파싱한 경우
        List<CParser.DeclarationSpecifierContext> specs = ctx.declarationSpecifiers().declarationSpecifier();
        for (int i = specs.size() - 1; i >= 0; i--) {
            CParser.DeclarationSpecifierContext ds = specs.get(i);
            if (ds.typeSpecifier() != null && ds.typeSpecifier().typedefName() != null) {
                return ds.typeSpecifier().typedefName().getText();
            }
        }
        return null;
    }

    /** 선언자 1개를 TYPEDEF/CONSTANT_FIELD/GLOBAL_VARIABLE 노드로 emit (해당 없으면 무시). */
    private void emitDeclarator(CParser.DeclarationContext ctx, CParser.InitDeclaratorContext initDecl,
                                boolean isTypedef, boolean isGlobal,
                                String variableType, String modifierStr,
                                ParserRuleContext lexicalScope) {
        String name = extractDeclaratorName(initDecl.declarator());
        if (name == null) return;

        // 함수 프로토타입 감지: declarator에 parameterTypeList가 있으면 함수 선언
        boolean isFunctionPrototype = isFunctionDeclarator(initDecl.declarator());
        String parameters = null;
        if (initDecl.declarator() != null && initDecl.declarator().directDeclarator() != null) {
            CParser.DirectDeclaratorContext dd = initDecl.declarator().directDeclarator();
            if (!dd.parameterTypeList().isEmpty()) {
                parameters = ParserUtils.getOriginalText(dd.parameterTypeList(0), h.getTokens());
            }
        }

        // 포인터 확인
        String actualVariableType = variableType;
        if (initDecl.declarator() != null && !initDecl.declarator().pointer().isEmpty()) {
            actualVariableType = (actualVariableType != null ? actualVariableType : "") + " *";
            actualVariableType = actualVariableType.trim();
        }

        boolean isConst = actualVariableType != null && actualVariableType.contains("const");
        String nodeType;
        if (isTypedef) {
            nodeType = "TYPEDEF";
        } else if (isFunctionPrototype) {
            Token nameToken = extractDeclaratorNameToken(initDecl.declarator());
            if (nameToken != null) {
                addCallable("declaration", "declaration", ctx, nameToken,
                        ctx.declarationSpecifiers(), initDecl.declarator(), false,
                        lexicalScope);
            }
            return;
        } else if (isConst) {
            nodeType = "CONSTANT_FIELD";
        } else if (isGlobal) {
            nodeType = "GLOBAL_VARIABLE";
        } else {
            // 지역 선언 자체는 노드가 아니지만, 초기화(`int rc = init();`)는 실행
            // 효과이므로 ASSIGNMENT leaf 로 보존한다 (spec 016 — lvalue 는 타입 제거).
            if (initDecl.initializer() != null && isInsideFunction()) {
                Node assignment = h.addLeafStatement(
                        "ASSIGNMENT", null, ctx.getStart().getLine(), ctx.getStop().getLine());
                assignment.target = name;
                assignment.operator = "=";
                assignment.expression = ParserUtils.getExactSourceText(initDecl.initializer());
                assignment.statementOrigin = "declaration_initializer";
                emitStructuralExpression(
                        "initializer_value", initDecl.initializer(), initDecl);
            }
            return;
        }

        Node node = h.enterStatement(nodeType, name, ctx.getStart().getLine());
        node.variableType = actualVariableType;
        if (isFunctionPrototype) {
            node.returnType = actualVariableType;
            node.parameters = parameters;
            node.variableType = null;
        }
        if (modifierStr != null) {
            node.modifiers = modifierStr;
        }
        // 초기화 정보(배열 size + initializer) 통합 → initValue.
        // C 의 `static char x [LEN +1]` 처럼 = 우변 없이 size 만 있는 경우도
        // 정의 시 외부 참조(LEN) 가 있으니 initValue 로 박아 reader 가 INIT_BY
        // 엣지를 만들 수 있게 한다. 호출 emit 은 실제 initializer 가 있을 때만.
        if (!isFunctionPrototype && !isTypedef) {
            String sizeText = extractArraySize(initDecl.declarator(), h.getTokens());
            String initText = (initDecl.initializer() != null)
                    ? ParserUtils.getOriginalText(initDecl.initializer(), h.getTokens())
                    : "";
            String combined = joinNonEmpty(sizeText, initText);
            if (!combined.isEmpty()) {
                node.initValue = combined;
            }
            if (!initText.isEmpty()) {
                ParserUtils.emitInitializerCall(
                        node, initText, node.startLine, ctx.getStop().getLine());
            }
        }

        // 즉시 닫기 (declaration은 한 줄)
        node.endLine = ctx.getStop().getLine();
        node.comment = ParserUtils.getComment(ctx, h.getTokens());
        h.getNodeStack().pop();
    }

    // ========================================
    // 함수 호출 (postfixExpression에서 '(' 감지)
    // ========================================

    @Override
    public void enterPostfixExpression(CParser.PostfixExpressionContext ctx) {
        emitStatementLevelPostfixUpdate(ctx);
        // postfixExpression: (primaryExpression | ...) ( '[' ... | '(' argumentExpressionList? ')' | ... )*
        // 함수 호출 패턴: Identifier '(' ... ')'
        if (ctx.primaryExpression() == null) return;

        // '(' 토큰이 있는지 확인 (함수 호출)
        boolean hasFunctionCall = false;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i).getText().equals("(") && i > 0) {
                hasFunctionCall = true;
                break;
            }
        }
        if (!hasFunctionCall) return;
        emitCallEvidence(ctx);

        // FUNCTION 노드 내부에서만 함수 호출 추출
        if (!isInsideFunction()) return;

        // 함수 호출 이름 추출: '->' 또는 '.' 멤버 접근 시 실제 호출 대상 추출
        // 예: svc->handler(req, reply) → name="handler", printf("hello") → name="printf"
        String name = null;
        for (int j = 0; j < ctx.getChildCount(); j++) {
            if (ctx.getChild(j).getText().equals("(") && j > 0) {
                // 멤버 접근 호출: svc->handler(), obj.func()
                if (j >= 2) {
                    String op = ctx.getChild(j - 2).getText();
                    if (".".equals(op) || "->".equals(op)) {
                        name = ctx.getChild(j - 1).getText();
                        break;
                    }
                }
                // 일반 호출: printf(), init_fn()
                if (ctx.primaryExpression().Identifier() != null) {
                    name = ctx.primaryExpression().Identifier().getText();
                }
                break;
            }
        }

        Node node = h.enterStatement("FUNCTION_CALL", name, ctx.getStart().getLine());
        node.endLine = ctx.getStop().getLine();
    }

    private void emitCallEvidence(CParser.PostfixExpressionContext context) {
        List<ParseTree> children = context.children == null ? List.of() : context.children;
        for (int index = 1; index < children.size(); index++) {
            ParseTree child = children.get(index);
            if (!(child instanceof TerminalNode open) || !"(".equals(open.getText())) continue;
            ParseTree previous = children.get(index - 1);
            Token calleeStop = previous instanceof TerminalNode terminal
                    ? terminal.getSymbol() : ((ParserRuleContext) previous).getStop();
            List<CParser.AssignmentExpressionContext> arguments = List.of();
            int closeIndex = index + 1;
            if (closeIndex < children.size()
                    && children.get(closeIndex) instanceof CParser.ArgumentExpressionListContext list) {
                arguments = list.assignmentExpression();
                closeIndex++;
            }
            if (closeIndex >= children.size()
                    || !(children.get(closeIndex) instanceof TerminalNode close)
                    || !")".equals(close.getText())) {
                throw new IllegalStateException("postfix call has no closing parenthesis");
            }
            String terminalName = callTerminalName(context, children, index);
            String calleeKind = index == 1 && context.primaryExpression() != null
                    && context.primaryExpression().Identifier() != null
                            ? "named" : "expression";
            CallEvidenceCandidate candidate = CallEvidenceCandidate.fromTokens(
                    "postfixExpression",
                    context.getStart(), close.getSymbol(), context.getStart(), calleeStop,
                    calleeKind, terminalName, arguments);
            callEvidence.add(candidate.withStructuralContext(
                    callReceiverRange(context, children, index),
                    lexicalScopePath(context)));
        }
    }

    private static String callTerminalName(CParser.PostfixExpressionContext context,
                                           List<ParseTree> children, int openIndex) {
        if (openIndex == 1 && context.primaryExpression() != null
                && context.primaryExpression().Identifier() != null) {
            return context.primaryExpression().Identifier().getText();
        }
        if (openIndex >= 3
                && children.get(openIndex - 2) instanceof TerminalNode operator
                && (".".equals(operator.getText()) || "->".equals(operator.getText()))
                && children.get(openIndex - 1) instanceof TerminalNode member) {
            return member.getText();
        }
        return null;
    }

    private static SourceRangeCandidate callReceiverRange(
            CParser.PostfixExpressionContext context,
            List<ParseTree> children,
            int openIndex) {
        if (openIndex < 3
                || !(children.get(openIndex - 2) instanceof TerminalNode operator)
                || !(".".equals(operator.getText()) || "->".equals(operator.getText()))) {
            return null;
        }
        ParseTree receiverEnd = children.get(openIndex - 3);
        Token stop = receiverEnd instanceof TerminalNode terminal
                ? terminal.getSymbol() : ((ParserRuleContext) receiverEnd).getStop();
        return range(context.getStart(), stop);
    }

    private void addCallable(String role, String grammarRule, ParserRuleContext factContext,
                             Token nameToken,
                             CParser.DeclarationSpecifiersContext specifiers,
                             CParser.DeclaratorContext declarator,
                             boolean definition,
                             ParserRuleContext lexicalScope) {
        try {
            requireExactRange(declarator.getStart(), declarator.getStop());
            SourceRangeCandidate factRange = range(factContext.getStart(), factContext.getStop());
            List<ScopeEvidenceCandidate> scopePath = lexicalScopePath(lexicalScope);
            SourceRangeCandidate scopeRange = scopePath.get(scopePath.size() - 1).range();
            List<SyntaxComponentCandidate> declarationSpecifiers = specifiers == null
                    ? List.of() : specifiers.declarationSpecifier().stream()
                            .map(this::syntaxComponent)
                            .toList();
            List<SyntaxComponentCandidate> attributes = directAttributeComponents(factContext);
            callableEvidence.add(new CallableCandidate(
                    grammarRule,
                    factRange,
                    range(nameToken, nameToken),
                    role,
                    definition ? factRange : null,
                    scopePath,
                    scopeRange,
                    declarator.getStop().getStopIndex() + 1,
                    new CallableSyntaxCandidate(
                            "c-callable-syntax/v1",
                            declarationSpecifiers,
                            syntaxComponent(declarator),
                            attributes)));
        } catch (IncompleteSyntaxEvidence ignored) {
            unresolvedCallableEvidence++;
        }
    }

    private List<ScopeEvidenceCandidate> lexicalScopePath(ParserRuleContext context) {
        List<ScopeEvidenceCandidate> nested = new ArrayList<>();
        ParserRuleContext cursor = context;
        while (cursor != null) {
            if (cursor instanceof CParser.FunctionDefinitionContext) {
                nested.add(0, new ScopeEvidenceCandidate(
                        "function", range(cursor.getStart(), cursor.getStop())));
            } else if (cursor instanceof CParser.CompoundStatementContext) {
                nested.add(0, new ScopeEvidenceCandidate(
                        "block", range(cursor.getStart(), cursor.getStop())));
            }
            cursor = cursor.getParent() instanceof ParserRuleContext
                    ? (ParserRuleContext) cursor.getParent() : null;
        }
        List<ScopeEvidenceCandidate> result = new ArrayList<>(nested.size() + 1);
        result.add(new ScopeEvidenceCandidate(
                "translation_unit", new SourceRangeCandidate(0, sourceLength)));
        result.addAll(nested);
        return List.copyOf(result);
    }

    private SyntaxComponentCandidate syntaxComponent(ParserRuleContext context) {
        requireExactRange(context.getStart(), context.getStop());
        List<SyntaxTokenCandidate> directTokens = new ArrayList<>();
        List<SyntaxComponentCandidate> children = new ArrayList<>();
        if (context.children != null) {
            for (ParseTree child : context.children) {
                if (child instanceof ParserRuleContext childContext) {
                    children.add(syntaxComponent(childContext));
                } else if (child instanceof TerminalNode terminal) {
                    Token token = terminal.getSymbol();
                    requireExactRange(token, token);
                    String tokenKind = CParser.VOCABULARY.getSymbolicName(token.getType());
                    if (tokenKind == null || tokenKind.isBlank()) {
                        throw new IllegalStateException(
                                "C lexer terminal has no symbolic name: " + token.getType());
                    }
                    directTokens.add(new SyntaxTokenCandidate(
                            tokenKind, range(token, token)));
                }
            }
        }
        return new SyntaxComponentCandidate(
                CParser.ruleNames[context.getRuleIndex()],
                range(context.getStart(), context.getStop()),
                directTokens,
                children);
    }

    private void emitStructuralExpression(
            String role, ParserRuleContext expression, ParserRuleContext owner) {
        if (expression == null) return;
        try {
            structuralExpressionEvidence.add(new ExpressionCandidate(
                    role,
                    range(expression.getStart(), expression.getStop()),
                    range(owner.getStart(), owner.getStop()),
                    lexicalScopePath(owner),
                    syntaxComponent(expression)));
        } catch (IncompleteSyntaxEvidence ignored) {
            unresolvedStructuralExpressionEvidence++;
        }
    }

    private List<SyntaxComponentCandidate> directAttributeComponents(
            ParserRuleContext context) {
        if (context.children == null) return List.of();
        List<SyntaxComponentCandidate> result = new ArrayList<>();
        for (ParseTree child : context.children) {
            if (child instanceof CParser.AttributeSpecifierSequenceContext attribute) {
                result.add(syntaxComponent(attribute));
            }
        }
        return List.copyOf(result);
    }

    private static CParser.CompoundStatementContext enclosingCompoundStatement(
            ParserRuleContext context) {
        ParserRuleContext cursor = context;
        while (cursor != null) {
            if (cursor instanceof CParser.CompoundStatementContext) {
                return (CParser.CompoundStatementContext) cursor;
            }
            cursor = cursor.getParent() instanceof ParserRuleContext
                    ? (ParserRuleContext) cursor.getParent() : null;
        }
        return null;
    }

    private static boolean isFunctionDeclarator(CParser.DeclaratorContext declarator) {
        return "function".equals(firstDerivedOperator(declarator));
    }

    private static String firstDerivedOperator(CParser.DeclaratorContext declarator) {
        if (declarator == null || declarator.directDeclarator() == null) return null;
        CParser.DirectDeclaratorContext direct = declarator.directDeclarator();
        if (direct.declarator() != null) {
            String nested = firstDerivedOperator(direct.declarator());
            if (nested != null) return nested;
        }
        String suffix = firstDirectSuffix(direct);
        if (suffix != null) return suffix;
        return declarator.pointer().isEmpty() ? null : "pointer";
    }

    private static String firstDirectSuffix(CParser.DirectDeclaratorContext direct) {
        int functionStart = direct.parameterTypeList().stream()
                .mapToInt(context -> context.getStart().getTokenIndex())
                .min().orElse(Integer.MAX_VALUE);
        int arrayStart = direct.LeftBracket().stream()
                .mapToInt(node -> node.getSymbol().getTokenIndex())
                .min().orElse(Integer.MAX_VALUE);
        if (functionStart == Integer.MAX_VALUE && arrayStart == Integer.MAX_VALUE) return null;
        return functionStart < arrayStart ? "function" : "array";
    }

    private static Token extractDeclaratorNameToken(CParser.DeclaratorContext ctx) {
        if (ctx == null || ctx.directDeclarator() == null) return null;
        CParser.DirectDeclaratorContext direct = ctx.directDeclarator();
        if (direct.declarator() != null) {
            return extractDeclaratorNameToken(direct.declarator());
        }
        return direct.Identifier() == null ? null : direct.Identifier().getSymbol();
    }

    private static SourceRangeCandidate range(Token start, Token stop) {
        requireExactRange(start, stop);
        return new SourceRangeCandidate(start.getStartIndex(), stop.getStopIndex() + 1);
    }

    private static void requireExactRange(Token start, Token stop) {
        if (start == null || stop == null || start.getStartIndex() < 0
                || stop.getStopIndex() < start.getStartIndex()) {
            throw new IncompleteSyntaxEvidence();
        }
    }

    private static final class IncompleteSyntaxEvidence extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    @Override
    public void exitPostfixExpression(CParser.PostfixExpressionContext ctx) {
        h.exitStatementWithFullComment("FUNCTION_CALL", ctx.getStop().getLine(), ctx);
    }

    /**
     * A standalone postfix increment/decrement is a state-changing statement, not a
     * mere expression.  Preserve the grammar-proven update as the same canonical
     * ASSIGNMENT shape used by compound assignments.  Updates in a for header,
     * condition, argument, or larger expression are deliberately excluded because
     * their value/ordering belongs to that enclosing expression.
     */
    private void emitStatementLevelPostfixUpdate(CParser.PostfixExpressionContext ctx) {
        if (!isInsideFunction() || !spansWholeExpressionStatement(ctx)) return;
        String operator = ctx.getChild(ctx.getChildCount() - 1).getText();
        if (!"++".equals(operator) && !"--".equals(operator)) return;
        String source = ParserUtils.getExactSourceText(ctx);
        Node node = h.addLeafStatement(
                "ASSIGNMENT", null, ctx.getStart().getLine(), ctx.getStop().getLine());
        node.target = source.substring(0, source.length() - operator.length()).trim();
        node.operator = "++".equals(operator) ? "+=" : "-=";
        node.expression = "1";
        node.statementOrigin = "postfix_update";
        emitStructuralExpression("update_expression", ctx, ctx);
    }

    @Override
    public void enterUnaryExpression(CParser.UnaryExpressionContext ctx) {
        if (!isInsideFunction() || !spansWholeExpressionStatement(ctx)) return;
        String operator = ctx.getChild(0).getText();
        if (!"++".equals(operator) && !"--".equals(operator)) return;
        Node node = h.addLeafStatement(
                "ASSIGNMENT", null, ctx.getStart().getLine(), ctx.getStop().getLine());
        node.target = ParserUtils.getExactSourceText(ctx.unaryExpression());
        node.operator = "++".equals(operator) ? "+=" : "-=";
        node.expression = "1";
        node.statementOrigin = "prefix_update";
        emitStructuralExpression("update_expression", ctx, ctx);
    }

    private static boolean spansWholeExpressionStatement(ParserRuleContext ctx) {
        ParserRuleContext cursor = ctx;
        while (cursor.getParent() instanceof ParserRuleContext) {
            ParserRuleContext parent = (ParserRuleContext) cursor.getParent();
            if (parent instanceof CParser.ExpressionStatementContext) {
                CParser.ExpressionContext expression =
                        ((CParser.ExpressionStatementContext) parent).expression();
                return expression != null
                        && expression.getStart().getTokenIndex() == ctx.getStart().getTokenIndex()
                        && expression.getStop().getTokenIndex() == ctx.getStop().getTokenIndex();
            }
            cursor = parent;
        }
        return false;
    }

    // ========================================
    // 제어 흐름 의미 AST (spec 007): IF / ELSE / LOOP / SWITCH / CASE
    //
    // 목적: rules/examples 폴백 분할용 "뼈대". Analyzer 는 이 노드들을 그래프에
    // 보존하되 analysis_targets 에서 제외한다(분석 불참). C 에는 try/catch 가 없다.
    // 기존 FUNCTION_CALL emit 은 변경하지 않으며, 그 노드들이 이 제어문 아래로
    // 자연 중첩된다(부모만 바뀌고 소실 없음).
    // ========================================

    @Override
    public void enterSelectionStatement(CParser.SelectionStatementContext ctx) {
        if (!isInsideFunction()) return;
        // selectionStatement: If '(' expr ')' stmt (Else stmt)?  |  Switch '(' expr ')' stmt
        Node node = h.enterStatement(ctx.If() != null ? "IF" : "SWITCH", ctx.getStart().getLine());
        // 조건식 원문 보존 (spec 016 FR-003) — downstream 이 괄호 짝을 재파싱하지 않는다.
        node.expression = ParserUtils.getExactSourceText(ctx.expression());
        emitStructuralExpression("condition", ctx.expression(), ctx);
    }

    @Override
    public void exitSelectionStatement(CParser.SelectionStatementContext ctx) {
        if (!isInsideFunction()) return;
        h.exitStatementWithFullComment(ctx.If() != null ? "IF" : "SWITCH",
                ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterIterationStatement(CParser.IterationStatementContext ctx) {
        if (!isInsideFunction()) return;
        // for / while / do-while → 종류 구분 없이 LOOP (PL/SQL 선례와 동일)
        Node node = h.enterStatement("LOOP", ctx.getStart().getLine());
        // 판정절만 expression 으로 보존 — for 의 초기화/증감은 도달 기계장치라 조건이
        // 아니다(TA-102). grammar 가 ';' 로 구분한 test 절을 그대로 소유시킨다.
        if (ctx.expression() != null) {
            node.expression = ParserUtils.getExactSourceText(ctx.expression());
            emitStructuralExpression("condition", ctx.expression(), ctx);
        } else if (ctx.forCondition() != null) {
            CParser.ForExpressionContext test = forTestClause(ctx.forCondition());
            node.expression = ParserUtils.getExactSourceText(test);
            emitStructuralExpression("condition", test, ctx);
        }
        if (ctx.Do() != null) {
            node.conditionTiming = "post";
        }
    }

    /** forCondition 의 첫 ';' 와 둘째 ';' 사이 test 절 — 없으면 null. */
    private static CParser.ForExpressionContext forTestClause(CParser.ForConditionContext ctx) {
        int semicolons = 0;
        for (int index = 0; index < ctx.getChildCount(); index++) {
            var child = ctx.getChild(index);
            if (child instanceof org.antlr.v4.runtime.tree.TerminalNode
                    && ";".equals(child.getText())) {
                semicolons++;
                continue;
            }
            if (semicolons == 1 && child instanceof CParser.ForExpressionContext) {
                return (CParser.ForExpressionContext) child;
            }
        }
        return null;
    }

    @Override
    public void exitIterationStatement(CParser.IterationStatementContext ctx) {
        if (!isInsideFunction()) return;
        h.exitStatementWithFullComment("LOOP", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterLabeledStatement(CParser.LabeledStatementContext ctx) {
        if (!isInsideFunction()) return;
        // A named label is not a conditional branch, but it is a deterministic
        // control-flow coordinate and must survive for downstream GOTO ownership.
        if (ctx.Case() == null && ctx.Default() == null) {
            if (ctx.Identifier() != null && ctx.Colon() != null) {
                h.addLeafStatement(
                        "LABEL", ctx.Identifier().getText(),
                        ctx.getStart().getLine(), ctx.getStart().getLine());
            }
            return;
        }
        Node node = h.enterStatement("CASE", ctx.getStart().getLine());
        // 라벨 상수 원문 보존 (spec 016 FR-003). default 는 expression null.
        node.expression = ParserUtils.getExactSourceText(ctx.constantExpression());
        emitStructuralExpression("case_value", ctx.constantExpression(), ctx);
    }

    @Override
    public void exitLabeledStatement(CParser.LabeledStatementContext ctx) {
        if (!isInsideFunction()) return;
        if (ctx.Case() == null && ctx.Default() == null) return;
        h.exitStatementWithFullComment("CASE", ctx.getStop().getLine(), ctx);
    }

    /**
     * C grammar의 labeledStatement는 라벨 직후 statement 하나만 품는다. 따라서 같은
     * case의 뒤 문장들은 parse tree상 SWITCH의 형제이며, 중첩 라벨은 CASE 안 CASE가 된다.
     * AST 소비자가 이를 재해석하지 않도록 생산 단계에서 source-level 소유권으로 확정한다.
     */
    private static void normalizeSwitchCases(Node node) {
        if ("SWITCH".equals(node.type)) {
            ArrayList<Node> flattened = new ArrayList<>();
            for (Node child : new ArrayList<>(node.children)) {
                if ("CASE".equals(child.type)) {
                    flattenCaseLabels(child, flattened);
                } else {
                    flattened.add(child);
                }
            }

            ArrayList<Node> normalized = new ArrayList<>();
            Node activeCase = null;
            for (Node child : flattened) {
                if ("CASE".equals(child.type)) {
                    child.parent = node;
                    normalized.add(child);
                    activeCase = child;
                } else if (activeCase == null) {
                    child.parent = node;
                    normalized.add(child);
                } else {
                    child.parent = activeCase;
                    activeCase.children.add(child);
                }
            }
            node.children = normalized;

            ArrayList<Node> cases = new ArrayList<>();
            for (Node child : normalized) {
                if ("CASE".equals(child.type)) cases.add(child);
            }
            for (int i = 0; i < cases.size(); i++) {
                Node current = cases.get(i);
                int end = i + 1 < cases.size()
                        ? cases.get(i + 1).startLine - 1
                        : node.endLine;
                current.endLine = Math.max(current.startLine, end);
            }
        }

        for (Node child : new ArrayList<>(node.children)) {
            normalizeSwitchCases(child);
        }
    }

    /** 중첩된 stacked label을 SWITCH-level CASE 형제로 평탄화한다. */
    private static void flattenCaseLabels(Node caseNode, List<Node> out) {
        ArrayList<Node> ownChildren = new ArrayList<>();
        ArrayList<Node> nestedCases = new ArrayList<>();
        for (Node child : caseNode.children) {
            if ("CASE".equals(child.type)) nestedCases.add(child);
            else ownChildren.add(child);
        }
        caseNode.children = ownChildren;
        out.add(caseNode);
        for (Node nested : nestedCases) flattenCaseLabels(nested, out);
    }

    // if 의 else 분기만 ELSE 노드로 감싼다. else-if 는 문법상 else 안에 중첩된
    // selectionStatement 이므로 ELSE 아래 IF 로 자연 표현된다(인위적 평탄화 없음).
    @Override
    public void enterStatement(CParser.StatementContext ctx) {
        if (!isInsideFunction()) return;
        if (isElseBranch(ctx)) {
            h.enterStatement("ELSE", ctx.getStart().getLine());
        }
    }

    @Override
    public void exitStatement(CParser.StatementContext ctx) {
        if (!isInsideFunction()) return;
        if (isElseBranch(ctx)) {
            h.exitStatementWithFullComment("ELSE", ctx.getStop().getLine(), ctx);
        }
    }

    /** ctx 가 부모 selectionStatement 의 else 분기(statement(1))인지. */
    private static boolean isElseBranch(CParser.StatementContext ctx) {
        if (!(ctx.getParent() instanceof CParser.SelectionStatementContext)) return false;
        CParser.SelectionStatementContext sel =
                (CParser.SelectionStatementContext) ctx.getParent();
        return sel.If() != null && sel.Else() != null
                && sel.statement().size() >= 2 && sel.statement(1) == ctx;
    }

    // ========================================
    // 구조 statement — jumpStatement (spec 016)
    // ========================================

    /**
     * grammar 가 구분하는 return/break/continue/goto 를 leaf 노드로 emit 한다.
     * leaf(스택 비진입)라 반환식 안의 FUNCTION_CALL 등 기존 자식은 종전 부모에
     * 그대로 붙는다 — 기존 노드 수·부모 보존(FR-009). 반환식 원문은 downstream 이
     * 소스를 다시 파싱하지 않도록 expression 필드로 보존한다(FR-003).
     */
    @Override
    public void enterJumpStatement(CParser.JumpStatementContext ctx) {
        if (!isInsideFunction()) return;
        String keyword = ctx.getStart().getText();
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();
        switch (keyword) {
            case "return": {
                Node node = h.addLeafStatement("RETURN", null, startLine, endLine);
                node.expression = ParserUtils.getExactSourceText(ctx.expression());
                emitStructuralExpression("return_value", ctx.expression(), ctx);
                break;
            }
            case "break":
                h.addLeafStatement("BREAK", null, startLine, endLine);
                break;
            case "continue":
                h.addLeafStatement("CONTINUE", null, startLine, endLine);
                break;
            case "goto": {
                // 'goto' Identifier | 'goto' unaryExpression(GCC 확장) — child(1)이 대상.
                String label = ctx.getChildCount() >= 2 ? ctx.getChild(1).getText() : null;
                h.addLeafStatement("GOTO", label, startLine, endLine);
                break;
            }
            default:
                // grammar 상 도달 불가 — 새 jump 형태가 생기면 조용히 삼키지 않도록 명시.
                throw new IllegalStateException("unknown C jumpStatement keyword: " + keyword);
        }
    }

    /**
     * statement-level 대입만 ASSIGNMENT leaf 로 emit 한다 (spec 016).
     * for 머리(초기화/증감)·조건식·인자 안 대입은 문장 효과가 아니라 도달 기계장치라
     * 노드가 아니다 — expressionStatement 문맥일 때만 grammar 사실이다.
     * 중첩 대입(`a = b = c`)은 바깥 문장 하나가 대표하고 우변 원문이 나머지를 보존한다.
     */
    @Override
    public void enterAssignmentExpression(CParser.AssignmentExpressionContext ctx) {
        if (!isInsideFunction()) return;
        if (ctx.assignementOperator == null) return;   // 대입 대안이 아닌 경우
        if (!(ctx.getParent() instanceof CParser.ExpressionContext)) return;
        if (!(ctx.getParent().getParent() instanceof CParser.ExpressionStatementContext)) return;
        Node node = h.addLeafStatement(
                "ASSIGNMENT", null, ctx.getStart().getLine(), ctx.getStop().getLine());
        node.target = ParserUtils.getExactSourceText(ctx.unaryExpression());
        node.operator = ctx.assignementOperator.getText();
        node.expression = ParserUtils.getExactSourceText(ctx.assignmentExpression());
        node.statementOrigin = "assignment_expression";
        emitStructuralExpression("assignment_target", ctx.unaryExpression(), ctx);
        emitStructuralExpression("assignment_value", ctx.assignmentExpression(), ctx);
    }

    // ========================================
    // struct 멤버 필드
    // ========================================

    @Override
    public void enterMemberDeclaration(CParser.MemberDeclarationContext ctx) {
        // STRUCT/UNION 내부에서만
        if (!isInsideType("STRUCT") && !isInsideType("UNION")) return;
        if (ctx.specifierQualifierList() == null) return;
        if (ctx.memberDeclaratorList() == null) return;

        String variableType = ParserUtils.getOriginalText(ctx.specifierQualifierList(), h.getTokens());

        for (CParser.MemberDeclaratorContext md : ctx.memberDeclaratorList().memberDeclarator()) {
            String name = null;
            if (md.declarator() != null) {
                name = extractDeclaratorName(md.declarator());
            }

            Node node = new Node("MEMBER", name, ctx.getStart().getLine(), h.getNodeStack().peek());
            node.variableType = variableType;
            node.endLine = ctx.getStop().getLine();
            node.comment = ParserUtils.getComment(ctx, h.getTokens());
        }
    }

    // ========================================
    // enum 상수
    // ========================================

    @Override
    public void enterEnumerator(CParser.EnumeratorContext ctx) {
        if (!isInsideType("ENUM")) return;

        String name = ctx.enumerationConstant() != null ? ctx.enumerationConstant().getText() : null;
        Node node = new Node("ENUM_CONSTANT", name, ctx.getStart().getLine(), h.getNodeStack().peek());
        node.endLine = ctx.getStop().getLine();
        node.comment = ParserUtils.getComment(ctx, h.getTokens());
    }

    // ========================================
    // 유틸리티
    // ========================================

    /**
     * declarationSpecifiers에서 returnType 추출
     * storage class(static, extern 등)를 제외한 타입 지정자만 추출하며
     * 원본 공백을 유지 (struct tm 등)
     */
    private String extractReturnType(CParser.DeclarationSpecifiersContext specs) {
        if (specs == null) return null;
        StringBuilder sb = new StringBuilder();
        for (CParser.DeclarationSpecifierContext ds : specs.declarationSpecifier()) {
            if (ds.storageClassSpecifier() != null) continue; // static, extern 등 제외
            if (ds.typeSpecifier() != null) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(ParserUtils.getOriginalText(ds.typeSpecifier(), h.getTokens()));
            } else if (ds.typeQualifier() != null) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(ds.typeQualifier().getText());
            } else if (ds.functionSpecifier() != null) {
                // WINAPI 등 calling convention은 returnType에 포함
                if (sb.length() > 0) sb.append(" ");
                sb.append(ds.functionSpecifier().getText());
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * declarationSpecifiers에서 modifiers 추출
     * storage class(static, extern 등)만 추출
     */
    private String extractModifiers(CParser.DeclarationSpecifiersContext specs) {
        if (specs == null) return null;
        StringBuilder sb = new StringBuilder();
        for (CParser.DeclarationSpecifierContext ds : specs.declarationSpecifier()) {
            if (ds.storageClassSpecifier() != null) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(ds.storageClassSpecifier().getText());
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * declarator에서 이름(Identifier) 추출
     */
    private String extractDeclaratorName(CParser.DeclaratorContext ctx) {
        if (ctx == null) return null;
        CParser.DirectDeclaratorContext dd = ctx.directDeclarator();
        if (dd == null) return null;
        // 중첩 declarator (포인터 등)
        if (dd.declarator() != null) {
            return extractDeclaratorName(dd.declarator());
        }
        if (dd.Identifier() != null) {
            return dd.Identifier().getText();
        }
        return null;
    }

    /**
     * 현재 스코프가 전역(FILE 직접 하위)인지 확인
     */
    private boolean isGlobalScope() {
        if (h.getNodeStack().isEmpty()) return true;
        Node current = h.getNodeStack().peek();
        return current.type.equals("FILE");
    }

    /**
     * 현재 FUNCTION 내부에 있는지 확인
     */
    private boolean isInsideFunction() {
        for (int i = h.getNodeStack().size() - 1; i >= 0; i--) {
            if (h.getNodeStack().get(i).type.equals("FUNCTION")) return true;
        }
        return false;
    }

    /**
     * 특정 타입의 노드 내부에 있는지 확인
     */
    private boolean isInsideType(String type) {
        for (int i = h.getNodeStack().size() - 1; i >= 0; i--) {
            if (h.getNodeStack().get(i).type.equals(type)) return true;
        }
        return false;
    }

    /**
     * declarator 원문에서 배열 dimension 표현식 ({@code [...]} 부분) 을 추출.
     * 배열이 아니면 빈 문자열. 다차원 ({@code [A][B]}) 도 한 번에 추출.
     * 예: {@code gc_x [LEN +1]} → {@code "[LEN +1]"}.
     */
    private static String extractArraySize(CParser.DeclaratorContext declarator, CommonTokenStream tokens) {
        if (declarator == null) return "";
        String full = ParserUtils.getOriginalText(declarator, tokens);
        int idx = full.indexOf('[');
        return (idx < 0) ? "" : full.substring(idx).trim();
    }

    /**
     * 비어있지 않은 텍스트들을 공백으로 합침. 둘 다 있으면 {@code "a b"},
     * 하나만 있으면 그것만, 둘 다 비면 빈 문자열.
     */
    private static String joinNonEmpty(String a, String b) {
        if (a == null || a.isEmpty()) return b == null ? "" : b;
        if (b == null || b.isEmpty()) return a;
        return a + " " + b;
    }
}
