package br.com.teclado.ime

object CursorSelectionPolicy {
    fun targetPosition(
        extractedStartOffset: Int,
        selectionEnd: Int,
        extractedLength: Int,
        delta: Int
    ): Int {
        val min = extractedStartOffset
        val max = extractedStartOffset + extractedLength.coerceAtLeast(0)
        val current = extractedStartOffset + selectionEnd
        return (current + delta).coerceIn(min, max)
    }
}
