package br.com.teclado.ime

import org.junit.Assert.assertEquals
import org.junit.Test

class AlternateSelectionTrackerTest {
    @Test
    fun mapsHorizontalPositionToAlternateIndex() {
        val tracker = AlternateSelectionTracker(
            optionCount = 5,
            optionWidthPx = 40f,
            popupLeftPx = 100f
        )

        assertEquals(0, tracker.indexFor(100f))
        assertEquals(1, tracker.indexFor(145f))
        assertEquals(2, tracker.indexFor(180f))
        assertEquals(4, tracker.indexFor(299f))
    }

    @Test
    fun clampsSelectionOutsidePopupEdges() {
        val tracker = AlternateSelectionTracker(
            optionCount = 5,
            optionWidthPx = 40f,
            popupLeftPx = 100f
        )

        assertEquals(0, tracker.indexFor(20f))
        assertEquals(4, tracker.indexFor(500f))
    }
}
