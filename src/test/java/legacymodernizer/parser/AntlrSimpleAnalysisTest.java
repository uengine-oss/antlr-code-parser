package legacymodernizer.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import legacymodernizer.parser.antlr.java.CustomJavaListener;
import legacymodernizer.parser.antlr.java.Java20Lexer;
import legacymodernizer.parser.antlr.java.Java20Parser;
import legacymodernizer.parser.service.FileParserService;

/**
 * 간소화 JSON 생성 테스트
 * - analysis/testanalysis 폴더에 type, name, startLine, endLine, children만 포함된 JSON 생성
 * - 기존 파싱 로직(analysis/target)에 영향 없음
 */
@SpringBootTest
public class AntlrSimpleAnalysisTest {
    
    @Autowired
    private FileParserService fileParserService;
    
    private Path testAnalysisDir;
    
    @BeforeEach
    void setUp() throws Exception {
        // analysis/testanalysis 폴더 경로
        testAnalysisDir = fileParserService.analysisDir().resolve("testanalysis");
        
        // 기존 testanalysis 폴더 정리
        if (Files.exists(testAnalysisDir)) {
            deleteRecursively(testAnalysisDir.toFile());
        }
        System.out.println("testanalysis 디렉토리 정리 완료: " + testAnalysisDir);
    }
    
    /**
     * source/ 폴더 내 모든 Java 파일을 파싱하여 간소화 JSON 생성
     * 결과: analysis/testanalysis/{상대경로}.json
     * 포맷: type, name, fileName, filePath, className, startLine, endLine, children
     */
    @Test
    void testSimpleAnalysis() throws Exception {
        Path sourceDir = fileParserService.sourceDir();
        
        System.out.println("========================================");
        System.out.println("간소화 JSON 생성 테스트");
        System.out.println("Source Dir: " + sourceDir);
        System.out.println("Output Dir: " + testAnalysisDir);
        System.out.println("========================================");
        
        if (!Files.exists(sourceDir)) {
            System.out.println("경고: source 디렉토리가 없습니다.");
            return;
        }
        
        // .java 파일만 찾기
        List<Path> javaFiles = Files.walk(sourceDir)
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".java"))
                .toList();
        
        if (javaFiles.isEmpty()) {
            System.out.println("경고: .java 파일이 없습니다.");
            return;
        }
        
        System.out.println("발견된 Java 파일 수: " + javaFiles.size());
        System.out.println();
        
        int successCount = 0;
        int errorCount = 0;
        
        for (Path javaFile : javaFiles) {
            Path relative = sourceDir.relativize(javaFile);
            String relStr = relative.toString();
            
            // 출력 경로: .java → .json
            int dot = relStr.lastIndexOf('.');
            String jsonPath = (dot > 0 ? relStr.substring(0, dot) : relStr) + ".json";
            Path outputPath = testAnalysisDir.resolve(jsonPath);
            
            try {
                // 파싱 + 간소화 JSON 저장
                parseAndSaveSimple(javaFile.toFile(), outputPath, sourceDir);
                
                String content = Files.readString(outputPath);
                System.out.println(String.format("  ✅ %s → %s (%d bytes)", relStr, jsonPath, content.length()));
                successCount++;
            } catch (Exception e) {
                System.out.println(String.format("  ❌ %s → 실패: %s", relStr, e.getMessage()));
                errorCount++;
            }
        }
        
        System.out.println();
        System.out.println("========================================");
        System.out.println(String.format("완료! 성공: %d개, 실패: %d개", successCount, errorCount));
        System.out.println("출력 폴더: " + testAnalysisDir);
        System.out.println("========================================");
        
        assertTrue(successCount > 0, "성공한 파일이 없습니다");
    }
    
    /**
     * Java 파일을 파싱하여 간소화 JSON으로 저장
     */
    private void parseAndSaveSimple(File file, Path outputPath, Path sourceDir) throws Exception {
        // 출력 디렉토리 생성
        Files.createDirectories(outputPath.getParent());
        
        try (InputStream in = new FileInputStream(file)) {
            CharStream charStream = CharStreams.fromStream(in);
            Java20Lexer lexer = new Java20Lexer(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            Java20Parser parser = new Java20Parser(tokens);
            
            Java20Parser.Start_Context tree = parser.start_();
            
            CustomJavaListener listener = new CustomJavaListener(tokens);
            
            // 파일 정보 설정
            String fileName = file.getName();
            Path filePath = file.toPath();
            String relativePath = fileName;
            try {
                if (filePath.startsWith(sourceDir)) {
                    relativePath = sourceDir.relativize(filePath).toString().replace('\\', '/');
                }
            } catch (Exception e) {
                // 무시
            }
            listener.setFileInfo(fileName, relativePath);
            
            new ParseTreeWalker().walk(listener, tree);
            
            // 간소화 JSON으로 저장
            try (FileWriter writer = new FileWriter(outputPath.toFile())) {
                writer.write(listener.getRoot().toSimpleJson());
            }
        }
    }
    
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

