package legacymodernizer.parser.parsing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;

import legacymodernizer.parser.model.IdentifierReference;
import legacymodernizer.parser.model.Node;

/** Projects lexer-confirmed identifier occurrences onto every containing AST node. */
public final class IdentifierReferenceProjector {

    private static final Set<String> IDENTIFIER_TOKENS = Set.of(
            "Identifier", "NAME", "REGULAR_ID", "PLSQLIDENTIFIER");

    private IdentifierReferenceProjector() {
    }

    public static void attach(Node root, CommonTokenStream tokens, Vocabulary vocabulary) {
        if (root == null || tokens == null || vocabulary == null) return;
        tokens.fill();
        List<Node> nodes = new ArrayList<>();
        collect(root, nodes);
        nodes.forEach(node -> node.identifierReferenceVersion = 1);
        nodes.sort(Comparator.comparingInt(node -> node.startLine));

        Map<Node, LinkedHashMap<String, IdentifierReference>> references =
                new LinkedHashMap<>();
        List<Node> active = new ArrayList<>();
        int nextNode = 0;
        int currentLine = -1;
        for (Token token : tokens.getTokens()) {
            if (token.getType() == Token.EOF
                    || token.getChannel() != Token.DEFAULT_CHANNEL
                    || !isIdentifier(token, vocabulary)) {
                continue;
            }
            int line = token.getLine();
            if (line <= 0) continue;
            if (line != currentLine) {
                while (nextNode < nodes.size() && nodes.get(nextNode).startLine <= line) {
                    active.add(nodes.get(nextNode++));
                }
                active.removeIf(node -> node.endLine < line);
                currentLine = line;
            }
            String name = token.getText();
            if (name == null || name.isBlank()) continue;
            for (Node node : active) {
                if (line > node.endLine) continue;
                references.computeIfAbsent(node, ignored -> new LinkedHashMap<>())
                        .putIfAbsent(line + "\0" + name, new IdentifierReference(name, line));
            }
        }
        references.forEach((node, values) ->
                node.identifierReferences = new ArrayList<>(values.values()));
    }

    private static boolean isIdentifier(Token token, Vocabulary vocabulary) {
        String symbolicName = vocabulary.getSymbolicName(token.getType());
        return symbolicName != null && IDENTIFIER_TOKENS.contains(symbolicName);
    }

    private static void collect(Node node, List<Node> output) {
        if (node.startLine > 0 && node.endLine >= node.startLine) output.add(node);
        for (Node child : node.children) collect(child, output);
    }
}
