package legacymodernizer.parser.recovery.evidence;

/**
 * Per-Agent-request transfer evidence (spec 012 FR-025): proves how little source was sent.
 * {@code promptTokens} is null when the provider does not report usage.
 */
public record AgentRequestEvidence(
        String sliceLevel,
        int sliceChars,
        int unitChars,
        double transferRatio,
        Integer promptTokens) {

    public static AgentRequestEvidence of(String sliceLevel, int sliceChars, int unitChars,
                                          Integer promptTokens) {
        double ratio = unitChars <= 0 ? 1.0
                : Math.round((double) sliceChars / unitChars * 10_000) / 10_000.0;
        return new AgentRequestEvidence(sliceLevel, sliceChars, unitChars, ratio, promptTokens);
    }
}
