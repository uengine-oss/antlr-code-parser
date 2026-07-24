package legacymodernizer.parser.recovery.source;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import legacymodernizer.parser.parsing.SourceTextCodec;
import legacymodernizer.parser.recovery.evidence.RecoveryOutcome;
import legacymodernizer.parser.recovery.quality.QualityDecision;
import legacymodernizer.parser.recovery.quality.QualityStatus;
import legacymodernizer.parser.recovery.workingcopy.Hashes;

class VerifiedSourceRepairApplierTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void defaultsToNoWriteWhenDisabled() throws Exception {
        Path source = write("p.sql", "BEGIN NULL END;".getBytes(StandardCharsets.UTF_8));
        byte[] before = Files.readAllBytes(source);
        RecoveryOutcome recovery = accepted(before, "BEGIN NULL; END;");

        SourceApplicationResult result =
                new VerifiedSourceRepairApplier(false).apply(source, recovery);

        assertEquals(SourceApplicationStatus.DISABLED, result.status());
        assertArrayEquals(before, Files.readAllBytes(source));
    }

    @Test
    void appliesVerifiedRepairAtomicallyWithOriginalCharset() throws Exception {
        Charset ms949 = Charset.forName("MS949");
        String beforeText = "/* 뷁 */\r\nBEGIN NULL END;\r\n";
        String repairedText = "/* 뷁 */\r\nBEGIN NULL; END;\r\n";
        byte[] before = beforeText.getBytes(ms949);
        Path source = write("p.sql", before);
        String detectedCharset = SourceTextCodec.decode(before).charset();

        SourceApplicationResult result =
                new VerifiedSourceRepairApplier(true).apply(source,
                        accepted(before, repairedText));

        assertEquals(SourceApplicationStatus.APPLIED, result.status());
        assertEquals(detectedCharset, result.charset());
        assertArrayEquals(repairedText.getBytes(Charset.forName(detectedCharset)),
                Files.readAllBytes(source));
    }

    @Test
    void refusesStaleLossyPartialAndNoOpSources() throws Exception {
        VerifiedSourceRepairApplier applier = new VerifiedSourceRepairApplier(true);

        Path stale = write("stale.sql", "A".getBytes(StandardCharsets.UTF_8));
        RecoveryOutcome staleRecovery =
                accepted(Files.readAllBytes(stale), "B");
        Files.writeString(stale, "changed", StandardCharsets.UTF_8);
        assertEquals(SourceApplicationStatus.STALE_SOURCE,
                applier.apply(stale, staleRecovery).status());
        assertEquals("changed", Files.readString(stale, StandardCharsets.UTF_8));

        byte[] invalid = {(byte) 0x81};
        Path lossy = write("lossy.sql", invalid);
        assertEquals(SourceApplicationStatus.LOSSY_SOURCE,
                applier.apply(lossy, accepted(invalid, "x")).status());
        assertArrayEquals(invalid, Files.readAllBytes(lossy));

        Path partial = write("partial.sql", "A".getBytes(StandardCharsets.UTF_8));
        RecoveryOutcome partialRecovery = outcome(Files.readAllBytes(partial), "B",
                QualityStatus.PARTIAL, true);
        assertEquals(SourceApplicationStatus.NO_REPAIR,
                applier.apply(partial, partialRecovery).status());
        assertEquals("A", Files.readString(partial));

        Path noOp = write("noop.sql", "same".getBytes(StandardCharsets.UTF_8));
        assertEquals(SourceApplicationStatus.NO_CHANGE,
                applier.apply(noOp, accepted(Files.readAllBytes(noOp), "same")).status());
        assertEquals("same", Files.readString(noOp));
    }

    private Path write(String name, byte[] bytes) throws Exception {
        Path path = temporaryDirectory.resolve(name);
        Files.write(path, bytes);
        return path;
    }

    private static RecoveryOutcome accepted(byte[] original, String repaired) {
        return outcome(original, repaired, QualityStatus.RECOVERED_VALIDATED, true);
    }

    private static RecoveryOutcome outcome(byte[] original, String repaired,
            QualityStatus status, boolean accepted) {
        QualityDecision decision = new QualityDecision(status, accepted,
                List.of(0, 0, 0, 0, 0, 0, 0), List.of("TEST"));
        return new RecoveryOutcome("{}", decision, List.of(), 0, 1, 0,
                Hashes.sha256(original), repaired);
    }
}
