package br.com.teclado.ime

import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import br.com.teclado.R

class KeyboardViewRenderer(private val context: Context) {
    fun render(
        container: LinearLayout,
        layout: KeyboardLayout,
        shiftEnabled: Boolean,
        onAction: (KeyboardAction) -> Unit
    ) {
        container.removeAllViews()
        val margin = context.resources.getDimensionPixelSize(R.dimen.key_margin)
        val height = context.resources.getDimensionPixelSize(R.dimen.key_height)
        val keyTextSize = context.resources.getDimension(R.dimen.key_text_size)

        layout.rows.forEach { row ->
            val rowView = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
            }
            row.forEach { key ->
                val label = when {
                    key.action is KeyboardAction.Character && shiftEnabled && key.action.value.isLetter() -> key.label.uppercase()
                    else -> key.label
                }
                val keyView = TextView(context).apply {
                    text = label
                    gravity = Gravity.CENTER
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, keyTextSize)
                    setTextColor(context.getColor(R.color.key_text))
                    setTypeface(typeface, Typeface.NORMAL)
                    background = context.getDrawable(R.drawable.key_background)
                    isClickable = true
                    isFocusable = false
                    setOnClickListener { onAction(key.action) }
                }
                val params = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, key.weight).apply {
                    setMargins(margin, margin, margin, margin)
                }
                rowView.addView(keyView, params)
            }
            container.addView(rowView)
        }
    }
}
