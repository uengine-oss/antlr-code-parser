package legacymodernizer.parser.parsing.build;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import legacymodernizer.parser.parsing.evidence.ConfiguredPreprocessingEvidence;

class ProjectCompilationCatalogContractTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void discoversNestedDatabasesAndPreservesAllConfigurationsForAnOriginAlias()
            throws Exception {
        Path origin = Files.createDirectories(temporaryDirectory.resolve("origin"));
        Path originSource = Files.createDirectories(origin.resolve("src")).resolve("sample.c");
        Files.writeString(originSource, "int sample;\n", StandardCharsets.UTF_8);
        Path workspace = Files.createDirectories(temporaryDirectory.resolve("workspace"));
        Path workspaceSource = Files.createDirectories(workspace.resolve("src")).resolve("sample.c");
        Files.copy(originSource, workspaceSource);
        writeDatabase(workspace.resolve("build/debug/compile_commands.json"), origin,
                originSource, "DEBUG=1", "debug.o");
        writeDatabase(workspace.resolve("build/release/compile_commands.json"), origin,
                originSource, "NDEBUG=1", "release.o");

        ProjectCompilationCatalog catalog = ProjectCompilationCatalog.discover(workspace);
        CompilationUnitBuildContext context = catalog.resolve(workspaceSource, originSource);

        assertEquals(ProjectCompilationCatalogStatus.EXACT, catalog.status());
        assertEquals(2, catalog.databases().size());
        assertEquals(2, catalog.commands().size());
        assertEquals(CompilationUnitBuildStatus.EXACT, context.status());
        assertEquals(2, context.population());
        assertEquals(2, context.emitted());
        assertEquals(0, context.explicitlyUnresolved());
        assertEquals(2, context.emittedCommandOccurrenceIds().size());
        assertNotEquals(context.emittedCommandOccurrenceIds().get(0),
                context.emittedCommandOccurrenceIds().get(1));
    }

    @Test
    void absentDatabaseAndUnmatchedTranslationUnitAreExplicitlyUnresolved() throws Exception {
        Path project = Files.createDirectories(temporaryDirectory.resolve("project"));
        Path source = project.resolve("sample.c");
        Files.writeString(source, "int sample;\n", StandardCharsets.UTF_8);

        ProjectCompilationCatalog absent = ProjectCompilationCatalog.discover(project);
        CompilationUnitBuildContext absentContext = absent.resolve(source, source);

        assertEquals(ProjectCompilationCatalogStatus.UNRESOLVED, absent.status());
        assertEquals(CompilationUnitBuildStatus.UNRESOLVED, absentContext.status());
        assertEquals(1, absentContext.population());
        assertEquals(0, absentContext.emitted());
        assertEquals(1, absentContext.explicitlyUnresolved());
        assertEquals(1, absentContext.unresolvedEvidenceIds().size());
        assertEquals(List.of("insufficient_compilation_database"),
                absentContext.unresolvedReasons());

        Path other = project.resolve("other.c");
        Files.writeString(other, "int other;\n", StandardCharsets.UTF_8);
        writeDatabase(project.resolve("compile_commands.json"), project, other,
                "OTHER=1", "other.o");
        ProjectCompilationCatalog unmatched = ProjectCompilationCatalog.discover(project);
        CompilationUnitBuildContext unmatchedContext = unmatched.resolve(source, source);

        assertEquals(CompilationUnitBuildStatus.UNRESOLVED, unmatchedContext.status());
        assertEquals(List.of("insufficient_translation_unit_compile_action"),
                unmatchedContext.unresolvedReasons());
    }

    @Test
    void malformedDatabaseMakesEverySourceResolutionPartialWithoutHidingExactCommands()
            throws Exception {
        Path project = Files.createDirectories(temporaryDirectory.resolve("project"));
        Path source = project.resolve("sample.c");
        Files.writeString(source, "int sample;\n", StandardCharsets.UTF_8);
        writeDatabase(project.resolve("build/compile_commands.json"), project, source,
                "FEATURE=1", "sample.o");
        Path malformed = project.resolve("vendor/compile_commands.json");
        Files.createDirectories(malformed.getParent());
        Files.writeString(malformed, "{malformed}", StandardCharsets.UTF_8);

        ProjectCompilationCatalog catalog = ProjectCompilationCatalog.discover(project);
        CompilationUnitBuildContext context = catalog.resolve(source, source);

        assertEquals(ProjectCompilationCatalogStatus.PARTIAL, catalog.status());
        assertEquals(CompilationUnitBuildStatus.PARTIAL, context.status());
        assertEquals(2, context.population());
        assertEquals(1, context.emitted());
        assertEquals(1, context.explicitlyUnresolved());
        assertEquals(1, context.emittedCommandOccurrenceIds().size());
        assertEquals(1, context.unresolvedEvidenceIds().size());
        assertEquals(List.of("insufficient_compilation_database_syntax"),
                context.unresolvedReasons());
    }

    @Test
    void catalogAndOccurrenceIdsAreStableAcrossDatabaseEntryAndFieldOrder() throws Exception {
        Path project = Files.createDirectories(temporaryDirectory.resolve("project"));
        Path a = project.resolve("a.c");
        Path b = project.resolve("b.c");
        Files.writeString(a, "int a;\n", StandardCharsets.UTF_8);
        Files.writeString(b, "int b;\n", StandardCharsets.UTF_8);
        Path database = project.resolve("compile_commands.json");
        String directory = jsonPath(project);
        String aCommand = """
                {"file":"a.c","arguments":["clang","-c","a.c"],"directory":"%s"}
                """.formatted(directory);
        String bCommand = """
                {"directory":"%s","arguments":["clang","-c","b.c"],"file":"b.c"}
                """.formatted(directory);
        Files.writeString(database, "[" + aCommand + "," + bCommand + "]",
                StandardCharsets.UTF_8);
        ProjectCompilationCatalog first = ProjectCompilationCatalog.discover(project);
        List<String> firstIds = first.commands().stream()
                .map(CompilationCommandOccurrence::occurrenceId).sorted().toList();

        Files.writeString(database, "[" + bCommand + "," + aCommand + "]",
                StandardCharsets.UTF_8);
        ProjectCompilationCatalog replay = ProjectCompilationCatalog.discover(project);
        List<String> replayIds = replay.commands().stream()
                .map(CompilationCommandOccurrence::occurrenceId).sorted().toList();

        assertEquals(first.databaseIds(), replay.databaseIds());
        assertEquals(firstIds, replayIds);
    }

    @Test
    void v1ClosureCannotAuthorizeRawSourceSemanticOccurrences() throws Exception {
        Path project = Files.createDirectories(temporaryDirectory.resolve("project"));
        Path source = project.resolve("sample.c");
        Files.writeString(source, "int sample;\n", StandardCharsets.UTF_8);
        writeDatabase(project.resolve("compile_commands.json"), project, source,
                "FEATURE=1", "sample.o");
        CompilationUnitBuildContext exactBuild = ProjectCompilationCatalog
                .discover(project).resolve(source, source);
        String traceId = "a".repeat(64);
        var exactTrace = new ConfiguredPreprocessingEvidence.TraceCompleteness(
                "exact", 1, 1, 0, List.of(traceId), List.of(traceId), List.of(),
                List.of());

        assertThrows(IllegalArgumentException.class, () ->
                new ConfiguredPreprocessingEvidence(
                        "1.0.0", "exact", "authoritative", exactBuild, exactTrace));
    }

    private static void writeDatabase(Path database, Path directory, Path source,
                                      String define, String output) throws Exception {
        Files.createDirectories(database.getParent());
        Files.writeString(database, """
                [{
                  "directory": "%s",
                  "arguments": ["clang", "-D%s", "-c", "%s"],
                  "file": "%s",
                  "output": "%s"
                }]
                """.formatted(jsonPath(directory), define, jsonPath(source),
                        jsonPath(source), output), StandardCharsets.UTF_8);
    }

    private static String jsonPath(Path path) {
        return path.toAbsolutePath().normalize().toString().replace("\\", "\\\\");
    }
}
