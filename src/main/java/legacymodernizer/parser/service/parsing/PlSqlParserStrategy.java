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
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import legacymodernizer.parser.antlr.CaseChangingCharStream;
import legacymodernizer.parser.antlr.CustomPlSqlListener;
import legacymodernizer.parser.antlr.plsql.PlSqlLexer;
import legacymodernizer.parser.antlr.plsql.PlSqlParser;
import legacymodernizer.parser.service.PlSqlFileParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Oracle PL/SQL 파싱 전략 구현체
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlSqlParserStrategy implements DbmsParserStrategy {
    
    private final PlSqlFileParserService plSqlFileParserService;
    
    @Override
    public Map<String, Object> processUploadByMetadata(String sessionUUID,
                                                      String projectName,
                                                      Object systemsObj,
                                                      Object ddlObj,
                                                      Object seqObj,
                                                      Map<String, MultipartFile> nameToFile) {
        return plSqlFileParserService.processUploadByMetadata(
            sessionUUID, projectName, systemsObj, ddlObj, seqObj, nameToFile
        );
    }
    
    @Override
    public Map<String, Object> processParsingBySystems(String sessionUUID,
                                                      String projectName,
                                                      List<?> systems) {
        // PL/SQL 파싱 전략을 사용하여 처리
        return plSqlFileParserService.processParsingBySystemsWithStrategy(
            sessionUUID, 
            projectName, 
            systems, 
            this::parseFile
        );
    }
    
    @Override
    public void parseFile(File file, String outputPath) throws Exception {
        log.debug("      [ANTLR PL/SQL 파싱 시작]");
        try (InputStream in = new FileInputStream(file)) {
            CharStream s = CharStreams.fromStream(in);
            CaseChangingCharStream upper = new CaseChangingCharStream(s, true);
            PlSqlLexer lexer = new PlSqlLexer(upper);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            PlSqlParser parser = new PlSqlParser(tokens);
            ParserRuleContext tree = parser.sql_script();
            CustomPlSqlListener listener = new CustomPlSqlListener(tokens);
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
        return "oracle";
    }
}

