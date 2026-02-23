package legacymodernizer.parser.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * JSON 직렬화 유틸
 */
public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private JsonUtils() {
    }

    public static void write(Path path, Object value) throws IOException {
        Files.createDirectories(path.getParent());
        MAPPER.writeValue(path.toFile(), value);
    }
}
