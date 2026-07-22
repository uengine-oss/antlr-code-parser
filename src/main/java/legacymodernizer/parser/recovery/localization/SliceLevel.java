package legacymodernizer.parser.recovery.localization;

/**
 * Deterministic context-expansion ladder. The Parser walks up one level between repair
 * retries; the Agent has no authority to widen a slice. Past L3 automatic repair stops
 * (treated as L4 → REVIEW_REQUIRED by the caller).
 */
public enum SliceLevel {
    /** Diagnostic line ± one line. */
    L0(240),
    /** Enclosing statement bounded by in-code statement terminators, bracket-balanced. */
    L1(800),
    /** L1 span widened slightly plus the read-only unit declaration header. */
    L2(1_600),
    /** Anchor-centred window capped by budget, plus the read-only header. */
    L3(4_000);

    private final int maxChars;

    SliceLevel(int maxChars) {
        this.maxChars = maxChars;
    }

    public int maxChars() {
        return maxChars;
    }

    public SliceLevel next() {
        return switch (this) {
            case L0 -> L1;
            case L1 -> L2;
            case L2 -> L3;
            case L3 -> null;
        };
    }
}
