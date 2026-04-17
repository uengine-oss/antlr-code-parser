package legacymodernizer.parser.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 소스 파일 파싱 오케스트레이션 서비스
 *
 * source/ 하위 파일을 순회하며 파싱 함수를 실행하고,
 * 진행 상황을 StreamCallback으로 실시간 전달한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParsingOrchestrator {

    private final FileStorageService storageService;

    /**
     * 파싱 함수 인터페이스
     */
    @FunctionalInterface
    public interface SingleFileParser {
        void parse(File file, String outputPath, ParseProgressTracker tracker) throws Exception;
    }

    /**
     * source/ 하위 모든 파일을 파싱하며 진행 상황을 스트림으로 전달
     *
     * 파싱 결과는 analysis/target/에 저장
     */
    public void parseAllFiles(SingleFileParser parser, StreamCallback callback) {
        Path sourceBase = storageService.sourceDir();
        Path analysisBase = storageService.analysisTargetDir();

        if (!Files.exists(sourceBase)) {
            callback.error("소스 디렉토리 없음: " + sourceBase);
            throw new RuntimeException("소스 디렉토리 없음: " + sourceBase);
        }

        try {
            List<Path> files = Files.walk(sourceBase)
                    .filter(Files::isRegularFile)
                    .toList();

            int totalFiles = files.size();
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);
            AtomicInteger totalLines = new AtomicInteger(0);

            callback.message(String.format("🚀 파싱을 시작합니다. (총 %d개 파일)", totalFiles));

            for (int i = 0; i < files.size(); i++) {
                Path file = files.get(i);
                parseSingleFile(file, sourceBase, analysisBase, parser, callback,
                        i + 1, totalFiles, successCount, errorCount, totalLines);
            }

            if (errorCount.get() > 0) {
                callback.message(String.format("⚠️ 파싱 완료 (일부 에러). 성공: %d개, 실패: %d개, 총 %,d라인",
                        successCount.get(), errorCount.get(), totalLines.get()));
            } else {
                callback.message(String.format("🎉 파싱 완료! 총 %d개 파일, %,d라인 처리됨",
                        successCount.get(), totalLines.get()));
            }

        } catch (IOException e) {
            callback.error("디렉토리 탐색 실패: " + sourceBase);
            throw new RuntimeException("디렉토리 탐색 실패: " + sourceBase, e);
        }
    }

    private void parseSingleFile(Path file, Path sourceBase, Path analysisBase,
                                  SingleFileParser parser, StreamCallback callback,
                                  int fileIndex, int totalFiles,
                                  AtomicInteger successCount, AtomicInteger errorCount,
                                  AtomicInteger totalLines) {
        Path relative = sourceBase.relativize(file);
        String fileName = relative.toString();

        try {
            int lineCount = countLines(file);

            String relStr = relative.toString();
            int dot = relStr.lastIndexOf('.');
            String jsonPath = (dot > 0 ? relStr.substring(0, dot) : relStr) + ".json";
            Path output = analysisBase.resolve(jsonPath);
            Files.createDirectories(output.getParent());

            ParseProgressTracker tracker = new ParseProgressTracker(callback, fileName);

            callback.message(String.format("📄 [%d/%d] %s 파싱 시작... (%,d라인)",
                    fileIndex, totalFiles, fileName, lineCount));

            parser.parse(file.toFile(), output.toString(), tracker);

            callback.message(String.format("✅ [%d/%d] %s 완료 (%,d라인)",
                    fileIndex, totalFiles, fileName, lineCount));

            successCount.incrementAndGet();
            totalLines.addAndGet(lineCount);
            log.info("  [PARSED] {}", relative);

        } catch (Throwable e) {
            errorCount.incrementAndGet();
            callback.error(String.format("❌ [%d/%d] %s 파싱 실패: %s",
                    fileIndex, totalFiles, fileName, e.getMessage()));
            log.error("  [PARSE ERROR] {} - {}", relative, e.getMessage(), e);
        }
    }

    private int countLines(Path file) {
        try {
            return (int) Files.lines(file, StandardCharsets.UTF_8).count();
        } catch (Exception e) {
            try {
                return (int) Files.lines(file, Charset.forName("EUC-KR")).count();
            } catch (Exception e2) {
                return 0;
            }
        }
    }
}
