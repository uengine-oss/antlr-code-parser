package legacymodernizer.parser.controller;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import legacymodernizer.parser.service.PlSqlFileParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FileUploadController {

    private final PlSqlFileParserService plSqlFileParserService;

    /**
     * 업로드 맵에서 요청 파일명을 대소문자 무시/베이스명 기준으로 찾아 반환합니다.
     * @param nameToFile 업로드 파일명(lowercase)→파일 매핑
     * @param requestedFileName 요청된 파일명(경로 포함 가능)
     * @return 매칭된 MultipartFile, 없으면 null
     */
    private MultipartFile resolveUploaded(Map<String, MultipartFile> nameToFile, String requestedFileName) {
        if (requestedFileName == null) return null;
        MultipartFile mf = nameToFile.get(requestedFileName.toLowerCase());
        if (mf != null) return mf;
        for (Map.Entry<String, MultipartFile> e : nameToFile.entrySet()) {
            String base = java.nio.file.Paths.get(e.getKey()).getFileName().toString();
            if (base.equalsIgnoreCase(requestedFileName)) return e.getValue();
        }
        return null;
    }

    /**
     * 단순 버킷(ddl/sequence) 리스트를 순회 저장합니다.
     * @param bucket 버킷 이름 (ddl|sequence)
     * @param listObj 파일명 목록 객체(List<String> 기대)
     * @param nameToFile 업로드 파일명(lowercase)→파일 매핑
     * @param sessionUUID 세션 UUID
     * @param projectName 프로젝트명
     * @param successFiles 성공 파일 정보 리스트(변경됨)
     * @param failedFiles 실패 파일 정보 리스트(변경됨)
     */
    private void saveSimpleBucketList(String bucket,
                                      Object listObj,
                                      Map<String, MultipartFile> nameToFile,
                                      String sessionUUID,
                                      String projectName,
                                      List<Map<String, String>> successFiles,
                                      List<Map<String, String>> failedFiles) {
        if (!(listObj instanceof List<?>)) return;
        for (Object o : (List<?>) listObj) {
            if (!(o instanceof String)) continue;
            String fileName = (String) o;
            MultipartFile mf = resolveUploaded(nameToFile, fileName);
            if (mf == null) { log.warn("  ✗ ({}) 업로드 목록에서 찾지 못함: {}", bucket, fileName); continue; }
            try {
                saveToBucketAndMaybeRespond(sessionUUID, projectName, bucket, null, fileName, mf, successFiles, false);
                log.info("  ({}) {}", bucket, fileName);
            } catch (Exception e) {
                failedFiles.add(Map.of("fileName", fileName, "error", e.getMessage()));
                log.warn("  ({}) {} - {}", bucket, fileName, e.getMessage());
            }
        }
    }

    /**
     * 파일을 지정된 버킷에 저장하고, 필요 시 내용까지 성공 목록에 포함합니다.
     * @param sessionUUID 세션 UUID
     * @param projectName 프로젝트명
     * @param bucket 버킷 이름(src|ddl|sequence)
     * @param systemName 시스템명(src일 때만 사용, null 가능)
     * @param fileName 원본 파일명
     * @param mf 업로드 파일
     * @param successFiles 성공 파일 목록(변경됨)
     * @param includeContent true면 파일 내용을 successFiles에 포함
     * @throws Exception 저장/읽기 실패 시
     */
    private void saveToBucketAndMaybeRespond(String sessionUUID,
                                             String projectName,
                                             String bucket,
                                             String systemName,
                                             String fileName,
                                             MultipartFile mf,
                                             List<Map<String, String>> successFiles,
                                             boolean includeContent) throws Exception {
        String path = plSqlFileParserService.saveBytesToBucket(sessionUUID, projectName, bucket, systemName, fileName, mf.getBytes());
        if (includeContent) {
            String content = plSqlFileParserService.readFileContent(new java.io.File(path));
            Map<String, String> ok = new HashMap<>();
            ok.put("fileName", fileName);
            ok.put("filePath", path);
            ok.put("fileContent", content);
            successFiles.add(ok);
        } else {
            log.debug("  · saved to {}: {}", bucket, path);
        }
    }


    /**
     * 파일 업로드를 처리합니다.
     * @param metadata 업로드 메타데이터(JSON 문자열). {dbms, projectName, systems[], ddl[], sequence[]} 포함
     * @param files 업로드된 파일 배열
     * @param httpRequest HTTP 요청(헤더의 Session-UUID 사용)
     * @return {dbms, successFiles[, failedFiles]} 응답
     */
    @PostMapping(value = "/fileUpload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> fileUpload(@RequestPart("metadata") String metadata,
                                                          @RequestPart("files") MultipartFile[] files,
                                                          HttpServletRequest httpRequest) {
        // 세션 정보 추출
        String sessionUUID = httpRequest.getHeader("Session-UUID");
        log.debug("세션 UUID: {}", sessionUUID);
        if (sessionUUID == null || sessionUUID.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "세션 정보가 없습니다"));
        }

        // 메타데이터 파싱
        Map<String, Object> request = new HashMap<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            request = mapper.readValue(metadata, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "metadata 파싱 실패: " + e.getMessage()));
        }

        String dbms = (String) request.getOrDefault("dbms", "");
        String projectName = (String) request.getOrDefault("projectName", "");
        Object systemsObj = request.get("systems");
        Object ddlObj = request.get("ddl");
        Object seqObj = request.get("sequence");

        int filesCount = files != null ? files.length : 0;
        int systemsCount = (systemsObj instanceof List<?>) ? ((List<?>) systemsObj).size() : 0;
        
        log.info("\n" +
                "================================================================================\n" +
                " [파일 업로드]\n" +
                "================================================================================\n" +
                "  DBMS: {}  |  프로젝트: {}  |  파일: {}개  |  시스템: {}개",
                dbms, projectName, filesCount, systemsCount);

        List<Map<String, String>> successFiles = new ArrayList<>();
        List<Map<String, String>> failedFiles = new ArrayList<>();

        // 1) 파일 저장 (메타데이터 기준 버킷 저장) - 응답은 SP만 포함
        if (files != null && files.length > 0) {
            log.info("\n[1단계] 파일 저장 (메타데이터 기준)");

            // 업로드된 파일을 빠르게 조회하기 위한 맵 (원본 파일명 대소문자 무시)
            Map<String, MultipartFile> nameToFile = new HashMap<>();
            for (MultipartFile f : files) {
                if (f == null || f.isEmpty()) continue;
                String key = f.getOriginalFilename() != null ? f.getOriginalFilename().toLowerCase() : null;
                if (key != null) nameToFile.put(key, f);
            }

            // 1-1) systems: 각 시스템별로 지정된 파일을 src/<system>/에 저장
            if (systemsObj instanceof List<?>) {
                List<?> systems = (List<?>) systemsObj;
                for (Object sys : systems) {
                    if (!(sys instanceof Map)) continue;
                    Map<?, ?> sysMap = (Map<?, ?>) sys;
                    String systemName = (String) sysMap.get("name");
                    Object spObj = sysMap.get("sp");
                    if (systemName == null || systemName.trim().isEmpty() || !(spObj instanceof List<?>)) continue;
                    for (Object sp : (List<?>) spObj) {
                        if (!(sp instanceof String)) continue;
                        String fileName = (String) sp;
                        MultipartFile mf = resolveUploaded(nameToFile, fileName);
                        if (mf == null) {
                            log.warn("  ✗ (systems) 업로드 목록에서 찾지 못함: {}", fileName);
                            continue;
                        }
                        try {
                            saveToBucketAndMaybeRespond(sessionUUID, projectName, "src", systemName, fileName, mf, successFiles, true);
                            log.info("  (systems) {} → src/{}/", fileName, systemName);
                        } catch (Exception e) {
                            failedFiles.add(Map.of("fileName", fileName, "error", e.getMessage()));
                            log.warn("  (systems) {} - {}", fileName, e.getMessage());
                        }
                    }
                }
            }

            // 1-2) ddl 버킷 그대로 저장
            saveSimpleBucketList("ddl", ddlObj, nameToFile, sessionUUID, projectName, successFiles, failedFiles);

            // 1-3) sequence 버킷 그대로 저장
            saveSimpleBucketList("sequence", seqObj, nameToFile, sessionUUID, projectName, successFiles, failedFiles);

            log.info("  → 저장 완료(메타데이터 기준): {}개", successFiles.size());
        }

        try {
            // DDL/SEQUENCE 파일 정리
            if (ddlObj instanceof List<?>) {
                List<?> ddlList = (List<?>) ddlObj;
                if (!ddlList.isEmpty()) {
                    log.info("\n[3단계] DDL 파일 정리 ({}개)", ddlList.size());
                    for (Object o : ddlList) {
                        if (o instanceof String) {
                            try {
                                plSqlFileParserService.ensureFileInTopLevelDir(sessionUUID, projectName, "ddl", (String) o);
                            } catch (Exception e) {
                                Map<String, String> failed = new HashMap<>();
                                failed.put("fileName", String.valueOf(o));
                                failed.put("error", e.getMessage());
                                failedFiles.add(failed);
                                log.warn("  ✗ {} - {}", o, e.getMessage());
                            }
                        }
                    }
                }
            }

            if (seqObj instanceof List<?>) {
                List<?> seqList = (List<?>) seqObj;
                if (!seqList.isEmpty()) {
                    log.info("\n[4단계] SEQUENCE 파일 정리 ({}개)", seqList.size());
                    for (Object o : seqList) {
                        if (o instanceof String) {
                            try {
                                plSqlFileParserService.ensureFileInTopLevelDir(sessionUUID, projectName, "sequence", (String) o);
                            } catch (Exception e) {
                                Map<String, String> failed = new HashMap<>();
                                failed.put("fileName", String.valueOf(o));
                                failed.put("error", e.getMessage());
                                failedFiles.add(failed);
                                log.warn("  ✗ {} - {}", o, e.getMessage());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("\n  처리 실패: {}", e.getMessage());
            log.error("================================================================================\n");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("dbms", dbms, "successFiles", successFiles, "failedFiles", failedFiles, "message", e.getMessage()));
        }

        if (!failedFiles.isEmpty()) {
            log.warn("\n  완료 (일부 실패) - 성공: {}개, 실패: {}개", successFiles.size(), failedFiles.size());
            log.warn("================================================================================\n");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("dbms", dbms, "successFiles", successFiles, "failedFiles", failedFiles));
        }

        log.info("\n  업로드 완료 - 총 {}개 파일", successFiles.size());
        log.info("================================================================================\n");
        return ResponseEntity.ok(Map.of("dbms", dbms, "successFiles", successFiles));
    }


    // 분석 결과 클래스
    private static class AnalysisResult {
        final List<Map<String, String>> successFiles;
        final List<Map<String, String>> failedFiles;
        AnalysisResult(List<Map<String, String>> successFiles, List<Map<String, String>> failedFiles) {
            this.successFiles = successFiles;
            this.failedFiles = failedFiles;
        }
    }

    private AnalysisResult analyzeSystems(List<?> systems, String sessionUUID, String projectName) {
        List<Map<String, String>> successFiles = new ArrayList<>();
        List<Map<String, String>> failedFiles = new ArrayList<>();

        int totalFiles = 0;
        for (Object sys : systems) {
            if (sys instanceof Map) {
                Map<?, ?> sysMap = (Map<?, ?>) sys;
                Object spObj = sysMap.get("sp");
                if (spObj instanceof List<?>) {
                    totalFiles += ((List<?>) spObj).size();
                }
            }
        }

        log.info("\n" +
                "================================================================================\n" +
                " [파일 분석]\n" +
                "================================================================================\n" +
                "  시스템: {}개  |  파일: {}개",
                systems.size(), totalFiles);
        
        for (Object sys : systems) {
            if (!(sys instanceof Map)) continue;
            Map<?, ?> sysMap = (Map<?, ?>) sys;
            String systemName = (String) sysMap.get("name");
            Object spObj = sysMap.get("sp");
            if (systemName == null || !(spObj instanceof List<?>)) continue;

            List<?> spArr = (List<?>) spObj;
            log.info("\n[시스템: {}] {}개 파일 분석 중...", systemName, spArr.size());
            
            for (Object sp : spArr) {
                if (!(sp instanceof String)) continue;
                String fileName = (String) sp;
                try {
                    long start = System.currentTimeMillis();
                    // 서비스에서 SP만 분석하고, 이미 분석된 경우 스킵
                    plSqlFileParserService.analyzeSpIfNeeded(sessionUUID, projectName, systemName, fileName);
                    Map<String, String> info = plSqlFileParserService.getFileInfoByName(sessionUUID, projectName, fileName);

                    Map<String, String> ok = new HashMap<>();
                    ok.put("system", systemName);
                    ok.put("fileName", info.getOrDefault("fileName", fileName));
                    ok.put("objectName", info.getOrDefault("objectName", ""));
                    ok.put("fileContent", info.getOrDefault("fileContent", ""));
                    ok.put("fileType", info.getOrDefault("fileType", ""));
                    ok.put("analysisExists", info.getOrDefault("analysisExists", "false"));
                    successFiles.add(ok);
                    long elapsed = System.currentTimeMillis() - start;
                    log.info("  {} ({}ms)", fileName, elapsed);
                } catch (Exception e) {
                    log.warn("  {} - {}", fileName, e.getMessage());
                    Map<String, String> failed = new HashMap<>();
                    failed.put("system", systemName);
                    failed.put("fileName", fileName);
                    failed.put("error", e.getMessage());
                    failedFiles.add(failed);
                }
            }
        }

        log.info("\n   분석 완료 - 성공: {}개, 실패: {}개", successFiles.size(), failedFiles.size());

        return new AnalysisResult(successFiles, failedFiles);
    }

    /**
     * 업로드된 파일들을 시스템별로 분석합니다.
     * @param request {dbms, projectName, systems:[{name, sp:[]}]}
     * @param httpRequest HTTP 요청(헤더의 Session-UUID 사용)
     * @return {dbms, successFiles[, failedFiles]} 응답
     */
    @PostMapping("/parsing")
    public ResponseEntity<Map<String, Object>> analysisContext(@RequestBody Map<String, Object> request, 
                                                HttpServletRequest httpRequest) {
        // 세션 검증
        String sessionUUID = httpRequest.getHeader("Session-UUID");
        if (sessionUUID == null || sessionUUID.trim().isEmpty()) {
            log.warn("[parsing] 세션 UUID가 없습니다");
            return ResponseEntity.badRequest().body(Map.of("message", "세션 정보가 없습니다"));
        }
        
        String dbms = (String) request.getOrDefault("dbms", "");
        String projectName = (String) request.getOrDefault("projectName", "");
        Object systemsObj = request.get("systems");
        if (!(systemsObj instanceof List<?>)) {
            log.warn("[parsing] systems 정보가 없거나 형식이 올바르지 않습니다");
            return ResponseEntity.badRequest().body(Map.of("dbms", dbms, "message", "systems 정보가 없습니다"));
        }

        List<?> systems = (List<?>) systemsObj;
        AnalysisResult result = analyzeSystems(systems, sessionUUID, projectName);

        if (!result.failedFiles.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("dbms", dbms, "successFiles", result.successFiles, "failedFiles", result.failedFiles));
        }
        return ResponseEntity.ok(Map.of("dbms", dbms, "successFiles", result.successFiles));
    }

    /**
     * 테스트 샘플(시스템/파일 목록 기반) 분석을 실행합니다.
     * @param request {dbms, projectName, systems:[{name, sp:[]}]}
     * @param httpRequest HTTP 요청(헤더의 Session-UUID 사용)
     * @return {dbms, successFiles[, failedFiles]} 응답
     */
    @PostMapping("/testsample")
    public ResponseEntity<Map<String, Object>> testSample(@RequestBody Map<String, Object> request, 
                                                         HttpServletRequest httpRequest) {
        // 세션 검증
        String sessionUUID = httpRequest.getHeader("Session-UUID");
        if (sessionUUID == null || sessionUUID.trim().isEmpty()) {
            log.warn("[testsample] 세션 UUID가 없습니다");
            return ResponseEntity.badRequest().body(Map.of("message", "세션 정보가 없습니다"));
        }
        
        // systems 기반 테스트 요청으로 변경
        String dbms = (String) request.getOrDefault("dbms", "");
        String projectName = (String) request.getOrDefault("projectName", "");
        Object systemsObj = request.get("systems");
        if (!(systemsObj instanceof List<?>)) {
            log.warn("[testsample] systems 정보가 없거나 형식이 올바르지 않습니다");
            return ResponseEntity.badRequest().body(Map.of("dbms", dbms, "message", "systems 정보가 없습니다"));
        }
        List<?> systems = (List<?>) systemsObj;
        AnalysisResult result = analyzeSystems(systems, sessionUUID, projectName);

        if (!result.failedFiles.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("dbms", dbms, "successFiles", result.successFiles, "failedFiles", result.failedFiles));
        }
        return ResponseEntity.ok(Map.of("dbms", dbms, "successFiles", result.successFiles));
    }

}