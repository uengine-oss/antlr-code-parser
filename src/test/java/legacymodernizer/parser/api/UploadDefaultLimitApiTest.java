package legacymodernizer.parser.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;

/**
 * 이미지 기본 한도가 실제로 문구와 같은 값이다 (2026-11-23).
 * 전에는 설정이 없어 Spring 기본값(파일 하나 1MB)이 걸렸다 — 2MB 파일 하나가 413 이었다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UploadDefaultLimitApiTest {

    @LocalServerPort private int port;
    @Autowired private Environment env;

    @Test
    void defaultsAreTheAdvertisedLimits() {
        assertEquals("100MB", env.getProperty("spring.servlet.multipart.max-file-size"));
        assertEquals("500MB", env.getProperty("spring.servlet.multipart.max-request-size"));
        assertEquals("16KB", env.getProperty("server.tomcat.max-part-header-size"));
    }

    @Test
    void twoMegabyteFileIsNotRejectedForSize() throws Exception {
        HttpResponse<String> r = MultipartPost.files(port, 1, 2 * 1024 * 1024, "src/Big.java");
        assertEquals(200, r.statusCode(), r.body());
    }

    @Test
    void sixtyKoreanPathFilesPass() throws Exception {   // 11-20 현장 모양: 한글 경로 · 파일 50개 넘게
        HttpResponse<String> r = MultipartPost.files(port, 60, 100, "소스/업무/인사관리/급여계산/모듈#.java");
        assertEquals(200, r.statusCode(), r.body());
    }
}
