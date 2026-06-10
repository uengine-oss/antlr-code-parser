package legacymodernizer.parser.service.strategy;

import java.io.File;
import java.nio.file.Path;

import legacymodernizer.parser.service.FileStorageService;

/**
 * 언어별 파싱 전략의 공통 베이스.
 *
 * <p>각 전략은 {@code parseFileWithStream()} (파일 1개 파싱) 과 확장자/타입만 구현하면 된다.
 * 업로드(저장)·전체 순회는 storage·orchestrator 영역으로 분리됨.
 */
public abstract class AbstractParserStrategy implements TargetParserStrategy {

    protected final FileStorageService storageService;

    protected AbstractParserStrategy(FileStorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * sourceDir 기준 상대 경로 계산 — 각 전략이 {@code listener.setFileInfo} 호출 시 사용.
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
