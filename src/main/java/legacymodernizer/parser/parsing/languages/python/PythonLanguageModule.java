package legacymodernizer.parser.parsing.languages.python;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import legacymodernizer.parser.antlr.python.PythonAstListener;
import legacymodernizer.parser.antlr.python.PythonLexer;
import legacymodernizer.parser.antlr.python.PythonParser;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.parsing.languages.AntlrLanguageModuleSupport;
import legacymodernizer.parser.recovery.boundaries.SourceUnit;
import legacymodernizer.parser.recovery.boundaries.UnitParseRequest;
import legacymodernizer.parser.recovery.quality.DeclarationCoverageCounter;
import legacymodernizer.parser.parsing.AntlrParseHarness;
import legacymodernizer.parser.parsing.SourceTextCodec;
import legacymodernizer.parser.parsing.evidence.EvidenceIrSealer;
import legacymodernizer.parser.recovery.workingcopy.Hashes;
import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.service.ParseProgressTracker;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PythonLanguageModule extends AntlrLanguageModuleSupport {

    private final PythonSourceUnitLocator unitLocator = new PythonSourceUnitLocator();

    public PythonLanguageModule(ParserWorkspace parserWorkspace) {
        super(parserWorkspace);
    }

    @Override
    public RawParseResult parseFile(File file, ParseProgressTracker tracker) throws Exception {
        log.debug("[Python] parsing: {}", file.getName());
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
                PythonLexer::new, PythonParser::new, PythonParser::root, PythonAstListener::new);
        String astJson = evidenceSourceId != null
                ? EvidenceIrSealer.sealExact(run.listener().getRoot(), sourceBytes, decoded,
                        evidenceSourceId, parseStatus(run), run.listener().callEvidenceCandidates(),
                        run.listener().importEvidenceExtraction())
                : run.astJson();
        var coverage = DeclarationCoverageCounter.count(run.parser(), run.tree(),
                Set.of("classdef", "funcdef"), astJson, Set.of("CLASS", "FUNCTION", "METHOD"));
        return new RawParseResult("python", "Python3", "root", Hashes.sha256(sourceBytes),
                astJson, run.diagnostics(), run.recoveries(), coverage,
                (System.nanoTime() - started) / 1_000_000L);
    }

    private static String parseStatus(AntlrParseHarness.Harnessed<?, ?> run) {
        return run.recoveries() == 0 && run.diagnostics().isEmpty() ? "exact" : "partial";
    }

    @Override
    public boolean supportsUnitParsing() { return true; }

    @Override
    public List<SourceUnit> locateUnits(String source) { return unitLocator.locate(source); }

    @Override
    public String languageId() { return "python"; }

    @Override
    public legacymodernizer.parser.recovery.localization.SliceSyntax sliceSyntax() {
        return legacymodernizer.parser.recovery.localization.SliceSyntax.python();
    }

    @Override
    public Set<String> parseExtensions() { return Set.of(".py"); }
}
