package br.com.teclado.ime

object KeyboardRenderPolicy {
    fun shouldRenderAfter(action: KeyboardAction, shiftWasEnabled: Boolean): Boolean = when (action) {
        KeyboardAction.Shift,
        KeyboardAction.Symbols,
        KeyboardAction.Letters -> true

        is KeyboardAction.Character -> shiftWasEnabled
        else -> false
    }
}
