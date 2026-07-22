package legacymodernizer.parser.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.intake.SourceIntakeClassifier;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.parsing.languages.LanguageModule;
import legacymodernizer.parser.recovery.boundaries.SourceUnit;
import legacymodernizer.parser.recovery.boundaries.UnitKind;
import legacymodernizer.parser.recovery.boundaries.UnitParseContext;
import legacymodernizer.parser.recovery.boundaries.UnitParseRequest;
import legacymodernizer.parser.recovery.diagnostics.DiagnosticPhase;
import legacymodernizer.parser.recovery.diagnostics.ParseDiagnostic;
import legacymodernizer.parser.recovery.evidence.RecoveryOutcome;
import legacymodernizer.parser.recovery.quality.DeclarationCoverage;
import legacymodernizer.parser.recovery.quality.ParseQualityGate;
import legacymodernizer.parser.recovery.quality.QualityStatus;
import legacymodernizer.parser.recovery.rules.RecoveryRuleRegistry;
import legacymodernizer.parser.recovery.rules.RecoveryRule;
import legacymodernizer.parser.recovery.rules.RecoveryRuleProposal;
import legacymodernizer.parser.recovery.workingcopy.Hashes;
import legacymodernizer.parser.recovery.workingcopy.TextEdit;
import legacymodernizer.parser.service.ParseProgressTracker;

class ContextReconstructionRecoveryTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    void acceptsOnlyTheUnitAstAndExcludesContextOnlyNodes() throws Exception {
        ParserWorkspace workspace = new ParserWorkspace(new SourceIntakeClassifier());
        String source = "context declaration\ncontext import\nclass Target {}\n";
        Path file = workspace.sourceDir().resolve("context/Target.ctx");
        Files.createDirectories(file.getParent());
        Files.writeString(file, source, StandardCharsets.UTF_8);
        int unitStart = source.indexOf("class Target");
        SourceUnit unit = new SourceUnit("unit-target", UnitKind.CLASS, "Target", null,
                unitStart, source.length(), 3, 3, 0, "EXACT");
        LanguageModule module = contextualModule(source, unit);
        ParseProgressTracker tracker = new ParseProgressTracker(null, file.getFileName().toString());
        RawParseResult firstPass = module.parseFile(file.toFile(), tracker);
        ParseQualityGate gate = new ParseQualityGate();

        RecoveryOutcome outcome = new LayeredRecoveryPipeline(gate,
                new RecoveryRuleRegistry(List.of())).recover(module, file,
                workspace.sourceDir(), firstPass, gate.evaluateFirstPass(firstPass), tracker);

        assertEquals(QualityStatus.RECOVERED_VALIDATED, outcome.decision().status());
        JsonNode children = JSON.readTree(outcome.astJson()).path("children");
        assertEquals(1, children.size());
        assertEquals("CLASS", children.get(0).path("type").asText());
        assertEquals("Target", children.get(0).path("name").asText());
        assertEquals(3, children.get(0).path("startLine").asInt());
        assertTrue(outcome.units().get(0).attempts().stream().anyMatch(attempt ->
                "CONTEXT_RECONSTRUCTION".equals(attempt.stage())
                        && "test.leading-context.v1".equals(attempt.ruleId())));
    }

    @Test
    void carriesSafeRuleEditsAndSourceMapIntoContextReconstruction() throws Exception {
        ParserWorkspace workspace = new ParserWorkspace(new SourceIntakeClassifier());
        String source = "context declaration\nBROKEN Target\n";
        Path file = workspace.sourceDir().resolve("context/RuleContext.ctx");
        Files.createDirectories(file.getParent());
        Files.writeString(file, source, StandardCharsets.UTF_8);
        int unitStart = source.indexOf("BROKEN");
        SourceUnit unit = new SourceUnit("unit-rule-context", UnitKind.CLASS, "Target", null,
                unitStart, source.length(), 2, 2, 0, "EXACT");

        LanguageModule module = ruleAndContextModule(source, unit);
        RecoveryRule normalizationRule = new RecoveryRule() {
            @Override public String id() { return "context-test.normalize-marker.v1"; }
            @Override public String ruleSetId() { return "context-test"; }
            @Override public Set<String> languages() { return Set.of("context-test"); }
            @Override
            public RecoveryRuleProposal propose(String unitSource, SourceUnit ignored,
                                                RawParseResult failedAttempt) {
                if (!unitSource.startsWith("BROKEN")) return RecoveryRuleProposal.none(id());
                return new RecoveryRuleProposal(id(), true, false,
                        List.of(new TextEdit(0, 6, "NORMALIZED", id(),
                                "Replace the synthetic invalid marker")),
                        "Synthetic deterministic normalization");
            }
        };
        ParseProgressTracker tracker = new ParseProgressTracker(null, file.getFileName().toString());
        RawParseResult firstPass = module.parseFile(file.toFile(), tracker);
        ParseQualityGate gate = new ParseQualityGate();

        RecoveryOutcome outcome = new LayeredRecoveryPipeline(gate,
                new RecoveryRuleRegistry(List.of(normalizationRule))).recover(
                module, file, workspace.sourceDir(), firstPass,
                gate.evaluateFirstPass(firstPass), tracker);

        assertEquals(QualityStatus.RECOVERED_SAFE, outcome.decision().status(),
                outcome.units().toString());
        assertTrue(outcome.units().get(0).attempts().stream().anyMatch(attempt ->
                "CONTEXT_RECONSTRUCTION".equals(attempt.stage())
                        && "context-test.wrapper.v1+context-test.normalize-marker.v1"
                                .equals(attempt.ruleId())
                        && !attempt.edits().isEmpty()
                        && attempt.sourceMap() != null
                        && attempt.diff().contains("context-test.normalize-marker.v1")));
    }

    private static LanguageModule contextualModule(String fileSource, SourceUnit unit) {
        return new LanguageModule() {
            @Override
            public RawParseResult parseFile(File file, ParseProgressTracker tracker) {
                return failed(fileSource);
            }

            @Override
            public RawParseResult parseUnit(UnitParseRequest request, ParseProgressTracker tracker) {
                if (request.leadingContextLines() == 2) {
                    String ast = "{\"type\":\"FILE\",\"startLine\":1,\"endLine\":3,"
                            + "\"children\":["
                            + "{\"type\":\"IMPORT\",\"name\":\"context\","
                            + "\"startLine\":2,\"endLine\":2,\"children\":[]},"
                            + "{\"type\":\"CLASS\",\"name\":\"Target\","
                            + "\"startLine\":3,\"endLine\":3,\"children\":[]}]}";
                    return new RawParseResult("context-test", "test-grammar", "file",
                            hash(request.sourceText()), ast, List.of(), 0,
                            completeCoverage(), 1);
                }
                return failed(request.sourceText());
            }

            @Override public String languageId() { return "context-test"; }
            @Override public Set<String> parseExtensions() { return Set.of(".ctx"); }
            @Override public boolean supportsUnitParsing() { return true; }
            @Override public List<SourceUnit> locateUnits(String source) { return List.of(unit); }

            @Override
            public Optional<UnitParseContext> reconstructUnitContext(String source, SourceUnit ignored) {
                return Optional.of(new UnitParseContext("test.leading-context.v1",
                        source, 2));
            }
        };
    }

    private static LanguageModule ruleAndContextModule(String fileSource, SourceUnit unit) {
        return new LanguageModule() {
            @Override public RawParseResult parseFile(File file, ParseProgressTracker tracker) {
                return failed(fileSource);
            }
            @Override
            public RawParseResult parseUnit(UnitParseRequest request, ParseProgressTracker tracker) {
                if (request.leadingContextLines() == 1
                        && request.sourceText().contains("NORMALIZED Target")) {
                    String ast = "{\"type\":\"FILE\",\"startLine\":1,\"endLine\":2,"
                            + "\"children\":[{\"type\":\"CLASS\",\"name\":\"Target\","
                            + "\"startLine\":2,\"endLine\":2,\"children\":[]}]}";
                    return new RawParseResult("context-test", "test-grammar", "file",
                            hash(request.sourceText()), ast, List.of(), 0,
                            completeCoverage(), 1);
                }
                return failed(request.sourceText());
            }
            @Override public String languageId() { return "context-test"; }
            @Override public Set<String> parseExtensions() { return Set.of(".ctx"); }
            @Override public boolean supportsUnitParsing() { return true; }
            @Override public List<SourceUnit> locateUnits(String ignored) { return List.of(unit); }
            @Override
            public Optional<UnitParseContext> reconstructUnitContext(
                    String ignored, SourceUnit sourceUnit, String unitSource) {
                return Optional.of(new UnitParseContext("context-test.wrapper.v1",
                        "context declaration\n" + unitSource, 1));
            }
        };
    }

    private static RawParseResult failed(String source) {
        String ast = "{\"type\":\"FILE\",\"startLine\":1,\"endLine\":3,\"children\":[]}";
        ParseDiagnostic diagnostic = new ParseDiagnostic(DiagnosticPhase.PARSER, "ERROR",
                "ANTLR_PARSER_SYNTAX", "context required", 3, 0,
                "class", "context", List.of("file"), "class Target {}");
        DeclarationCoverage coverage = new DeclarationCoverage(1, 0,
                Map.of("class", 1), Map.of(), List.of("Target"));
        return new RawParseResult("context-test", "test-grammar", "file", hash(source),
                ast, List.of(diagnostic), 1, coverage, 1);
    }

    private static DeclarationCoverage completeCoverage() {
        return new DeclarationCoverage(1, 1, Map.of("class", 1),
                Map.of("CLASS", 1), List.of());
    }

    private static String hash(String source) {
        return Hashes.sha256(source.getBytes(StandardCharsets.UTF_8));
    }
}
