package legacymodernizer.parser.controller;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

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
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private static final String SESSION_HEADER = "Session-UUID";
    
    /** NDJSON Content-Type */
    private static final String APPLICATION_NDJSON = "application/x-ndjson";

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
     * 파싱 (ANTLR 분석) - NDJSON 스트림 방식
     * 
     * 진행 상황을 실시간으로 스트림으로 전달합니다.
     * 500라인 기준 초과 시 현재 라인 정보를 전달합니다.
     * 
     * 응답 형식 (NDJSON):
     *   {"type": "message", "content": "📄 UserService.java 파싱 시작..."}\n
     *   {"type": "message", "content": "📍 UserService.java - 523라인까지 파싱 중..."}\n
     *   {"type": "complete"}\n
     */
    @PostMapping(value = "/parsing", produces = APPLICATION_NDJSON)
    public ResponseBodyEmitter parse(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {

        // 타임아웃 30분 (대용량 파일 대비)
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(30 * 60 * 1000L);

        // 세션 검증
        String sessionUUID = request.getHeader(SESSION_HEADER);
        if (isBlank(sessionUUID)) {
            sendErrorAndComplete(emitter, "Session-UUID 헤더가 필요합니다");
            return emitter;
        }

        String target = (String) body.get("target");
        String projectName = (String) body.get("projectName");

        // 필수값 검증
        if (isBlank(target)) {
            sendErrorAndComplete(emitter, "target 필수");
            return emitter;
        }
        if (isBlank(projectName)) {
            sendErrorAndComplete(emitter, "projectName 필수");
            return emitter;
        }

        // 비동기로 파싱 실행
        executor.execute(() -> {
            try {
                TargetParserStrategy strategy = parserStrategyFactory.getStrategy(target);
                
                // 스트림 콜백으로 진행 상황 전달
                strategy.parseWithStream(sessionUUID, projectName, (type, content) -> {
                    try {
                        if (content != null) {
                            emitter.send(Map.of("type", type, "content", content));
                        } else {
                            emitter.send(Map.of("type", type));
                        }
                        emitter.send("\n");
                    } catch (IOException e) {
                        log.warn("[스트림 전송 실패] {}", e.getMessage());
                    }
                });
                
                // 완료 신호
                emitter.send(Map.of("type", "complete"));
                emitter.send("\n");
                emitter.complete();
                
                log.info("[파싱 완료] session={}, project={}", sessionUUID, projectName);

            } catch (IllegalArgumentException e) {
                sendErrorAndComplete(emitter, "지원하지 않는 target: " + target);
            } catch (Exception e) {
                log.error("[파싱 실패] {}", e.getMessage(), e);
                sendErrorAndComplete(emitter, e.getMessage());
            }
        });

        return emitter;
    }
    
    /**
     * 에러 메시지를 보내고 스트림 완료
     */
    private void sendErrorAndComplete(ResponseBodyEmitter emitter, String message) {
        try {
            emitter.send(Map.of("type", "error", "content", message));
            emitter.send("\n");
            emitter.complete();
        } catch (IOException e) {
            log.warn("[에러 전송 실패] {}", e.getMessage());
            emitter.completeWithError(e);
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
