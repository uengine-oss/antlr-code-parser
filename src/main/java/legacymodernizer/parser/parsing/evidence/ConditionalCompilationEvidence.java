package legacymodernizer.parser.parsing.evidence;

import java.util.Comparator;
import java.util.List;

/** Presence map and exact directive-group regions supplied by a language frontend. */
public record ConditionalCompilationEvidence(
        List<PresenceSpan> spans,
        List<ConditionalRegionCandidate> regions) {

    public static final ConditionalCompilationEvidence NONE =
            new ConditionalCompilationEvidence(List.of(), List.of());

    public ConditionalCompilationEvidence {
        spans = sortedCopy(spans);
        regions = List.copyOf(regions == null ? List.of() : regions);
    }

    public Presence presenceAt(int codePointOffset) {
        int low = 0;
        int high = spans.size() - 1;
        while (low <= high) {
            int middle = (low + high) >>> 1;
            PresenceSpan span = spans.get(middle);
            if (codePointOffset < span.range().startOffset()) high = middle - 1;
            else if (codePointOffset >= span.range().endOffset()) low = middle + 1;
            else return span.presence();
        }
        return Presence.active();
    }

    public long unresolvedRegionCount() {
        return regions.stream().filter(region -> List.of("conditional", "unknown")
                        .contains(region.presence().status()))
                .count();
    }

    private static List<PresenceSpan> sortedCopy(List<PresenceSpan> source) {
        return (source == null ? List.<PresenceSpan>of() : source).stream()
                .sorted(Comparator.comparingInt(span -> span.range().startOffset()))
                .toList();
    }

    public record Presence(String status, String condition, String provenance) {
        public Presence {
            if (!List.of("active", "inactive", "conditional", "unknown").contains(status)) {
                throw new IllegalArgumentException("invalid presence status: " + status);
            }
        }

        public static Presence active() {
            return new Presence("active", null, "unconditional");
        }
    }

    public record PresenceSpan(SourceRangeCandidate range, Presence presence) {
    }

    public record ConditionalRegionCandidate(
            SourceRangeCandidate range,
            String condition,
            Presence presence) {
    }
}
