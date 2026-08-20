package legacymodernizer.parser.parsing.build;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CompilationDatabaseContractTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void preservesEveryConfigurationForTheSameTranslationUnit() throws Exception {
        Path project = Files.createDirectories(temporaryDirectory.resolve("project"));
        Path build = Files.createDirectories(project.resolve("build"));
        Path source = Files.createDirectories(project.resolve("src")).resolve("sample.c");
        Files.writeString(source, "int sample(void) { return 0; }\n", StandardCharsets.UTF_8);
        Path database = project.resolve("compile_commands.json");
        Files.writeString(database, """
                [
                  {
                    "directory": "%s",
                    "arguments": ["/toolchain/clang", "-DFEATURE=1", "-c", "../src/sample.c"],
                    "file": "../src/sample.c",
                    "output": "feature-on.o"
                  },
                  {
                    "directory": "%s",
                    "arguments": ["/toolchain/clang", "-DFEATURE=0", "-c", "../src/sample.c"],
                    "file": "../src/sample.c",
                    "output": "feature-off.o"
                  }
                ]
                """.formatted(jsonPath(build), jsonPath(build)), StandardCharsets.UTF_8);

        CompilationDatabaseSnapshot snapshot = CompilationDatabaseLoader.load(database);

        assertEquals(CompilationDatabaseStatus.EXACT, snapshot.status());
        assertEquals(2, snapshot.population());
        assertEquals(2, snapshot.commands().size());
        assertEquals(0, snapshot.explicitlyUnresolved());
        assertEquals(source.toAbsolutePath().normalize(), snapshot.commands().get(0).file());
        assertEquals(source.toAbsolutePath().normalize(), snapshot.commands().get(1).file());
        assertNotEquals(snapshot.commands().get(0).commandObjectId(),
                snapshot.commands().get(1).commandObjectId());
        assertEquals(List.of("/toolchain/clang", "-DFEATURE=1", "-c", "../src/sample.c"),
                snapshot.commands().get(0).arguments());
        assertEquals(CompilationCommandCapability.ARGUMENT_VECTOR,
                snapshot.commands().get(0).capability());
    }

    @Test
    void shellCommandTextIsPreservedButNeverEligibleForAuthoritativeTrace() throws Exception {
        Path project = Files.createDirectories(temporaryDirectory.resolve("project"));
        Path source = project.resolve("sample.c");
        Files.writeString(source, "int sample(void) { return 0; }\n", StandardCharsets.UTF_8);
        Path database = project.resolve("compile_commands.json");
        Files.writeString(database, """
                [{
                  "directory": "%s",
                  "command": "clang -DVALUE=3 -c sample.c",
                  "file": "sample.c"
                }]
                """.formatted(jsonPath(project)), StandardCharsets.UTF_8);

        CompilationDatabaseSnapshot snapshot = CompilationDatabaseLoader.load(database);

        assertEquals(CompilationDatabaseStatus.PARTIAL, snapshot.status());
        assertEquals(1, snapshot.population());
        assertEquals(1, snapshot.commands().size());
        assertEquals(1, snapshot.explicitlyUnresolved());
        CompilationCommand command = snapshot.commands().get(0);
        assertEquals(CompilationCommandCapability.SHELL_COMMAND_ONLY, command.capability());
        assertEquals(List.of(), command.arguments());
        assertEquals("clang -DVALUE=3 -c sample.c", command.command());
        assertEquals(List.of("insufficient_argument_vector"), command.unresolvedReasons());
    }

    @Test
    void absentOrMalformedDatabaseIsExplicitlyUnresolvedRatherThanEmptySuccess() throws Exception {
        Path absent = temporaryDirectory.resolve("absent-compile_commands.json");
        CompilationDatabaseSnapshot absentSnapshot = CompilationDatabaseLoader.load(absent);

        assertEquals(CompilationDatabaseStatus.UNRESOLVED, absentSnapshot.status());
        assertEquals(1, absentSnapshot.population());
        assertEquals(0, absentSnapshot.commands().size());
        assertEquals(1, absentSnapshot.explicitlyUnresolved());
        assertEquals(List.of("insufficient_compilation_database"),
                absentSnapshot.unresolvedReasons());

        Path malformed = temporaryDirectory.resolve("compile_commands.json");
        Files.writeString(malformed, "{not-an-array}", StandardCharsets.UTF_8);
        CompilationDatabaseSnapshot malformedSnapshot = CompilationDatabaseLoader.load(malformed);

        assertEquals(CompilationDatabaseStatus.UNRESOLVED, malformedSnapshot.status());
        assertEquals(1, malformedSnapshot.population());
        assertEquals(0, malformedSnapshot.commands().size());
        assertEquals(1, malformedSnapshot.explicitlyUnresolved());
        assertEquals(64, malformedSnapshot.rawSha256().length());
        assertEquals(List.of("insufficient_compilation_database_syntax"),
                malformedSnapshot.unresolvedReasons());
    }

    @Test
    void commandObjectIdsAreDeterministicAcrossDatabaseEntryOrder() throws Exception {
        Path project = Files.createDirectories(temporaryDirectory.resolve("project"));
        Files.writeString(project.resolve("a.c"), "int a;\n", StandardCharsets.UTF_8);
        Files.writeString(project.resolve("b.c"), "int b;\n", StandardCharsets.UTF_8);
        String first = commandJson(project, "a.c", "a.o", "A=1");
        String second = commandJson(project, "b.c", "b.o", "B=1");
        Path left = temporaryDirectory.resolve("left.json");
        Path right = temporaryDirectory.resolve("right.json");
        Files.writeString(left, "[" + first + "," + second + "]", StandardCharsets.UTF_8);
        Files.writeString(right, "[" + second + "," + first + "]", StandardCharsets.UTF_8);

        List<String> leftIds = CompilationDatabaseLoader.load(left).commands().stream()
                .map(CompilationCommand::commandObjectId).sorted().toList();
        List<String> rightIds = CompilationDatabaseLoader.load(right).commands().stream()
                .map(CompilationCommand::commandObjectId).sorted().toList();

        assertEquals(leftIds, rightIds);
        assertEquals(2, leftIds.size());
        assertTrue(leftIds.stream().allMatch(id -> id.matches("[0-9a-f]{64}")));
    }

    @Test
    void malformedCommandObjectAndMissingSourceRemainInExactAccounting() throws Exception {
        Path project = Files.createDirectories(temporaryDirectory.resolve("project"));
        Path existing = project.resolve("existing.c");
        Files.writeString(existing, "int existing;\n", StandardCharsets.UTF_8);
        Path database = project.resolve("compile_commands.json");
        Files.writeString(database, """
                [
                  {
                    "directory": "%s",
                    "arguments": ["clang", "-c", "existing.c"],
                    "file": "existing.c"
                  },
                  {
                    "directory": "%s",
                    "arguments": ["clang", "-c", "missing.c"],
                    "file": "missing.c"
                  },
                  17
                ]
                """.formatted(jsonPath(project), jsonPath(project)), StandardCharsets.UTF_8);

        CompilationDatabaseSnapshot snapshot = CompilationDatabaseLoader.load(database);

        assertEquals(CompilationDatabaseStatus.PARTIAL, snapshot.status());
        assertEquals(3, snapshot.population());
        assertEquals(1, snapshot.emitted());
        assertEquals(2, snapshot.explicitlyUnresolved());
        assertEquals(3, snapshot.commands().size());
        assertEquals(List.of("insufficient_compilation_command_object",
                        "insufficient_translation_unit_source"),
                snapshot.unresolvedReasons());
    }

    @Test
    void duplicateCommandObjectsReceiveDistinctExactOnceIds() throws Exception {
        Path project = Files.createDirectories(temporaryDirectory.resolve("project"));
        Files.writeString(project.resolve("sample.c"), "int sample;\n", StandardCharsets.UTF_8);
        String command = commandJson(project, "sample.c", "sample.o", "FEATURE=1");
        Path database = project.resolve("compile_commands.json");
        Files.writeString(database, "[" + command + "," + command + "]",
                StandardCharsets.UTF_8);

        CompilationDatabaseSnapshot snapshot = CompilationDatabaseLoader.load(database);

        assertEquals(CompilationDatabaseStatus.EXACT, snapshot.status());
        assertEquals(2, snapshot.population());
        assertEquals(2, snapshot.emitted());
        assertEquals(2, snapshot.commands().stream()
                .map(CompilationCommand::commandObjectId).distinct().count());
    }

    @Test
    void malformedOptionalFieldIsNotSilentlyCollapsedAndFieldOrderDoesNotChangeIdentity()
            throws Exception {
        Path project = Files.createDirectories(temporaryDirectory.resolve("project"));
        Files.writeString(project.resolve("sample.c"), "int sample;\n", StandardCharsets.UTF_8);
        String directory = jsonPath(project);
        Path malformed = temporaryDirectory.resolve("malformed-field.json");
        Files.writeString(malformed, """
                [{
                  "file": "sample.c",
                  "output": 17,
                  "arguments": ["clang", "-c", "sample.c"],
                  "directory": "%s"
                }]
                """.formatted(directory), StandardCharsets.UTF_8);
        CompilationDatabaseSnapshot malformedSnapshot = CompilationDatabaseLoader.load(malformed);

        assertEquals(CompilationDatabaseStatus.PARTIAL, malformedSnapshot.status());
        assertEquals(List.of("insufficient_compilation_output_syntax"),
                malformedSnapshot.unresolvedReasons());

        Path left = temporaryDirectory.resolve("left-fields.json");
        Path right = temporaryDirectory.resolve("right-fields.json");
        Files.writeString(left, """
                [{"directory":"%s","file":"sample.c","arguments":["clang","-c","sample.c"]}]
                """.formatted(directory), StandardCharsets.UTF_8);
        Files.writeString(right, """
                [{"arguments":["clang","-c","sample.c"],"file":"sample.c","directory":"%s"}]
                """.formatted(directory), StandardCharsets.UTF_8);

        assertEquals(CompilationDatabaseLoader.load(left).commands().get(0).commandObjectId(),
                CompilationDatabaseLoader.load(right).commands().get(0).commandObjectId());
    }

    private static String commandJson(Path directory, String file, String output, String define) {
        return """
                {
                  "directory": "%s",
                  "arguments": ["clang", "-D%s", "-c", "%s"],
                  "file": "%s",
                  "output": "%s"
                }
                """.formatted(jsonPath(directory), define, file, file, output);
    }

    private static String jsonPath(Path path) {
        return path.toAbsolutePath().normalize().toString().replace("\\", "\\\\");
    }
}
