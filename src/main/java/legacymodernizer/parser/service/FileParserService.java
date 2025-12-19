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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

/**
 * 파일 저장/파싱 서비스
 * 
 * 저장 구조:
 *   data/{sessionUUID}/{projectName}/
 *     ├── source/   → 소스 파일 (원본 폴더 구조 유지)
 *     ├── ddl/      → DDL 파일 (원본 폴더 구조 유지)
 *     └── analysis/ → 파싱 결과 JSON (source와 동일 구조)
 */
@Slf4j
@Service
public class FileParserService {

    private static final String BASE_DIR = resolveBaseDir();
    private static final String SOURCE = "source";
    private static final String DDL = "ddl";
    private static final String ANALYSIS = "analysis";

    private static String resolveBaseDir() {
        String dockerContext = System.getenv("DOCKER_COMPOSE_CONTEXT");
        if (dockerContext != null) {
            return dockerContext;
        }
        return new File(System.getProperty("user.dir")).getParent() + File.separator + "data";
    }

    // ═══════════════════════════════════════════════════════════════════
    // 경로
    // ═══════════════════════════════════════════════════════════════════

    public Path projectRoot(String session, String project) {
        return Paths.get(BASE_DIR, session, project);
    }

    public Path sourceDir(String session, String project) {
        return projectRoot(session, project).resolve(SOURCE);
    }

    public Path ddlDir(String session, String project) {
        return projectRoot(session, project).resolve(DDL);
    }

    public Path analysisDir(String session, String project) {
        return projectRoot(session, project).resolve(ANALYSIS);
    }

    // ═══════════════════════════════════════════════════════════════════
    // 파일 업로드
    // ═══════════════════════════════════════════════════════════════════

    /**
     * 파일 업로드
     * 
     * @param session 세션 UUID
     * @param project 프로젝트명
     * @param files   업로드 파일들 (filename: {project}/{상대경로})
     * @return {projectName, files: [...], ddlFiles: [...]}
     */
    public Map<String, Object> uploadFiles(String session, String project, MultipartFile[] files) {
        List<Map<String, String>> srcList = new ArrayList<>();
        List<Map<String, String>> ddlList = new ArrayList<>();

        Path sourceBase = sourceDir(session, project);
        Path ddlBase = ddlDir(session, project);
        String prefix = project + "/";

        for (MultipartFile mf : files) {
            if (mf == null || mf.isEmpty()) continue;

            String originalName = mf.getOriginalFilename();
            if (originalName == null || originalName.isBlank()) continue;

            // {project}/ 접두사 제거 → 상대 경로
            String relativePath = originalName.startsWith(prefix)
                    ? originalName.substring(prefix.length())
                    : originalName;

            // ddl/ 로 시작하면 DDL, 아니면 소스
            boolean isDdl = relativePath.startsWith("ddl/") || relativePath.startsWith("ddl\\");

            try {
                if (isDdl) {
                    String ddlPath = relativePath.substring(4); // "ddl/" 제거
                    Path dest = ddlBase.resolve(ddlPath);
                    saveFile(mf, dest);

                    ddlList.add(Map.of(
                            "fileName", relativePath,
                            "fileContent", readContent(dest)));
                    log.debug("  [DDL] {}", relativePath);
                } else {
                    Path dest = sourceBase.resolve(relativePath);
                    saveFile(mf, dest);

                    srcList.add(Map.of(
                            "fileName", relativePath,
                            "fileContent", readContent(dest)));
                    log.debug("  [SOURCE] {}", relativePath);
                }
            } catch (IOException e) {
                throw new RuntimeException("파일 저장 실패: " + originalName, e);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("projectName", project);
        result.put("files", srcList);
        result.put("ddlFiles", ddlList);
        return result;
    }

    private void saveFile(MultipartFile mf, Path dest) throws IOException {
        Files.createDirectories(dest.getParent());
        Files.copy(mf.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
    }

    private String readContent(Path path) throws IOException {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (MalformedInputException e) {
            try {
                return Files.readString(path, Charset.forName("EUC-KR"));
            } catch (Exception e2) {
                return Files.readString(path, Charset.forName("MS949"));
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // 파싱
    // ═══════════════════════════════════════════════════════════════════

    @FunctionalInterface
    public interface ParsingFunction {
        void parse(File file, String outputPath) throws Exception;
    }

    /**
     * source/ 하위 모든 파일을 파싱하여 analysis/ 에 동일 구조로 JSON 저장
     */
    public void parseProject(String session, String project, ParsingFunction parser) {
        Path sourceBase = sourceDir(session, project);
        Path analysisBase = analysisDir(session, project);

        if (!Files.exists(sourceBase)) {
            throw new RuntimeException("소스 디렉토리 없음: " + sourceBase);
        }

        try {
            Files.walk(sourceBase)
                    .filter(Files::isRegularFile)
                    .forEach(file -> parseFile(file, sourceBase, analysisBase, parser));
        } catch (IOException e) {
            throw new RuntimeException("디렉토리 탐색 실패: " + sourceBase, e);
        }
    }

    private void parseFile(Path file, Path sourceBase, Path analysisBase, ParsingFunction parser) {
        try {
            // 상대 경로 유지
            Path relative = sourceBase.relativize(file);

            // 확장자 → .json
            String relStr = relative.toString();
            int dot = relStr.lastIndexOf('.');
            String jsonPath = (dot > 0 ? relStr.substring(0, dot) : relStr) + ".json";

            Path output = analysisBase.resolve(jsonPath);
            Files.createDirectories(output.getParent());

            parser.parse(file.toFile(), output.toString());
            log.info("  [PARSED] {}", relative);

        } catch (Exception e) {
            throw new RuntimeException("파싱 실패: " + file, e);
        }
    }
}
