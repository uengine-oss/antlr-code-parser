package legacymodernizer.parser.recovery.repair;

import java.util.List;

/**
 * Agent-facing repair request. Since schema 2.0.0 {@code sourceExcerpt} is the Parser-chosen
 * {@code ContextSlice} text — never the whole unit. Slice offsets carry three frames
 * (file/unit/excerpt) so a patch can be re-anchored and stale snapshots rejected.
 * {@code declarationHeader} is read-only context and not a valid edit target.
 */
public record FailureEnvelope(
        String schemaVersion,
        String failureEnvelopeHash,
        String language,
        String grammarRevision,
        String fileSha256,
        String unitSha256,
        String unitId,
        String unitKind,
        int originalStartOffset,
        int originalEndOffset,
        int originalStartLine,
        int originalEndLine,
        String sliceLevel,
        int sliceUnitStartOffset,
        int sliceUnitEndOffset,
        int sliceFileStartOffset,
        int sliceFileEndOffset,
        String sliceSha256,
        String declarationHeader,
        List<DiagnosticEvidence> diagnostics,
        CoverageEvidence coverage,
        List<PriorAttempt> priorAttempts,
        String contextHeader,
        String sourceExcerpt,
        List<Integer> sourceLineStartOffsets,
        List<SourceTokenEvidence> diagnosticWindowTokens,
        RepairConstraints constraints) {

    public FailureEnvelope withHash(String hash) {
        return new FailureEnvelope(schemaVersion, hash, language, grammarRevision, fileSha256,
                unitSha256, unitId, unitKind, originalStartOffset, originalEndOffset,
                originalStartLine, originalEndLine, sliceLevel, sliceUnitStartOffset,
                sliceUnitEndOffset, sliceFileStartOffset, sliceFileEndOffset, sliceSha256,
                declarationHeader, diagnostics, coverage, priorAttempts, contextHeader,
                sourceExcerpt, sourceLineStartOffsets, diagnosticWindowTokens, constraints);
    }
}
