package br.com.teclado.ime

class SpaceCursorTracker(private val stepPx: Float) {
    init {
        require(stepPx > 0f)
    }

    private var active = false
    private var lastEmittedX = 0f
    private var moved = false

    fun down(x: Float) {
        active = true
        lastEmittedX = x
        moved = false
    }

    fun move(x: Float): Int {
        if (!active) return 0

        val steps = ((x - lastEmittedX) / stepPx).toInt()
        if (steps != 0) {
            lastEmittedX += steps * stepPx
            moved = true
        }
        return steps
    }

    fun up(): Boolean {
        if (!active) return false
        val didMove = moved
        active = false
        moved = false
        return didMove
    }

    fun cancel() {
        active = false
        moved = false
    }
}
