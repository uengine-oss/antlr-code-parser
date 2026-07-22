package legacymodernizer.parser.intake;

import java.io.IOException;
import java.nio.charset.Charset;
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

import legacymodernizer.parser.intake.SourceIntakeClassifier.Kind;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 파일 저장/삭제/경로 관리 + 입구 반입(content 기반 분류) 서비스.
 *
 * 저장 구조 — analysis/ 는 source/ 폴더 구조를 그대로 미러:
 *   data/
 *     ├── source/              → 소스 원본 (프로시저/함수/기타 코드)
 *     ├── ddl/                 → 표 정의(DDL) — 내용으로 분류
 *     └── analysis/            → 파싱 결과 JSON (source 와 동일 구조)
 *
 * 분류는 파일명/경로가 아니라 {@link SourceIntakeClassifier}(내용)로 한다(헌법 III v1.1.0).
 * 업로드/경로 두 모드 모두 결과가 동일한 data/ 로 수렴한다(spec 006 입구 통일).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParserWorkspace {

    private final SourceIntakeClassifier classifier;

    private static final String BASE_DIR = resolveBaseDir();
    private static final String SOURCE = "source";
    private static final String DDL = "ddl";
    private static final String ANALYSIS = "analysis";
    private static final String DIAGNOSTICS = "diagnostics";
    private static final String REPAIRS = "repairs";

    /** 입구 반입 요약 — 분류 수 + 건너뛴 파일(사유 포함). */
    public record IntakeResult(int ddlCount, int sourceCount, List<String> skipped) {}

    private static String resolveBaseDir() {
        String configuredTestRoot = System.getProperty("parser.data.root");
        if (configuredTestRoot != null && !configuredTestRoot.isBlank()) {
            return configuredTestRoot;
        }
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

    public Path diagnosticsDir() {
        return Paths.get(BASE_DIR, DIAGNOSTICS);
    }

    public Path repairsDir() {
        return Paths.get(BASE_DIR, REPAIRS);
    }

    /** analysis/ 비우기 — 파싱이 매 run 자기 출력 디렉토리를 새로 만든다(경로 모드의 stale AST 방지). */
    public void clearAnalysisDir() {
        clearDirectory(analysisDir());
    }

    public void clearRecoveryArtifacts() {
        clearDirectory(diagnosticsDir());
        clearDirectory(repairsDir());
    }

    // ═══════════════════════════════════════════════════════════════════
    // 파일 업로드
    // ═══════════════════════════════════════════════════════════════════

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

            // ddl/ 접두는 역호환 힌트로만 — 진실은 내용(헌법 III). 분류 전 접두 제거.
            String relativePath = originalName.replace("\\", "/");
            if (relativePath.startsWith("ddl/")) relativePath = relativePath.substring(4);

            try {
                byte[] bytes = mf.getBytes();
                String content = decode(bytes);

                if (classifier.classify(relativePath, content) == Kind.DDL) {
                    Path dest = ddlBase.resolve(relativePath);
                    writeBytes(dest, bytes);
                    ddlList.add(Map.of("fileName", relativePath, "fileContent", content));
                    log.debug("  [DDL] {}", relativePath);
                } else {
                    // 원본명에 폴더가 없으면 targetFolder prefix 적용(폴더 picker 상대경로는 유지).
                    String srcRel = relativePath;
                    if (!normalizedTargetFolder.isEmpty() && !srcRel.contains("/")) {
                        srcRel = normalizedTargetFolder + "/" + srcRel;
                    }
                    Path dest = sourceBase.resolve(srcRel);
                    writeBytes(dest, bytes);

                    if (isTargetExtension(srcRel, targetExtensions)) {
                        srcList.add(Map.of("fileName", srcRel, "fileContent", content));
                        log.debug("  [SOURCE] {}", srcRel);
                    } else {
                        nontargetList.add(Map.of("fileName", srcRel, "fileContent", content));
                        log.debug("  [SOURCE:NONTARGET] {}", srcRel);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("파일 저장 실패: " + originalName, e);
            }
        }

        Map<String, Object> uploadSummary = new HashMap<>();
        uploadSummary.put("files", srcList);
        uploadSummary.put("ddlFiles", ddlList);
        uploadSummary.put("nontargetFiles", nontargetList);
        return uploadSummary;
    }

    /**
     * 경로 모드 입구 — 로컬 폴더를 내용 분류해 data/{source, ddl} 로 반입(하드링크→복사 폴백).
     * data/ 는 매 반입마다 전량 리셋(헌법 IV). 한 파일 실패는 격리·기록하고 계속(spec 006 US4).
     */
    public IntakeResult intakeFromPath(Path localRoot) {
        clearDirectory(sourceDir());
        clearDirectory(ddlDir());
        clearDirectory(analysisDir());
        Path sourceBase = sourceDir();
        Path ddlBase = ddlDir();

        List<Path> files;
        try (var walk = Files.walk(localRoot)) {
            files = walk.filter(Files::isRegularFile).sorted().toList();
        } catch (IOException e) {
            throw new RuntimeException("로컬 폴더 탐색 실패: " + localRoot, e);
        }

        int ddlCount = 0, sourceCount = 0;
        List<String> skipped = new ArrayList<>();
        for (Path f : files) {
            String rel = localRoot.relativize(f).toString().replace("\\", "/");
            try {
                Kind kind = classifier.classify(rel, readContent(f));
                Path dest = (kind == Kind.DDL ? ddlBase : sourceBase).resolve(rel);
                materialize(f, dest);
                if (kind == Kind.DDL) ddlCount++; else sourceCount++;
            } catch (Exception e) {
                skipped.add(rel + " (" + e.getMessage() + ")");
                log.warn("[반입 건너뜀] {} - {}", rel, e.getMessage());
            }
        }
        log.info("[경로 반입] ddl={}개, source={}개, 건너뜀={}개", ddlCount, sourceCount, skipped.size());
        return new IntakeResult(ddlCount, sourceCount, skipped);
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

    /** 하드링크 우선(같은 볼륨 0 추가) → 불가(다른 볼륨/미지원/권한) 시 복사 폴백. */
    private void materialize(Path src, Path dest) throws IOException {
        Files.createDirectories(dest.getParent());
        Files.deleteIfExists(dest);
        try {
            Files.createLink(dest, src);
        } catch (IOException | UnsupportedOperationException e) {
            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void writeBytes(Path dest, byte[] bytes) throws IOException {
        Files.createDirectories(dest.getParent());
        Files.write(dest, bytes);
    }

    private String readContent(Path path) throws IOException {
        return decode(Files.readAllBytes(path));
    }

    /** 텍스트 디코드 — UTF-8 → EUC-KR → MS949 폴백. 모두 실패 시 "[binary file]". */
    private String decode(byte[] bytes) {
        for (String cs : new String[] {"UTF-8", "EUC-KR", "MS949"}) {
            try {
                var decoder = Charset.forName(cs).newDecoder();
                decoder.onMalformedInput(java.nio.charset.CodingErrorAction.REPORT);
                decoder.onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT);
                return decoder.decode(java.nio.ByteBuffer.wrap(bytes)).toString();
            } catch (Exception ignored) {
                // 다음 인코딩 시도
            }
        }
        return "[binary file]";
    }
}
