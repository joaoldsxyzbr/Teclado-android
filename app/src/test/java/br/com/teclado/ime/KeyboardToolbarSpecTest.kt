package br.com.teclado.ime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardToolbarSpecTest {
    @Test
    fun toolbarKeepsReferenceOrder() {
        assertEquals(
            listOf(
                KeyboardToolbarAction.TOOLS,
                KeyboardToolbarAction.CLIPBOARD,
                KeyboardToolbarAction.TRANSLATE,
                KeyboardToolbarAction.SETTINGS,
                KeyboardToolbarAction.VOICE
            ),
            KeyboardToolbarSpec.items.map { it.action }
        )
    }

    @Test
    fun onlyPrivateLocalToolbarActionsAreEnabled() {
        val enabled = KeyboardToolbarSpec.items.filter { it.enabled }
        val disabled = KeyboardToolbarSpec.items.filterNot { it.enabled }.map { it.action }.toSet()

        assertTrue(enabled.isNotEmpty())
        assertTrue(enabled.all { it.localOnly })
        assertEquals(setOf(KeyboardToolbarAction.TRANSLATE, KeyboardToolbarAction.VOICE), disabled)
    }

    @Test
    fun toolbarRoutesOnlyLocalDestinations() {
        assertEquals(KeyboardToolbarDestination.TOOLS_PANEL, KeyboardToolbarSpec.destination(KeyboardToolbarAction.TOOLS))
        assertEquals(KeyboardToolbarDestination.CLIPBOARD_PANEL, KeyboardToolbarSpec.destination(KeyboardToolbarAction.CLIPBOARD))
        assertEquals(KeyboardToolbarDestination.OPEN_SETTINGS, KeyboardToolbarSpec.destination(KeyboardToolbarAction.SETTINGS))
        assertEquals(KeyboardToolbarDestination.DISABLED, KeyboardToolbarSpec.destination(KeyboardToolbarAction.TRANSLATE))
        assertEquals(KeyboardToolbarDestination.DISABLED, KeyboardToolbarSpec.destination(KeyboardToolbarAction.VOICE))
    }

    @Test
    fun emojiCatalogIsBundledLocally() {
        assertTrue(EmojiCatalog.common.size >= 20)
        assertTrue(EmojiCatalog.common.contains("😀"))
        assertTrue(EmojiCatalog.common.contains("👍"))
        assertFalse(EmojiCatalog.common.any { it.isBlank() })
    }
}
