package legacymodernizer.parser.service;

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
import java.util.Set;
import java.io.File;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

/**
 * 파일 저장/삭제/경로 관리 서비스
 *
 * 저장 구조 — analysis/ 는 source/ 폴더 구조를 그대로 미러:
 *   data/
 *     ├── source/              → 소스 파일 (업로드 시)
 *     ├── ddl/                 → DDL 파일 (원본 폴더 구조 유지)
 *     └── analysis/            → 파싱 결과 JSON (source 와 동일 구조)
 */
@Slf4j
@Service
public class FileStorageService {

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

    public Path sourceDir() {
        return Paths.get(BASE_DIR, SOURCE);
    }

    public Path ddlDir() {
        return Paths.get(BASE_DIR, DDL);
    }

    public Path analysisDir() {
        return Paths.get(BASE_DIR, ANALYSIS);
    }

    // ═══════════════════════════════════════════════════════════════════
    // 파일 업로드
    // ═══════════════════════════════════════════════════════════════════

    /**
     * 파일 업로드 (기존 폴더 비우고 새로 저장). targetFolder 없이 호출 시 기존 동작 유지.
     */
    public Map<String, Object> uploadFiles(MultipartFile[] files, Set<String> targetExtensions) {
        return uploadFiles(files, targetExtensions, null);
    }

    /**
     * 파일 업로드 (기존 폴더 비우고 새로 저장)
     *
     * - DDL 파일 (ddl/ 접두) → ddl/ 저장
     * - 그 외 모든 파일 → source/ 저장 (타겟/비타겟 구분 없이)
     * - targetFolder 지정 시 원본명에 폴더가 없는 단일 파일에만 prefix 적용
     *   (폴더 picker 로 올린 파일은 상대경로 그대로 유지)
     *
     * @param files 업로드 파일들 (filename: 상대경로)
     * @param targetExtensions 타겟 언어 확장자 목록 (예: {".java"})
     * @param targetFolder source/ 아래에 강제 배치할 폴더 prefix (null/빈값이면 미적용)
     * @return {files: [...], ddlFiles: [...], nontargetFiles: [...]}
     */
    public Map<String, Object> uploadFiles(MultipartFile[] files, Set<String> targetExtensions, String targetFolder) {
        clearDirectory(sourceDir());
        clearDirectory(ddlDir());
        clearDirectory(analysisDir());

        List<Map<String, String>> srcList = new ArrayList<>();
        List<Map<String, String>> ddlList = new ArrayList<>();
        List<Map<String, String>> nontargetList = new ArrayList<>();

        Path sourceBase = sourceDir();
        Path ddlBase = ddlDir();

        String normalizedTargetFolder = normalizeFolderPrefix(targetFolder);

        for (MultipartFile mf : files) {
            if (mf == null || mf.isEmpty()) continue;

            String originalName = mf.getOriginalFilename();
            if (originalName == null || originalName.isBlank()) continue;

            String relativePath = originalName.replace("\\", "/");
            boolean isDdl = relativePath.startsWith("ddl/");

            // 원본명에 폴더가 없으면 targetFolder prefix 적용 (DDL 은 제외: 기존 규칙 유지)
            if (!isDdl && !normalizedTargetFolder.isEmpty() && !relativePath.contains("/")) {
                relativePath = normalizedTargetFolder + "/" + relativePath;
            }

            try {
                if (isDdl) {
                    String ddlPath = relativePath.substring(4);
                    Path dest = ddlBase.resolve(ddlPath);
                    saveFile(mf, dest);

                    ddlList.add(Map.of(
                            "fileName", relativePath,
                            "fileContent", readContent(dest)));
                    log.debug("  [DDL] {}", relativePath);
                } else {
                    Path dest = sourceBase.resolve(relativePath);
                    saveFile(mf, dest);

                    if (isTargetExtension(relativePath, targetExtensions)) {
                        srcList.add(Map.of(
                                "fileName", relativePath,
                                "fileContent", readContent(dest)));
                        log.debug("  [SOURCE] {}", relativePath);
                    } else {
                        nontargetList.add(Map.of(
                                "fileName", relativePath,
                                "fileContent", readContent(dest)));
                        log.debug("  [SOURCE:NONTARGET] {}", relativePath);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("파일 저장 실패: " + originalName, e);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("files", srcList);
        result.put("ddlFiles", ddlList);
        result.put("nontargetFiles", nontargetList);
        return result;
    }

    // ═══════════════════════════════════════════════════════════════════
    // 내부 유틸
    // ═══════════════════════════════════════════════════════════════════

    private String normalizeFolderPrefix(String raw) {
        if (raw == null) return "";
        String normalized = raw.replace("\\", "/").trim();
        while (normalized.startsWith("/")) normalized = normalized.substring(1);
        while (normalized.endsWith("/")) normalized = normalized.substring(0, normalized.length() - 1);
        return normalized;
    }

    private boolean isTargetExtension(String filePath, Set<String> targetExtensions) {
        int dot = filePath.lastIndexOf('.');
        if (dot < 0) return false;
        String ext = filePath.substring(dot).toLowerCase();
        return targetExtensions.contains(ext);
    }

    private void clearDirectory(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try {
            Files.walk(dir)
                    .sorted((a, b) -> b.compareTo(a))
                    .filter(p -> !p.equals(dir))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            log.warn("파일 삭제 실패: {}", p, e);
                        }
                    });
            log.debug("디렉토리 초기화: {}", dir);
        } catch (IOException e) {
            log.warn("디렉토리 탐색 실패: {}", dir, e);
        }
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
                try {
                    return Files.readString(path, Charset.forName("MS949"));
                } catch (Exception e3) {
                    log.warn("텍스트로 읽을 수 없는 파일 (바이너리): {}", path);
                    return "[binary file]";
                }
            }
        }
    }
}
