package legacymodernizer.parser.parsing.languages;

import java.io.File;
import java.nio.file.Path;

import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.recovery.workingcopy.Hashes;

/**
 * 언어별 파싱 전략의 공통 베이스.
 *
 * <p>Each module implements file parsing plus its language id and parse extensions.
 * 업로드(저장)·전체 순회는 storage·orchestrator 영역으로 분리됨.
 */
public abstract class AntlrLanguageModuleSupport implements LanguageModule {

    protected final ParserWorkspace parserWorkspace;

    protected AntlrLanguageModuleSupport(ParserWorkspace parserWorkspace) {
        this.parserWorkspace = parserWorkspace;
    }

    /**
     * sourceDir 기준 상대 경로 계산 — 각 전략이 {@code listener.setFileInfo} 호출 시 사용.
     */
    protected String computeRelativePath(File file) {
        String fileName = file.getName();
        Path sourceDir = parserWorkspace.sourceDir();
        Path filePath = file.toPath();
        try {
            if (filePath.startsWith(sourceDir)) {
                return sourceDir.relativize(filePath).toString().replace('\\', '/');
            } else {
                return filePath.toString().replace('\\', '/');
            }
        } catch (IllegalArgumentException cannotRelativize) {
            // 서로 다른 루트(드라이브) 경로는 relativize 불가 — 파일명으로 폴백.
            return fileName;
        }
    }

    /** Machine-independent identity for every public full-file parse. */
    protected String evidenceSourceId(File file, byte[] sourceBytes) {
        Path sourceRoot = parserWorkspace.sourceDir().toAbsolutePath().normalize();
        Path candidate = file.toPath().toAbsolutePath().normalize();
        if (candidate.startsWith(sourceRoot)) {
            return sourceRoot.relativize(candidate).toString().replace('\\', '/');
        }
        return "unscoped/" + Hashes.sha256(sourceBytes) + "/" + file.getName();
    }
}
