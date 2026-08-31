package legacymodernizer.parser.antlr.python;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import legacymodernizer.parser.parsing.AntlrParseHarness;
import legacymodernizer.parser.parsing.evidence.CallEvidenceCandidate;
import legacymodernizer.parser.parsing.evidence.BindingTargetEvidenceExtraction;
import legacymodernizer.parser.parsing.evidence.BindingTargetEvidenceExtraction.BindingTargetCandidate;
import legacymodernizer.parser.parsing.evidence.CallableEvidenceExtraction;
import legacymodernizer.parser.parsing.evidence.CallableEvidenceExtraction.CallableCandidate;
import legacymodernizer.parser.parsing.evidence.CallableEvidenceExtraction.CallableSyntaxCandidate;
import legacymodernizer.parser.parsing.evidence.GrammarStructureEvidence;
import legacymodernizer.parser.parsing.evidence.GrammarStructureEvidence.IncompleteGrammarEvidence;
import legacymodernizer.parser.parsing.evidence.ImportBindingCandidate;
import legacymodernizer.parser.parsing.evidence.ImportEvidenceCandidate;
import legacymodernizer.parser.parsing.evidence.ImportEvidenceExtraction;
import legacymodernizer.parser.parsing.evidence.ParameterEvidenceExtraction;
import legacymodernizer.parser.parsing.evidence.ParameterEvidenceExtraction.ParameterCandidate;
import legacymodernizer.parser.parsing.evidence.SourceRangeCandidate;
import legacymodernizer.parser.parsing.evidence.ScopeDirectiveEvidenceExtraction;
import legacymodernizer.parser.parsing.evidence.ScopeDirectiveEvidenceExtraction.ScopeDirectiveCandidate;
import legacymodernizer.parser.parsing.evidence.SyntaxComponentCandidate;
import legacymodernizer.parser.model.Node;
import legacymodernizer.parser.antlr.ListenerHelper;
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
public class PythonAstListener extends PythonParserBaseListener
        implements AntlrParseHarness.AstListener {

    private final ListenerHelper h;
    private final GrammarStructureEvidence structuralEvidence;
    private List<String> pendingDecorators = new ArrayList<>();
    private final List<CallEvidenceCandidate> callEvidence = new ArrayList<>();
    private final List<CallableCandidate> callableEvidence = new ArrayList<>();
    private int unresolvedCallableEvidence;
    private final List<ImportEvidenceCandidate> importEvidence = new ArrayList<>();
    private final List<ParameterCandidate> parameterEvidence = new ArrayList<>();
    private int unresolvedParameterEvidence;
    private final List<BindingTargetCandidate> bindingTargetEvidence = new ArrayList<>();
    private int unresolvedBindingTargetEvidence;
    private final List<ScopeDirectiveCandidate> scopeDirectiveEvidence = new ArrayList<>();
    private int unresolvedScopeDirectiveEvidence;

    public PythonAstListener(CommonTokenStream tokens, ParseProgressTracker tracker) {
        this.h = new ListenerHelper(tokens, tracker);
        this.structuralEvidence = new GrammarStructureEvidence(
                tokens, PythonParser.VOCABULARY, PythonParser.ruleNames);
    }

    public Node getRoot() {
        return h.getRoot();
    }

    public void setFileInfo(String fileName, String filePath) {
        h.setFileInfo(fileName, filePath);
    }

    @Override
    public List<CallEvidenceCandidate> callEvidenceCandidates() {
        return List.copyOf(callEvidence);
    }

    @Override
    public ImportEvidenceExtraction importEvidenceExtraction() {
        return new ImportEvidenceExtraction(importEvidence, 0, List.of());
    }

    public CallableEvidenceExtraction callableEvidenceExtraction() {
        return new CallableEvidenceExtraction(
                "python", "antlr-python/v1", callableEvidence, unresolvedCallableEvidence,
                unresolvedCallableEvidence == 0
                        ? List.of() : List.of("insufficient_parser_recovery"),
                new ParameterEvidenceExtraction(
                        parameterEvidence,
                        unresolvedParameterEvidence,
                        unresolvedParameterEvidence == 0
                                ? List.of() : List.of("insufficient_parser_recovery")),
                new BindingTargetEvidenceExtraction(
                        bindingTargetEvidence,
                        unresolvedBindingTargetEvidence,
                        unresolvedBindingTargetEvidence == 0
                                ? List.of() : List.of("insufficient_parser_recovery")),
                new ScopeDirectiveEvidenceExtraction(
                        scopeDirectiveEvidence,
                        unresolvedScopeDirectiveEvidence,
                        unresolvedScopeDirectiveEvidence == 0
                                ? List.of() : List.of("insufficient_parser_recovery")));
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        h.checkProgress(ctx);
    }

    // ========================================
    // 노드 생성/종료
    // ========================================

    /**
     * Python용 exitStatement: 로컬 getLeadingComment(# 주석)을 사용하고
     * comment가 null인 경우에만 설정한다.
     */
    private void exitStatement(String type, int line, ParserRuleContext ctx) {
        Stack<Node> nodeStack = h.getNodeStack();
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
        Stack<Node> nodeStack = h.getNodeStack();
        for (int i = nodeStack.size() - 1; i >= 0; i--) {
            String t = nodeStack.get(i).type;
            if ("CLASS".equals(t) || "METHOD".equals(t) || "FUNCTION".equals(t) || "FILE".equals(t)) {
                return t;
            }
        }
        return "FILE";
    }

    /**
     * 현재 스코프가 frozen=True dataclass 내부인지 확인.
     */
    private boolean isInsideFrozenDataclass() {
        Stack<Node> nodeStack = h.getNodeStack();
        for (int i = nodeStack.size() - 1; i >= 0; i--) {
            Node n = nodeStack.get(i);
            if ("CLASS".equals(n.type) && n.annotations != null && n.annotations.contains("frozen=True")) {
                return true;
            }
        }
        return false;
    }

    private static final java.util.regex.Pattern ALL_CAPS = java.util.regex.Pattern.compile("^[A-Z][A-Z0-9_]*$");

    /**
     * Python 상수 판별: Final 타입 힌트, ALL_CAPS 변수명, frozen dataclass 필드.
     */
    private boolean isPythonConstant(String varName, String typeAnnotation) {
        if (typeAnnotation != null && typeAnnotation.contains("Final")) return true;
        if (varName != null && varName.length() > 1 && ALL_CAPS.matcher(varName).matches()) return true;
        if (isInsideFrozenDataclass()) return true;
        return false;
    }

    // ========================================
    // 주석 추출
    // ========================================

    private String getLeadingComment(ParserRuleContext ctx) {
        Token startToken = ctx.getStart();
        if (startToken == null) return null;

        int tokenIndex = startToken.getTokenIndex();
        List<Token> comments = h.getTokens().getHiddenTokensToLeft(tokenIndex, Token.HIDDEN_CHANNEL);
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
                    return ParserUtils.getOriginalText(exprStmt.testlist_star_expr(), h.getTokens());
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
                decoratorText += "(" + ParserUtils.getOriginalText(ctx.arglist(), h.getTokens()) + ")";
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
            name = ParserUtils.getOriginalText(ctx.dotted_as_names(), h.getTokens());
        }
        h.enterStatement("IMPORT", name, ctx.getStart().getLine());
        List<ImportBindingCandidate> entries = new ArrayList<>();
        for (PythonParser.Dotted_as_nameContext item
                : ctx.dotted_as_names().dotted_as_name()) {
            SourceRangeCandidate alias = item.name() == null ? null : range(item.name());
            entries.add(new ImportBindingCandidate(
                    "module", "qualified", range(item),
                    pythonNameRanges(item.dotted_name()), null, alias,
                    0, false, "unspecified"));
        }
        importEvidence.add(new ImportEvidenceCandidate(
                "import_stmt", range(ctx), "import", entries,
                structuralEvidence.scopePath(ctx, this::scopeKind)));
    }

    @Override
    public void exitImport_stmt(PythonParser.Import_stmtContext ctx) {
        exitStatement("IMPORT", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterFrom_stmt(PythonParser.From_stmtContext ctx) {
        String text = ParserUtils.getOriginalText(ctx, h.getTokens());
        h.enterStatement("IMPORT", text, ctx.getStart().getLine());
        int relativeLevel = ctx.DOT().size() + ctx.ELLIPSIS().size() * 3;
        List<SourceRangeCandidate> path = ctx.dotted_name() == null
                ? List.of() : pythonNameRanges(ctx.dotted_name());
        List<ImportBindingCandidate> entries = new ArrayList<>();
        if (ctx.STAR() != null) {
            entries.add(new ImportBindingCandidate(
                    "module_member", "qualified",
                    range(ctx.STAR().getSymbol(), ctx.STAR().getSymbol()),
                    path, null, null, relativeLevel, true, "unspecified"));
        } else {
            for (PythonParser.Import_as_nameContext item
                    : ctx.import_as_names().import_as_name()) {
                List<PythonParser.NameContext> names = item.name();
                SourceRangeCandidate member = range(names.get(0));
                SourceRangeCandidate alias = names.size() > 1 ? range(names.get(1)) : null;
                entries.add(new ImportBindingCandidate(
                        "module_member", "qualified", range(item), path,
                        member, alias, relativeLevel, false, "unspecified"));
            }
        }
        importEvidence.add(new ImportEvidenceCandidate(
                "from_stmt", range(ctx), "import", entries,
                structuralEvidence.scopePath(ctx, this::scopeKind)));
    }

    @Override
    public void exitFrom_stmt(PythonParser.From_stmtContext ctx) {
        exitStatement("IMPORT", ctx.getStop().getLine(), ctx);
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

    private static List<SourceRangeCandidate> pythonNameRanges(ParseTree path) {
        List<SourceRangeCandidate> result = new ArrayList<>();
        collectPythonNameRanges(path, result);
        return List.copyOf(result);
    }

    private static void collectPythonNameRanges(
            ParseTree tree, List<SourceRangeCandidate> result) {
        if (tree instanceof PythonParser.NameContext name) {
            result.add(range(name));
            return;
        }
        for (int index = 0; index < tree.getChildCount(); index++) {
            collectPythonNameRanges(tree.getChild(index), result);
        }
    }

    // ========================================
    // 클래스
    // ========================================

    @Override
    public void enterClassdef(PythonParser.ClassdefContext ctx) {
        addDefinitionBindingTarget(ctx, ctx.name(), "class_definition");
        String name = ctx.name() != null ? ctx.name().getText() : null;
        Node node = h.enterStatement("CLASS", name, ctx.getStart().getLine());

        // 시그니처: class Name(bases):
        node.signature = ParserUtils.extractSignature(ctx, h.getTokens(), ":");

        // 데코레이터
        if (!pendingDecorators.isEmpty()) {
            node.annotations = String.join(" ", pendingDecorators);
            pendingDecorators.clear();
        }

        // 상속 관계
        if (ctx.arglist() != null) {
            String bases = ParserUtils.getOriginalText(ctx.arglist(), h.getTokens());
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
        Stack<Node> nodeStack = h.getNodeStack();
        if (!nodeStack.isEmpty() && nodeStack.peek().type.equals("CLASS")) {
            Node node = nodeStack.peek();
            exitStatement("CLASS", ctx.getStop().getLine(), ctx);
            ListenerHelper.propagateModuleName(node, node.name);
        } else {
            exitStatement("CLASS", ctx.getStop().getLine(), ctx);
        }
    }

    // ========================================
    // 함수/메서드
    // ========================================

    @Override
    public void enterFuncdef(PythonParser.FuncdefContext ctx) {
        addDefinitionBindingTarget(ctx, ctx.name(), "function_definition");
        addCallable(ctx);
        String name = ctx.name() != null ? ctx.name().getText() : null;

        // 클래스 내부이면 METHOD, 아니면 FUNCTION
        Stack<Node> nodeStack = h.getNodeStack();
        String nodeType = "FUNCTION";
        if (!nodeStack.isEmpty()) {
            String parentType = nodeStack.peek().type;
            if ("CLASS".equals(parentType)) {
                nodeType = "METHOD";
            }
        }

        Node node = h.enterStatement(nodeType, name, ctx.getStart().getLine());

        // 시그니처
        node.signature = ParserUtils.extractSignature(ctx, h.getTokens(), ":");

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
            node.parameters = ParserUtils.getOriginalText(ctx.typedargslist(), h.getTokens());
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
        Stack<Node> nodeStack = h.getNodeStack();
        if (!nodeStack.isEmpty()) {
            String topType = nodeStack.peek().type;
            if ("METHOD".equals(topType) || "FUNCTION".equals(topType)) {
                Node node = nodeStack.pop();
                node.endLine = ctx.getStop().getLine();
            }
        }
    }

    private void addCallable(PythonParser.FuncdefContext context) {
        if (context.name() == null || context.COLON() == null) {
            unresolvedCallableEvidence++;
            return;
        }
        try {
            ParserRuleContext factContext = context;
            String grammarRule = "funcdef";
            List<SyntaxComponentCandidate> specifiers = context.test() == null
                    ? List.of() : List.of(structuralEvidence.component(context.test()));
            ParserRuleContext declarator = context.typedargslist() == null
                    ? context.name() : context.typedargslist();
            List<SyntaxComponentCandidate> attributes = List.of();
            if (context.getParent() instanceof PythonParser.Class_or_func_def_stmtContext parent) {
                attributes = parent.decorator().stream()
                        .map(structuralEvidence::component)
                        .toList();
                if (!parent.decorator().isEmpty()) {
                    factContext = parent;
                    grammarRule = "class_or_func_def_stmt";
                }
            }
            var scopePath = structuralEvidence.scopePath(
                    factContext.getParent(), this::scopeKind);
            var factRange = structuralEvidence.range(factContext);
            callableEvidence.add(new CallableCandidate(
                    grammarRule,
                    factRange,
                    structuralEvidence.range(context.name()),
                    "definition",
                    structuralEvidence.range(context),
                    scopePath,
                    scopePath.get(scopePath.size() - 1).range(),
                    context.COLON().getSymbol().getStopIndex() + 1,
                    new CallableSyntaxCandidate(
                            "python-callable-syntax/v1",
                            specifiers,
                            structuralEvidence.component(declarator),
                            attributes)));
        } catch (IncompleteGrammarEvidence ignored) {
            unresolvedCallableEvidence++;
        }
    }

    private String scopeKind(ParserRuleContext context) {
        if (context instanceof PythonParser.ClassdefContext) return "class";
        if (context instanceof PythonParser.FuncdefContext) return "function";
        if (context instanceof PythonParser.TestContext test && test.LAMBDA() != null) {
            return "lambda";
        }
        if (context instanceof PythonParser.Testlist_compContext value
                && value.comp_for() != null) return "comprehension";
        if (context instanceof PythonParser.DictorsetmakerContext value
                && value.comp_for() != null) return "comprehension";
        if (context instanceof PythonParser.ArgumentContext value
                && value.comp_for() != null) return "comprehension";
        if (context instanceof PythonParser.SuiteContext
                && !(context.getParent() instanceof PythonParser.ClassdefContext)) {
            return "block";
        }
        return null;
    }

    @Override
    public void enterNamed_parameter(PythonParser.Named_parameterContext ctx) {
        addParameter(ctx, ctx.name());
    }

    @Override
    public void enterVardef_parameter(PythonParser.Vardef_parameterContext ctx) {
        if (ctx.name() != null) addParameter(ctx, ctx.name());
    }

    @Override
    public void enterVarargs(PythonParser.VarargsContext ctx) {
        addParameter(ctx, ctx.name());
    }

    @Override
    public void enterVarkwargs(PythonParser.VarkwargsContext ctx) {
        addParameter(ctx, ctx.name());
    }

    private void addParameter(ParserRuleContext grammarContext, PythonParser.NameContext name) {
        if (name == null) {
            unresolvedParameterEvidence++;
            return;
        }
        try {
            List<legacymodernizer.parser.parsing.evidence.ScopeEvidenceCandidate> scopePath =
                    structuralEvidence.scopePath(grammarContext, this::scopeKind);
            parameterEvidence.add(new ParameterCandidate(
                    PythonParser.ruleNames[grammarContext.getRuleIndex()],
                    structuralEvidence.range(name), scopePath));
        } catch (IncompleteGrammarEvidence ignored) {
            unresolvedParameterEvidence++;
        }
    }

    private void addBindingTarget(ParserRuleContext target, String bindingContext) {
        if (target == null) {
            unresolvedBindingTargetEvidence++;
            return;
        }
        try {
            bindingTargetEvidence.add(new BindingTargetCandidate(
                    PythonParser.ruleNames[target.getRuleIndex()],
                    structuralEvidence.range(target),
                    bindingContext,
                    structuralEvidence.scopePath(target, this::scopeKind),
                    structuralEvidence.component(target)));
        } catch (IncompleteGrammarEvidence ignored) {
            unresolvedBindingTargetEvidence++;
        }
    }

    private void addDefinitionBindingTarget(
            ParserRuleContext definition,
            PythonParser.NameContext name,
            String bindingContext) {
        if (name == null) {
            unresolvedBindingTargetEvidence++;
            return;
        }
        try {
            bindingTargetEvidence.add(new BindingTargetCandidate(
                    PythonParser.ruleNames[name.getRuleIndex()],
                    structuralEvidence.range(name),
                    bindingContext,
                    structuralEvidence.scopePath(definition.getParent(), this::scopeKind),
                    structuralEvidence.component(name)));
        } catch (IncompleteGrammarEvidence ignored) {
            unresolvedBindingTargetEvidence++;
        }
    }

    @Override
    public void enterWith_item(PythonParser.With_itemContext ctx) {
        if (ctx.AS() != null) addBindingTarget(ctx.expr(), "with_target");
    }

    @Override
    public void enterComp_for(PythonParser.Comp_forContext ctx) {
        addBindingTarget(ctx.exprlist(), "comprehension_target");
    }

    @Override
    public void enterDel_stmt(PythonParser.Del_stmtContext ctx) {
        addBindingTarget(ctx.exprlist(), "delete_target");
    }

    private void addScopeDirectives(
            ParserRuleContext grammarContext,
            List<PythonParser.NameContext> names,
            String directiveKind) {
        if (names == null || names.isEmpty()) {
            unresolvedScopeDirectiveEvidence++;
            return;
        }
        for (PythonParser.NameContext name : names) {
            try {
                scopeDirectiveEvidence.add(new ScopeDirectiveCandidate(
                        PythonParser.ruleNames[grammarContext.getRuleIndex()],
                        structuralEvidence.range(name),
                        directiveKind,
                        structuralEvidence.scopePath(name, this::scopeKind)));
            } catch (IncompleteGrammarEvidence ignored) {
                unresolvedScopeDirectiveEvidence++;
            }
        }
    }

    @Override
    public void enterGlobal_stmt(PythonParser.Global_stmtContext ctx) {
        addScopeDirectives(ctx, ctx.name(), "global");
    }

    @Override
    public void enterNonlocal_stmt(PythonParser.Nonlocal_stmtContext ctx) {
        addScopeDirectives(ctx, ctx.name(), "nonlocal");
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

        String bindingContext = assignPart.op != null
                ? "augmented_assignment"
                : assignPart.COLON() != null ? "annotated_assignment" : "assignment";
        addBindingTarget(ctx.testlist_star_expr(), bindingContext);
        if (assignPart.op == null && assignPart.COLON() == null) {
            int chainedTargets = Math.max(0, assignPart.ASSIGN().size() - 1);
            List<PythonParser.Testlist_star_exprContext> chained =
                    assignPart.testlist_star_expr();
            for (int index = 0; index < Math.min(chainedTargets, chained.size()); index++) {
                addBindingTarget(chained.get(index), "assignment");
            }
        }

        // routine 안 대입 statement 는 ASSIGNMENT leaf 로도 emit (spec 016) —
        // 선언 의미(VARIABLE/FIELD)와 별개의 문장 효과 사실이다.
        emitRoutineAssignment(ctx, assignPart);

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

        // self.xxx = ... → FIELD 또는 CONSTANT_FIELD (인스턴스 변수)
        if (varName.startsWith("self.")) {
            String fieldName = varName.substring(5);
            if (fieldName.contains(".")) {
                return;
            }
            String fieldType = isPythonConstant(fieldName, typeAnnotation) ? "CONSTANT_FIELD" : "FIELD";
            openAssignedNode(fieldType, fieldName, ctx.getStart().getLine(),
                    typeAnnotation, initializerText);
            return;
        }

        String enclosing = findEnclosingType();

        if ("CLASS".equals(enclosing)) {
            String fieldType = isPythonConstant(varName, typeAnnotation) ? "CONSTANT_FIELD" : "FIELD";
            openAssignedNode(fieldType, varName, ctx.getStart().getLine(),
                    typeAnnotation, initializerText);
        } else if ("METHOD".equals(enclosing) || "FUNCTION".equals(enclosing)) {
            // 함수 내 지역변수는 노드로 만들지 않음 — 초기화식의 호출/생성은 enterTrailer 가 잡는다.
            return;
        } else {
            // 모듈 레벨: ALL_CAPS면 CONSTANT_FIELD, 아니면 VARIABLE
            String modType = isPythonConstant(varName, typeAnnotation) ? "CONSTANT_FIELD" : "VARIABLE";
            openAssignedNode(modType, varName, ctx.getStart().getLine(),
                    typeAnnotation, initializerText);
        }
    }

    /**
     * routine 안 statement-level 대입을 ASSIGNMENT leaf 로 emit 한다 (spec 016).
     * target/operator/expression 은 소스 원문 그대로 보존한다(FR-003). 연쇄 대입
     * (`a = b = 1`)은 문장 하나가 대표하고 우변 원문이 나머지를 보존한다.
     * 값 없는 annotation(`x: int`)은 대입이 아니므로 노드가 아니다.
     */
    private void emitRoutineAssignment(
            PythonParser.Expr_stmtContext ctx, PythonParser.Assign_partContext assignPart) {
        if (!isInsideRoutine() || ctx.testlist_star_expr() == null) return;

        String operator = null;
        ParserRuleContext rhsStart = null;
        if (assignPart.op != null) {
            // augmented (`+=` 등)
            operator = assignPart.op.getText();
            rhsStart = assignPart.yield_expr() != null
                    ? assignPart.yield_expr()
                    : assignPart.testlist();
        } else if (assignPart.COLON() != null) {
            // annassign — 값이 있을 때만 대입이다 (`x: int = 5`)
            if (assignPart.ASSIGN() == null || assignPart.ASSIGN().isEmpty()) return;
            operator = "=";
            rhsStart = assignPart.testlist();
        } else if (assignPart.ASSIGN() != null && !assignPart.ASSIGN().isEmpty()) {
            operator = "=";
            rhsStart = !assignPart.testlist_star_expr().isEmpty()
                    ? assignPart.testlist_star_expr(0)
                    : assignPart.yield_expr();
        }
        if (operator == null || rhsStart == null) return;

        Node node = h.addLeafStatement(
                "ASSIGNMENT", null, ctx.getStart().getLine(), ctx.getStop().getLine());
        node.target = ParserUtils.getExactSourceText(ctx.testlist_star_expr());
        node.operator = operator;
        node.expression = ParserUtils.getExactSourceText(rhsStart, assignPart);
    }

    /** 3분기(self 필드·클래스 필드·모듈 변수) 공통: 노드 생성 + 어노테이션·initValue·타입 추론. */
    private void openAssignedNode(String nodeType, String name, int line,
                                  String typeAnnotation, String initializerText) {
        Node node = h.enterStatement(nodeType, name, line);
        if (typeAnnotation != null) {
            node.variableType = typeAnnotation;
        }
        node.initValue = initializerText;
    }

    @Override
    public void exitExpr_stmt(PythonParser.Expr_stmtContext ctx) {
        if (ctx.assign_part() == null) return;

        PythonParser.Assign_partContext assignPart = ctx.assign_part();
        boolean hasAssign = assignPart.ASSIGN() != null && !assignPart.ASSIGN().isEmpty();
        boolean hasColon = assignPart.COLON() != null;
        if (!hasAssign && !hasColon) return;

        // FIELD, CONSTANT_FIELD, VARIABLE 종료
        Stack<Node> nodeStack = h.getNodeStack();
        if (!nodeStack.isEmpty()) {
            String topType = nodeStack.peek().type;
            if ("FIELD".equals(topType) || "CONSTANT_FIELD".equals(topType) || "VARIABLE".equals(topType)) {
                Node node = nodeStack.pop();
                node.endLine = ctx.getStop().getLine();
            }
        }
    }

    /**
     * assign_part에서 = 오른쪽 텍스트를 추출.
     */
    private String extractInitializerText(PythonParser.Assign_partContext assignPart) {
        if (assignPart == null) return null;
        if (assignPart.ASSIGN() != null && !assignPart.ASSIGN().isEmpty()
                && assignPart.testlist_star_expr() != null && !assignPart.testlist_star_expr().isEmpty()) {
            return assignPart.testlist_star_expr(0).getText();
        }
        if (assignPart.COLON() != null && assignPart.testlist() != null) {
            return assignPart.testlist().getText();
        }
        return null;
    }

    // ========================================
    // 함수/메서드/생성자 후보 호출 (FUNCTION_CALL)
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

        {
            PythonParser.ExprContext expression = (PythonParser.ExprContext) ctx.getParent();
            Token calleeStop = ctx.name() != null
                    ? ctx.name().getStop()
                    : tokenBeforeTrailer(expression, ctx);
            Node call = h.enterStatement("FUNCTION_CALL", callName, expression.getStart().getLine());
            CallEvidenceCandidate candidate = CallEvidenceCandidate.fromTokens("trailer",
                    expression.getStart(), ctx.getStop(), expression.getStart(), calleeStop,
                    callName == null ? "expression" : "named", callName,
                    ctx.arguments().arglist() == null
                            ? List.of() : ctx.arguments().arglist().argument());
            SourceRangeCandidate receiverRange = ctx.name() == null ? null
                    : structuralEvidence.range(
                            expression.getStart(), tokenBeforeTrailer(expression, ctx));
            callEvidence.add(candidate.withStructuralContext(
                    receiverRange, callScopePath(ctx)));
        }
    }

    private List<legacymodernizer.parser.parsing.evidence.ScopeEvidenceCandidate>
            callScopePath(ParserRuleContext callContext) {
        ParserRuleContext comprehension = nearestComprehension(callContext);
        PythonParser.Comp_forContext outerFor = outerComprehensionFor(comprehension);
        if (outerFor != null && isDescendantOf(callContext, outerFor.logical_test())) {
            return structuralEvidence.scopePath(
                    (ParserRuleContext) comprehension.getParent(), this::scopeKind);
        }
        return structuralEvidence.scopePath(callContext, this::scopeKind);
    }

    private static ParserRuleContext nearestComprehension(ParserRuleContext context) {
        ParserRuleContext cursor = context;
        while (cursor != null) {
            if (cursor instanceof PythonParser.Testlist_compContext value
                    && value.comp_for() != null) return cursor;
            if (cursor instanceof PythonParser.DictorsetmakerContext value
                    && value.comp_for() != null) return cursor;
            if (cursor instanceof PythonParser.ArgumentContext value
                    && value.comp_for() != null) return cursor;
            cursor = cursor.getParent();
        }
        return null;
    }

    private static PythonParser.Comp_forContext outerComprehensionFor(
            ParserRuleContext comprehension) {
        if (comprehension instanceof PythonParser.Testlist_compContext value) {
            return value.comp_for();
        }
        if (comprehension instanceof PythonParser.DictorsetmakerContext value) {
            return value.comp_for();
        }
        if (comprehension instanceof PythonParser.ArgumentContext value) {
            return value.comp_for();
        }
        return null;
    }

    private static boolean isDescendantOf(ParseTree child, ParseTree ancestor) {
        ParseTree cursor = child;
        while (cursor != null) {
            if (cursor == ancestor) return true;
            cursor = cursor.getParent();
        }
        return false;
    }

    @Override
    public void exitTrailer(PythonParser.TrailerContext ctx) {
        if (ctx.arguments() == null) return;
        if (ctx.arguments().OPEN_PAREN() == null) return;

        exitStatement("FUNCTION_CALL", ctx.getStop().getLine(), null);
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

    private static Token tokenBeforeTrailer(PythonParser.ExprContext expression,
                                            PythonParser.TrailerContext current) {
        List<PythonParser.TrailerContext> trailers = expression.trailer();
        int index = trailers.indexOf(current);
        if (index < 0) throw new IllegalStateException("trailer is detached from expression");
        return index == 0 ? expression.atom().getStop() : trailers.get(index - 1).getStop();
    }

    // ========================================
    // 제어 흐름 의미 AST (spec 007 계약의 Python 구현): IF / ELSE / LOOP / TRY / CATCH
    //
    // C/Java 리스너와 동일 원칙: 함수/메서드 안에서만 emit, for/while 은 LOOP,
    // elif 는 ELSE(분기 — 조건은 code_text 에), else 는 부모가 if 일 때만 ELSE
    // (Python 의 loop-else/try-else 는 계약 부모=IF 위반이라 제외 — 미지원 명시).
    // Python 에는 SWITCH/CASE 가 없다(계약 §1). match 문은 이번 범위 밖 — 미지원 명시.
    // ========================================

    private boolean isInsideRoutine() {
        Stack<Node> stack = h.getNodeStack();
        for (int i = stack.size() - 1; i >= 0; i--) {
            String t = stack.get(i).type;
            if ("FUNCTION".equals(t) || "METHOD".equals(t)) return true;
        }
        return false;
    }

    @Override
    public void enterIf_stmt(PythonParser.If_stmtContext ctx) {
        if (!isInsideRoutine()) return;
        // 조건식 원문 보존 (spec 016 FR-003) — 이하 elif/while/for 동일.
        h.enterStatement("IF", ctx.getStart().getLine())
                .expression = ParserUtils.getExactSourceText(ctx.test());
    }

    @Override
    public void exitIf_stmt(PythonParser.If_stmtContext ctx) {
        if (isInsideRoutine()) h.exitStatementWithFullComment("IF", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterElif_clause(PythonParser.Elif_clauseContext ctx) {
        if (!isInsideRoutine()) return;
        h.enterStatement("ELSE", ctx.getStart().getLine())
                .expression = ParserUtils.getExactSourceText(ctx.test());
    }

    @Override
    public void exitElif_clause(PythonParser.Elif_clauseContext ctx) {
        if (isInsideRoutine()) h.exitStatementWithFullComment("ELSE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterElse_clause(PythonParser.Else_clauseContext ctx) {
        if (isInsideRoutine() && ctx.getParent() instanceof PythonParser.If_stmtContext) {
            h.enterStatement("ELSE", ctx.getStart().getLine());
        }
    }

    @Override
    public void exitElse_clause(PythonParser.Else_clauseContext ctx) {
        if (isInsideRoutine() && ctx.getParent() instanceof PythonParser.If_stmtContext) {
            h.exitStatementWithFullComment("ELSE", ctx.getStop().getLine(), ctx);
        }
    }

    @Override
    public void enterWhile_stmt(PythonParser.While_stmtContext ctx) {
        if (!isInsideRoutine()) return;
        h.enterStatement("LOOP", ctx.getStart().getLine())
                .expression = ParserUtils.getExactSourceText(ctx.test());
    }

    @Override
    public void exitWhile_stmt(PythonParser.While_stmtContext ctx) {
        if (isInsideRoutine()) h.exitStatementWithFullComment("LOOP", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterFor_stmt(PythonParser.For_stmtContext ctx) {
        addBindingTarget(ctx.exprlist(), "for_target");
        if (!isInsideRoutine()) return;
        Node node = h.enterStatement("LOOP", ctx.getStart().getLine());
        // `for <exprlist> in <testlist>` — 반복 지배식 전체를 조건으로 보존.
        if (ctx.exprlist() != null && ctx.testlist() != null) {
            node.expression = ParserUtils.getExactSourceText(ctx.exprlist(), ctx.testlist());
        }
    }

    @Override
    public void exitFor_stmt(PythonParser.For_stmtContext ctx) {
        if (isInsideRoutine()) h.exitStatementWithFullComment("LOOP", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterTry_stmt(PythonParser.Try_stmtContext ctx) {
        if (isInsideRoutine()) h.enterStatement("TRY", ctx.getStart().getLine());
    }

    @Override
    public void exitTry_stmt(PythonParser.Try_stmtContext ctx) {
        if (isInsideRoutine()) h.exitStatementWithFullComment("TRY", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterExcept_clause(PythonParser.Except_clauseContext ctx) {
        if (ctx.name() != null) addBindingTarget(ctx.name(), "except_target");
        if (isInsideRoutine()) h.enterStatement("CATCH", ctx.getStart().getLine());
    }

    @Override
    public void exitExcept_clause(PythonParser.Except_clauseContext ctx) {
        if (isInsideRoutine()) h.exitStatementWithFullComment("CATCH", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterWith_stmt(PythonParser.With_stmtContext ctx) {
        // 자원 블록(컨텍스트 매니저) — 획득/해제 의미가 함수 내부 분석에서 소실되지
        // 않도록 emit. analyzer 소비(분기 역할)는 첫 Python corpus red 테스트와 함께 확정.
        if (isInsideRoutine()) h.enterStatement("WITH", ctx.getStart().getLine());
    }

    @Override
    public void exitWith_stmt(PythonParser.With_stmtContext ctx) {
        if (isInsideRoutine()) h.exitStatementWithFullComment("WITH", ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // 구조 statement — return/raise/break/continue (spec 016)
    // ========================================
    // leaf(스택 비진입) emit — 표현식 안의 METHOD_CALL 등 기존 자식은 종전 부모에
    // 그대로 붙어 기존 노드 수·부모가 보존된다(FR-009). 표현식 원문은 expression
    // 필드로 보존한다(FR-003). raise 는 return 과 다른 노드다(FR-006).

    @Override
    public void enterReturn_stmt(PythonParser.Return_stmtContext ctx) {
        if (!isInsideRoutine()) return;
        Node node = h.addLeafStatement(
                "RETURN", null, ctx.getStart().getLine(), ctx.getStop().getLine());
        node.expression = ParserUtils.getExactSourceText(ctx.testlist());
    }

    @Override
    public void enterRaise_stmt(PythonParser.Raise_stmtContext ctx) {
        if (!isInsideRoutine()) return;
        Node node = h.addLeafStatement(
                "RAISE", null, ctx.getStart().getLine(), ctx.getStop().getLine());
        // RAISE test (COMMA test)* (FROM test)? — 첫 test 부터 문장 끝까지가 예외식이다.
        if (!ctx.test().isEmpty()) {
            node.expression = ParserUtils.getExactSourceText(ctx.test(0), ctx);
        }
    }

    @Override
    public void enterYield_stmt(PythonParser.Yield_stmtContext ctx) {
        // 생산 statement — 함수가 값을 하나씩 내보내는 generator 의미의 사실.
        if (!isInsideRoutine()) return;
        Node node = h.addLeafStatement(
                "YIELD", null, ctx.getStart().getLine(), ctx.getStop().getLine());
        if (ctx.yield_expr() != null && ctx.yield_expr().yield_arg() != null) {
            node.expression = ParserUtils.getExactSourceText(ctx.yield_expr().yield_arg());
        }
    }

    @Override
    public void enterBreak_stmt(PythonParser.Break_stmtContext ctx) {
        if (isInsideRoutine()) {
            h.addLeafStatement("BREAK", null, ctx.getStart().getLine(), ctx.getStop().getLine());
        }
    }

    @Override
    public void enterContinue_stmt(PythonParser.Continue_stmtContext ctx) {
        if (isInsideRoutine()) {
            h.addLeafStatement("CONTINUE", null, ctx.getStart().getLine(), ctx.getStop().getLine());
        }
    }
}
