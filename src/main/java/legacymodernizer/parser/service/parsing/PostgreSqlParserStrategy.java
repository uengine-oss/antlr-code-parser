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

import legacymodernizer.parser.antlr.postgresql.CustomPostgreSQLListener;
import legacymodernizer.parser.antlr.postgresql.PostgreSQLLexer;
import legacymodernizer.parser.antlr.postgresql.PostgreSQLParser;
import legacymodernizer.parser.service.FileParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * PostgreSQL 파싱 전략
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostgreSqlParserStrategy implements TargetParserStrategy {

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
        log.debug("[ANTLR PostgreSQL] 파싱: {}", file.getName());
        try (InputStream in = new FileInputStream(file)) {
            CharStream charStream = CharStreams.fromStream(in);
            PostgreSQLLexer lexer = new PostgreSQLLexer(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            PostgreSQLParser parser = new PostgreSQLParser(tokens);

            PostgreSQLParser.RootContext tree = parser.root();

            CustomPostgreSQLListener listener = new CustomPostgreSQLListener(tokens);
            new ParseTreeWalker().walk(listener, tree);

            try (FileWriter writer = new FileWriter(outputPath)) {
                writer.write(listener.getRoot().toJson());
            }
        }
    }

    @Override
    public String getSupportedTargetType() {
        return "postgresql";
    }
}
