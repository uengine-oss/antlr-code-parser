package legacymodernizer.parser.parsing.build;

/** One discovered compilation database, identified independently of host root paths. */
public record CompilationDatabaseOccurrence(
        String databaseId,
        String projectRelativePath,
        CompilationDatabaseSnapshot snapshot) {

    public CompilationDatabaseOccurrence {
        if (databaseId == null || !databaseId.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("databaseId must be a SHA-256 hex digest");
        }
        if (projectRelativePath == null || projectRelativePath.isBlank() || snapshot == null) {
            throw new IllegalArgumentException("database path and snapshot are required");
        }
    }
}
