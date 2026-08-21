package legacymodernizer.parser.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
        String configuredExport = System.getProperty("parser.evidence.export", "");
        Assumptions.assumeTrue(!configuredCorpus.isBlank() && !configuredReport.isBlank(),
                "Set parser.evidence.corpus and parser.evidence.report for actual validation");

        Path corpus = Path.of(configuredCorpus).toAbsolutePath().normalize();
        Path reportPath = Path.of(configuredReport).toAbsolutePath().normalize();
        assertTrue(Files.isDirectory(corpus), "actual corpus does not exist: " + corpus);
        String normalizedReport = reportPath.toString().replace('\\', '/');
        assertTrue(normalizedReport.contains(
                        "/specs/131-cross-node-semantic-grounding/_runs/framework/"),
                "actual evidence must stay inside spec 131 _runs/framework: " + reportPath);
        Path exportRoot = configuredExport.isBlank() ? null
                : Path.of(configuredExport).toAbsolutePath().normalize();
        if (exportRoot != null) {
            String normalizedExport = exportRoot.toString().replace('\\', '/');
            assertTrue(normalizedExport.contains(
                            "/specs/131-cross-node-semantic-grounding/_runs/framework/"),
                    "actual export must stay inside spec 131 _runs/framework: " + exportRoot);
            if (Files.exists(exportRoot)) {
                try (var walk = Files.walk(exportRoot)) {
                    assertFalse(walk.anyMatch(Files::isRegularFile),
                            "actual export refuses to overwrite files: " + exportRoot);
                }
            }
        }

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
        List<String> orderedImportIds = new ArrayList<>();
        List<String> orderedMacroIds = new ArrayList<>();
        List<String> orderedPreprocessingIds = new ArrayList<>();
        Map<String, ObjectNode> sealedSelection = new LinkedHashMap<>();
        ArrayNode files = JSON.createArrayNode();
        long legacyCalls = 0;
        long legacyIncludes = 0;
        long callFacts = 0;
        long activeCalls = 0;
        long inactiveCalls = 0;
        long conditionalCalls = 0;
        long namedCalls = 0;
        long constructorCalls = 0;
        long expressionCalls = 0;
        long conditionalRegions = 0;
        long importFacts = 0;
        long quotedImports = 0;
        long angleImports = 0;
        long computedImports = 0;
        long explicitlyUnresolvedImports = 0;
        long macroFacts = 0;
        long objectMacros = 0;
        long functionMacros = 0;
        long activeMacros = 0;
        long inactiveMacros = 0;
        long conditionalMacros = 0;
        long explicitlyUnresolvedMacros = 0;
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
            assertEquals("1.1.0", evidence.path("version").asText());
            assertEquals(first.sourceSha256(), evidence.path("rawSourceSha256").asText());
            assertEquals(source, evidence.path("decodedText").asText(),
                    "sealed decoded source mismatch in " + sourcePath);
            verifyCompleteness(evidence, sourcePath);
            JsonNode configuredPreprocessing = evidence.path("configuredPreprocessing");
            assertEquals("1.0.0", configuredPreprocessing.path("version").asText());
            assertEquals("unresolved", configuredPreprocessing.path("status").asText());
            assertEquals("unresolved", configuredPreprocessing.path("trust").asText());
            JsonNode build = configuredPreprocessing.path("build");
            assertEquals("unresolved", build.path("status").asText());
            assertEquals(1, build.path("population").asInt());
            assertEquals(0, build.path("emitted").asInt());
            assertEquals(1, build.path("explicitlyUnresolved").asInt());
            assertTrue(build.path("commandOccurrenceIds").isEmpty());
            assertEquals(List.of("insufficient_compilation_database"),
                    strings(build.path("reasons")));
            JsonNode trace = configuredPreprocessing.path("trace");
            assertEquals("unresolved", trace.path("status").asText());
            assertEquals(1, trace.path("population").asInt());
            assertEquals(0, trace.path("emitted").asInt());
            assertEquals(1, trace.path("explicitlyUnresolved").asInt());
            assertEquals(List.of("insufficient_preprocessing_build_context"),
                    strings(trace.path("reasons")));
            for (JsonNode evidenceId : build.path("unresolvedEvidenceIds")) {
                String id = evidenceId.asText();
                assertEquals(64, id.length(), "invalid build evidence ID in " + sourcePath);
                assertTrue(globalFactIds.add(id), "duplicate canonical evidence ID: " + id);
                orderedPreprocessingIds.add(id);
            }
            for (JsonNode evidenceId : trace.path("evidenceIds")) {
                String id = evidenceId.asText();
                assertEquals(64, id.length(), "invalid trace evidence ID in " + sourcePath);
                assertTrue(globalFactIds.add(id), "duplicate canonical evidence ID: " + id);
                orderedPreprocessingIds.add(id);
            }
            if (exportRoot != null) {
                exportAnalyzerFixture(exportRoot, sourceBytes, first, evidence);
            }

            List<JsonNode> calls = new ArrayList<>();
            List<JsonNode> imports = new ArrayList<>();
            List<JsonNode> macros = new ArrayList<>();
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
                } else if ("import".equals(fact.path("kind").asText())) {
                    verifyImportSubranges(source, fact, sourcePath);
                    imports.add(fact);
                    importFacts++;
                    orderedImportIds.add(factId);
                    JsonNode importEntry = fact.path("payload").path("entries").get(0);
                    switch (importEntry.path("targetKind").asText()) {
                        case "quoted" -> quotedImports++;
                        case "angle" -> angleImports++;
                        case "computed" -> computedImports++;
                        default -> throw new AssertionError("invalid import target kind: " + fact);
                    }
                } else if ("conditional_region".equals(fact.path("kind").asText())) {
                    regions.add(fact);
                    conditionalRegions++;
                } else if ("macro".equals(fact.path("kind").asText())) {
                    verifyMacroSubranges(source, fact, sourcePath);
                    macros.add(fact);
                    macroFacts++;
                    orderedMacroIds.add(factId);
                    switch (fact.path("payload").path("macroKind").asText()) {
                        case "object" -> objectMacros++;
                        case "function" -> functionMacros++;
                        default -> throw new AssertionError("invalid macro kind: " + fact);
                    }
                    switch (presence(evidence, fact).path("status").asText()) {
                        case "active" -> activeMacros++;
                        case "inactive" -> inactiveMacros++;
                        case "conditional", "unknown" -> conditionalMacros++;
                        default -> throw new AssertionError("invalid macro presence: " + fact);
                    }
                }
            }

            JsonNode macroCompleteness = completeness(evidence, "macro");
            explicitlyUnresolvedMacros += macroCompleteness
                    .path("explicitlyUnresolved").asLong();
            JsonNode importCompleteness = completeness(evidence, "import");
            explicitlyUnresolvedImports += importCompleteness
                    .path("explicitlyUnresolved").asLong();

            List<JsonNode> includeNodes = nodes(root, "INCLUDE");
            assertEquals(imports.size(), includeNodes.size(),
                    "grammar import facts and legacy INCLUDE projection diverged in " + sourcePath);
            for (int i = 0; i < imports.size(); i++) {
                JsonNode fact = imports.get(i);
                JsonNode include = includeNodes.get(i);
                JsonNode targetRange = fact.path("payload").path("entries").get(0)
                        .path("targetRange");
                assertEquals(slice(source, targetRange), include.path("name").asText(),
                        "legacy INCLUDE target diverged from grammar fact in " + sourcePath);
                assertEquals(lineOfOffset(source, rangeStart(fact.path("range"))),
                        include.path("startLine").asInt(),
                        "legacy INCLUDE start line diverged from grammar fact in " + sourcePath);
                assertEquals(lineOfOffset(source, Math.max(rangeStart(fact.path("range")),
                                rangeEnd(fact.path("range")) - 1)),
                        include.path("endLine").asInt(),
                        "legacy INCLUDE end line diverged from grammar fact in " + sourcePath);
                selectImport(sealedSelection, fact, evidence, sourceId, source,
                        "complete_import_population");
            }
            for (JsonNode region : regions) {
                selectConditionalRegion(sealedSelection, region, evidence, sourceId, source,
                        "complete_conditional_region_population");
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
            macros.sort(Comparator.comparingInt(macro -> rangeStart(macro.path("range"))));
            if (!macros.isEmpty()) {
                selectMacro(sealedSelection, macros.get(0), evidence, sourceId, source,
                        "first_macro_in_source");
                selectMacro(sealedSelection, macros.get(macros.size() - 1), evidence,
                        sourceId, source, "last_macro_in_source");
            }
            for (JsonNode macro : macros) {
                JsonNode payload = macro.path("payload");
                if ("function".equals(payload.path("macroKind").asText())) {
                    selectMacro(sealedSelection, macro, evidence, sourceId, source,
                            "function_like_macro");
                }
                if (payload.path("variadic").asBoolean()) {
                    selectMacro(sealedSelection, macro, evidence, sourceId, source,
                            "variadic_macro");
                }
                if (payload.path("replacementRange").isNull()) {
                    selectMacro(sealedSelection, macro, evidence, sourceId, source,
                            "empty_replacement");
                }
                if (!"active".equals(presence(evidence, macro).path("status").asText())) {
                    selectMacro(sealedSelection, macro, evidence, sourceId, source,
                            "non_active_presence");
                }
                String directive = slice(source, macro.path("range"));
                if (directive.contains("\n") || directive.contains("\r")) {
                    selectMacro(sealedSelection, macro, evidence, sourceId, source,
                            "multiline_macro");
                }
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
            long fileLegacyIncludes = includeNodes.size();
            legacyIncludes += fileLegacyIncludes;
            diagnostics += first.diagnostics().size();
            recoveries += first.antlrRecoveries();
            ObjectNode fileRow = files.addObject();
            fileRow.put("sourceId", evidence.path("sourceId").asText());
            fileRow.put("rawSourceSha256", first.sourceSha256());
            fileRow.put("parseStatus", evidence.path("parseStatus").asText());
            fileRow.put("legacyFunctionCallNodes", fileLegacyCalls);
            fileRow.put("legacyIncludeNodes", fileLegacyIncludes);
            fileRow.put("callFacts", calls.size());
            fileRow.put("importFacts", imports.size());
            fileRow.put("explicitlyUnresolvedImports",
                    importCompleteness.path("explicitlyUnresolved").asLong());
            fileRow.put("macroFacts", macros.size());
            fileRow.put("explicitlyUnresolvedMacros",
                    macroCompleteness.path("explicitlyUnresolved").asLong());
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
        assertFalse(orderedImportIds.isEmpty(), "actual corpus emitted no import facts");
        assertFalse(orderedMacroIds.isEmpty(), "actual corpus emitted no macro facts");
        assertEquals(importFacts, legacyIncludes,
                "actual grammar import population and legacy INCLUDE population diverged");
        assertEquals(importFacts, quotedImports + angleImports + computedImports,
                "import target syntax partition is incomplete");
        assertEquals(macroFacts, objectMacros + functionMacros,
                "macro syntax partition is incomplete");
        assertEquals(macroFacts, activeMacros + inactiveMacros + conditionalMacros,
                "macro presence partition is incomplete");
        assertEquals(46, orderedPreprocessingIds.size(),
                "configured preprocessing evidence population changed");
        assertEquals(orderedFactIds.size() + orderedPreprocessingIds.size(),
                globalFactIds.size(), "global fact/evidence ID set accounting mismatch");

        ObjectNode report = JSON.createObjectNode();
        report.put("contractVersion", "1.1.0");
        report.put("corpus", corpus.toString().replace('\\', '/'));
        report.put("sourceFiles", sources.size());
        report.put("sourceInventorySha256", sourceInventoryHash(workspace.sourceDir(), sources));
        report.put("deterministicReplay", true);
        report.put("legacyFunctionCallNodes", legacyCalls);
        report.put("legacyIncludeNodes", legacyIncludes);
        report.put("callFacts", callFacts);
        report.put("activeCalls", activeCalls);
        report.put("inactiveCalls", inactiveCalls);
        report.put("conditionalOrUnknownCalls", conditionalCalls);
        report.put("namedCalls", namedCalls);
        report.put("constructorCalls", constructorCalls);
        report.put("expressionCalls", expressionCalls);
        report.put("conditionalRegions", conditionalRegions);
        report.put("importFacts", importFacts);
        report.put("quotedImports", quotedImports);
        report.put("angleImports", angleImports);
        report.put("computedImports", computedImports);
        report.put("explicitlyUnresolvedImports", explicitlyUnresolvedImports);
        report.put("macroFacts", macroFacts);
        report.put("objectMacros", objectMacros);
        report.put("functionMacros", functionMacros);
        report.put("activeMacros", activeMacros);
        report.put("inactiveMacros", inactiveMacros);
        report.put("conditionalOrUnknownMacros", conditionalMacros);
        report.put("explicitlyUnresolvedMacros", explicitlyUnresolvedMacros);
        report.put("diagnostics", diagnostics);
        report.put("antlrRecoveries", recoveries);
        report.put("factIdLedgerSha256", ledgerHash(orderedFactIds));
        report.put("callFactIdLedgerSha256", ledgerHash(orderedCallIds));
        report.put("importFactIdLedgerSha256", ledgerHash(orderedImportIds));
        report.put("macroFactIdLedgerSha256", ledgerHash(orderedMacroIds));
        report.put("configuredPreprocessingSourcePopulation", sources.size());
        report.put("configuredPreprocessingStatus", "unresolved");
        report.put("configuredPreprocessingTrust", "unresolved");
        report.put("configuredPreprocessingEvidenceIds", orderedPreprocessingIds.size());
        report.put("configuredPreprocessingEvidenceIdLedgerSha256",
                ledgerHash(orderedPreprocessingIds));
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

    private static void exportAnalyzerFixture(Path root, byte[] sourceBytes,
                                               RawParseResult parsed,
                                               JsonNode evidence) throws Exception {
        String sourceId = evidence.path("sourceId").asText();
        assertFalse(sourceId.isBlank(), "cannot export blank sourceId");
        Path sourcePath = root.resolve("source").resolve(sourceId).normalize();
        Path astPath = root.resolve("analysis").resolve(sourceId + ".json").normalize();
        Path diagnosticPath = root.resolve("diagnostics")
                .resolve(sourceId + ".parse.json").normalize();
        for (Path path : List.of(sourcePath, astPath, diagnosticPath)) {
            assertTrue(path.startsWith(root), "export path escaped run root: " + path);
            Files.createDirectories(path.getParent());
        }
        Files.write(sourcePath, sourceBytes, StandardOpenOption.CREATE_NEW);
        Files.writeString(astPath, parsed.astJson(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW);

        ObjectNode diagnostic = JSON.createObjectNode();
        diagnostic.put("schemaVersion", "1.1.0");
        diagnostic.put("sourcePath", sourceId);
        diagnostic.put("sourceSha256", parsed.sourceSha256());
        diagnostic.put("language", parsed.language());
        diagnostic.put("status", "EXACT");
        ObjectNode firstPass = diagnostic.putObject("firstPass");
        firstPass.put("antlrRecoveries", parsed.antlrRecoveries());
        ObjectNode coverage = firstPass.putObject("coverage");
        coverage.put("knownAndComplete", parsed.coverage().isKnownAndComplete());
        coverage.putArray("missingDeclarations");
        ObjectNode summary = diagnostic.putObject("summary");
        summary.put("lexerErrors", parsed.diagnostics().stream()
                .filter(item -> "LEXER".equals(item.phase().name())).count());
        summary.put("parserErrors", parsed.diagnostics().stream()
                .filter(item -> "PARSER".equals(item.phase().name())).count());
        Files.writeString(diagnosticPath, JSON.writeValueAsString(diagnostic),
                StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
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

    private static List<JsonNode> nodes(JsonNode root, String type) {
        List<JsonNode> result = new ArrayList<>();
        collectNodes(root, type, result);
        return result;
    }

    private static void collectNodes(JsonNode node, String type, List<JsonNode> result) {
        if (type.equals(node.path("type").asText())) result.add(node);
        for (JsonNode child : node.path("children")) collectNodes(child, type, result);
    }

    private static int lineOfOffset(String source, int codePointOffset) {
        int[] codePoints = source.codePoints().toArray();
        int line = 1;
        for (int i = 0; i < codePointOffset && i < codePoints.length; i++) {
            if (codePoints[i] == '\n') line++;
            else if (codePoints[i] == '\r') {
                line++;
                if (i + 1 < codePointOffset && codePoints[i + 1] == '\n') i++;
            }
        }
        return line;
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

    private static void verifyMacroSubranges(String source, JsonNode macro, Path path) {
        int macroStart = rangeStart(macro.path("range"));
        int macroEnd = rangeEnd(macro.path("range"));
        JsonNode payload = macro.path("payload");
        JsonNode nameRange = payload.path("nameRange");
        assertSubrange(macroStart, macroEnd, nameRange, "macro name", path);
        assertEquals(slice(source, nameRange), payload.path("terminalName").asText(),
                "macro terminal name is not its exact grammar range in " + path);
        int previousEnd = rangeEnd(nameRange);
        for (JsonNode parameter : payload.path("parameterRanges")) {
            assertSubrange(macroStart, macroEnd, parameter, "macro parameter", path);
            assertTrue(rangeStart(parameter) >= previousEnd,
                    "macro parameter order mismatch in " + path);
            previousEnd = rangeEnd(parameter);
        }
        JsonNode replacement = payload.path("replacementRange");
        if (!replacement.isNull()) {
            assertSubrange(macroStart, macroEnd, replacement, "macro replacement", path);
            assertTrue(rangeStart(replacement) >= previousEnd,
                    "macro replacement precedes its declaration in " + path);
        }
        assertTrue(Set.of("object", "function")
                .contains(payload.path("macroKind").asText()));
        assertFalse("object".equals(payload.path("macroKind").asText())
                        && (payload.path("variadic").asBoolean()
                                || !payload.path("parameterRanges").isEmpty()),
                "object macro claimed function parameters in " + path);
    }

    private static void verifyImportSubranges(String source, JsonNode fact, Path path) {
        int directiveStart = rangeStart(fact.path("range"));
        int directiveEnd = rangeEnd(fact.path("range"));
        JsonNode payload = fact.path("payload");
        assertEquals("include", payload.path("directiveKind").asText(),
                "invalid import directive kind in " + path);
        assertEquals(1, payload.path("entries").size(),
                "C include must own exactly one binding entry in " + path);
        JsonNode entry = payload.path("entries").get(0);
        assertTrue(Set.of("quoted", "angle", "computed")
                        .contains(entry.path("targetKind").asText()),
                "invalid import target kind in " + path + ": " + fact);
        JsonNode targetRange = entry.path("targetRange");
        assertSubrange(directiveStart, directiveEnd, targetRange, "import target", path);
        assertFalse(slice(source, targetRange).isBlank(),
                "empty import target range in " + path);
        entry.path("pathComponentRanges").forEach(component ->
                assertSubrange(directiveStart, directiveEnd, component,
                        "import path component", path));
    }

    private static void assertSubrange(int outerStart, int outerEnd, JsonNode range,
                                       String label, Path path) {
        assertTrue(rangeStart(range) >= outerStart && rangeEnd(range) <= outerEnd,
                label + " is outside macro range in " + path + ": " + range);
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

    private static void selectMacro(Map<String, ObjectNode> selection, JsonNode fact,
                                    JsonNode evidence, String sourceId, String source,
                                    String reason) {
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
            display.put("macroDirective", slice(source, fact.path("range")));
            display.put("terminalName", slice(source,
                    fact.path("payload").path("nameRange")));
            ArrayNode parameters = display.putArray("parameters");
            fact.path("payload").path("parameterRanges").forEach(range ->
                    parameters.add(slice(source, range)));
            JsonNode replacement = fact.path("payload").path("replacementRange");
            if (replacement.isNull()) display.putNull("replacement");
            else display.put("replacement", slice(source, replacement));
            row.set("selectionReasons", JSON.createArrayNode());
            return row;
        });
        ArrayNode reasons = (ArrayNode) selected.path("selectionReasons");
        boolean exists = false;
        for (JsonNode existing : reasons) exists |= reason.equals(existing.asText());
        if (!exists) reasons.add(reason);
    }

    private static void selectImport(Map<String, ObjectNode> selection, JsonNode fact,
                                     JsonNode evidence, String sourceId, String source,
                                     String reason) {
        selectRangeFact(selection, fact, evidence, sourceId, reason, display -> {
            display.put("directive", slice(source, fact.path("range")));
            display.put("target", slice(source, fact.path("payload").path("entries")
                    .get(0).path("targetRange")));
        });
    }

    private static void selectConditionalRegion(Map<String, ObjectNode> selection, JsonNode fact,
                                                JsonNode evidence, String sourceId, String source,
                                                String reason) {
        selectRangeFact(selection, fact, evidence, sourceId, reason, display ->
                display.put("conditionalRegion", slice(source, fact.path("range"))));
    }

    private static void selectRangeFact(Map<String, ObjectNode> selection, JsonNode fact,
                                        JsonNode evidence, String sourceId, String reason,
                                        java.util.function.Consumer<ObjectNode> addDisplay) {
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
            addDisplay.accept(display);
            row.set("selectionReasons", JSON.createArrayNode());
            return row;
        });
        ArrayNode reasons = (ArrayNode) selected.path("selectionReasons");
        boolean exists = false;
        for (JsonNode existing : reasons) exists |= reason.equals(existing.asText());
        if (!exists) reasons.add(reason);
    }

    private static JsonNode completeness(JsonNode evidence, String kind) {
        List<JsonNode> rows = new ArrayList<>();
        evidence.path("completeness").forEach(row -> {
            if (kind.equals(row.path("kind").asText())) rows.add(row);
        });
        assertEquals(1, rows.size(), "missing or duplicate completeness row for " + kind);
        return rows.get(0);
    }

    private static JsonNode presence(JsonNode evidence, JsonNode fact) {
        int reference = fact.path("presenceRef").asInt(-1);
        JsonNode presences = evidence.path("presences");
        assertTrue(reference >= 0 && reference < presences.size(),
                "invalid presenceRef: " + fact);
        return presences.get(reference);
    }

    private static List<String> strings(JsonNode values) {
        List<String> result = new ArrayList<>();
        values.forEach(value -> result.add(value.asText()));
        return result;
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
