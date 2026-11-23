package legacymodernizer.parser.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * 업로드 한도에 걸리면 <b>걸린 한도</b>를 그 값으로 알린다 (2026-11-23 · 포스코 현장 2026-11-20).
 * 전에는 이유와 상관없이 "최대 100MB" 였고, 실제 한도는 Spring 기본값(1MB · 10MB)이었다.
 * 실제 Tomcat 을 띄워 보낸다 — 예외 문구는 Tomcat 판마다 달라 모의 객체로는 못 잡는다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.servlet.multipart.max-file-size=2KB",
        "spring.servlet.multipart.max-request-size=8KB",
        "server.tomcat.max-part-header-size=1KB",
        "server.tomcat.max-part-count=6",
})
class UploadLimitApiTest {

    @LocalServerPort private int port;

    @Test
    void oneFileTooLarge_saysPerFileLimit() throws Exception {
        HttpResponse<String> r = MultipartPost.files(port, 1, 3 * 1024, "src/A#.java");
        assertEquals(413, r.statusCode(), r.body());
        assertTrue(r.body().contains("FILE_TOO_LARGE") && r.body().contains("2KB"), r.body());
    }

    @Test
    void totalTooLarge_saysRequestLimit() throws Exception {
        HttpResponse<String> r = MultipartPost.files(port, 5, 1900, "src/A#.java");
        assertEquals(413, r.statusCode(), r.body());
        assertTrue(r.body().contains("REQUEST_TOO_LARGE") && r.body().contains("8KB"), r.body());
    }

    @Test
    void tooManyFiles_saysCount() throws Exception {
        HttpResponse<String> r = MultipartPost.files(port, 10, 10, "src/A#.java");
        assertEquals(413, r.statusCode(), r.body());
        assertTrue(r.body().contains("TOO_MANY_FILES") && r.body().contains("6"), r.body());
    }

    @Test
    void longKoreanPath_saysPathTooLong() throws Exception {
        String deep = "한글폴더이름/".repeat(80) + "파일#.java";   // 한 글자 3바이트 → 1KB 를 넘는다
        assertTrue(deep.getBytes(StandardCharsets.UTF_8).length > 1024);
        HttpResponse<String> r = MultipartPost.files(port, 1, 10, deep);
        assertEquals(413, r.statusCode(), r.body());
        assertTrue(r.body().contains("PATH_TOO_LONG") && r.body().contains("1KB"), r.body());
    }

    @Test
    void noLongerSaysTheFixed100MbText() throws Exception {
        HttpResponse<String> r = MultipartPost.files(port, 1, 3 * 1024, "src/A#.java");
        assertFalse(r.body().contains("100MB"), r.body());
    }
}
