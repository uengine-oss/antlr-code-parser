package legacymodernizer.parser.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.junit.jupiter.api.Test;

import legacymodernizer.parser.antlr.c.CLexer;
import legacymodernizer.parser.antlr.java.Java20Lexer;
import legacymodernizer.parser.antlr.plsql.PlSqlLexer;
import legacymodernizer.parser.antlr.postgresql.PostgreSQLLexer;
import legacymodernizer.parser.antlr.python.PythonLexer;
import legacymodernizer.parser.model.Node;

class IdentifierReferenceProjectorTest {

    @Test
    void commonFrontendsProjectOnlyLexerIdentifiers() {
        assertNames(new CLexer(CharStreams.fromString("foo = bar; /* ghost */")));
        assertNames(new Java20Lexer(CharStreams.fromString("foo = bar; // ghost")));
        assertNames(new PythonLexer(CharStreams.fromString("foo = bar # ghost\n")));
        assertNames(new PlSqlLexer(CharStreams.fromString("foo := bar; -- ghost\n")));
        assertNames(new PostgreSQLLexer(CharStreams.fromString("foo := bar; -- ghost\n")));
    }

    @Test
    void unitLineOffsetRebasesOccurrencesWithTheirNode() {
        Node root = new Node("FILE", 0, null);
        Node routine = new Node("FUNCTION", "run", 1, root);
        routine.endLine = 1;
        Lexer lexer = new CLexer(CharStreams.fromString("foo = bar;"));
        IdentifierReferenceProjector.attach(
                root, new CommonTokenStream(lexer), lexer.getVocabulary());

        AstCoordinates.rebaseChildren(root, 12);

        assertEquals(13, routine.startLine);
        assertEquals(List.of(13, 13), routine.identifierReferences.stream()
                .map(reference -> reference.line()).toList());
    }

    private static void assertNames(Lexer lexer) {
        Node root = new Node("FILE", 0, null);
        Node routine = new Node("FUNCTION", "run", 1, root);
        routine.endLine = 1;
        IdentifierReferenceProjector.attach(
                root, new CommonTokenStream(lexer), lexer.getVocabulary());

        assertEquals(List.of("foo", "bar"), routine.identifierReferences.stream()
                .map(reference -> reference.name()).toList());
    }
}
