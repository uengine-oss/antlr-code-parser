package legacymodernizer.parser.parsing.languages.oracle;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import legacymodernizer.parser.antlr.CaseChangingCharStream;
import legacymodernizer.parser.antlr.plsql.PlSqlAstListener;
import legacymodernizer.parser.antlr.plsql.PlSqlLexer;
import legacymodernizer.parser.antlr.plsql.PlSqlParser;
import legacymodernizer.parser.recovery.diagnostics.ParseDiagnostic;
import legacymodernizer.parser.parsing.AntlrParseHarness;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.parsing.SourceTextCodec;
import legacymodernizer.parser.parsing.evidence.ConditionalCompilationEvidence;
import legacymodernizer.parser.parsing.evidence.EvidenceIrSealer;
import legacymodernizer.parser.parsing.languages.AffinityMarkers;
import legacymodernizer.parser.parsing.languages.AntlrLanguageModuleSupport;
import legacymodernizer.parser.recovery.boundaries.SourceUnit;
import legacymodernizer.parser.recovery.boundaries.UnitParseRequest;
import legacymodernizer.parser.recovery.boundaries.UnitParseContext;
import legacymodernizer.parser.recovery.quality.DeclarationCoverageCounter;
import legacymodernizer.parser.recovery.workingcopy.Hashes;
import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.service.ParseProgressTracker;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OracleLanguageModule extends AntlrLanguageModuleSupport {

    private static final List<AffinityMarkers.Marker> AFFINITY_MARKERS = List.of(
            new AffinityMarkers.Marker(Pattern.compile("\\bVARCHAR2\\b", Pattern.CASE_INSENSITIVE), 10),
            new AffinityMarkers.Marker(Pattern.compile("\\bDBMS_\\w+", Pattern.CASE_INSENSITIVE), 10),
            new AffinityMarkers.Marker(Pattern.compile("\\bNVL\\s*\\(", Pattern.CASE_INSENSITIVE), 5),
            new AffinityMarkers.Marker(Pattern.compile("\\bSYSDATE\\b", Pattern.CASE_INSENSITIVE), 5),
            new AffinityMarkers.Marker(Pattern.compile("\\bFROM\\s+DUAL\\b", Pattern.CASE_INSENSITIVE), 8),
            new AffinityMarkers.Marker(Pattern.compile("\\bPACKAGE\\s+BODY\\b", Pattern.CASE_INSENSITIVE), 8),
            new AffinityMarkers.Marker(Pattern.compile("\\bEXCEPTION\\s+WHEN\\b", Pattern.CASE_INSENSITIVE), 4),
            new AffinityMarkers.Marker(Pattern.compile("\\bMERGE\\s+INTO\\b", Pattern.CASE_INSENSITIVE), 3),
            new AffinityMarkers.Marker(Pattern.compile("\\bROWNUM\\b", Pattern.CASE_INSENSITIVE), 4),
            new AffinityMarkers.Marker(Pattern.compile("\\bCONNECT\\s+BY\\b", Pattern.CASE_INSENSITIVE), 6),
            new AffinityMarkers.Marker(Pattern.compile("\\b(IS|AS)\\s+BEGIN\\b", Pattern.CASE_INSENSITIVE), 3));

    private final OracleSourceUnitLocator unitLocator = new OracleSourceUnitLocator();
    private final OraclePackageMemberLocator packageMemberLocator = new OraclePackageMemberLocator();

    public OracleLanguageModule(ParserWorkspace parserWorkspace) {
        super(parserWorkspace);
    }

    @Override
    public RawParseResult parseFile(File file, ParseProgressTracker tracker) throws Exception {
        byte[] sourceBytes = Files.readAllBytes(file.toPath());
        return parseContent(sourceBytes, SourceTextCodec.decode(sourceBytes),
                file.getName(), computeRelativePath(file), 0, tracker,
                evidenceSourceId(file, sourceBytes));
    }

    @Override
    public boolean supportsUnitParsing() {
        return true;
    }

    @Override
    public RawParseResult parseUnit(UnitParseRequest request, ParseProgressTracker tracker) throws Exception {
        byte[] bytes = request.sourceText().getBytes(StandardCharsets.UTF_8);
        int lineOffset = request.originalLineOffset();
        return parseContent(bytes, new SourceTextCodec.DecodedText(
                request.sourceText(), StandardCharsets.UTF_8.name(), false),
                request.fileName(), request.filePath(), lineOffset, tracker, null);
    }

    private RawParseResult parseContent(byte[] sourceBytes, SourceTextCodec.DecodedText decoded,
                                        String fileName,
                                        String filePath, int lineOffset,
                                        ParseProgressTracker tracker,
                                        String evidenceSourceId) throws Exception {
        long started = System.nanoTime();
        String source = decoded.text();
        var run = AntlrParseHarness.run(source, fileName, filePath, lineOffset, tracker,
                chars -> new PlSqlLexer(new CaseChangingCharStream(chars, true)),
                PlSqlParser::new, PlSqlParser::sql_script, PlSqlAstListener::new);
        String astJson = evidenceSourceId != null
                ? EvidenceIrSealer.sealExact(run.listener().getRoot(), sourceBytes, decoded,
                        evidenceSourceId, parseStatus(run), List.of(),
                        ConditionalCompilationEvidence.NONE, false)
                : run.astJson();
        var coverage = DeclarationCoverageCounter.count(run.parser(), run.tree(),
                Set.of("create_procedure_body", "create_function_body", "create_trigger",
                        "procedure_body", "function_body"),
                astJson, Set.of("PROCEDURE", "FUNCTION", "TRIGGER"));
        return new RawParseResult("oracle", "PlSql", "sql_script", Hashes.sha256(sourceBytes),
                astJson, run.diagnostics(), run.recoveries(), coverage,
                (System.nanoTime() - started) / 1_000_000L);
    }

    private static String parseStatus(AntlrParseHarness.Harnessed<?, ?> run) {
        return run.recoveries() == 0 && run.diagnostics().isEmpty() ? "exact" : "partial";
    }

    @Override
    public String languageId() { return "oracle"; }

    @Override
    public legacymodernizer.parser.recovery.localization.SliceSyntax sliceSyntax() {
        return legacymodernizer.parser.recovery.localization.SliceSyntax.sql();
    }

    @Override
    public legacymodernizer.parser.recovery.candidates.RepairProfile repairProfile() {
        // Oracle table aliases forbid AS; the grammar reports it as extraneous input.
        return new legacymodernizer.parser.recovery.candidates.RepairProfile(java.util.Set.of("AS"));
    }

    @Override
    public Set<String> parseExtensions() {
        return Set.of(".sql", ".pks", ".pkb", ".prc", ".fnc");
    }

    @Override
    public String languageFamily() { return "dbms"; }

    @Override
    public int contentAffinity(String source) {
        return AffinityMarkers.score(AFFINITY_MARKERS, source);
    }

    @Override
    public int sharedExtensionPriority() { return 100; }

    @Override
    public List<SourceUnit> locateUnits(String source) { return unitLocator.locate(source); }

    @Override
    public List<SourceUnit> locateRecoveryUnits(String source, RawParseResult failedParse) {
        List<SourceUnit> recoveryUnits = new ArrayList<>();
        for (SourceUnit unit : unitLocator.locate(source)) {
            if (unit.kind() != legacymodernizer.parser.recovery.boundaries.UnitKind.PACKAGE) {
                recoveryUnits.add(unit);
                continue;
            }
            List<ParseDiagnostic> packageDiagnostics = failedParse.diagnostics().stream()
                    .filter(diagnostic -> diagnostic.line() >= unit.startLine()
                            && diagnostic.line() <= unit.endLine())
                    .toList();
            if (packageDiagnostics.isEmpty()) {
                recoveryUnits.add(unit);
                continue;
            }
            List<SourceUnit> members = packageMemberLocator.locate(source, unit);
            boolean everyDiagnosticBelongsToMember = !members.isEmpty()
                    && packageDiagnostics.stream().allMatch(diagnostic -> members.stream()
                            .anyMatch(member -> diagnostic.line() >= member.startLine()
                                    && diagnostic.line() <= member.endLine()));
            recoveryUnits.addAll(everyDiagnosticBelongsToMember ? members : List.of(unit));
        }
        return recoveryUnits.stream()
                .sorted(java.util.Comparator.comparingInt(SourceUnit::startOffset))
                .toList();
    }

    @Override
    public Optional<UnitParseContext> reconstructUnitContext(
            String fileSource, SourceUnit unit, String unitSource) {
        if (fileSource == null || unit == null || unit.parentUnitId() == null
                || (unit.kind() != legacymodernizer.parser.recovery.boundaries.UnitKind.PROCEDURE
                    && unit.kind() != legacymodernizer.parser.recovery.boundaries.UnitKind.FUNCTION)) {
            return Optional.empty();
        }
        String memberSource = unitSource;
        String separator = memberSource.endsWith("\n") ? "" : "\n";
        String contextualSource = "CREATE OR REPLACE PACKAGE BODY PARSER_RECOVERY_CONTEXT AS\n"
                + memberSource + separator
                + "END PARSER_RECOVERY_CONTEXT;\n/\n";
        return Optional.of(new UnitParseContext("oracle.package-member-wrapper.v1",
                contextualSource, 1));
    }
}
