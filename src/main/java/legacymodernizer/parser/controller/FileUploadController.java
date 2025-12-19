package legacymodernizer.parser.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import legacymodernizer.parser.service.parsing.ParserStrategyFactory;
import legacymodernizer.parser.service.parsing.TargetParserStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 파일 업로드 및 파싱 API
 * 
 * ┌─────────────────────────────────────────────────────────────────┐
 * │ POST /antlr/fileUpload                                          │
 * ├─────────────────────────────────────────────────────────────────┤
 * │ Content-Type: multipart/form-data                               │
 * │ Header: Session-UUID (필수)                                      │
 * │                                                                  │
 * │ Parts:                                                          │
 * │   metadata: {"target": "java", "projectName": "MyProject"}      │
 * │   files: 파일들 (filename에 상대경로 포함)                        │
 * │          예: MyProject/user/UserService.java                    │
 * │              MyProject/ddl/schema.sql (→ ddl 폴더로 자동 분류)   │
 * │                                                                  │
 * │ Response:                                                       │
 * │   { "projectName": "...",                                       │
 * │     "files": [{"fileName": "...", "fileContent": "..."}],       │
 * │     "ddlFiles": [{"fileName": "...", "fileContent": "..."}] }   │
 * └─────────────────────────────────────────────────────────────────┘
 * 
 * ┌─────────────────────────────────────────────────────────────────┐
 * │ POST /antlr/parse                                               │
 * ├─────────────────────────────────────────────────────────────────┤
 * │ Content-Type: application/json                                  │
 * │ Header: Session-UUID (필수)                                      │
 * │                                                                  │
 * │ Body: {"target": "java", "projectName": "MyProject"}            │
 * │                                                                  │
 * │ Response: {"projectName": "...", "status": "complete"}          │
 * └─────────────────────────────────────────────────────────────────┘
 * 
 * 저장 구조:
 *   data/{sessionUUID}/{projectName}/
 *     ├── source/   → 소스 파일 (원본 폴더 구조 유지)
 *     ├── ddl/      → DDL 파일 (원본 폴더 구조 유지)
 *     └── analysis/ → 파싱 결과 JSON (source와 동일 구조)
 */
@Slf4j
@RestController
@RequestMapping("/antlr")
@RequiredArgsConstructor
public class FileUploadController {

    private final ParserStrategyFactory parserStrategyFactory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SESSION_HEADER = "Session-UUID";

    /**
     * 파일 업로드
     */
    @PostMapping(value = "/fileUpload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> upload(
            @RequestPart("metadata") String metadata,
            @RequestPart("files") MultipartFile[] files,
            HttpServletRequest request) {

        // 세션 검증
        String sessionUUID = request.getHeader(SESSION_HEADER);
        if (isBlank(sessionUUID)) {
            return badRequest("Session-UUID 헤더가 필요합니다");
        }

        // 메타데이터 파싱
        Map<String, Object> meta;
        try {
            meta = objectMapper.readValue(metadata, new TypeReference<>() {});
        } catch (Exception e) {
            return badRequest("metadata JSON 파싱 실패");
        }

        String target = (String) meta.get("target");
        String projectName = (String) meta.get("projectName");

        // 필수값 검증
        if (isBlank(target)) return badRequest("target 필수");
        if (isBlank(projectName)) return badRequest("projectName 필수");
        if (files == null || files.length == 0) return badRequest("files 필수");

        // 업로드 처리
        try {
            TargetParserStrategy strategy = parserStrategyFactory.getStrategy(target);
            Map<String, Object> result = strategy.upload(sessionUUID, projectName, files);

            log.info("[업로드 완료] session={}, project={}, src={}개, ddl={}개",
                    sessionUUID, projectName,
                    size(result.get("files")),
                    size(result.get("ddlFiles")));

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return badRequest("지원하지 않는 target: " + target);
        } catch (Exception e) {
            log.error("[업로드 실패] {}", e.getMessage(), e);
            return badRequest(e.getMessage());
        }
    }

    /**
     * 파싱 (ANTLR 분석)
     */
    @PostMapping("/parse")
    public ResponseEntity<Map<String, Object>> parse(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {

        // 세션 검증
        String sessionUUID = request.getHeader(SESSION_HEADER);
        if (isBlank(sessionUUID)) {
            return badRequest("Session-UUID 헤더가 필요합니다");
        }

        String target = (String) body.get("target");
        String projectName = (String) body.get("projectName");

        // 필수값 검증
        if (isBlank(target)) return badRequest("target 필수");
        if (isBlank(projectName)) return badRequest("projectName 필수");

        // 파싱 처리
        try {
            TargetParserStrategy strategy = parserStrategyFactory.getStrategy(target);
            strategy.parse(sessionUUID, projectName);

            log.info("[파싱 완료] session={}, project={}", sessionUUID, projectName);

            return ResponseEntity.ok(Map.of(
                    "projectName", projectName,
                    "status", "complete"));

        } catch (IllegalArgumentException e) {
            return badRequest("지원하지 않는 target: " + target);
        } catch (Exception e) {
            log.error("[파싱 실패] {}", e.getMessage(), e);
            return badRequest(e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private int size(Object list) {
        return list instanceof java.util.List<?> l ? l.size() : 0;
    }

    private ResponseEntity<Map<String, Object>> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", message));
    }
}
