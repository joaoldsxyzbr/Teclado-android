package br.com.teclado.ime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PortugueseAlternatesTest {
    @Test
    fun commonPortugueseLettersExposeExpectedAlternates() {
        assertEquals(listOf('á', 'à', 'â', 'ã', 'ä'), PortugueseAlternates.forCharacter('a'))
        assertEquals(listOf('é', 'è', 'ê', 'ë'), PortugueseAlternates.forCharacter('e'))
        assertEquals(listOf('í', 'ì', 'î', 'ï'), PortugueseAlternates.forCharacter('i'))
        assertEquals(listOf('ó', 'ò', 'ô', 'õ', 'ö'), PortugueseAlternates.forCharacter('o'))
        assertEquals(listOf('ú', 'ù', 'û', 'ü'), PortugueseAlternates.forCharacter('u'))
        assertEquals(listOf('ç'), PortugueseAlternates.forCharacter('c'))
    }

    @Test
    fun uppercaseInputReturnsUppercaseAlternates() {
        assertEquals(listOf('Á', 'À', 'Â', 'Ã', 'Ä'), PortugueseAlternates.forCharacter('A'))
        assertEquals(listOf('Ç'), PortugueseAlternates.forCharacter('C'))
    }

    @Test
    fun lettersWithoutAlternatesReturnEmptyList() {
        assertTrue(PortugueseAlternates.forCharacter('b').isEmpty())
        assertTrue(PortugueseAlternates.forCharacter('z').isEmpty())
    }
}
