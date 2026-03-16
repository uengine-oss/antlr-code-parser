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
        log.debug("[C] 파싱: {}", file.getName());

        try (InputStream in = new FileInputStream(file)) {
            CharStream charStream = CharStreams.fromStream(in);
            CLexer lexer = new CLexer(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            CParser parser = new CParser(tokens);

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
}
