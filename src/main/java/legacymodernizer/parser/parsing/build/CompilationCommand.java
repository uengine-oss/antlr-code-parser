package legacymodernizer.parser.parsing.build;

import java.nio.file.Path;
import java.util.List;

/** Immutable command object; literals and resolved paths are deliberately kept separate. */
public record CompilationCommand(
        String commandObjectId,
        String directoryLiteral,
        String fileLiteral,
        Path directory,
        Path file,
        List<String> arguments,
        String command,
        String output,
        CompilationCommandCapability capability,
        List<String> unresolvedReasons) {

    public CompilationCommand {
        if (commandObjectId == null || !commandObjectId.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("commandObjectId must be a SHA-256 hex digest");
        }
        arguments = List.copyOf(arguments == null ? List.of() : arguments);
        unresolvedReasons = List.copyOf(unresolvedReasons == null ? List.of() : unresolvedReasons);
        if (capability == null) {
            throw new IllegalArgumentException("capability is required");
        }
        if (capability == CompilationCommandCapability.ARGUMENT_VECTOR
                && !unresolvedReasons.isEmpty()) {
            throw new IllegalArgumentException(
                    "argument-vector command cannot carry unresolved reasons");
        }
        if (capability != CompilationCommandCapability.ARGUMENT_VECTOR
                && unresolvedReasons.isEmpty()) {
            throw new IllegalArgumentException(
                    "non-authoritative command capability requires an explicit reason");
        }
    }

    public boolean emitted() {
        return capability == CompilationCommandCapability.ARGUMENT_VECTOR;
    }
}
