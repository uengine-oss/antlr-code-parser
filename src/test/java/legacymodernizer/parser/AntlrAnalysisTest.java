package legacymodernizer.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.parsing.ParseOrchestrator;
import legacymodernizer.parser.parsing.ParserSelection;

/**
 * end-to-end 파싱 스모크 테스트.
 *
 * <p>호출자가 target(언어)을 던지지 않는다 — {@link ParseOrchestrator}가
 * {@link ParserSelection}으로 파일별 파서를 자동 결정해 {@code data/source}를 파싱하고,
 * {@code data/analysis} 에 JSON 을 미러로 떨군다. (테스트 데이터가 있을 때만 검증)
 */
@SpringBootTest
public class AntlrAnalysisTest {

    @Autowired
    private ParseOrchestrator parseOrchestrator;

    @Autowired
    private ParserWorkspace parserWorkspace;

    @Autowired
    private ParserSelection parserSelection;

    /** Windows 콘솔 한글 깨짐 방지: System.out 을 UTF-8 로 설정. */
    @BeforeAll
    static void setConsoleUtf8() {
        try {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        // 직전 run 의 analysis/ 정리 (파싱이 source 구조 미러로 새로 생성).
        Path analysisDir = parserWorkspace.analysisDir();
        if (Files.exists(analysisDir)) {
            deleteRecursively(analysisDir.toFile());
        }
    }

    /**
     * {@code data/source} 를 자동 감지·파싱해 {@code data/analysis} 에 JSON 이 생성되는지 검증.
     * 파싱 대상 파일이 없으면(테스트 데이터 미배치) 스킵한다.
     */
    @Test
    void parsesSourceDirIntoAnalysisJson() {
        Path sourceDir = parserWorkspace.sourceDir();
        if (!Files.exists(sourceDir)) {
            System.out.println("스킵: source 디렉토리 없음 — " + sourceDir);
            return;
        }
        if (parserSelection.detect(sourceDir).modulesByFile().isEmpty()) {
            System.out.println("스킵: 파싱 대상(지원 확장자) 파일 없음 — " + sourceDir);
            return;
        }

        List<String> streamMessages = new ArrayList<>();
        parseOrchestrator.parse(null, event -> {
            String message = String.format("[%s/%s] %s", event.type(), event.event(),
                    event.content() != null ? event.content() : "");
            streamMessages.add(message);
            System.out.println(message);
        });

        assertTrue(streamMessages.stream().anyMatch(m -> m.contains("파싱을 시작합니다")),
                "시작 메시지가 없습니다");
        assertTrue(streamMessages.stream().anyMatch(m -> m.contains("파싱 완료")),
                "완료 메시지가 없습니다");

        Path analysisDir = parserWorkspace.analysisDir();
        assertTrue(Files.exists(analysisDir), "analysis 디렉토리가 생성되지 않았습니다");

        long jsonCount = countNonEmptyJson(analysisDir);
        System.out.println("생성된 JSON 파일 수: " + jsonCount);
        assertTrue(jsonCount > 0, "생성된 JSON 파일이 없습니다");
    }

    private long countNonEmptyJson(Path analysisDir) {
        try {
            return Files.walk(analysisDir)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".json"))
                    .peek(p -> {
                        try {
                            assertFalse(Files.readString(p).isEmpty(), "분석 결과 파일이 비어있습니다: " + p);
                        } catch (Exception e) {
                            fail("파일 읽기 실패: " + p, e);
                        }
                    })
                    .count();
        } catch (Exception e) {
            fail("analysis 디렉토리 탐색 실패: " + analysisDir, e);
            return 0;
        }
    }

    /** 디렉터리 재귀 삭제. */
    private static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) deleteRecursively(child);
            }
        }
        file.delete();
    }
}
