package legacymodernizer.parser.parsing.build;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import legacymodernizer.parser.recovery.workingcopy.Hashes;

/**
 * Lossless intake for the Clang JSON compilation-database interchange format.
 * This class never tokenizes a shell command and never executes a compiler.
 */
public final class CompilationDatabaseLoader {

    private static final String VERSION = "1.0.0";
    private static final String ID_DOMAIN = "robo-compilation-command-v1";
    private static final ObjectMapper JSON = new ObjectMapper();

    private CompilationDatabaseLoader() {
    }

    public static CompilationDatabaseSnapshot load(Path databasePath) {
        Path normalizedPath = databasePath.toAbsolutePath().normalize();
        if (!Files.isRegularFile(normalizedPath)) {
            return unresolved(normalizedPath, null, "insufficient_compilation_database");
        }

        byte[] raw;
        try {
            raw = Files.readAllBytes(normalizedPath);
        } catch (IOException unreadable) {
            return unresolved(normalizedPath, null,
                    "insufficient_compilation_database_read");
        }
        String rawSha256 = Hashes.sha256(raw);
        JsonNode root;
        try {
            root = JSON.readTree(raw);
        } catch (IOException | RuntimeException malformed) {
            return unresolved(normalizedPath, rawSha256,
                    "insufficient_compilation_database_syntax");
        }
        if (!(root instanceof ArrayNode array)) {
            return unresolved(normalizedPath, rawSha256,
                    "insufficient_compilation_database_syntax");
        }
        if (array.isEmpty()) {
            return unresolved(normalizedPath, rawSha256,
                    "insufficient_compilation_commands");
        }

        Path databaseDirectory = normalizedPath.getParent();
        List<Draft> drafts = new ArrayList<>(array.size());
        for (JsonNode node : array) {
            drafts.add(readCommand(node, databaseDirectory));
        }

        Map<String, Integer> ordinalsByBaseId = new HashMap<>();
        List<CompilationCommand> commands = new ArrayList<>(drafts.size());
        for (Draft draft : drafts) {
            int ordinal = ordinalsByBaseId.merge(draft.baseId(), 1, Integer::sum) - 1;
            String commandObjectId = Hashes.sha256((ID_DOMAIN + "\0" + draft.baseId()
                    + "\0" + ordinal).getBytes(StandardCharsets.UTF_8));
            commands.add(draft.seal(commandObjectId));
        }

        int emitted = (int) commands.stream().filter(CompilationCommand::emitted).count();
        int explicitlyUnresolved = commands.size() - emitted;
        CompilationDatabaseStatus status = explicitlyUnresolved == 0
                ? CompilationDatabaseStatus.EXACT
                : CompilationDatabaseStatus.PARTIAL;
        List<String> reasons = commands.stream()
                .flatMap(command -> command.unresolvedReasons().stream()).distinct().sorted().toList();
        return new CompilationDatabaseSnapshot(VERSION, normalizedPath, rawSha256, status,
                commands.size(), emitted, explicitlyUnresolved, commands, reasons);
    }

    private static Draft readCommand(JsonNode node, Path databaseDirectory) {
        if (!(node instanceof ObjectNode object)) {
            String baseId = baseId(node);
            return new Draft(baseId, null, null, null, null, List.of(), null, null,
                    CompilationCommandCapability.UNRESOLVED,
                    List.of("insufficient_compilation_command_object"));
        }

        String directoryLiteral = textual(object.get("directory"));
        String fileLiteral = textual(object.get("file"));
        String command = textual(object.get("command"));
        String output = textual(object.get("output"));
        List<String> arguments = stringArray(object.get("arguments"));
        LinkedHashSet<String> reasons = new LinkedHashSet<>();

        if (object.has("directory") && !object.get("directory").isTextual()) {
            reasons.add("insufficient_compilation_directory_syntax");
        } else if (directoryLiteral == null || directoryLiteral.isBlank()) {
            reasons.add("insufficient_compilation_directory");
        }
        if (object.has("file") && !object.get("file").isTextual()) {
            reasons.add("insufficient_translation_unit_file_syntax");
        } else if (fileLiteral == null || fileLiteral.isBlank()) {
            reasons.add("insufficient_translation_unit_file");
        }
        if (object.has("command") && !object.get("command").isTextual()) {
            reasons.add("insufficient_shell_command_syntax");
        }
        if (object.has("output") && !object.get("output").isTextual()) {
            reasons.add("insufficient_compilation_output_syntax");
        }
        JsonNode argumentsNode = object.get("arguments");
        if (argumentsNode != null && arguments == null) {
            reasons.add("insufficient_argument_vector_syntax");
            arguments = List.of();
        }
        if (arguments == null) {
            arguments = List.of();
        }
        boolean hasArguments = !arguments.isEmpty() && !arguments.get(0).isBlank();
        boolean hasCommand = command != null && !command.isBlank();
        if (!hasArguments) {
            reasons.add(hasCommand ? "insufficient_argument_vector"
                    : "insufficient_compilation_invocation");
        }

        Path directory = resolveDirectory(databaseDirectory, directoryLiteral, reasons);
        Path file = resolveFile(directory, fileLiteral, reasons);
        if (file != null && !Files.isRegularFile(file)) {
            reasons.add("insufficient_translation_unit_source");
        }

        CompilationCommandCapability capability;
        if (reasons.isEmpty()) {
            capability = CompilationCommandCapability.ARGUMENT_VECTOR;
        } else if (hasCommand && reasons.equals(
                new LinkedHashSet<>(List.of("insufficient_argument_vector")))) {
            capability = CompilationCommandCapability.SHELL_COMMAND_ONLY;
        } else {
            capability = CompilationCommandCapability.UNRESOLVED;
        }

        String baseId = baseId(object);
        return new Draft(baseId, directoryLiteral, fileLiteral, directory, file, arguments,
                command, output, capability, List.copyOf(reasons));
    }

    private static Path resolveDirectory(Path databaseDirectory, String literal,
                                         LinkedHashSet<String> reasons) {
        if (literal == null || literal.isBlank()) {
            return null;
        }
        try {
            Path path = Path.of(literal);
            return (path.isAbsolute() ? path : databaseDirectory.resolve(path))
                    .toAbsolutePath().normalize();
        } catch (InvalidPathException invalid) {
            reasons.add("insufficient_compilation_directory_syntax");
            return null;
        }
    }

    private static Path resolveFile(Path directory, String literal,
                                    LinkedHashSet<String> reasons) {
        if (directory == null || literal == null || literal.isBlank()) {
            return null;
        }
        try {
            Path path = Path.of(literal);
            return (path.isAbsolute() ? path : directory.resolve(path))
                    .toAbsolutePath().normalize();
        } catch (InvalidPathException invalid) {
            reasons.add("insufficient_translation_unit_file_syntax");
            return null;
        }
    }

    private static String textual(JsonNode node) {
        return node != null && node.isTextual() ? node.textValue() : null;
    }

    private static List<String> stringArray(JsonNode node) {
        if (node == null) {
            return null;
        }
        if (!node.isArray()) {
            return null;
        }
        List<String> result = new ArrayList<>(node.size());
        for (JsonNode item : node) {
            if (!item.isTextual()) {
                return null;
            }
            result.add(item.textValue());
        }
        return List.copyOf(result);
    }

    private static String baseId(JsonNode commandObject) {
        return Hashes.sha256((ID_DOMAIN + "\0" + canonicalize(commandObject))
                .getBytes(StandardCharsets.UTF_8));
    }

    private static JsonNode canonicalize(JsonNode node) {
        if (node.isObject()) {
            ObjectNode result = JSON.createObjectNode();
            TreeMap<String, JsonNode> ordered = new TreeMap<>();
            node.fields().forEachRemaining(entry -> ordered.put(entry.getKey(), entry.getValue()));
            ordered.forEach((field, value) -> result.set(field, canonicalize(value)));
            return result;
        }
        if (node.isArray()) {
            ArrayNode result = JSON.createArrayNode();
            node.forEach(item -> result.add(canonicalize(item)));
            return result;
        }
        return node.deepCopy();
    }

    private static CompilationDatabaseSnapshot unresolved(Path path, String rawSha256,
                                                          String reason) {
        return new CompilationDatabaseSnapshot(VERSION, path, rawSha256,
                CompilationDatabaseStatus.UNRESOLVED, 1, 0, 1, List.of(), List.of(reason));
    }

    private record Draft(
            String baseId,
            String directoryLiteral,
            String fileLiteral,
            Path directory,
            Path file,
            List<String> arguments,
            String command,
            String output,
            CompilationCommandCapability capability,
            List<String> reasons) {

        CompilationCommand seal(String commandObjectId) {
            return new CompilationCommand(commandObjectId, directoryLiteral, fileLiteral,
                    directory, file, arguments, command, output, capability, reasons);
        }
    }
}
