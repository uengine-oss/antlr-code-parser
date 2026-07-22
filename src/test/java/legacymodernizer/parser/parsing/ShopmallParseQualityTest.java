package legacymodernizer.parser.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import legacymodernizer.parser.api.stream.ParseStreamEvent;
import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.recovery.workingcopy.Hashes;

@SpringBootTest
class ShopmallParseQualityTest {

    @Autowired private ParseOrchestrator orchestrator;
    @Autowired private ParserWorkspace workspace;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    static void requireIsolatedDataRoot() {
        assertTrue(System.getProperty("parser.data.root", "").replace('\\', '/')
                .contains("/target/test-data"));
    }

    @Test
    void matchesAllMaintainedShopmallFunctionsAndFriendlyStreamContract() throws Exception {
        String sourceProperty = System.getProperty("parser.shopmall.source");
        String answerProperty = System.getProperty("parser.shopmall.answer.key");
        Assumptions.assumeTrue(sourceProperty != null && answerProperty != null);
        Path source = Path.of(sourceProperty).toAbsolutePath().normalize();
        Path answerPath = Path.of(answerProperty).toAbsolutePath().normalize();
        Assumptions.assumeTrue(Files.isDirectory(source) && Files.isRegularFile(answerPath));

        String sourceBefore = inventoryHash(source);
        ParserWorkspace.IntakeResult intake = workspace.intakeFromPath(source);
        List<ParseStreamEvent> events = new ArrayList<>();
        orchestrator.parse(null, events::add);
        assertEquals(sourceBefore, inventoryHash(source), "Operational source changed");
        assertEquals(12, intake.sourceCount());

        JsonNode expected = mapper.readTree(answerPath.toFile());
        Map<String, List<ActualFunction>> actual = readFunctions(workspace.analysisDir());
        assertEquals(124, expected.size());
        assertEquals(124, actual.values().stream().mapToInt(List::size).sum());
        assertEquals(124, actual.size());

        List<String> mismatches = new ArrayList<>();
        expected.fields().forEachRemaining(entry -> compare(
                entry.getKey(), entry.getValue(), actual, mismatches));
        assertTrue(mismatches.isEmpty(), String.join("\n", mismatches));

        long astFiles;
        try (var files = Files.walk(workspace.analysisDir())) {
            astFiles = files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json")).count();
        }
        assertEquals(12, astFiles);
        assertEquals(12, diagnosticStatuses("EXACT"));
        assertFalse(hasFileWithSuffix(workspace.repairsDir(), ".repair.json"));
        assertFalse(Files.exists(workspace.repairsDir()
                .resolve("review/promotion-candidates.json")));

        List<String> lifecycle = events.stream().map(ParseStreamEvent::event).toList();
        assertEquals("run_started", lifecycle.get(0));
        assertTrue(lifecycle.contains("language_detected"));
        assertEquals(12, events.stream().filter(event -> "file_result".equals(event.event())).count());
        assertEquals(12, events.stream().filter(event -> "file_result".equals(event.event()))
                .filter(event -> "EXACT".equals(event.quality())).count());
        assertTrue(lifecycle.contains("quality_summary"));
        assertEquals("run_completed", lifecycle.get(lifecycle.size() - 1));
        assertFalse(events.stream().anyMatch(event -> "error".equals(event.type())));
        ParseStreamEvent summary = events.stream()
                .filter(event -> "run_completed".equals(event.event())).findFirst().orElseThrow();
        assertEquals(12, summary.counts().get("exact"));
        assertTrue(summary.content().contains("정확 12"));
    }

    private Map<String, List<ActualFunction>> readFunctions(Path analysisRoot) throws Exception {
        Map<String, List<ActualFunction>> functions = new LinkedHashMap<>();
        try (var files = Files.walk(analysisRoot)) {
            for (Path path : files.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".json")).sorted().toList()) {
                JsonNode root = mapper.readTree(path.toFile());
                collect(root, path.getFileName().toString().replace(".json", ".c"), null, functions);
            }
        }
        return functions;
    }

    private void collect(JsonNode node, String file, ActualFunction owner,
                         Map<String, List<ActualFunction>> functions) {
        ActualFunction current = owner;
        if ("FUNCTION".equals(node.path("type").asText())) {
            current = new ActualFunction(file, node.path("startLine").asInt(),
                    node.path("endLine").asInt());
            functions.computeIfAbsent(node.path("name").asText(), ignored -> new ArrayList<>())
                    .add(current);
        } else if (current != null) {
            String type = node.path("type").asText();
            if ("FUNCTION_CALL".equals(type)) current.calls.add(node.path("name").asText());
            if ("IF".equals(type)) current.control.merge("if", 1, Integer::sum);
            if ("LOOP".equals(type)) current.control.merge("loop", 1, Integer::sum);
            if ("SWITCH".equals(type)) current.control.merge("switch", 1, Integer::sum);
            if ("CASE".equals(type)) current.control.merge("case", 1, Integer::sum);
        }
        for (JsonNode child : node.path("children")) {
            collect(child, file, current, functions);
        }
    }

    private static void compare(String name, JsonNode expected,
                                Map<String, List<ActualFunction>> actual,
                                List<String> mismatches) {
        List<ActualFunction> matches = actual.getOrDefault(name, List.of());
        if (matches.size() != 1) {
            mismatches.add(name + " count expected=1 actual=" + matches.size());
            return;
        }
        ActualFunction function = matches.get(0);
        String expectedFile = expected.path("file").asText();
        if (!expectedFile.equals(function.file)
                || expected.path("start_line").asInt() != function.startLine
                || expected.path("end_line").asInt() != function.endLine) {
            mismatches.add(name + " range expected=" + expectedFile + ":"
                    + expected.path("start_line").asInt() + "-" + expected.path("end_line").asInt()
                    + " actual=" + function.file + ":" + function.startLine + "-" + function.endLine);
        }
        expected.path("calls").forEach(call -> {
            if (!function.calls.contains(call.asText())) {
                mismatches.add(name + " missing call " + call.asText());
            }
        });
        compareControl(name, "if", expected.path("control").path("if").asInt(), function, mismatches);
        int expectedLoops = expected.path("control").path("for").asInt()
                + expected.path("control").path("while").asInt();
        compareControl(name, "loop", expectedLoops, function, mismatches);
        compareControl(name, "switch", expected.path("control").path("switch").asInt(), function, mismatches);
        compareControl(name, "case", expected.path("control").path("case").asInt(), function, mismatches);
    }

    private static void compareControl(String functionName, String control, int expected,
                                       ActualFunction actual, List<String> mismatches) {
        int value = actual.control.getOrDefault(control, 0);
        if (expected != value) {
            mismatches.add(functionName + "." + control + " expected=" + expected + " actual=" + value);
        }
    }

    private long diagnosticStatuses(String status) throws Exception {
        if (!Files.isDirectory(workspace.diagnosticsDir())) return 0;
        try (var files = Files.walk(workspace.diagnosticsDir())) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".parse.json"))
                    .filter(path -> {
                        try { return status.equals(mapper.readTree(path.toFile()).path("status").asText()); }
                        catch (Exception error) { return false; }
                    }).count();
        }
    }

    private static boolean hasFileWithSuffix(Path root, String suffix) throws Exception {
        if (!Files.isDirectory(root)) return false;
        try (var files = Files.walk(root)) {
            return files.anyMatch(path -> Files.isRegularFile(path) && path.toString().endsWith(suffix));
        }
    }

    private static String inventoryHash(Path root) throws Exception {
        StringBuilder inventory = new StringBuilder();
        try (var files = Files.walk(root)) {
            for (Path file : files.filter(Files::isRegularFile).sorted().toList()) {
                inventory.append(root.relativize(file).toString().replace('\\', '/')).append('\n')
                        .append(Hashes.sha256(Files.readAllBytes(file))).append('\n');
            }
        }
        return Hashes.sha256(inventory.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static final class ActualFunction {
        private final String file;
        private final int startLine;
        private final int endLine;
        private final Set<String> calls = new LinkedHashSet<>();
        private final Map<String, Integer> control = new HashMap<>();

        private ActualFunction(String file, int startLine, int endLine) {
            this.file = file;
            this.startLine = startLine;
            this.endLine = endLine;
        }
    }
}
