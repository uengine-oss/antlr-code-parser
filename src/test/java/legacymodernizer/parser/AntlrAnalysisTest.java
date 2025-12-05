package legacymodernizer.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import legacymodernizer.parser.controller.FileUploadController;
import legacymodernizer.parser.service.FileParserService;

@SpringBootTest
public class AntlrAnalysisTest {
    
    @Autowired
    private FileUploadController fileUploadController;
    
    @Autowired
    private FileParserService fileParserService;

    private MockHttpServletRequest mockRequest;
    
    @Value("${test.session:TestSession}")
    private String TEST_SESSION;
    
    @Value("${test.project:test}")
    private String TEST_PROJECT;
    
    @Value("${test.target:java}")
    private String TEST_TARGET;
    
    @Value("${test.system:sample}")
    private String TEST_SYSTEM;

    // ========================================
    // 테스트 설정
    // ========================================

    @BeforeEach
    void setUp() throws Exception {
        mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("Session-UUID", TEST_SESSION);
        
        String srcDir = fileParserService.getTargetDirectory(TEST_SESSION, TEST_PROJECT, null);
        File srcDirFile = new File(srcDir);
        if (!srcDirFile.exists()) {
            srcDirFile.mkdirs();
        }

        String analysisDir = fileParserService.getAnalysisDirectory(TEST_SESSION, TEST_PROJECT, null);
        File analysisDirFile = new File(analysisDir);
        if (analysisDirFile.exists()) {
            deleteRecursively(analysisDirFile);
        }
        System.out.println("Analysis 디렉토리 정리 완료: " + analysisDir);
    }

    // ========================================
    // 테스트 케이스
    // ========================================
    
    /**
     * 폴더 내 모든 파일 분석 테스트
     * - src 디렉터리의 파일들을 파싱
     * - 분석 결과 JSON 파일 생성 검증
     */
    @Test
    void testAnalysisWithExistingFiles() throws Exception {
        String srcDir = fileParserService.getTargetDirectory(TEST_SESSION, TEST_PROJECT, null);
        File srcDirFile = new File(srcDir);
        
        System.out.println("========================================");
        System.out.println("Target: " + TEST_TARGET);
        System.out.println("Project: " + TEST_PROJECT);
        System.out.println("Source Dir: " + srcDir);
        System.out.println("========================================");
        
        // src 디렉토리가 존재하는지 확인
        assertTrue(srcDirFile.exists(), "src 디렉토리가 존재하지 않습니다: " + srcDir);
        assertTrue(srcDirFile.isDirectory(), "src 경로가 디렉토리가 아닙니다: " + srcDir);
        
        // 시스템별 폴더 및 파일을 동적으로 탐지
        List<Map<String, Object>> systems = new ArrayList<>();
        Map<String, List<File>> systemToFiles = new HashMap<>();
        
        File[] systemDirs = srcDirFile.listFiles(File::isDirectory);
        assertNotNull(systemDirs, "시스템 폴더를 찾을 수 없습니다");
        assertTrue(systemDirs.length > 0, "시스템 폴더가 없습니다");
        
        // 각 폴더를 시스템명으로 인식 (확장자 무관, 모든 파일 대상)
        for (File systemDir : systemDirs) {
            String systemName = systemDir.getName();
            File[] files = systemDir.listFiles(File::isFile);
            
            if (files != null && files.length > 0) {
                List<File> fileList = new ArrayList<>();
                List<String> spList = new ArrayList<>();
                
                for (File file : files) {
                    fileList.add(file);
                    spList.add(file.getName());
                }
                
                Map<String, Object> system = new HashMap<>();
                system.put("name", systemName);
                system.put("sp", spList);
                systems.add(system);
                systemToFiles.put(systemName, fileList);
                
                System.out.println("시스템: " + systemName + " - " + files.length + "개 파일 발견");
            }
        }
        
        assertTrue(systems.size() > 0, "분석할 시스템이 없습니다");
        
        Map<String, Object> request = new HashMap<>();
        request.put("projectName", TEST_PROJECT);
        request.put("target", TEST_TARGET);
        request.put("systems", systems);

        System.out.println("\n파싱 시작...\n");
        
        ResponseEntity<Map<String, Object>> response = fileUploadController.analysisContext(request, mockRequest);

        assertEquals(200, response.getStatusCode().value(), "분석이 실패했습니다");
        assertTrue(response.getBody().containsKey("successFiles"), "successFiles가 없습니다");
        
        System.out.println("\n========================================");
        System.out.println("파싱 결과:");
        System.out.println("========================================");
        
        // 각 시스템별로 분석 결과 파일 검증
        int totalFiles = 0;
        for (Map<String, Object> system : systems) {
            String systemName = (String) system.get("name");
            List<File> files = systemToFiles.get(systemName);
            
            if (files != null) {
                String analysisDir = fileParserService.getAnalysisDirectory(TEST_SESSION, TEST_PROJECT, systemName);
                for (File file : files) {
                    String baseFileName = file.getName().substring(0, file.getName().lastIndexOf('.'));
                    Path found = findJsonRecursively(Paths.get(analysisDir), baseFileName + ".json");
                    assertNotNull(found, String.format("분석 결과 파일이 생성되지 않았습니다: system=%s, file=%s.json", systemName, baseFileName));
                    
                    String content = Files.readString(found);
                    assertFalse(content.isEmpty(), 
                        String.format("분석 결과 파일이 비어있습니다: %s", found));
                    
                    System.out.println(String.format("[%s] %s → %s (%d bytes)", 
                        systemName, file.getName(), found.getFileName(), content.length()));
                    totalFiles++;
                }
            }
        }
        
        System.out.println("========================================");
        System.out.println("총 " + totalFiles + "개 파일 파싱 완료!");
        System.out.println("========================================");
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

    /**
     * JSON 파일 재귀 검색
     */
    private static Path findJsonRecursively(Path root, String fileName) throws Exception {
        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().equalsIgnoreCase(fileName))
                .findFirst()
                .orElse(null);
        }
    }

}
