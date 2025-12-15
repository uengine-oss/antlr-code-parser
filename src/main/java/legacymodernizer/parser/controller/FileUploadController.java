package legacymodernizer.parser.controller;

import java.util.Map;
import java.util.List;
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
import legacymodernizer.parser.service.parsing.TargetParserStrategy;
import legacymodernizer.parser.service.parsing.ParserStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FileUploadController {

    private final ParserStrategyFactory parserStrategyFactory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 파일 업로드
     * 
     * 성공 (200): {projectName, systemFiles, ddlFiles}
     * 실패 (400): {error}
     */
    @PostMapping(value = "/fileUpload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> fileUpload(@RequestPart("metadata") String metadata,
            @RequestPart("files") MultipartFile[] files,
            HttpServletRequest httpRequest) {
        
        String sessionUUID = httpRequest.getHeader("Session-UUID");
        if (sessionUUID == null || sessionUUID.trim().isEmpty()) {
            return error("세션 정보 없음");
        }

        Map<String, Object> request;
        try {
            request = objectMapper.readValue(metadata, new TypeReference<>() {});
        } catch (Exception e) {
            return error("metadata 형식 오류");
        }

        String target = (String) request.get("target");
        String projectName = (String) request.get("projectName");
        Object systemsObj = request.get("systems");
        Object ddlObj = request.get("ddl");

        if (target == null || target.isBlank()) {
            return error("target 필수");
        }
        if (projectName == null || projectName.isBlank()) {
            return error("projectName 필수");
        }
        if (!(systemsObj instanceof List<?>)) {
            return error("systems 필수");
        }
        if (files == null || files.length == 0) {
            return error("파일 필수");
        }

        Map<String, MultipartFile> nameToFile = new HashMap<>();
        for (MultipartFile f : files) {
            if (f == null || f.isEmpty()) continue;
            String key = f.getOriginalFilename();
            if (key != null) {
                nameToFile.put(key.toLowerCase(), f);
            }
        }
        if (nameToFile.isEmpty()) {
            return error("유효한 파일 없음");
        }

        try {
            TargetParserStrategy strategy = parserStrategyFactory.getStrategy(target);
            List<?> systems = (List<?>) systemsObj;
            List<?> ddlList = ddlObj instanceof List<?> ? (List<?>) ddlObj : null;

            Map<String, Object> result = strategy.upload(sessionUUID, projectName, systems, ddlList, nameToFile);

            log.info("[업로드] 프로젝트: {}, 시스템파일: {}개, DDL: {}개",
                    projectName,
                    ((List<?>) result.get("systemFiles")).size(),
                    ((List<?>) result.get("ddlFiles")).size());

            Map<String, Object> response = new HashMap<>();
            response.put("projectName", projectName);
            response.put("systemFiles", result.get("systemFiles"));
            response.put("ddlFiles", result.get("ddlFiles"));
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return error("지원하지 않는 target: " + target);
        } catch (RuntimeException e) {
            log.error("[업로드 실패] {}", e.getMessage());
            return error(e.getMessage());
        }
    }

    /**
     * 파싱 (ANTLR 분석)
     * 
     * 성공 (200): {projectName, files}
     * 실패 (400): {error}
     */
    @PostMapping("/parsing")
    public ResponseEntity<Map<String, Object>> parsing(@RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        
        String sessionUUID = httpRequest.getHeader("Session-UUID");
        if (sessionUUID == null || sessionUUID.trim().isEmpty()) {
            return error("세션 정보 없음");
        }

        String target = (String) request.get("target");
        String projectName = (String) request.get("projectName");
        Object systemsObj = request.get("systems");

        if (target == null || target.isBlank()) {
            return error("target 필수");
        }
        if (projectName == null || projectName.isBlank()) {
            return error("projectName 필수");
        }
        if (!(systemsObj instanceof List<?>)) {
            return error("systems 필수");
        }

        try {
            TargetParserStrategy strategy = parserStrategyFactory.getStrategy(target);
            List<?> systems = (List<?>) systemsObj;

            Map<String, Object> result = strategy.parse(sessionUUID, projectName, systems);

            log.info("[파싱] 프로젝트: {}, 파일: {}개",
                    projectName,
                    ((List<?>) result.get("files")).size());

            Map<String, Object> response = new HashMap<>();
            response.put("projectName", projectName);
            response.put("files", result.get("files"));
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return error("지원하지 않는 target: " + target);
        } catch (RuntimeException e) {
            log.error("[파싱 실패] {}", e.getMessage());
            return error(e.getMessage());
        }
    }

    private ResponseEntity<Map<String, Object>> error(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", message));
    }
}
