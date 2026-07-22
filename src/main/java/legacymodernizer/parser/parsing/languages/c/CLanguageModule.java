package legacymodernizer.parser.parsing.languages.c;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.stereotype.Component;
import legacymodernizer.parser.antlr.c.CLexer;
import legacymodernizer.parser.antlr.c.CParser;
import legacymodernizer.parser.antlr.c.CAstListener;
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
import legacymodernizer.parser.parsing.languages.c.CSourceUnitLocator;
import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.service.ParseProgressTracker;
import lombok.extern.slf4j.Slf4j;

/**
 * C 파싱 전략
 */
@Slf4j
@Component
public class CLanguageModule extends AntlrLanguageModuleSupport {

    /** 소스 파일들에서 수집한 사용자 정의 타입 이름 */
    private Set<String> collectedTypeNames = new HashSet<>();
    private final CSourceUnitLocator unitLocator = new CSourceUnitLocator();

    public CLanguageModule(ParserWorkspace parserWorkspace) {
        super(parserWorkspace);
    }

    /** C 는 파일별 파싱 전, .c/.h 전체에서 typedef/struct 타입을 먼저 수집해야 정확하다. */
    @Override
    public void prepareProjectContext() {
        collectTypeNamesFromSource();
    }

    @Override
    public RawParseResult parseFile(File file, ParseProgressTracker tracker) throws Exception {
        log.debug("[C] 파싱: {}", file.getName());

        long started = System.nanoTime();
        byte[] sourceBytes = Files.readAllBytes(file.toPath());
        String source = readFileContent(file.toPath());
        String workingSource = preprocessSource(source);
        CharStream charStream = CharStreams.fromString(workingSource);
        CLexer lexer = new CLexer(charStream);
        CollectingAntlrErrorListener lexerErrors = new CollectingAntlrErrorListener(
                DiagnosticPhase.LEXER, workingSource);
        lexer.removeErrorListeners();
        lexer.addErrorListener(lexerErrors);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CParser parser = new CParser(tokens);
        CollectingAntlrErrorListener parserErrors = new CollectingAntlrErrorListener(
                DiagnosticPhase.PARSER, workingSource);
        CountingErrorStrategy errorStrategy = new CountingErrorStrategy();
        parser.removeErrorListeners();
        parser.addErrorListener(parserErrors);
        parser.setErrorHandler(errorStrategy);

        if (!collectedTypeNames.isEmpty()) {
            parser.registerTypeNames(collectedTypeNames);
        }

        CParser.CompilationUnitContext tree = parser.compilationUnit();

        CAstListener listener = new CAstListener(tokens, tracker);
        listener.setFileInfo(file.getName(), computeRelativePath(file));

        new ParseTreeWalker().walk(listener, tree);

        String astJson = listener.getRoot().toJson();
        var diagnostics = new ArrayList<>(lexerErrors.diagnostics());
        diagnostics.addAll(parserErrors.diagnostics());
        var coverage = DeclarationCoverageCounter.count(parser, tree,
                Set.of("functionDefinition"), astJson, Set.of("FUNCTION"));
        return new RawParseResult("c", "C", "compilationUnit", Hashes.sha256(sourceBytes),
                astJson, diagnostics, errorStrategy.recoveryCount(), coverage,
                (System.nanoTime() - started) / 1_000_000L);
    }

    @Override
    public RawParseResult parseUnit(UnitParseRequest request, ParseProgressTracker tracker) throws Exception {
        long started = System.nanoTime();
        byte[] sourceBytes = request.sourceText().getBytes(StandardCharsets.UTF_8);
        String workingSource = preprocessSource(request.sourceText());
        int lineOffset = request.originalLineOffset();
        CharStream charStream = CharStreams.fromString(workingSource);
        CLexer lexer = new CLexer(charStream);
        CollectingAntlrErrorListener lexerErrors = new CollectingAntlrErrorListener(
                DiagnosticPhase.LEXER, workingSource);
        lexer.removeErrorListeners();
        lexer.addErrorListener(lexerErrors);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CParser parser = new CParser(tokens);
        CollectingAntlrErrorListener parserErrors = new CollectingAntlrErrorListener(
                DiagnosticPhase.PARSER, workingSource);
        CountingErrorStrategy errorStrategy = new CountingErrorStrategy();
        parser.removeErrorListeners();
        parser.addErrorListener(parserErrors);
        parser.setErrorHandler(errorStrategy);
        if (!collectedTypeNames.isEmpty()) parser.registerTypeNames(collectedTypeNames);

        CParser.CompilationUnitContext tree = parser.compilationUnit();
        CAstListener listener = new CAstListener(tokens, tracker);
        listener.setFileInfo(request.fileName(), request.filePath());
        new ParseTreeWalker().walk(listener, tree);
        AstCoordinates.rebaseChildren(listener.getRoot(), lineOffset);

        String astJson = listener.getRoot().toJson();
        List<ParseDiagnostic> diagnostics = new ArrayList<>();
        lexerErrors.diagnostics().stream().map(diagnostic -> AstCoordinates.rebase(diagnostic, lineOffset))
                .forEach(diagnostics::add);
        parserErrors.diagnostics().stream().map(diagnostic -> AstCoordinates.rebase(diagnostic, lineOffset))
                .forEach(diagnostics::add);
        var coverage = DeclarationCoverageCounter.count(parser, tree,
                Set.of("functionDefinition"), astJson, Set.of("FUNCTION"));
        return new RawParseResult("c", "C", "compilationUnit", Hashes.sha256(sourceBytes),
                astJson, diagnostics, errorStrategy.recoveryCount(), coverage,
                (System.nanoTime() - started) / 1_000_000L);
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
        List<UnitParseContext> contexts = new ArrayList<>();
        contexts.add(new UnitParseContext("c.original-prefix.v1", sourceWithOriginalPrefix,
                Math.max(0, unit.startLine() - 1)));
        String alternateBranches = selectAlternateConditionalBranches(sourceWithOriginalPrefix);
        if (!alternateBranches.equals(sourceWithOriginalPrefix)) {
            contexts.add(new UnitParseContext("c.alternate-preprocessor-branches.v1",
                    alternateBranches, Math.max(0, unit.startLine() - 1)));
        }
        return List.copyOf(contexts);
    }

    private static String selectAlternateConditionalBranches(String source) {
        String[] lines = source.split("\n", -1);
        record ConditionalFrame(boolean parentActive, boolean alternateSelected) { }
        java.util.ArrayDeque<ConditionalFrame> frames = new java.util.ArrayDeque<>();
        StringBuilder selected = new StringBuilder(source.length());
        boolean active = true;
        for (int index = 0; index < lines.length; index++) {
            String line = lines[index];
            String trimmed = line.trim();
            if (trimmed.startsWith("#ifdef") || trimmed.startsWith("#ifndef")
                    || trimmed.startsWith("#if ") || trimmed.equals("#if")) {
                frames.push(new ConditionalFrame(active, false));
                active = false;
                selected.append(' ');
            } else if ((trimmed.startsWith("#else") || trimmed.startsWith("#elif"))
                    && !frames.isEmpty()) {
                ConditionalFrame frame = frames.pop();
                active = frame.parentActive() && !frame.alternateSelected();
                frames.push(new ConditionalFrame(frame.parentActive(), true));
                selected.append(' ');
            } else if (trimmed.startsWith("#endif") && !frames.isEmpty()) {
                ConditionalFrame frame = frames.pop();
                active = frame.parentActive();
                selected.append(' ');
            } else if (active) {
                selected.append(line);
            }
            if (index + 1 < lines.length) selected.append('\n');
        }
        return selected.toString();
    }

    // ═══════════════════════════════════════════════════════════════════
    // typedef/struct/enum 이름 사전 수집
    // ═══════════════════════════════════════════════════════════════════

    /**
     * source 디렉토리의 모든 .h/.c 파일을 스캔하여
     * typedef, struct, union, enum으로 정의된 사용자 타입 이름을 수집
     */
    private void collectTypeNamesFromSource() {
        collectedTypeNames.clear();
        Path sourceDir = parserWorkspace.sourceDir();
        if (!Files.exists(sourceDir)) return;

        try {
            Files.walk(sourceDir)
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.toString().toLowerCase();
                        return name.endsWith(".h") || name.endsWith(".c");
                    })
                    .forEach(p -> {
                        try {
                            String content = readFileContent(p);
                            extractTypeNames(content);
                            inferTypeNamesFromUsage(content);
                        } catch (Exception e) {
                            log.warn("타입 이름 수집 중 파일 읽기 실패: {}", p, e);
                        }
                    });

            if (!collectedTypeNames.isEmpty()) {
                log.info("소스에서 수집한 사용자 정의 타입: {}개 - {}", collectedTypeNames.size(), collectedTypeNames);
            }
        } catch (Exception e) {
            log.warn("타입 이름 수집 실패", e);
        }
    }

    // typedef 한 줄 패턴: typedef int MyType; / typedef unsigned long DWORD;
    private static final Pattern TYPEDEF_SIMPLE = Pattern.compile(
            "typedef\\s+[^;{]+\\s+(\\w+)\\s*;");

    // typedef struct/union/enum { ... } Name; (멀티라인, 중첩 브레이스 지원)
    private static final Pattern TYPEDEF_BLOCK = Pattern.compile(
            "typedef\\s+(?:struct|union|enum)\\s*\\w*\\s*\\{(?:[^{}]|\\{[^{}]*\\})*\\}\\s*(\\w+)\\s*;",
            Pattern.DOTALL);

    // typedef 함수 포인터: typedef void (*Name)(...);
    private static final Pattern TYPEDEF_FUNCPTR = Pattern.compile(
            "typedef\\s+[^;]+\\(\\s*\\*\\s*(\\w+)\\s*\\)\\s*\\([^)]*\\)\\s*;");

    // struct/union/enum 전방 선언 또는 정의: struct Name { 또는 struct Name;
    // } Name; 패턴 (typedef 블록 끝에서 이름 추출)
    private static final Pattern STRUCT_DEF = Pattern.compile(
            "(?:struct|union|enum)\\s+(\\w+)\\s*[{;]");
    private static final Pattern CLOSING_TYPEDEF = Pattern.compile(
            "\\}\\s*(\\w+)\\s*;");

    // C 키워드 (수집에서 제외)
    private static final Set<String> C_KEYWORDS = Set.of(
            "auto", "break", "case", "char", "const", "continue", "default", "do",
            "double", "else", "enum", "extern", "float", "for", "goto", "if",
            "inline", "int", "long", "register", "restrict", "return", "short",
            "signed", "sizeof", "static", "struct", "switch", "typedef", "union",
            "unsigned", "void", "volatile", "while", "_Bool", "_Complex", "_Imaginary",
            "bool", "true", "false", "NULL", "nullptr",
            // struct/union 멤버 이름이 잘못 수집되는 것 방지
            "value", "data", "next", "prev", "head", "tail", "buf", "ptr",
            "len", "size", "count", "index", "type", "name", "key", "node"
    );

    private void extractTypeNames(String content) {
        // 주석 제거 (간단 처리)
        content = content.replaceAll("//[^\n]*", "");
        content = content.replaceAll("/\\*.*?\\*/", " ");

        extractWithPattern(TYPEDEF_BLOCK, content);
        extractWithPattern(TYPEDEF_FUNCPTR, content);
        extractWithPattern(TYPEDEF_SIMPLE, content);
        extractWithPattern(STRUCT_DEF, content);
        extractWithPattern(CLOSING_TYPEDEF, content);
    }

    private void extractWithPattern(Pattern pattern, String content) {
        Matcher m = pattern.matcher(content);
        while (m.find()) {
            String name = m.group(1);
            if (name != null && !name.isEmpty()
                    && !C_KEYWORDS.contains(name)
                    && name.matches("[A-Za-z_]\\w*")) {
                collectedTypeNames.add(name);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // 사용 패턴 기반 타입 이름 추론 (헤더 파일 없이도 동작)
    // ═══════════════════════════════════════════════════════════════════

    // 함수 정의 패턴: returnType funcName(params) { — 함수 호출과 구분
    private static final Pattern FUNC_DEFINITION = Pattern.compile(
            "\\w+\\s+\\*?\\s*\\w+\\s*\\(([^)]+)\\)\\s*\\{");

    // 파라미터 내 포인터 타입: TypeName *varName
    private static final Pattern PARAM_PTR_TYPE = Pattern.compile(
            "(\\w+)\\s+\\*\\s*\\w+");

    // 줄 시작 포인터 변수 선언: TypeName *var;
    private static final Pattern USAGE_LINE_PTR_DECL = Pattern.compile(
            "^\\s*(\\w+)\\s+\\*\\s*\\w+\\s*[;=,\\[]", Pattern.MULTILINE);

    // const/static/extern 뒤 포인터 타입: static TypeName *var
    private static final Pattern USAGE_QUALIFIED_PTR = Pattern.compile(
            "(?:const|volatile|static|extern)\\s+(\\w+)\\s+\\*\\s*\\w+");

    // 포인터 캐스트: (TypeName *)
    private static final Pattern USAGE_CAST_PTR = Pattern.compile(
            "\\(\\s*(\\w+)\\s*\\*\\s*\\)");

    // _t 접미사 비포인터 사용: TypeName_t varName; 또는 (TypeName_t var,
    private static final Pattern USAGE_T_SUFFIX = Pattern.compile(
            "[,(;{}\\s](\\w+_t)\\s+\\w+\\s*[;=,\\[)]");

    /**
     * 소스 코드의 사용 패턴에서 타입 이름을 추론.
     * 헤더 파일이 없어도 .c 파일 내에서 타입이 사용되는 문맥을 분석하여 타입 식별.
     *
     * 핵심: 함수 정의(returnType funcName(params) {)의 파라미터에서만 포인터 타입을 추출.
     *       함수 호출(func(a * b, c))과 구분하여 오인식 방지.
     */
    private void inferTypeNamesFromUsage(String content) {
        String cleaned = content.replaceAll("//[^\n]*", "");
        cleaned = cleaned.replaceAll("/\\*.*?\\*/", " ");

        // 1. 함수 정의 파라미터에서 포인터 타입 추출 (가장 정확)
        Matcher funcMatcher = FUNC_DEFINITION.matcher(cleaned);
        while (funcMatcher.find()) {
            String params = funcMatcher.group(1);
            Matcher paramMatcher = PARAM_PTR_TYPE.matcher(params);
            while (paramMatcher.find()) {
                String name = paramMatcher.group(1);
                if (name != null && !name.isEmpty()
                        && !C_KEYWORDS.contains(name)
                        && name.matches("[A-Za-z_]\\w*")) {
                    collectedTypeNames.add(name);
                }
            }
        }

        // 2. 기타 안전한 패턴
        extractWithPattern(USAGE_LINE_PTR_DECL, cleaned);
        extractWithPattern(USAGE_QUALIFIED_PTR, cleaned);
        extractWithPattern(USAGE_CAST_PTR, cleaned);
        extractWithPattern(USAGE_T_SUFFIX, cleaned);
    }

    /**
     * C 전처리기 처리:
     * #ifdef / #ifndef / #else / #endif 조건부 컴파일 → 첫 번째 분기 유지.
     * 라인 번호는 빈 줄로 대체하여 유지.
     *
     * 매크로 상수의 숫자 치환은 하지 않는다(spec 008) — ANTLR C 문법은 `x[LEN_SQL + 1]` 의
     * 식별자 상수식을 정상 파싱하며, 치환은 initValue 의 심볼을 소실시켜 analyzer 의
     * INIT_BY(정의 시 참조) 링킹을 깨뜨렸다(헤더 매크로만 치환되는 비대칭 포함).
     */
    private String preprocessSource(String source) {
        // 1단계: 조건부 컴파일 처리
        StringBuilder normalizedSource = new StringBuilder();
        String[] lines = source.split("\n", -1);
        int depth = 0;
        int skipDepth = 0;
        boolean skipping = false;

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.startsWith("#ifdef") || trimmed.startsWith("#ifndef")
                    || (trimmed.startsWith("#if ") || trimmed.equals("#if"))) {
                depth++;
                normalizedSource.append("\n");
                continue;
            }

            if (trimmed.startsWith("#else") || trimmed.startsWith("#elif")) {
                if (!skipping && depth > 0) {
                    skipping = true;
                    skipDepth = depth;
                }
                normalizedSource.append("\n");
                continue;
            }

            if (trimmed.startsWith("#endif")) {
                if (skipping && depth == skipDepth) {
                    skipping = false;
                    skipDepth = 0;
                }
                depth--;
                if (depth < 0) depth = 0;
                normalizedSource.append("\n");
                continue;
            }

            if (skipping) {
                normalizedSource.append("\n");
            } else {
                normalizedSource.append(line).append("\n");
            }
        }

        return normalizedSource.toString();
    }

    private String readFileContent(Path path) throws Exception {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (MalformedInputException e) {
            try {
                return Files.readString(path, Charset.forName("EUC-KR"));
            } catch (Exception e2) {
                return Files.readString(path, Charset.forName("MS949"));
            }
        }
    }
}
