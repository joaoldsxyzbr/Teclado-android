package br.com.teclado.ime

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardRenderPolicyTest {
    @Test
    fun regularCharacterDoesNotRequireFullRenderWhenShiftIsOff() {
        assertFalse(
            KeyboardRenderPolicy.shouldRenderAfter(
                KeyboardAction.Character('a'),
                shiftWasEnabled = false
            )
        )
    }

    @Test
    fun shiftedCharacterRequiresRenderToRestoreLowercaseLabels() {
        assertTrue(
            KeyboardRenderPolicy.shouldRenderAfter(
                KeyboardAction.Character('a'),
                shiftWasEnabled = true
            )
        )
    }

    @Test
    fun layoutChangingActionsRequireRender() {
        assertTrue(KeyboardRenderPolicy.shouldRenderAfter(KeyboardAction.Shift, false))
        assertTrue(KeyboardRenderPolicy.shouldRenderAfter(KeyboardAction.Symbols, false))
        assertTrue(KeyboardRenderPolicy.shouldRenderAfter(KeyboardAction.Letters, false))
    }

    @Test
    fun typingActionsDoNotRequireRender() {
        assertFalse(KeyboardRenderPolicy.shouldRenderAfter(KeyboardAction.Space, false))
        assertFalse(KeyboardRenderPolicy.shouldRenderAfter(KeyboardAction.Backspace, false))
        assertFalse(KeyboardRenderPolicy.shouldRenderAfter(KeyboardAction.Enter, false))
        assertFalse(KeyboardRenderPolicy.shouldRenderAfter(KeyboardAction.Accent('´'), false))
    }
}
