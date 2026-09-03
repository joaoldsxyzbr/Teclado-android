package br.com.teclado.ime

import org.junit.Assert.assertEquals
import org.junit.Test

class PortugueseAccentComposerTest {
    @Test fun composesPortugueseAccents() {
        assertEquals("á", PortugueseAccentComposer.compose('´', 'a'))
        assertEquals("é", PortugueseAccentComposer.compose('´', 'e'))
        assertEquals("ô", PortugueseAccentComposer.compose('^', 'o'))
        assertEquals("ã", PortugueseAccentComposer.compose('~', 'a'))
        assertEquals("à", PortugueseAccentComposer.compose('`', 'a'))
        assertEquals("ü", PortugueseAccentComposer.compose('¨', 'u'))
    }

    @Test fun invalidCombinationPreservesInput() {
        assertEquals("^x", PortugueseAccentComposer.compose('^', 'x'))
    }
}
