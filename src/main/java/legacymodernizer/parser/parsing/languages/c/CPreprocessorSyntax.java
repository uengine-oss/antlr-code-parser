package legacymodernizer.parser.parsing.languages.c;

import java.util.List;

import legacymodernizer.parser.parsing.evidence.ConditionalCompilationEvidence;
import legacymodernizer.parser.parsing.evidence.ImportEvidenceExtraction;
import legacymodernizer.parser.parsing.evidence.MacroEvidenceExtraction;
import legacymodernizer.parser.parsing.evidence.SourceRangeCandidate;

/** One grammar-owned extraction shared by the C parser, AST adapter, and evidence sealer. */
public record CPreprocessorSyntax(
        MacroEvidenceExtraction macros,
        ImportEvidenceExtraction imports,
        ConditionalCompilationEvidence conditional,
        List<SourceRangeCandidate> directiveRanges) {

    public CPreprocessorSyntax {
        if (macros == null || imports == null || conditional == null) {
            throw new IllegalArgumentException("preprocessor extraction fields are required");
        }
        directiveRanges = List.copyOf(directiveRanges == null ? List.of() : directiveRanges);
    }

    /** Replace grammar-owned directives with same-code-point-count whitespace for main C parsing. */
    public String maskDirectives(String source) {
        int[] codePoints = (source == null ? "" : source).codePoints().toArray();
        for (SourceRangeCandidate range : directiveRanges) {
            for (int index = range.startOffset(); index < range.endOffset(); index++) {
                if (codePoints[index] != '\r' && codePoints[index] != '\n') codePoints[index] = ' ';
            }
        }
        return new String(codePoints, 0, codePoints.length);
    }
}
