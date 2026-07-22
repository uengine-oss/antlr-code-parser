package legacymodernizer.parser.parsing;

import legacymodernizer.parser.model.Node;
import legacymodernizer.parser.recovery.diagnostics.ParseDiagnostic;

public final class AstCoordinates {

    private AstCoordinates() {
    }

    public static ParseDiagnostic rebase(ParseDiagnostic diagnostic, int lineOffset) {
        if (lineOffset == 0) return diagnostic;
        return new ParseDiagnostic(diagnostic.phase(), diagnostic.severity(), diagnostic.code(),
                diagnostic.message(), diagnostic.line() + lineOffset, diagnostic.column(),
                diagnostic.offendingToken(), diagnostic.expectedTokens(), diagnostic.ruleStack(),
                diagnostic.tokenWindow());
    }

    public static void rebaseChildren(Node root, int lineOffset) {
        if (lineOffset == 0 || root == null) return;
        root.children.forEach(child -> rebaseNode(child, lineOffset));
    }

    private static void rebaseNode(Node node, int lineOffset) {
        if (node.startLine > 0) node.startLine += lineOffset;
        if (node.endLine > 0) node.endLine += lineOffset;
        node.children.forEach(child -> rebaseNode(child, lineOffset));
    }
}
