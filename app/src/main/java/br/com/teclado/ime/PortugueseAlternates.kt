package br.com.teclado.ime

object PortugueseAlternates {
    private val alternates = mapOf(
        'a' to listOf('á', 'à', 'â', 'ã', 'ä'),
        'e' to listOf('é', 'è', 'ê', 'ë'),
        'i' to listOf('í', 'ì', 'î', 'ï'),
        'o' to listOf('ó', 'ò', 'ô', 'õ', 'ö'),
        'u' to listOf('ú', 'ù', 'û', 'ü'),
        'c' to listOf('ç')
    )

    fun forCharacter(value: Char): List<Char> {
        val values = alternates[value.lowercaseChar()].orEmpty()
        return if (value.isUpperCase()) values.map(Char::uppercaseChar) else values
    }
}
