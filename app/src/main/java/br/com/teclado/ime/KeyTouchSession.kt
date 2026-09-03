package br.com.teclado.ime

fun interface ScheduledTouchTask {
    fun cancel()
}

interface TouchScheduler {
    fun schedule(delayMs: Long, block: () -> Unit): ScheduledTouchTask
}

data class KeyRepeatPolicy(
    val initialDelayMs: Long,
    val intervalMs: (repeatCount: Int) -> Long
)

object BackspaceRepeatPolicy {
    const val INITIAL_DELAY_MS = 400L

    fun intervalMs(repeatCount: Int): Long = when {
        repeatCount < 12 -> 80L
        repeatCount < 30 -> 55L
        else -> 40L
    }

    val policy = KeyRepeatPolicy(INITIAL_DELAY_MS, ::intervalMs)
}

class KeyTouchSession(
    private val scheduler: TouchScheduler,
    private val repeatPolicy: KeyRepeatPolicy? = null,
    private val longPressDelayMs: Long? = null,
    private val onTap: () -> Unit,
    private val onRepeat: () -> Unit,
    private val onLongPress: () -> Unit,
    private val onFeedback: () -> Unit
) {
    private var active = false
    private var longPressTriggered = false
    private var repeatCount = 0
    private var scheduledTask: ScheduledTouchTask? = null

    fun down() {
        cancelScheduledTask()
        active = true
        longPressTriggered = false
        repeatCount = 0
        onFeedback()

        val repeat = repeatPolicy
        when {
            repeat != null -> {
                onRepeat()
                scheduleRepeat(repeat.initialDelayMs)
            }
            longPressDelayMs != null -> scheduleLongPress(longPressDelayMs)
        }
    }

    fun up() {
        if (!active) return
        active = false
        cancelScheduledTask()
        if (repeatPolicy == null && !longPressTriggered) {
            onTap()
        }
    }

    fun cancel() {
        active = false
        cancelScheduledTask()
    }

    fun moveOutside() = cancel()

    private fun scheduleLongPress(delayMs: Long) {
        scheduledTask = scheduler.schedule(delayMs) {
            if (active) {
                longPressTriggered = true
                onLongPress()
            }
        }
    }

    private fun scheduleRepeat(delayMs: Long) {
        val repeat = repeatPolicy ?: return
        scheduledTask = scheduler.schedule(delayMs) {
            if (active) {
                onRepeat()
                repeatCount += 1
                scheduleRepeat(repeat.intervalMs(repeatCount))
            }
        }
    }

    private fun cancelScheduledTask() {
        scheduledTask?.cancel()
        scheduledTask = null
    }
}
