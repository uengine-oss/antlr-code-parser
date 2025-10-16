package legacymodernizer.parser.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.stereotype.Service;

import legacymodernizer.parser.antlr.CaseChangingCharStream;
import legacymodernizer.parser.antlr.CustomPlSqlListener;
import legacymodernizer.parser.antlr.plsql.PlSqlLexer;
import legacymodernizer.parser.antlr.plsql.PlSqlParser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PlSqlFileParserService {

    // 기본 경로 상수
    private static final String BASE_DIR = System.getenv("DOCKER_COMPOSE_CONTEXT") != null ?
            System.getenv("DOCKER_COMPOSE_CONTEXT") :
            new File(System.getProperty("user.dir")).getParent() + File.separator + "data";


    // 각 디렉터리 경로 상수
    private static final String PLSQL_DIR = "src";
    private static final String DDL_DIR = "ddl";
    private static final String SEQ_DIR = "sequence";
    private static final String ANALYSIS_DIR = "analysis";

    // SQL 객체명 추출 정규식
    private static final Pattern SQL_OBJECT_PATTERN = Pattern.compile(
        "(?is)\\bCREATE\\s+(?:OR\\s+REPLACE\\s+)?(?:EDITIONABLE\\s+|NONEDITIONABLE\\s+)?"
      + "(?:PACKAGE\\s+BODY|PACKAGE|PROCEDURE|FUNCTION|TRIGGER)\\s+"
      + "(?<full>(?:\"[^\"]+\"|[\\w$]+)(?:\\s*\\.\\s*(?:\"[^\"]+\"|[\\w$]+))?)"
    );


    /**
     * 전달된 문자열에서 경로를 제거한 베이스 파일명을 반환합니다.
     * @param name 원본 파일명 또는 경로를 포함한 문자열 (null 허용)
     * @return 경로를 제외한 파일명; name이 null이면 null
     */
    private static String toBaseName(String name) {
        if (name == null) return null;
        return Paths.get(name).getFileName().toString();
    }

    /**
     * 프로젝트 루트 디렉터리 절대경로를 반환합니다.
     * @param sessionUUID 세션 UUID (필수, 공백 불가)
     * @param projectName 프로젝트명 (필수, 공백 불가)
     * @return data/<session>/<project> 디렉터리의 절대경로 문자열
     * @throws IOException 입력값이 비어있을 때
     */
    public String getProjectRootDirectory(String sessionUUID, String projectName) throws IOException {
        if (sessionUUID == null || sessionUUID.isBlank()) {
            throw new IOException("세션 UUID가 비어있습니다");
        }
        if (projectName == null || projectName.isBlank()) {
            throw new IOException("프로젝트명이 비어있습니다");
        }
        return BASE_DIR + File.separator + sessionUUID + File.separator + projectName;
    }

    /**
     * 파일명 힌트로 서브디렉터리(src/ddl/sequence)를 결정해 절대경로를 반환합니다.
     * @param sessionUUID 세션 UUID
     * @param projectName 프로젝트명
     * @param fileName 파일명 힌트(DDL/SEQ 포함 시 해당 폴더 선택). null 가능
     * @return 대상 디렉터리 절대경로
     * @throws IOException 프로젝트 루트 계산 실패 시
     */
    public String getTargetDirectory(String sessionUUID, String projectName, String fileName) throws IOException {
        String subDir = PLSQL_DIR; // 기본값
        if (fileName != null) {
            String upperFileName = fileName.toUpperCase();
            if (upperFileName.contains("DDL")) subDir = DDL_DIR;
            if (upperFileName.contains("SEQ")) subDir = SEQ_DIR;
        }
        return getProjectRootDirectory(sessionUUID, projectName) + File.separator + subDir;
    }


    /**
     * 프로젝트 공통 분석 디렉터리 절대경로를 반환합니다.
     * @param sessionUUID 세션 UUID
     * @param projectName 프로젝트명
     * @return data/<session>/<project>/analysis 절대경로
     * @throws IOException 프로젝트 루트 계산 실패 시
     */
    public String getAnalysisDirectory(String sessionUUID, String projectName) throws IOException {
        return getProjectRootDirectory(sessionUUID, projectName) + File.separator + ANALYSIS_DIR;
    }

    /**
     * 시스템별 분석 디렉터리 절대경로를 반환합니다.
     * @param sessionUUID 세션 UUID
     * @param projectName 프로젝트명
     * @param systemName 시스템명 (null/빈값이면 공통 분석 디렉터리 반환)
     * @return data/<session>/<project>/analysis[/<system>] 절대경로
     * @throws IOException 프로젝트 루트 계산 실패 시
     */
    public String getAnalysisDirectory(String sessionUUID, String projectName, String systemName) throws IOException {
        if (systemName == null || systemName.isEmpty()) {
            return getAnalysisDirectory(sessionUUID, projectName);
        }
        return getAnalysisDirectory(sessionUUID, projectName) + File.separator + systemName;
    }

    /**
     * 파일명으로부터 타입을 판별합니다.
     * @param fileName 파일명 또는 경로
     * @return PLSQL | DDL | SEQ
     */
    private String getFileType(String fileName) {
        if (fileName == null) return "PLSQL";
        String upperFileName = toBaseName(fileName).toUpperCase();
        if (upperFileName.contains("DDL")) return "DDL";
        // SEQ, SEQUENCE 둘 다 인식
        if (upperFileName.contains("SEQUENCE") || upperFileName.contains("SEQ")) return "SEQ";
        return "PLSQL";
    }

    /**
     * 파일 정보 공통 맵을 생성합니다.
     * @param fileName 파일명
     * @param fileContent 파일 내용
     * @param objectName SQL 객체명
     * @param fileType 파일 타입
     * @return 파일 정보를 담은 맵
     */
    private Map<String, String> createFileInfoMap(String fileName, String fileContent, String objectName, String fileType) {
        Map<String, String> result = new HashMap<>();
        result.put("fileName", fileName != null ? fileName : "");
        result.put("fileContent", fileContent != null ? fileContent : "");
        result.put("objectName", objectName != null ? objectName : "");
        result.put("fileType", fileType != null ? fileType : "UNKNOWN");
        return result;
    }

    /**
     * 분석 파일 존재 여부를 포함한 파일 정보 맵을 생성합니다.
     * @param fileName 파일명
     * @param fileContent 파일 내용
     * @param objectName SQL 객체명
     * @param fileType 파일 타입
     * @param analysisExists 분석 결과 파일 존재 여부
     * @return 파일 정보를 담은 맵
     */
    private Map<String, String> createFileInfoMapWithAnalysis(String fileName, String fileContent, String objectName, String fileType, boolean analysisExists) {
        Map<String, String> map = createFileInfoMap(fileName, fileContent, objectName, fileType);
        map.put("analysisExists", String.valueOf(analysisExists));
        return map;
    }


    /**
     * 상위 디렉터리(ddl 또는 sequence)에 파일의 존재를 보장합니다. 다른 위치에서 발견되면 이동합니다.
     * @param sessionUUID 세션 UUID
     * @param projectName 프로젝트명
     * @param topLevel 상위 디렉터리 이름 (ddl|sequence)
     * @param fileName 대상 파일명(경로 허용)
     * @throws IOException 파일 미존재 또는 부적절한 디렉터리 지정 등 실패 시
     */
    public void ensureFileInTopLevelDir(String sessionUUID, String projectName, String topLevel, String fileName) throws IOException {
        if (!DDL_DIR.equals(topLevel) && !SEQ_DIR.equals(topLevel)) throw new IOException("허용되지 않은 상위 디렉터리: " + topLevel);
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IOException("파일명이 비어있습니다");
        }
        String baseFileName = toBaseName(fileName);

        String targetRoot = getProjectRootDirectory(sessionUUID, projectName) + File.separator + topLevel;
        createDirectoryIfNotExists(targetRoot);

        // 검색 우선순위: 해당 타입 디렉터리 -> src 전체 -> 다른 타입 디렉터리
        File targetDir = new File(targetRoot);
        File found = null;
        // 1) 이미 그 위치에 있으면 ok
        File direct = new File(targetDir, baseFileName);
        if (direct.exists()) found = direct;
        // 2) src 하위 전체에서 검색
        if (found == null) {
            String srcRoot = getTargetDirectory(sessionUUID, projectName, null);
            found = findFileRecursivelyByNameIgnoreCase(new File(srcRoot), baseFileName);
        }
        // 3) 다른 타입 디렉토리에서 검색
        if (found == null) {
            String other = DDL_DIR.equals(topLevel) ? SEQ_DIR : DDL_DIR;
            String otherRoot = getProjectRootDirectory(sessionUUID, projectName) + File.separator + other;
            found = findFileRecursivelyByNameIgnoreCase(new File(otherRoot), baseFileName);
        }

        if (found == null) throw new IOException("파일을 찾지 못했습니다: " + fileName);

        File desired = new File(targetDir, found.getName());
        if (!found.getAbsolutePath().equals(desired.getAbsolutePath())) {
            Files.move(found.toPath(), desired.toPath(), StandardCopyOption.REPLACE_EXISTING);
            log.debug("      → 파일 이동: {} → {}", found.getName(), topLevel);
        }
    }



    /**
     * 파일의 내용을 다양한 인코딩으로 시도하여 읽어옴
     * @param file 읽을 파일 객체
     * @return 파일의 내용 문자열
     * @throws IOException 파일 읽기 실패시 발생
     */
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

    /**
     * PL/SQL 파일을 파싱하여 JSON 구조로 저장합니다. 시스템 힌트가 있으면 해당 시스템 분석 디렉터리에 저장합니다.
     * @param fileName 분석 대상 파일명
     * @param sessionUUID 세션 UUID
     * @param projectName 프로젝트명
     * @param systemNameHint 시스템명 힌트 (null 가능)
     * @throws IOException 파일 탐색/저장/파싱 실패 시
     */
    public void parseAndSaveStructure(String fileName, String sessionUUID, String projectName, String systemNameHint) throws IOException {

        File candidate = findExistingSqlFile(sessionUUID, projectName, fileName);
        if (candidate == null) {
            throw new IOException("분석 대상 파일을 찾을 수 없음: " + fileName);
        }

        String systemName = systemNameHint;
        if (systemName == null) {
            systemName = detectSystemNameForFile(sessionUUID, projectName, candidate);
        }

        String analysisDir = systemName != null ? getAnalysisDirectory(sessionUUID, projectName, systemName)
                                                : getAnalysisDirectory(sessionUUID, projectName);
        String baseFileName = fileName.substring(0, fileName.lastIndexOf('.'));
        String outputPath = analysisDir + File.separator + baseFileName + ".json";

        createDirectoryIfNotExists(analysisDir);

        log.debug("      [ANTLR 파싱 시작]");
        try (InputStream in = new FileInputStream(candidate)) {
            CharStream s = CharStreams.fromStream(in);
            CaseChangingCharStream upper = new CaseChangingCharStream(s, true);
            PlSqlLexer lexer = new PlSqlLexer(upper);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            PlSqlParser parser = new PlSqlParser(tokens);
            ParserRuleContext tree = parser.sql_script();
            CustomPlSqlListener listener = new CustomPlSqlListener(tokens);
            new ParseTreeWalker().walk(listener, tree);
            File analysisFile = new File(outputPath);
            try (FileWriter file = new FileWriter(analysisFile)) {
                file.write(listener.getRoot().toJson());
            }
            log.debug("      → 분석 결과 저장: {}", analysisFile.getName());
        }
    }

    /**
     * 디렉터리가 없으면 생성합니다.
     * @param path 생성할 디렉터리 경로(절대/상대)
     * @throws IOException 생성 실패 시
     */
    private void createDirectoryIfNotExists(String path) throws IOException {
        Path dirPath = Paths.get(path);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
            log.debug("      → 디렉터리 생성: {}", dirPath.getFileName());
        }
    }

    /**
     * 지정한 버킷(src/ddl/sequence) 및 선택적 시스템 하위에 파일 바이트를 저장합니다.
     * @param sessionUUID 세션 UUID
     * @param projectName 프로젝트명
     * @param bucket 버킷 이름 (src|ddl|sequence)
     * @param systemName 시스템명 (src일 때만 사용, null 가능)
     * @param fileName 원본 파일명(경로 허용)
     * @param bytes 저장할 파일 바이트
     * @return 저장된 파일의 절대경로
     * @throws IOException 경로 생성/쓰기 실패 시
     */
    public String saveBytesToBucket(String sessionUUID,
                                    String projectName,
                                    String bucket,
                                    String systemName,
                                    String fileName,
                                    byte[] bytes) throws IOException {
        String baseFileName = toBaseName(fileName);
        String projectRoot = getProjectRootDirectory(sessionUUID, projectName);
        File targetDir = new File(projectRoot, bucket);
        if (PLSQL_DIR.equals(bucket) && systemName != null && !systemName.isBlank()) {
            targetDir = new File(targetDir, systemName);
        }
        createDirectoryIfNotExists(targetDir.getAbsolutePath());
        File out = new File(targetDir, baseFileName);
        Files.write(out.toPath(), bytes);
        return out.getAbsolutePath();
    }

    /**
     * 하위 디렉터리를 재귀적으로 순회하여 대소문자 무시 파일명으로 파일을 찾습니다.
     * @param root 시작 루트 디렉터리
     * @param fileName 찾을 파일명
     * @return 발견된 파일 객체, 없으면 null
     */
    private File findFileRecursivelyByNameIgnoreCase(File root, String fileName) {
        if (root == null || !root.exists()) return null;
        if (fileName == null) return null;
        File[] list = root.listFiles();
        if (list == null) return null;
        for (File f : list) {
            if (f.isDirectory()) {
                File found = findFileRecursivelyByNameIgnoreCase(f, fileName);
                if (found != null) return found;
            } else if (f.getName().equalsIgnoreCase(fileName)) {
                return f;
            }
        }
        return null;
    }

    /**
     * 프로젝트 내에서 실제 존재하는 SQL 파일을 src 재귀 → ddl → sequence 순서로 탐색합니다.
     * @param sessionUUID 세션 UUID
     * @param projectName 프로젝트명
     * @param fileName 파일명(경로 허용)
     * @return 발견된 파일 객체, 없으면 null
     * @throws IOException 경로 계산 실패 시
     */
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

    /**
     * 파일명이 가리키는 실제 파일 객체를 반환합니다.
     * @param sessionUUID 세션 UUID
     * @param projectName 프로젝트명
     * @param fileName 파일명(경로 허용)
     * @return 존재하는 파일 객체, 없으면 null
     * @throws IOException 경로 계산 실패 시
     */
    public File resolveExistingSqlFile(String sessionUUID, String projectName, String fileName) throws IOException {
        String base = toBaseName(fileName);
        return findExistingSqlFile(sessionUUID, projectName, base);
    }

    /**
     * 파일의 프로젝트 상대 경로를 기준으로 버킷(src|ddl|sequence)을 판별합니다.
     * @param sessionUUID 세션 UUID
     * @param projectName 프로젝트명
     * @param file 판별 대상 파일
     * @return 버킷 이름 또는 unknown
     * @throws IOException 경로 해석 실패 시
     */
    public String getBucketForFile(String sessionUUID, String projectName, File file) throws IOException {
        String projectRoot = getProjectRootDirectory(sessionUUID, projectName);
        Path project = new File(projectRoot).toPath().toRealPath();
        Path path = file.toPath().toRealPath();
        Path rel = project.relativize(path);
        if (rel.getNameCount() == 0) return "unknown";
        String first = rel.getName(0).toString();
        if (DDL_DIR.equals(first)) return DDL_DIR;
        if (SEQ_DIR.equals(first)) return SEQ_DIR;
        if (PLSQL_DIR.equals(first)) return PLSQL_DIR;
        return "unknown";
    }

    /**
     * 파일 경로가 src/<system>/... 구조일 때 시스템명을 추출합니다.
     * @param sessionUUID 세션 UUID
     * @param projectName 프로젝트명
     * @param file 대상 파일
     * @return 시스템명 또는 null
     * @throws IOException 경로 해석 실패 시
     */
    public String detectSystemNameForFile(String sessionUUID, String projectName, File file) throws IOException {
        try {
            Path srcPath = new File(getTargetDirectory(sessionUUID, projectName, null)).toPath().toRealPath();
            Path filePath = file.toPath().toRealPath();
            if (filePath.startsWith(srcPath)) {
                Path rel = srcPath.relativize(filePath);
                if (rel.getNameCount() >= 2) {
                    return rel.getName(0).toString();
                }
            }
        } catch (Exception ignore) {}
        return null;
    }

    /**
     * 분석 결과 JSON 파일 존재 여부를 확인합니다.
     * @param sessionUUID 세션 UUID
     * @param projectName 프로젝트명
     * @param systemName 시스템명 (null 가능)
     * @param fileName 기준 파일명(확장자 기준으로 매칭)
     * @return 존재하면 true, 없으면 false
     * @throws IOException 경로 계산 실패 시
     */
    public boolean analysisExists(String sessionUUID, String projectName, String systemName, String fileName) throws IOException {
        String analysisDir = systemName != null ? getAnalysisDirectory(sessionUUID, projectName, systemName)
                                                : getAnalysisDirectory(sessionUUID, projectName);
        String base = toBaseName(fileName);
        String baseNoExt = base.contains(".") ? base.substring(0, base.lastIndexOf('.')) : base;
        File analysisFile = new File(analysisDir, baseNoExt + ".json");
        return analysisFile.exists();
    }

    /**
     * src 버킷의 파일인 경우에만 분석을 수행하고, 이미 분석된 경우는 스킵합니다.
     * @param sessionUUID 세션 UUID
     * @param projectName 프로젝트명
     * @param systemNameHint 시스템명 힌트 (null 가능)
     * @param fileName 파일명(경로 허용)
     * @return 실제 분석을 수행했으면 true, 이미 존재하거나 대상 외면 false
     * @throws IOException 파일 탐색/판별 실패 시
     */
    public boolean analyzeSpIfNeeded(String sessionUUID, String projectName, String systemNameHint, String fileName) throws IOException {
        File located = resolveExistingSqlFile(sessionUUID, projectName, fileName);
        if (located == null) throw new IOException("분석 대상 파일을 찾지 못했습니다: " + fileName);
        String bucket = getBucketForFile(sessionUUID, projectName, located);
        if (!PLSQL_DIR.equals(bucket)) {
            return false;
        }

        String systemName = systemNameHint != null ? systemNameHint : detectSystemNameForFile(sessionUUID, projectName, located);
        boolean already = analysisExists(sessionUUID, projectName, systemName, fileName);
        if (!already) {
            parseAndSaveStructure(fileName, sessionUUID, projectName, systemName);
            return true;
        }
        return false;
    }


    /**
     * 파일명을 기준으로 실제 파일을 찾아 내용/객체명/타입 및 분석 존재 여부를 반환합니다.
     * @param sessionUUID 세션 UUID
     * @param projectName 프로젝트명
     * @param fileName 파일명(경로 허용)
     * @return 파일 정보 맵 {fileName, fileContent, objectName, fileType, analysisExists}
     * @throws IOException 파일을 찾지 못한 경우
     */
    public Map<String, String> getFileInfoByName(String sessionUUID, String projectName, String fileName) throws IOException {
        File file = findExistingSqlFile(sessionUUID, projectName, fileName);
        if (file == null) throw new IOException("파일을 찾을 수 없습니다: " + fileName);

        String fileContent = readFileContent(file);
        String objectName = extractSqlObjectName(fileContent);
        String fileType = getFileType(file.getName());

        String systemName = detectSystemNameForFile(sessionUUID, projectName, file);
        String analysisDir = systemName != null ? getAnalysisDirectory(sessionUUID, projectName, systemName)
                                               : getAnalysisDirectory(sessionUUID, projectName);
        String baseFileName = file.getName().substring(0, file.getName().lastIndexOf('.'));
        File analysisFile = new File(analysisDir, baseFileName + ".json");
        boolean analysisExists = analysisFile.exists();

        return createFileInfoMapWithAnalysis(file.getName(), fileContent, objectName, fileType, analysisExists);
    }
    
    /**
     * SQL 내용에서 객체 이름을 추출
     * @param sqlContent SQL 파일 내용
     * @return 추출된 객체 이름, 매칭되지 않으면 null
     */
    public String extractSqlObjectName(String sqlContent) {
        Matcher matcher = SQL_OBJECT_PATTERN.matcher(sqlContent);
        if (matcher.find()) {
            String name = matcher.group("full");
            if (name == null) return null;
            name = name.replace("\"", "");
            name = name.replaceAll("\\s*\\.\\s*", ".");
            name = name.replaceAll("^.*\\.", ""); // 스키마 접두사 제거
            return name;
        }
        return null;
    }
}