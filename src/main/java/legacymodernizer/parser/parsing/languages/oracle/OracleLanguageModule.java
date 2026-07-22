package legacymodernizer.parser.parsing.languages.oracle;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.stereotype.Component;

import legacymodernizer.parser.antlr.CaseChangingCharStream;
import legacymodernizer.parser.antlr.plsql.PlSqlAstListener;
import legacymodernizer.parser.antlr.plsql.PlSqlLexer;
import legacymodernizer.parser.antlr.plsql.PlSqlParser;
import legacymodernizer.parser.model.Node;
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
import legacymodernizer.parser.recovery.workingcopy.Hashes;
import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.service.ParseProgressTracker;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OracleLanguageModule extends AntlrLanguageModuleSupport {

    private record Marker(Pattern pattern, int weight) { }

    private static final List<Marker> AFFINITY_MARKERS = List.of(
            new Marker(Pattern.compile("\\bVARCHAR2\\b", Pattern.CASE_INSENSITIVE), 10),
            new Marker(Pattern.compile("\\bDBMS_\\w+", Pattern.CASE_INSENSITIVE), 10),
            new Marker(Pattern.compile("\\bNVL\\s*\\(", Pattern.CASE_INSENSITIVE), 5),
            new Marker(Pattern.compile("\\bSYSDATE\\b", Pattern.CASE_INSENSITIVE), 5),
            new Marker(Pattern.compile("\\bFROM\\s+DUAL\\b", Pattern.CASE_INSENSITIVE), 8),
            new Marker(Pattern.compile("\\bPACKAGE\\s+BODY\\b", Pattern.CASE_INSENSITIVE), 8),
            new Marker(Pattern.compile("\\bEXCEPTION\\s+WHEN\\b", Pattern.CASE_INSENSITIVE), 4),
            new Marker(Pattern.compile("\\bMERGE\\s+INTO\\b", Pattern.CASE_INSENSITIVE), 3),
            new Marker(Pattern.compile("\\bROWNUM\\b", Pattern.CASE_INSENSITIVE), 4),
            new Marker(Pattern.compile("\\bCONNECT\\s+BY\\b", Pattern.CASE_INSENSITIVE), 6),
            new Marker(Pattern.compile("\\b(IS|AS)\\s+BEGIN\\b", Pattern.CASE_INSENSITIVE), 3));

    private final OracleSourceUnitLocator unitLocator = new OracleSourceUnitLocator();
    private final OraclePackageMemberLocator packageMemberLocator = new OraclePackageMemberLocator();

    public OracleLanguageModule(ParserWorkspace parserWorkspace) {
        super(parserWorkspace);
    }

    @Override
    public RawParseResult parseFile(File file, ParseProgressTracker tracker) throws Exception {
        byte[] sourceBytes = Files.readAllBytes(file.toPath());
        return parseContent(sourceBytes, new String(sourceBytes, StandardCharsets.UTF_8),
                file.getName(), computeRelativePath(file), 0, tracker);
    }

    @Override
    public boolean supportsUnitParsing() {
        return true;
    }

    @Override
    public RawParseResult parseUnit(UnitParseRequest request, ParseProgressTracker tracker) throws Exception {
        byte[] bytes = request.sourceText().getBytes(StandardCharsets.UTF_8);
        int lineOffset = request.originalLineOffset();
        return parseContent(bytes, request.sourceText(), request.fileName(), request.filePath(),
                lineOffset, tracker);
    }

    private RawParseResult parseContent(byte[] sourceBytes, String source, String fileName,
                                        String filePath, int lineOffset,
                                        ParseProgressTracker tracker) throws Exception {
        long started = System.nanoTime();
        try (ByteArrayInputStream input = new ByteArrayInputStream(sourceBytes)) {
            CharStream chars = CharStreams.fromStream(input);
            PlSqlLexer lexer = new PlSqlLexer(new CaseChangingCharStream(chars, true));
            CollectingAntlrErrorListener lexerErrors = new CollectingAntlrErrorListener(
                    DiagnosticPhase.LEXER, source);
            lexer.removeErrorListeners();
            lexer.addErrorListener(lexerErrors);

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            PlSqlParser parser = new PlSqlParser(tokens);
            CollectingAntlrErrorListener parserErrors = new CollectingAntlrErrorListener(
                    DiagnosticPhase.PARSER, source);
            CountingErrorStrategy errorStrategy = new CountingErrorStrategy();
            parser.removeErrorListeners();
            parser.addErrorListener(parserErrors);
            parser.setErrorHandler(errorStrategy);
            ParserRuleContext tree = parser.sql_script();

            PlSqlAstListener listener = new PlSqlAstListener(tokens, tracker);
            listener.setFileInfo(fileName, filePath);
            new ParseTreeWalker().walk(listener, tree);
            if (lineOffset > 0) rebaseNodeLines(listener.getRoot(), lineOffset, true);

            String astJson = listener.getRoot().toJson();
            List<ParseDiagnostic> diagnostics = new ArrayList<>();
            lexerErrors.diagnostics().stream().map(diagnostic -> rebase(diagnostic, lineOffset))
                    .forEach(diagnostics::add);
            parserErrors.diagnostics().stream().map(diagnostic -> rebase(diagnostic, lineOffset))
                    .forEach(diagnostics::add);
            var coverage = DeclarationCoverageCounter.count(parser, tree,
                    Set.of("create_procedure_body", "create_function_body", "create_trigger",
                            "procedure_body", "function_body"),
                    astJson, Set.of("PROCEDURE", "FUNCTION", "TRIGGER"));
            return new RawParseResult("oracle", "PlSql", "sql_script", Hashes.sha256(sourceBytes),
                    astJson, diagnostics, errorStrategy.recoveryCount(), coverage,
                    (System.nanoTime() - started) / 1_000_000L);
        }
    }

    private static ParseDiagnostic rebase(ParseDiagnostic diagnostic, int lineOffset) {
        if (lineOffset == 0) return diagnostic;
        return new ParseDiagnostic(diagnostic.phase(), diagnostic.severity(), diagnostic.code(),
                diagnostic.message(), diagnostic.line() + lineOffset, diagnostic.column(),
                diagnostic.offendingToken(), diagnostic.expectedTokens(), diagnostic.ruleStack(),
                diagnostic.tokenWindow());
    }

    private static void rebaseNodeLines(Node node, int lineOffset, boolean root) {
        if (!root) {
            if (node.startLine > 0) node.startLine += lineOffset;
            if (node.endLine > 0) node.endLine += lineOffset;
        }
        node.children.forEach(child -> rebaseNodeLines(child, lineOffset, false));
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
        int score = 0;
        for (Marker marker : AFFINITY_MARKERS) {
            var matcher = marker.pattern().matcher(source);
            while (matcher.find()) score += marker.weight();
        }
        return score;
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
