package legacymodernizer.parser.api;

import java.util.Map;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.DefaultPropertiesPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 업로드 한도의 이미지 기본값 (2026-11-23).
 *
 * <p>전에는 아무 값도 없어 Spring 기본값(파일 하나 1MB · 요청 전체 10MB)이 걸려 있었다. 그런데 거절 문구는
 * "최대 파일 100MB, 최대 요청 500MB" 로 고정돼 있어, 한도 안이라 믿은 사용자가 엉뚱한 안내를 받았다.
 * 앞단 nginx 는 512m 이다.
 *
 * <p>Tomcat 쪽 셋은 포스코 현장(2026-11-20)에서 폴더 업로드가 막혀 compose env 로 올렸던 값이다
 * (한글 경로가 part 헤더 512B 를 넘음 · 파일 50개 · 인자 1000개). 이미지 기본값으로 옮긴다.
 *
 * <p>가장 낮은 우선순위(default properties)라 env · 로컬 application.properties 가 덮는다
 * (예: {@code SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=1GB}). application.properties 는 이 저장소에서
 * 개인 로컬 설정이라 git 이 무시하므로 여기에 둔다.
 */
public class UploadLimitDefaults implements EnvironmentPostProcessor {

    static final Map<String, Object> DEFAULTS = Map.of(
            "spring.servlet.multipart.max-file-size", "100MB",
            "spring.servlet.multipart.max-request-size", "500MB",
            "server.tomcat.max-part-header-size", "16KB",
            "server.tomcat.max-part-count", "100000",
            "server.tomcat.max-parameter-count", "100000");

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        DefaultPropertiesPropertySource.addOrMerge(DEFAULTS, environment.getPropertySources());
    }
}
