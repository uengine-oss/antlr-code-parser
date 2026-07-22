package legacymodernizer.parser.parsing.languages.postgresql;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.stereotype.Component;

import legacymodernizer.parser.antlr.postgresql.PostgreSQLLexer;
import legacymodernizer.parser.antlr.postgresql.PostgreSQLParser;
import legacymodernizer.parser.antlr.postgresql.PostgreSqlAstListener;
import legacymodernizer.parser.recovery.diagnostics.CollectingAntlrErrorListener;
import legacymodernizer.parser.recovery.diagnostics.CountingErrorStrategy;
import legacymodernizer.parser.recovery.diagnostics.DiagnosticPhase;
import legacymodernizer.parser.recovery.diagnostics.ParseDiagnostic;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.parsing.languages.AntlrLanguageModuleSupport;
import legacymodernizer.parser.recovery.boundaries.SourceUnit;
import legacymodernizer.parser.recovery.boundaries.UnitParseRequest;
import legacymodernizer.parser.recovery.quality.DeclarationCoverageCounter;
import legacymodernizer.parser.parsing.AstCoordinates;
import legacymodernizer.parser.recovery.workingcopy.Hashes;
import legacymodernizer.parser.parsing.languages.postgresql.PostgreSqlSourceUnitLocator;
import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.service.ParseProgressTracker;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PostgreSqlLanguageModule extends AntlrLanguageModuleSupport {

    private record Marker(Pattern pattern, int weight) { }

    private static final List<Marker> AFFINITY_MARKERS = List.of(
            new Marker(Pattern.compile("\\$\\$"), 10),
            new Marker(Pattern.compile("\\bLANGUAGE\\s+plpgsql\\b", Pattern.CASE_INSENSITIVE), 10),
            new Marker(Pattern.compile("\\bCREATE\\s+SCHEMA\\s+IF\\s+NOT\\s+EXISTS\\b",
                    Pattern.CASE_INSENSITIVE), 15),
            new Marker(Pattern.compile("\\bCREATE\\s+TABLE\\s+IF\\s+NOT\\s+EXISTS\\b",
                    Pattern.CASE_INSENSITIVE), 15),
            new Marker(Pattern.compile("\\bSERIAL\\b", Pattern.CASE_INSENSITIVE), 6),
            new Marker(Pattern.compile("\\bRETURNS\\s+\\w", Pattern.CASE_INSENSITIVE), 5),
            new Marker(Pattern.compile("\\bRAISE\\s+NOTICE\\b", Pattern.CASE_INSENSITIVE), 8),
            new Marker(Pattern.compile("::\\w"), 3),
            new Marker(Pattern.compile("\\bnow\\s*\\(\\s*\\)", Pattern.CASE_INSENSITIVE), 3),
            new Marker(Pattern.compile("\\bTEXT\\b\\s*(,|\\)|DEFAULT|NOT)", Pattern.CASE_INSENSITIVE), 2));

    private final PostgreSqlSourceUnitLocator unitLocator = new PostgreSqlSourceUnitLocator();

    public PostgreSqlLanguageModule(ParserWorkspace parserWorkspace) {
        super(parserWorkspace);
    }

    @Override
    public RawParseResult parseFile(File file, ParseProgressTracker tracker) throws Exception {
        log.debug("[PostgreSQL] parsing: {}", file.getName());
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
            PostgreSQLLexer lexer = new PostgreSQLLexer(chars);
            CollectingAntlrErrorListener lexerErrors = new CollectingAntlrErrorListener(
                    DiagnosticPhase.LEXER, source);
            lexer.removeErrorListeners();
            lexer.addErrorListener(lexerErrors);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            PostgreSQLParser parser = new PostgreSQLParser(tokens);
            CollectingAntlrErrorListener parserErrors = new CollectingAntlrErrorListener(
                    DiagnosticPhase.PARSER, source);
            CountingErrorStrategy errorStrategy = new CountingErrorStrategy();
            parser.removeErrorListeners();
            parser.addErrorListener(parserErrors);
            parser.setErrorHandler(errorStrategy);

            PostgreSQLParser.RootContext tree = parser.root();
            PostgreSqlAstListener listener = new PostgreSqlAstListener(tokens, tracker);
            listener.setFileInfo(fileName, filePath);
            new ParseTreeWalker().walk(listener, tree);
            AstCoordinates.rebaseChildren(listener.getRoot(), lineOffset);

            String astJson = listener.getRoot().toJson();
            List<ParseDiagnostic> diagnostics = new ArrayList<>();
            lexerErrors.diagnostics().stream().map(diagnostic -> AstCoordinates.rebase(diagnostic, lineOffset))
                    .forEach(diagnostics::add);
            parserErrors.diagnostics().stream().map(diagnostic -> AstCoordinates.rebase(diagnostic, lineOffset))
                    .forEach(diagnostics::add);
            listener.getNestedDiagnostics().stream().map(diagnostic -> AstCoordinates.rebase(diagnostic, lineOffset))
                    .forEach(diagnostics::add);
            var coverage = DeclarationCoverageCounter.count(parser, tree,
                    Set.of("createfunctionstmt", "createtrigstmt"), astJson,
                    Set.of("PROCEDURE", "TRIGGER"));
            return new RawParseResult("postgresql", "PostgreSQL", "root", Hashes.sha256(sourceBytes),
                    astJson, diagnostics, errorStrategy.recoveryCount() + listener.getNestedRecoveries(), coverage,
                    (System.nanoTime() - started) / 1_000_000L);
        }
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
        int score = 0;
        for (Marker marker : AFFINITY_MARKERS) {
            var matcher = marker.pattern().matcher(source);
            while (matcher.find()) score += marker.weight();
        }
        return score;
    }
}
