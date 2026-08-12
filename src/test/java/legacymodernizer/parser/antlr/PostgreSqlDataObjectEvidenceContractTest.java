package legacymodernizer.parser.antlr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import legacymodernizer.parser.antlr.postgresql.PostgreSQLLexer;
import legacymodernizer.parser.antlr.postgresql.PostgreSQLParser;
import legacymodernizer.parser.antlr.postgresql.PostgreSqlAstListener;
import legacymodernizer.parser.model.Node;

/** spec 127: PostgreSQL must provide the same data-object evidence class as PL/SQL. */
class PostgreSqlDataObjectEvidenceContractTest {

    private static Node parse(String source) {
        PostgreSQLLexer lexer = new PostgreSQLLexer(CharStreams.fromString(source));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PostgreSQLParser parser = new PostgreSQLParser(tokens);
        PostgreSQLParser.RootContext tree = parser.root();
        PostgreSqlAstListener listener = new PostgreSqlAstListener(tokens, null);
        listener.setFileInfo("sample.sql", "sample.sql");
        new ParseTreeWalker().walk(listener, tree);
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
    void selectOwnsPhysicalTablesAliasesColumnsAndNotCteNames() {
        Node root = parse("""
                WITH recent AS (
                  SELECT o.id, o.member_id FROM app.orders o WHERE o.status = 'PAID'
                )
                SELECT r.id, m.grade
                  FROM recent r JOIN app.member m ON m.id = r.member_id;
                """);

        List<Node> selects = all(root, "SELECT");
        assertEquals(2, selects.size());
        Node inner = selects.stream().filter(node -> node.dataObjectReferences.stream()
                .anyMatch(reference -> "orders".equals(reference.name))).findFirst().orElseThrow();
        Node outer = selects.stream().filter(node -> node.dataObjectReferences.stream()
                .anyMatch(reference -> "member".equals(reference.name))).findFirst().orElseThrow();

        assertEquals(List.of("orders:o"), inner.dataObjectReferences.stream()
                .map(reference -> reference.name + ":" + reference.alias).toList());
        assertEquals(List.of("member:m"), outer.dataObjectReferences.stream()
                .map(reference -> reference.name + ":" + reference.alias).toList());
        assertTrue(inner.toJson().contains("\"rawReference\":\"o.member_id\""));
        assertTrue(outer.toJson().contains("\"rawReference\":\"m.grade\""));
    }

    @Test
    void insertSelectKeepsWriteTargetAndNestedSelectRead() {
        Node root = parse("""
                INSERT INTO audit.result_t (id, amount)
                SELECT o.id, p.amount
                  FROM app.orders o JOIN payment p ON p.order_id = o.id;
                """);

        Node insert = all(root, "INSERT").get(0);
        Node select = all(root, "SELECT").get(0);
        assertEquals(List.of("result_t:WRITE"), insert.dataObjectReferences.stream()
                .map(reference -> reference.name + ":" + reference.access).toList());
        assertEquals(List.of("orders:READ", "payment:READ"), select.dataObjectReferences.stream()
                .map(reference -> reference.name + ":" + reference.access).toList());
    }

    @Test
    void updateFromDeleteUsingAndMergeSeparateReadFromWrite() {
        Node root = parse("""
                UPDATE app.target t SET value = s.value
                  FROM app.source s WHERE s.id = t.id;
                DELETE FROM app.target t USING app.source s WHERE s.id = t.id;
                MERGE INTO app.target t USING app.source s ON t.id = s.id
                  WHEN MATCHED THEN UPDATE SET value = s.value
                  WHEN NOT MATCHED THEN INSERT (id, value) VALUES (s.id, s.value);
                """);

        for (String type : List.of("UPDATE", "DELETE", "MERGE")) {
            Node node = all(root, type).get(0);
            assertEquals(List.of("target:WRITE", "source:READ"), node.dataObjectReferences.stream()
                    .map(reference -> reference.name + ":" + reference.access).toList(), type);
            assertTrue(node.toJson().contains("\"rawReference\":\"s.id\""), type);
            assertTrue(node.toJson().contains("\"rawReference\":\"t.id\""), type);
        }
    }

    @Test
    void quotedAliasesRemainDistinctAndCorrelatedOuterTargetIsPreserved() {
        Node quotedRoot = parse("""
                SELECT "A".id, "a".id
                  FROM tree_t "A" JOIN tree_t "a" ON "A".id = "a".id;
                """);

        Node selfJoin = all(quotedRoot, "SELECT").stream()
                .filter(node -> node.dataObjectReferences.size() == 2
                        && node.dataObjectReferences.stream()
                                .allMatch(reference -> "tree_t".equals(reference.name)))
                .findFirst().orElseThrow();
        assertEquals(List.of("\"A\"", "\"a\""), selfJoin.dataObjectReferences.stream()
                .map(reference -> reference.alias).toList());
        assertTrue(selfJoin.toJson().contains("\"qualifier\":\"\\\"A\\\"\""));
        assertTrue(selfJoin.toJson().contains("\"qualifier\":\"\\\"a\\\"\""));
        Node quotedObject = all(parse("SELECT q.id FROM \"CaseTable\" q;"), "SELECT").get(0);
        assertTrue(quotedObject.toJson().contains("\"name\":\"\\\"CaseTable\\\"\""));
        assertTrue(quotedObject.toJson().contains("\"nameQuoted\":true"));

        Node correlatedRoot = parse("""
                UPDATE target_t t SET value = 1
                 WHERE EXISTS (SELECT 1 FROM source_t s WHERE s.id = t.id);
                """);
        Node nested = all(correlatedRoot, "SELECT").stream()
                .filter(node -> node.dataObjectReferences.stream()
                        .anyMatch(reference -> "source_t".equals(reference.name)))
                .findFirst().orElseThrow();
        assertTrue(nested.toJson().contains("\"rawReference\":\"s.id\""));
        assertTrue(nested.toJson().contains("\"rawReference\":\"t.id\""));
    }
}
