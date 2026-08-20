package legacymodernizer.parser.parsing.build;

import java.nio.file.Path;
import java.util.List;

/** Exact accounting for one compilation-database input. */
public record CompilationDatabaseSnapshot(
        String version,
        Path databasePath,
        String rawSha256,
        CompilationDatabaseStatus status,
        int population,
        int emitted,
        int explicitlyUnresolved,
        List<CompilationCommand> commands,
        List<String> unresolvedReasons) {

    public CompilationDatabaseSnapshot {
        if (!"1.0.0".equals(version)) {
            throw new IllegalArgumentException("unsupported compilation database snapshot version");
        }
        if (databasePath == null || status == null) {
            throw new IllegalArgumentException("databasePath and status are required");
        }
        commands = List.copyOf(commands == null ? List.of() : commands);
        unresolvedReasons = List.copyOf(unresolvedReasons == null ? List.of() : unresolvedReasons);
        if (population < 0 || emitted < 0 || explicitlyUnresolved < 0
                || population != emitted + explicitlyUnresolved) {
            throw new IllegalArgumentException(
                    "population must equal emitted + explicitlyUnresolved");
        }
        long emittedCommands = commands.stream().filter(CompilationCommand::emitted).count();
        long unresolvedCommands = commands.size() - emittedCommands;
        if (!commands.isEmpty()
                && (emitted != emittedCommands || explicitlyUnresolved != unresolvedCommands)) {
            throw new IllegalArgumentException("command accounting does not match snapshot totals");
        }
        if (status == CompilationDatabaseStatus.EXACT
                && (explicitlyUnresolved != 0 || !unresolvedReasons.isEmpty())) {
            throw new IllegalArgumentException("exact snapshot cannot contain unresolved evidence");
        }
    }
}
