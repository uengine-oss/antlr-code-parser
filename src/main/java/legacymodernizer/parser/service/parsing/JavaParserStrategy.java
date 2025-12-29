package legacymodernizer.parser.service.parsing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
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
import legacymodernizer.parser.service.ParseProgressTracker;
import legacymodernizer.parser.service.StreamCallback;
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
    public Map<String, Object> upload(String session, String project, MultipartFile[] files) {
        return fileParserService.uploadFiles(session, project, files);
    }

    @Override
    public void parse(String session, String project) {
        fileParserService.parseProject(session, project, this::parseFile);
    }

    @Override
    public void parseWithStream(String session, String project, StreamCallback callback) {
        fileParserService.parseProjectWithStream(session, project, this::parseFileWithStream, callback);
    }

    @Override
    public void parseFile(File file, String outputPath) throws Exception {
        log.debug("[Java] 파싱: {}", file.getName());

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
    public void parseFileWithStream(File file, String outputPath, ParseProgressTracker tracker) throws Exception {
        log.debug("[Java] 파싱 (스트림): {}", file.getName());

        try (InputStream in = new FileInputStream(file)) {
            CharStream charStream = CharStreams.fromStream(in);
            Java20Lexer lexer = new Java20Lexer(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            Java20Parser parser = new Java20Parser(tokens);

            Java20Parser.Start_Context tree = parser.start_();

            // 스트림 콜백을 전달하는 Listener 사용
            CustomJavaListener listener = new CustomJavaListener(tokens, tracker);
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
