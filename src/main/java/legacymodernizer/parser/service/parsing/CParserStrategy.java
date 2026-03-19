package legacymodernizer.parser.service.parsing;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import legacymodernizer.parser.antlr.c.CLexer;
import legacymodernizer.parser.antlr.c.CParser;
import legacymodernizer.parser.antlr.c.CustomCListener;
import legacymodernizer.parser.service.FileParserService;
import legacymodernizer.parser.service.ParseProgressTracker;
import legacymodernizer.parser.service.StreamCallback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * C 파싱 전략
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CParserStrategy implements TargetParserStrategy {

    private final FileParserService fileParserService;

    /** 소스 파일들에서 수집한 사용자 정의 타입 이름 */
    private Set<String> collectedTypeNames = new HashSet<>();

    @Override
    public Map<String, Object> upload(MultipartFile[] files) {
        return fileParserService.uploadFiles(files, getTargetExtensions());
    }

    @Override
    public void parseWithStream(StreamCallback callback) {
        // 파싱 전에 소스 파일에서 매크로 상수 + typedef/struct/enum 이름을 수집
        collectMacroConstants();
        collectTypeNamesFromSource();
        fileParserService.parseProjectWithStream(this::parseFileWithStream, callback);
    }

    @Override
    public void parseFileWithStream(File file, String outputPath, ParseProgressTracker tracker) throws Exception {
        log.debug("[C] 파싱: {}", file.getName());

        try (InputStream in = new FileInputStream(file)) {
            // 전처리기 조건부 컴파일(#ifdef/#else/#endif) 처리
            String source = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            source = preprocessSource(source);
            CharStream charStream = CharStreams.fromString(source);
            CLexer lexer = new CLexer(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            CParser parser = new CParser(tokens);

            // 수집된 사용자 정의 타입 이름을 파서에 등록
            if (!collectedTypeNames.isEmpty()) {
                parser.registerTypeNames(collectedTypeNames);
            }

            CParser.CompilationUnitContext tree = parser.compilationUnit();

            CustomCListener listener = new CustomCListener(tokens, tracker);

            // 파일 정보 설정
            String fileName = file.getName();
            Path sourceDir = fileParserService.sourceDir();
            Path filePath = file.toPath();
            String relativePath = null;
            try {
                if (filePath.startsWith(sourceDir)) {
                    relativePath = sourceDir.relativize(filePath).toString().replace('\\', '/');
                } else {
                    relativePath = filePath.toString().replace('\\', '/');
                }
            } catch (Exception e) {
                relativePath = fileName;
            }
            listener.setFileInfo(fileName, relativePath);

            new ParseTreeWalker().walk(listener, tree);

            Files.writeString(Path.of(outputPath), listener.getRoot().toJson(), StandardCharsets.UTF_8);
        }
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
        Path sourceDir = fileParserService.sourceDir();
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

    /** #define NAME 숫자값 패턴 (매크로 상수) */
    private static final Pattern DEFINE_CONST = Pattern.compile(
            "^#\\s*define\\s+(\\w+)\\s+(\\d+)");

    /** 소스 파일 전체에서 수집된 #define 매크로 상수 */
    private Map<String, String> collectedMacroConstants = new java.util.HashMap<>();

    /**
     * 소스 디렉토리의 모든 파일에서 #define 매크로 상수를 수집
     */
    private void collectMacroConstants() {
        collectedMacroConstants.clear();
        Path sourceDir = fileParserService.sourceDir();
        if (!Files.exists(sourceDir)) return;

        try {
            Files.walk(sourceDir)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".h"))
                    .forEach(p -> {
                        try {
                            String content = readFileContent(p);
                            for (String line : content.split("\n")) {
                                Matcher m = DEFINE_CONST.matcher(line.trim());
                                if (m.find()) {
                                    collectedMacroConstants.put(m.group(1), m.group(2));
                                }
                            }
                        } catch (Exception e) {
                            log.warn("매크로 상수 수집 중 파일 읽기 실패: {}", p, e);
                        }
                    });

            if (!collectedMacroConstants.isEmpty()) {
                log.info("소스에서 수집한 매크로 상수: {}개", collectedMacroConstants.size());
            }
        } catch (Exception e) {
            log.warn("매크로 상수 수집 실패", e);
        }
    }

    /**
     * C 전처리기 처리:
     * 1. #ifdef / #ifndef / #else / #endif 조건부 컴파일 → 첫 번째 분기 유지
     * 2. #define NAME 숫자 → 소스 내 매크로 상수 치환
     * 라인 번호는 빈 줄로 대체하여 유지.
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

        // 2단계: 매크로 상수 치환 (배열 크기 등에서 사용되는 #define 값)
        // #define 라인 자체는 치환에서 제외 (HIDDEN 채널에서 원본 추출을 위해)
        String processed = result.toString();
        String[] processedLines = processed.split("\n", -1);
        for (int li = 0; li < processedLines.length; li++) {
            String trimmedLine = processedLines[li].trim();
            if (trimmedLine.startsWith("#define") || trimmedLine.startsWith("# define")) {
                continue; // #define 라인은 치환하지 않음
            }
            String replaced = processedLines[li];
            for (Map.Entry<String, String> entry : collectedMacroConstants.entrySet()) {
                replaced = replaced.replaceAll("\\b" + Pattern.quote(entry.getKey()) + "\\b", entry.getValue());
            }
            processedLines[li] = replaced;
        }
        processed = String.join("\n", processedLines);

        return processed;
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
