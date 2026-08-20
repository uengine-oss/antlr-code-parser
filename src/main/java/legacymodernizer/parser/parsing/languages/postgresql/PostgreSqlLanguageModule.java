package legacymodernizer.parser.parsing.languages.postgresql;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import legacymodernizer.parser.antlr.postgresql.PostgreSQLLexer;
import legacymodernizer.parser.antlr.postgresql.PostgreSQLParser;
import legacymodernizer.parser.antlr.postgresql.PostgreSqlAstListener;
import legacymodernizer.parser.recovery.diagnostics.ParseDiagnostic;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.parsing.languages.AffinityMarkers;
import legacymodernizer.parser.parsing.languages.AntlrLanguageModuleSupport;
import legacymodernizer.parser.recovery.boundaries.SourceUnit;
import legacymodernizer.parser.recovery.boundaries.UnitParseRequest;
import legacymodernizer.parser.recovery.quality.DeclarationCoverageCounter;
import legacymodernizer.parser.parsing.AntlrParseHarness;
import legacymodernizer.parser.parsing.AstCoordinates;
import legacymodernizer.parser.parsing.SourceTextCodec;
import legacymodernizer.parser.parsing.evidence.ConditionalCompilationEvidence;
import legacymodernizer.parser.parsing.evidence.EvidenceIrSealer;
import legacymodernizer.parser.recovery.workingcopy.Hashes;
import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.service.ParseProgressTracker;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PostgreSqlLanguageModule extends AntlrLanguageModuleSupport {

    private static final List<AffinityMarkers.Marker> AFFINITY_MARKERS = List.of(
            new AffinityMarkers.Marker(Pattern.compile("\\$\\$"), 10),
            new AffinityMarkers.Marker(Pattern.compile("\\bLANGUAGE\\s+plpgsql\\b", Pattern.CASE_INSENSITIVE), 10),
            new AffinityMarkers.Marker(Pattern.compile("\\bCREATE\\s+SCHEMA\\s+IF\\s+NOT\\s+EXISTS\\b",
                    Pattern.CASE_INSENSITIVE), 15),
            new AffinityMarkers.Marker(Pattern.compile("\\bCREATE\\s+TABLE\\s+IF\\s+NOT\\s+EXISTS\\b",
                    Pattern.CASE_INSENSITIVE), 15),
            new AffinityMarkers.Marker(Pattern.compile("\\bSERIAL\\b", Pattern.CASE_INSENSITIVE), 6),
            new AffinityMarkers.Marker(Pattern.compile("\\bRETURNS\\s+\\w", Pattern.CASE_INSENSITIVE), 5),
            new AffinityMarkers.Marker(Pattern.compile("\\bRAISE\\s+NOTICE\\b", Pattern.CASE_INSENSITIVE), 8),
            new AffinityMarkers.Marker(Pattern.compile("::\\w"), 3),
            new AffinityMarkers.Marker(Pattern.compile("\\bnow\\s*\\(\\s*\\)", Pattern.CASE_INSENSITIVE), 3),
            new AffinityMarkers.Marker(Pattern.compile("\\bTEXT\\b\\s*(,|\\)|DEFAULT|NOT)",
                    Pattern.CASE_INSENSITIVE), 2));

    private final PostgreSqlSourceUnitLocator unitLocator = new PostgreSqlSourceUnitLocator();

    public PostgreSqlLanguageModule(ParserWorkspace parserWorkspace) {
        super(parserWorkspace);
    }

    @Override
    public RawParseResult parseFile(File file, ParseProgressTracker tracker) throws Exception {
        log.debug("[PostgreSQL] parsing: {}", file.getName());
        byte[] bytes = Files.readAllBytes(file.toPath());
        return parseContent(bytes, SourceTextCodec.decode(bytes), file.getName(),
                computeRelativePath(file), 0, tracker, evidenceSourceId(file, bytes));
    }

    @Override
    public RawParseResult parseUnit(UnitParseRequest request, ParseProgressTracker tracker) throws Exception {
        byte[] bytes = request.sourceText().getBytes(StandardCharsets.UTF_8);
        return parseContent(bytes, new SourceTextCodec.DecodedText(
                request.sourceText(), StandardCharsets.UTF_8.name(), false),
                request.fileName(), request.filePath(), request.originalLineOffset(), tracker, null);
    }

    private RawParseResult parseContent(byte[] sourceBytes, SourceTextCodec.DecodedText decoded,
                                        String fileName,
                                        String filePath, int lineOffset,
                                        ParseProgressTracker tracker,
                                        String evidenceSourceId) throws Exception {
        long started = System.nanoTime();
        String source = decoded.text();
        var run = AntlrParseHarness.run(source, fileName, filePath, lineOffset, tracker,
                PostgreSQLLexer::new, PostgreSQLParser::new, PostgreSQLParser::root,
                PostgreSqlAstListener::new);
        // 리스너가 $$ 내부 PL/pgSQL 을 중첩 파싱하므로 그 진단·복구 수를 병합해야 한다.
        List<ParseDiagnostic> diagnostics = new ArrayList<>(run.diagnostics());
        run.listener().getNestedDiagnostics().stream()
                .map(diagnostic -> AstCoordinates.rebase(diagnostic, lineOffset))
                .forEach(diagnostics::add);
        int recoveries = run.recoveries() + run.listener().getNestedRecoveries();
        String astJson = evidenceSourceId != null
                ? EvidenceIrSealer.sealExact(run.listener().getRoot(), sourceBytes, decoded,
                        evidenceSourceId,
                        recoveries == 0 && diagnostics.isEmpty() ? "exact" : "partial",
                        List.of(), ConditionalCompilationEvidence.NONE, false)
                : run.astJson();
        var coverage = DeclarationCoverageCounter.count(run.parser(), run.tree(),
                Set.of("createfunctionstmt", "createtrigstmt"), astJson,
                Set.of("PROCEDURE", "TRIGGER"));
        return new RawParseResult("postgresql", "PostgreSQL", "root", Hashes.sha256(sourceBytes),
                astJson, diagnostics, recoveries, coverage,
                (System.nanoTime() - started) / 1_000_000L);
    }

    @Override
    public boolean supportsUnitParsing() { return true; }

    @Override
    public List<SourceUnit> locateUnits(String source) { return unitLocator.locate(source); }

    @Override
    public String languageId() { return "postgresql"; }

    @Override
    public legacymodernizer.parser.recovery.localization.SliceSyntax sliceSyntax() {
        return legacymodernizer.parser.recovery.localization.SliceSyntax.sql();
    }

    @Override
    public Set<String> parseExtensions() { return Set.of(".sql"); }

    @Override
    public String languageFamily() { return "dbms"; }

    @Override
    public int contentAffinity(String source) {
        return AffinityMarkers.score(AFFINITY_MARKERS, source);
    }
}
