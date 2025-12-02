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
import legacymodernizer.parser.service.DbmsFileParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * PostgreSQL (DDL + PL/pgSQL) 파싱 전략
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostgreSqlParserStrategy implements DbmsParserStrategy {

    private final DbmsFileParserService dbmsFileParserService;

    @Override
    public Map<String, Object> processUploadByMetadata(String sessionUUID,
                                                      String projectName,
                                                      Object systemsObj,
                                                      Object ddlObj,
                                                      Object seqObj,
                                                      Map<String, MultipartFile> nameToFile) {
        return dbmsFileParserService.processUploadByMetadata(
            sessionUUID, projectName, systemsObj, ddlObj, seqObj, nameToFile
        );
    }

    @Override
    public Map<String, Object> processParsingBySystems(String sessionUUID,
                                                      String projectName,
                                                      List<?> systems) {
        return dbmsFileParserService.processParsingBySystemsWithStrategy(
            sessionUUID,
            projectName,
            systems,
            this::parseFile
        );
    }

    @Override
    public void parseFile(File file, String outputPath) throws Exception {
        log.debug("      [ANTLR PostgreSQL 파싱 시작]");
        try (InputStream in = new FileInputStream(file)) {
            CharStream charStream = CharStreams.fromStream(in);
            PostgreSQLLexer lexer = new PostgreSQLLexer(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            PostgreSQLParser parser = new PostgreSQLParser(tokens);

            PostgreSQLParser.RootContext tree = parser.root();

            CustomPostgreSQLListener listener = new CustomPostgreSQLListener(tokens);
            new ParseTreeWalker().walk(listener, tree);

            File analysisFile = new File(outputPath);
            try (FileWriter writer = new FileWriter(analysisFile)) {
                writer.write(listener.getRoot().toJson());
            }
            log.debug("      → 분석 결과 저장: {}", analysisFile.getName());
        }
    }

    @Override
    public String getSupportedDbmsType() {
        return "postgresql";
    }
}

