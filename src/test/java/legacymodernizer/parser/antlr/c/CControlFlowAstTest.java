package legacymodernizer.parser.antlr.c;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import legacymodernizer.parser.model.Node;

/**
 * spec 007: C 제어 흐름 의미 AST 노드(IF/ELSE/LOOP/SWITCH/CASE) 회귀 테스트.
 *
 * 검증: (US1) 중첩 if/else·loop·switch/case 가 정확한 부모관계로 emit 되는지,
 *       (US2) 제어문 아래 FUNCTION_CALL 이 소실되지 않는지(총 개수 보존),
 *       C 에는 TRY/CATCH 가 생성되지 않는지.
 */
class CControlFlowAstTest {

    private static Node parse(String source) {
        CharStream cs = CharStreams.fromString(source);
        CLexer lexer = new CLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CParser parser = new CParser(tokens);
        CParser.CompilationUnitContext tree = parser.compilationUnit();
        CAstListener listener = new CAstListener(tokens, null);
        listener.setFileInfo("sample.c", "sample.c");
        new ParseTreeWalker().walk(listener, tree);
        return listener.getRoot();
    }

    // ── 트리 탐색 헬퍼 ──────────────────────────────────────────────
    private static Node findFirst(Node n, String type, String name) {
        if (type.equals(n.type) && (name == null || name.equals(n.name))) return n;
        for (Node c : n.children) {
            Node r = findFirst(c, type, name);
            if (r != null) return r;
        }
        return null;
    }

    private static void collect(Node n, String type, List<Node> out) {
        if (type.equals(n.type)) out.add(n);
        for (Node c : n.children) collect(c, type, out);
    }

    private static int count(Node n, String type) {
        List<Node> out = new ArrayList<>();
        collect(n, type, out);
        return out.size();
    }

    private static int directChildrenOfType(Node n, String type) {
        int k = 0;
        for (Node c : n.children) if (type.equals(c.type)) k++;
        return k;
    }

    private static final String SRC =
        "int checkout(int paid, int stock) {\n" +
        "    if (paid) {\n" +
        "        save_order();\n" +
        "        if (stock > 0) {\n" +
        "            reserve();\n" +
        "        } else {\n" +
        "            backorder();\n" +
        "        }\n" +
        "    } else if (stock) {\n" +
        "        hold();\n" +
        "    } else {\n" +
        "        cancel();\n" +
        "    }\n" +
        "    for (int i = 0; i < 10; i++) {\n" +
        "        tick();\n" +
        "    }\n" +
        "    while (paid) {\n" +
        "        drain();\n" +
        "    }\n" +
        "    switch (stock) {\n" +
        "        case 1: one(); break;\n" +
        "        default: other(); break;\n" +
        "    }\n" +
        "    if (paid) quick();\n" +
        "    return 0;\n" +
        "}\n" +
        "\n" +
        "int plain(int a) {\n" +
        "    return a + 1;\n" +
        "}\n";

    @Test
    void controlNodesEmittedWithCorrectNesting() {
        Node root = parse(SRC);
        Node checkout = findFirst(root, "FUNCTION", "checkout");
        assertNotNull(checkout, "checkout FUNCTION 노드가 있어야 한다");

        // 함수 직속: IF 2개(if-else 체인 1 + 단일라인 if 1), LOOP 2개(for/while), SWITCH 1개
        assertEquals(2, directChildrenOfType(checkout, "IF"), "checkout 직속 IF 2개");
        assertEquals(2, directChildrenOfType(checkout, "LOOP"), "checkout 직속 LOOP 2개(for/while)");
        assertEquals(1, directChildrenOfType(checkout, "SWITCH"), "checkout 직속 SWITCH 1개");
    }

    @Test
    void nestedIfAndElseUnderOuterIf() {
        Node root = parse(SRC);
        Node checkout = findFirst(root, "FUNCTION", "checkout");

        // 첫 IF(paid): 직속 자식으로 FUNCTION_CALL(save_order), 중첩 IF, ELSE 가 있어야 함
        Node outerIf = null;
        for (Node c : checkout.children) {
            if ("IF".equals(c.type) && findFirst(c, "FUNCTION_CALL", "save_order") != null) {
                outerIf = c; break;
            }
        }
        assertNotNull(outerIf, "save_order 를 품은 바깥 IF 존재");
        assertEquals(1, directChildrenOfType(outerIf, "ELSE"), "바깥 IF 는 ELSE 1개를 가진다");
        assertTrue(directChildrenOfType(outerIf, "IF") >= 1, "바깥 IF 아래 중첩 IF(stock>0) 존재");

        // 중첩 IF(stock>0): FUNCTION_CALL(reserve) + ELSE(→backorder)
        Node innerIf = null;
        for (Node c : outerIf.children) {
            if ("IF".equals(c.type)) { innerIf = c; break; }
        }
        assertNotNull(innerIf);
        assertNotNull(findFirst(innerIf, "FUNCTION_CALL", "reserve"), "중첩 IF 아래 reserve 호출");
        Node innerElse = null;
        for (Node c : innerIf.children) if ("ELSE".equals(c.type)) innerElse = c;
        assertNotNull(innerElse, "중첩 IF 의 ELSE");
        assertNotNull(findFirst(innerElse, "FUNCTION_CALL", "backorder"), "ELSE 아래 backorder 호출");
    }

    @Test
    void elseIfRepresentedAsNestedIfUnderElse() {
        Node root = parse(SRC);
        Node checkout = findFirst(root, "FUNCTION", "checkout");
        Node outerIf = null;
        for (Node c : checkout.children) {
            if ("IF".equals(c.type) && findFirst(c, "FUNCTION_CALL", "save_order") != null) {
                outerIf = c; break;
            }
        }
        assertNotNull(outerIf);
        Node outerElse = null;
        for (Node c : outerIf.children) if ("ELSE".equals(c.type)) outerElse = c;
        assertNotNull(outerElse, "바깥 IF 의 ELSE");
        // else if (stock) → ELSE 안에 중첩 IF (별도 ELSEIF 타입 없음)
        Node elseIf = null;
        for (Node c : outerElse.children) if ("IF".equals(c.type)) elseIf = c;
        assertNotNull(elseIf, "else-if 는 ELSE 안 중첩 IF 로 표현");
        assertNotNull(findFirst(elseIf, "FUNCTION_CALL", "hold"), "else-if 아래 hold 호출");
        // 그리고 그 IF 의 ELSE 아래 cancel 호출(마지막 else)
        Node elseIfElse = null;
        for (Node c : elseIf.children) if ("ELSE".equals(c.type)) elseIfElse = c;
        assertNotNull(elseIfElse, "else-if 의 마지막 else");
        assertNotNull(findFirst(elseIfElse, "FUNCTION_CALL", "cancel"), "마지막 else 아래 cancel 호출");
    }

    @Test
    void switchHasCaseChildren() {
        Node root = parse(SRC);
        Node sw = findFirst(root, "SWITCH", null);
        assertNotNull(sw, "SWITCH 노드 존재");
        assertTrue(count(sw, "CASE") >= 2, "case 1 + default → CASE 2개 이상");
        assertNotNull(findFirst(sw, "FUNCTION_CALL", "one"), "case 아래 one 호출");
        assertNotNull(findFirst(sw, "FUNCTION_CALL", "other"), "default 아래 other 호출");
    }

    @Test
    void functionCallsNotLostUnderControlNodes() {
        Node root = parse(SRC);
        // save_order, reserve, backorder, hold, cancel, tick, drain, one, other, quick = 10
        assertEquals(10, count(root, "FUNCTION_CALL"),
                "제어문 아래로 중첩돼도 FUNCTION_CALL 총 개수 보존(소실 0)");
    }

    @Test
    void noTryCatchInC() {
        Node root = parse(SRC);
        assertEquals(0, count(root, "TRY"), "C 에는 TRY 노드 없음");
        assertEquals(0, count(root, "CATCH"), "C 에는 CATCH 노드 없음");
    }

    @Test
    void functionWithoutControlFlowUnchanged() {
        Node root = parse(SRC);
        Node plain = findFirst(root, "FUNCTION", "plain");
        assertNotNull(plain);
        assertEquals(0, count(plain, "IF"));
        assertEquals(0, count(plain, "LOOP"));
        assertEquals(0, count(plain, "SWITCH"));
        assertEquals(0, count(plain, "FUNCTION_CALL"));
    }
}
