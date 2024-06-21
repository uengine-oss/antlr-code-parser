// src/main/java/legacymodernizer/parser/controller/FileUploadController.java
package legacymodernizer.parser.controller;

import java.nio.file.Files;
import java.util.Map;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import legacymodernizer.parser.service.PlSqlFileParserService;

@RestController
public class FileUploadController {

    private final PlSqlFileParserService plSqlFileParserService;

    public FileUploadController(PlSqlFileParserService plSqlFileParserService) {
        this.plSqlFileParserService = plSqlFileParserService;
    }

    @PostMapping("/fileUpload")
    public ResponseEntity<Map<String, String>> FileUpload(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "No file uploaded"), HttpStatus.BAD_REQUEST);
        }

        try {
            Map<String, String> fileInfo = plSqlFileParserService.saveFile(file);
            System.out.println("\n저장완료\n");
            return new ResponseEntity<>(fileInfo, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Map.of("message", "Failed to upload and save file: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/analysis")
    public ResponseEntity<Resource> analysisContext(@RequestParam("fileName") String fileName) {
        // 환경 변수를 사용하여 경로 설정
        String analysisDir = System.getenv("ANALYSIS_DIR") != null ? System.getenv("ANALYSIS_DIR")
                : "C:\\Users\\roede\\Desktop\\uEngine\\Antlr-Server\\result\\analysis";

        try {
            String baseFileName = fileName.substring(0, fileName.lastIndexOf('.'));
            Path resultPath = Paths.get(analysisDir, baseFileName + ".json");

            if (Files.exists(resultPath)) {
                ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(resultPath));
                System.out.println("\n기존 파일 사용\n");
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(resource);
            }

            File analysisFile = plSqlFileParserService.parseAndSaveStructure(fileName);
            Path path = analysisFile.toPath();
            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
            System.out.println("\n분석완료\n");
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}