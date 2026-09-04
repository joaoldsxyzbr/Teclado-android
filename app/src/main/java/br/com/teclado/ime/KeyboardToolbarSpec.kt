package br.com.teclado.ime

enum class KeyboardToolbarAction {
    EMOJI,
    CLIPBOARD,
    UNDO,
    REDO,
    TEXT_EDIT,
    SETTINGS
}

enum class KeyboardToolbarDestination {
    EMOJI_PANEL,
    CLIPBOARD_PANEL,
    UNDO,
    REDO,
    TEXT_EDIT_PANEL,
    OPEN_SETTINGS
}

enum class KeyboardPanel {
    NONE,
    CLIPBOARD,
    EMOJI,
    TEXT_EDIT
}

enum class KeyboardEditorAction {
    LEFT,
    RIGHT,
    SELECT_ALL,
    CUT,
    COPY,
    PASTE
}

data class KeyboardToolbarItem(
    val action: KeyboardToolbarAction,
    val icon: String,
    val enabled: Boolean,
    val localOnly: Boolean
)

object KeyboardToolbarSpec {
    val items = listOf(
        KeyboardToolbarItem(KeyboardToolbarAction.EMOJI, "☺", enabled = true, localOnly = true),
        KeyboardToolbarItem(KeyboardToolbarAction.CLIPBOARD, "▤", enabled = true, localOnly = true),
        KeyboardToolbarItem(KeyboardToolbarAction.UNDO, "↶", enabled = true, localOnly = true),
        KeyboardToolbarItem(KeyboardToolbarAction.REDO, "↷", enabled = true, localOnly = true),
        KeyboardToolbarItem(KeyboardToolbarAction.TEXT_EDIT, "↔", enabled = true, localOnly = true),
        KeyboardToolbarItem(KeyboardToolbarAction.SETTINGS, "⚙", enabled = true, localOnly = true)
    )

    fun destination(action: KeyboardToolbarAction): KeyboardToolbarDestination = when (action) {
        KeyboardToolbarAction.EMOJI -> KeyboardToolbarDestination.EMOJI_PANEL
        KeyboardToolbarAction.CLIPBOARD -> KeyboardToolbarDestination.CLIPBOARD_PANEL
        KeyboardToolbarAction.UNDO -> KeyboardToolbarDestination.UNDO
        KeyboardToolbarAction.REDO -> KeyboardToolbarDestination.REDO
        KeyboardToolbarAction.TEXT_EDIT -> KeyboardToolbarDestination.TEXT_EDIT_PANEL
        KeyboardToolbarAction.SETTINGS -> KeyboardToolbarDestination.OPEN_SETTINGS
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
