package br.com.teclado.ime

import org.junit.Assert.assertEquals
import org.junit.Test

class CursorSelectionPolicyTest {
    @Test
    fun movesFromCurrentCaretWithinExtractedText() {
        assertEquals(
            17,
            CursorSelectionPolicy.targetPosition(
                extractedStartOffset = 10,
                selectionEnd = 5,
                extractedLength = 12,
                delta = 2
            )
        )
    }

    @Test
    fun clampsAtBeginningAndEndOfAvailableText() {
        assertEquals(
            10,
            CursorSelectionPolicy.targetPosition(
                extractedStartOffset = 10,
                selectionEnd = 1,
                extractedLength = 12,
                delta = -5
            )
        )
        assertEquals(
            22,
            CursorSelectionPolicy.targetPosition(
                extractedStartOffset = 10,
                selectionEnd = 11,
                extractedLength = 12,
                delta = 5
            )
        )
    }
}
