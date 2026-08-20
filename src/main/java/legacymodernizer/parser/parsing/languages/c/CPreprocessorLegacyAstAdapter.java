package legacymodernizer.parser.parsing.languages.c;

import legacymodernizer.parser.model.Node;
import legacymodernizer.parser.parsing.evidence.MacroEvidenceCandidate;
import legacymodernizer.parser.parsing.evidence.MacroEvidenceExtraction;
import legacymodernizer.parser.parsing.evidence.SourceRangeCandidate;

/**
 * Preserves the legacy DEFINE projection from grammar-owned macro evidence.
 * This adapter never discovers syntax from source text.
 */
public final class CPreprocessorLegacyAstAdapter {

    private CPreprocessorLegacyAstAdapter() {
    }

    public static void appendDefines(Node root, String source,
                                     MacroEvidenceExtraction extraction) {
        int[] codePoints = source.codePoints().toArray();
        int[] sourceLines = lineIndex(codePoints);
        for (MacroEvidenceCandidate macro : extraction.candidates()) {
            if (!"object".equals(macro.macroKind())
                    || macro.replacementRange() == null
                    || !isLegacyConstantName(macro.terminalName())) {
                continue;
            }
            int startLine = sourceLines[macro.range().startOffset()];
            int endLine = sourceLines[Math.max(
                    macro.range().startOffset(), macro.range().endOffset() - 1)];
            Node node = new Node("DEFINE", macro.terminalName(), startLine, root);
            node.endLine = endLine;
            node.initValue = slice(codePoints, macro.replacementRange());
        }
    }

    private static boolean isLegacyConstantName(String name) {
        if (name.isEmpty() || name.charAt(0) != '_'
                && (name.charAt(0) < 'A' || name.charAt(0) > 'Z')) {
            return false;
        }
        for (int index = 1; index < name.length(); index++) {
            char value = name.charAt(index);
            if (value != '_' && (value < 'A' || value > 'Z')
                    && (value < '0' || value > '9')) {
                return false;
            }
        }
        return true;
    }

    private static int[] lineIndex(int[] source) {
        int[] lines = new int[source.length + 1];
        int line = 1;
        for (int index = 0; index < source.length; index++) {
            lines[index] = line;
            if (source[index] == '\n') {
                line++;
            } else if (source[index] == '\r'
                    && (index + 1 >= source.length || source[index + 1] != '\n')) {
                line++;
            }
        }
        lines[source.length] = line;
        return lines;
    }

    private static String slice(int[] source, SourceRangeCandidate range) {
        return new String(source, range.startOffset(), range.endOffset() - range.startOffset());
    }
}
