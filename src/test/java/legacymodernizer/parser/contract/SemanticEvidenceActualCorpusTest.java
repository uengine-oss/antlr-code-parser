package legacymodernizer.parser.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.intake.SourceIntakeClassifier;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.parsing.SourceTextCodec;
import legacymodernizer.parser.parsing.languages.c.CLanguageModule;
import legacymodernizer.parser.recovery.workingcopy.Hashes;
import legacymodernizer.parser.service.ParseProgressTracker;

/** Opt-in, deterministic full-population accounting for spec 131 actual corpora. */
class SemanticEvidenceActualCorpusTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    void actualCorpusIsRangeExactReplayStableAndFullyAccounted() throws Exception {
        String configuredCorpus = System.getProperty("parser.evidence.corpus", "");
        String configuredReport = System.getProperty("parser.evidence.report", "");
        Assumptions.assumeTrue(!configuredCorpus.isBlank() && !configuredReport.isBlank(),
                "Set parser.evidence.corpus and parser.evidence.report for actual validation");

        Path corpus = Path.of(configuredCorpus).toAbsolutePath().normalize();
        Path reportPath = Path.of(configuredReport).toAbsolutePath().normalize();
        assertTrue(Files.isDirectory(corpus), "actual corpus does not exist: " + corpus);
        String normalizedReport = reportPath.toString().replace('\\', '/');
        assertTrue(normalizedReport.contains(
                        "/specs/131-cross-node-semantic-grounding/_runs/framework/"),
                "actual evidence must stay inside spec 131 _runs/framework: " + reportPath);

        ParserWorkspace workspace = new ParserWorkspace(new SourceIntakeClassifier());
        workspace.intakeFromPath(corpus);
        CLanguageModule module = new CLanguageModule(workspace);
        module.prepareProjectContext();
        List<Path> sources;
        try (var walk = Files.walk(workspace.sourceDir())) {
            sources = walk.filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".c")
                            || path.toString().toLowerCase().endsWith(".h"))
                    .sorted().toList();
        }
        assertEquals(23, sources.size(), "APAS source population changed");

        ManagementFactory.getMemoryPoolMXBeans().forEach(pool -> {
            if (pool.isValid()) pool.resetPeakUsage();
        });
        long started = System.nanoTime();
        Set<String> globalFactIds = new HashSet<>();
        List<String> orderedFactIds = new ArrayList<>();
        List<String> orderedCallIds = new ArrayList<>();
        Map<String, ObjectNode> sealedSelection = new LinkedHashMap<>();
        ArrayNode files = JSON.createArrayNode();
        long legacyCalls = 0;
        long callFacts = 0;
        long activeCalls = 0;
        long inactiveCalls = 0;
        long conditionalCalls = 0;
        long namedCalls = 0;
        long constructorCalls = 0;
        long expressionCalls = 0;
        long conditionalRegions = 0;
        long diagnostics = 0;
        long recoveries = 0;

        for (Path sourcePath : sources) {
            byte[] sourceBytes = Files.readAllBytes(sourcePath);
            String source = SourceTextCodec.decode(sourceBytes).text();
            RawParseResult first = module.parseFile(sourcePath.toFile(),
                    new ParseProgressTracker(null, sourcePath.getFileName().toString()));
            RawParseResult repeated = module.parseFile(sourcePath.toFile(),
                    new ParseProgressTracker(null, sourcePath.getFileName().toString()));
            assertEquals(first.astJson(), repeated.astJson(),
                    "replay changed AST/evidence bytes for " + sourcePath);

            JsonNode root = JSON.readTree(first.astJson());
            JsonNode evidence = root.path("evidence");
            assertEquals("1.0.0", evidence.path("version").asText());
            assertEquals(first.sourceSha256(), evidence.path("rawSourceSha256").asText());
            assertEquals(source, evidence.path("decodedText").asText(),
                    "sealed decoded source mismatch in " + sourcePath);
            verifyCompleteness(evidence, sourcePath);

            List<JsonNode> calls = new ArrayList<>();
            List<JsonNode> regions = new ArrayList<>();
            String sourceId = evidence.path("sourceId").asText();
            for (JsonNode fact : evidence.path("facts")) {
                String factId = fact.path("factId").asText();
                assertEquals(64, factId.length(), "invalid fact ID in " + sourcePath);
                assertTrue(globalFactIds.add(factId), "duplicate canonical fact ID: " + factId);
                orderedFactIds.add(factId);
                verifyExactSlice(source, fact, sourcePath);
                if ("call".equals(fact.path("kind").asText())) {
                    verifyCallSubranges(source, fact, sourcePath);
                    calls.add(fact);
                    callFacts++;
                    switch (fact.path("payload").path("calleeKind").asText()) {
                        case "named" -> namedCalls++;
                        case "constructor" -> constructorCalls++;
                        case "expression" -> expressionCalls++;
                        default -> throw new AssertionError("invalid callee kind: " + fact);
                    }
                    orderedCallIds.add(factId);
                    switch (presence(evidence, fact).path("status").asText()) {
                        case "active" -> activeCalls++;
                        case "inactive" -> inactiveCalls++;
                        case "conditional", "unknown" -> conditionalCalls++;
                        default -> throw new AssertionError("invalid call presence: " + fact);
                    }
                } else if ("conditional_region".equals(fact.path("kind").asText())) {
                    regions.add(fact);
                    conditionalRegions++;
                }
            }

            calls.sort(Comparator.comparingInt(call -> rangeStart(call.path("range"))));
            if (!calls.isEmpty()) {
                select(sealedSelection, calls.get(0), evidence, sourceId, source,
                        "first_call_in_source");
                select(sealedSelection, calls.get(calls.size() - 1), evidence, sourceId, source,
                        "last_call_in_source");
            }
            Map<String, List<JsonNode>> callsByRange = new LinkedHashMap<>();
            for (JsonNode call : calls) {
                String expression = slice(source, call.path("range"));
                if (!"active".equals(presence(evidence, call).path("status").asText())) {
                    select(sealedSelection, call, evidence, sourceId, source,
                            "non_active_presence");
                }
                if (expression.contains("\n") || expression.contains("\r")) {
                    select(sealedSelection, call, evidence, sourceId, source,
                            "multiline_call");
                }
                String rangeKey = call.path("range").toString();
                callsByRange.computeIfAbsent(rangeKey, ignored -> new ArrayList<>()).add(call);
            }
            callsByRange.values().stream().filter(group -> group.size() > 1)
                    .flatMap(List::stream)
                    .forEach(call -> select(sealedSelection, call, evidence, sourceId, source,
                            "same_range_multiple_facts"));
            for (JsonNode call : calls) {
                int callStart = rangeStart(call.path("range"));
                boolean insideConditionalRegion = regions.stream().anyMatch(region ->
                        callStart >= rangeStart(region.path("range"))
                                && callStart < rangeEnd(region.path("range")));
                if (insideConditionalRegion) {
                    select(sealedSelection, call, evidence, sourceId, source,
                            "inside_conditional_region");
                }
            }

            long fileLegacyCalls = countNodes(root, "FUNCTION_CALL");
            legacyCalls += fileLegacyCalls;
            diagnostics += first.diagnostics().size();
            recoveries += first.antlrRecoveries();
            ObjectNode fileRow = files.addObject();
            fileRow.put("sourceId", evidence.path("sourceId").asText());
            fileRow.put("rawSourceSha256", first.sourceSha256());
            fileRow.put("parseStatus", evidence.path("parseStatus").asText());
            fileRow.put("legacyFunctionCallNodes", fileLegacyCalls);
            fileRow.put("callFacts", calls.size());
            fileRow.put("diagnostics", first.diagnostics().size());
            fileRow.put("antlrRecoveries", first.antlrRecoveries());
            fileRow.put("firstParseElapsedMillis", first.elapsedMillis());
            fileRow.put("repeatedParseElapsedMillis", repeated.elapsedMillis());
        }

        assertEquals(callFacts, activeCalls + inactiveCalls + conditionalCalls,
                "call presence partition is incomplete");
        assertEquals(callFacts, namedCalls + constructorCalls + expressionCalls,
                "callee syntax partition is incomplete");
        assertFalse(orderedCallIds.isEmpty(), "actual corpus emitted no call facts");
        assertEquals(orderedFactIds.size(), globalFactIds.size(), "fact ID set accounting mismatch");

        ObjectNode report = JSON.createObjectNode();
        report.put("contractVersion", "1.0.0");
        report.put("corpus", corpus.toString().replace('\\', '/'));
        report.put("sourceFiles", sources.size());
        report.put("sourceInventorySha256", sourceInventoryHash(workspace.sourceDir(), sources));
        report.put("deterministicReplay", true);
        report.put("legacyFunctionCallNodes", legacyCalls);
        report.put("callFacts", callFacts);
        report.put("activeCalls", activeCalls);
        report.put("inactiveCalls", inactiveCalls);
        report.put("conditionalOrUnknownCalls", conditionalCalls);
        report.put("namedCalls", namedCalls);
        report.put("constructorCalls", constructorCalls);
        report.put("expressionCalls", expressionCalls);
        report.put("conditionalRegions", conditionalRegions);
        report.put("diagnostics", diagnostics);
        report.put("antlrRecoveries", recoveries);
        report.put("factIdLedgerSha256", ledgerHash(orderedFactIds));
        report.put("callFactIdLedgerSha256", ledgerHash(orderedCallIds));
        report.put("wallMillisTwoPass", (System.nanoTime() - started) / 1_000_000L);
        report.put("peakHeapBytes", ManagementFactory.getMemoryPoolMXBeans().stream()
                .filter(pool -> pool.getType() == MemoryType.HEAP)
                .mapToLong(pool -> pool.getPeakUsage().getUsed()).sum());
        report.set("files", files);
        ArrayNode selection = report.putArray("sealedDirectJudgmentPopulation");
        sealedSelection.values().forEach(selection::add);

        Files.createDirectories(reportPath.getParent());
        new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                .writeValue(reportPath.toFile(), report);
    }

    private static void verifyCompleteness(JsonNode evidence, Path source) {
        Set<String> kinds = new HashSet<>();
        for (JsonNode row : evidence.path("completeness")) {
            assertTrue(kinds.add(row.path("kind").asText()),
                    "duplicate completeness kind in " + source);
            assertEquals(row.path("population").asLong(),
                    row.path("emitted").asLong() + row.path("explicitlyUnresolved").asLong(),
                    "completeness equation failed in " + source + ": " + row);
        }
        assertEquals(Set.of("call", "import", "symbol", "literal", "assignment",
                "parameter", "macro", "embedded_language", "conditional_region"), kinds);
    }

    private static void verifyExactSlice(String source, JsonNode fact, Path path) {
        int start = rangeStart(fact.path("range"));
        int end = rangeEnd(fact.path("range"));
        int[] codePoints = source.codePoints().toArray();
        assertTrue(start >= 0 && end >= start && end <= codePoints.length,
                "invalid fact range in " + path + ": " + fact);
        new String(codePoints, start, end - start);
        assertFalse(fact.has("sourceSliceSha256"), "redundant slice hash in " + path);
        assertFalse(fact.has("syntaxOwnerFactId"), "Parser emitted Analyzer-owned syntax owner");
    }

    private static long countNodes(JsonNode node, String type) {
        long count = type.equals(node.path("type").asText()) ? 1 : 0;
        for (JsonNode child : node.path("children")) count += countNodes(child, type);
        return count;
    }

    private static void verifyCallSubranges(String source, JsonNode call, Path path) {
        int callStart = rangeStart(call.path("range"));
        int callEnd = rangeEnd(call.path("range"));
        JsonNode payload = call.path("payload");
        String callee = slice(source, payload.path("calleeRange"));
        assertFalse(callee.isBlank(), "empty callee range in " + path);
        String calleeKind = payload.path("calleeKind").asText();
        assertTrue(Set.of("named", "constructor", "expression").contains(calleeKind),
                "invalid callee kind in " + path + ": " + call);
        if ("expression".equals(calleeKind)) {
            assertTrue(payload.path("terminalName").isNull(),
                    "expression callee claimed a terminal name in " + path);
        } else {
            assertFalse(payload.path("terminalName").asText().isBlank(),
                    "named callee omitted terminal name in " + path);
        }
        assertFalse(payload.has("callExpression"), "call source text was duplicated in " + path);
        assertFalse(payload.has("calleeExpression"), "callee source text was duplicated in " + path);
        int previousEnd = callStart;
        for (JsonNode range : payload.path("argumentRanges")) {
            int start = rangeStart(range);
            int end = rangeEnd(range);
            assertTrue(start >= callStart && end <= callEnd && start >= previousEnd,
                    "argument range/order mismatch in " + path + ": " + range);
            previousEnd = end;
        }
    }

    private static String slice(String source, JsonNode range) {
        int start = rangeStart(range);
        int end = rangeEnd(range);
        int[] codePoints = source.codePoints().toArray();
        return new String(codePoints, start, end - start);
    }

    private static int rangeStart(JsonNode range) {
        return range.path("charOffset").asInt(-1);
    }

    private static int rangeEnd(JsonNode range) {
        int start = rangeStart(range);
        return start + range.path("charLength").asInt(-1);
    }

    private static void select(Map<String, ObjectNode> selection, JsonNode fact,
                               JsonNode evidence, String sourceId, String source, String reason) {
        String id = fact.path("factId").asText();
        ObjectNode selected = selection.computeIfAbsent(id, ignored -> {
            ObjectNode row = JSON.createObjectNode();
            row.put("factId", id);
            row.put("sourceId", sourceId);
            row.put("grammarRule", evidence.path("grammarRules")
                    .path(fact.path("grammarRuleRef").asInt()).asText());
            row.set("range", fact.path("range").deepCopy());
            row.set("presence", presence(evidence, fact).deepCopy());
            row.set("payload", fact.path("payload").deepCopy());
            ObjectNode display = row.putObject("displayPreview");
            display.put("callExpression", slice(source, fact.path("range")));
            display.put("calleeExpression",
                    slice(source, fact.path("payload").path("calleeRange")));
            ArrayNode arguments = display.putArray("arguments");
            fact.path("payload").path("argumentRanges").forEach(range ->
                    arguments.add(slice(source, range)));
            row.set("selectionReasons", JSON.createArrayNode());
            return row;
        });
        ArrayNode reasons = (ArrayNode) selected.path("selectionReasons");
        boolean exists = false;
        for (JsonNode existing : reasons) exists |= reason.equals(existing.asText());
        if (!exists) reasons.add(reason);
    }

    private static JsonNode presence(JsonNode evidence, JsonNode fact) {
        int reference = fact.path("presenceRef").asInt(-1);
        JsonNode presences = evidence.path("presences");
        assertTrue(reference >= 0 && reference < presences.size(),
                "invalid presenceRef: " + fact);
        return presences.get(reference);
    }

    private static String ledgerHash(List<String> ids) {
        return Hashes.sha256(String.join("\n", ids).getBytes(StandardCharsets.UTF_8));
    }

    private static String sourceInventoryHash(Path sourceRoot, List<Path> sources) throws Exception {
        List<String> rows = new ArrayList<>();
        for (Path source : sources) {
            rows.add(sourceRoot.relativize(source).toString().replace('\\', '/') + "\0"
                    + Hashes.sha256(Files.readAllBytes(source)));
        }
        return ledgerHash(rows);
    }
}
