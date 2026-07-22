package legacymodernizer.parser.parsing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import legacymodernizer.parser.recovery.diagnostics.ParseDiagnostic;
import legacymodernizer.parser.recovery.quality.DeclarationCoverage;

public record RawParseResult(
        String language,
        String grammarRevision,
        String entryRule,
        String sourceSha256,
        String astJson,
        List<ParseDiagnostic> diagnostics,
        int antlrRecoveries,
        DeclarationCoverage coverage,
        long elapsedMillis) {

    public RawParseResult {
        List<ParseDiagnostic> ordered = new ArrayList<>(diagnostics == null ? List.of() : diagnostics);
        ordered.sort(Comparator.comparingInt(ParseDiagnostic::line)
                .thenComparingInt(ParseDiagnostic::column)
                .thenComparing(ParseDiagnostic::phase)
                .thenComparing(ParseDiagnostic::code));
        diagnostics = List.copyOf(ordered);
        coverage = coverage == null ? DeclarationCoverage.unknown() : coverage;
    }
}
