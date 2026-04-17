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

import legacymodernizer.parser.antlr.postgresql.PostgreSqlAstListener;
import legacymodernizer.parser.antlr.postgresql.PostgreSQLLexer;
import legacymodernizer.parser.antlr.postgresql.PostgreSQLParser;
import legacymodernizer.parser.service.FileStorageService;
import legacymodernizer.parser.service.ParsingOrchestrator;
import legacymodernizer.parser.service.ParseProgressTracker;
import lombok.extern.slf4j.Slf4j;

/**
 * PostgreSQL 파싱 전략
 */
@Slf4j
@Component
public class PostgreSqlParserStrategy extends AbstractParserStrategy {

    public PostgreSqlParserStrategy(FileStorageService storageService, ParsingOrchestrator orchestrator) {
        super(storageService, orchestrator);
    }

    @Override
    public void parseFileWithStream(File file, String outputPath, ParseProgressTracker tracker) throws Exception {
        log.debug("[PostgreSQL] 파싱: {}", file.getName());

        try (InputStream in = new FileInputStream(file)) {
            CharStream charStream = CharStreams.fromStream(in);
            PostgreSQLLexer lexer = new PostgreSQLLexer(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            PostgreSQLParser parser = new PostgreSQLParser(tokens);

            PostgreSQLParser.RootContext tree = parser.root();

            PostgreSqlAstListener listener = new PostgreSqlAstListener(tokens, tracker);
            new ParseTreeWalker().walk(listener, tree);

            Files.writeString(Path.of(outputPath), listener.getRoot().toJson(), StandardCharsets.UTF_8);
        }
    }

    @Override
    public String getSupportedTargetType() {
        return "postgresql";
    }

    @Override
    public Set<String> getTargetExtensions() {
        return Set.of(".sql");
    }
}
