package legacymodernizer.parser.service.parsing;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import legacymodernizer.parser.antlr.python.CustomPythonListener;
import legacymodernizer.parser.antlr.python.PythonLexer;
import legacymodernizer.parser.antlr.python.PythonParser;
import legacymodernizer.parser.service.FileParserService;
import legacymodernizer.parser.service.ParseProgressTracker;
import legacymodernizer.parser.service.StreamCallback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Python 파싱 전략
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PythonParserStrategy implements TargetParserStrategy {

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
        log.debug("[Python] 파싱: {}", file.getName());

        try (InputStream in = new FileInputStream(file)) {
            CharStream charStream = CharStreams.fromStream(in);
            PythonLexer lexer = new PythonLexer(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            PythonParser parser = new PythonParser(tokens);

            PythonParser.RootContext tree = parser.root();

            CustomPythonListener listener = new CustomPythonListener(tokens, tracker);

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
        return "python";
    }

    @Override
    public Set<String> getTargetExtensions() {
        return Set.of(".py");
    }
}
