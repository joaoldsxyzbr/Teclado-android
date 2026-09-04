package br.com.teclado.ime

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.ExtractedTextRequest
import android.widget.LinearLayout
import br.com.teclado.MainActivity
import br.com.teclado.R
import kotlin.math.abs

class KeyboardService : InputMethodService() {
    private val controller = KeyboardController()
    private lateinit var renderer: KeyboardViewRenderer
    private lateinit var toolbarRenderer: KeyboardToolbarRenderer
    private var keyboardContainer: LinearLayout? = null
    private var toolbarContainer: LinearLayout? = null
    private var panelContainer: LinearLayout? = null
    private var activePanel: KeyboardPanel = KeyboardPanel.NONE

    override fun onCreateInputView(): View {
        val root = layoutInflater.inflate(R.layout.keyboard_view, null)
        keyboardContainer = root.findViewById(R.id.keyboard_container)
        toolbarContainer = root.findViewById(R.id.keyboard_toolbar)
        panelContainer = root.findViewById(R.id.keyboard_panel)
        renderer = KeyboardViewRenderer(this)
        toolbarRenderer = KeyboardToolbarRenderer(this)
        renderToolbar()
        renderPanel()
        renderKeyboard()
        return root
    }

    private fun renderKeyboard() {
        val target = keyboardContainer ?: return
        renderer.render(
            target,
            controller.layout(),
            controller.shiftEnabled,
            ::handleAction,
            ::moveCursor
        )
    }

    private fun renderToolbar() {
        val target = toolbarContainer ?: return
        toolbarRenderer.renderToolbar(target, ::handleToolbarAction)
    }

    private fun renderPanel() {
        val target = panelContainer ?: return
        toolbarRenderer.renderPanel(
            container = target,
            panel = activePanel,
            clipboardText = if (activePanel == KeyboardPanel.CLIPBOARD) currentClipboardText() else null,
            onCommitText = ::commitPanelText,
            onEditorAction = ::handleEditorAction
        )
    }

    private fun handleToolbarAction(action: KeyboardToolbarAction) {
        when (KeyboardToolbarSpec.destination(action)) {
            KeyboardToolbarDestination.EMOJI_PANEL -> togglePanel(KeyboardPanel.EMOJI)
            KeyboardToolbarDestination.CLIPBOARD_PANEL -> togglePanel(KeyboardPanel.CLIPBOARD)
            KeyboardToolbarDestination.UNDO -> {
                closePanel()
                performEditorContextAction(android.R.id.undo)
            }
            KeyboardToolbarDestination.REDO -> {
                closePanel()
                performEditorContextAction(android.R.id.redo)
            }
            KeyboardToolbarDestination.TEXT_EDIT_PANEL -> togglePanel(KeyboardPanel.TEXT_EDIT)
            KeyboardToolbarDestination.OPEN_SETTINGS -> openSettings()
        }
    }

    private fun handleEditorAction(action: KeyboardEditorAction) {
        when (action) {
            KeyboardEditorAction.LEFT -> moveCursor(-1)
            KeyboardEditorAction.RIGHT -> moveCursor(1)
            KeyboardEditorAction.SELECT_ALL -> performEditorContextAction(android.R.id.selectAll)
            KeyboardEditorAction.CUT -> performEditorContextAction(android.R.id.cut)
            KeyboardEditorAction.COPY -> performEditorContextAction(android.R.id.copy)
            KeyboardEditorAction.PASTE -> performEditorContextAction(android.R.id.paste)
        }
    }

    private fun performEditorContextAction(id: Int) {
        currentInputConnection?.performContextMenuAction(id)
    }

    private fun togglePanel(panel: KeyboardPanel) {
        activePanel = if (activePanel == panel) KeyboardPanel.NONE else panel
        renderPanel()
    }

    private fun closePanel() {
        if (activePanel != KeyboardPanel.NONE) {
            activePanel = KeyboardPanel.NONE
            renderPanel()
        }
    }

    private fun commitPanelText(text: String) {
        currentInputConnection?.commitText(text, 1)
        if (activePanel == KeyboardPanel.CLIPBOARD) {
            closePanel()
        }
    }

    private fun currentClipboardText(): String? {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip ?: return null
        if (clip.itemCount == 0) return null
        return clip.getItemAt(0).text?.toString()?.takeIf { it.isNotBlank() }
    }

    private fun openSettings() {
        requestHideSelf(0)
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun moveCursor(delta: Int) {
        if (delta == 0) return
        val connection = currentInputConnection ?: return
        val extracted = connection.getExtractedText(ExtractedTextRequest(), 0)
        val extractedText = extracted?.text

        if (extracted != null && extractedText != null) {
            val target = CursorSelectionPolicy.targetPosition(
                extractedStartOffset = extracted.startOffset,
                selectionEnd = extracted.selectionEnd,
                extractedLength = extractedText.length,
                delta = delta
            )
            connection.setSelection(target, target)
            return
        }

        val keyCode = if (delta < 0) KeyEvent.KEYCODE_DPAD_LEFT else KeyEvent.KEYCODE_DPAD_RIGHT
        repeat(abs(delta)) {
            connection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            connection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
        }
    }

    private fun handleAction(action: KeyboardAction) {
        if (action == KeyboardAction.Emoji) {
            togglePanel(KeyboardPanel.EMOJI)
            return
        }

        closePanel()
        val connection = currentInputConnection ?: return
        val shiftWasEnabled = controller.shiftEnabled

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
            KeyboardAction.Emoji -> Unit
        }

        if (KeyboardRenderPolicy.shouldRenderAfter(action, shiftWasEnabled)) {
            renderKeyboard()
        }
    }
}
