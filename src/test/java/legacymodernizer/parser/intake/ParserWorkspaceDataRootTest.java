package legacymodernizer.parser.intake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class ParserWorkspaceDataRootTest {

    @Test
    void serverUsesSharedWorkspaceRoot() {
        Path shared = Path.of("build", "shared-data").toAbsolutePath().normalize();

        String actual = ParserWorkspace.resolveBaseDir(
                null,
                shared.toString(),
                null,
                Path.of("parser-repo").toAbsolutePath().toString());

        assertEquals(shared.toString(), actual);
    }

    @Test
    void explicitTestRootRemainsHighestPriority() {
        Path testRoot = Path.of("build", "test-data").toAbsolutePath().normalize();
        Path shared = Path.of("build", "shared-data").toAbsolutePath().normalize();

        String actual = ParserWorkspace.resolveBaseDir(
                testRoot.toString(),
                shared.toString(),
                "docker-data",
                Path.of("parser-repo").toAbsolutePath().toString());

        assertEquals(testRoot.toString(), actual);
    }

    @Test
    void defaultStillUsesSiblingDataDirectory() {
        Path repository = Path.of("build", "project", "antlr-code-parser")
                .toAbsolutePath().normalize();
        Path expected = repository.getParent().resolve("data").normalize();

        String actual = ParserWorkspace.resolveBaseDir(
                null,
                null,
                null,
                repository.toString());

        assertEquals(expected.toString(), actual);
    }

    @Test
    void blankSharedRootFailsClosed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ParserWorkspace.resolveBaseDir(
                        null,
                        " ",
                        null,
                        Path.of("parser-repo").toAbsolutePath().toString()));
    }
}
