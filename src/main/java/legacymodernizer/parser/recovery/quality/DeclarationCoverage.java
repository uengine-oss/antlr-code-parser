package legacymodernizer.parser.recovery.quality;

import java.util.List;
import java.util.Map;

public record DeclarationCoverage(
        int declarationsDiscovered,
        int declarationsEmitted,
        Map<String, Integer> discoveredByKind,
        Map<String, Integer> emittedByKind,
        List<String> missingDeclarations) {

    public static DeclarationCoverage unknown() {
        return new DeclarationCoverage(-1, -1, Map.of(), Map.of(), List.of());
    }

    public boolean isKnownAndComplete() {
        return declarationsDiscovered >= 0
                && declarationsEmitted >= declarationsDiscovered
                && missingDeclarations.isEmpty();
    }
}
