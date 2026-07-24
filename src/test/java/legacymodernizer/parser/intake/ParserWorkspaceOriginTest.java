package legacymodernizer.parser.intake;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ParserWorkspaceOriginTest {

    @TempDir
    Path externalRoot;

    @Test
    void remembersExternalOriginForVerifiedWriteBack() throws Exception {
        Path original = externalRoot.resolve("nested/Sample.java");
        Files.createDirectories(original.getParent());
        Files.writeString(original, "class Sample {}\n");
        ParserWorkspace workspace = new ParserWorkspace(new SourceIntakeClassifier());

        workspace.intakeFromPath(externalRoot);

        Path workspaceFile = workspace.sourceDir().resolve("nested/Sample.java");
        assertEquals(original.toAbsolutePath().normalize(),
                workspace.sourceOrigin(workspaceFile));
        Path uploadedStyle = workspace.sourceDir().resolve("unmapped.java");
        assertEquals(uploadedStyle.toAbsolutePath().normalize(),
                workspace.sourceOrigin(uploadedStyle));
    }
}
