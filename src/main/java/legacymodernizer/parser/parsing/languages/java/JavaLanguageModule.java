package legacymodernizer.parser.parsing.languages.java;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.Optional;

import org.springframework.stereotype.Component;

import legacymodernizer.parser.antlr.java.Java20Lexer;
import legacymodernizer.parser.antlr.java.Java20Parser;
import legacymodernizer.parser.antlr.java.JavaAstListener;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.parsing.languages.AntlrLanguageModuleSupport;
import legacymodernizer.parser.recovery.boundaries.SourceUnit;
import legacymodernizer.parser.recovery.boundaries.UnitParseRequest;
import legacymodernizer.parser.recovery.boundaries.UnitParseContext;
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
public class JavaLanguageModule extends AntlrLanguageModuleSupport {

    private final JavaSourceUnitLocator unitLocator = new JavaSourceUnitLocator();

    public JavaLanguageModule(ParserWorkspace parserWorkspace) {
        super(parserWorkspace);
    }

    @Override
    public RawParseResult parseFile(File file, ParseProgressTracker tracker) throws Exception {
        log.debug("[Java] parsing: {}", file.getName());
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
                Java20Lexer::new, Java20Parser::new, Java20Parser::start_, JavaAstListener::new);
        String astJson = evidenceSourceId != null
                ? EvidenceIrSealer.sealExact(run.listener().getRoot(), sourceBytes, decoded,
                        evidenceSourceId, parseStatus(run), run.listener().callEvidenceCandidates(),
                        run.listener().importEvidenceExtraction(),
                        run.listener().callableEvidenceExtraction())
                : run.astJson();
        var coverage = DeclarationCoverageCounter.count(run.parser(), run.tree(),
                Set.of("normalClassDeclaration", "normalInterfaceDeclaration",
                        "methodDeclaration", "interfaceMethodDeclaration"),
                JavaLanguageModule::isEmittedDeclaration,
                astJson, Set.of("CLASS", "INTERFACE", "METHOD"));
        return new RawParseResult("java", "Java20", "start_", Hashes.sha256(sourceBytes),
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
    public String languageId() { return "java"; }

    @Override
    public legacymodernizer.parser.recovery.localization.SliceSyntax sliceSyntax() {
        return legacymodernizer.parser.recovery.localization.SliceSyntax.cFamily();
    }

    @Override
    public Set<String> parseExtensions() { return Set.of(".java"); }

    @Override
    public Optional<UnitParseContext> reconstructUnitContext(
            String fileSource, SourceUnit unit, String unitSource) {
        if (fileSource == null || unit == null || unit.startOffset() <= 0) return Optional.empty();
        String sourceThroughUnit = fileSource.substring(0, unit.startOffset()) + unitSource;
        return Optional.of(new UnitParseContext("java.original-prefix.v1",
                sourceThroughUnit, Math.max(0, unit.startLine() - 1)));
    }

    private static boolean isEmittedDeclaration(org.antlr.v4.runtime.ParserRuleContext context) {
        if (context instanceof Java20Parser.NormalClassDeclarationContext
                || context instanceof Java20Parser.NormalInterfaceDeclarationContext) {
            return true;
        }
        if (!(context instanceof Java20Parser.MethodDeclarationContext)
                && !(context instanceof Java20Parser.InterfaceMethodDeclarationContext)) {
            return false;
        }
        org.antlr.v4.runtime.tree.ParseTree ancestor = context.getParent();
        while (ancestor != null) {
            if (ancestor instanceof Java20Parser.NormalClassDeclarationContext
                    || ancestor instanceof Java20Parser.NormalInterfaceDeclarationContext) {
                return true;
            }
            if (ancestor instanceof Java20Parser.ClassInstanceCreationExpressionContext
                    || ancestor instanceof Java20Parser.UnqualifiedClassInstanceCreationExpressionContext) {
                return false;
            }
            if (ancestor instanceof Java20Parser.MethodDeclarationContext
                    || ancestor instanceof Java20Parser.InterfaceMethodDeclarationContext) {
                return false;
            }
            ancestor = ancestor.getParent();
        }
        return false;
    }
}
