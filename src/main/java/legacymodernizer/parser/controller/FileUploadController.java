package legacymodernizer.parser.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import legacymodernizer.parser.service.PlSqlFileParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:8080/", allowedHeaders = "*")
@RequiredArgsConstructor
public class FileUploadController {

    // 분석 결과 파일이 저장될 디렉토리 경로 설정
    private static final String ANALYSIS_DIR = System.getenv("ANALYSIS_DIR") != null ? System.getenv("ANALYSIS_DIR")
            : new File(System.getProperty("user.dir")).getParent() + File.separator + "analysis";
    

    // PLSQL 파일이 저장될 디렉토리 경로 설정
    private static final String PLSQL_DIR = System.getenv("PLSQL_DIR") != null ? System.getenv("PLSQL_DIR") : 
            new File(System.getProperty("user.dir")).getParent() + File.separator + "src";
        

    private final PlSqlFileParserService plSqlFileParserService;

    /**
     * 파일 업로드를 처리하는 엔드포인트
     * 
     * @param files 업로드된 파일 배열
     * @return 성공/실패한 파일 정보를 포함한 응답
     */
    @PostMapping("/fileUpload")
    public ResponseEntity<Map<String, Object>> fileUpload(@RequestParam("files") MultipartFile[] files) {

        // 빈 파일 체크
        if (files.length == 0) {
            System.out.println("빈 파일이 업로드됨");
            return ResponseEntity.badRequest().body(Map.of("message", "No files uploaded"));
        }

        // 성공/실패 파일 목록 관리
        List<Map<String, String>> successFiles = new ArrayList<>();
        List<Map<String, String>> failedFiles = new ArrayList<>();

        // 각 파일별 처리
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    // 파일 저장 및 정보 추출
                    Map<String, String> fileInfo = plSqlFileParserService.saveFile(file, PLSQL_DIR);
                    Map<String, String> fileData = new HashMap<>();
                    fileData.put("objectName", fileInfo.get("objectName"));
                    fileData.put("fileContent", fileInfo.get("fileContent"));
                    fileData.put("fileName", fileInfo.get("fileName"));
                    successFiles.add(fileData);
                    System.out.println("파일 저장 완료: " + file.getOriginalFilename() + " (오브젝트 이름: "
                            + fileInfo.get("objectName") + ")\n");

                } catch (Exception e) {
                    // 실패 정보 기록
                    System.out.println("파일 업로드 실패: " + file.getOriginalFilename());
                    e.printStackTrace();
                    Map<String, String> failedFileData = new HashMap<>();
                    failedFileData.put("fileName", file.getOriginalFilename());
                    failedFileData.put("error", "파일 업로드 및 저장 실패: " + e.getMessage());
                    failedFiles.add(failedFileData);
                }
            }
        }

        // 최종 응답 생성
        if (!failedFiles.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("successFiles", successFiles, "failedFiles", failedFiles));
        }
        return ResponseEntity.ok(Map.of("successFiles", successFiles));
    }

    /**
     * 업로드시 파일 분석을 처리하는 엔드포인트
     * 
     * @param request 분석할 파일 정보를 포함한 요청
     * @return 분석 결과 상태
     */
    @PostMapping("/analysis")
    public ResponseEntity<String> analysisContext(@RequestBody Map<String, List<Map<String, String>>> request) {

        // 분석 디렉토리 생성
        try {
            Path analysisPath = Paths.get(ANALYSIS_DIR);
            if (!Files.exists(analysisPath)) {
                Files.createDirectories(analysisPath);
                System.out.println("분석 디렉토리 생성됨: " + ANALYSIS_DIR);
            }
        } catch (IOException e) {
            System.out.println("분석 디렉토리 생성 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("분석 디렉토리 생성 실패");
        }

        // 분석할 파일 목록 추출
        List<Map<String, String>> fileNames = request.get("fileNames");
        System.out.println("분석 요청된 파일 정보들: " + fileNames);

        // 파일 목록 유효성 검사
        if (fileNames == null || fileNames.isEmpty()) {
            return ResponseEntity.badRequest().body("파일 정보가 없습니다");
        }

        // 각 파일별 분석 수행
        for (Map<String, String> fileInfo : fileNames) {
            try {
                String fileName = fileInfo.get("fileName");
                String baseFileName = fileName.substring(0, fileName.lastIndexOf('.'));
                Path resultPath = Paths.get(ANALYSIS_DIR, baseFileName + ".json");

                // 이미 분석된 파일인지 확인
                if (Files.exists(resultPath)) {
                    System.out.println("기존 분석 파일 사용: " + resultPath);
                } else {
                    // 새로운 분석 수행
                    plSqlFileParserService.parseAndSaveStructure(fileName, resultPath.toString(), PLSQL_DIR);
                    System.out.println("새로운 분석 완료: " + fileName);
                }
            } catch (Exception e) {
                System.out.println("분석 실패: " + fileInfo);
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("분석 실패");
            }
        }

        return ResponseEntity.ok("OK");
    }

    /**
     * 시작 버튼을 눌렀을 때 파일 분석을 처리하는 엔드포인트
     *
     * @return 분석된 파일 이름과 프로시저 이름이 하나의 요소로 구성된 리스트
     */
    @PostMapping("/start")
    public ResponseEntity<?> startAnalysisContext() {
        
        // 분석 디렉토리 생성
        try {
            Path analysisPath = Paths.get(ANALYSIS_DIR);
            if (!Files.exists(analysisPath)) {
                Files.createDirectories(analysisPath);
                System.out.println("분석 디렉토리 생성됨: " + ANALYSIS_DIR);
            }
        } catch (IOException e) {
            System.out.println("분석 디렉토리 생성 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("분석 디렉토리 생성 실패");
        }
    
        // PLSQL 디렉토리 확인
        File plsqlDirectory = new File(PLSQL_DIR);
        if (!plsqlDirectory.exists() || !plsqlDirectory.isDirectory()) {
            System.out.println("PLSQL 디렉토리를 찾을 수 없음: " + PLSQL_DIR);
            return ResponseEntity.badRequest().body("PLSQL 디렉토리를 찾을 수 없음");
        }
    
        // SQL 파일 목록 가져오기
        File[] allFiles = plsqlDirectory.listFiles();
        if (allFiles == null || allFiles.length == 0) {
            System.out.println("파일을 찾을 수 없음: " + PLSQL_DIR);
            return ResponseEntity.badRequest().body("파일을 찾을 수 없음");
        }
    
        System.out.println("분석 시작: 총 " + allFiles.length + "개의 파일");
        List<Map<String, String>> successFiles = new ArrayList<>();

        // 각 파일별 분석 수행
        for (File sqlFile : allFiles) {
            try {
                String fileName = sqlFile.getName();
                String fileContent = plSqlFileParserService.readFileContent(sqlFile);
                String objectName = plSqlFileParserService.extractSqlObjectName(fileContent);

                String baseFileName = fileName.substring(0, fileName.lastIndexOf('.'));
                Path resultPath = Paths.get(ANALYSIS_DIR, baseFileName + ".json");
    

                Map<String, String> fileData = new HashMap<>();
                fileData.put("fileName", fileName);
                fileData.put("objectName", objectName);
                fileData.put("fileContent", fileContent);
                successFiles.add(fileData);
    
                // 이미 분석된 파일인지 확인
                if (Files.exists(resultPath)) {
                    System.out.println("기존 분석 파일 사용: " + resultPath);
                } else {
                    // 새로운 분석 수행
                    plSqlFileParserService.parseAndSaveStructure(fileName, resultPath.toString(), PLSQL_DIR);
                    System.out.println("새로운 분석 완료: " + fileName);
                }
            } catch (Exception e) {
                System.out.println("분석 실패: " + sqlFile.getName());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("분석 실패");
            }
        }
    
        System.out.println("모든 파일 분석 완료");
        return ResponseEntity.ok(Map.of("successFiles", successFiles));
    }
}