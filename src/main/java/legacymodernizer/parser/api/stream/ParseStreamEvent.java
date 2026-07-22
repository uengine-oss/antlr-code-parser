package legacymodernizer.parser.api.stream;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * One additive, backwards-compatible NDJSON event emitted by the Parser API.
 *
 * <p>{@code type} and {@code content} are the legacy wire contract. The remaining fields let a
 * UI render progress without reverse-engineering Korean display text.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "schemaVersion", "type", "event", "content", "phase", "status",
        "current", "total", "percent", "file", "language", "line", "quality", "counts"
})
public record ParseStreamEvent(
        String schemaVersion,
        String type,
        String event,
        String content,
        String phase,
        String status,
        Integer current,
        Integer total,
        Integer percent,
        String file,
        String language,
        Integer line,
        String quality,
        Map<String, Integer> counts) {

    public static final String SCHEMA_VERSION = "1.1.0";

    public ParseStreamEvent {
        if (type == null || type.isBlank()) throw new IllegalArgumentException("type is required");
        counts = counts == null ? null
                : Collections.unmodifiableMap(new LinkedHashMap<>(counts));
    }

    public static Builder builder(String type, String event) {
        return new Builder(type, event);
    }

    public static ParseStreamEvent complete() {
        return builder("complete", "complete")
                .phase("COMPLETED").status("COMPLETED").percent(100).build();
    }

    public static final class Builder {
        private final String type;
        private final String event;
        private String content;
        private String phase;
        private String status;
        private Integer current;
        private Integer total;
        private Integer percent;
        private String file;
        private String language;
        private Integer line;
        private String quality;
        private Map<String, Integer> counts;

        private Builder(String type, String event) {
            this.type = type;
            this.event = event;
        }

        public Builder content(String value) { content = value; return this; }
        public Builder phase(String value) { phase = value; return this; }
        public Builder status(String value) { status = value; return this; }
        public Builder current(Integer value) { current = value; return this; }
        public Builder total(Integer value) { total = value; return this; }
        public Builder percent(Integer value) {
            percent = value == null ? null : Math.max(0, Math.min(100, value));
            return this;
        }
        public Builder file(String value) { file = value; return this; }
        public Builder language(String value) { language = value; return this; }
        public Builder line(Integer value) { line = value; return this; }
        public Builder quality(String value) { quality = value; return this; }
        public Builder counts(Map<String, Integer> value) { counts = value; return this; }

        public ParseStreamEvent build() {
            return new ParseStreamEvent(SCHEMA_VERSION, type, event, content, phase, status,
                    current, total, percent, file, language, line, quality, counts);
        }
    }
}

