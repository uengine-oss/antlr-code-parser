package legacymodernizer.parser.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

/**
 * 파일 저장/파싱 서비스
 * 
 * 저장 구조:
 * data/{sessionUUID}/{projectName}/
 *   ├── ddl/              → DDL 파일
 *   └── {systemName}/     → 시스템별 폴더
 *       ├── src/          → 소스 파일
 *       └── analysis/     → 파싱 결과 JSON
 */
@Slf4j
@Service
public class FileParserService {

    private static final String BASE_DIR = System.getenv("DOCKER_COMPOSE_CONTEXT") != null ?
            System.getenv("DOCKER_COMPOSE_CONTEXT") :
            new File(System.getProperty("user.dir")).getParent() + File.separator + "data";

    private static final String SRC_DIR = "src";
    private static final String DDL_DIR = "ddl";
    private static final String ANALYSIS_DIR = "analysis";

    // ========================================
    // 경로 유틸리티
    // ========================================

    private static String toBaseName(String name) {
        if (name == null) return null;
        return Paths.get(name).getFileName().toString();
    }

    private static String toBaseNameWithoutExt(String name) {
        String base = toBaseName(name);
        if (base == null) return null;
        int idx = base.lastIndexOf('.');
        return idx > 0 ? base.substring(0, idx) : base;
    }

    /**
     * 프로젝트 루트: data/{session}/{project}
     */
    public String getProjectRoot(String sessionUUID, String projectName) {
        return BASE_DIR + File.separator + sessionUUID + File.separator + projectName;
    }

    /**
     * 시스템 소스 디렉토리: data/{session}/{project}/{system}/src
     */
    public String getSystemSrcDir(String sessionUUID, String projectName, String systemName) {
        return getProjectRoot(sessionUUID, projectName) + File.separator + systemName + File.separator + SRC_DIR;
    }

    /**
     * 시스템 분석 디렉토리: data/{session}/{project}/{system}/analysis
     */
    public String getSystemAnalysisDir(String sessionUUID, String projectName, String systemName) {
        return getProjectRoot(sessionUUID, projectName) + File.separator + systemName + File.separator + ANALYSIS_DIR;
    }

    /**
     * DDL 디렉토리: data/{session}/{project}/ddl
     */
    public String getDdlDir(String sessionUUID, String projectName) {
        return getProjectRoot(sessionUUID, projectName) + File.separator + DDL_DIR;
    }

    private void createDirectoryIfNotExists(String path) throws IOException {
        Path dirPath = Paths.get(path);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
    }

    // ========================================
    // 파일 I/O
    // ========================================

    public String readFileContent(File file) throws IOException {
        try {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (MalformedInputException e) {
            try {
                return Files.readString(file.toPath(), Charset.forName("EUC-KR"));
            } catch (Exception e2) {
                return Files.readString(file.toPath(), Charset.forName("MS949"));
            }
        }
    }

    public String readAnalysisResult(String sessionUUID, String projectName, String systemName, String fileName) throws IOException {
        String analysisDir = getSystemAnalysisDir(sessionUUID, projectName, systemName);
        String baseNoExt = toBaseNameWithoutExt(fileName);
        File analysisFile = new File(analysisDir, baseNoExt + ".json");
        if (analysisFile.exists()) {
            return Files.readString(analysisFile.toPath(), StandardCharsets.UTF_8);
        }
        return null;
    }

    // ========================================
    // 업로드 처리
    // ========================================

    /**
     * 시스템별 소스 파일 업로드
     * @return 성공한 파일 목록 [{system, fileName, fileContent}]
     */
    public List<Map<String, String>> uploadSystemFiles(String sessionUUID,
                                                        String projectName,
                                                        List<?> systems,
                                                        Map<String, MultipartFile> nameToFile) {
        List<Map<String, String>> result = new ArrayList<>();

        for (Object sys : systems) {
            if (!(sys instanceof Map<?, ?>)) continue;
            Map<?, ?> sysMap = (Map<?, ?>) sys;
            String systemName = (String) sysMap.get("name");
            Object spObj = sysMap.get("sp");
            if (systemName == null || systemName.isBlank() || !(spObj instanceof List<?>)) continue;

            String srcDir = getSystemSrcDir(sessionUUID, projectName, systemName);
            
            for (Object sp : (List<?>) spObj) {
                if (!(sp instanceof String)) continue;
                String fileName = (String) sp;
                String baseName = toBaseName(fileName);

                MultipartFile mf = nameToFile.getOrDefault(baseName.toLowerCase(), null);
                if (mf == null || mf.isEmpty()) {
                    throw new RuntimeException("파일 없음: " + fileName);
                }

                try {
                    createDirectoryIfNotExists(srcDir);
                    File outFile = new File(srcDir, baseName);
                    Files.copy(mf.getInputStream(), outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    
                    String content = readFileContent(outFile);
                    result.add(Map.of(
                        "system", systemName,
                        "fileName", baseName,
                        "fileContent", content
                    ));
                } catch (IOException e) {
                    throw new RuntimeException("저장 실패: " + fileName, e);
                }
            }
        }

        return result;
    }

    /**
     * DDL 파일 업로드
     * @return 성공한 파일 목록 [{fileName, fileContent}]
     */
    public List<Map<String, String>> uploadDdlFiles(String sessionUUID,
                                                     String projectName,
                                                     List<?> ddlList,
                                                     Map<String, MultipartFile> nameToFile) {
        List<Map<String, String>> result = new ArrayList<>();
        if (ddlList == null) return result;

        String ddlDir = getDdlDir(sessionUUID, projectName);

        for (Object obj : ddlList) {
            if (!(obj instanceof String)) continue;
            String fileName = (String) obj;
            String baseName = toBaseName(fileName);

            MultipartFile mf = nameToFile.getOrDefault(baseName.toLowerCase(), null);
            if (mf == null || mf.isEmpty()) continue;

            try {
                createDirectoryIfNotExists(ddlDir);
                File outFile = new File(ddlDir, baseName);
                Files.copy(mf.getInputStream(), outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                String content = readFileContent(outFile);
                result.add(Map.of(
                    "fileName", baseName,
                    "fileContent", content
                ));
            } catch (IOException e) {
                throw new RuntimeException("DDL 저장 실패: " + fileName, e);
            }
        }

        return result;
    }

    // ========================================
    // 파싱 처리
    // ========================================

    @FunctionalInterface
    public interface ParsingFunction {
        void parse(File file, String outputPath) throws Exception;
    }

    /**
     * 시스템별 파싱 수행
     * @return 성공한 파일 목록 [{system, fileName, analysisResult}]
     */
    public List<Map<String, String>> parseSystemFiles(String sessionUUID,
                                                       String projectName,
                                                       List<?> systems,
                                                       ParsingFunction parsingFunction) {
        List<Map<String, String>> result = new ArrayList<>();

        for (Object sys : systems) {
            if (!(sys instanceof Map<?, ?>)) continue;
            Map<?, ?> sysMap = (Map<?, ?>) sys;
            String systemName = (String) sysMap.get("name");
            Object spObj = sysMap.get("sp");
            if (systemName == null || !(spObj instanceof List<?>)) continue;

            String srcDir = getSystemSrcDir(sessionUUID, projectName, systemName);
            String analysisDir = getSystemAnalysisDir(sessionUUID, projectName, systemName);

            for (Object sp : (List<?>) spObj) {
                if (!(sp instanceof String)) continue;
                String fileName = (String) sp;
                String baseName = toBaseName(fileName);

                File srcFile = new File(srcDir, baseName);
                if (!srcFile.exists()) {
                    throw new RuntimeException("파일 없음: " + systemName + "/" + baseName);
                }

                try {
                    createDirectoryIfNotExists(analysisDir);
                    String baseNoExt = toBaseNameWithoutExt(baseName);
                    String outputPath = analysisDir + File.separator + baseNoExt + ".json";

                    parsingFunction.parse(srcFile, outputPath);

                    String analysisResult = Files.readString(new File(outputPath).toPath(), StandardCharsets.UTF_8);
                    result.add(Map.of(
                        "system", systemName,
                        "fileName", baseName,
                        "analysisResult", analysisResult
                    ));

                    log.info("  파싱 완료: {}/{}", systemName, baseName);
                } catch (Exception e) {
                    throw new RuntimeException("파싱 실패: " + systemName + "/" + baseName + " - " + e.getMessage(), e);
                }
            }
        }

        return result;
    }
}
