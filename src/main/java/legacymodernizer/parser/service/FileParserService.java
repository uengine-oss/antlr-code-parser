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
import java.util.concurrent.atomic.AtomicInteger;

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

    /**
     * 스트림 파싱용 함수 인터페이스
     * ParseProgressTracker를 통해 진행 상황을 실시간 전달
     */
    @FunctionalInterface
    public interface StreamParsingFunction {
        void parse(File file, String outputPath, ParseProgressTracker tracker) throws Exception;
    }

    /**
     * source/ 하위 모든 파일을 파싱하며 진행 상황을 스트림으로 전달
     * 
     * @param session 세션 UUID
     * @param project 프로젝트명
     * @param parser  스트림 파싱 함수
     * @param callback 스트림 콜백
     */
    public void parseProjectWithStream(String session, String project, 
                                        StreamParsingFunction parser, StreamCallback callback) {
        Path sourceBase = sourceDir(session, project);
        Path analysisBase = analysisDir(session, project);

        if (!Files.exists(sourceBase)) {
            callback.error("소스 디렉토리 없음: " + sourceBase);
            throw new RuntimeException("소스 디렉토리 없음: " + sourceBase);
        }

        try {
            // 파일 목록 수집
            List<Path> files = Files.walk(sourceBase)
                    .filter(Files::isRegularFile)
                    .toList();

            int totalFiles = files.size();
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);
            AtomicInteger totalLines = new AtomicInteger(0);

            callback.message(String.format("🚀 파싱을 시작합니다. (총 %d개 파일)", totalFiles));

            for (int i = 0; i < files.size(); i++) {
                Path file = files.get(i);
                parseFileWithStream(file, sourceBase, analysisBase, parser, callback,
                        i + 1, totalFiles, successCount, errorCount, totalLines);
            }

            // 최종 결과 메시지
            if (errorCount.get() > 0) {
                callback.message(String.format("⚠️ 파싱 완료 (일부 에러). 성공: %d개, 실패: %d개, 총 %,d라인",
                        successCount.get(), errorCount.get(), totalLines.get()));
            } else {
                callback.message(String.format("🎉 파싱 완료! 총 %d개 파일, %,d라인 처리됨",
                        successCount.get(), totalLines.get()));
            }

        } catch (IOException e) {
            callback.error("디렉토리 탐색 실패: " + sourceBase);
            throw new RuntimeException("디렉토리 탐색 실패: " + sourceBase, e);
        }
    }

    private void parseFileWithStream(Path file, Path sourceBase, Path analysisBase,
                                      StreamParsingFunction parser, StreamCallback callback,
                                      int fileIndex, int totalFiles,
                                      AtomicInteger successCount, AtomicInteger errorCount,
                                      AtomicInteger totalLines) {
        Path relative = sourceBase.relativize(file);
        String fileName = relative.toString();

        try {
            // 파일 라인 수 계산
            int lineCount = countLines(file);
            
            // 출력 경로 계산
            String relStr = relative.toString();
            int dot = relStr.lastIndexOf('.');
            String jsonPath = (dot > 0 ? relStr.substring(0, dot) : relStr) + ".json";
            Path output = analysisBase.resolve(jsonPath);
            Files.createDirectories(output.getParent());

            // 진행 상황 추적기 생성
            ParseProgressTracker tracker = new ParseProgressTracker(callback, fileName);
            
            // 파싱 시작 알림
            callback.message(String.format("📄 [%d/%d] %s 파싱 시작... (%,d라인)", 
                    fileIndex, totalFiles, fileName, lineCount));

            // 파싱 실행
            parser.parse(file.toFile(), output.toString(), tracker);

            // 파싱 완료 알림
            callback.message(String.format("✅ [%d/%d] %s 완료 (%,d라인)", 
                    fileIndex, totalFiles, fileName, lineCount));
            
            successCount.incrementAndGet();
            totalLines.addAndGet(lineCount);
            log.info("  [PARSED] {}", relative);

        } catch (Exception e) {
            errorCount.incrementAndGet();
            callback.error(String.format("❌ [%d/%d] %s 파싱 실패: %s", 
                    fileIndex, totalFiles, fileName, e.getMessage()));
            log.error("  [PARSE ERROR] {} - {}", relative, e.getMessage());
        }
    }

    private int countLines(Path file) {
        try {
            return (int) Files.lines(file, StandardCharsets.UTF_8).count();
        } catch (Exception e) {
            try {
                return (int) Files.lines(file, Charset.forName("EUC-KR")).count();
            } catch (Exception e2) {
                return 0; // 라인 수를 알 수 없음
            }
        }
    }
}
