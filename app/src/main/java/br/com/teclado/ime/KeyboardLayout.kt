package br.com.teclado.ime

enum class KeyboardMode { LETTERS, SYMBOLS }

sealed interface KeyboardAction {
    data class Character(val value: Char) : KeyboardAction
    data object Shift : KeyboardAction
    data object Backspace : KeyboardAction
    data object Enter : KeyboardAction
    data object Space : KeyboardAction
    data object Symbols : KeyboardAction
    data object Letters : KeyboardAction
    data class Accent(val value: Char) : KeyboardAction
}

data class KeyboardKey(val label: String, val action: KeyboardAction, val weight: Float = 1f)
data class KeyboardLayout(val rows: List<List<KeyboardKey>>) {
    companion object {
        fun letters(): KeyboardLayout = KeyboardLayout(
            listOf(
                listOf('´', '~', '^', '`').map { KeyboardKey(it.toString(), KeyboardAction.Accent(it), 0.7f) },
                "qwertyuiop".map { KeyboardKey(it.toString(), KeyboardAction.Character(it)) },
                "asdfghjklç".map { KeyboardKey(it.toString(), KeyboardAction.Character(it)) },
                listOf(KeyboardKey("⇧", KeyboardAction.Shift, 1.35f)) +
                    "zxcvbnm".map { KeyboardKey(it.toString(), KeyboardAction.Character(it)) } +
                    KeyboardKey("⌫", KeyboardAction.Backspace, 1.35f),
                listOf(
                    KeyboardKey("123", KeyboardAction.Symbols, 1.25f),
                    KeyboardKey(",", KeyboardAction.Character(',')),
                    KeyboardKey("espaço", KeyboardAction.Space, 4f),
                    KeyboardKey(".", KeyboardAction.Character('.')),
                    KeyboardKey("↵", KeyboardAction.Enter, 1.25f)
                )
            )
        )

        fun symbols(): KeyboardLayout = KeyboardLayout(
            listOf(
                "1234567890".map { KeyboardKey(it.toString(), KeyboardAction.Character(it)) },
                listOf('@', '#', '$', '%', '&', '*', '-', '+', '(', ')').map { KeyboardKey(it.toString(), KeyboardAction.Character(it)) },
                listOf('!', '"', '\'', ':', ';', '/', '?', '_', '=', '°').map { KeyboardKey(it.toString(), KeyboardAction.Character(it)) },
                listOf(
                    KeyboardKey("ABC", KeyboardAction.Letters, 1.5f),
                    KeyboardKey(",", KeyboardAction.Character(',')),
                    KeyboardKey("espaço", KeyboardAction.Space, 4f),
                    KeyboardKey(".", KeyboardAction.Character('.')),
                    KeyboardKey("↵", KeyboardAction.Enter, 1.5f)
                )
            )
        )
    }
}
