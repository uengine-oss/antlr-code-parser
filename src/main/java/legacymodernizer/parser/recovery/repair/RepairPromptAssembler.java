package legacymodernizer.parser.recovery.repair;

import java.util.List;

final class RepairPromptAssembler {

    private final RepairSkillCatalog catalog;

    RepairPromptAssembler(RepairSkillCatalog catalog) {
        this.catalog = catalog;
    }

    String assemble(String basePrompt, String languageId) {
        if (basePrompt == null || basePrompt.isBlank()) {
            throw new RepairAgentException("REPAIR_AGENT_PROMPT_MISSING");
        }
        List<RepairSkill> skills = catalog.forLanguage(languageId);
        String normalizedLanguage = languageId == null ? "" : languageId.trim().toLowerCase();
        StringBuilder prompt = new StringBuilder(basePrompt.strip());
        prompt.append("\n\n<repair_skills language_id=\"")
                .append(xml(normalizedLanguage))
                .append("\">\n");
        for (RepairSkill skill : skills) {
            prompt.append("<repair_skill name=\"").append(xml(skill.name()))
                    .append("\" description=\"").append(xml(skill.description()))
                    .append("\">\n")
                    .append(skill.body())
                    .append("\n</repair_skill>\n");
        }
        return prompt.append("</repair_skills>").toString();
    }

    private static String xml(String value) {
        return value.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
