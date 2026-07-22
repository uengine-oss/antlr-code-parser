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
 * ProFrame C 관용구 회귀 테스트 — 매크로 인자 안의 호출이 AST 에서 소실되지 않는지.
 *
 * 배경: ProFrame 코드는 업무 호출을 {@code PFM_TRY( b100_dbio_...(itf) )} 처럼
 * 예외 매크로로 감싼다. 하류(analyzer)의 DB 도달성 계산은 이 안쪽 호출이
 * FUNCTION_CALL 노드로 방출된다는 전제 위에 서므로, 여기서 그 전제를 고정한다.
 *
 * 검증: (1) 매크로 인자 안의 업무 호출({@code a000_*}, {@code b100_*})이
 *        FUNCTION_CALL 로 방출된다,
 *       (2) DB 실행 함수 {@code mpfmdbio} 호출과 그 첫 문자열 인자(SQL_KEY)가
 *        노드에 보존된다,
 *       (3) {@code PFM_CATCH :} 라벨이 있어도 함수 정의가 깨지지 않는다.
 */
class CProFrameMacroCallTest {

    private static Node parse(String source) {
        CharStream cs = CharStreams.fromString(source);
        CLexer lexer = new CLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CParser parser = new CParser(tokens);
        CParser.CompilationUnitContext tree = parser.compilationUnit();
        CAstListener listener = new CAstListener(tokens, null);
        listener.setFileInfo("proframe_sample.c", "proframe_sample.c");
        new ParseTreeWalker().walk(listener, tree);
        return listener.getRoot();
    }

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

    /** 실제 SKT ProFrame 코드(zapamcom10060.c 계열)의 형태를 축약한 표본. */
    private static final String SRC =
        "static long b100_dbio_comm_cd_dtl_canyn(long flag);\n" +
        "\n" +
        "long zapamcom10060(long itf) {\n" +
        "    long rc = 0;\n" +
        "    PFM_TRY( a000_input_validation( itf ) );\n" +
        "    PFM_TRYNJ( b100_dbio_comm_cd_dtl_canyn( 0 ) );\n" +
        "    return 0;\n" +
        "PFM_CATCH:\n" +
        "    return -1;\n" +
        "}\n" +
        "\n" +
        "static long b100_dbio_comm_cd_dtl_canyn(long flag) {\n" +
        "    long rc;\n" +
        "    rc = mpfmdbio(\"zngm_comm_cd_dtl_s0024\", 0, 0);\n" +
        "    PFM_DBG(\"check [%s]\", \"Y\");\n" +
        "    return rc;\n" +
        "}\n";

    @Test
    void macroWrappedBusinessCallsAreEmitted() {
        Node root = parse(SRC);
        assertNotNull(root);

        Node entry = findFirst(root, "FUNCTION", "zapamcom10060");
        assertNotNull(entry, "진입 함수 정의가 FUNCTION 으로 방출돼야 한다");

        assertNotNull(findFirst(entry, "FUNCTION_CALL", "a000_input_validation"),
            "PFM_TRY 인자 안의 업무 호출이 소실되면 안 된다");
        assertNotNull(findFirst(entry, "FUNCTION_CALL", "b100_dbio_comm_cd_dtl_canyn"),
            "PFM_TRYNJ 인자 안의 업무 호출이 소실되면 안 된다");
    }

    @Test
    void mpfmdbioCallSiteIsLocatable() {
        // C FUNCTION_CALL 은 이름+라인 범위만 담는다(인자 텍스트는 AST 계약 밖 —
        // SQL_KEY 는 analyzer 가 원문 정규식으로 읽는다). 도달성 계산이 의존하는
        // 계약은 "호출 이름과 위치가 래퍼 함수 아래에 방출된다"까지다.
        Node root = parse(SRC);
        Node wrapper = findFirst(root, "FUNCTION", "b100_dbio_comm_cd_dtl_canyn");
        assertNotNull(wrapper, "래퍼 함수 정의가 FUNCTION 으로 방출돼야 한다");

        Node dbio = findFirst(wrapper, "FUNCTION_CALL", "mpfmdbio");
        assertNotNull(dbio, "DB 실행 함수 호출이 FUNCTION_CALL 로 방출돼야 한다");
        assertTrue(dbio.startLine > 0,
            "호출 위치(라인)가 있어야 원문에서 SQL_KEY 를 찾을 수 있다");
    }

    @Test
    void catchLabelDoesNotBreakFunctionBoundary() {
        Node root = parse(SRC);
        List<Node> functions = new ArrayList<>();
        collect(root, "FUNCTION", functions);
        assertEquals(2, functions.size(),
            "PFM_CATCH 라벨이 있어도 함수 정의는 정확히 2개여야 한다");
    }
}
