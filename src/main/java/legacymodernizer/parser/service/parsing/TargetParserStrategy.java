package legacymodernizer.parser.service.parsing;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

/**
 * Target별 파싱 전략 인터페이스
 * - 전략 패턴을 통해 Target 종류에 따른 파싱 로직을 캡슐화
 */
public interface TargetParserStrategy {
    
    /**
     * 메타데이터 기반 파일 업로드 처리
     * @param sessionUUID 세션 UUID
     * @param projectName 프로젝트명
     * @param systemsObj systems 배열
     * @param ddlObj ddl 배열
     * @param seqObj sequence 배열
     * @param nameToFile 업로드 파일 맵 (파일명 소문자 → MultipartFile)
     * @return {successFiles}
     */
    Map<String, Object> processUploadByMetadata(String sessionUUID,
                                                String projectName,
                                                Object systemsObj,
                                                Object ddlObj,
                                                Object seqObj,
                                                Map<String, MultipartFile> nameToFile);
    
    /**
     * 시스템별 파싱 처리 (ANTLR 분석)
     * @param sessionUUID 세션 UUID
     * @param projectName 프로젝트명
     * @param systems systems 배열
     * @return {successFiles}
     */
    Map<String, Object> processParsingBySystems(String sessionUUID,
                                                String projectName,
                                                List<?> systems);
    
    /**
     * ANTLR 파싱 실행 (Target별 구현)
     * @param file 파싱 대상 파일
     * @param outputPath 출력 JSON 파일 경로
     */
    void parseFile(File file, String outputPath) throws Exception;
    
    /**
     * 지원하는 Target 타입 반환
     * @return Target 타입 (예: "oracle", "postgresql", "java")
     */
    String getSupportedTargetType();
}

