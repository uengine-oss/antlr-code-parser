package legacymodernizer.parser.antlr.python;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import legacymodernizer.parser.antlr.Node;
import legacymodernizer.parser.antlr.ParserUtils;
import legacymodernizer.parser.service.ParseProgressTracker;

/**
 * Python 파일 분석을 위한 커스텀 리스너
 * - import 문은 IMPORT 노드로 추출
 * - class 정의는 CLASS 노드 (상속 관계 포함)
 * - def 함수/메서드는 METHOD 또는 FUNCTION 노드
 * - 클래스 내부 def는 METHOD, 최상위 def는 FUNCTION
 * - 데코레이터는 annotations 속성에 포함
 * - 변수 할당은 VARIABLE 노드 (로컬/모듈), FIELD 노드 (인스턴스/클래스 변수)
 * - 타입 어노테이션만 있는 선언 (name: type)도 FIELD로 추출
 * - 함수/메서드 호출은 METHOD_CALL 노드로 추출
 * - 선행 주석(# 주석)과 docstring은 comment 속성에 포함
 * - 통일된 속성명 사용 (Node 클래스 참조)
 */
public class CustomPythonListener extends PythonParserBaseListener {

    private CommonTokenStream tokens;
    private Stack<Node> nodeStack = new Stack<>();
    private Node root = new Node("FILE", 0, null);
    private ParseProgressTracker progressTracker;
    private List<String> pendingDecorators = new ArrayList<>();

    public Node getRoot() {
        return root;
    }

    public void setFileInfo(String fileName, String filePath) {
        root.fileName = fileName;
        root.filePath = filePath;
    }

    public CustomPythonListener(CommonTokenStream tokens) {
        this.tokens = tokens;
        nodeStack.push(root);
    }

    public CustomPythonListener(CommonTokenStream tokens, ParseProgressTracker tracker) {
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
            if (ctx != null && node.comment == null) {
                node.comment = getLeadingComment(ctx);
            }
        }
    }

    /**
     * 스택에서 해당 타입의 노드를 찾아 가장 가까운 부모 타입을 반환.
     * 현재 노드가 CLASS/METHOD/FUNCTION 중 어디에 속하는지 판별용.
     */
    private String findEnclosingType() {
        for (int i = nodeStack.size() - 1; i >= 0; i--) {
            String t = nodeStack.get(i).type;
            if ("CLASS".equals(t) || "METHOD".equals(t) || "FUNCTION".equals(t) || "FILE".equals(t)) {
                return t;
            }
        }
        return "FILE";
    }

    // ========================================
    // 주석 추출
    // ========================================

    private String getLeadingComment(ParserRuleContext ctx) {
        Token startToken = ctx.getStart();
        if (startToken == null) return null;

        int tokenIndex = startToken.getTokenIndex();
        List<Token> comments = tokens.getHiddenTokensToLeft(tokenIndex, Token.HIDDEN_CHANNEL);
        if (comments == null || comments.isEmpty()) return null;

        StringBuilder sb = new StringBuilder();
        for (Token t : comments) {
            String text = t.getText().trim();
            if (text.startsWith("#")) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(text);
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * suite 내부의 첫 번째 문장이 문자열(docstring)이면 반환
     */
    private String extractDocstring(PythonParser.SuiteContext suite) {
        if (suite == null) return null;
        // suite : simple_stmt | NEWLINE INDENT stmt+ DEDENT
        List<PythonParser.StmtContext> stmts = suite.stmt();
        if (stmts != null && !stmts.isEmpty()) {
            PythonParser.StmtContext firstStmt = stmts.get(0);
            return extractDocstringFromStmt(firstStmt);
        }
        // simple_stmt 직접
        if (suite.simple_stmt() != null) {
            return extractDocstringFromSimpleStmt(suite.simple_stmt());
        }
        return null;
    }

    private String extractDocstringFromStmt(PythonParser.StmtContext stmt) {
        if (stmt == null || stmt.simple_stmt() == null) return null;
        return extractDocstringFromSimpleStmt(stmt.simple_stmt());
    }

    private String extractDocstringFromSimpleStmt(PythonParser.Simple_stmtContext simpleStmt) {
        if (simpleStmt == null || simpleStmt.small_stmt() == null || simpleStmt.small_stmt().isEmpty()) return null;
        PythonParser.Small_stmtContext smallStmt = simpleStmt.small_stmt(0);
        if (smallStmt instanceof PythonParser.Expr_stmtContext) {
            PythonParser.Expr_stmtContext exprStmt = (PythonParser.Expr_stmtContext) smallStmt;
            if (exprStmt.testlist_star_expr() != null && exprStmt.assign_part() == null) {
                String text = exprStmt.testlist_star_expr().getText();
                if (text != null && (text.startsWith("\"\"\"") || text.startsWith("'''"))) {
                    // 원본 텍스트 사용 (공백/개행 보존)
                    return ParserUtils.getOriginalText(exprStmt.testlist_star_expr(), tokens);
                }
            }
        }
        return null;
    }

    // ========================================
    // 데코레이터
    // ========================================

    @Override
    public void enterDecorator(PythonParser.DecoratorContext ctx) {
        if (ctx.dotted_name() != null) {
            String decoratorText = "@" + ctx.dotted_name().getText();
            if (ctx.arglist() != null) {
                decoratorText += "(" + ParserUtils.getOriginalText(ctx.arglist(), tokens) + ")";
            }
            pendingDecorators.add(decoratorText);
        }
    }

    // ========================================
    // import 문
    // ========================================

    @Override
    public void enterImport_stmt(PythonParser.Import_stmtContext ctx) {
        String name = null;
        if (ctx.dotted_as_names() != null) {
            name = ParserUtils.getOriginalText(ctx.dotted_as_names(), tokens);
        }
        enterStatement("IMPORT", name, ctx.getStart().getLine());
    }

    @Override
    public void exitImport_stmt(PythonParser.Import_stmtContext ctx) {
        exitStatement("IMPORT", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterFrom_stmt(PythonParser.From_stmtContext ctx) {
        String text = ParserUtils.getOriginalText(ctx, tokens);
        enterStatement("IMPORT", text, ctx.getStart().getLine());
    }

    @Override
    public void exitFrom_stmt(PythonParser.From_stmtContext ctx) {
        exitStatement("IMPORT", ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // 모듈명 전파
    // ========================================

    private void propagateModuleName(Node node, String moduleName) {
        for (Node child : node.children) {
            child.moduleName = moduleName;
            propagateModuleName(child, moduleName);
        }
    }

    // ========================================
    // 클래스
    // ========================================

    @Override
    public void enterClassdef(PythonParser.ClassdefContext ctx) {
        String name = ctx.name() != null ? ctx.name().getText() : null;
        Node node = enterStatement("CLASS", name, ctx.getStart().getLine());

        // 시그니처: class Name(bases):
        node.signature = ParserUtils.extractSignature(ctx, tokens, ":");

        // 데코레이터
        if (!pendingDecorators.isEmpty()) {
            node.annotations = String.join(" ", pendingDecorators);
            pendingDecorators.clear();
        }

        // 상속 관계
        if (ctx.arglist() != null) {
            String bases = ParserUtils.getOriginalText(ctx.arglist(), tokens);
            node.extendsType = bases;
        }

        // docstring
        if (ctx.suite() != null) {
            String docstring = extractDocstring(ctx.suite());
            if (docstring != null) {
                node.comment = docstring;
            }
        }
    }

    @Override
    public void exitClassdef(PythonParser.ClassdefContext ctx) {
        if (!nodeStack.isEmpty() && nodeStack.peek().type.equals("CLASS")) {
            Node node = nodeStack.peek();
            exitStatement("CLASS", ctx.getStop().getLine(), ctx);
            propagateModuleName(node, node.name);
        } else {
            exitStatement("CLASS", ctx.getStop().getLine(), ctx);
        }
    }

    // ========================================
    // 함수/메서드
    // ========================================

    @Override
    public void enterFuncdef(PythonParser.FuncdefContext ctx) {
        String name = ctx.name() != null ? ctx.name().getText() : null;

        // 클래스 내부이면 METHOD, 아니면 FUNCTION
        String nodeType = "FUNCTION";
        if (!nodeStack.isEmpty()) {
            String parentType = nodeStack.peek().type;
            if ("CLASS".equals(parentType)) {
                nodeType = "METHOD";
            }
        }

        Node node = enterStatement(nodeType, name, ctx.getStart().getLine());

        // 시그니처
        node.signature = ParserUtils.extractSignature(ctx, tokens, ":");

        // async 키워드
        if (ctx.ASYNC() != null) {
            node.modifiers = "async";
        }

        // 데코레이터
        if (!pendingDecorators.isEmpty()) {
            node.annotations = String.join(" ", pendingDecorators);
            pendingDecorators.clear();
        }

        // 파라미터
        if (ctx.typedargslist() != null) {
            node.parameters = ParserUtils.getOriginalText(ctx.typedargslist(), tokens);
        }

        // 리턴 타입 (-> type)
        if (ctx.test() != null) {
            node.returnType = ctx.test().getText();
        }

        // docstring
        if (ctx.suite() != null) {
            String docstring = extractDocstring(ctx.suite());
            if (docstring != null) {
                node.comment = docstring;
            }
        }
    }

    @Override
    public void exitFuncdef(PythonParser.FuncdefContext ctx) {
        if (!nodeStack.isEmpty()) {
            String topType = nodeStack.peek().type;
            if ("METHOD".equals(topType) || "FUNCTION".equals(topType)) {
                Node node = nodeStack.pop();
                node.endLine = ctx.getStop().getLine();
            }
        }
    }

    // ========================================
    // 변수/필드 할당 (expr_stmt with assign_part)
    // ========================================

    /**
     * 할당문 (=) 또는 타입 어노테이션 (: type) 처리.
     *
     * assign_part 규칙:
     *   - ASSIGN (...)            → 일반 할당 (name = value)
     *   - COLON test (ASSIGN ...)?  → 타입 어노테이션 (name: type 또는 name: type = value)
     *   - augmented assignment    → +=, -= 등 (무시)
     */
    @Override
    public void enterExpr_stmt(PythonParser.Expr_stmtContext ctx) {
        PythonParser.Assign_partContext assignPart = ctx.assign_part();

        // assign_part가 없으면 순수 표현식 → 무시
        if (assignPart == null) return;

        boolean hasAssign = assignPart.ASSIGN() != null && !assignPart.ASSIGN().isEmpty();
        boolean hasColon = assignPart.COLON() != null;

        // augmented assignment (+=, -= 등)은 무시
        if (!hasAssign && !hasColon) return;

        String varName = null;
        if (ctx.testlist_star_expr() != null) {
            varName = ctx.testlist_star_expr().getText();
        }
        if (varName == null) return;

        // 타입 어노테이션 추출 (: type)
        String typeAnnotation = null;
        if (hasColon && assignPart.test() != null) {
            typeAnnotation = assignPart.test().getText();
        }

        // 초기화식(= 오른쪽) 텍스트 추출 → 플래그 판별용
        String initializerText = extractInitializerText(assignPart);

        // self.xxx = ... → FIELD (인스턴스 변수)
        if (varName.startsWith("self.")) {
            String fieldName = varName.substring(5);
            // self.xxx.yyy = ... 은 기존 객체 속성 변경이므로 필드 선언이 아님 → 무시
            if (fieldName.contains(".")) {
                return;
            }
            Node node = enterStatement("FIELD", fieldName, ctx.getStart().getLine());
            if (typeAnnotation != null) {
                node.variableType = typeAnnotation;
            }
            applyInitializerFlags(node, initializerText);
            return;
        }

        String enclosing = findEnclosingType();

        if ("CLASS".equals(enclosing)) {
            // 클래스 바디 직속 변수 → FIELD (클래스 변수 또는 타입 어노테이션 필드)
            Node node = enterStatement("FIELD", varName, ctx.getStart().getLine());
            if (typeAnnotation != null) {
                node.variableType = typeAnnotation;
            }
            applyInitializerFlags(node, initializerText);
        } else if ("METHOD".equals(enclosing) || "FUNCTION".equals(enclosing)) {
            // 함수/메서드 내부 → VARIABLE (로컬)
            Node node = enterStatement("VARIABLE", varName, ctx.getStart().getLine());
            if (typeAnnotation != null) {
                node.variableType = typeAnnotation;
            }
            applyInitializerFlags(node, initializerText);
        } else {
            // 모듈 레벨 → VARIABLE
            Node node = enterStatement("VARIABLE", varName, ctx.getStart().getLine());
            if (typeAnnotation != null) {
                node.variableType = typeAnnotation;
            }
            applyInitializerFlags(node, initializerText);
        }
    }

    @Override
    public void exitExpr_stmt(PythonParser.Expr_stmtContext ctx) {
        if (ctx.assign_part() == null) return;

        PythonParser.Assign_partContext assignPart = ctx.assign_part();
        boolean hasAssign = assignPart.ASSIGN() != null && !assignPart.ASSIGN().isEmpty();
        boolean hasColon = assignPart.COLON() != null;
        if (!hasAssign && !hasColon) return;

        // FIELD 또는 VARIABLE 종료
        if (!nodeStack.isEmpty()) {
            String topType = nodeStack.peek().type;
            if ("FIELD".equals(topType) || "VARIABLE".equals(topType)) {
                Node node = nodeStack.pop();
                node.endLine = ctx.getStop().getLine();
            }
        }
    }

    // ========================================
    // 초기화식 플래그 판별 (Java CustomJavaListener와 동일 패턴)
    // ========================================

    /**
     * assign_part에서 = 오른쪽 텍스트를 추출.
     * Python: name = StatsService(db) → "StatsService(db)"
     */
    private String extractInitializerText(PythonParser.Assign_partContext assignPart) {
        if (assignPart == null) return null;
        // ASSIGN ... testlist_star_expr 형태
        if (assignPart.ASSIGN() != null && !assignPart.ASSIGN().isEmpty()
                && assignPart.testlist_star_expr() != null && !assignPart.testlist_star_expr().isEmpty()) {
            // 첫 번째 = 오른쪽 값
            return assignPart.testlist_star_expr(0).getText();
        }
        // COLON test ASSIGN testlist 형태 (name: Type = value)
        if (assignPart.COLON() != null && assignPart.testlist() != null) {
            return assignPart.testlist().getText();
        }
        return null;
    }

    /**
     * 초기화식에 함수 호출 패턴이 있는지 판별.
     * Python에서는 ClassName(args) 패턴 = Java의 new ClassName(args)에 해당.
     * 예: StatsService(db), Database(), Config.from_file(path)
     */
    private static boolean initializerMatchesMethodCall(String text) {
        if (text == null || text.isEmpty()) return false;
        return text.matches("(?s).*\\w+\\s*\\(.*");
    }

    /**
     * 초기화식에 대문자로 시작하는 생성자 호출 패턴이 있는지 판별.
     * Python: ClassName(args) = Java의 new ClassName(args)
     * 예: StatsService(db), BookService(db)
     */
    private static boolean initializerMatchesNewInstance(String text) {
        if (text == null || text.isEmpty()) return false;
        return text.matches("(?s).*\\b[A-Z]\\w*\\s*\\(.*");
    }

    /**
     * 초기화식에서 생성자 클래스명 추출.
     * "StatsService(db)" → "StatsService", "Config.from_file(x)" → null
     */
    private static String extractNewInstanceType(String text) {
        if (text == null || text.isEmpty()) return null;
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("^([A-Z]\\w*)\\s*\\(").matcher(text);
        return m.find() ? m.group(1) : null;
    }

    /** VARIABLE/FIELD 노드에 초기화식 플래그 + variableType 세팅 */
    private static void applyInitializerFlags(Node node, String initializerText) {
        if (initializerText == null || initializerText.isEmpty()) return;
        node.initializerContainsMethodCall = initializerMatchesMethodCall(initializerText);
        node.initializerContainsNewInstance = initializerMatchesNewInstance(initializerText);
        // 생성자 호출이면 variableType을 클래스명으로 세팅 (타입 어노테이션이 없는 경우만)
        if (Boolean.TRUE.equals(node.initializerContainsNewInstance) && node.variableType == null) {
            String className = extractNewInstanceType(initializerText);
            if (className != null) {
                node.variableType = className;
            }
        }
    }

    // ========================================
    // 함수/메서드 호출 (METHOD_CALL)
    // ========================================

    /**
     * Python 문법에서 함수 호출은 trailer 규칙으로 표현:
     *   expr : AWAIT? atom trailer* ;
     *   trailer : DOT name arguments? | arguments ;
     *   arguments : OPEN_PAREN arglist? CLOSE_PAREN | OPEN_BRACKET ... ;
     *
     * trailer에 arguments(괄호)가 있으면 함수 호출.
     * - DOT name arguments → obj.method() 형태 (name이 호출명)
     * - arguments만 (DOT 없음) → 직접 호출 — atom에서 이름 추출
     */
    @Override
    public void enterTrailer(PythonParser.TrailerContext ctx) {
        // arguments가 없거나, 대괄호 인덱싱이면 호출이 아님
        if (ctx.arguments() == null) return;
        if (ctx.arguments().OPEN_PAREN() == null) return;

        String callName = null;

        if (ctx.name() != null) {
            // obj.method() 패턴: trailer = DOT name arguments
            callName = ctx.name().getText();
        } else {
            // func() 패턴: trailer = arguments (DOT 없음)
            // 부모 expr에서 atom의 name을 추출
            callName = extractAtomNameFromParent(ctx);
        }

        if (callName != null) {
            enterStatement("METHOD_CALL", callName, ctx.getStart().getLine());
        }
    }

    @Override
    public void exitTrailer(PythonParser.TrailerContext ctx) {
        if (ctx.arguments() == null) return;
        if (ctx.arguments().OPEN_PAREN() == null) return;

        exitStatement("METHOD_CALL", ctx.getStop().getLine(), null);
    }

    /**
     * trailer의 부모(expr)에서 atom의 name을 추출.
     * expr: AWAIT? atom trailer*
     * atom: name | ... (다른 대안들)
     */
    private String extractAtomNameFromParent(PythonParser.TrailerContext trailerCtx) {
        if (trailerCtx.getParent() instanceof PythonParser.ExprContext) {
            PythonParser.ExprContext exprCtx = (PythonParser.ExprContext) trailerCtx.getParent();
            if (exprCtx.atom() != null && exprCtx.atom().name() != null) {
                return exprCtx.atom().name().getText();
            }
        }
        return null;
    }

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
        if (node.variableType != null) info.append(" type:").append(node.variableType);
        if (node.returnType != null) info.append(" returns:").append(node.returnType);
        if (node.parameters != null) info.append(" params:").append(node.parameters);
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
