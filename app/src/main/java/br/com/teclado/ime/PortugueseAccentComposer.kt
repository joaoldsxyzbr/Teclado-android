package br.com.teclado.ime

object PortugueseAccentComposer {
    private val combinations = mapOf(
        '´' to mapOf('a' to 'á', 'e' to 'é', 'i' to 'í', 'o' to 'ó', 'u' to 'ú', 'A' to 'Á', 'E' to 'É', 'I' to 'Í', 'O' to 'Ó', 'U' to 'Ú'),
        '^' to mapOf('a' to 'â', 'e' to 'ê', 'o' to 'ô', 'A' to 'Â', 'E' to 'Ê', 'O' to 'Ô'),
        '~' to mapOf('a' to 'ã', 'o' to 'õ', 'A' to 'Ã', 'O' to 'Õ'),
        '`' to mapOf('a' to 'à', 'A' to 'À'),
        '¨' to mapOf('u' to 'ü', 'U' to 'Ü')
    )

    fun compose(accent: Char, char: Char): String =
        combinations[accent]?.get(char)?.toString() ?: "$accent$char"
}
