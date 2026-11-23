package legacymodernizer.parser.api;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import java.util.Map;


@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 모든 origin 허용 - CORS는 API Gateway에서 통합 관리
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
        String detail = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        return ResponseEntity.status(status).body(Map.of("detail", detail != null ? detail : ""));
    }

    private final org.springframework.core.env.Environment env;

    GlobalExceptionHandler(org.springframework.core.env.Environment env) {
        this.env = env;
    }

    /** 어느 한도에 걸렸는지 골라 그 한도 값으로 알린다 — {@link UploadLimitMessages}. */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Map<String, String>> handleMultipartException(MultipartException ex) {
        UploadLimitMessages.Reason reason = UploadLimitMessages.classify(ex);
        if (reason == UploadLimitMessages.Reason.NOT_A_LIMIT && ex instanceof MaxUploadSizeExceededException) {
            reason = UploadLimitMessages.Reason.LIMIT_OTHER;
        }
        // 원문은 서버 로그에만 — 화면 문구로 못 고른 경우에도 원인을 찾을 수 있게.
        org.slf4j.LoggerFactory.getLogger(getClass()).warn("[업로드 거절] {} — {}", reason, rootMessage(ex));
        return ResponseEntity.status(UploadLimitMessages.status(reason))
                .body(UploadLimitMessages.body(reason, env, ex));
    }

    private static String rootMessage(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        for (Throwable t = ex; t != null; t = t.getCause() == t ? null : t.getCause()) {
            if (sb.length() > 0) sb.append(" ← ");
            sb.append(t.getClass().getSimpleName()).append(": ").append(t.getMessage());
        }
        return sb.toString();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        // 500으로 변환되며 스택이 사라지면 원인 추적이 불가능하다 — 서버 로그가 유일한 흔적.
        org.slf4j.LoggerFactory.getLogger(getClass()).error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("detail", "Unexpected error: " + (ex.getMessage() != null ? ex.getMessage() : "")));
    }
}
