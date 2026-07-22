package legacymodernizer.parser.recovery.quality;

import java.util.ArrayList;
import java.util.List;

import legacymodernizer.parser.recovery.diagnostics.DiagnosticPhase;
import legacymodernizer.parser.recovery.quality.QualityDecision;
import legacymodernizer.parser.recovery.quality.QualityStatus;
import legacymodernizer.parser.parsing.RawParseResult;
import org.springframework.stereotype.Component;

@Component
public final class ParseQualityGate {

    public QualityDecision evaluateFirstPass(RawParseResult parseAttempt) {
        int lexerErrors = (int) parseAttempt.diagnostics().stream()
                .filter(diagnostic -> diagnostic.phase() == DiagnosticPhase.LEXER).count();
        int parserErrors = (int) parseAttempt.diagnostics().stream()
                .filter(diagnostic -> diagnostic.phase() == DiagnosticPhase.PARSER).count();
        int missingDeclarations = parseAttempt.coverage().missingDeclarations().size();
        int unknownCoverage = parseAttempt.coverage().isKnownAndComplete() ? 0 : 1;

        List<String> reasons = new ArrayList<>();
        if (lexerErrors > 0) reasons.add("LEXER_ERRORS");
        if (parserErrors > 0) reasons.add("PARSER_ERRORS");
        if (parseAttempt.antlrRecoveries() > 0) reasons.add("ANTLR_RECOVERY");
        if (unknownCoverage > 0) reasons.add("COVERAGE_INCOMPLETE_OR_UNKNOWN");

        boolean exact = reasons.isEmpty();
        return new QualityDecision(
                exact ? QualityStatus.EXACT : QualityStatus.UNRESOLVED,
                exact,
                List.of(unknownCoverage, missingDeclarations, parserErrors, lexerErrors,
                        parseAttempt.antlrRecoveries(), 0, 0),
                List.copyOf(reasons));
    }
}
