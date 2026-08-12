package legacymodernizer.parser.antlr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import legacymodernizer.parser.antlr.plsql.PlSqlAstListener;
import legacymodernizer.parser.antlr.plsql.PlSqlLexer;
import legacymodernizer.parser.antlr.plsql.PlSqlParser;
import legacymodernizer.parser.model.Node;

/** spec 017: grammar-owned PL/SQL data-object and qualified-column evidence. */
class PlSqlDataObjectEvidenceContractTest {

    private static Node parse(String source) {
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

    private static void collect(Node node, String type, List<Node> out) {
        if (type.equals(node.type)) out.add(node);
        for (Node child : node.children) collect(child, type, out);
    }

    private static List<Node> all(Node root, String type) {
        List<Node> out = new ArrayList<>();
        collect(root, type, out);
        return out;
    }

    @Test
    void preservesSchemaLinkAliasAndQualifiedColumnOwnershipAtTheFirstWrongCoordinate() {
        Node root = parse("""
                CREATE OR REPLACE PROCEDURE P AS
                BEGIN
                  INSERT INTO APP.RESULT_T@WRITE_LINK (LOG_TIME, BNB_CODE, VAL)
                  SELECT B.LOG_TIME, A.BNB_CODE, SUM(B.VAL)
                    FROM APP.RDIB2EN_TB@READ_LINK A,
                         RDD01DD_TB B
                   WHERE A.SUB_TAGSN = B.TAGSN
                     AND A.TAG_GUBUN <> 'O'
                   GROUP BY B.LOG_TIME, A.BNB_CODE;
                END;
                /
                """);

        Node insert = all(root, "INSERT").get(0);
        Node select = all(root, "SELECT").get(0);
        String insertJson = insert.toJson();
        String selectJson = select.toJson();

        assertTrue(insertJson.contains("\"dataObjectEvidenceVersion\":1"));
        assertTrue(insertJson.contains("\"rawReference\":\"APP.RESULT_T@WRITE_LINK\""));
        assertTrue(insertJson.contains("\"schema\":\"APP\""));
        assertTrue(insertJson.contains("\"name\":\"RESULT_T\""));
        assertTrue(insertJson.contains("\"databaseLink\":\"WRITE_LINK\""));
        assertTrue(insertJson.contains("\"access\":\"WRITE\""));

        assertTrue(selectJson.contains("\"rawReference\":\"APP.RDIB2EN_TB@READ_LINK\""));
        assertTrue(selectJson.contains("\"alias\":\"A\""));
        assertTrue(selectJson.contains("\"name\":\"RDD01DD_TB\""));
        assertTrue(selectJson.contains("\"alias\":\"B\""));
        assertEquals(2, count(selectJson, "\"access\":\"READ\""));

        for (String ref : List.of(
                "A.BNB_CODE", "A.SUB_TAGSN", "A.TAG_GUBUN",
                "B.LOG_TIME", "B.VAL", "B.TAGSN")) {
            assertTrue(selectJson.contains("\"rawReference\":\"" + ref + "\""), ref);
        }
    }

    @Test
    void nestedQueriesOwnTheirTablesOnceAndPreserveCorrelatedQualifiedReferences() {
        Node root = parse("""
                CREATE OR REPLACE PROCEDURE P AS
                  V NUMBER;
                BEGIN
                  SELECT COUNT(*) INTO V
                    FROM OUTER_T O
                   WHERE EXISTS (
                     SELECT 1 FROM INNER_T I WHERE I.ID = O.ID
                   );
                END;
                /
                """);

        List<Node> selects = all(root, "SELECT");
        assertEquals(2, selects.size());
        selects.sort((left, right) -> Integer.compare(left.startLine, right.startLine));
        Node outer = selects.get(0);
        Node inner = selects.get(1);

        String innerJson = inner.toJson();
        assertEquals(List.of("OUTER_T"), outer.dataObjectReferences.stream().map(r -> r.name).toList());
        assertEquals(List.of("INNER_T"), inner.dataObjectReferences.stream().map(r -> r.name).toList());
        assertTrue(innerJson.contains("\"rawReference\":\"I.ID\""));
        assertTrue(innerJson.contains("\"rawReference\":\"O.ID\""));
    }

    @Test
    void sameLineNestedQueriesWithTheSameRangeKeepDistinctPhysicalObjects() {
        Node root = parse("""
                CREATE OR REPLACE PROCEDURE P AS
                  V NUMBER;
                BEGIN
                  SELECT COUNT(*) INTO V FROM (SELECT * FROM OUTER_T WHERE ID IN (SELECT ID FROM INNER_T));
                END;
                /
                """);

        List<String> names = all(root, "SELECT").stream()
                .flatMap(node -> node.dataObjectReferences == null
                        ? java.util.stream.Stream.empty()
                        : node.dataObjectReferences.stream())
                .map(reference -> reference.name)
                .toList();
        assertEquals(List.of("OUTER_T", "INNER_T"), names,
                "same-line parent/child SELECT must not dedupe away inner object evidence");
    }

    @Test
    void selfJoinKeepsAliasSpecificColumnsAndNestedUpdateCorrelation() {
        Node root = parse("""
                CREATE OR REPLACE PROCEDURE P AS
                BEGIN
                  SELECT L.ID, R.PARENT_ID
                    FROM TREE_T L JOIN TREE_T R ON L.ID = R.PARENT_ID;
                  UPDATE TARGET_T T
                     SET T.VALUE = (SELECT S.VALUE FROM SOURCE_T S WHERE S.ID = T.ID);
                END;
                /
                """);

        List<Node> selects = all(root, "SELECT");
        Node selfJoin = selects.stream()
                .filter(node -> node.startLine == 3)
                .findFirst().orElseThrow();
        assertEquals(
                List.of("TREE_T:L", "TREE_T:R"),
                selfJoin.dataObjectReferences.stream()
                        .map(reference -> reference.name + ":" + reference.alias)
                        .toList());
        assertTrue(selfJoin.toJson().contains("\"rawReference\":\"L.ID\""));
        assertTrue(selfJoin.toJson().contains("\"rawReference\":\"R.PARENT_ID\""));

        Node nested = selects.stream()
                .filter(node -> node.startLine == 6)
                .findFirst().orElseThrow();
        assertEquals(List.of("SOURCE_T"),
                nested.dataObjectReferences.stream().map(reference -> reference.name).toList());
        assertTrue(nested.toJson().contains("\"rawReference\":\"S.ID\""));
        assertTrue(nested.toJson().contains("\"rawReference\":\"T.ID\""));
    }

    @Test
    void quotedAliasesRemainCaseSensitiveGrammarFacts() {
        Node root = parse("""
                CREATE OR REPLACE PROCEDURE P AS
                  V NUMBER;
                BEGIN
                  SELECT "A".ID + "a".ID INTO V
                    FROM TREE_T "A" JOIN TREE_T "a" ON "A".ID = "a".ID;
                END;
                /
                """);

        Node select = all(root, "SELECT").get(0);
        assertEquals(List.of("\"A\"", "\"a\""),
                select.dataObjectReferences.stream().map(reference -> reference.alias).toList());
        assertTrue(select.toJson().contains("\"qualifier\":\"\\\"A\\\"\""));
        assertTrue(select.toJson().contains("\"qualifier\":\"\\\"a\\\"\""));
    }

    @Test
    void dmlTargetsAreWritesWithoutChangingNestedSelectReadOwnership() {
        Node root = parse("""
                CREATE OR REPLACE PROCEDURE P AS
                BEGIN
                  UPDATE APP.TARGET_T T SET T.VALUE = 1 WHERE T.ID = 1;
                  DELETE FROM APP.DELETE_T D WHERE D.ID = 1;
                  MERGE INTO APP.MERGE_T M
                  USING (SELECT S.ID FROM APP.SOURCE_T S) Q
                     ON (M.ID = Q.ID)
                  WHEN MATCHED THEN UPDATE SET M.VALUE = Q.ID;
                END;
                /
                """);

        for (String type : List.of("UPDATE", "DELETE", "MERGE")) {
            String json = all(root, type).get(0).toJson();
            assertTrue(json.contains("\"access\":\"WRITE\""), type);
        }
        String selectJson = all(root, "SELECT").get(0).toJson();
        assertTrue(selectJson.contains("\"name\":\"SOURCE_T\""));
        assertTrue(selectJson.contains("\"access\":\"READ\""));
    }

    @Test
    void mergeDirectUsingTableIsReadAndTargetIsWrite() {
        Node root = parse("""
                CREATE OR REPLACE PROCEDURE P AS
                BEGIN
                  MERGE INTO APP.MERGE_T M
                  USING APP.SOURCE_T S
                     ON (M.ID = S.ID)
                  WHEN MATCHED THEN UPDATE SET M.VALUE = S.VALUE;
                END;
                /
                """);

        Node merge = all(root, "MERGE").get(0);
        assertEquals(
                List.of("MERGE_T:WRITE:M", "SOURCE_T:READ:S"),
                merge.dataObjectReferences.stream()
                        .map(reference -> reference.name + ":" + reference.access + ":" + reference.alias)
                        .toList());
        assertTrue(merge.toJson().contains("\"rawReference\":\"S.VALUE\""));
    }

    @Test
    void mergeInlineTargetKeepsSinglePhysicalWriteAndDatabaseLink() {
        Node root = parse("""
                CREATE OR REPLACE PROCEDURE P AS
                BEGIN
                  MERGE INTO (
                    SELECT * FROM APP.TARGET_T@WRITE_LINK WHERE ACTIVE_YN = 'Y'
                  ) T
                  USING (SELECT S.ID FROM APP.SOURCE_T@READ_LINK S) Q
                     ON (T.ID = Q.ID)
                  WHEN MATCHED THEN UPDATE SET T.VALUE = Q.ID;
                END;
                /
                """);

        Node merge = all(root, "MERGE").get(0);
        assertEquals(
                List.of("TARGET_T:WRITE:T"),
                merge.dataObjectReferences.stream()
                        .map(reference -> reference.name + ":" + reference.access + ":" + reference.alias)
                        .toList());
        assertTrue(merge.toJson().contains("\"databaseLink\":\"WRITE_LINK\""));

        List<String> selectObjects = all(root, "SELECT").stream()
                .flatMap(node -> node.dataObjectReferences == null
                        ? java.util.stream.Stream.empty()
                        : node.dataObjectReferences.stream())
                .map(reference -> reference.name + ":" + reference.access + ":" + reference.databaseLink)
                .toList();
        assertEquals(
                List.of("TARGET_T:READ:WRITE_LINK", "SOURCE_T:READ:READ_LINK"),
                selectObjects);
    }

    @Test
    void mergeInlineTargetDoesNotGuessWriteObjectForMultiTableView() {
        Node root = parse("""
                CREATE OR REPLACE PROCEDURE P AS
                BEGIN
                  MERGE INTO (
                    SELECT A.ID, A.VALUE, B.FLAG
                      FROM TARGET_A A JOIN TARGET_B B ON A.ID = B.ID
                  ) T
                  USING SOURCE_T S
                     ON (T.ID = S.ID)
                  WHEN MATCHED THEN UPDATE SET T.VALUE = S.VALUE;
                END;
                /
                """);

        Node merge = all(root, "MERGE").get(0);
        assertFalse(merge.dataObjectReferences.stream()
                        .anyMatch(reference -> "WRITE".equals(reference.access)),
                "a multi-table inline target needs key-preservation semantics; parser must not guess");
        assertEquals(List.of("SOURCE_T:READ"), merge.dataObjectReferences.stream()
                        .map(reference -> reference.name + ":" + reference.access)
                        .toList());
        assertEquals(
                List.of("TARGET_A", "TARGET_B"),
                all(root, "SELECT").get(0).dataObjectReferences.stream()
                        .map(reference -> reference.name)
                        .toList());
    }

    private static int count(String text, String needle) {
        int count = 0;
        for (int at = 0; (at = text.indexOf(needle, at)) >= 0; at += needle.length()) count++;
        return count;
    }
}
