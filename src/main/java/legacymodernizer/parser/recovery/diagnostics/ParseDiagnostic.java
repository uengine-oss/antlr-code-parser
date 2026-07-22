package legacymodernizer.parser.recovery.diagnostics;

import java.util.List;

public record ParseDiagnostic(
        DiagnosticPhase phase,
        String severity,
        String code,
        String message,
        int line,
        int column,
        String offendingToken,
        String expectedTokens,
        List<String> ruleStack,
        String tokenWindow) {
}
