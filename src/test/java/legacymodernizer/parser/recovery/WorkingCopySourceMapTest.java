package legacymodernizer.parser.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import legacymodernizer.parser.recovery.workingcopy.TextEdit;
import legacymodernizer.parser.recovery.workingcopy.WorkingCopy;

class WorkingCopySourceMapTest {

    @Test
    void appliesBoundedOriginalRelativeEditsAndMapsLinesBack() {
        String original = "alpha\nbetaSET value = 1;\nomega\n";
        int split = original.indexOf("SET");
        WorkingCopy working = WorkingCopy.exact(original).applyOriginalEdits(List.of(
                new TextEdit(split, split, " ", "test.insert-space", "separate tokens")));

        assertEquals("alpha\nbeta SET value = 1;\nomega\n", working.workingText());
        assertNotEquals(working.originalSha256(), working.workingSha256());
        assertEquals(2, working.sourceMap().originalLine(2));
        assertEquals(3, working.sourceMap().originalLine(3));
        assertTrue(working.sourceMap().preservesLineCount());
        assertEquals("ORIGINAL_OFFSET", working.sourceMap().summary().mappingMode());
        assertEquals(4, working.sourceMap().summary().originalLineCount());
        assertEquals(4, working.sourceMap().summary().workingLineCount());
        assertTrue(working.unifiedDiff("sample.sql").contains("test.insert-space"));
        assertEquals(original, working.originalText());
    }

    @Test
    void rejectsOverlappingAndOutOfBoundsEdits() {
        WorkingCopy original = WorkingCopy.exact("abcdef");
        assertThrows(IllegalArgumentException.class, () -> original.applyOriginalEdits(List.of(
                new TextEdit(1, 4, "x", "a", ""),
                new TextEdit(3, 5, "y", "b", ""))));
        assertThrows(IllegalArgumentException.class, () -> original.applyOriginalEdits(List.of(
                new TextEdit(6, 7, "x", "a", ""))));
    }
}
