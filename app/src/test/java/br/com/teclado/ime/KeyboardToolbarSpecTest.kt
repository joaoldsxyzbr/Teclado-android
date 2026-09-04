package br.com.teclado.ime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardToolbarSpecTest {
    @Test
    fun toolbarUsesModernLocalActionOrder() {
        assertEquals(
            listOf(
                KeyboardToolbarAction.EMOJI,
                KeyboardToolbarAction.CLIPBOARD,
                KeyboardToolbarAction.UNDO,
                KeyboardToolbarAction.REDO,
                KeyboardToolbarAction.TEXT_EDIT,
                KeyboardToolbarAction.SETTINGS
            ),
            KeyboardToolbarSpec.items.map { it.action }
        )
    }

    @Test
    fun everyToolbarActionIsEnabledAndLocalOnly() {
        assertTrue(KeyboardToolbarSpec.items.all { it.enabled })
        assertTrue(KeyboardToolbarSpec.items.all { it.localOnly })
    }

    @Test
    fun toolbarRoutesToLocalEditorDestinations() {
        assertEquals(KeyboardToolbarDestination.EMOJI_PANEL, KeyboardToolbarSpec.destination(KeyboardToolbarAction.EMOJI))
        assertEquals(KeyboardToolbarDestination.CLIPBOARD_PANEL, KeyboardToolbarSpec.destination(KeyboardToolbarAction.CLIPBOARD))
        assertEquals(KeyboardToolbarDestination.UNDO, KeyboardToolbarSpec.destination(KeyboardToolbarAction.UNDO))
        assertEquals(KeyboardToolbarDestination.REDO, KeyboardToolbarSpec.destination(KeyboardToolbarAction.REDO))
        assertEquals(KeyboardToolbarDestination.TEXT_EDIT_PANEL, KeyboardToolbarSpec.destination(KeyboardToolbarAction.TEXT_EDIT))
        assertEquals(KeyboardToolbarDestination.OPEN_SETTINGS, KeyboardToolbarSpec.destination(KeyboardToolbarAction.SETTINGS))
    }

    @Test
    fun textEditPanelExposesCoreLocalEditorActions() {
        assertTrue(KeyboardPanel.entries.contains(KeyboardPanel.TEXT_EDIT))
        assertEquals(
            listOf(
                KeyboardEditorAction.LEFT,
                KeyboardEditorAction.RIGHT,
                KeyboardEditorAction.SELECT_ALL,
                KeyboardEditorAction.CUT,
                KeyboardEditorAction.COPY,
                KeyboardEditorAction.PASTE
            ),
            KeyboardEditorAction.entries
        )
    }

    @Test
    fun emojiCatalogIsBundledLocally() {
        assertTrue(EmojiCatalog.common.size >= 20)
        assertTrue(EmojiCatalog.common.contains("😀"))
        assertTrue(EmojiCatalog.common.contains("👍"))
        assertFalse(EmojiCatalog.common.any { it.isBlank() })
    }
}
