package legacymodernizer.parser.service.strategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.stereotype.Component;

import legacymodernizer.parser.antlr.python.PythonAstListener;
import legacymodernizer.parser.antlr.python.PythonLexer;
import legacymodernizer.parser.antlr.python.PythonParser;
import legacymodernizer.parser.service.FileStorageService;
import legacymodernizer.parser.service.ParseProgressTracker;
import lombok.extern.slf4j.Slf4j;

/**
 * Python 파싱 전략
 */
@Slf4j
@Component
public class PythonParserStrategy extends AbstractParserStrategy {

    public PythonParserStrategy(FileStorageService storageService) {
        super(storageService);
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

            PythonAstListener listener = new PythonAstListener(tokens, tracker);
            listener.setFileInfo(file.getName(), computeRelativePath(file));

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
