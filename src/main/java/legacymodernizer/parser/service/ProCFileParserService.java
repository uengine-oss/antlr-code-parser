package legacymodernizer.parser.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import legacymodernizer.parser.antlr.CaseChangingCharStream;
import legacymodernizer.parser.antlr.CustomProCListener;
import legacymodernizer.parser.antlr.ProCLexer;
import legacymodernizer.parser.antlr.ProCParser;

@Service
public class ProCFileParserService {

    private static final String PROC_DIR = System.getenv("PROC_DIR") != null ? System.getenv("PROC_DIR") : "/Users/jhyg/Desktop/legacy-modernizer/Antlr-Server/proc";
    private static final String ANALYSIS_DIR = System.getenv("ANALYSIS_DIR") != null ? System.getenv("ANALYSIS_DIR") : "/Users/jhyg/Desktop/legacy-modernizer/Antlr-Server/analysis";

    public File parseAndSaveStructure(String fileName) throws IOException {
        System.out.println("\n분석시작\n");

        InputStream in = new FileInputStream(PROC_DIR + "\\" + fileName);
        CharStream s = CharStreams.fromStream(in);
    
        CaseChangingCharStream upper = new CaseChangingCharStream(s, true);
    
        ProCLexer lexer = new ProCLexer(upper);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
    
        ProCParser parser = new ProCParser(tokens);
        ParserRuleContext tree = parser.compoundStatement();
    
        ParseTreeWalker walker = new ParseTreeWalker();
        CustomProCListener listener = new CustomProCListener(tokens);
        walker.walk(listener, tree);
    
        // listener.printStructure();
        System.out.println("\n결과 : \n");

        System.out.println(listener.getRoot().toJson());
    
        String outputDir = ANALYSIS_DIR;
        File directory = new File(outputDir);
        if (!directory.exists()) {
            boolean dirsCreated = directory.mkdirs(); 
            if (!dirsCreated) {
                throw new IOException("Failed to create directory structure: " + outputDir);
            }
        }
    
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        String outputFilePath = outputDir + "\\" + baseName + ".json";
        File analysisFile = new File(outputFilePath);
        try (FileWriter file = new FileWriter(analysisFile)) {
            file.write(listener.getRoot().toJson());
            file.flush();
        }
    
        return analysisFile;
    }

    public Map<String, String> saveFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        // Windows 환경에서의 절대 경로 설정
        String baseDir = PROC_DIR;
        File directory = new File(baseDir);
        if (!directory.exists()) {
            boolean dirsCreated = directory.mkdirs(); // 디렉토리가 없으면 생성
            if (!dirsCreated) {
                throw new IOException("Failed to create directory structure: " + baseDir);
            }
        }
    
        String path = baseDir + "\\" + fileName; // Windows 경로 구분자
        File outputFile = new File(path);
    
        // 파일 저장
        file.transferTo(outputFile);
    
        String fileContent = Files.readString(outputFile.toPath(), StandardCharsets.UTF_8);
    
        Map<String, String> fileInfo = new HashMap<>();
        fileInfo.put("fileName", fileName);
        fileInfo.put("fileContent", fileContent);
        return fileInfo;
    }
}