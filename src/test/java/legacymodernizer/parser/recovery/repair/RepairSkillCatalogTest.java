package legacymodernizer.parser.recovery.repair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

class RepairSkillCatalogTest {

    @Test
    void assemblesCommonThenExactlyOneParserSelectedLanguageSkill() {
        RepairPromptAssembler assembler =
                new RepairPromptAssembler(new RepairSkillCatalog());

        String prompt = assembler.assemble("BASE CONTRACT", "oracle");

        assertTrue(prompt.startsWith("BASE CONTRACT"));
        assertTrue(prompt.contains("<repair_skills language_id=\"oracle\">"));
        assertTrue(prompt.contains("name=\"common-syntax-repair\""));
        assertTrue(prompt.contains("name=\"oracle-syntax-repair\""));
        assertFalse(prompt.contains("name=\"java-syntax-repair\""));
        assertFalse(prompt.contains("name=\"python-syntax-repair\""));
        assertFalse(prompt.contains("name=\"c-syntax-repair\""));
        assertFalse(prompt.contains("name=\"postgresql-syntax-repair\""));
        assertTrue(prompt.indexOf("name=\"common-syntax-repair\"")
                < prompt.indexOf("name=\"oracle-syntax-repair\""));
    }

    @Test
    void oracleSkillPermitsUniqueMissingBeginButForbidsGuessedMergeTarget() {
        List<RepairSkill> skills = new RepairSkillCatalog().forLanguage("oracle");
        String oracle = skills.get(1).body().replaceAll("\\s+", " ");

        assertTrue(oracle.contains(
                "Absence of `BEGIN` is not by itself a reason to abstain"));
        assertTrue(oracle.contains("insert only `BEGIN `"));
        assertTrue(oracle.contains("Never change schema, table, column"));
        assertTrue(oracle.contains("MERGE target"));
    }

    @Test
    void rejectsUnsafeMissingAndMalformedSkillsFailClosed() {
        RepairAgentException unsafe = assertThrows(RepairAgentException.class,
                () -> new RepairSkillCatalog().forLanguage("../oracle"));
        assertEquals("REPAIR_SKILL_LANGUAGE_ID_INVALID", unsafe.getMessage());

        RepairAgentException missing = assertThrows(RepairAgentException.class,
                () -> new RepairSkillCatalog().forLanguage("kotlin"));
        assertEquals("REPAIR_SKILL_MISSING:kotlin-syntax-repair", missing.getMessage());

        RepairSkillCatalog malformed = new RepairSkillCatalog(path -> {
            String text = path.contains("common-syntax-repair")
                    ? skill("common-syntax-repair", "common", "Use the common rule.")
                    : "---\nname: oracle-syntax-repair\nunexpected: value\n---\nBad";
            return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        });
        RepairAgentException invalid = assertThrows(RepairAgentException.class,
                () -> malformed.forLanguage("oracle"));
        assertEquals("REPAIR_SKILL_FRONTMATTER_INVALID:oracle-syntax-repair",
                invalid.getMessage());
    }

    private static String skill(String name, String description, String body) {
        return "---\nname: " + name + "\ndescription: " + description
                + "\n---\n\n" + body;
    }
}
