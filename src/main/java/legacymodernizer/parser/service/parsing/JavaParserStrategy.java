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
 * Java 파싱 전략
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JavaParserStrategy implements TargetParserStrategy {

    private final FileParserService fileParserService;

    @Override
    public Map<String, Object> upload(String sessionUUID,
                                       String projectName,
                                       List<?> systems,
                                       List<?> ddlList,
                                       Map<String, MultipartFile> nameToFile) {
        var systemFiles = fileParserService.uploadSystemFiles(sessionUUID, projectName, systems, nameToFile);
        var ddlFiles = fileParserService.uploadDdlFiles(sessionUUID, projectName, ddlList, nameToFile);
        return Map.of("systemFiles", systemFiles, "ddlFiles", ddlFiles);
    }

    @Override
    public Map<String, Object> parse(String sessionUUID,
                                      String projectName,
                                      List<?> systems) {
        var files = fileParserService.parseSystemFiles(sessionUUID, projectName, systems, this::parseFile);
        return Map.of("files", files);
    }

    @Override
    public void parseFile(File file, String outputPath) throws Exception {
        log.debug("[ANTLR Java] 파싱: {}", file.getName());
        try (InputStream in = new FileInputStream(file)) {
            CharStream charStream = CharStreams.fromStream(in);
            Java20Lexer lexer = new Java20Lexer(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            Java20Parser parser = new Java20Parser(tokens);

            Java20Parser.Start_Context tree = parser.start_();

            CustomJavaListener listener = new CustomJavaListener(tokens);
            new ParseTreeWalker().walk(listener, tree);

            try (FileWriter writer = new FileWriter(outputPath)) {
                writer.write(listener.getRoot().toJson());
            }
        }
    }

    @Override
    public String getSupportedTargetType() {
        return "java";
    }
}
