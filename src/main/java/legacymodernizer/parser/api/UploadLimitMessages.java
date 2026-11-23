package legacymodernizer.parser.api;

import java.util.Locale;
import java.util.Map;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.util.unit.DataSize;

/**
 * 업로드가 한도에 걸렸을 때 <b>어느 한도에 걸렸는지</b> 사용자 말로 알려 준다 (2026-11-23).
 *
 * <p>전에는 어떤 이유든 "최대 파일 크기 100MB, 최대 요청 크기 500MB" 한 문구였다. 포스코 현장(2026-11-20)에서
 * 72KB 폴더가 이 문구로 거절됐는데, 실제 원인은 한글 경로가 part 헤더 한도를 넘은 것, 그다음은 파일 수였다.
 * 문구가 엉뚱해 원인을 찾는 데 시간이 걸렸다.
 *
 * <p>Tomcat · Spring 의 예외 문구는 판마다 조금씩 다르다. 그래서 원인 사슬의 <b>클래스 이름</b>을 먼저 보고,
 * 그다음 문구의 낱말로 고른다. 한도 값은 고정 문구가 아니라 <b>지금 설정</b>에서 읽는다.
 */
final class UploadLimitMessages {

    enum Reason { FILE_TOO_LARGE, REQUEST_TOO_LARGE, TOO_MANY_FILES, PATH_TOO_LONG, TOO_MANY_FIELDS, LIMIT_OTHER, NOT_A_LIMIT }

    private UploadLimitMessages() {}

    static Reason classify(Throwable ex) {
        boolean sizeHint = false;
        for (Throwable t = ex; t != null; t = t.getCause() == t ? null : t.getCause()) {
            String name = t.getClass().getSimpleName();
            String msg = t.getMessage() == null ? "" : t.getMessage().toLowerCase(Locale.ROOT);
            // Tomcat 은 part 헤더 한도에도 SizeLimitExceededException 을 쓴다("Header section has more than …") —
            // 그래서 낱말 "header" 를 클래스보다 먼저 본다.
            if (msg.contains("header")) return Reason.PATH_TOO_LONG;
            if (name.equals("FileCountLimitExceededException")) return Reason.TOO_MANY_FILES;
            if (name.equals("FileSizeLimitExceededException")) return Reason.FILE_TOO_LARGE;
            if (name.equals("SizeLimitExceededException")) return Reason.REQUEST_TOO_LARGE;
            if (msg.contains("parameter")) return Reason.TOO_MANY_FIELDS;
            if (msg.contains("number of parts") || msg.contains("part count") || msg.contains("file count")
                    || msg.contains("maximum number of")) return Reason.TOO_MANY_FILES;
            if (msg.contains("exceed") || name.contains("SizeException") || name.contains("MaxUploadSize")) sizeHint = true;
        }
        return sizeHint ? Reason.LIMIT_OTHER : Reason.NOT_A_LIMIT;
    }

    static HttpStatus status(Reason r) {
        return r == Reason.NOT_A_LIMIT ? HttpStatus.BAD_REQUEST : HttpStatus.PAYLOAD_TOO_LARGE;
    }

    static Map<String, String> body(Reason r, Environment env, Throwable ex) {
        String file = size(env, "spring.servlet.multipart.max-file-size", "1MB");
        String request = size(env, "spring.servlet.multipart.max-request-size", "10MB");
        String header = size(env, "server.tomcat.max-part-header-size", "512B");
        String parts = number(env, "server.tomcat.max-part-count", "50");
        String params = number(env, "server.tomcat.max-parameter-count", "1000");
        String msg = switch (r) {
            case FILE_TOO_LARGE -> "파일 하나가 너무 큽니다. 한 파일은 최대 " + file + "까지 올릴 수 있습니다.";
            case REQUEST_TOO_LARGE -> "한 번에 올리는 전체 크기가 너무 큽니다. 최대 " + request
                    + "까지 올릴 수 있습니다. 폴더를 나눠 올려 주세요.";
            case TOO_MANY_FILES -> "한 번에 올리는 파일 수가 너무 많습니다. 최대 " + parts
                    + "개까지 올릴 수 있습니다. 폴더를 나눠 올려 주세요.";
            case PATH_TOO_LONG -> "파일 경로(폴더·파일 이름)가 너무 깁니다. 한 파일의 경로 정보는 최대 " + header
                    + "까지입니다. 폴더 깊이나 이름을 줄여 주세요.";
            case TOO_MANY_FIELDS -> "요청에 담긴 항목 수가 너무 많습니다. 최대 " + params + "개까지입니다. 폴더를 나눠 올려 주세요.";
            case LIMIT_OTHER -> "업로드 한도를 넘었습니다. 한 파일 최대 " + file + ", 한 번에 최대 " + request
                    + ", 파일 수 최대 " + parts + "개입니다.";
            case NOT_A_LIMIT -> "파일 업로드 처리 중 오류가 발생했습니다: " + (ex.getMessage() == null ? "" : ex.getMessage());
        };
        return Map.of("error", msg, "reason", r.name());
    }

    private static String size(Environment env, String key, String fallback) {
        DataSize v = Binder.get(env).bind(key, DataSize.class).orElse(DataSize.parse(fallback));
        long b = v.toBytes();
        if (b >= 1L << 30 && b % (1L << 30) == 0) return (b >> 30) + "GB";
        if (b >= 1L << 20 && b % (1L << 20) == 0) return (b >> 20) + "MB";
        if (b >= 1L << 10 && b % (1L << 10) == 0) return (b >> 10) + "KB";
        return b + "B";
    }

    private static String number(Environment env, String key, String fallback) {
        Integer v = Binder.get(env).bind(key, Integer.class).orElse(Integer.parseInt(fallback));
        return v < 0 ? "제한 없음" : String.format("%,d", v);
    }
}
