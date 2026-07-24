package legacymodernizer.parser.recovery.source;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import legacymodernizer.parser.parsing.SourceTextCodec;
import legacymodernizer.parser.recovery.evidence.RecoveryOutcome;
import legacymodernizer.parser.recovery.quality.QualityStatus;
import legacymodernizer.parser.recovery.workingcopy.Hashes;

@Component
public final class VerifiedSourceRepairApplier {

    private final boolean enabled;

    public VerifiedSourceRepairApplier() {
        this(booleanSetting("parser.repair.apply.to.source",
                "PARSER_REPAIR_APPLY_TO_SOURCE", false));
    }

    VerifiedSourceRepairApplier(boolean enabled) {
        this.enabled = enabled;
    }

    public SourceApplicationResult apply(Path sourceFile, RecoveryOutcome recovery)
            throws IOException {
        Path target = sourceFile.toAbsolutePath().normalize();
        if (!enabled) return result(SourceApplicationStatus.DISABLED, target, null, null, null);
        if (!eligible(recovery)) {
            return result(SourceApplicationStatus.NO_REPAIR, target, null, null, null);
        }

        byte[] before = Files.readAllBytes(target);
        String beforeHash = Hashes.sha256(before);
        if (!beforeHash.equals(recovery.originalFileSha256())) {
            return result(SourceApplicationStatus.STALE_SOURCE, target,
                    beforeHash, null, null);
        }

        SourceTextCodec.DecodedText decoded = SourceTextCodec.decode(before);
        if (decoded.lossy()) {
            return result(SourceApplicationStatus.LOSSY_SOURCE, target,
                    beforeHash, null, decoded.charset());
        }
        if (decoded.text().equals(recovery.repairedSource())) {
            return result(SourceApplicationStatus.NO_CHANGE, target,
                    beforeHash, beforeHash, decoded.charset());
        }

        byte[] repairedBytes = encode(recovery.repairedSource(), decoded.charset());
        String afterHash = Hashes.sha256(repairedBytes);
        Path parent = target.getParent();
        if (parent == null) throw new IOException("SOURCE_REPAIR_PARENT_MISSING");
        Path temporary = parent.resolve("." + target.getFileName()
                + ".repair-" + UUID.randomUUID() + ".tmp").normalize();
        if (!temporary.getParent().equals(parent)) {
            throw new IOException("SOURCE_REPAIR_TEMP_PATH_INVALID");
        }

        try {
            Files.write(temporary, repairedBytes, StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE);
            copyPosixPermissions(target, temporary);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
        return result(SourceApplicationStatus.APPLIED, target,
                beforeHash, afterHash, decoded.charset());
    }

    private static boolean eligible(RecoveryOutcome recovery) {
        if (recovery == null || !recovery.hasVerifiedSourceRepair()) return false;
        QualityStatus status = recovery.decision().status();
        return status == QualityStatus.RECOVERED_SAFE
                || status == QualityStatus.RECOVERED_VALIDATED;
    }

    private static byte[] encode(String text, String charsetName)
            throws CharacterCodingException {
        ByteBuffer buffer = Charset.forName(charsetName).newEncoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .encode(CharBuffer.wrap(text));
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    private static void copyPosixPermissions(Path source, Path target) {
        try {
            Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(source);
            Files.setPosixFilePermissions(target, permissions);
        } catch (IOException | UnsupportedOperationException ignored) {
            // Windows and non-POSIX filesystems do not expose this attribute view.
        }
    }

    private static SourceApplicationResult result(SourceApplicationStatus status, Path path,
            String beforeHash, String afterHash, String charset) {
        return new SourceApplicationResult(status, path, beforeHash, afterHash, charset);
    }

    private static boolean booleanSetting(String property, String environment, boolean fallback) {
        String propertyValue = System.getProperty(property);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return Boolean.parseBoolean(propertyValue.trim());
        }
        String environmentValue = System.getenv(environment);
        return environmentValue == null || environmentValue.isBlank()
                ? fallback : Boolean.parseBoolean(environmentValue.trim());
    }
}
