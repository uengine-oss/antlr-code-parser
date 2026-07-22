package legacymodernizer.parser.recovery.repair;

public interface RepairAgent {

    boolean enabled();

    PatchProposal propose(FailureEnvelope envelope) throws RepairAgentException;

    /** FR-025: prompt token count of the most recent proposal, null when unreported. */
    default Integer lastPromptTokens() {
        return null;
    }

    static RepairAgent disabled() {
        return new RepairAgent() {
            @Override public boolean enabled() { return false; }
            @Override public PatchProposal propose(FailureEnvelope envelope) {
                throw new RepairAgentException("REPAIR_AGENT_DISABLED");
            }
        };
    }
}
