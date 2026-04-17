package legacymodernizer.parser.antlr;

import java.util.Stack;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

import legacymodernizer.parser.model.Node;
import legacymodernizer.parser.service.ParseProgressTracker;

/**
 * 모든 언어별 ANTLR 리스너가 공유하는 AST 구축 헬퍼.
 *
 * 각 리스너는 서로 다른 ANTLR 생성 베이스 클래스를 상속하므로
 * 공통 상속이 불가능하다. 대신 이 헬퍼를 컴포지션으로 사용하여
 * enterStatement / exitStatement / setFileInfo 등을 위임한다.
 */
public class ListenerHelper {

    private final CommonTokenStream tokens;
    private final Stack<Node> nodeStack = new Stack<>();
    private final Node root;
    private final ParseProgressTracker progressTracker;

    public ListenerHelper(CommonTokenStream tokens, ParseProgressTracker tracker) {
        this.tokens = tokens;
        this.progressTracker = tracker;
        this.root = new Node("FILE", 0, null);
        this.nodeStack.push(root);
    }

    public Node getRoot() {
        return root;
    }

    public Stack<Node> getNodeStack() {
        return nodeStack;
    }

    public CommonTokenStream getTokens() {
        return tokens;
    }

    // ═══════════════════════════════════════════════════════════════════
    // 진행률 추적
    // ═══════════════════════════════════════════════════════════════════

    public void checkProgress(ParserRuleContext ctx) {
        if (progressTracker != null && ctx.getStart() != null) {
            progressTracker.checkLine(ctx.getStart().getLine());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // 노드 생성 / 종료
    // ═══════════════════════════════════════════════════════════════════

    public Node enterStatement(String type, String name, int line) {
        Node node = new Node(type, name, line, nodeStack.peek());
        nodeStack.push(node);
        return node;
    }

    /**
     * 노드 종료 — 기본: leading 주석만 추출 (Java, Python, PostgreSQL 등)
     */
    public void exitStatement(String type, int line, ParserRuleContext ctx) {
        if (!nodeStack.isEmpty() && nodeStack.peek().type.equals(type)) {
            Node node = nodeStack.pop();
            node.endLine = line;
            if (ctx != null) {
                node.comment = ParserUtils.getLeadingComment(ctx, tokens);
            }
        }
    }

    /**
     * 노드 종료 — leading + trailing 주석 모두 추출 (C 등)
     */
    public void exitStatementWithFullComment(String type, int line, ParserRuleContext ctx) {
        if (!nodeStack.isEmpty() && nodeStack.peek().type.equals(type)) {
            Node node = nodeStack.pop();
            node.endLine = line;
            if (ctx != null) {
                node.comment = ParserUtils.getComment(ctx, tokens);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // 파일 정보 설정
    // ═══════════════════════════════════════════════════════════════════

    /**
     * FILE 노드에 파일명, 경로, 패키지명 설정.
     * 패키지명은 filePath의 디렉토리 부분에서 추출 (Java package 선언 등으로 덮어쓸 수 있음).
     */
    public void setFileInfo(String fileName, String filePath) {
        root.fileName = fileName;
        root.filePath = filePath;
        String normalized = filePath.replace("\\", "/");
        int lastSlash = normalized.lastIndexOf('/');
        String dir = (lastSlash > 0) ? normalized.substring(0, lastSlash).replace("/", ".") : "";
        while (dir.startsWith(".")) dir = dir.substring(1);
        root.packageName = "root" + (dir.isEmpty() ? "" : "." + dir);
    }

    // ═══════════════════════════════════════════════════════════════════
    // 유틸리티
    // ═══════════════════════════════════════════════════════════════════

    /**
     * 모듈명(클래스명, 구조체명 등)을 모든 자식 노드에 재귀 전파
     */
    public static void propagateModuleName(Node node, String moduleName) {
        for (Node child : node.children) {
            child.moduleName = moduleName;
            propagateModuleName(child, moduleName);
        }
    }
}
