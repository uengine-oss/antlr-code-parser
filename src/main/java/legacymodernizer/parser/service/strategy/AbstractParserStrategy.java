package legacymodernizer.parser.service.strategy;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import legacymodernizer.parser.service.FileStorageService;
import legacymodernizer.parser.service.ParsingOrchestrator;
import legacymodernizer.parser.service.StreamCallback;

/**
 * 모든 언어별 파싱 전략의 공통 베이스.
 *
 * upload()와 parseWithStream()은 모든 언어에서 동일하므로 여기서 구현하고,
 * 각 언어 전략은 parseFileWithStream()만 구현하면 된다.
 */
public abstract class AbstractParserStrategy implements TargetParserStrategy {

    protected final FileStorageService storageService;
    protected final ParsingOrchestrator orchestrator;

    protected AbstractParserStrategy(FileStorageService storageService, ParsingOrchestrator orchestrator) {
        this.storageService = storageService;
        this.orchestrator = orchestrator;
    }

    @Override
    public Map<String, Object> upload(MultipartFile[] files, String targetFolder) {
        return storageService.uploadFiles(files, getTargetExtensions(), targetFolder);
    }

    @Override
    public void parseWithStream(StreamCallback callback) {
        orchestrator.parseAllFiles(this::parseFileWithStream, callback);
    }

    /**
     * sourceDir 기준 상대 경로 계산.
     * 모든 언어 전략의 parseFileWithStream에서 listener.setFileInfo 호출 시 사용.
     */
    protected String computeRelativePath(File file) {
        String fileName = file.getName();
        Path sourceDir = storageService.sourceDir();
        Path filePath = file.toPath();
        try {
            if (filePath.startsWith(sourceDir)) {
                return sourceDir.relativize(filePath).toString().replace('\\', '/');
            } else {
                return filePath.toString().replace('\\', '/');
            }
        } catch (Exception e) {
            return fileName;
        }
    }
}
