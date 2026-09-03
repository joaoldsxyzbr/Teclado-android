package br.com.teclado.ime

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import br.com.teclado.R

class KeyboardToolbarRenderer(private val context: Context) {
    private val preferences = KeyboardPreferences(context)

    fun renderToolbar(
        container: LinearLayout,
        onAction: (KeyboardToolbarAction) -> Unit
    ) {
        container.removeAllViews()
        val height = context.resources.getDimensionPixelSize(R.dimen.toolbar_height)
        val iconSize = context.resources.getDimension(R.dimen.toolbar_icon_size)

        KeyboardToolbarSpec.items.forEach { item ->
            val button = TextView(context).apply {
                text = item.icon
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_PX, iconSize)
                setTextColor(context.getColor(R.color.key_text))
                isEnabled = item.enabled
                isClickable = item.enabled
                isFocusable = item.enabled
                alpha = if (item.enabled) 1f else 0.32f
                contentDescription = descriptionFor(item.action)
                if (item.enabled) {
                    setOnClickListener {
                        performHaptic(this)
                        onAction(item.action)
                    }
                }
            }
            container.addView(button, LinearLayout.LayoutParams(0, height, 1f))
        }
    }

    fun renderPanel(
        container: LinearLayout,
        panel: KeyboardPanel,
        clipboardText: String?,
        onPanelRequest: (KeyboardPanel) -> Unit,
        onCommitText: (String) -> Unit
    ) {
        container.removeAllViews()
        if (panel == KeyboardPanel.NONE) {
            container.visibility = View.GONE
            return
        }

        container.visibility = View.VISIBLE
        when (panel) {
            KeyboardPanel.TOOLS -> {
                val row = panelRow()
                addChip(row, context.getString(R.string.tools_emoji)) {
                    onPanelRequest(KeyboardPanel.EMOJI)
                }
                addChip(row, context.getString(R.string.tools_clipboard)) {
                    onPanelRequest(KeyboardPanel.CLIPBOARD)
                }
                container.addView(row)
            }

            KeyboardPanel.CLIPBOARD -> {
                val row = panelRow()
                if (clipboardText.isNullOrBlank()) {
                    addMessage(row, context.getString(R.string.clipboard_empty))
                } else {
                    addChip(row, clipboardPreview(clipboardText)) {
                        onCommitText(clipboardText)
                    }
                }
                container.addView(row)
            }

            KeyboardPanel.EMOJI -> {
                val scroll = HorizontalScrollView(context).apply {
                    isHorizontalScrollBarEnabled = false
                    overScrollMode = View.OVER_SCROLL_NEVER
                }
                val row = panelRow()
                EmojiCatalog.common.forEach { emoji ->
                    addEmoji(row, emoji) { onCommitText(emoji) }
                }
                scroll.addView(row)
                container.addView(
                    scroll,
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            }

            KeyboardPanel.NONE -> Unit
        }
    }

    private fun panelRow(): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun addChip(row: LinearLayout, label: String, onClick: () -> Unit) {
        val margin = context.resources.getDimensionPixelSize(R.dimen.key_margin)
        val height = context.resources.getDimensionPixelSize(R.dimen.panel_chip_height)
        val textSize = context.resources.getDimension(R.dimen.panel_text_size)
        val horizontalPadding = context.resources.getDimensionPixelSize(R.dimen.panel_chip_padding)
        val view = TextView(context).apply {
            text = label
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            setTextColor(context.getColor(R.color.key_text))
            background = context.getDrawable(R.drawable.key_background)
            setPadding(horizontalPadding, 0, horizontalPadding, 0)
            isClickable = true
            isFocusable = true
            setOnClickListener {
                performHaptic(this)
                onClick()
            }
        }
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height).apply {
            setMargins(margin, margin, margin, margin)
        }
        row.addView(view, params)
    }

    private fun addMessage(row: LinearLayout, message: String) {
        val textSize = context.resources.getDimension(R.dimen.panel_text_size)
        row.addView(
            TextView(context).apply {
                text = message
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
                setTextColor(context.getColor(R.color.key_text))
                alpha = 0.72f
            },
            LinearLayout.LayoutParams(0, context.resources.getDimensionPixelSize(R.dimen.panel_chip_height), 1f)
        )
    }

    private fun addEmoji(row: LinearLayout, emoji: String, onClick: () -> Unit) {
        val margin = context.resources.getDimensionPixelSize(R.dimen.key_margin)
        val size = context.resources.getDimensionPixelSize(R.dimen.panel_chip_height)
        val textSize = context.resources.getDimension(R.dimen.toolbar_icon_size)
        val view = TextView(context).apply {
            text = emoji
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            background = context.getDrawable(R.drawable.key_background)
            contentDescription = context.getString(R.string.emoji_description, emoji)
            isClickable = true
            isFocusable = true
            setOnClickListener {
                performHaptic(this)
                onClick()
            }
        }
        val params = LinearLayout.LayoutParams(size, size).apply {
            setMargins(margin, margin, margin, margin)
        }
        row.addView(view, params)
    }

    private fun clipboardPreview(value: String): String {
        val singleLine = value.replace('\n', ' ').replace('\r', ' ').trim()
        return if (singleLine.length <= 64) singleLine else singleLine.take(64) + "…"
    }

    private fun performHaptic(view: View) {
        if (preferences.hapticEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    private fun descriptionFor(action: KeyboardToolbarAction): String = when (action) {
        KeyboardToolbarAction.TOOLS -> context.getString(R.string.toolbar_tools)
        KeyboardToolbarAction.CLIPBOARD -> context.getString(R.string.toolbar_clipboard)
        KeyboardToolbarAction.TRANSLATE -> context.getString(R.string.toolbar_translate_disabled)
        KeyboardToolbarAction.SETTINGS -> context.getString(R.string.toolbar_settings)
        KeyboardToolbarAction.VOICE -> context.getString(R.string.toolbar_voice_disabled)
    }
}
