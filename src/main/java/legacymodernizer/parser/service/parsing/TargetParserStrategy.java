package legacymodernizer.parser.service.parsing;

import java.io.File;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

/**
 * Target별 파싱 전략 인터페이스
 * 
 * 구현체: JavaParserStrategy, PlSqlParserStrategy, PostgreSqlParserStrategy
 */
public interface TargetParserStrategy {

    /**
     * 파일 업로드
     * 
     * @return {projectName, files: [{fileName, fileContent}], ddlFiles: [{fileName, fileContent}]}
     */
    Map<String, Object> upload(String session, String project, MultipartFile[] files);

    /**
     * 전체 프로젝트 파싱 (source → analysis)
     */
    void parse(String session, String project);

    /**
     * 단일 파일 ANTLR 파싱 (구현체별로 다른 파서 사용)
     */
    void parseFile(File file, String outputPath) throws Exception;

    /**
     * 지원하는 target 타입 (예: "java", "plsql", "postgresql")
     */
    String getSupportedTargetType();
}
