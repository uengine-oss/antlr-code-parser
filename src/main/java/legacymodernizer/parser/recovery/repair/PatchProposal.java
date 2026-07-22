package legacymodernizer.parser.recovery.repair;

import java.util.List;

public record PatchProposal(
        String schemaVersion,
        String failureEnvelopeHash,
        List<AgentTextEdit> edits,
        String rationale,
        double confidence,
        List<String> ambiguities) {
}
