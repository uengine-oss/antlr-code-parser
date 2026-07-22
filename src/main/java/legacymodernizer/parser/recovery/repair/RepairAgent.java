package legacymodernizer.parser.recovery.repair;

public interface RepairAgent {

    boolean enabled();

    PatchProposal propose(FailureEnvelope envelope) throws RepairAgentException;

    static RepairAgent disabled() {
        return new RepairAgent() {
            @Override public boolean enabled() { return false; }
            @Override public PatchProposal propose(FailureEnvelope envelope) {
                throw new RepairAgentException("REPAIR_AGENT_DISABLED");
            }
        };
    }
}
