package br.com.teclado.ime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardControllerTest {
    @Test fun shiftToggles() {
        val c = KeyboardController()
        c.onAction(KeyboardAction.Shift)
        assertTrue(c.shiftEnabled)
        c.onAction(KeyboardAction.Shift)
        assertFalse(c.shiftEnabled)
    }

    @Test fun switchesModes() {
        val c = KeyboardController()
        c.onAction(KeyboardAction.Symbols)
        assertEquals(KeyboardMode.SYMBOLS, c.mode)
        c.onAction(KeyboardAction.Letters)
        assertEquals(KeyboardMode.LETTERS, c.mode)
    }

    @Test fun letterAfterShiftResetsShift() {
        val c = KeyboardController()
        c.onAction(KeyboardAction.Shift)
        c.onAction(KeyboardAction.Character('a'))
        assertFalse(c.shiftEnabled)
    }
}
