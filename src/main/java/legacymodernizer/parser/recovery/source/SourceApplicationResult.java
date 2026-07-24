package legacymodernizer.parser.recovery.source;

import java.nio.file.Path;

public record SourceApplicationResult(
        SourceApplicationStatus status,
        Path path,
        String beforeSha256,
        String afterSha256,
        String charset) {
}
