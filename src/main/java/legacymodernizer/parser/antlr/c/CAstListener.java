package legacymodernizer.parser.antlr.c;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import legacymodernizer.parser.parsing.AntlrParseHarness;
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

    /**
     * typedef struct { ... } Name; 패턴에서 Name을 임시 보관.
     * enterDeclaration이 먼저 실행되어 이름을 저장하면,
     * 이후 enterStructOrUnionSpecifier / enterEnumSpecifier 에서 사용한다.
     */
    private String pendingTypedefName = null;

    public Node getRoot() {
        return h.getRoot();
    }

    public void setFileInfo(String fileName, String filePath) {
        h.setFileInfo(fileName, filePath);
    }

    public CAstListener(CommonTokenStream tokens, ParseProgressTracker tracker) {
        this.h = new ListenerHelper(tokens, tracker);
        extractFileHeaderComment();
        extractIncludes();
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
    // #include 추출 (HIDDEN 채널에서)
    // ========================================

    private void extractIncludes() {
        h.getTokens().fill();
        for (Token token : h.getTokens().getTokens()) {
            if (token.getType() == CLexer.Directive) {
                String text = token.getText().trim();
                if (text.startsWith("#include") || text.startsWith("# include")) {
                    String includeName = text.replaceFirst("^#\\s*include\\s*", "").trim();
                    Node node = new Node("INCLUDE", includeName, token.getLine(), h.getRoot());
                    node.endLine = token.getLine();
                }
                // #define 대문자 상수를 DEFINE으로 추출
                // 값은 숫자/문자열/문자/수식/식별자 매크로 참조 모두 허용.
                // 이름 뒤에 공백을 요구하므로 함수형 매크로 `#define FOO(x) ...`는 자동 제외됨.
                if (text.startsWith("#define") || text.startsWith("# define")) {
                    Matcher m = Pattern
                        .compile("^#\\s*define\\s+([A-Z_][A-Z0-9_]*)\\s+(.+?)\\s*(?:/[/*].*)?$")
                        .matcher(text);
                    if (m.find()) {
                        Node node = new Node("DEFINE", m.group(1), token.getLine(), h.getRoot());
                        node.endLine = token.getLine();
                        // 값(RHS) 을 initValue 에 저장 — 다른 언어 const initializer 와 동일 모델.
                        // 트레일링 주석 (`/* */`, `//`) 은 정규식에서 제외.
                        node.initValue = m.group(2).trim();
                    }
                }
            }
        }
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
        // .h 파일에서는 함수 정의 노드를 생성하지 않음 (중복 방지)
        if (isHeaderFile()) return;

        String name = null;

        // 함수 이름 추출: declarator → directDeclarator → Identifier
        if (ctx.declarator() != null && ctx.declarator().directDeclarator() != null) {
            CParser.DirectDeclaratorContext dd = ctx.declarator().directDeclarator();
            if (dd.Identifier() != null) {
                name = dd.Identifier().getText();
            }
        }

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
            emitDeclarator(ctx, initDecl, shape.isTypedef(), isGlobal, variableType, modifierStr);
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
                                String variableType, String modifierStr) {
        String name = extractDeclaratorName(initDecl.declarator());
        if (name == null) return;

        // 함수 프로토타입 감지: declarator에 parameterTypeList가 있으면 함수 선언
        boolean isFunctionPrototype = false;
        String parameters = null;
        if (initDecl.declarator() != null && initDecl.declarator().directDeclarator() != null) {
            CParser.DirectDeclaratorContext dd = initDecl.declarator().directDeclarator();
            if (dd.parameterTypeList() != null && !dd.parameterTypeList().isEmpty()) {
                isFunctionPrototype = true;
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
        } else if (isFunctionPrototype && isGlobal) {
            return;
        } else if (isConst) {
            nodeType = "CONSTANT_FIELD";
        } else if (isGlobal) {
            nodeType = "GLOBAL_VARIABLE";
        } else {
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

    @Override
    public void exitPostfixExpression(CParser.PostfixExpressionContext ctx) {
        h.exitStatementWithFullComment("FUNCTION_CALL", ctx.getStop().getLine(), ctx);
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
        h.enterStatement(ctx.If() != null ? "IF" : "SWITCH", ctx.getStart().getLine());
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
        h.enterStatement("LOOP", ctx.getStart().getLine());
    }

    @Override
    public void exitIterationStatement(CParser.IterationStatementContext ctx) {
        if (!isInsideFunction()) return;
        h.exitStatementWithFullComment("LOOP", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterLabeledStatement(CParser.LabeledStatementContext ctx) {
        if (!isInsideFunction()) return;
        // case X: / default: → CASE. goto 레이블(Identifier ':')은 제어분기가 아니므로 제외.
        if (ctx.Case() == null && ctx.Default() == null) return;
        h.enterStatement("CASE", ctx.getStart().getLine());
    }

    @Override
    public void exitLabeledStatement(CParser.LabeledStatementContext ctx) {
        if (!isInsideFunction()) return;
        if (ctx.Case() == null && ctx.Default() == null) return;
        h.exitStatementWithFullComment("CASE", ctx.getStop().getLine(), ctx);
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
     * 현재 파일이 헤더(.h) 파일인지 확인
     */
    private boolean isHeaderFile() {
        return h.getRoot().fileName != null && h.getRoot().fileName.endsWith(".h");
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
