package br.com.teclado.ime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GboardStyleLayoutTest {
    @Test
    fun lettersStartWithPermanentNumberRow() {
        val labels = KeyboardLayout.letters().rows.first().map { it.label }

        assertEquals(listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"), labels)
    }

    @Test
    fun lettersDoNotUseDedicatedAccentKeys() {
        val actions = KeyboardLayout.letters().rows.flatten().map { it.action }

        assertFalse(actions.any { it is KeyboardAction.Accent })
    }

    @Test
    fun bottomRowIncludesLocalEmojiShortcut() {
        val bottomRow = KeyboardLayout.letters().rows.last()

        assertEquals(listOf("123", ",", "☺", "espaço", ".", "↵"), bottomRow.map { it.label })
        assertTrue(bottomRow.any { it.action == KeyboardAction.Emoji })
    }
}
