package br.com.teclado.ime

class AlternateSelectionTracker(
    private val optionCount: Int,
    private val optionWidthPx: Float,
    private val popupLeftPx: Float
) {
    init {
        require(optionCount > 0)
        require(optionWidthPx > 0f)
    }

    fun indexFor(rawX: Float): Int {
        val index = ((rawX - popupLeftPx) / optionWidthPx).toInt()
        return index.coerceIn(0, optionCount - 1)
    }
}
