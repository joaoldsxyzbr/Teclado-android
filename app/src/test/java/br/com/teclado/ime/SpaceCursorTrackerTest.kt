package br.com.teclado.ime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpaceCursorTrackerTest {
    @Test
    fun movementBelowThresholdKeepsSpaceTap() {
        val tracker = SpaceCursorTracker(stepPx = 20f)

        tracker.down(100f)

        assertEquals(0, tracker.move(119f))
        assertFalse(tracker.up())
    }

    @Test
    fun rightDragEmitsIncrementalCursorSteps() {
        val tracker = SpaceCursorTracker(stepPx = 20f)

        tracker.down(100f)

        assertEquals(2, tracker.move(145f))
        assertEquals(0, tracker.move(159f))
        assertEquals(1, tracker.move(161f))
        assertTrue(tracker.up())
    }

    @Test
    fun leftDragEmitsNegativeCursorSteps() {
        val tracker = SpaceCursorTracker(stepPx = 20f)

        tracker.down(100f)

        assertEquals(-2, tracker.move(59f))
        assertTrue(tracker.up())
    }

    @Test
    fun cancelResetsActiveGesture() {
        val tracker = SpaceCursorTracker(stepPx = 20f)

        tracker.down(100f)
        assertEquals(1, tracker.move(125f))
        tracker.cancel()

        assertEquals(0, tracker.move(200f))
        assertFalse(tracker.up())
    }
}
