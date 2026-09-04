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
    private var activeAlternatePopup: AlternatePopup? = null

    fun render(
        container: LinearLayout,
        layout: KeyboardLayout,
        shiftEnabled: Boolean,
        onAction: (KeyboardAction) -> Unit,
        onCursorMove: (Int) -> Unit
    ) {
        activeAlternatePopup?.dismiss()
        activeAlternatePopup = null
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
                val action = key.action
                val params = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, key.weight).apply {
                    if (action == null) {
                        setMargins(0, margin, 0, margin)
                    } else {
                        setMargins(margin, margin, margin, margin)
                    }
                }

                if (action == null) {
                    rowView.addView(View(context), params)
                    return@forEach
                }

                val label = when {
                    action is KeyboardAction.Character && shiftEnabled && action.value.isLetter() -> key.label.uppercase()
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
                    setOnClickListener { onAction(action) }
                }

                attachTouchBehavior(
                    view = keyView,
                    action = action,
                    shiftEnabled = shiftEnabled,
                    keyHeight = height,
                    keyTextSize = keyTextSize,
                    onAction = onAction,
                    onCursorMove = onCursorMove
                )

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
        onAction: (KeyboardAction) -> Unit,
        onCursorMove: (Int) -> Unit
    ) {
        when (action) {
            KeyboardAction.Space -> attachSpacebarBehavior(view, onCursorMove)

            KeyboardAction.Backspace -> {
                val session = KeyTouchSession(
                    scheduler = touchScheduler,
                    repeatPolicy = BackspaceRepeatPolicy.policy,
                    onTap = {},
                    onRepeat = { onAction(KeyboardAction.Backspace) },
                    onLongPress = {},
                    onFeedback = { performHaptic(view) }
                )
                view.setOnTouchListener { _, event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> session.down()
                        MotionEvent.ACTION_UP -> session.up()
                        MotionEvent.ACTION_CANCEL -> session.cancel()
                    }
                    true
                }
            }

            is KeyboardAction.Character -> attachCharacterBehavior(
                view = view,
                action = action,
                shiftEnabled = shiftEnabled,
                keyHeight = keyHeight,
                keyTextSize = keyTextSize,
                onAction = onAction
            )

            else -> attachSimpleHaptic(view)
        }
    }

    private fun attachSpacebarBehavior(view: TextView, onCursorMove: (Int) -> Unit) {
        val stepPx = 24f * context.resources.displayMetrics.density
        val tracker = SpaceCursorTracker(stepPx)

        view.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    performHaptic(view)
                    tracker.down(event.x)
                }
                MotionEvent.ACTION_MOVE -> {
                    val steps = tracker.move(event.x)
                    if (steps != 0) onCursorMove(steps)
                }
                MotionEvent.ACTION_UP -> {
                    if (!tracker.up()) view.performClick()
                }
                MotionEvent.ACTION_CANCEL -> tracker.cancel()
            }
            true
        }
    }

    private fun attachCharacterBehavior(
        view: TextView,
        action: KeyboardAction.Character,
        shiftEnabled: Boolean,
        keyHeight: Int,
        keyTextSize: Float,
        onAction: (KeyboardAction) -> Unit
    ) {
        val displayedCharacter = if (shiftEnabled && action.value.isLetter()) {
            action.value.uppercaseChar()
        } else {
            action.value
        }
        val alternates = PortugueseAlternates.forCharacter(displayedCharacter)
        if (alternates.isEmpty()) {
            attachSimpleHaptic(view)
            return
        }

        var popup: AlternatePopup? = null
        val session = KeyTouchSession(
            scheduler = touchScheduler,
            longPressDelayMs = ViewConfiguration.getLongPressTimeout().toLong(),
            onTap = { view.performClick() },
            onRepeat = {},
            onLongPress = {
                popup = showAlternates(view, alternates, keyHeight, keyTextSize, onAction)
                activeAlternatePopup = popup
            },
            onFeedback = { performHaptic(view) }
        )

        view.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> session.down()
                MotionEvent.ACTION_MOVE -> {
                    val currentPopup = popup
                    if (currentPopup != null) {
                        currentPopup.update(event.rawX)
                    } else if (isOutside(view, event)) {
                        session.moveOutside()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    val currentPopup = popup
                    if (currentPopup != null) {
                        currentPopup.update(event.rawX)
                        currentPopup.commit()
                        if (activeAlternatePopup === currentPopup) activeAlternatePopup = null
                        popup = null
                        session.cancel()
                    } else {
                        session.up()
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    val currentPopup = popup
                    currentPopup?.dismiss()
                    if (activeAlternatePopup === currentPopup) activeAlternatePopup = null
                    popup = null
                    session.cancel()
                }
            }
            true
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
    ): AlternatePopup {
        val padding = (4 * context.resources.displayMetrics.density).toInt()
        val popupWidth = keyHeight * alternates.size + padding * 2
        val popupHeight = keyHeight + padding * 2
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(padding, padding, padding, padding)
        }
        val optionViews = alternates.map { character ->
            TextView(context).apply {
                text = character.toString()
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_PX, keyTextSize)
                setTextColor(context.getColor(R.color.key_text))
                background = context.getDrawable(R.drawable.key_background)
                isFocusable = false
            }.also { row.addView(it, LinearLayout.LayoutParams(keyHeight, keyHeight)) }
        }

        val popupWindow = PopupWindow(row, popupWidth, popupHeight, false).apply {
            isOutsideTouchable = false
            inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
            setBackgroundDrawable(ColorDrawable(context.getColor(R.color.keyboard_background)))
            elevation = 8 * context.resources.displayMetrics.density
        }
        val xOffset = (anchor.width - popupWidth) / 2
        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        val optionLeft = location[0] + xOffset + padding
        val tracker = AlternateSelectionTracker(
            optionCount = alternates.size,
            optionWidthPx = keyHeight.toFloat(),
            popupLeftPx = optionLeft.toFloat()
        )
        val controller = AlternatePopup(
            popupWindow = popupWindow,
            tracker = tracker,
            optionViews = optionViews,
            alternates = alternates,
            anchor = anchor,
            onAction = onAction
        )
        controller.select(0)
        popupWindow.showAsDropDown(anchor, xOffset, -(anchor.height + popupHeight))
        return controller
    }

    private inner class AlternatePopup(
        private val popupWindow: PopupWindow,
        private val tracker: AlternateSelectionTracker,
        private val optionViews: List<TextView>,
        private val alternates: List<Char>,
        private val anchor: View,
        private val onAction: (KeyboardAction) -> Unit
    ) {
        private var selectedIndex = 0

        fun update(rawX: Float) {
            select(tracker.indexFor(rawX))
        }

        fun select(index: Int) {
            selectedIndex = index.coerceIn(optionViews.indices)
            optionViews.forEachIndexed { optionIndex, view ->
                view.alpha = if (optionIndex == selectedIndex) 1f else 0.58f
            }
        }

        fun commit() {
            val character = alternates[selectedIndex]
            performHaptic(anchor)
            onAction(KeyboardAction.Character(character))
            dismiss()
        }

        fun dismiss() {
            popupWindow.dismiss()
        }
    }
}
