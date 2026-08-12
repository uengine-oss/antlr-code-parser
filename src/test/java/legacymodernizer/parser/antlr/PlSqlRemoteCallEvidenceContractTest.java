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

/** spec 019: explicit remote function calls are CALLs; remote sequences are not. */
class PlSqlRemoteCallEvidenceContractTest {

    private static Node parse(String source) {
        PlSqlLexer lexer = new PlSqlLexer(
                new CaseChangingCharStream(CharStreams.fromString(source), true));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PlSqlParser parser = new PlSqlParser(tokens);
        PlSqlParser.Sql_scriptContext tree = parser.sql_script();
        PlSqlAstListener listener = new PlSqlAstListener(tokens, null);
        listener.setFileInfo("sample.sql", "sample.sql");
        new ParseTreeWalker().walk(listener, tree);
        listener.finalizeAst();
        return listener.getRoot();
    }

    private static void collect(Node node, String type, List<Node> out) {
        if (type.equals(node.type)) out.add(node);
        for (Node child : node.children) collect(child, type, out);
    }

    @Test
    void remoteFunctionsAreCallsButRemoteNextvalReferencesAreNot() {
        Node root = parse("""
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
                """);

        List<Node> calls = new ArrayList<>();
        collect(root, "CALL", calls);
        assertEquals(List.of("APP.PKG.FN@REMOTE_DB", "FN2@REMOTE_DB"),
                calls.stream().map(node -> node.name).toList());
        assertEquals(List.of(4, 4), calls.stream().map(node -> node.startLine).toList());
    }
}

