package legacymodernizer.parser.api;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
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

import legacymodernizer.parser.api.stream.ParseStreamEvent;
import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.parsing.ParseOrchestrator;
import legacymodernizer.parser.parsing.ParserSelection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 파일 업로드 및 파싱 API — <b>호출자가 target(언어)을 던지지 않는다.</b>
 *
 * <p>언어/방언은 {@link ParserSelection} 이 자동 판별한다: 확장자로 1차 라우팅 +
 * {@code .sql} 은 내용의 방언 마커(Oracle vs PostgreSQL)로 결정. 프로젝트에 여러 언어가
 * 섞여 있어도 파일마다 알맞은 파서로 라우팅된다.
 *
 * <pre>
 * POST /antlr/fileUpload  (multipart)  소스 저장. metadata.targetFolder 만 선택 사용.
 * POST /antlr/parsing     (NDJSON)     source/ 자동 감지·파싱 스트림.
 *
 * 저장 구조: data/{ source/ , ddl/ , analysis/ }
 * </pre>
 */
@Slf4j
@RestController
@RequestMapping("/antlr")
@RequiredArgsConstructor
public class FileUploadController {

    private final ParseOrchestrator parseOrchestrator;
    private final ParserWorkspace parserWorkspace;
    private final ParserSelection parserSelection;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private static final String APPLICATION_NDJSON = "application/x-ndjson";

    /**
     * 파일 업로드 (기존 폴더 비우고 새로 저장). 모든 지원 확장자 소스를 저장하며,
     * 어떤 언어인지는 파싱 시 자동 감지한다. metadata 의 {@code targetFolder} 만 선택 사용.
     */
    @PostMapping(value = "/fileUpload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> upload(
            @RequestPart(value = "metadata", required = false) String metadata,
            @RequestPart("files") MultipartFile[] files) {

        if (files == null || files.length == 0) return badRequest("files 필수");

        String targetFolder = null;
        if (metadata != null && !metadata.isBlank()) {
            try {
                Map<String, Object> meta = objectMapper.readValue(metadata, new TypeReference<>() {});
                targetFolder = (String) meta.get("targetFolder");
            } catch (Exception e) {
                return badRequest("metadata JSON 파싱 실패");
            }
        }

        try {
            Map<String, Object> uploadSummary =
                    parserWorkspace.uploadFiles(files, parserSelection.supportedExtensions(), targetFolder);
            log.info("[업로드 완료] src={}개, ddl={}개", size(uploadSummary.get("files")),
                    size(uploadSummary.get("ddlFiles")));
            return ResponseEntity.ok(uploadSummary);
        } catch (Exception e) {
            log.error("[업로드 실패] {}", e.getMessage(), e);
            return badRequest(e.getMessage());
        }
    }

    /**
     * 파싱 (자동 감지) — NDJSON 스트림. 언어/target 은 자동 감지(body 무시).
     * 단 {@code project_root} 가 오면 그 로컬 폴더를 직접 파싱(Electron 경로 모드),
     * 없으면 업로드된 {@code data/source} 를 파싱(브라우저 업로드 모드).
     */
    @PostMapping(value = "/parsing", produces = APPLICATION_NDJSON)
    public ResponseEntity<ResponseBodyEmitter> parse(
            @RequestBody(required = false) Map<String, Object> body) {
        // 타임아웃 30분 (대용량 대비)
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(30 * 60 * 1000L);

        // 경로 모드: body.project_root = 분석할 로컬 폴더(robo 와 동일 키). 없으면 업로드 모드.
        String sourcePath = body != null ? (String) body.get("project_root") : null;

        executor.execute(() -> {
            try {
                parseOrchestrator.parse(sourcePath, event -> {
                    try {
                        sendEvent(emitter, event);
                    } catch (IOException sendError) {
                        log.warn("[스트림 전송 실패] {}", sendError.getMessage());
                    }
                });
                sendEvent(emitter, ParseStreamEvent.complete());
                emitter.complete();
                log.info("[파싱 완료]");
            } catch (Exception e) {
                log.error("[파싱 실패] {}", e.getMessage(), e);
                sendErrorAndComplete(emitter, e.getMessage());
            }
        });

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(APPLICATION_NDJSON))
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-transform")
                .header("X-Accel-Buffering", "no")
                .body(emitter);
    }

    // ─────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────

    private void sendErrorAndComplete(ResponseBodyEmitter emitter, String message) {
        try {
            sendEvent(emitter, ParseStreamEvent.builder("error", "run_failed")
                    .content("❌ 파싱을 완료하지 못했어요 — " + message)
                    .phase("FAILED").status("FAILED").build());
            emitter.complete();
        } catch (IOException e) {
            log.warn("[에러 전송 실패] {}", e.getMessage());
            emitter.completeWithError(e);
        }
    }

    private void sendEvent(ResponseBodyEmitter emitter, ParseStreamEvent event) throws IOException {
        emitter.send(event);
        emitter.send("\n");
    }

    private int size(Object list) {
        return list instanceof java.util.List<?> l ? l.size() : 0;
    }

    private ResponseEntity<Map<String, Object>> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", message));
    }
}
