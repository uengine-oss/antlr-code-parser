package legacymodernizer.parser.recovery.repair;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

final class RepairSkillCatalog {

    private static final String ROOT = "/recovery/skills/";
    private static final String COMMON = "common-syntax-repair";
    private static final Pattern SAFE_LANGUAGE_ID =
            Pattern.compile("[a-z][a-z0-9-]{0,63}");
    private static final Pattern SAFE_SKILL_NAME =
            Pattern.compile("[a-z0-9]+(?:-[a-z0-9]+)*");
    private static final int MAX_SKILL_BYTES = 32_768;
    private static final Set<String> REQUIRED_FIELDS = Set.of("name", "description");

    @FunctionalInterface
    interface ResourceLoader {
        InputStream open(String resourcePath);
    }

    private final ResourceLoader resourceLoader;
    private final Map<String, RepairSkill> cache = new ConcurrentHashMap<>();

    RepairSkillCatalog() {
        this(RepairSkillCatalog.class::getResourceAsStream);
    }

    RepairSkillCatalog(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    List<RepairSkill> forLanguage(String languageId) {
        String normalized = languageId == null ? "" : languageId.trim().toLowerCase();
        if (!SAFE_LANGUAGE_ID.matcher(normalized).matches()) {
            throw new RepairAgentException("REPAIR_SKILL_LANGUAGE_ID_INVALID");
        }
        return List.of(load(COMMON), load(normalized + "-syntax-repair"));
    }

    private RepairSkill load(String skillName) {
        if (!SAFE_SKILL_NAME.matcher(skillName).matches()) {
            throw new RepairAgentException("REPAIR_SKILL_NAME_INVALID");
        }
        return cache.computeIfAbsent(skillName, this::read);
    }

    private RepairSkill read(String skillName) {
        String resourcePath = ROOT + skillName + "/SKILL.md";
        try (InputStream input = resourceLoader.open(resourcePath)) {
            if (input == null) {
                throw new RepairAgentException("REPAIR_SKILL_MISSING:" + skillName);
            }
            byte[] bytes = input.readNBytes(MAX_SKILL_BYTES + 1);
            if (bytes.length > MAX_SKILL_BYTES) {
                throw new RepairAgentException("REPAIR_SKILL_TOO_LARGE:" + skillName);
            }
            return parse(skillName, resourcePath,
                    new String(bytes, StandardCharsets.UTF_8));
        } catch (RepairAgentException error) {
            throw error;
        } catch (IOException error) {
            throw new RepairAgentException("REPAIR_SKILL_UNREADABLE:" + skillName, error);
        }
    }

    private static RepairSkill parse(String expectedName, String resourcePath, String content) {
        String normalized = content == null ? "" : content.replace("\r\n", "\n");
        if (!normalized.startsWith("---\n")) {
            throw new RepairAgentException("REPAIR_SKILL_FRONTMATTER_MISSING:" + expectedName);
        }
        int end = normalized.indexOf("\n---\n", 4);
        if (end < 0) {
            throw new RepairAgentException("REPAIR_SKILL_FRONTMATTER_UNCLOSED:" + expectedName);
        }

        Map<String, String> metadata = new LinkedHashMap<>();
        for (String line : normalized.substring(4, end).split("\n")) {
            if (line.isBlank()) continue;
            int separator = line.indexOf(':');
            if (separator <= 0) {
                throw new RepairAgentException(
                        "REPAIR_SKILL_FRONTMATTER_INVALID:" + expectedName);
            }
            String key = line.substring(0, separator).trim();
            String value = unquote(line.substring(separator + 1).trim());
            if (!REQUIRED_FIELDS.contains(key) || value.isBlank()
                    || metadata.putIfAbsent(key, value) != null) {
                throw new RepairAgentException(
                        "REPAIR_SKILL_FRONTMATTER_INVALID:" + expectedName);
            }
        }

        String actualName = metadata.get("name");
        String description = metadata.get("description");
        String body = normalized.substring(end + 5).trim();
        if (!expectedName.equals(actualName)
                || description == null || description.length() > 1_024
                || body.isBlank() || body.contains("[TODO")) {
            throw new RepairAgentException("REPAIR_SKILL_CONTENT_INVALID:" + expectedName);
        }
        return new RepairSkill(actualName, description, body, resourcePath);
    }

    private static String unquote(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
