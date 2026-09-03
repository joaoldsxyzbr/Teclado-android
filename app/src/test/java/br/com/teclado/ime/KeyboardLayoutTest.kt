package br.com.teclado.ime

import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardLayoutTest {
    @Test fun lettersContainCorePtBrKeys() {
        val labels = KeyboardLayout.letters().rows.flatten().map { it.label }.toSet()
        listOf("q", "w", "e", "r", "t", "y", "ç", "⇧", "⌫", "123", "espaço", "↵").forEach {
            assertTrue("missing $it", labels.contains(it))
        }
    }

    @Test fun symbolsContainDigitsAndAbc() {
        val labels = KeyboardLayout.symbols().rows.flatten().map { it.label }.toSet()
        (0..9).forEach { assertTrue(labels.contains(it.toString())) }
        assertTrue(labels.contains("ABC"))
    }
}
