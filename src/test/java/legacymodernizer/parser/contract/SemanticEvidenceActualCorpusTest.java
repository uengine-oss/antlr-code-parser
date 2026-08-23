package legacymodernizer.parser.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import org.junit.jupiter.api.Disabled;
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
@Disabled("sealed IR 1.3 Strategy A 312/312 history; C-080 forbids rerun/reanalysis")
class SemanticEvidenceActualCorpusTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    void actualCorpusIsRangeExactReplayStableAndFullyAccounted() throws Exception {
        String configuredCorpus = System.getProperty("parser.evidence.corpus", "");
        String configuredReport = System.getProperty("parser.evidence.report", "");
        String configuredExport = System.getProperty("parser.evidence.export", "");
        String configuredAstBaseline = System.getProperty(
                "parser.evidence.astBaseline", "");
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
        Path astBaselineRoot = configuredAstBaseline.isBlank() ? null
                : Path.of(configuredAstBaseline).toAbsolutePath().normalize();
        if (astBaselineRoot != null) {
            String normalizedBaseline = astBaselineRoot.toString().replace('\\', '/');
            assertTrue(normalizedBaseline.contains(
                            "/specs/131-cross-node-semantic-grounding/_runs/framework/"),
                    "actual AST baseline must stay inside spec 131 _runs/framework: "
                            + astBaselineRoot);
            assertTrue(Files.isDirectory(astBaselineRoot),
                    "actual AST baseline does not exist: " + astBaselineRoot);
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
        List<String> orderedCallBindingRows = new ArrayList<>();
        List<String> orderedCallableIds = new ArrayList<>();
        List<String> orderedImportIds = new ArrayList<>();
        List<String> orderedMacroIds = new ArrayList<>();
        List<String> orderedSymbolIds = new ArrayList<>();
        List<String> orderedPreprocessingIds = new ArrayList<>();
        Map<String, ObjectNode> sealedSelection = new LinkedHashMap<>();
        List<ObjectNode> sealedSymbolCandidates = new ArrayList<>();
        List<ObjectNode> sealedCallBindingCandidates = new ArrayList<>();
        ArrayNode files = JSON.createArrayNode();
        long legacyCalls = 0;
        long legacyFunctions = 0;
        long legacyIncludes = 0;
        long callFacts = 0;
        long activeCalls = 0;
        long inactiveCalls = 0;
        long conditionalCalls = 0;
        long namedCalls = 0;
        long constructorCalls = 0;
        long expressionCalls = 0;
        long declarationBoundCalls = 0;
        long directDefinitionCalls = 0;
        long compatibleDefinitionCalls = 0;
        long externalCalls = 0;
        long configurationDependentCalls = 0;
        long dynamicCalls = 0;
        long ambiguousCalls = 0;
        long unsupportedCalls = 0;
        long callableFacts = 0;
        long callableDeclarations = 0;
        long callableDefinitions = 0;
        long sourceFileCallables = 0;
        long corpusCallables = 0;
        long unavailableCallableCompatibility = 0;
        long sourceFileCallableCompatibility = 0;
        long corpusCallableCompatibility = 0;
        long configurationCallableCompatibility = 0;
        long configurationDependentDefinitions = 0;
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
        long symbolFacts = 0;
        long symbolDefinitions = 0;
        long symbolLookups = 0;
        long resolvedSymbolLookups = 0;
        long unresolvedSymbolLookups = 0;
        long diagnostics = 0;
        long recoveries = 0;
        long baselineComparedFiles = 0;
        long baselineTypedefDelta = 0;

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
            assertEquals("1.3.0", evidence.path("version").asText());
            assertEquals(first.sourceSha256(), evidence.path("rawSourceSha256").asText());
            assertEquals(source, evidence.path("decodedText").asText(),
                    "sealed decoded source mismatch in " + sourcePath);
            if (astBaselineRoot != null) {
                Path baselinePath = astBaselineRoot.resolve(sourcePath.getFileName() + ".json");
                assertTrue(Files.isRegularFile(baselinePath),
                        "missing accepted AST baseline for " + sourcePath);
                JsonNode baseline = JSON.readTree(Files.readString(
                        baselinePath, StandardCharsets.UTF_8));
                assertEquals(childrenWithoutType(baseline, "TYPEDEF"),
                        childrenWithoutType(root, "TYPEDEF"),
                        "non-TYPEDEF compatibility AST changed for " + sourcePath);
                baselineComparedFiles++;
                baselineTypedefDelta += countNodes(root, "TYPEDEF")
                        - countNodes(baseline, "TYPEDEF");
            }
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
            List<JsonNode> callables = new ArrayList<>();
            List<JsonNode> imports = new ArrayList<>();
            List<JsonNode> macros = new ArrayList<>();
            List<JsonNode> regions = new ArrayList<>();
            List<JsonNode> symbols = new ArrayList<>();
            Set<String> definitionIds = new HashSet<>();
            Map<String, JsonNode> factsById = new LinkedHashMap<>();
            evidence.path("facts").forEach(fact -> {
                String factId = fact.path("factId").asText();
                assertFalse(factId.isBlank(), "fact omitted identity in " + sourcePath);
                assertNull(factsById.put(factId, fact),
                        "duplicate fact identity in " + sourcePath + ": " + factId);
            });
            evidence.path("facts").forEach(fact -> {
                if ("symbol".equals(fact.path("kind").asText())
                        && "definition".equals(fact.path("payload").path("role").asText())) {
                    definitionIds.add(fact.path("factId").asText());
                }
            });
            String sourceId = evidence.path("sourceId").asText();
            for (JsonNode fact : evidence.path("facts")) {
                String factId = fact.path("factId").asText();
                assertEquals(64, factId.length(), "invalid fact ID in " + sourcePath);
                assertTrue(globalFactIds.add(factId), "duplicate canonical fact ID: " + factId);
                orderedFactIds.add(factId);
                verifyExactSlice(source, fact, sourcePath);
                if ("call".equals(fact.path("kind").asText())) {
                    verifyCallSubranges(source, fact, sourcePath);
                    verifyCallBinding(fact, factsById, evidence, source, sourcePath);
                    orderedCallBindingRows.add(factId + "\0"
                            + fact.path("payload").path("binding").toString());
                    calls.add(fact);
                    callFacts++;
                    switch (fact.path("payload").path("calleeKind").asText()) {
                        case "named" -> namedCalls++;
                        case "constructor" -> constructorCalls++;
                        case "expression" -> expressionCalls++;
                        default -> throw new AssertionError("invalid callee kind: " + fact);
                    }
                    switch (fact.path("payload").path("binding")
                            .path("status").asText()) {
                        case "declaration_bound" -> declarationBoundCalls++;
                        case "external" -> externalCalls++;
                        case "configuration_dependent" -> configurationDependentCalls++;
                        case "dynamic" -> dynamicCalls++;
                        case "ambiguous" -> ambiguousCalls++;
                        case "unsupported" -> unsupportedCalls++;
                        default -> throw new AssertionError("invalid call binding status: " + fact);
                    }
                    switch (fact.path("payload").path("binding")
                            .path("resolutionMode").asText()) {
                        case "direct_definition" -> directDefinitionCalls++;
                        case "compatible_definition" -> compatibleDefinitionCalls++;
                        case "none" -> { }
                        default -> throw new AssertionError(
                                "invalid call binding resolution mode: " + fact);
                    }
                    orderedCallIds.add(factId);
                    sealedCallBindingCandidates.add(callBindingCandidate(
                            fact, evidence, sourceId, source));
                    switch (presence(evidence, fact).path("status").asText()) {
                        case "active" -> activeCalls++;
                        case "inactive" -> inactiveCalls++;
                        case "conditional", "unknown" -> conditionalCalls++;
                        default -> throw new AssertionError("invalid call presence: " + fact);
                    }
                } else if ("callable".equals(fact.path("kind").asText())) {
                    verifyCallableFact(fact, sourcePath);
                    callables.add(fact);
                    callableFacts++;
                    orderedCallableIds.add(factId);
                    if ("definition".equals(fact.path("payload").path("role").asText())) {
                        callableDefinitions++;
                    } else {
                        callableDeclarations++;
                    }
                    if ("source_file".equals(fact.path("payload")
                            .path("targetScope").asText())) {
                        sourceFileCallables++;
                    } else {
                        corpusCallables++;
                    }
                    if ("unavailable".equals(fact.path("payload")
                            .path("compatibilityStatus").asText())) {
                        unavailableCallableCompatibility++;
                    }
                    switch (fact.path("payload").path("compatibilityScope").asText()) {
                        case "source_file" -> sourceFileCallableCompatibility++;
                        case "corpus" -> corpusCallableCompatibility++;
                        case "configuration" -> configurationCallableCompatibility++;
                        case "unavailable" -> { }
                        default -> throw new AssertionError(
                                "invalid callable compatibility scope: " + fact);
                    }
                    if ("configuration_dependent".equals(fact.path("payload")
                            .path("definitionStatus").asText())) {
                        configurationDependentDefinitions++;
                    }
                    sealedCallBindingCandidates.add(callableCandidate(
                            fact, evidence, sourceId, source));
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
                } else if ("symbol".equals(fact.path("kind").asText())) {
                    verifySymbolFact(fact, definitionIds, sourcePath);
                    symbols.add(fact);
                    symbolFacts++;
                    orderedSymbolIds.add(factId);
                    String role = fact.path("payload").path("role").asText();
                    if ("definition".equals(role)) {
                        symbolDefinitions++;
                    } else {
                        symbolLookups++;
                        if ("resolved".equals(fact.path("payload")
                                .path("resolutionStatus").asText())) {
                            resolvedSymbolLookups++;
                        } else {
                            unresolvedSymbolLookups++;
                        }
                    }
                    sealedSymbolCandidates.add(symbolCandidate(
                            fact, evidence, sourceId, source));
                }
            }

            JsonNode macroCompleteness = completeness(evidence, "macro");
            explicitlyUnresolvedMacros += macroCompleteness
                    .path("explicitlyUnresolved").asLong();
            JsonNode importCompleteness = completeness(evidence, "import");
            explicitlyUnresolvedImports += importCompleteness
                    .path("explicitlyUnresolved").asLong();
            JsonNode symbolCompleteness = completeness(evidence, "symbol");
            JsonNode bindingCompleteness = completeness(evidence, "call_binding");
            assertEquals(calls.size(), bindingCompleteness.path("population").asInt());
            assertEquals(calls.size(), bindingCompleteness.path("emitted").asInt());
            JsonNode callableCompleteness = completeness(evidence, "callable");
            assertEquals(callables.size(), callableCompleteness.path("population").asInt());
            assertEquals(callables.size(), callableCompleteness.path("emitted").asInt());
            List<String> unresolvedIds = symbols.stream()
                    .filter(fact -> "lookup".equals(fact.path("payload")
                            .path("role").asText()))
                    .filter(fact -> "unresolved".equals(fact.path("payload")
                            .path("resolutionStatus").asText()))
                    .map(fact -> fact.path("factId").asText())
                    .toList();
            assertEquals(unresolvedIds,
                    strings(symbolCompleteness.path("unresolvedFactIds")),
                    "symbol unresolved ledger diverged in " + sourcePath);

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
            long fileLegacyFunctions = countNodes(root, "FUNCTION");
            legacyFunctions += fileLegacyFunctions;
            long fileLegacyIncludes = includeNodes.size();
            legacyIncludes += fileLegacyIncludes;
            diagnostics += first.diagnostics().size();
            recoveries += first.antlrRecoveries();
            ObjectNode fileRow = files.addObject();
            fileRow.put("sourceId", evidence.path("sourceId").asText());
            fileRow.put("rawSourceSha256", first.sourceSha256());
            fileRow.put("parseStatus", evidence.path("parseStatus").asText());
            fileRow.put("legacyFunctionCallNodes", fileLegacyCalls);
            fileRow.put("legacyFunctionNodes", fileLegacyFunctions);
            fileRow.put("legacyIncludeNodes", fileLegacyIncludes);
            fileRow.put("callFacts", calls.size());
            fileRow.put("callableFacts", callables.size());
            fileRow.put("callableDefinitions", callables.stream()
                    .filter(fact -> "definition".equals(fact.path("payload")
                            .path("role").asText())).count());
            fileRow.put("importFacts", imports.size());
            fileRow.put("explicitlyUnresolvedImports",
                    importCompleteness.path("explicitlyUnresolved").asLong());
            fileRow.put("macroFacts", macros.size());
            fileRow.put("explicitlyUnresolvedMacros",
                    macroCompleteness.path("explicitlyUnresolved").asLong());
            fileRow.put("symbolFacts", symbols.size());
            fileRow.put("symbolDefinitions", symbols.stream()
                    .filter(fact -> "definition".equals(fact.path("payload")
                            .path("role").asText())).count());
            fileRow.put("symbolLookups", symbols.stream()
                    .filter(fact -> "lookup".equals(fact.path("payload")
                            .path("role").asText())).count());
            fileRow.put("unresolvedSymbolLookups", unresolvedIds.size());
            fileRow.put("diagnostics", first.diagnostics().size());
            fileRow.put("antlrRecoveries", first.antlrRecoveries());
            fileRow.put("firstParseElapsedMillis", first.elapsedMillis());
            fileRow.put("repeatedParseElapsedMillis", repeated.elapsedMillis());
        }

        assertEquals(callFacts, activeCalls + inactiveCalls + conditionalCalls,
                "call presence partition is incomplete");
        assertEquals(callFacts, namedCalls + constructorCalls + expressionCalls,
                "callee syntax partition is incomplete");
        assertEquals(callFacts, declarationBoundCalls + externalCalls
                        + configurationDependentCalls + dynamicCalls
                        + ambiguousCalls + unsupportedCalls,
                "call binding status partition is incomplete");
        assertEquals(declarationBoundCalls,
                directDefinitionCalls + compatibleDefinitionCalls,
                "declaration-bound resolution mode partition is incomplete");
        assertFalse(orderedCallIds.isEmpty(), "actual corpus emitted no call facts");
        assertFalse(orderedCallableIds.isEmpty(), "actual corpus emitted no callable facts");
        assertEquals(callableFacts, callableDeclarations + callableDefinitions,
                "callable role partition is incomplete");
        assertEquals(callableFacts, sourceFileCallables + corpusCallables,
                "callable target scope partition is incomplete");
        assertEquals(callableFacts, sourceFileCallableCompatibility
                        + corpusCallableCompatibility
                        + configurationCallableCompatibility
                        + unavailableCallableCompatibility,
                "callable compatibility scope partition is incomplete");
        assertEquals(legacyCalls, callFacts,
                "legacy AST and canonical call populations diverged");
        assertEquals(legacyFunctions, callableDefinitions,
                "legacy AST and canonical callable definition populations diverged");
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
        assertFalse(orderedSymbolIds.isEmpty(), "actual corpus emitted no symbol facts");
        assertEquals(symbolFacts, symbolDefinitions + symbolLookups,
                "symbol role partition is incomplete");
        assertEquals(symbolLookups, resolvedSymbolLookups + unresolvedSymbolLookups,
                "symbol resolution partition is incomplete");
        assertEquals(46, orderedPreprocessingIds.size(),
                "configured preprocessing evidence population changed");
        assertEquals(orderedFactIds.size() + orderedPreprocessingIds.size(),
                globalFactIds.size(), "global fact/evidence ID set accounting mismatch");

        long bindingJoinTargetZero = 0;
        long bindingJoinTargetUnique = 0;
        long bindingJoinTargetAmbiguous = 0;
        long bindingJoinSameSourceUnique = 0;
        long bindingJoinCrossSourceUnique = 0;
        for (ObjectNode candidate : sealedCallBindingCandidates) {
            if (!"call_binding".equals(candidate.path("semanticKind").asText())
                    || !"declaration_bound".equals(candidate.path("binding")
                            .path("status").asText())) continue;
            List<String> definitionFactIds = eligibleDefinitionFactIds(
                    candidate, sealedCallBindingCandidates);
            ArrayNode eligible = candidate.putArray("eligibleDefinitionFactIds");
            definitionFactIds.forEach(eligible::add);
            switch (definitionFactIds.size()) {
                case 0 -> bindingJoinTargetZero++;
                case 1 -> {
                    bindingJoinTargetUnique++;
                    String targetSourceId = sourceIdForFact(
                            definitionFactIds.get(0), sealedCallBindingCandidates);
                    if (candidate.path("sourceId").asText().equals(targetSourceId)) {
                        bindingJoinSameSourceUnique++;
                    } else {
                        bindingJoinCrossSourceUnique++;
                    }
                }
                default -> bindingJoinTargetAmbiguous++;
            }
        }
        assertEquals(declarationBoundCalls, bindingJoinTargetZero
                        + bindingJoinTargetUnique + bindingJoinTargetAmbiguous,
                "declaration-bound definition join accounting is incomplete");
        assertEquals(bindingJoinTargetUnique,
                bindingJoinSameSourceUnique + bindingJoinCrossSourceUnique,
                "unique definition join source partition is incomplete");

        ObjectNode report = JSON.createObjectNode();
        report.put("contractVersion", "1.3.0");
        report.put("corpus", corpus.toString().replace('\\', '/'));
        report.put("sourceFiles", sources.size());
        report.put("sourceInventorySha256", sourceInventoryHash(workspace.sourceDir(), sources));
        report.put("deterministicReplay", true);
        report.put("legacyFunctionCallNodes", legacyCalls);
        report.put("legacyFunctionNodes", legacyFunctions);
        report.put("legacyIncludeNodes", legacyIncludes);
        report.put("callFacts", callFacts);
        report.put("activeCalls", activeCalls);
        report.put("inactiveCalls", inactiveCalls);
        report.put("conditionalOrUnknownCalls", conditionalCalls);
        report.put("namedCalls", namedCalls);
        report.put("constructorCalls", constructorCalls);
        report.put("expressionCalls", expressionCalls);
        report.put("declarationBoundCalls", declarationBoundCalls);
        report.put("directDefinitionCalls", directDefinitionCalls);
        report.put("compatibleDefinitionCalls", compatibleDefinitionCalls);
        report.put("externalCalls", externalCalls);
        report.put("configurationDependentCalls", configurationDependentCalls);
        report.put("dynamicCalls", dynamicCalls);
        report.put("ambiguousCalls", ambiguousCalls);
        report.put("unsupportedCalls", unsupportedCalls);
        report.put("bindingJoinTargetZero", bindingJoinTargetZero);
        report.put("bindingJoinTargetUnique", bindingJoinTargetUnique);
        report.put("bindingJoinTargetAmbiguous", bindingJoinTargetAmbiguous);
        report.put("bindingJoinSameSourceUnique", bindingJoinSameSourceUnique);
        report.put("bindingJoinCrossSourceUnique", bindingJoinCrossSourceUnique);
        report.put("callableFacts", callableFacts);
        report.put("callableDeclarations", callableDeclarations);
        report.put("callableDefinitions", callableDefinitions);
        report.put("sourceFileCallables", sourceFileCallables);
        report.put("corpusCallables", corpusCallables);
        report.put("unavailableCallableCompatibility",
                unavailableCallableCompatibility);
        report.put("sourceFileCallableCompatibility",
                sourceFileCallableCompatibility);
        report.put("corpusCallableCompatibility", corpusCallableCompatibility);
        report.put("configurationCallableCompatibility",
                configurationCallableCompatibility);
        report.put("configurationDependentDefinitions",
                configurationDependentDefinitions);
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
        report.put("symbolFacts", symbolFacts);
        report.put("symbolDefinitions", symbolDefinitions);
        report.put("symbolLookups", symbolLookups);
        report.put("resolvedSymbolLookups", resolvedSymbolLookups);
        report.put("unresolvedSymbolLookups", unresolvedSymbolLookups);
        report.put("diagnostics", diagnostics);
        report.put("antlrRecoveries", recoveries);
        report.put("acceptedAstComparedFiles", baselineComparedFiles);
        report.put("acceptedAstTypedefDelta", baselineTypedefDelta);
        report.put("factIdLedgerSha256", ledgerHash(orderedFactIds));
        report.put("callFactIdLedgerSha256", ledgerHash(orderedCallIds));
        report.put("callBindingLedgerSha256", ledgerHash(orderedCallBindingRows));
        report.put("callableFactIdLedgerSha256", ledgerHash(orderedCallableIds));
        report.put("importFactIdLedgerSha256", ledgerHash(orderedImportIds));
        report.put("macroFactIdLedgerSha256", ledgerHash(orderedMacroIds));
        report.put("symbolFactIdLedgerSha256", ledgerHash(orderedSymbolIds));
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
        ArrayNode symbolSelection = report.putArray("sealedSymbolDirectJudgmentPopulation");
        ObjectNode symbolStrata = report.putObject("sealedSymbolDirectJudgmentStrata");
        for (ObjectNode candidate : stratifiedSymbolSelection(sealedSymbolCandidates, 40)) {
            candidate.remove("selectionRank");
            symbolSelection.add(candidate);
            String stratum = candidate.path("selectionReason").asText();
            symbolStrata.put(stratum, symbolStrata.path(stratum).asInt() + 1);
        }
        ArrayNode callBindingSelection = report.putArray(
                "sealedCallBindingDirectJudgmentPopulation");
        ObjectNode callBindingStrata = report.putObject(
                "sealedCallBindingDirectJudgmentStrata");
        for (ObjectNode candidate : stratifiedFactSelection(
                sealedCallBindingCandidates, 40)) {
            callBindingSelection.add(candidate);
            String stratum = candidate.path("selectionReason").asText();
            callBindingStrata.put(stratum, callBindingStrata.path(stratum).asInt() + 1);
        }

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
        assertEquals(Set.of("call", "call_binding", "callable", "import", "symbol",
                "literal", "assignment", "parameter", "macro",
                "embedded_language", "conditional_region"), kinds);
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

    private static ArrayNode childrenWithoutType(JsonNode root, String omittedType) {
        ArrayNode result = JSON.createArrayNode();
        for (JsonNode child : root.path("children")) {
            JsonNode filtered = nodeWithoutType(child, omittedType);
            if (filtered != null) result.add(filtered);
        }
        return result;
    }

    private static JsonNode nodeWithoutType(JsonNode node, String omittedType) {
        if (omittedType.equals(node.path("type").asText())) return null;
        ObjectNode copy = node.deepCopy();
        if (node.has("children")) {
            ArrayNode children = JSON.createArrayNode();
            for (JsonNode child : node.path("children")) {
                JsonNode filtered = nodeWithoutType(child, omittedType);
                if (filtered != null) children.add(filtered);
            }
            copy.set("children", children);
        }
        return copy;
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

    private static void verifyCallBinding(
            JsonNode call, Map<String, JsonNode> factsById,
            JsonNode evidence, String source, Path path) {
        JsonNode binding = call.path("payload").path("binding");
        assertTrue(binding.isObject(), "call omitted binding payload in " + path);
        assertFalse(binding.path("adapterSchema").asText().isBlank(),
                "call omitted adapter schema in " + path);
        String status = binding.path("status").asText();
        assertTrue(Set.of("declaration_bound", "external", "configuration_dependent",
                        "dynamic", "ambiguous", "unsupported").contains(status),
                "invalid binding status in " + path + ": " + call);
        String resolutionMode = binding.path("resolutionMode").asText();
        assertTrue(Set.of("direct_definition", "compatible_definition", "none")
                        .contains(resolutionMode),
                "invalid binding resolution mode in " + path + ": " + call);
        assertEquals("declaration_bound".equals(status), !"none".equals(resolutionMode),
                "binding status/resolution mode mismatch in " + path + ": " + call);
        assertTrue(Set.of("source_file", "corpus", "runtime")
                        .contains(binding.path("targetScope").asText()),
                "invalid binding target scope in " + path + ": " + call);
        assertTrue(Set.of("exact", "virtual", "dynamic")
                        .contains(binding.path("dispatch").asText()),
                "invalid binding dispatch in " + path + ": " + call);
        if ("declaration_bound".equals(status)) {
            JsonNode declaration = factsById.get(
                    binding.path("declarationFactId").asText());
            assertTrue(declaration != null
                            && "callable".equals(declaration.path("kind").asText()),
                    "bound call references a missing callable in " + path + ": " + call);
            assertEquals(declaration.path("payload").path("bindingKey"),
                    binding.path("bindingKey"));
            assertEquals(declaration.path("payload").path("compatibilityKey"),
                    binding.path("compatibilityKey"));
            assertEquals(declaration.path("payload").path("compatibilityScope"),
                    binding.path("compatibilityScope"));
            assertEquals(64, binding.path("bindingKey").asText().length());
            if ("compatible_definition".equals(resolutionMode)) {
                assertEquals("declaration",
                        declaration.path("payload").path("role").asText());
                assertEquals(64, binding.path("compatibilityKey").asText().length());
            } else {
                assertEquals("definition",
                        declaration.path("payload").path("role").asText());
                assertEquals("exact", declaration.path("payload")
                        .path("definitionStatus").asText());
            }
            assertTrue(binding.path("reason").isNull());
        } else {
            assertTrue(binding.path("declarationFactId").isNull());
            assertTrue(binding.path("bindingKey").isNull());
            assertTrue(binding.path("compatibilityKey").isNull());
            assertTrue(binding.path("compatibilityScope").isNull());
            assertFalse(binding.path("reason").asText().isBlank());
        }
        Set<String> candidateIds = new HashSet<>();
        List<JsonNode> candidates = new ArrayList<>();
        for (JsonNode candidateId : binding.path("candidateFactIds")) {
            assertTrue(candidateIds.add(candidateId.asText()),
                    "binding candidate is duplicated in " + path + ": " + call);
            JsonNode candidate = factsById.get(candidateId.asText());
            assertTrue(candidate != null,
                    "binding candidate fact is missing in " + path + ": " + call);
            candidates.add(candidate);
        }
        if (Set.of("declaration_bound", "external", "dynamic").contains(status)) {
            assertTrue(candidates.isEmpty(),
                    "resolved/external/dynamic binding retained candidates in "
                            + path + ": " + call);
        }
        if ("configuration_dependent".equals(status)) {
            assertFalse(candidates.isEmpty(),
                    "configuration-dependent binding omitted candidates in "
                            + path + ": " + call);
        }
        if ("ambiguous".equals(status)) {
            assertTrue(candidates.size() >= 2
                            && candidates.stream().allMatch(candidate ->
                                    "callable".equals(candidate.path("kind").asText())),
                    "ambiguous binding lacks multiple callable candidates in "
                            + path + ": " + call);
        }
        if ("unsupported".equals(status)
                && "insufficient_callable_type_compatibility".equals(
                        binding.path("reason").asText())) {
            assertFalse(candidates.isEmpty(),
                    "unsupported callable compatibility omitted its evidence in "
                            + path + ": " + call);
            String terminalName = call.path("payload").path("terminalName").asText();
            int callStart = rangeStart(call.path("range"));
            for (JsonNode candidate : candidates) {
                assertEquals("callable", candidate.path("kind").asText(),
                        "unsupported compatibility referenced a non-callable fact in "
                                + path + ": " + call);
                JsonNode payload = candidate.path("payload");
                assertEquals("unavailable", payload.path("compatibilityStatus").asText(),
                        "unsupported compatibility referenced a supported callable in "
                                + path + ": " + call);
                assertEquals(terminalName, slice(source, payload.path("nameRange")),
                        "unsupported compatibility referenced a different callable name in "
                                + path + ": " + call);
                assertEquals("active", presence(evidence, candidate).path("status").asText(),
                        "unsupported compatibility referenced a non-active callable in "
                                + path + ": " + call);
                assertTrue(payload.path("visibilityStartOffset").asInt() <= callStart
                                && callStart < rangeEnd(payload.path("visibilityRange")),
                        "unsupported compatibility referenced a non-visible callable in "
                                + path + ": " + call);
            }
        }
    }

    private static void verifyCallableFact(JsonNode fact, Path path) {
        JsonNode payload = fact.path("payload");
        assertTrue(Set.of("declaration", "definition")
                        .contains(payload.path("role").asText()),
                "invalid callable role in " + path + ": " + fact);
        assertEquals(64, payload.path("bindingKey").asText().length(),
                "invalid callable binding key in " + path + ": " + fact);
        assertTrue(Set.of("exact", "configuration_bound", "unavailable")
                        .contains(payload.path("compatibilityStatus").asText()),
                "invalid callable compatibility status in " + path + ": " + fact);
        if ("unavailable".equals(payload.path("compatibilityStatus").asText())) {
            assertTrue(payload.path("compatibilityKey").isNull());
        } else {
            assertEquals(64, payload.path("compatibilityKey").asText().length());
        }
        assertTrue(Set.of("source_file", "corpus", "configuration", "unavailable")
                        .contains(payload.path("compatibilityScope").asText()),
                "invalid callable compatibility scope in " + path + ": " + fact);
        assertEquals("unavailable".equals(
                        payload.path("compatibilityStatus").asText()),
                "unavailable".equals(payload.path("compatibilityScope").asText()),
                "callable compatibility status/scope mismatch in " + path + ": " + fact);
        assertTrue(Set.of("exact", "configuration_dependent", "not_applicable")
                        .contains(payload.path("definitionStatus").asText()),
                "invalid callable definition status in " + path + ": " + fact);
        assertTrue(rangeStart(fact.path("range"))
                        <= rangeStart(payload.path("nameRange"))
                        && rangeEnd(payload.path("nameRange"))
                        <= rangeEnd(fact.path("range")),
                "callable name escaped fact range in " + path + ": " + fact);
        assertTrue(rangeStart(payload.path("visibilityRange"))
                        <= rangeStart(fact.path("range"))
                        && rangeEnd(fact.path("range"))
                        <= rangeEnd(payload.path("visibilityRange")),
                "callable escaped visibility range in " + path + ": " + fact);
        assertTrue(payload.path("visibilityStartOffset").asInt(-1)
                        >= rangeEnd(payload.path("nameRange"))
                        && payload.path("visibilityStartOffset").asInt(-1)
                        <= rangeEnd(payload.path("visibilityRange")),
                "invalid callable visibility boundary in " + path + ": " + fact);
        if ("definition".equals(payload.path("role").asText())) {
            assertEquals(fact.path("range"), payload.path("astNodeRange"));
            assertFalse("not_applicable".equals(
                    payload.path("definitionStatus").asText()));
        } else {
            assertTrue(payload.path("astNodeRange").isNull());
            assertEquals("not_applicable",
                    payload.path("definitionStatus").asText());
        }
    }

    private static void verifySymbolFact(
            JsonNode fact, Set<String> definitionIds, Path path) {
        JsonNode payload = fact.path("payload");
        String role = payload.path("role").asText();
        assertTrue(Set.of("definition", "lookup").contains(role),
                "invalid symbol role in " + path + ": " + fact);
        if ("definition".equals(role)) {
            assertTrue(Set.of("typedef_name", "ordinary_identifier")
                    .contains(payload.path("symbolKind").asText()),
                    "invalid symbol definition kind in " + path + ": " + fact);
            assertTrue(Set.of("file", "block", "function_prototype")
                    .contains(payload.path("scopeKind").asText()),
                    "invalid symbol scope in " + path + ": " + fact);
            assertTrue(rangeStart(payload.path("scopeRange"))
                            <= rangeStart(fact.path("range"))
                            && rangeEnd(fact.path("range"))
                            <= rangeEnd(payload.path("scopeRange")),
                    "symbol definition escaped scope in " + path + ": " + fact);
            assertTrue(payload.path("visibilityStartOffset").asInt(-1)
                            >= rangeEnd(fact.path("range")),
                    "symbol visibility precedes declaration in " + path + ": " + fact);
            return;
        }

        assertEquals("type_name", payload.path("lookupKind").asText());
        assertTrue(Set.of("type_name", "ordinary_identifier")
                .contains(payload.path("parserDecision").asText()),
                "invalid parser decision in " + path + ": " + fact);
        String status = payload.path("resolutionStatus").asText();
        String provenance = payload.path("provenance").asText();
        assertTrue(Set.of("resolved", "unresolved").contains(status),
                "invalid symbol resolution in " + path + ": " + fact);
        switch (provenance) {
            case "source_declaration" -> {
                assertEquals("resolved", status);
                assertTrue(definitionIds.contains(payload.path("definitionFactId").asText()),
                        "symbol lookup references a missing definition in " + path + ": " + fact);
                assertTrue(payload.path("configuredEvidenceId").isNull());
            }
            case "grammar_context" -> {
                assertEquals("resolved", status);
                assertTrue(payload.path("definitionFactId").isNull());
                assertTrue(payload.path("configuredEvidenceId").isNull());
            }
            case "configured_preprocessing" -> {
                assertEquals("resolved", status);
                assertTrue(payload.path("definitionFactId").isNull());
                assertEquals(64, payload.path("configuredEvidenceId").asText().length());
            }
            case "unresolved_environment" -> {
                assertEquals("unresolved", status);
                assertTrue(payload.path("definitionFactId").isNull());
                assertTrue(payload.path("configuredEvidenceId").isNull());
            }
            default -> throw new AssertionError(
                    "invalid symbol provenance in " + path + ": " + fact);
        }
    }

    private static ObjectNode callBindingCandidate(
            JsonNode fact, JsonNode evidence, String sourceId, String source) {
        JsonNode binding = fact.path("payload").path("binding");
        ObjectNode row = JSON.createObjectNode();
        String bindingStratum = "binding:" + binding.path("status").asText();
        if ("declaration_bound".equals(binding.path("status").asText())) {
            bindingStratum += ":" + binding.path("resolutionMode").asText();
        }
        row.put("selectionReason", bindingStratum);
        row.put("selectionDiversityKey", fact.path("payload")
                .path("terminalName").isNull()
                        ? slice(source, fact.path("payload").path("calleeRange"))
                        : fact.path("payload").path("terminalName").asText());
        row.put("semanticKind", "call_binding");
        row.put("factId", fact.path("factId").asText());
        row.put("sourceId", sourceId);
        row.put("parseStatus", evidence.path("parseStatus").asText());
        row.set("range", fact.path("range").deepCopy());
        row.set("presence", presence(evidence, fact).deepCopy());
        row.put("sourceSlice", slice(source, fact.path("range")));
        row.put("calleeSlice", slice(source,
                fact.path("payload").path("calleeRange")));
        row.set("binding", binding.deepCopy());
        return row;
    }

    private static ObjectNode callableCandidate(
            JsonNode fact, JsonNode evidence, String sourceId, String source) {
        JsonNode payload = fact.path("payload");
        ObjectNode row = JSON.createObjectNode();
        row.put("selectionReason", String.join(":", "callable",
                payload.path("role").asText(), payload.path("targetScope").asText(),
                payload.path("compatibilityStatus").asText()));
        row.put("selectionDiversityKey", slice(source, payload.path("nameRange")));
        row.put("semanticKind", "callable");
        row.put("factId", fact.path("factId").asText());
        row.put("sourceId", sourceId);
        row.put("parseStatus", evidence.path("parseStatus").asText());
        row.set("range", fact.path("range").deepCopy());
        row.set("presence", presence(evidence, fact).deepCopy());
        row.put("sourceSlice", slice(source, fact.path("range")));
        row.put("nameSlice", slice(source, payload.path("nameRange")));
        row.set("payload", payload.deepCopy());
        return row;
    }

    private static List<String> eligibleDefinitionFactIds(
            ObjectNode call, List<ObjectNode> candidates) {
        JsonNode binding = call.path("binding");
        if ("direct_definition".equals(binding.path("resolutionMode").asText())) {
            String referenced = binding.path("declarationFactId").asText();
            return candidates.stream()
                    .filter(candidate -> referenced.equals(
                            candidate.path("factId").asText()))
                    .filter(candidate -> "callable".equals(
                            candidate.path("semanticKind").asText()))
                    .filter(candidate -> "definition".equals(
                            candidate.path("payload").path("role").asText()))
                    .filter(candidate -> "exact".equals(candidate.path("payload")
                            .path("definitionStatus").asText()))
                    .map(candidate -> candidate.path("factId").asText())
                    .toList();
        }
        List<String> result = new ArrayList<>();
        for (ObjectNode candidate : candidates) {
            if (!"callable".equals(candidate.path("semanticKind").asText())) continue;
            JsonNode payload = candidate.path("payload");
            if (!"definition".equals(payload.path("role").asText())
                    || !"exact".equals(payload.path("definitionStatus").asText())) {
                continue;
            }
            if (binding.path("adapterSchema").equals(payload.path("adapterSchema"))
                    && binding.path("bindingKey").equals(payload.path("bindingKey"))
                    && binding.path("compatibilityKey")
                            .equals(payload.path("compatibilityKey"))
                    && binding.path("compatibilityScope")
                            .equals(payload.path("compatibilityScope"))
                    && binding.path("targetScope").equals(payload.path("targetScope"))
                    && binding.path("configurationId")
                            .equals(payload.path("configurationId"))) {
                result.add(candidate.path("factId").asText());
            }
        }
        return List.copyOf(result);
    }

    private static String sourceIdForFact(
            String factId, List<ObjectNode> candidates) {
        return candidates.stream()
                .filter(candidate -> factId.equals(candidate.path("factId").asText()))
                .map(candidate -> candidate.path("sourceId").asText())
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "eligible definition fact omitted source identity: " + factId));
    }

    private static List<ObjectNode> stratifiedFactSelection(
            List<ObjectNode> candidates, int target) {
        Map<String, List<ObjectNode>> byStratum = candidates.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        candidate -> candidate.path("selectionReason").asText(),
                        java.util.TreeMap::new,
                        java.util.stream.Collectors.toCollection(ArrayList::new)));
        Comparator<ObjectNode> hardestFirst = Comparator
                .<ObjectNode>comparingInt(candidate -> candidate.path("range")
                        .path("charLength").asInt()).reversed()
                .thenComparing(candidate -> candidate.path("sourceId").asText())
                .thenComparingInt(candidate -> candidate.path("range")
                        .path("charOffset").asInt())
                .thenComparing(candidate -> candidate.path("factId").asText());
        byStratum.replaceAll((ignored, group) -> {
            group.sort(hardestFirst);
            Map<String, ObjectNode> firstByDiversity = new LinkedHashMap<>();
            for (ObjectNode candidate : group) {
                firstByDiversity.putIfAbsent(
                        candidate.path("selectionDiversityKey").asText(), candidate);
            }
            List<ObjectNode> diverseFirst = new ArrayList<>(firstByDiversity.values());
            Set<String> firstIds = diverseFirst.stream()
                    .map(candidate -> candidate.path("factId").asText())
                    .collect(java.util.stream.Collectors.toSet());
            group.stream().filter(candidate -> !firstIds.contains(
                            candidate.path("factId").asText()))
                    .forEach(diverseFirst::add);
            return diverseFirst;
        });

        List<ObjectNode> selected = new ArrayList<>();
        int ordinal = 0;
        while (selected.size() < target) {
            boolean added = false;
            for (List<ObjectNode> group : byStratum.values()) {
                if (ordinal < group.size() && selected.size() < target) {
                    selected.add(group.get(ordinal));
                    added = true;
                }
            }
            if (!added) break;
            ordinal++;
        }
        assertEquals(target, selected.size(),
                "call-binding direct-judgment population is smaller than its sealed target");
        return selected;
    }

    private static ObjectNode symbolCandidate(
            JsonNode fact, JsonNode evidence, String sourceId, String source) {
        JsonNode payload = fact.path("payload");
        String role = payload.path("role").asText();
        String provenance = payload.path("provenance").asText();
        int rank;
        String reason;
        if ("unresolved".equals(payload.path("resolutionStatus").asText())) {
            rank = 0;
            reason = "unresolved_type_name_environment";
        } else if ("lookup".equals(role) && "source_declaration".equals(provenance)) {
            rank = 1;
            reason = "resolved_source_declaration_lookup";
        } else if ("lookup".equals(role)) {
            rank = 2;
            reason = "grammar_context_lookup";
        } else if ("typedef_name".equals(payload.path("symbolKind").asText())) {
            rank = 3;
            reason = "typedef_definition";
        } else {
            rank = 4;
            reason = "ordinary_definition";
        }
        ObjectNode row = JSON.createObjectNode();
        row.put("selectionRank", rank);
        row.put("selectionReason", reason);
        row.put("factId", fact.path("factId").asText());
        row.put("sourceId", sourceId);
        row.put("parseStatus", evidence.path("parseStatus").asText());
        row.put("grammarRule", evidence.path("grammarRules")
                .path(fact.path("grammarRuleRef").asInt()).asText());
        row.set("range", fact.path("range").deepCopy());
        row.put("sourceSlice", slice(source, fact.path("range")));
        row.set("payload", payload.deepCopy());
        return row;
    }

    private static List<ObjectNode> stratifiedSymbolSelection(
            List<ObjectNode> candidates, int target) {
        List<String> strata = List.of(
                "unresolved_type_name_environment",
                "resolved_source_declaration_lookup",
                "grammar_context_lookup",
                "typedef_definition",
                "ordinary_definition");
        int quota = target / strata.size();
        List<ObjectNode> selected = new ArrayList<>();
        Set<String> selectedIds = new HashSet<>();
        Comparator<ObjectNode> byFactId = Comparator.comparing(
                candidate -> candidate.path("factId").asText());
        for (String stratum : strata) {
            candidates.stream()
                    .filter(candidate -> stratum.equals(
                            candidate.path("selectionReason").asText()))
                    .sorted(byFactId)
                    .limit(quota)
                    .forEach(candidate -> {
                        selected.add(candidate);
                        selectedIds.add(candidate.path("factId").asText());
                    });
        }
        candidates.stream()
                .filter(candidate -> !selectedIds.contains(
                        candidate.path("factId").asText()))
                .sorted(Comparator
                        .<ObjectNode>comparingInt(
                                candidate -> candidate.path("selectionRank").asInt())
                        .thenComparing(byFactId))
                .limit(Math.max(0, target - selected.size()))
                .forEach(selected::add);
        assertEquals(target, selected.size(),
                "symbol direct-judgment population is smaller than its sealed target");
        return selected;
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
