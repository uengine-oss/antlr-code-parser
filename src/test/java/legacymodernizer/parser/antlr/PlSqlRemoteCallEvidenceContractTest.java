package legacymodernizer.parser.antlr;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import legacymodernizer.parser.antlr.plsql.PlSqlAstListener;
import legacymodernizer.parser.antlr.plsql.PlSqlLexer;
import legacymodernizer.parser.antlr.plsql.PlSqlParser;
import legacymodernizer.parser.model.Node;
import legacymodernizer.parser.parsing.evidence.CallEvidenceCandidate;
import legacymodernizer.parser.parsing.evidence.SourceRangeCandidate;

/** spec 019: explicit remote function calls are CALLs; remote sequences are not. */
class PlSqlRemoteCallEvidenceContractTest {

    private record ParsedRemoteCalls(Node root, List<CallEvidenceCandidate> calls) {
    }

    private static ParsedRemoteCalls parse(String source) {
        PlSqlLexer lexer = new PlSqlLexer(
                new CaseChangingCharStream(CharStreams.fromString(source), true));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PlSqlParser parser = new PlSqlParser(tokens);
        PlSqlParser.Sql_scriptContext tree = parser.sql_script();
        PlSqlAstListener listener = new PlSqlAstListener(tokens, null);
        listener.setFileInfo("sample.sql", "sample.sql");
        new ParseTreeWalker().walk(listener, tree);
        listener.finalizeAst();
        return new ParsedRemoteCalls(
                listener.getRoot(), listener.callEvidenceCandidates());
    }

    private static void collect(Node node, String type, List<Node> out) {
        if (type.equals(node.type)) out.add(node);
        for (Node child : node.children) collect(child, type, out);
    }

    @Test
    void remoteFunctionsAreCallsButRemoteNextvalReferencesAreNot() {
        String source = """
                CREATE OR REPLACE PROCEDURE P AS
                  V NUMBER;
                BEGIN
                  SELECT APP.PKG.FN@REMOTE_DB(ID), FN2@REMOTE_DB(ID)
                    INTO V
                    FROM SOURCE_T;
                  INSERT INTO TARGET_T(ID) VALUES (APP.SEQ_A.NEXTVAL@REMOTE_DB);
                  V := SEQ_B.NEXTVAL@REMOTE_DB;
                END;
                /
                """;
        ParsedRemoteCalls parsed = parse(source);

        List<Node> calls = new ArrayList<>();
        collect(parsed.root(), "CALL", calls);
        assertEquals(List.of("FN", "FN2"),
                calls.stream().map(node -> node.name).toList());
        assertEquals(List.of(4, 4), calls.stream().map(node -> node.startLine).toList());

        assertEquals(List.of("FN", "FN2"),
                parsed.calls().stream().map(CallEvidenceCandidate::terminalName).toList());
        assertEquals(List.of(
                        List.of("APP", "PKG", "FN"),
                        List.of("FN2")),
                parsed.calls().stream()
                        .map(call -> slices(source, call.calleePathRanges()))
                        .toList());
        assertEquals(List.of("REMOTE_DB", "REMOTE_DB"),
                parsed.calls().stream()
                        .map(call -> slice(source, call.databaseLinkRange()))
                        .toList());
        assertEquals(List.of("APP.PKG.FN@REMOTE_DB", "FN2@REMOTE_DB"),
                parsed.calls().stream()
                        .map(call -> slice(source, call.calleeRange()))
                        .toList());
    }

    private static List<String> slices(
            String source, List<SourceRangeCandidate> ranges) {
        return ranges.stream().map(range -> slice(source, range)).toList();
    }

    private static String slice(String source, SourceRangeCandidate range) {
        return source.substring(range.startOffset(), range.endOffset());
    }
}
