package legacymodernizer.parser.service.parsing;

import java.io.File;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import legacymodernizer.parser.service.ParseProgressTracker;
import legacymodernizer.parser.service.StreamCallback;

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
     * 전체 프로젝트 파싱 (스트림 방식)
     * 진행 상황을 실시간으로 콜백에 전달
     * 
     * @param session  세션 UUID
     * @param project  프로젝트명
     * @param callback 스트림 콜백
     */
    void parseWithStream(String session, String project, StreamCallback callback);

    /**
     * 단일 파일 ANTLR 파싱 (구현체별로 다른 파서 사용)
     */
    void parseFile(File file, String outputPath) throws Exception;

    /**
     * 단일 파일 ANTLR 파싱 (스트림 방식)
     * 진행 상황을 ParseProgressTracker를 통해 전달
     * 
     * @param file       파싱할 파일
     * @param outputPath 출력 경로
     * @param tracker    진행 상황 추적기
     */
    void parseFileWithStream(File file, String outputPath, ParseProgressTracker tracker) throws Exception;

    /**
     * 지원하는 target 타입 (예: "java", "plsql", "postgresql")
     */
    String getSupportedTargetType();
}
