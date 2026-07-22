package legacymodernizer.parser.recovery.localization;

import java.nio.charset.StandardCharsets;

import legacymodernizer.parser.recovery.workingcopy.Hashes;

/**
 * One contiguous, editable region of a failed unit chosen by the Parser. Patch offsets are
 * slice-relative; {@code unitStartOffset} re-anchors them onto the unit snapshot. The optional
 * {@code headerText} is read-only declaration context and is never a valid edit target.
 */
public record ContextSlice(
        SliceLevel level,
        String text,
        int unitStartOffset,
        int unitEndOffset,
        String headerText,
        String sliceSha256) {

    public static ContextSlice of(SliceLevel level, String unitText,
                                  int start, int end, String headerText) {
        String text = unitText.substring(start, end);
        return new ContextSlice(level, text, start, end,
                headerText == null ? "" : headerText,
                Hashes.sha256(text.getBytes(StandardCharsets.UTF_8)));
    }

    public int length() {
        return text.length();
    }

    public int toUnitOffset(int sliceOffset) {
        if (sliceOffset < 0 || sliceOffset > text.length()) {
            throw new IllegalArgumentException("SLICE_OFFSET_OUT_OF_RANGE");
        }
        return unitStartOffset + sliceOffset;
    }
}
