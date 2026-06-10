package legacymodernizer.parser.service.strategy;

import java.io.File;
import java.util.Set;

import legacymodernizer.parser.service.ParseProgressTracker;

/**
 * Target(언어)별 단일 파일 파싱 전략.
 *
 * <p>호출자가 target 을 던지지 않는다 — {@code LanguageDetector} 가 파일별로 적절한 전략을
 * 자동 선택하고, {@code ParsingOrchestrator} 가 파일마다 알맞은 전략의
 * {@link #parseFileWithStream}을 호출한다. 그래서 전략은 "파일 1개를 어떻게 파싱하나"와
 * "어떤 확장자/타입을 담당하나"만 안다. (업로드/전체순회는 storage·orchestrator 영역)
 *
 * <p>구현체: JavaParserStrategy, CParserStrategy, PlSqlParserStrategy, PostgreSqlParserStrategy, PythonParserStrategy
 */
public interface TargetParserStrategy {

    /**
     * 파일별 파싱 전 1회 준비(선택) — 프로젝트 전역 컨텍스트가 필요한 전략용.
     * 예: C 는 {@code .c/.h} 전체에서 typedef/매크로를 먼저 수집해야 정확히 파싱된다. 기본 no-op.
     */
    default void prepare() {
    }

    /**
     * 단일 파일 ANTLR 파싱 (스트림 방식) — 진행 상황을 {@link ParseProgressTracker} 로 전달.
     *
     * @param file       파싱할 파일
     * @param outputPath 출력 경로 (analysis/*.json)
     * @param tracker    진행 상황 추적기
     */
    void parseFileWithStream(File file, String outputPath, ParseProgressTracker tracker) throws Exception;

    /** 지원하는 target 타입 (예: "java", "oracle", "postgresql"). */
    String getSupportedTargetType();

    /**
     * 담당 확장자 목록 (예: java → {@code {".java"}}). {@code LanguageDetector} 가 확장자→전략
     * 라우팅을 이 값에서 역으로 구성한다. {@code .sql} 처럼 여러 전략이 같은 확장자를 주장하면
     * 내용 기반 방언 판별로 가린다.
     */
    Set<String> getTargetExtensions();
}
