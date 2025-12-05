package legacymodernizer.parser.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileParserService {

    private static final String BASE_DIR = System.getenv("DOCKER_COMPOSE_CONTEXT") != null ?
            System.getenv("DOCKER_COMPOSE_CONTEXT") :
            new File(System.getProperty("user.dir")).getParent() + File.separator + "data";

    private static final String SRC_DIR = "src";
    private static final String DDL_DIR = "ddl";
    private static final String SEQ_DIR = "sequence";
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

    public String getProjectRootDirectory(String sessionUUID, String projectName) throws IOException {
        if (sessionUUID == null || sessionUUID.isBlank()) {
            throw new IOException("세션 UUID가 비어있습니다");
        }
        if (projectName == null || projectName.isBlank()) {
            throw new IOException("프로젝트명이 비어있습니다");
        }
        return BASE_DIR + File.separator + sessionUUID + File.separator + projectName;
    }

    public String getTargetDirectory(String sessionUUID, String projectName, String fileName) throws IOException {
        String subDir = SRC_DIR;
        if (fileName != null) {
            String upperFileName = fileName.toUpperCase();
            if (upperFileName.contains("DDL")) subDir = DDL_DIR;
            if (upperFileName.contains("SEQ")) subDir = SEQ_DIR;
        }
        return getProjectRootDirectory(sessionUUID, projectName) + File.separator + subDir;
    }

    public String getAnalysisDirectory(String sessionUUID, String projectName, String systemName) throws IOException {
        String base = getProjectRootDirectory(sessionUUID, projectName) + File.separator + ANALYSIS_DIR;
        if (systemName == null || systemName.isEmpty()) {
            return base;
        }
        return base + File.separator + systemName;
    }

    public String getBucketForFile(String sessionUUID, String projectName, File file) throws IOException {
        String projectRoot = getProjectRootDirectory(sessionUUID, projectName);
        Path project = new File(projectRoot).toPath().toRealPath();
        Path path = file.toPath().toRealPath();
        Path rel = project.relativize(path);
        if (rel.getNameCount() == 0) return "unknown";
        String first = rel.getName(0).toString();
        if (DDL_DIR.equals(first)) return DDL_DIR;
        if (SEQ_DIR.equals(first)) return SEQ_DIR;
        if (SRC_DIR.equals(first)) return SRC_DIR;
        return "unknown";
    }

    public String detectSystemNameForFile(String sessionUUID, String projectName, File file) throws IOException {
        Path srcPath = new File(getTargetDirectory(sessionUUID, projectName, null)).toPath().toRealPath();
        Path filePath = file.toPath().toRealPath();
        if (filePath.startsWith(srcPath)) {
            Path rel = srcPath.relativize(filePath);
            if (rel.getNameCount() >= 2) {
                return rel.getName(0).toString();
            }
        }
        return null;
    }

    // ========================================
    // 파일 I/O
    // ========================================

    public String readFileContent(File file) throws IOException {
        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            log.debug("[readFile] 인코딩 선택 - UTF-8, file={}", file.getName());
            return content;
        } catch (MalformedInputException e) {
            try {
                String content = Files.readString(file.toPath(), Charset.forName("EUC-KR"));
                log.debug("[readFile] 인코딩 선택 - EUC-KR, file={}", file.getName());
                return content;
            } catch (Exception e2) {
                String content = Files.readString(file.toPath(), Charset.forName("MS949"));
                log.debug("[readFile] 인코딩 선택 - MS949, file={}", file.getName());
                return content;
            }
        }
    }

    public String saveToBucketFromStream(String sessionUUID,
                                         String projectName,
                                         String bucket,
                                         String systemName,
                                         String fileName,
                                         InputStream inputStream) throws IOException {
        String baseFileName = toBaseName(fileName);
        String projectRoot = getProjectRootDirectory(sessionUUID, projectName);
        File targetDir = new File(projectRoot, bucket);
        if (SRC_DIR.equals(bucket) && systemName != null && !systemName.isBlank()) {
            targetDir = new File(targetDir, systemName);
        }
        createDirectoryIfNotExists(targetDir.getAbsolutePath());
        File out = new File(targetDir, baseFileName);
        Files.copy(inputStream, out.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return out.getAbsolutePath();
    }

    private void createDirectoryIfNotExists(String path) throws IOException {
        Path dirPath = Paths.get(path);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
            log.debug("      → 디렉터리 생성: {}", dirPath.getFileName());
        }
    }

    // ========================================
    // 파싱 및 분석
    // ========================================

    public void parseAndSaveStructureWithStrategy(File candidate,
                                                  String displayFileName,
                                                  String sessionUUID,
                                                  String projectName,
                                                  String systemName,
                                                  ParsingFunction parsingFunction) throws IOException {
        String analysisDir = getAnalysisDirectory(sessionUUID, projectName, systemName);
        String baseFileName = toBaseNameWithoutExt(displayFileName != null ? displayFileName : candidate.getName());
        String outputPath = analysisDir + File.separator + baseFileName + ".json";

        createDirectoryIfNotExists(analysisDir);

        try {
            parsingFunction.parse(candidate, outputPath);
        } catch (Exception e) {
            throw new IOException("파싱 실패: " + e.getMessage(), e);
        }
    }

    @FunctionalInterface
    public interface ParsingFunction {
        void parse(File file, String outputPath) throws Exception;
    }

    public boolean analysisExists(String sessionUUID, String projectName, String systemName, String fileName) throws IOException {
        String analysisDir = getAnalysisDirectory(sessionUUID, projectName, systemName);
        String base = toBaseName(fileName);
        String baseNoExt = base.contains(".") ? base.substring(0, base.lastIndexOf('.')) : base;
        File analysisFile = new File(analysisDir, baseNoExt + ".json");
        return analysisFile.exists();
    }

    private boolean analyzeSpIfNeededWithStrategy(String sessionUUID, String projectName, String systemNameHint, File located, ParsingFunction parsingFunction) throws IOException {
        String bucket = getBucketForFile(sessionUUID, projectName, located);
        if (!SRC_DIR.equals(bucket)) {
            return false;
        }

        String systemName = systemNameHint != null ? systemNameHint : detectSystemNameForFile(sessionUUID, projectName, located);
        boolean already = analysisExists(sessionUUID, projectName, systemName, located.getName());
        if (!already) {
            parseAndSaveStructureWithStrategy(located, located.getName(), sessionUUID, projectName, systemName, parsingFunction);
            return true;
        }
        return false;
    }

    // ========================================
    // 업로드 처리
    // ========================================

    public Map<String, Object> processUploadByMetadata(String sessionUUID,
                                                       String projectName,
                                                       Object systemsObj,
                                                       Object ddlObj,
                                                       Object seqObj,
                                                       Map<String, MultipartFile> nameToFile) {
        List<Map<String, String>> successFiles = new ArrayList<>();

        Map<String, File> fileIndex = buildProjectFileIndex(sessionUUID, projectName);

        if (systemsObj instanceof List<?>) {
            for (Object sys : (List<?>) systemsObj) {
                if (!(sys instanceof Map<?, ?>)) continue;
                Map<?, ?> sysMap = (Map<?, ?>) sys;
                String systemName = (String) sysMap.get("name");
                Object spObj = sysMap.get("sp");
                if (systemName == null || systemName.isBlank() || !(spObj instanceof List<?>)) continue;
                for (Object sp : (List<?>) spObj) {
                    if (!(sp instanceof String)) continue;
                    String fileName = (String) sp;
                    try {
                        File found = fileIndex.getOrDefault(toBaseName(fileName).toLowerCase(), null);
                        if (found != null) {
                            Map<String, String> ok = new HashMap<>();
                            ok.put("system", systemName);
                            ok.put("fileName", found.getName());
                            ok.put("filePath", found.getAbsolutePath());
                            ok.put("fileContent", readFileContent(found));
                            ok.put("analysisExists", analysisExists(sessionUUID, projectName, systemName, found.getName()) ? "true" : "false");
                            successFiles.add(ok);
                            continue;
                        }

                        MultipartFile mf = nameToFile != null ? nameToFile.getOrDefault(fileName.toLowerCase(), null) : null;
                        if (mf == null || mf.isEmpty()) {
                            throw new RuntimeException("파일이 존재하지 않으며 업로드 파일도 없습니다: system=" + systemName + ", file=" + fileName);
                        }

                        String savedPath = saveToBucketFromStream(sessionUUID, projectName, SRC_DIR, systemName, fileName, mf.getInputStream());
                        Map<String, String> ok = new HashMap<>();
                        ok.put("system", systemName);
                        ok.put("fileName", fileName);
                        ok.put("filePath", savedPath);
                        ok.put("fileContent", readFileContent(new File(savedPath)));
                        ok.put("analysisExists", analysisExists(sessionUUID, projectName, systemName, fileName) ? "true" : "false");
                        successFiles.add(ok);
                    } catch (Exception e) {
                        throw new RuntimeException("업로드 처리 실패: system=" + systemName + ", file=" + fileName + " - " + e.getMessage(), e);
                    }
                }
            }
        }

        saveBucketListFromMap(sessionUUID, projectName, DDL_DIR, ddlObj, nameToFile);
        saveBucketListFromMap(sessionUUID, projectName, SEQ_DIR, seqObj, nameToFile);

        return Map.of("successFiles", successFiles);
    }

    public Map<String, Object> processParsingBySystemsWithStrategy(String sessionUUID,
                                                                   String projectName,
                                                                   List<?> systems,
                                                                   ParsingFunction parsingFunction) {
        List<Map<String, String>> successFiles = new ArrayList<>();

        Map<String, File> fileIndex = buildProjectFileIndex(sessionUUID, projectName);

        if (systems == null) return Map.of("successFiles", successFiles);

        for (Object sys : systems) {
            if (!(sys instanceof Map<?, ?>)) continue;
            Map<?, ?> sysMap = (Map<?, ?>) sys;
            String systemName = (String) sysMap.get("name");
            Object spObj = sysMap.get("sp");
            if (systemName == null || !(spObj instanceof List<?>)) continue;

            List<?> spArr = (List<?>) spObj;
            for (Object sp : spArr) {
                if (!(sp instanceof String)) continue;
                String fileName = (String) sp;
                long start = System.currentTimeMillis();
                File located;
                try {
                    located = locateFileByName(sessionUUID, projectName, fileName, fileIndex);
                } catch (IOException io) {
                    throw new RuntimeException("파일 검색 실패: system=" + systemName + ", file=" + fileName + " - " + io.getMessage(), io);
                }
                if (located != null) {
                    try {
                        analyzeSpIfNeededWithStrategy(sessionUUID, projectName, systemName, located, parsingFunction);
                        Map<String, String> info = getFileInfoForFile(sessionUUID, projectName, located);
                        Map<String, String> ok = new HashMap<>();
                        ok.put("system", systemName);
                        ok.put("fileName", info.getOrDefault("fileName", fileName));
                        ok.put("fileContent", info.getOrDefault("fileContent", ""));
                        ok.put("analysisExists", info.getOrDefault("analysisExists", "false"));
                        successFiles.add(ok);
                    } catch (Exception e) {
                        throw new RuntimeException("파싱 실패: system=" + systemName + ", file=" + fileName + " - " + e.getMessage(), e);
                    }
                } else {
                    throw new RuntimeException("파일을 찾을 수 없습니다: " + fileName);
                }
                long elapsed = System.currentTimeMillis() - start;
                log.info("  {} ({}ms)", fileName, elapsed);
            }
        }

        return Map.of("successFiles", successFiles);
    }

    private void saveBucketListFromMap(String sessionUUID,
                                       String projectName,
                                       String bucket,
                                       Object listObj,
                                       Map<String, MultipartFile> nameToFile) {
        if (!(listObj instanceof List<?>) || nameToFile == null || nameToFile.isEmpty()) return;
        for (Object o : (List<?>) listObj) {
            if (!(o instanceof String)) continue;
            String fileName = (String) o;
            MultipartFile mf = nameToFile.getOrDefault(fileName.toLowerCase(), null);
            if (mf == null || mf.isEmpty()) continue;
            try {
                saveToBucketFromStream(sessionUUID, projectName, bucket, null, fileName, mf.getInputStream());
            } catch (Exception e) {
                throw new RuntimeException("버킷 저장 중 오류: " + bucket + "/" + fileName + " - " + e.getMessage(), e);
            }
        }
    }

    // ========================================
    // 파일 검색 및 인덱싱
    // ========================================

    private Map<String, File> buildProjectFileIndex(String sessionUUID, String projectName) {
        Map<String, File> index = new HashMap<>();
        try {
            String srcRoot = getTargetDirectory(sessionUUID, projectName, null);
            File srcRootDir = new File(srcRoot);
            if (srcRootDir.exists()) {
                try (java.util.stream.Stream<Path> stream = Files.walk(srcRootDir.toPath())) {
                    stream.filter(Files::isRegularFile)
                          .map(Path::toFile)
                          .forEach(f -> index.putIfAbsent(f.getName().toLowerCase(), f));
                }
            }
            String projectRoot = getProjectRootDirectory(sessionUUID, projectName);
            File ddlDir = new File(projectRoot, DDL_DIR);
            if (ddlDir.exists()) {
                File[] list = ddlDir.listFiles();
                if (list != null) {
                    for (File f : list) {
                        if (f.isFile()) index.putIfAbsent(f.getName().toLowerCase(), f);
                    }
                }
            }
            File seqDir = new File(projectRoot, SEQ_DIR);
            if (seqDir.exists()) {
                File[] list = seqDir.listFiles();
                if (list != null) {
                    for (File f : list) {
                        if (f.isFile()) index.putIfAbsent(f.getName().toLowerCase(), f);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("프로젝트 파일 인덱스 생성 실패: " + e.getMessage(), e);
        }
        return index;
    }

    private File locateFileByName(String sessionUUID, String projectName, String fileName, Map<String, File> fileIndex) throws IOException {
        String key = toBaseName(fileName);
        if (key != null) {
            File byIndex = fileIndex.getOrDefault(key.toLowerCase(), null);
            if (byIndex != null) return byIndex;
        }
        return findExistingSqlFile(sessionUUID, projectName, fileName);
    }

    private File findExistingSqlFile(String sessionUUID, String projectName, String fileName) throws IOException {
        String baseFileName = toBaseName(fileName);
        String srcRoot = getTargetDirectory(sessionUUID, projectName, null);
        File srcRootDir = new File(srcRoot);
        File candidate = findFileRecursivelyByNameIgnoreCase(srcRootDir, baseFileName);
        if (candidate != null) return candidate;

        String projectRoot = getProjectRootDirectory(sessionUUID, projectName);
        File inDdl = new File(projectRoot + File.separator + DDL_DIR, baseFileName);
        if (inDdl.exists() && inDdl.isFile()) return inDdl;
        File inSeq = new File(projectRoot + File.separator + SEQ_DIR, baseFileName);
        if (inSeq.exists() && inSeq.isFile()) return inSeq;
        return null;
    }

    private File findFileRecursivelyByNameIgnoreCase(File root, String fileName) {
        if (root == null || !root.exists() || fileName == null) return null;
        try {
            try (java.util.stream.Stream<Path> stream = Files.walk(root.toPath())) {
                return stream
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .filter(f -> f.getName().equalsIgnoreCase(fileName))
                    .findFirst()
                    .orElse(null);
            }
        } catch (IOException e) {
            throw new RuntimeException("디렉터리 검색 실패: " + root.getAbsolutePath() + " - " + e.getMessage(), e);
        }
    }

    public Map<String, String> getFileInfoByName(String sessionUUID, String projectName, String fileName) throws IOException {
        File file = findExistingSqlFile(sessionUUID, projectName, fileName);
        if (file == null) throw new IOException("파일을 찾을 수 없습니다: " + fileName);
        return getFileInfoForFile(sessionUUID, projectName, file);
    }

    private Map<String, String> getFileInfoForFile(String sessionUUID, String projectName, File file) throws IOException {
        String fileContent = readFileContent(file);
        String systemName = detectSystemNameForFile(sessionUUID, projectName, file);
        boolean exists = analysisExists(sessionUUID, projectName, systemName, file.getName());
        return makeFileInfo(file.getName(), fileContent, exists);
    }

    private Map<String, String> makeFileInfo(String fileName, String fileContent, boolean analysisExists) {
        Map<String, String> map = new HashMap<>();
        map.put("fileName", fileName != null ? fileName : "");
        map.put("fileContent", fileContent != null ? fileContent : "");
        map.put("analysisExists", String.valueOf(analysisExists));
        return map;
    }
}

