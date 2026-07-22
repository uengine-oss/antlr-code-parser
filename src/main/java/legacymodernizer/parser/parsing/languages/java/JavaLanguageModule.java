package legacymodernizer.parser.parsing.languages.java;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Optional;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.stereotype.Component;

import legacymodernizer.parser.antlr.java.Java20Lexer;
import legacymodernizer.parser.antlr.java.Java20Parser;
import legacymodernizer.parser.antlr.java.JavaAstListener;
import legacymodernizer.parser.recovery.diagnostics.CollectingAntlrErrorListener;
import legacymodernizer.parser.recovery.diagnostics.CountingErrorStrategy;
import legacymodernizer.parser.recovery.diagnostics.DiagnosticPhase;
import legacymodernizer.parser.recovery.diagnostics.ParseDiagnostic;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.parsing.languages.AntlrLanguageModuleSupport;
import legacymodernizer.parser.recovery.boundaries.SourceUnit;
import legacymodernizer.parser.recovery.boundaries.UnitParseRequest;
import legacymodernizer.parser.recovery.boundaries.UnitParseContext;
import legacymodernizer.parser.recovery.quality.DeclarationCoverageCounter;
import legacymodernizer.parser.parsing.AstCoordinates;
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
        return parseContent(bytes, new String(bytes, StandardCharsets.UTF_8), file.getName(),
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
        try (ByteArrayInputStream input = new ByteArrayInputStream(sourceBytes)) {
            CharStream chars = CharStreams.fromStream(input);
            Java20Lexer lexer = new Java20Lexer(chars);
            CollectingAntlrErrorListener lexerErrors = new CollectingAntlrErrorListener(
                    DiagnosticPhase.LEXER, source);
            lexer.removeErrorListeners();
            lexer.addErrorListener(lexerErrors);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            Java20Parser parser = new Java20Parser(tokens);
            CollectingAntlrErrorListener parserErrors = new CollectingAntlrErrorListener(
                    DiagnosticPhase.PARSER, source);
            CountingErrorStrategy errorStrategy = new CountingErrorStrategy();
            parser.removeErrorListeners();
            parser.addErrorListener(parserErrors);
            parser.setErrorHandler(errorStrategy);

            Java20Parser.Start_Context tree = parser.start_();
            JavaAstListener listener = new JavaAstListener(tokens, tracker);
            listener.setFileInfo(fileName, filePath);
            new ParseTreeWalker().walk(listener, tree);
            AstCoordinates.rebaseChildren(listener.getRoot(), lineOffset);

            String astJson = listener.getRoot().toJson();
            List<ParseDiagnostic> diagnostics = new ArrayList<>();
            lexerErrors.diagnostics().stream().map(diagnostic -> AstCoordinates.rebase(diagnostic, lineOffset))
                    .forEach(diagnostics::add);
            parserErrors.diagnostics().stream().map(diagnostic -> AstCoordinates.rebase(diagnostic, lineOffset))
                    .forEach(diagnostics::add);
            var coverage = DeclarationCoverageCounter.count(parser, tree,
                    Set.of("normalClassDeclaration", "normalInterfaceDeclaration",
                            "methodDeclaration", "interfaceMethodDeclaration"),
                    JavaLanguageModule::isEmittedDeclaration,
                    astJson, Set.of("CLASS", "INTERFACE", "METHOD"));
            return new RawParseResult("java", "Java20", "start_", Hashes.sha256(sourceBytes),
                    astJson, diagnostics, errorStrategy.recoveryCount(), coverage,
                    (System.nanoTime() - started) / 1_000_000L);
        }
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
