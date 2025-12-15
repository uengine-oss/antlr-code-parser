package legacymodernizer.parser.service.parsing;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

/**
 * Target별 파싱 전략 인터페이스
 */
public interface TargetParserStrategy {
    
    /**
     * 파일 업로드 처리
     * @return {systemFiles: [{system, fileName, fileContent}], ddlFiles: [{fileName, fileContent}]}
     */
    Map<String, Object> upload(String sessionUUID,
                               String projectName,
                               List<?> systems,
                               List<?> ddlList,
                               Map<String, MultipartFile> nameToFile);
    
    /**
     * 파싱 처리 (ANTLR 분석)
     * @return {files: [{system, fileName, analysisResult}]}
     */
    Map<String, Object> parse(String sessionUUID,
                              String projectName,
                              List<?> systems);
    
    /**
     * ANTLR 파싱 실행 (Target별 구현)
     */
    void parseFile(File file, String outputPath) throws Exception;
    
    /**
     * 지원하는 Target 타입 반환
     */
    String getSupportedTargetType();
}
