package br.com.teclado.ime

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import br.com.teclado.R

class KeyboardViewRenderer(private val context: Context) {
    private val touchScheduler = AndroidTouchScheduler()
    private val preferences = KeyboardPreferences(context)
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()

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

                attachTouchBehavior(
                    view = keyView,
                    action = key.action,
                    shiftEnabled = shiftEnabled,
                    keyHeight = height,
                    keyTextSize = keyTextSize,
                    onAction = onAction
                )

                val params = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, key.weight).apply {
                    setMargins(margin, margin, margin, margin)
                }
                rowView.addView(keyView, params)
            }
            container.addView(rowView)
        }
    }

    private fun attachTouchBehavior(
        view: TextView,
        action: KeyboardAction,
        shiftEnabled: Boolean,
        keyHeight: Int,
        keyTextSize: Float,
        onAction: (KeyboardAction) -> Unit
    ) {
        when (action) {
            KeyboardAction.Backspace -> {
                val session = KeyTouchSession(
                    scheduler = touchScheduler,
                    repeatPolicy = BackspaceRepeatPolicy.policy,
                    onTap = {},
                    onRepeat = { onAction(KeyboardAction.Backspace) },
                    onLongPress = {},
                    onFeedback = { performHaptic(view) }
                )
                view.setOnTouchListener { _, event -> handleSessionTouch(view, event, session) }
            }

            is KeyboardAction.Character -> {
                val displayedCharacter = if (shiftEnabled && action.value.isLetter()) {
                    action.value.uppercaseChar()
                } else {
                    action.value
                }
                val alternates = PortugueseAlternates.forCharacter(displayedCharacter)
                if (alternates.isEmpty()) {
                    attachSimpleHaptic(view)
                } else {
                    val session = KeyTouchSession(
                        scheduler = touchScheduler,
                        longPressDelayMs = ViewConfiguration.getLongPressTimeout().toLong(),
                        onTap = { view.performClick() },
                        onRepeat = {},
                        onLongPress = {
                            showAlternates(view, alternates, keyHeight, keyTextSize, onAction)
                        },
                        onFeedback = { performHaptic(view) }
                    )
                    view.setOnTouchListener { _, event -> handleSessionTouch(view, event, session) }
                }
            }

            else -> attachSimpleHaptic(view)
        }
    }

    private fun attachSimpleHaptic(view: View) {
        view.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                performHaptic(view)
            }
            false
        }
    }

    private fun handleSessionTouch(
        view: View,
        event: MotionEvent,
        session: KeyTouchSession
    ): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> session.down()
            MotionEvent.ACTION_MOVE -> if (isOutside(view, event)) session.moveOutside()
            MotionEvent.ACTION_UP -> session.up()
            MotionEvent.ACTION_CANCEL -> session.cancel()
        }
        return true
    }

    private fun isOutside(view: View, event: MotionEvent): Boolean =
        event.x < -touchSlop ||
            event.y < -touchSlop ||
            event.x > view.width + touchSlop ||
            event.y > view.height + touchSlop

    private fun performHaptic(view: View) {
        if (preferences.hapticEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    private fun showAlternates(
        anchor: TextView,
        alternates: List<Char>,
        keyHeight: Int,
        keyTextSize: Float,
        onAction: (KeyboardAction) -> Unit
    ) {
        val padding = (4 * context.resources.displayMetrics.density).toInt()
        val popupWidth = keyHeight * alternates.size + padding * 2
        val popupHeight = keyHeight + padding * 2
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(padding, padding, padding, padding)
        }

        lateinit var popup: PopupWindow
        alternates.forEach { character ->
            val option = TextView(context).apply {
                text = character.toString()
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_PX, keyTextSize)
                setTextColor(context.getColor(R.color.key_text))
                background = context.getDrawable(R.drawable.key_background)
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    performHaptic(this)
                    onAction(KeyboardAction.Character(character))
                    popup.dismiss()
                }
            }
            row.addView(option, LinearLayout.LayoutParams(keyHeight, keyHeight))
        }

        popup = PopupWindow(row, popupWidth, popupHeight, true).apply {
            isOutsideTouchable = true
            inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
            setBackgroundDrawable(ColorDrawable(context.getColor(R.color.keyboard_background)))
            elevation = 8 * context.resources.displayMetrics.density
        }
        popup.showAsDropDown(
            anchor,
            (anchor.width - popupWidth) / 2,
            -(anchor.height + popupHeight)
        )
    }
}
