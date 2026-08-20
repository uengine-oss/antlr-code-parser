package legacymodernizer.parser.parsing.build;

/** Exact command occurrence scoped by its project compilation database. */
public record CompilationCommandOccurrence(
        String occurrenceId,
        String databaseId,
        CompilationCommand command) {

    public CompilationCommandOccurrence {
        if (occurrenceId == null || !occurrenceId.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("occurrenceId must be a SHA-256 hex digest");
        }
        if (databaseId == null || !databaseId.matches("[0-9a-f]{64}") || command == null) {
            throw new IllegalArgumentException("databaseId and command are required");
        }
    }
}
