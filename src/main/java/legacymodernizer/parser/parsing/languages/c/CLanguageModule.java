package legacymodernizer.parser.parsing.languages.c;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import legacymodernizer.parser.antlr.c.CLexer;
import legacymodernizer.parser.antlr.c.CParser;
import legacymodernizer.parser.antlr.c.CAstListener;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.parsing.languages.AntlrLanguageModuleSupport;
import legacymodernizer.parser.recovery.boundaries.SourceUnit;
import legacymodernizer.parser.recovery.boundaries.UnitParseRequest;
import legacymodernizer.parser.recovery.boundaries.UnitParseContext;
import legacymodernizer.parser.recovery.quality.DeclarationCoverageCounter;
import legacymodernizer.parser.parsing.AntlrParseHarness;
import legacymodernizer.parser.parsing.SourceTextCodec;
import legacymodernizer.parser.parsing.evidence.EvidenceIrSealer;
import legacymodernizer.parser.parsing.evidence.ConfiguredPreprocessingEvidence;
import legacymodernizer.parser.parsing.build.ProjectCompilationCatalog;
import legacymodernizer.parser.recovery.workingcopy.Hashes;
import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.service.ParseProgressTracker;
import lombok.extern.slf4j.Slf4j;

/**
 * C 파싱 전략
 */
@Slf4j
@Component
public class CLanguageModule extends AntlrLanguageModuleSupport {

    private final CSourceUnitLocator unitLocator = new CSourceUnitLocator();
    private ProjectCompilationCatalog compilationCatalog;

    public CLanguageModule(ParserWorkspace parserWorkspace) {
        super(parserWorkspace);
    }

    @Override
    public void prepareProjectContext() {
        compilationCatalog = ProjectCompilationCatalog.discover(parserWorkspace.sourceDir());
    }

    @Override
    public RawParseResult parseFile(File file, ParseProgressTracker tracker) throws Exception {
        log.debug("[C] 파싱: {}", file.getName());
        byte[] sourceBytes = Files.readAllBytes(file.toPath());
        String sourceId = evidenceSourceId(file, sourceBytes);
        ProjectCompilationCatalog catalog = compilationCatalog();
        var buildContext = catalog.resolve(file.toPath(),
                parserWorkspace.sourceOrigin(file.toPath()));
        return parseContent(sourceBytes, SourceTextCodec.decode(sourceBytes),
                file.getName(), computeRelativePath(file), 0, tracker,
                sourceId, ConfiguredPreprocessingEvidence.withoutTrace(
                        sourceId, buildContext));
    }

    @Override
    public RawParseResult parseUnit(UnitParseRequest request, ParseProgressTracker tracker) throws Exception {
        byte[] sourceBytes = request.sourceText().getBytes(StandardCharsets.UTF_8);
        return parseContent(sourceBytes, new SourceTextCodec.DecodedText(
                request.sourceText(), StandardCharsets.UTF_8.name(), false),
                request.fileName(), request.filePath(), request.originalLineOffset(), tracker,
                null, null);
    }

    private RawParseResult parseContent(byte[] sourceBytes, SourceTextCodec.DecodedText decoded,
                                        String fileName,
                                        String filePath, int lineOffset,
                                        ParseProgressTracker tracker,
                                        String evidenceSourceId,
                                        ConfiguredPreprocessingEvidence configuredPreprocessing) {
        long started = System.nanoTime();
        String source = decoded.text();
        var preprocessorSyntax = CPreprocessorEvidenceExtractor.extract(source);
        String workingSource = preprocessorSyntax.maskDirectives(source);
        var run = AntlrParseHarness.run(workingSource, fileName, filePath, lineOffset, tracker,
                CLexer::new,
                CParser::new,
                CParser::compilationUnit,
                (tokens, progress) -> new CAstListener(
                        tokens, progress, source, preprocessorSyntax));
        String parserStatus = parseStatus(run);
        String astJson = evidenceSourceId != null
                ? EvidenceIrSealer.sealExact(run.listener().getRoot(), sourceBytes, decoded,
                        evidenceSourceId, parserStatus, run.listener().callEvidenceCandidates(),
                        run.listener().conditionalCompilationEvidence(),
                        preprocessorSyntax.macros(), preprocessorSyntax.imports(),
                        // A grammar-owned call range proves its callee is an expression, so an
                        // earlier negative typedef lookahead there is not missing type context.
                        configuredPreprocessing, run.parser().symbolEvidenceExtraction(
                                run.listener().callEvidenceCandidates()))
                : run.astJson();
        var coverage = DeclarationCoverageCounter.count(run.parser(), run.tree(),
                Set.of("functionDefinition"), astJson, Set.of("FUNCTION"));
        return new RawParseResult("c", "C", "compilationUnit", Hashes.sha256(sourceBytes),
                astJson, run.diagnostics(), run.recoveries(), coverage,
                (System.nanoTime() - started) / 1_000_000L);
    }

    private static String parseStatus(AntlrParseHarness.Harnessed<?, ?> run) {
        return run.recoveries() == 0 && run.diagnostics().isEmpty() ? "exact" : "partial";
    }

    private ProjectCompilationCatalog compilationCatalog() {
        if (compilationCatalog == null) {
            compilationCatalog = ProjectCompilationCatalog.discover(parserWorkspace.sourceDir());
        }
        return compilationCatalog;
    }

    @Override
    public boolean supportsUnitParsing() { return true; }

    @Override
    public List<SourceUnit> locateUnits(String source) { return unitLocator.locate(source); }

    @Override
    public String languageId() {
        return "c";
    }

    @Override
    public legacymodernizer.parser.recovery.localization.SliceSyntax sliceSyntax() {
        return legacymodernizer.parser.recovery.localization.SliceSyntax.cFamily();
    }

    @Override
    public Set<String> parseExtensions() {
        return Set.of(".c", ".h");
    }

    @Override
    public List<UnitParseContext> reconstructUnitContexts(
            String fileSource, SourceUnit unit, String unitSource) {
        if (fileSource == null || unit == null || unit.startOffset() <= 0) return List.of();
        String sourceWithOriginalPrefix = fileSource.substring(0, unit.startOffset()) + unitSource;
        return List.of(new UnitParseContext("c.original-prefix.v1", sourceWithOriginalPrefix,
                Math.max(0, unit.startLine() - 1)));
    }

}
