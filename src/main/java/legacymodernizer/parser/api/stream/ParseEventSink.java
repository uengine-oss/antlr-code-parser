package legacymodernizer.parser.api.stream;

/** Parser workflow boundary for emitting typed progress events. */
@FunctionalInterface
public interface ParseEventSink {

    void emit(ParseStreamEvent event);

    default void message(String event, String content, String phase, String status) {
        emit(ParseStreamEvent.builder("message", event)
                .content(content).phase(phase).status(status).build());
    }

    default void warning(String event, String content, String phase) {
        emit(ParseStreamEvent.builder("warning", event)
                .content(content).phase(phase).status("WARNING").build());
    }

    default void error(String event, String content, String phase) {
        emit(ParseStreamEvent.builder("error", event)
                .content(content).phase(phase).status("FAILED").build());
    }
}

