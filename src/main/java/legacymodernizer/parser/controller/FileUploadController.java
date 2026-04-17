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

import legacymodernizer.parser.service.strategy.ParserStrategyFactory;
import legacymodernizer.parser.service.strategy.TargetParserStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 파일 업로드 및 파싱 API
 * 
 * ┌─────────────────────────────────────────────────────────────────┐
 * │ POST /antlr/fileUpload                                          │
 * ├─────────────────────────────────────────────────────────────────┤
 * │ Content-Type: multipart/form-data                               │
 * │ Headers:                                                        │
 * │   Accept-Language: ko (선택)                                     │
 * │   OpenAI-Api-Key: sk-... (선택)                                  │
 * │                                                                  │
 * │ Parts:                                                          │
 * │   metadata: {                                                   │
 * │     "strategy": "framework",  // "framework" | "dbms"           │
 * │     "target": "java",         // "java"|"oracle"|"postgresql"   │
 * │     "nameCase": "original"    // "original"|"uppercase"|"lowercase"│
 * │   }                                                             │
 * │   files: 파일들 (filename에 상대경로 포함)                        │
 * │                                                                  │
 * │ Response:                                                       │
 * │   { "files": [...], "ddlFiles": [...] }                         │
 * └─────────────────────────────────────────────────────────────────┘
 * 
 * ┌─────────────────────────────────────────────────────────────────┐
 * │ POST /antlr/parsing                                             │
 * ├─────────────────────────────────────────────────────────────────┤
 * │ Content-Type: application/json                                  │
 * │ Headers:                                                        │
 * │   Accept-Language: ko (선택)                                     │
 * │                                                                  │
 * │ Body: {                                                         │
 * │   "strategy": "framework",                                      │
 * │   "target": "java",                                             │
 * │   "nameCase": "original"                                        │
 * │ }                                                               │
 * │                                                                  │
 * │ Response: NDJSON 스트림                                          │
 * └─────────────────────────────────────────────────────────────────┘
 * 
 * 저장 구조:
 *   data/
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

    /** NDJSON Content-Type */
    private static final String APPLICATION_NDJSON = "application/x-ndjson";

    /**
     * 파일 업로드 (기존 폴더 비우고 새로 저장)
     */
    @PostMapping(value = "/fileUpload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> upload(
            @RequestPart("metadata") String metadata,
            @RequestPart("files") MultipartFile[] files) {

        // 메타데이터 파싱
        Map<String, Object> meta;
        try {
            meta = objectMapper.readValue(metadata, new TypeReference<>() {});
        } catch (Exception e) {
            return badRequest("metadata JSON 파싱 실패");
        }

        String target = (String) meta.get("target");
        // strategy, nameCase는 필요시 사용
        // String strategy = (String) meta.get("strategy");
        // String nameCase = (String) meta.get("nameCase");

        // 필수값 검증
        if (isBlank(target)) return badRequest("target 필수");
        if (files == null || files.length == 0) return badRequest("files 필수");

        // 업로드 처리
        try {
            TargetParserStrategy strategy = parserStrategyFactory.getStrategy(target);
            Map<String, Object> result = strategy.upload(files);

            log.info("[업로드 완료] target={}, src={}개, ddl={}개",
                    target,
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
     * 
     * 응답 형식 (NDJSON):
     *   {"type": "message", "content": "📄 UserService.java 파싱 시작..."}\n
     *   {"type": "message", "content": "📍 UserService.java - 523라인까지 파싱 중..."}\n
     *   {"type": "complete"}\n
     */
    @PostMapping(value = "/parsing", produces = APPLICATION_NDJSON)
    public ResponseBodyEmitter parse(@RequestBody Map<String, Object> body) {

        // 타임아웃 30분 (대용량 파일 대비)
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(30 * 60 * 1000L);

        String target = (String) body.get("target");
        // strategy, nameCase는 필요시 사용
        // String strategy = (String) body.get("strategy");
        // String nameCase = (String) body.get("nameCase");

        // 필수값 검증
        if (isBlank(target)) {
            sendErrorAndComplete(emitter, "target 필수");
            return emitter;
        }

        // 비동기로 파싱 실행
        executor.execute(() -> {
            try {
                TargetParserStrategy strategy = parserStrategyFactory.getStrategy(target);
                
                // 스트림 콜백으로 진행 상황 전달
                strategy.parseWithStream((type, content) -> {
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
                
                log.info("[파싱 완료] target={}", target);

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
