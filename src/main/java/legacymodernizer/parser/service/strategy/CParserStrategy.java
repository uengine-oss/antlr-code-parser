package legacymodernizer.parser.service.strategy;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
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
import legacymodernizer.parser.service.FileStorageService;
import legacymodernizer.parser.service.ParseProgressTracker;
import lombok.extern.slf4j.Slf4j;

/**
 * C 파싱 전략
 */
@Slf4j
@Component
public class CParserStrategy extends AbstractParserStrategy {

    /** 소스 파일들에서 수집한 사용자 정의 타입 이름 */
    private Set<String> collectedTypeNames = new HashSet<>();

    public CParserStrategy(FileStorageService storageService) {
        super(storageService);
    }

    /** C 는 파일별 파싱 전, .c/.h 전체에서 typedef/struct 타입을 먼저 수집해야 정확하다. */
    @Override
    public void prepare() {
        collectTypeNamesFromSource();
    }

    @Override
    public void parseFileWithStream(File file, String outputPath, ParseProgressTracker tracker) throws Exception {
        log.debug("[C] 파싱: {}", file.getName());

        String source = readFileContent(file.toPath());
        source = preprocessSource(source);
        CharStream charStream = CharStreams.fromString(source);
        CLexer lexer = new CLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CParser parser = new CParser(tokens);

        if (!collectedTypeNames.isEmpty()) {
            parser.registerTypeNames(collectedTypeNames);
        }

        CParser.CompilationUnitContext tree = parser.compilationUnit();

        CAstListener listener = new CAstListener(tokens, tracker);
        listener.setFileInfo(file.getName(), computeRelativePath(file));

        new ParseTreeWalker().walk(listener, tree);

        Files.writeString(Path.of(outputPath), listener.getRoot().toJson(), StandardCharsets.UTF_8);
    }

    @Override
    public String getSupportedTargetType() {
        return "c";
    }

    @Override
    public Set<String> getTargetExtensions() {
        return Set.of(".c", ".h");
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
        Path sourceDir = storageService.sourceDir();
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
        StringBuilder result = new StringBuilder();
        String[] lines = source.split("\n", -1);
        int depth = 0;
        int skipDepth = 0;
        boolean skipping = false;

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.startsWith("#ifdef") || trimmed.startsWith("#ifndef")
                    || (trimmed.startsWith("#if ") || trimmed.equals("#if"))) {
                depth++;
                result.append("\n");
                continue;
            }

            if (trimmed.startsWith("#else") || trimmed.startsWith("#elif")) {
                if (!skipping && depth > 0) {
                    skipping = true;
                    skipDepth = depth;
                }
                result.append("\n");
                continue;
            }

            if (trimmed.startsWith("#endif")) {
                if (skipping && depth == skipDepth) {
                    skipping = false;
                    skipDepth = 0;
                }
                depth--;
                if (depth < 0) depth = 0;
                result.append("\n");
                continue;
            }

            if (skipping) {
                result.append("\n");
            } else {
                result.append(line).append("\n");
            }
        }

        return result.toString();
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
