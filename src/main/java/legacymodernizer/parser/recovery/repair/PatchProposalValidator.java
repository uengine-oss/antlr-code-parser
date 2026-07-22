package legacymodernizer.parser.recovery.repair;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import legacymodernizer.parser.recovery.workingcopy.TextEdit;

public final class PatchProposalValidator {

    private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");

    public List<TextEdit> validate(FailureEnvelope envelope, PatchProposal proposal) {
        if (proposal == null || !"1.0.0".equals(proposal.schemaVersion())) {
            throw new IllegalArgumentException("AGENT_INVALID_SCHEMA_VERSION");
        }
        if (proposal.rationale() == null || proposal.rationale().isBlank()
                || proposal.rationale().length() > 2_048
                || proposal.confidence() < 0.0 || proposal.confidence() > 1.0) {
            throw new IllegalArgumentException("AGENT_PROPOSAL_METADATA_INVALID");
        }
        if (proposal.failureEnvelopeHash() == null
                || !SHA256.matcher(proposal.failureEnvelopeHash()).matches()
                || !proposal.failureEnvelopeHash().equals(envelope.failureEnvelopeHash())) {
            throw new IllegalArgumentException("AGENT_ENVELOPE_HASH_MISMATCH");
        }
        // Any declared ambiguity is an honest abstention (the schema allows empty edits for
        // it); it is never adopted, only recorded with its own reason.
        if (proposal.ambiguities() == null || !proposal.ambiguities().isEmpty()) {
            throw new IllegalArgumentException("AGENT_AMBIGUOUS_PROPOSAL");
        }
        if (proposal.edits() == null || proposal.edits().isEmpty() || proposal.edits().size() > 64) {
            throw new IllegalArgumentException("AGENT_EMPTY_OR_EXCESSIVE_EDITS");
        }
        int previousEnd = envelope.constraints().allowedStartOffset();
        int changedCharacters = 0;
        int changedLines = 0;
        List<TextEdit> validated = new ArrayList<>();
        for (AgentTextEdit edit : proposal.edits()) {
            if (edit == null || edit.startOffset() < previousEnd
                    || edit.endOffset() < edit.startOffset()
                    || edit.endOffset() > envelope.constraints().allowedEndOffset()) {
                throw new IllegalArgumentException("AGENT_EDIT_OUT_OF_RANGE_OR_OVERLAPPING");
            }
            if (edit.replacement() == null || edit.replacement().length() > 16_384
                    || edit.expectedText() == null || edit.expectedText().length() > 16_384
                    || edit.reason() == null || edit.reason().isBlank()
                    || edit.reason().length() > 512) {
                throw new IllegalArgumentException("AGENT_EDIT_CONTENT_INVALID");
            }
            int allowedSpan = envelope.constraints().allowedEndOffset()
                    - envelope.constraints().allowedStartOffset();
            int editSpan = edit.endOffset() - edit.startOffset();
            // A span covering (almost) the whole excerpt is a rewrite, not a repair —
            // [0, len-1] must not slip past an exact-bounds check (audit, 2026-07-22).
            if (allowedSpan > 0 && editSpan * 10 >= allowedSpan * 9) {
                throw new IllegalArgumentException("AGENT_FULL_EXCERPT_REWRITE_FORBIDDEN");
            }
            if (looksLikeAstPayload(edit.replacement())) {
                throw new IllegalArgumentException("AGENT_AST_PAYLOAD_FORBIDDEN");
            }
            int startOffset = edit.startOffset();
            int endOffset = edit.endOffset();
            String original = envelope.sourceExcerpt().substring(startOffset, endOffset);
            if (!original.equals(edit.expectedText())) {
                // Models are reliable at copying expectedText but weak at counting characters.
                // When the copied text occurs exactly once in the excerpt, the true position is
                // unambiguous and we re-anchor deterministically; otherwise reject as before.
                int unique = uniqueOccurrence(envelope.sourceExcerpt(), edit.expectedText());
                if (unique < 0 || unique < previousEnd
                        || unique + edit.expectedText().length()
                                > envelope.constraints().allowedEndOffset()) {
                    throw new IllegalArgumentException("AGENT_EXPECTED_TEXT_MISMATCH");
                }
                startOffset = unique;
                endOffset = unique + edit.expectedText().length();
                original = edit.expectedText();
            }
            if (original.equals(edit.replacement())) {
                throw new IllegalArgumentException("AGENT_NO_OP_EDIT");
            }
            changedCharacters += Math.max(original.length(), edit.replacement().length());
            changedLines += Math.max(lines(original), lines(edit.replacement()));
            // Excerpt-relative offsets are re-anchored onto the unit snapshot frame here, so
            // downstream working copies never need to know which slice the Agent saw.
            validated.add(new TextEdit(
                    envelope.sliceUnitStartOffset() + startOffset,
                    envelope.sliceUnitStartOffset() + endOffset,
                    edit.replacement(), "REPAIR_AGENT", edit.reason()));
            previousEnd = endOffset;
        }
        if (changedCharacters > envelope.constraints().maxChangedCharacters()) {
            throw new IllegalArgumentException("AGENT_CHANGED_CHARACTER_LIMIT_EXCEEDED");
        }
        if (changedLines > envelope.constraints().maxChangedLines()) {
            throw new IllegalArgumentException("AGENT_CHANGED_LINE_LIMIT_EXCEEDED");
        }
        return List.copyOf(validated);
    }

    /** Index of the only occurrence of {@code needle} in {@code haystack}, or -1. */
    private static int uniqueOccurrence(String haystack, String needle) {
        if (needle == null || needle.isEmpty()) return -1;
        int first = haystack.indexOf(needle);
        if (first < 0) return -1;
        return haystack.indexOf(needle, first + 1) < 0 ? first : -1;
    }

    private static int lines(String text) {
        return (int) text.chars().filter(character -> character == '\n').count() + 1;
    }

    private static boolean looksLikeAstPayload(String replacement) {
        String trimmed = replacement.stripLeading();
        if (!(trimmed.startsWith("{") || trimmed.startsWith("["))) return false;
        String lower = trimmed.toLowerCase(java.util.Locale.ROOT);
        return lower.contains("\"astjson\"")
                || (lower.contains("\"type\"")
                    && (lower.contains("\"children\"")
                        || lower.contains("\"startline\"")
                        || lower.contains("\"endline\"")));
    }
}
