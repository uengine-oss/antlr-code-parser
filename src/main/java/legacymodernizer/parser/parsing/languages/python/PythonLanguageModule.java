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
        return parseContent(bytes, SourceTextCodec.decode(bytes).text(), file.getName(),
                computeRelativePath(file), 0, tracker);
    }

    @Override
    public RawParseResult parseUnit(UnitParseRequest request, ParseProgressTracker tracker) throws Exception {
        byte[] bytes = request.sourceText().getBytes(StandardCharsets.UTF_8);
        return parseContent(bytes, request.sourceText(), request.fileName(), request.filePath(),
                request.originalLineOffset(), tracker);
    }

    private RawParseResult parseContent(byte[] sourceBytes, String source, String fileName,
                                        String filePath, int lineOffset,
                                        ParseProgressTracker tracker) throws Exception {
        long started = System.nanoTime();
        var run = AntlrParseHarness.run(source, fileName, filePath, lineOffset, tracker,
                PythonLexer::new, PythonParser::new, PythonParser::root, PythonAstListener::new);
        var coverage = DeclarationCoverageCounter.count(run.parser(), run.tree(),
                Set.of("classdef", "funcdef"), run.astJson(), Set.of("CLASS", "FUNCTION", "METHOD"));
        return new RawParseResult("python", "Python3", "root", Hashes.sha256(sourceBytes),
                run.astJson(), run.diagnostics(), run.recoveries(), coverage,
                (System.nanoTime() - started) / 1_000_000L);
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
