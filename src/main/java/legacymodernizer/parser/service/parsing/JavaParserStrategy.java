package legacymodernizer.parser.service.parsing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import legacymodernizer.parser.antlr.java.CustomJavaListener;
import legacymodernizer.parser.antlr.java.Java20Lexer;
import legacymodernizer.parser.antlr.java.Java20Parser;
import legacymodernizer.parser.service.FileParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Java 파싱 전략 구현체
 * - 클래스/인터페이스 구조 분석
 * - 메서드/필드 추출
 * - 의존 관계 (메서드 호출, 객체 생성) 추출
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JavaParserStrategy implements TargetParserStrategy {

    private final FileParserService fileParserService;

    @Override
    public Map<String, Object> processUploadByMetadata(String sessionUUID,
                                                      String projectName,
                                                      Object systemsObj,
                                                      Object ddlObj,
                                                      Object seqObj,
                                                      Map<String, MultipartFile> nameToFile) {
        return fileParserService.processUploadByMetadata(
            sessionUUID, projectName, systemsObj, ddlObj, seqObj, nameToFile
        );
    }

    @Override
    public Map<String, Object> processParsingBySystems(String sessionUUID,
                                                      String projectName,
                                                      List<?> systems) {
        return fileParserService.processParsingBySystemsWithStrategy(
            sessionUUID,
            projectName,
            systems,
            this::parseFile
        );
    }

    @Override
    public void parseFile(File file, String outputPath) throws Exception {
        log.debug("      [ANTLR Java 파싱 시작] - {}", file.getName());
        try (InputStream in = new FileInputStream(file)) {
            CharStream charStream = CharStreams.fromStream(in);
            Java20Lexer lexer = new Java20Lexer(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            Java20Parser parser = new Java20Parser(tokens);

            Java20Parser.Start_Context tree = parser.start_();

            CustomJavaListener listener = new CustomJavaListener(tokens);
            new ParseTreeWalker().walk(listener, tree);

            File analysisFile = new File(outputPath);
            try (FileWriter writer = new FileWriter(analysisFile)) {
                writer.write(listener.getRoot().toJson());
            }
            log.debug("      → 분석 결과 저장: {}", analysisFile.getName());
        }
    }

    @Override
    public String getSupportedTargetType() {
        return "java";
    }
}

