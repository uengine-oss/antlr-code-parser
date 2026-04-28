package legacymodernizer.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import legacymodernizer.parser.service.FileStorageService;
import legacymodernizer.parser.service.strategy.ParserStrategyFactory;
import legacymodernizer.parser.service.strategy.TargetParserStrategy;

@SpringBootTest
public class AntlrAnalysisTest {
    
    @Autowired
    private ParserStrategyFactory parserStrategyFactory;
    
    @Autowired
    private FileStorageService storageService;
    
    @Value("${test.target:c}")
    private String TEST_TARGET;

    // ========================================
    // 테스트 설정
    // ========================================

    /** Windows 콘솔 한글 깨짐 방지: System.out을 UTF-8로 설정 */
    @BeforeAll
    static void setConsoleUtf8() {
        try {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        // 기존 analysis/target 폴더만 정리
        Path targetDir = storageService.analysisTargetDir();
        if (Files.exists(targetDir)) {
            deleteRecursively(targetDir.toFile());
        }
        System.out.println("Analysis/target 디렉토리 정리 완료");
    }

    // ========================================
    // 테스트 케이스
    // ========================================
    
    /**
     * source/ 폴더 내 모든 파일 분석 테스트 (스트림 방식)
     * 
     * 저장 구조:
     * data/source/파일
     * data/analysis/파일.json
     */
    @Test
    void testAnalysisWithExistingFiles() throws Exception {
        Path sourceDir = storageService.sourceDir();
        
        System.out.println("========================================");
        System.out.println("Target: " + TEST_TARGET);
        System.out.println("Source Dir: " + sourceDir);
        System.out.println("========================================");
        
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
        
        strategy.parseWithStream((type, content) -> {
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
        
        // analysis/target 폴더에서 생성된 JSON 파일 검증
        Path targetDir = storageService.analysisTargetDir();
        assertTrue(Files.exists(targetDir), "analysis/target 디렉토리가 생성되지 않았습니다");
        
        long jsonCount = Files.walk(targetDir)
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".json"))
                .peek(p -> {
                    try {
                        String content = Files.readString(p);
                        assertFalse(content.isEmpty(), "분석 결과 파일이 비어있습니다: " + p);
                        Path relative = targetDir.relativize(p);
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
     * 특정 경로에 있는 프로젝트 파일들을 읽어서
     * 업로드(upload) 단계만 수행하는 테스트.
     *
     * - projectRoot: 외부/로컬 프로젝트 루트 (하드코딩)
     * - 이 경로 아래의 파일들을 MultipartFile로 감싸서
     *   TargetParserStrategy.upload(...)를 호출
     * - 실제 파싱(parse)은 하지 않고, data/source 등에 잘 적재되는지만 본다.
     */
    @Test
    void testUploadExternalProjectToDataSource() throws Exception {
        // 1) 하드코딩된 업로드 대상 경로 (필요시 변경해서 사용)**
        // 예: 기존 레거시 자바 프로젝트 루트 등
        // Path projectRoot = Paths.get("D:/문서/카카오톡 받은 파일/loan-ejb-app");
        // RWIS 테스트데이터를 data/source + data/ddl로 적재하기 위한 경로
        // - 이 경로 아래에 ddl/ 폴더와 RWIS/ (소스) 폴더가 존재
        // - ddl/ 아래 파일은 data/ddl로, 그 외 타겟 확장자(.sql 등)는 data/source로 적재됨
        Path projectRoot = Paths.get("C:/uEngine/robo/테스트데이터/apascrt00010t02_20260406");
        
        System.out.println("=== 업로드 대상 프로젝트 루트 ===");
        System.out.println("projectRoot: " + projectRoot);

        if (!Files.exists(projectRoot)) {
            System.out.println("경고: 업로드 대상 경로가 없습니다. 경로를 확인해주세요.");
            return;
        }

        // 2) 하위 모든 파일을 MultipartFile로 래핑
        List<MultipartFile> multipartFiles = new ArrayList<>();

        Files.walk(projectRoot)
                .filter(Files::isRegularFile)
                .forEach(p -> {
                    try {
                        String relativePath = projectRoot.relativize(p).toString().replace("\\", "/");
                        byte[] bytes = Files.readAllBytes(p);
                        MultipartFile mf = new MockMultipartFile(
                                "files",              // form field name
                                relativePath,         // original filename (상대 경로 유지)
                                "text/plain",         // content type (대략적인 값)
                                bytes
                        );
                        multipartFiles.add(mf);
                    } catch (Exception e) {
                        throw new RuntimeException("업로드 대상 파일 읽기 실패: " + p, e);
                    }
                });

        System.out.println("발견된 업로드 대상 파일 수: " + multipartFiles.size());
        if (multipartFiles.isEmpty()) {
            System.out.println("경고: 업로드 대상 파일이 없습니다.");
            return;
        }

        // 3) target 타입에 맞는 전략 가져오기 (예: java)
        TargetParserStrategy strategy = parserStrategyFactory.getStrategy(TEST_TARGET);

        // 4) upload 호출 → data/source, ddl 등에 저장
        MultipartFile[] array = multipartFiles.toArray(new MultipartFile[0]);
        System.out.println("업로드 호출 시작 (target=" + TEST_TARGET + ") ...");
        var result = strategy.upload(array, null);

        // 5) 결과 요약 출력 및 간단 검증
        @SuppressWarnings("unchecked")
        List<?> srcFiles = (List<?>) result.get("files");

        System.out.println("업로드된 타겟 파일 수: " + (srcFiles != null ? srcFiles.size() : 0));
        if (srcFiles != null) {
            assertTrue(srcFiles.size() > 0, "업로드된 타겟 파일이 없습니다");
        }

        // 실제 파일 시스템상의 data/source/ 도 한 번 확인
        Path sourceDir = storageService.sourceDir();
        long uploadedCount = Files.exists(sourceDir)
                ? Files.walk(sourceDir).filter(Files::isRegularFile).count()
                : 0;

        System.out.println("data/source 내 실제 파일 수: " + uploadedCount);
        assertTrue(uploadedCount > 0, "data/source 내 업로드된 파일이 없습니다");
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
