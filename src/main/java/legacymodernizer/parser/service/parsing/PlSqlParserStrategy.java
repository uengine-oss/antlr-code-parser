package legacymodernizer.parser.service.parsing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Map;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import legacymodernizer.parser.antlr.CaseChangingCharStream;
import legacymodernizer.parser.antlr.plsql.CustomPlSqlListener;
import legacymodernizer.parser.antlr.plsql.PlSqlLexer;
import legacymodernizer.parser.antlr.plsql.PlSqlParser;
import legacymodernizer.parser.service.FileParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Oracle PL/SQL 파싱 전략
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlSqlParserStrategy implements TargetParserStrategy {

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
    public void parseFile(File file, String outputPath) throws Exception {
        log.debug("[PL/SQL] 파싱: {}", file.getName());

        try (InputStream in = new FileInputStream(file)) {
            CharStream s = CharStreams.fromStream(in);
            CaseChangingCharStream upper = new CaseChangingCharStream(s, true);
            PlSqlLexer lexer = new PlSqlLexer(upper);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            PlSqlParser parser = new PlSqlParser(tokens);
            ParserRuleContext tree = parser.sql_script();

            CustomPlSqlListener listener = new CustomPlSqlListener(tokens);
            new ParseTreeWalker().walk(listener, tree);

            try (FileWriter writer = new FileWriter(outputPath)) {
                writer.write(listener.getRoot().toJson());
            }
        }
    }

    @Override
    public String getSupportedTargetType() {
        return "oracle";
    }
}
