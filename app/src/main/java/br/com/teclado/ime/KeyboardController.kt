package br.com.teclado.ime

class KeyboardController {
    var mode: KeyboardMode = KeyboardMode.LETTERS
        private set
    var shiftEnabled: Boolean = false
        private set
    var pendingAccent: Char? = null
        private set

    fun onAction(action: KeyboardAction): KeyboardController {
        when (action) {
            KeyboardAction.Shift -> shiftEnabled = !shiftEnabled
            KeyboardAction.Symbols -> { mode = KeyboardMode.SYMBOLS; shiftEnabled = false }
            KeyboardAction.Letters -> mode = KeyboardMode.LETTERS
            is KeyboardAction.Accent -> pendingAccent = action.value
            is KeyboardAction.Character -> if (shiftEnabled) shiftEnabled = false
            else -> Unit
        }
        return this
    }

    fun clearAccent() { pendingAccent = null }
    fun layout(): KeyboardLayout = if (mode == KeyboardMode.LETTERS) KeyboardLayout.letters() else KeyboardLayout.symbols()
}
