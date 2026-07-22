package legacymodernizer.parser.recovery.repair;

import java.util.List;

public record CoverageEvidence(
        int declarationsDiscovered,
        int declarationsEmitted,
        List<String> missingDeclarations,
        boolean knownAndComplete) {
}
