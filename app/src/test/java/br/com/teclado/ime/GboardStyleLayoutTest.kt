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
    fun primaryLettersUseStandardQwertyWithoutCedillaKey() {
        val layout = KeyboardLayout.letters()
        val homeRow = layout.rows[2]

        assertEquals("asdfghjkl", homeRow.joinToString(separator = "") { it.label })
        assertFalse(layout.rows.flatten().any { it.label == "ç" })
    }

    @Test
    fun homeRowKeepsHalfKeyInsetOnBothSides() {
        val homeRow = KeyboardLayout.letters().rows[2]

        assertEquals("", homeRow.first().label)
        assertEquals(0.5, homeRow.first().weight.toDouble(), 0.001)
        assertEquals("", homeRow.last().label)
        assertEquals(0.5, homeRow.last().weight.toDouble(), 0.001)
        assertEquals(10.0, homeRow.sumOf { it.weight.toDouble() }, 0.001)
    }

    @Test
    fun bottomRowIncludesLocalEmojiShortcut() {
        val bottomRow = KeyboardLayout.letters().rows.last()

        assertEquals(listOf("123", ",", "☺", "espaço", ".", "↵"), bottomRow.map { it.label })
        assertTrue(bottomRow.any { it.action == KeyboardAction.Emoji })
    }
}
