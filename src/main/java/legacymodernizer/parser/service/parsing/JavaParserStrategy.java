package legacymodernizer.parser.service.parsing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

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
    public Map<String, Object> upload(MultipartFile[] files) {
        return fileParserService.uploadFiles(files, getTargetExtensions());
    }

    @Override
    public void parseWithStream(StreamCallback callback) {
        fileParserService.parseProjectWithStream(this::parseFileWithStream, callback);
    }

    @Override
    public void parseFileWithStream(File file, String outputPath, ParseProgressTracker tracker) throws Exception {
        log.debug("[Java] 파싱: {}", file.getName());

        try (InputStream in = new FileInputStream(file)) {
            CharStream charStream = CharStreams.fromStream(in);
            Java20Lexer lexer = new Java20Lexer(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            Java20Parser parser = new Java20Parser(tokens);

            Java20Parser.Start_Context tree = parser.start_();

            CustomJavaListener listener = new CustomJavaListener(tokens, tracker);
            
            // 파일 정보 설정
            String fileName = file.getName();
            // sourceDir 기준 상대 경로 계산
            Path sourceDir = fileParserService.sourceDir();
            Path filePath = file.toPath();
            String relativePath = null;
            try {
                if (filePath.startsWith(sourceDir)) {
                    relativePath = sourceDir.relativize(filePath).toString().replace('\\', '/');
                } else {
                    // sourceDir 밖이면 절대 경로 사용
                    relativePath = filePath.toString().replace('\\', '/');
                }
            } catch (Exception e) {
                // 경로 계산 실패 시 파일명만 사용
                relativePath = fileName;
            }
            listener.setFileInfo(fileName, relativePath);
            
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

    @Override
    public Set<String> getTargetExtensions() {
        return Set.of(".java");
    }
}
