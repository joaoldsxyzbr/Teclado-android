package br.com.teclado.ime

enum class KeyboardToolbarAction {
    TOOLS,
    CLIPBOARD,
    TRANSLATE,
    SETTINGS,
    VOICE
}

enum class KeyboardToolbarDestination {
    TOOLS_PANEL,
    CLIPBOARD_PANEL,
    OPEN_SETTINGS,
    DISABLED
}

enum class KeyboardPanel {
    NONE,
    TOOLS,
    CLIPBOARD,
    EMOJI
}

data class KeyboardToolbarItem(
    val action: KeyboardToolbarAction,
    val icon: String,
    val enabled: Boolean,
    val localOnly: Boolean
)

object KeyboardToolbarSpec {
    val items = listOf(
        KeyboardToolbarItem(KeyboardToolbarAction.TOOLS, "▦", enabled = true, localOnly = true),
        KeyboardToolbarItem(KeyboardToolbarAction.CLIPBOARD, "▤", enabled = true, localOnly = true),
        KeyboardToolbarItem(KeyboardToolbarAction.TRANSLATE, "文", enabled = false, localOnly = false),
        KeyboardToolbarItem(KeyboardToolbarAction.SETTINGS, "⚙", enabled = true, localOnly = true),
        KeyboardToolbarItem(KeyboardToolbarAction.VOICE, "🎤", enabled = false, localOnly = false)
    )

    fun destination(action: KeyboardToolbarAction): KeyboardToolbarDestination = when (action) {
        KeyboardToolbarAction.TOOLS -> KeyboardToolbarDestination.TOOLS_PANEL
        KeyboardToolbarAction.CLIPBOARD -> KeyboardToolbarDestination.CLIPBOARD_PANEL
        KeyboardToolbarAction.SETTINGS -> KeyboardToolbarDestination.OPEN_SETTINGS
        KeyboardToolbarAction.TRANSLATE,
        KeyboardToolbarAction.VOICE -> KeyboardToolbarDestination.DISABLED
    }
}

object EmojiCatalog {
    val common = listOf(
        "😀", "😃", "😄", "😁", "😂", "🤣", "😊", "😍",
        "🥰", "😘", "😎", "🤔", "😅", "😢", "😭", "😡",
        "👍", "👎", "👏", "🙏", "💪", "👌", "✌️", "🤝",
        "❤️", "🔥", "✨", "🎉", "✅", "💯", "🚀", "🇧🇷"
    )
}
