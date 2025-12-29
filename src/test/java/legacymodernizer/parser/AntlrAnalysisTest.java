package legacymodernizer.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import legacymodernizer.parser.service.FileParserService;
import legacymodernizer.parser.service.parsing.ParserStrategyFactory;
import legacymodernizer.parser.service.parsing.TargetParserStrategy;

@SpringBootTest
public class AntlrAnalysisTest {
    
    @Autowired
    private ParserStrategyFactory parserStrategyFactory;
    
    @Autowired
    private FileParserService fileParserService;
    
    @Value("${test.session:TestSession}")
    private String TEST_SESSION;
    
    @Value("${test.project:test}")
    private String TEST_PROJECT;
    
    @Value("${test.target:java}")
    private String TEST_TARGET;

    // ========================================
    // 테스트 설정
    // ========================================

    @BeforeEach
    void setUp() throws Exception {
        // 프로젝트 루트 생성
        Path projectRoot = fileParserService.projectRoot(TEST_SESSION, TEST_PROJECT);
        Files.createDirectories(projectRoot);

        // 기존 analysis 폴더 정리
        Path analysisDir = fileParserService.analysisDir(TEST_SESSION, TEST_PROJECT);
        if (Files.exists(analysisDir)) {
            deleteRecursively(analysisDir.toFile());
        }
        System.out.println("Analysis 디렉토리 정리 완료");
    }

    // ========================================
    // 테스트 케이스
    // ========================================
    
    /**
     * source/ 폴더 내 모든 파일 분석 테스트 (스트림 방식)
     * 
     * 저장 구조:
     * data/{session}/{project}/source/파일
     * data/{session}/{project}/analysis/파일.json
     */
    @Test
    void testAnalysisWithExistingFiles() throws Exception {
        Path projectRoot = fileParserService.projectRoot(TEST_SESSION, TEST_PROJECT);
        Path sourceDir = fileParserService.sourceDir(TEST_SESSION, TEST_PROJECT);
        
        System.out.println("========================================");
        System.out.println("Target: " + TEST_TARGET);
        System.out.println("Project: " + TEST_PROJECT);
        System.out.println("Project Dir: " + projectRoot);
        System.out.println("========================================");
        
        assertTrue(Files.exists(projectRoot), "프로젝트 디렉토리가 존재하지 않습니다: " + projectRoot);
        
        // source/ 폴더 확인
        if (!Files.exists(sourceDir)) {
            System.out.println("경고: source 디렉토리가 없습니다. 테스트 파일을 준비해주세요.");
            System.out.println("예상 경로: " + sourceDir);
            return;
        }
        
        // source/ 하위 모든 파일 찾기
        long fileCount = Files.walk(sourceDir)
                .filter(Files::isRegularFile)
                .count();
        
        if (fileCount == 0) {
            System.out.println("경고: source 디렉토리에 파일이 없습니다.");
            System.out.println("예상 경로: " + sourceDir);
            return;
        }
        
        System.out.println("발견된 파일 수: " + fileCount);

        System.out.println("\n파싱 시작 (스트림 방식)...\n");
        
        // Strategy 직접 호출 (스트림 방식)
        TargetParserStrategy strategy = parserStrategyFactory.getStrategy(TEST_TARGET);
        
        // 스트림 메시지 수집
        List<String> streamMessages = new ArrayList<>();
        
        strategy.parseWithStream(TEST_SESSION, TEST_PROJECT, (type, content) -> {
            String message = String.format("[%s] %s", type, content != null ? content : "");
            streamMessages.add(message);
            System.out.println(message);
        });
        
        System.out.println("\n========================================");
        System.out.println("스트림 메시지 수: " + streamMessages.size());
        System.out.println("========================================");
        
        // 스트림 메시지 검증
        assertTrue(streamMessages.size() > 0, "스트림 메시지가 없습니다");
        assertTrue(streamMessages.stream().anyMatch(m -> m.contains("파싱을 시작합니다")), 
                "시작 메시지가 없습니다");
        assertTrue(streamMessages.stream().anyMatch(m -> m.contains("파싱 완료")), 
                "완료 메시지가 없습니다");
        
        System.out.println("\n========================================");
        System.out.println("파싱 결과:");
        System.out.println("========================================");
        
        // analysis/ 폴더에서 생성된 JSON 파일 검증
        Path analysisDir = fileParserService.analysisDir(TEST_SESSION, TEST_PROJECT);
        assertTrue(Files.exists(analysisDir), "analysis 디렉토리가 생성되지 않았습니다");
        
        long jsonCount = Files.walk(analysisDir)
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".json"))
                .peek(p -> {
                    try {
                        String content = Files.readString(p);
                        assertFalse(content.isEmpty(), "분석 결과 파일이 비어있습니다: " + p);
                        Path relative = analysisDir.relativize(p);
                        System.out.println(String.format("  %s (%d bytes)", relative, content.length()));
                    } catch (Exception e) {
                        fail("파일 읽기 실패: " + p, e);
                    }
                })
                .count();
        
        System.out.println("========================================");
        System.out.println("총 " + jsonCount + "개 JSON 파일 생성 완료!");
        System.out.println("========================================");
        
        assertTrue(jsonCount > 0, "생성된 JSON 파일이 없습니다");
    }
    
    /**
     * 기존 방식 파싱 테스트 (비스트림)
     */
    @Test
    void testAnalysisWithoutStream() throws Exception {
        Path sourceDir = fileParserService.sourceDir(TEST_SESSION, TEST_PROJECT);
        
        if (!Files.exists(sourceDir)) {
            System.out.println("경고: source 디렉토리가 없습니다. 테스트 스킵.");
            return;
        }
        
        long fileCount = Files.walk(sourceDir)
                .filter(Files::isRegularFile)
                .count();
        
        if (fileCount == 0) {
            System.out.println("경고: source 디렉토리에 파일이 없습니다. 테스트 스킵.");
            return;
        }
        
        System.out.println("파싱 시작 (비스트림 방식)...");
        
        // Strategy 직접 호출 (비스트림 방식)
        TargetParserStrategy strategy = parserStrategyFactory.getStrategy(TEST_TARGET);
        strategy.parse(TEST_SESSION, TEST_PROJECT);
        
        System.out.println("파싱 완료!");
        
        // 결과 검증
        Path analysisDir = fileParserService.analysisDir(TEST_SESSION, TEST_PROJECT);
        assertTrue(Files.exists(analysisDir), "analysis 디렉토리가 생성되지 않았습니다");
    }

    // ========================================
    // 테스트 유틸리티
    // ========================================

    /**
     * 디렉터리 재귀 삭제
     */
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
