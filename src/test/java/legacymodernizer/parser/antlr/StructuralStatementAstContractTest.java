package legacymodernizer.parser.antlr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import legacymodernizer.parser.antlr.c.CAstListener;
import legacymodernizer.parser.antlr.c.CLexer;
import legacymodernizer.parser.antlr.c.CParser;
import legacymodernizer.parser.antlr.java.Java20Lexer;
import legacymodernizer.parser.antlr.java.Java20Parser;
import legacymodernizer.parser.antlr.java.JavaAstListener;
import legacymodernizer.parser.antlr.plsql.PlSqlAstListener;
import legacymodernizer.parser.antlr.plsql.PlSqlLexer;
import legacymodernizer.parser.antlr.plsql.PlSqlParser;
import legacymodernizer.parser.antlr.python.PythonAstListener;
import legacymodernizer.parser.antlr.python.PythonLexer;
import legacymodernizer.parser.antlr.python.PythonParser;
import legacymodernizer.parser.model.Node;

/**
 * spec 016: 구조 구문 AST 생산자 계약 — grammar 가 식별하는 RETURN/THROW/RAISE/
 * BREAK/CONTINUE/GOTO 를 언어별 listener 가 AST 노드로 emit 하고, downstream 이
 * 소스를 다시 파싱하지 않도록 표현식을 {@link Node#expression} 에 보존한다.
 *
 * <p>검증 축 (FR-001~FR-006):
 * <ul>
 *   <li>source occurrence ↔ AST node 누락 0 (missing)</li>
 *   <li>값 없는 return 도 RETURN 노드로 남고 expression 만 null (FR-004)</li>
 *   <li>multiline/nested statement 는 grammar context 범위 그대로 한 노드 (FR-005)</li>
 *   <li>throw/raise 와 normal return 을 합치지 않음 (FR-006)</li>
 *   <li>구조 부모 정확성 — 분기 안 statement 는 그 분기 노드의 자손 (FR-002)</li>
 * </ul>
 */
class StructuralStatementAstContractTest {

    // ── 언어별 파싱 헬퍼 (각 LanguageModule 의 harness 배선과 동일 인자) ──

    private static Node parseC(String source) {
        CharStream cs = CharStreams.fromString(source);
        CLexer lexer = new CLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CParser parser = new CParser(tokens);
        CParser.CompilationUnitContext tree = parser.compilationUnit();
        CAstListener listener = new CAstListener(tokens, null);
        listener.setFileInfo("sample.c", "sample.c");
        new ParseTreeWalker().walk(listener, tree);
        listener.finalizeAst();
        return listener.getRoot();
    }

    private static Node parseJava(String source) {
        CharStream cs = CharStreams.fromString(source);
        Java20Lexer lexer = new Java20Lexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Java20Parser parser = new Java20Parser(tokens);
        Java20Parser.Start_Context tree = parser.start_();
        JavaAstListener listener = new JavaAstListener(tokens, null);
        listener.setFileInfo("Sample.java", "Sample.java");
        new ParseTreeWalker().walk(listener, tree);
        listener.finalizeAst();
        return listener.getRoot();
    }

    private static Node parsePython(String source) {
        CharStream cs = CharStreams.fromString(source);
        PythonLexer lexer = new PythonLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PythonParser parser = new PythonParser(tokens);
        PythonParser.RootContext tree = parser.root();
        PythonAstListener listener = new PythonAstListener(tokens, null);
        listener.setFileInfo("sample.py", "sample.py");
        new ParseTreeWalker().walk(listener, tree);
        listener.finalizeAst();
        return listener.getRoot();
    }

    private static Node parsePlSql(String source) {
        CharStream cs = CharStreams.fromString(source);
        PlSqlLexer lexer = new PlSqlLexer(new CaseChangingCharStream(cs, true));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PlSqlParser parser = new PlSqlParser(tokens);
        PlSqlParser.Sql_scriptContext tree = parser.sql_script();
        PlSqlAstListener listener = new PlSqlAstListener(tokens, null);
        listener.setFileInfo("sample.prc", "sample.prc");
        new ParseTreeWalker().walk(listener, tree);
        listener.finalizeAst();
        return listener.getRoot();
    }

    // ── 트리 탐색 헬퍼 ──────────────────────────────────────────────

    private static void collect(Node node, String type, List<Node> out) {
        if (type.equals(node.type)) out.add(node);
        for (Node child : node.children) collect(child, type, out);
    }

    private static List<Node> all(Node root, String type) {
        List<Node> out = new ArrayList<>();
        collect(root, type, out);
        return out;
    }

    private static Node firstAtLine(Node root, String type, int startLine) {
        for (Node candidate : all(root, type)) {
            if (candidate.startLine == startLine) return candidate;
        }
        return null;
    }

    private static boolean hasAncestorOfType(Node node, String type) {
        for (Node current = node.parent; current != null; current = current.parent) {
            if (type.equals(current.type)) return true;
        }
        return false;
    }

    private static String normalized(String text) {
        return text == null ? null : String.join(" ", text.trim().split("\\s+"));
    }

    // ═══════════════════════════════════════════════════════════════════
    // C — jumpStatement: return/break/continue/goto
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void cReturnOccurrencesBecomeReturnNodesWithExpression() {
        Node root = parseC(
                "long f(long x) {\n" +                 // 1
                "  if (x > 0) {\n" +                   // 2
                "    return -1;\n" +                   // 3
                "  }\n" +                              // 4
                "  return x\n" +                       // 5 (multiline 표현식)
                "      + 2;\n" +                       // 6
                "}\n" +                                // 7
                "void g(void) {\n" +                   // 8
                "  return;\n" +                        // 9
                "}\n");
        List<Node> returns = all(root, "RETURN");
        assertEquals(3, returns.size(), "source return 3건 = RETURN 노드 3건 (missing 0)");

        Node inBranch = firstAtLine(root, "RETURN", 3);
        assertNotNull(inBranch, "분기 안 return 노드");
        assertEquals("-1", normalized(inBranch.expression));
        assertTrue(hasAncestorOfType(inBranch, "IF"), "분기 안 return 은 IF 자손 (FR-002)");

        Node multiline = firstAtLine(root, "RETURN", 5);
        assertNotNull(multiline, "multiline return 노드 (FR-005)");
        assertEquals(6, multiline.endLine, "multiline return 은 grammar 범위 그대로 한 노드");
        assertEquals("x + 2", normalized(multiline.expression));

        Node valueless = firstAtLine(root, "RETURN", 9);
        assertNotNull(valueless, "값 없는 return 도 RETURN 노드 (FR-004)");
        assertNull(valueless.expression, "값 없는 return 의 expression 은 null (FR-004)");
    }

    @Test
    void cBreakContinueGotoBecomeNodes() {
        Node root = parseC(
                "void f(int n) {\n" +                  // 1
                "  int i;\n" +                         // 2
                "  for (i = 0; i < n; i++) {\n" +      // 3
                "    if (i == 1) { continue; }\n" +    // 4
                "    if (i == 2) { break; }\n" +       // 5
                "    if (i == 3) { goto done; }\n" +   // 6
                "  }\n" +                              // 7
                "done:\n" +                            // 8
                "  return;\n" +                        // 9
                "}\n");
        assertEquals(1, all(root, "CONTINUE").size(), "continue 1건");
        assertEquals(1, all(root, "BREAK").size(), "break 1건");
        List<Node> gotos = all(root, "GOTO");
        assertEquals(1, gotos.size(), "goto 1건");
        assertEquals("done", gotos.get(0).name, "goto 대상 라벨은 name 으로 보존");
        List<Node> labels = all(root, "LABEL");
        assertEquals(1, labels.size(), "goto 대상 LABEL 1건");
        assertEquals("done", labels.get(0).name, "LABEL 선언 이름 보존");
        assertEquals(8, labels.get(0).startLine, "LABEL 선언 좌표 보존");
        assertTrue(hasAncestorOfType(all(root, "BREAK").get(0), "LOOP"),
                "loop 안 break 는 LOOP 자손");
        // switch 의 case 종결 break 회귀: 기존 CASE 소유권과 충돌하지 않아야 한다.
        Node switchRoot = parseC(
                "void d(int c) {\n" +
                "  switch (c) {\n" +
                "    case 1: work(); break;\n" +
                "    default: fallback(); break;\n" +
                "  }\n" +
                "}\n");
        assertEquals(2, all(switchRoot, "BREAK").size(), "switch 안 break 2건");
        for (Node breakNode : all(switchRoot, "BREAK")) {
            assertTrue(hasAncestorOfType(breakNode, "CASE"), "case 본문 break 는 CASE 자손");
        }
    }

    @Test
    void cStandaloneUpdatesBecomeAssignmentsButHeaderAndNestedUpdatesDoNot() {
        Node root = parseC(
                "void f(int n) {\n" +
                "  int i = 0;\n" +
                "  i++;\n" +
                "  --n;\n" +
                "  for (i = 0; i < n; i++) {\n" +
                "    consume(i++);\n" +
                "  }\n" +
                "}\n");
        List<Node> assignments = all(root, "ASSIGNMENT");
        assertEquals(3, assignments.size(),
                "initializer and two standalone updates only");

        Node postfix = firstAtLine(root, "ASSIGNMENT", 3);
        assertNotNull(postfix);
        assertEquals("i", postfix.target);
        assertEquals("+=", postfix.operator);
        assertEquals("1", postfix.expression);

        Node prefix = firstAtLine(root, "ASSIGNMENT", 4);
        assertNotNull(prefix);
        assertEquals("n", prefix.target);
        assertEquals("-=", prefix.operator);
        assertEquals("1", prefix.expression);

        assertNull(firstAtLine(root, "ASSIGNMENT", 6),
                "argument update belongs to the enclosing call expression");
    }

    // ═══════════════════════════════════════════════════════════════════
    // Java — returnStatement / throwStatement / break / continue
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void javaReturnAndThrowAreDistinctNodes() {
        Node root = parseJava(
                "class Sample {\n" +                                    // 1
                "  int f(int x) {\n" +                                  // 2
                "    if (x > 0) {\n" +                                  // 3
                "      return x + 1;\n" +                               // 4
                "    }\n" +                                             // 5
                "    throw new IllegalStateException(\"bad\");\n" +     // 6
                "  }\n" +                                               // 7
                "  void g() {\n" +                                      // 8
                "    return;\n" +                                       // 9
                "  }\n" +                                               // 10
                "}\n");
        List<Node> returns = all(root, "RETURN");
        assertEquals(2, returns.size(), "return 2건 (missing 0)");
        Node valued = firstAtLine(root, "RETURN", 4);
        assertNotNull(valued);
        assertEquals("x + 1", normalized(valued.expression));
        assertTrue(hasAncestorOfType(valued, "IF"), "분기 안 return 은 IF 자손");
        Node valueless = firstAtLine(root, "RETURN", 9);
        assertNotNull(valueless, "값 없는 return 도 RETURN 노드 (FR-004)");
        assertNull(valueless.expression);

        List<Node> throws_ = all(root, "THROW");
        assertEquals(1, throws_.size(), "throw 는 RETURN 과 합치지 않는다 (FR-006)");
        assertEquals("new IllegalStateException(\"bad\")",
                normalized(throws_.get(0).expression));
    }

    @Test
    void javaBreakContinueBecomeNodes() {
        Node root = parseJava(
                "class Sample {\n" +
                "  void f(int n) {\n" +
                "    for (int i = 0; i < n; i++) {\n" +
                "      if (i == 1) { continue; }\n" +
                "      if (i == 2) { break; }\n" +
                "    }\n" +
                "  }\n" +
                "}\n");
        assertEquals(1, all(root, "CONTINUE").size());
        assertEquals(1, all(root, "BREAK").size());
    }

    // ═══════════════════════════════════════════════════════════════════
    // Python — return_stmt / raise_stmt / break / continue
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void pythonReturnAndRaiseAreDistinctNodes() {
        Node root = parsePython(
                "def f(x):\n" +                        // 1
                "    if x > 0:\n" +                    // 2
                "        return x + 1\n" +             // 3
                "    raise ValueError('bad')\n" +      // 4
                "\n" +                                 // 5
                "def g():\n" +                         // 6
                "    return\n");                       // 7
        List<Node> returns = all(root, "RETURN");
        assertEquals(2, returns.size(), "return 2건 (missing 0)");
        Node valued = firstAtLine(root, "RETURN", 3);
        assertNotNull(valued);
        assertEquals("x + 1", normalized(valued.expression));
        assertTrue(hasAncestorOfType(valued, "IF"), "분기 안 return 은 IF 자손");
        Node valueless = firstAtLine(root, "RETURN", 7);
        assertNotNull(valueless, "값 없는 return 도 RETURN 노드 (FR-004)");
        assertNull(valueless.expression);

        List<Node> raises = all(root, "RAISE");
        assertEquals(1, raises.size(), "raise 는 RETURN 과 합치지 않는다 (FR-006)");
        assertEquals("ValueError('bad')", normalized(raises.get(0).expression));
    }

    @Test
    void pythonBreakContinueBecomeNodes() {
        Node root = parsePython(
                "def f(items):\n" +
                "    for item in items:\n" +
                "        if item == 1:\n" +
                "            continue\n" +
                "        if item == 2:\n" +
                "            break\n");
        assertEquals(1, all(root, "CONTINUE").size());
        assertEquals(1, all(root, "BREAK").size());
        assertTrue(hasAncestorOfType(all(root, "BREAK").get(0), "LOOP"),
                "loop 안 break 는 LOOP 자손");
    }

    // ═══════════════════════════════════════════════════════════════════
    // ASSIGNMENT — statement-level 대입의 target/operator/expression (FR-003)
    // for 머리(초기화/증감)의 대입은 문장 효과가 아니므로 노드가 아니다 (TA-102 정합).
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void cStatementAssignmentsBecomeNodesButForHeaderDoesNot() {
        Node root = parseC(
                "void f(int n) {\n" +                        // 1
                "  int i;\n" +                               // 2
                "  long rc;\n" +                             // 3
                "  rc = init();\n" +                         // 4
                "  for (i = 0; i < n; i++) {\n" +            // 5
                "    rc += i;\n" +                           // 6
                "  }\n" +                                    // 7
                "  if (n > 0) {\n" +                         // 8
                "    rc = n * 2;\n" +                        // 9
                "  }\n" +                                    // 10
                "}\n");
        List<Node> assignments = all(root, "ASSIGNMENT");
        assertEquals(3, assignments.size(),
                "문장 대입 3건(4·6·9행) — for 머리 i=0/i++ 는 제외 (TA-102)");
        Node top = firstAtLine(root, "ASSIGNMENT", 4);
        assertNotNull(top);
        assertEquals("rc", normalized(top.target));
        assertEquals("=", top.operator);
        assertEquals("init()", normalized(top.expression));
        Node compound = firstAtLine(root, "ASSIGNMENT", 6);
        assertNotNull(compound, "복합 대입도 grammar 사실이다");
        assertEquals("+=", compound.operator);
        assertTrue(hasAncestorOfType(compound, "LOOP"), "loop 본문 대입은 LOOP 자손");
        Node inBranch = firstAtLine(root, "ASSIGNMENT", 9);
        assertNotNull(inBranch);
        assertTrue(hasAncestorOfType(inBranch, "IF"), "분기 안 대입은 IF 자손");
        assertEquals("n * 2", normalized(inBranch.expression));
    }

    @Test
    void javaStatementAssignmentsBecomeNodes() {
        Node root = parseJava(
                "class Sample {\n" +                          // 1
                "  void f(int n) {\n" +                       // 2
                "    int rc;\n" +                             // 3
                "    rc = n + 1;\n" +                         // 4
                "    for (int i = 0; i < n; i++) {\n" +       // 5
                "      rc = rc + i;\n" +                      // 6
                "    }\n" +                                   // 7
                "  }\n" +                                     // 8
                "}\n");
        List<Node> assignments = all(root, "ASSIGNMENT");
        assertEquals(2, assignments.size(), "문장 대입 2건(4·6행) — for 머리는 제외");
        Node top = firstAtLine(root, "ASSIGNMENT", 4);
        assertNotNull(top);
        assertEquals("rc", normalized(top.target));
        assertEquals("=", top.operator);
        assertEquals("n + 1", normalized(top.expression));
        assertTrue(hasAncestorOfType(firstAtLine(root, "ASSIGNMENT", 6), "LOOP"));
    }

    @Test
    void pythonStatementAssignmentsBecomeNodes() {
        Node root = parsePython(
                "def f(n):\n" +                               // 1
                "    rc = n + 1\n" +                          // 2
                "    if n > 0:\n" +                           // 3
                "        rc += 1\n" +                         // 4
                "    return rc\n");                           // 5
        List<Node> assignments = all(root, "ASSIGNMENT");
        assertEquals(2, assignments.size(), "함수 안 대입 2건(2·4행)");
        Node top = firstAtLine(root, "ASSIGNMENT", 2);
        assertNotNull(top);
        assertEquals("rc", normalized(top.target));
        assertEquals("=", top.operator);
        assertEquals("n + 1", normalized(top.expression));
        Node augmented = firstAtLine(root, "ASSIGNMENT", 4);
        assertNotNull(augmented, "augmented 대입도 grammar 사실이다");
        assertEquals("+=", augmented.operator);
        assertTrue(hasAncestorOfType(augmented, "IF"), "분기 안 대입은 IF 자손");
    }

    @Test
    void pythonCallsDoNotGuessConstructorsOrVariableTypesFromCapitalization() {
        Node root = parsePython(
                "def HTTP():\n" +                              // 대문자 함수
                "    return 1\n" +
                "\n" +
                "class lower:\n" +                            // 소문자 callable class
                "    pass\n" +
                "\n" +
                "upper_result = HTTP()\n" +
                "lower_result = lower()\n");

        assertEquals(2, all(root, "FUNCTION_CALL").size(),
                "두 호출은 모두 문법이 증명한 call로 보존한다");
        assertEquals(0, all(root, "NEW_INSTANCE").size(),
                "Python call 문법만으로 constructor를 추측하지 않는다");

        Node upper = all(root, "VARIABLE").stream()
                .filter(node -> "upper_result".equals(node.name)).findFirst().orElseThrow();
        Node lower = all(root, "VARIABLE").stream()
                .filter(node -> "lower_result".equals(node.name)).findFirst().orElseThrow();
        assertNull(upper.variableType,
                "대문자 함수 호출을 변수 타입으로 승격하지 않는다");
        assertNull(lower.variableType,
                "소문자 callable class도 Parser가 임의 타입으로 승격하지 않는다");
    }

    @Test
    void declarationInitializersBecomeAssignmentNodes() {
        Node cRoot = parseC(
                "void f(int n) {\n" +                 // 1
                "  long rc = init();\n" +             // 2 (선언 초기화 = 실행 효과)
                "  int bare;\n" +                     // 3 (초기화 없음 — 노드 아님)
                "  for (int i = 0; i < n; i++) {\n" + // 4 (for 머리 — 제외)
                "    use(i);\n" +                     // 5
                "  }\n" +                             // 6
                "}\n");
        List<Node> cAssigns = all(cRoot, "ASSIGNMENT");
        assertEquals(1, cAssigns.size(), "선언 초기화 1건만 — bare/for머리 제외");
        assertEquals("rc", cAssigns.get(0).target);
        assertEquals("init()", normalized(cAssigns.get(0).expression));

        Node javaRoot = parseJava(
                "class Sample {\n" +
                "  void f(int n) {\n" +
                "    int rc = n + 1;\n" +             // 3
                "    int bare;\n" +                   // 4
                "    for (int i = 0; i < n; i++) {\n" + // 5 (for 머리 — 제외)
                "      use(i);\n" +
                "    }\n" +
                "  }\n" +
                "  void use(int v) { }\n" +
                "}\n");
        List<Node> javaAssigns = all(javaRoot, "ASSIGNMENT");
        assertEquals(1, javaAssigns.size(), "선언 초기화 1건만 — bare/for머리 제외");
        assertEquals("rc", javaAssigns.get(0).target);
        assertEquals("n + 1", normalized(javaAssigns.get(0).expression));
    }

    @Test
    void plsqlAssignmentCarriesTargetOperatorExpression() {
        Node root = parsePlSql(
                "CREATE OR REPLACE PROCEDURE set_status(p_id NUMBER)\n" +  // 1
                "IS\n" +                                                   // 2
                "  v_status VARCHAR2(10);\n" +                             // 3
                "BEGIN\n" +                                                // 4
                "  v_status := 'OK';\n" +                                  // 5
                "END;\n" +                                                 // 6
                "/\n");
        List<Node> assignments = all(root, "ASSIGNMENT");
        assertEquals(1, assignments.size());
        Node assignment = assignments.get(0);
        assertEquals("v_status", normalized(assignment.target));
        assertEquals(":=", assignment.operator);
        assertEquals("'OK'", normalized(assignment.expression));
    }

    // ═══════════════════════════════════════════════════════════════════
    // 제어 조건 — IF/LOOP/SWITCH/CASE 의 expression 보존 (FR-003)
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void controlConditionsCarryExpression() {
        Node cRoot = parseC(
                "void f(int n) {\n" +                        // 1
                "  int i;\n" +                               // 2
                "  for (i = 0; i < n; i++) {\n" +            // 3
                "    if (i == 1) { work(); }\n" +            // 4
                "  }\n" +                                    // 5
                "  switch (n) {\n" +                         // 6
                "    case 1: work(); break;\n" +             // 7
                "    default: fallback(); break;\n" +        // 8
                "  }\n" +                                    // 9
                "}\n");
        assertEquals("i < n", normalized(firstAtLine(cRoot, "LOOP", 3).expression),
                "for 머리의 판정절만 조건 — 초기화/증감 제외 (TA-102)");
        assertEquals("i == 1", normalized(firstAtLine(cRoot, "IF", 4).expression));
        assertEquals("n", normalized(firstAtLine(cRoot, "SWITCH", 6).expression));
        assertEquals("1", normalized(firstAtLine(cRoot, "CASE", 7).expression));
        assertNull(firstAtLine(cRoot, "CASE", 8).expression, "default 라벨은 expression null");

        Node javaRoot = parseJava(
                "class Sample {\n" +
                "  void f(int n) {\n" +
                "    for (int i = 0; i < n; i++) {\n" +      // 3
                "      if (i == 1) { work(); }\n" +          // 4
                "    }\n" +
                "  }\n" +
                "  void work() { }\n" +
                "}\n");
        assertEquals("i < n", normalized(firstAtLine(javaRoot, "LOOP", 3).expression));
        assertEquals("i == 1", normalized(firstAtLine(javaRoot, "IF", 4).expression));

        Node pythonRoot = parsePython(
                "def f(items):\n" +
                "    for item in items:\n" +                  // 2
                "        if item == 1:\n" +                   // 3
                "            work(item)\n");
        assertEquals("item in items", normalized(firstAtLine(pythonRoot, "LOOP", 2).expression));
        assertEquals("item == 1", normalized(firstAtLine(pythonRoot, "IF", 3).expression));

        Node plsqlRoot = parsePlSql(
                "CREATE OR REPLACE FUNCTION get_status(p_id NUMBER)\n" +
                "RETURN VARCHAR2\n" +
                "IS\n" +
                "BEGIN\n" +
                "  IF p_id > 0 THEN\n" +                      // 5
                "    RETURN 'OK';\n" +
                "  END IF;\n" +
                "  RETURN 'NG';\n" +
                "END;\n" +
                "/\n");
        assertEquals("p_id > 0", normalized(firstAtLine(plsqlRoot, "IF", 5).expression));
    }

    @Test
    void doWhileConditionIsExplicitlyPostTested() {
        Node cRoot = parseC(
                "void f(int n) {\n" +
                "  do { n--; } while (n > 0);\n" +
                "}\n");
        Node cLoop = firstAtLine(cRoot, "LOOP", 2);
        assertEquals("n > 0", normalized(cLoop.expression));
        assertEquals("post", cLoop.conditionTiming);

        Node javaRoot = parseJava(
                "class Sample {\n" +
                "  void f(int n) {\n" +
                "    do { n--; } while (n > 0);\n" +
                "  }\n" +
                "}\n");
        Node javaLoop = firstAtLine(javaRoot, "LOOP", 3);
        assertEquals("n > 0", normalized(javaLoop.expression));
        assertEquals("post", javaLoop.conditionTiming);
    }

    // ═══════════════════════════════════════════════════════════════════
    // PL/SQL — RETURN 노드는 이미 존재; 표현식 필드가 계약 (FR-003)
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void plsqlReturnCarriesExpression() {
        Node root = parsePlSql(
                "CREATE OR REPLACE FUNCTION get_status(p_id NUMBER)\n" +  // 1
                "RETURN VARCHAR2\n" +                                     // 2
                "IS\n" +                                                  // 3
                "BEGIN\n" +                                               // 4
                "  IF p_id > 0 THEN\n" +                                  // 5
                "    RETURN 'OK';\n" +                                    // 6
                "  END IF;\n" +                                           // 7
                "  RETURN 'NG';\n" +                                      // 8
                "END;\n" +                                                // 9
                "/\n");
        List<Node> returns = all(root, "RETURN");
        assertEquals(2, returns.size(), "실행 return 2건 (missing 0)");
        Node inBranch = firstAtLine(root, "RETURN", 6);
        assertNotNull(inBranch, "분기 안 RETURN 노드");
        assertEquals("'OK'", normalized(inBranch.expression),
                "반환식은 expression 필드로 보존 (FR-003)");
        assertTrue(hasAncestorOfType(inBranch, "IF"), "분기 안 RETURN 은 IF 자손");
        Node trailing = firstAtLine(root, "RETURN", 8);
        assertNotNull(trailing);
        assertEquals("'NG'", normalized(trailing.expression));
    }

    @Test
    void plsqlTableFunctionKeepsValuelessReturn() {
        Node root = parsePlSql(
                "CREATE OR REPLACE FUNCTION rows_for(p_id NUMBER)\n" +
                "RETURN TABLE IS\n" +
                "BEGIN\n" +
                "  SELECT p_id AS ID FROM DUAL;\n" +
                "  RETURN;\n" +
                "END;\n" +
                "/\n");
        Node trailing = firstAtLine(root, "RETURN", 5);
        assertNotNull(trailing, "table function의 값 없는 RETURN도 실행 statement다");
        assertNull(trailing.expression);
    }

    @Test
    void plsqlTableFunctionKeepsReturnAfterUnionWithScalarSubqueries() {
        Node root = parsePlSql(
                "CREATE OR REPLACE FUNCTION rows_for(p_id NUMBER)\n" +
                "RETURN TABLE IS\n" +
                "BEGIN\n" +
                "  SELECT p_id AS ID, (SELECT 1 FROM DUAL) AS V1 FROM DUAL\n" +
                "  UNION ALL\n" +
                "  SELECT p_id AS ID, (SELECT 2 FROM DUAL) AS V1 FROM DUAL\n" +
                "  RETURN;\n" +
                "END;\n" +
                "/\n");
        Node trailing = firstAtLine(root, "RETURN", 7);
        assertNotNull(trailing,
                "UNION SELECT 뒤의 값 없는 RETURN도 SELECT 범위에 흡수되지 않는다");
        assertNull(trailing.expression);
        Node union = all(root, "UNION_ALL").get(0);
        assertEquals(6, union.endLine, "RETURN 좌표를 UNION/SELECT 범위에서 분리한다");
        assertFalse(all(root, "SELECT").stream()
                .flatMap(node -> node.dataObjectReferences == null
                        ? java.util.stream.Stream.empty()
                        : node.dataObjectReferences.stream())
                .anyMatch(reference -> "RETURN".equalsIgnoreCase(reference.alias)),
                "RETURN을 DUAL의 table alias 근거로 남기지 않는다");
    }

    /**
     * spec 118 §1.4 — {@code EXIT WHEN <조건>} 의 조건식 원문 보존 (FR-003).
     *
     * <p>조건 없는 {@code LOOP} 은 자기 조건이 없으므로 반복 종료 판정이 오직 여기에만
     * 있다. 이 조건을 버리면 AST 어디에도 남지 않는다 — 2026-08-07 rwis 실측에서
     * {@code EXIT WHEN c%NOTFOUND} 5건이 전부 빈 expression 이었고, 그 결과 analyzer 가
     * 커서 루프의 종료 조건을 만들 근거를 잃었다. {@code enterElsif_part} 는 같은 계약을
     * 이미 지키고 있었다 — 한쪽만 지킨 상태였다.
     */
    @Test
    void plsqlExitWhenCarriesConditionExpression() {
        Node root = parsePlSql(
                "CREATE OR REPLACE PROCEDURE drain(p_id NUMBER)\n" +   // 1
                "IS\n" +                                               // 2
                "  CURSOR c IS SELECT 1 AS v FROM DUAL;\n" +           // 3
                "  r c%ROWTYPE;\n" +                                   // 4
                "BEGIN\n" +                                            // 5
                "  OPEN c;\n" +                                        // 6
                "  LOOP\n" +                                           // 7
                "    FETCH c INTO r;\n" +                              // 8
                "    EXIT WHEN c%NOTFOUND;\n" +                        // 9
                "  END LOOP;\n" +                                      // 10
                "  CLOSE c;\n" +                                       // 11
                "END;\n" +                                             // 12
                "/\n");
        List<Node> exits = all(root, "EXIT");
        assertEquals(1, exits.size(), "EXIT 1건 (missing 0)");
        Node conditional = firstAtLine(root, "EXIT", 9);
        assertNotNull(conditional, "조건부 EXIT 노드");
        assertEquals("c%NOTFOUND", normalized(conditional.expression),
                "WHEN 조건은 expression 필드로 보존 (FR-003)");
        assertTrue(hasAncestorOfType(conditional, "LOOP"), "loop 안 EXIT 는 LOOP 자손");
    }

    /** 무조건 {@code EXIT} 는 조건이 없는 것이 사실이므로 expression 이 null 이다. */
    @Test
    void plsqlUnconditionalExitHasNoExpression() {
        Node root = parsePlSql(
                "CREATE OR REPLACE PROCEDURE once\n" +                 // 1
                "IS\n" +                                               // 2
                "BEGIN\n" +                                            // 3
                "  LOOP\n" +                                           // 4
                "    EXIT;\n" +                                        // 5
                "  END LOOP;\n" +                                      // 6
                "END;\n" +                                             // 7
                "/\n");
        Node unconditional = firstAtLine(root, "EXIT", 5);
        assertNotNull(unconditional, "무조건 EXIT 도 노드로 남는다");
        assertNull(unconditional.expression, "조건이 없다는 사실을 null 로 표현한다");
    }

    @Test
    void plsqlTransactionControlKeepsCommitAndRollbackAsSiblingStatements() {
        Node root = parsePlSql(
                "CREATE OR REPLACE PROCEDURE finish_work(p_ok NUMBER)\n" + // 1
                "IS\n" +                                                    // 2
                "BEGIN\n" +                                                 // 3
                "  IF p_ok = 1 THEN\n" +                                    // 4
                "    COMMIT;\n" +                                           // 5
                "  ELSE\n" +                                                // 6
                "    ROLLBACK;\n" +                                         // 7
                "  END IF;\n" +                                             // 8
                "END;\n" +                                                  // 9
                "/\n");
        List<Node> commits = all(root, "COMMIT");
        List<Node> rollbacks = all(root, "ROLLBACK");
        assertEquals(1, commits.size(), "COMMIT 1건 (missing 0)");
        assertEquals(1, rollbacks.size(), "ROLLBACK 1건 (missing 0)");
        assertEquals(5, commits.get(0).startLine);
        assertEquals(7, rollbacks.get(0).startLine);
        assertTrue(hasAncestorOfType(commits.get(0), "IF"));
        assertTrue(hasAncestorOfType(rollbacks.get(0), "ELSE"));
    }
}
