package br.com.teclado.ime

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.widget.LinearLayout
import br.com.teclado.R

class KeyboardService : InputMethodService() {
    private val controller = KeyboardController()
    private lateinit var renderer: KeyboardViewRenderer
    private var container: LinearLayout? = null

    override fun onCreateInputView(): View {
        val root = layoutInflater.inflate(R.layout.keyboard_view, null)
        container = root.findViewById(R.id.keyboard_container)
        renderer = KeyboardViewRenderer(this)
        renderKeyboard()
        return root
    }

    private fun renderKeyboard() {
        val target = container ?: return
        renderer.render(target, controller.layout(), controller.shiftEnabled, ::handleAction)
    }

    private fun handleAction(action: KeyboardAction) {
        val connection = currentInputConnection ?: return
        when (action) {
            is KeyboardAction.Accent -> controller.onAction(action)
            is KeyboardAction.Character -> {
                val char = if (controller.shiftEnabled && action.value.isLetter()) action.value.uppercaseChar() else action.value
                val accent = controller.pendingAccent
                val text = if (accent != null) PortugueseAccentComposer.compose(accent, char) else char.toString()
                connection.commitText(text, 1)
                controller.clearAccent()
                controller.onAction(action)
            }
            KeyboardAction.Space -> {
                controller.pendingAccent?.let { connection.commitText(it.toString(), 1) }
                controller.clearAccent()
                connection.commitText(" ", 1)
            }
            KeyboardAction.Backspace -> {
                if (controller.pendingAccent != null) controller.clearAccent()
                else connection.deleteSurroundingText(1, 0)
            }
            KeyboardAction.Enter -> {
                controller.pendingAccent?.let { connection.commitText(it.toString(), 1) }
                controller.clearAccent()
                if (!sendDefaultEditorAction(true)) {
                    connection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                    connection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                }
            }
            KeyboardAction.Shift, KeyboardAction.Symbols, KeyboardAction.Letters -> controller.onAction(action)
        }
        renderKeyboard()
    }
}
