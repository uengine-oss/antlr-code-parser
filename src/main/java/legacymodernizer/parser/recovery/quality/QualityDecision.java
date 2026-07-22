package legacymodernizer.parser.recovery.quality;

import java.util.List;

public record QualityDecision(
        QualityStatus status,
        boolean accepted,
        List<Integer> qualityTuple,
        List<String> reasons) {
}
