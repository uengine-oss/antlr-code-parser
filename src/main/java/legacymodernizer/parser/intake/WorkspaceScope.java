package legacymodernizer.parser.intake;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 요청이 쓸 작업 폴더 — 프로젝트마다 갈린다.
 *
 * <p><b>작업 폴더가 서버에 하나였다.</b> {@code ROBO_DATA_DIR/{source,ddl,analysis}} 를
 * 모든 사용자가 같이 썼고, 파싱은 그 폴더를 통째로 비우고 다시 쓴다. 그래서
 * 이런 일이 일어났다.
 *
 * <pre>
 *   A 사용자  hr-sample 업로드   →  analysis = 자바 AST
 *   B 사용자  c-src 폴더 지정    →  AST 는 여전히 자바
 *   B 의 분석 결과                →  자기 프로젝트에 **남의 코드**가 들어간다
 * </pre>
 *
 * <p>오류가 나지 않는다. 노드 수도 그럴듯해서, 그래프 안의 이름을 보기 전에는
 * 알 수 없다.
 *
 * <p>그래서 요청이 가리키는 graph 로 한 칸 내려 쓴다.
 *
 * <pre>
 *   &lt;data&gt;/projects/&lt;graph&gt;/{source,ddl,analysis}
 * </pre>
 *
 * <p>값이 없으면(단독 실행·옛 호출자) 예전처럼 루트를 쓴다 — 동작이 바뀌지 않는다.
 *
 * <p><b>스레드 풀을 건너야 한다.</b> 파싱은 {@code ExecutorService} 에서 도는데 풀
 * 스레드는 재사용되므로, 요청 스레드의 값을 그대로 물려받게 두면 <b>다음 요청이
 * 남의 폴더를 쓴다.</b> 작업을 넘길 때 값을 명시적으로 실어 보내고
 * ({@link #capture()}), 작업 안에서 세웠다가 끝나면 반드시 지운다.
 */
public final class WorkspaceScope {

    /** 프로젝트별 폴더가 모이는 자리. 옛 배치(루트 직속)와 섞이지 않게 한 칸 둔다. */
    private static final String PROJECTS = "projects";

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private WorkspaceScope() {}

    /** 헤더에서 온 값. 빈 값이면 범위 없음(= 루트). */
    public static void set(String workspace) {
        String clean = sanitize(workspace);
        if (clean == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(clean);
        }
    }

    /** 지금 스레드의 범위. 없으면 {@code null}. */
    public static String current() {
        return CURRENT.get();
    }

    /** 다른 스레드로 넘길 값. {@link #set(String)} 과 짝이다. */
    public static String capture() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }

    /** 기준 폴더 아래에서 이 요청이 쓸 뿌리. */
    public static Path rootOf(String baseDir) {
        String ws = CURRENT.get();
        if (ws == null) {
            return Paths.get(baseDir);
        }
        return Paths.get(baseDir, PROJECTS, ws);
    }

    /**
     * 폴더 이름으로 쓸 수 있는 값만 통과시킨다.
     *
     * <p>graph 이름이 그대로 경로가 되므로 {@code ../} 같은 것이 들어오면 작업
     * 폴더 밖을 비우게 된다 — 이 코드는 <b>폴더를 지우는</b> 쪽이라 특히 위험하다.
     */
    static String sanitize(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty() || !trimmed.matches("[A-Za-z0-9_-]{1,128}")) {
            return null;
        }
        return trimmed;
    }
}
