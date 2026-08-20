package legacymodernizer.parser.parsing.build;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

import legacymodernizer.parser.recovery.workingcopy.Hashes;

/** Discovers every compilation database and resolves commands only by exact path/file identity. */
public final class ProjectCompilationCatalog {

    private static final String DATABASE_ID_DOMAIN = "robo-compilation-database-v1";
    private static final String OCCURRENCE_ID_DOMAIN = "robo-compilation-command-occurrence-v1";
    private static final String RESOLUTION_ID_DOMAIN = "robo-compilation-unit-build-v1";

    private final Path projectRoot;
    private final ProjectCompilationCatalogStatus status;
    private final List<CompilationDatabaseOccurrence> databases;
    private final List<CompilationCommandOccurrence> commands;
    private final List<String> globalUnknownSeeds;
    private final List<String> globalUnknownReasons;

    private ProjectCompilationCatalog(
            Path projectRoot,
            ProjectCompilationCatalogStatus status,
            List<CompilationDatabaseOccurrence> databases,
            List<CompilationCommandOccurrence> commands,
            List<String> globalUnknownSeeds,
            List<String> globalUnknownReasons) {
        this.projectRoot = projectRoot;
        this.status = status;
        this.databases = List.copyOf(databases);
        this.commands = List.copyOf(commands);
        this.globalUnknownSeeds = List.copyOf(globalUnknownSeeds);
        this.globalUnknownReasons = List.copyOf(globalUnknownReasons);
    }

    public static ProjectCompilationCatalog discover(Path projectRoot) {
        Path normalizedRoot = projectRoot.toAbsolutePath().normalize();
        if (!Files.isDirectory(normalizedRoot)) {
            return unresolved(normalizedRoot, "insufficient_project_source_root");
        }

        List<Path> databasePaths;
        try (var walk = Files.walk(normalizedRoot)) {
            databasePaths = walk.filter(Files::isRegularFile)
                    .filter(path -> "compile_commands.json".equals(
                            path.getFileName().toString()))
                    .sorted(Comparator.comparing(path -> relativePath(normalizedRoot, path)))
                    .toList();
        } catch (IOException | RuntimeException discoveryFailure) {
            return unresolved(normalizedRoot, "insufficient_compilation_database_discovery");
        }
        if (databasePaths.isEmpty()) {
            return unresolved(normalizedRoot, "insufficient_compilation_database");
        }

        List<CompilationDatabaseOccurrence> databases = new ArrayList<>();
        List<CompilationCommandOccurrence> commands = new ArrayList<>();
        LinkedHashSet<String> globalReasons = new LinkedHashSet<>();
        List<String> globalSeeds = new ArrayList<>();
        int emittedCommands = 0;
        int unresolvedCommands = 0;

        for (Path databasePath : databasePaths) {
            CompilationDatabaseSnapshot snapshot = CompilationDatabaseLoader.load(databasePath);
            String relativePath = relativePath(normalizedRoot, databasePath);
            String databaseId = databaseId(relativePath, snapshot);
            databases.add(new CompilationDatabaseOccurrence(databaseId, relativePath, snapshot));
            for (CompilationCommand command : snapshot.commands()) {
                String occurrenceId = Hashes.sha256((OCCURRENCE_ID_DOMAIN + "\0" + databaseId
                        + "\0" + command.commandObjectId()).getBytes(StandardCharsets.UTF_8));
                commands.add(new CompilationCommandOccurrence(
                        occurrenceId, databaseId, command));
                if (command.emitted()) emittedCommands++;
                else unresolvedCommands++;
                if (command.file() == null) {
                    globalSeeds.add(occurrenceId);
                    globalReasons.addAll(command.unresolvedReasons());
                }
            }
            if (snapshot.commands().isEmpty()
                    && snapshot.explicitlyUnresolved() > 0) {
                globalSeeds.add(databaseId);
                globalReasons.addAll(snapshot.unresolvedReasons());
            }
        }
        commands.sort(Comparator.comparing(CompilationCommandOccurrence::occurrenceId));

        ProjectCompilationCatalogStatus status = unresolvedCommands == 0 && globalSeeds.isEmpty()
                ? ProjectCompilationCatalogStatus.EXACT
                : emittedCommands == 0 ? ProjectCompilationCatalogStatus.UNRESOLVED
                : ProjectCompilationCatalogStatus.PARTIAL;
        return new ProjectCompilationCatalog(normalizedRoot, status, databases, commands,
                globalSeeds, globalReasons.stream().sorted().toList());
    }

    public CompilationUnitBuildContext resolve(Path workspaceSource, Path originSource) {
        Path workspace = workspaceSource.toAbsolutePath().normalize();
        Path origin = originSource == null ? workspace : originSource.toAbsolutePath().normalize();
        List<String> allIds = new ArrayList<>();
        List<String> emittedIds = new ArrayList<>();
        List<String> unresolvedIds = new ArrayList<>();
        List<String> unresolvedEvidenceIds = new ArrayList<>();
        LinkedHashSet<String> reasons = new LinkedHashSet<>(globalUnknownReasons);
        String sourceKey = sourceKey(workspace);
        for (String seed : globalUnknownSeeds) {
            unresolvedEvidenceIds.add(resolutionEvidenceId(sourceKey, seed));
        }

        for (CompilationCommandOccurrence occurrence : commands) {
            CompilationCommand command = occurrence.command();
            if (command.file() == null
                    || !sameSource(command.file(), workspace, origin)) {
                continue;
            }
            allIds.add(occurrence.occurrenceId());
            if (command.emitted()) {
                emittedIds.add(occurrence.occurrenceId());
            } else {
                unresolvedIds.add(occurrence.occurrenceId());
                unresolvedEvidenceIds.add(occurrence.occurrenceId());
                reasons.addAll(command.unresolvedReasons());
            }
        }

        int emitted = emittedIds.size();
        int explicitlyUnresolved = unresolvedEvidenceIds.size();
        if (allIds.isEmpty() && globalUnknownSeeds.isEmpty()) {
            explicitlyUnresolved = 1;
            unresolvedEvidenceIds.add(resolutionEvidenceId(
                    sourceKey, "insufficient_translation_unit_compile_action"));
            reasons.add("insufficient_translation_unit_compile_action");
        }
        int population = emitted + explicitlyUnresolved;
        CompilationUnitBuildStatus status = explicitlyUnresolved == 0 && emitted > 0
                ? CompilationUnitBuildStatus.EXACT
                : emitted > 0 ? CompilationUnitBuildStatus.PARTIAL
                : CompilationUnitBuildStatus.UNRESOLVED;
        return new CompilationUnitBuildContext("1.0.0", status, population, emitted,
                explicitlyUnresolved, allIds, emittedIds, unresolvedIds,
                unresolvedEvidenceIds,
                reasons.stream().sorted().toList());
    }

    public Path projectRoot() {
        return projectRoot;
    }

    public ProjectCompilationCatalogStatus status() {
        return status;
    }

    public List<CompilationDatabaseOccurrence> databases() {
        return databases;
    }

    public List<CompilationCommandOccurrence> commands() {
        return commands;
    }

    public List<String> databaseIds() {
        return databases.stream().map(CompilationDatabaseOccurrence::databaseId).sorted().toList();
    }

    private static boolean sameSource(Path commandFile, Path workspace, Path origin) {
        Path normalized = commandFile.toAbsolutePath().normalize();
        if (normalized.equals(workspace) || normalized.equals(origin)) {
            return true;
        }
        return isSameFile(normalized, workspace) || isSameFile(normalized, origin);
    }

    private static boolean isSameFile(Path left, Path right) {
        if (!Files.isRegularFile(left) || !Files.isRegularFile(right)) {
            return false;
        }
        try {
            return Files.isSameFile(left, right);
        } catch (IOException | SecurityException unavailable) {
            return false;
        }
    }

    private static String databaseId(String relativePath,
                                     CompilationDatabaseSnapshot snapshot) {
        List<String> commandIds = snapshot.commands().stream()
                .map(CompilationCommand::commandObjectId).sorted().toList();
        String unresolvedIdentity = commandIds.isEmpty()
                ? String.valueOf(snapshot.rawSha256()) : String.join("\0", commandIds);
        String tuple = DATABASE_ID_DOMAIN + "\0" + relativePath + "\0"
                + snapshot.status() + "\0" + unresolvedIdentity;
        return Hashes.sha256(tuple.getBytes(StandardCharsets.UTF_8));
    }

    private static String relativePath(Path root, Path path) {
        return root.relativize(path.toAbsolutePath().normalize()).toString().replace('\\', '/');
    }

    private String sourceKey(Path workspace) {
        if (workspace.startsWith(projectRoot)) {
            return relativePath(projectRoot, workspace);
        }
        try {
            return "unscoped/" + Hashes.sha256(Files.readAllBytes(workspace)) + "/"
                    + workspace.getFileName();
        } catch (IOException unreadable) {
            return "unscoped/unreadable/" + workspace.getFileName();
        }
    }

    private static String resolutionEvidenceId(String sourceKey, String seed) {
        return Hashes.sha256((RESOLUTION_ID_DOMAIN + "\0" + sourceKey + "\0" + seed)
                .getBytes(StandardCharsets.UTF_8));
    }

    private static ProjectCompilationCatalog unresolved(Path root, String reason) {
        return new ProjectCompilationCatalog(root, ProjectCompilationCatalogStatus.UNRESOLVED,
                List.of(), List.of(), List.of(reason), List.of(reason));
    }
}
