package br.com.teclado.ime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyTouchSessionTest {
    private class FakeScheduler : TouchScheduler {
        private data class Task(
            val delayMs: Long,
            val block: () -> Unit,
            var cancelled: Boolean = false
        )

        private val tasks = mutableListOf<Task>()

        override fun schedule(delayMs: Long, block: () -> Unit): ScheduledTouchTask {
            val task = Task(delayMs, block)
            tasks += task
            return ScheduledTouchTask { task.cancelled = true }
        }

        fun nextDelay(): Long? = tasks.firstOrNull { !it.cancelled }?.delayMs

        fun runNext() {
            val task = tasks.firstOrNull { !it.cancelled } ?: return
            tasks.remove(task)
            task.block()
        }

        fun runAll(limit: Int = 20) {
            repeat(limit) { runNext() }
        }

        fun hasPending(): Boolean = tasks.any { !it.cancelled }
    }

    @Test
    fun backspaceRepeatsImmediatelyThenStopsOnRelease() {
        val scheduler = FakeScheduler()
        var deletes = 0
        var feedback = 0
        val session = KeyTouchSession(
            scheduler = scheduler,
            repeatPolicy = BackspaceRepeatPolicy.policy,
            onTap = {},
            onRepeat = { deletes++ },
            onLongPress = {},
            onFeedback = { feedback++ }
        )

        session.down()

        assertEquals(1, deletes)
        assertEquals(1, feedback)
        assertEquals(BackspaceRepeatPolicy.INITIAL_DELAY_MS, scheduler.nextDelay())

        scheduler.runNext()
        assertEquals(2, deletes)
        assertEquals(BackspaceRepeatPolicy.intervalMs(1), scheduler.nextDelay())

        session.up()
        scheduler.runAll()

        assertEquals(2, deletes)
        assertFalse(scheduler.hasPending())
    }

    @Test
    fun movingOutsideCancelsBackspaceRepeat() {
        val scheduler = FakeScheduler()
        var deletes = 0
        val session = KeyTouchSession(
            scheduler = scheduler,
            repeatPolicy = BackspaceRepeatPolicy.policy,
            onTap = {},
            onRepeat = { deletes++ },
            onLongPress = {},
            onFeedback = {}
        )

        session.down()
        session.moveOutside()
        scheduler.runAll()

        assertEquals(1, deletes)
        assertFalse(scheduler.hasPending())
    }

    @Test
    fun shortPressTapsButLongPressOpensAlternatesAndSuppressesTap() {
        val shortScheduler = FakeScheduler()
        var shortTaps = 0
        var shortLongPresses = 0
        val shortSession = KeyTouchSession(
            scheduler = shortScheduler,
            longPressDelayMs = 500L,
            onTap = { shortTaps++ },
            onRepeat = {},
            onLongPress = { shortLongPresses++ },
            onFeedback = {}
        )

        shortSession.down()
        shortSession.up()
        shortScheduler.runAll()

        assertEquals(1, shortTaps)
        assertEquals(0, shortLongPresses)

        val longScheduler = FakeScheduler()
        var longTaps = 0
        var longPresses = 0
        val longSession = KeyTouchSession(
            scheduler = longScheduler,
            longPressDelayMs = 500L,
            onTap = { longTaps++ },
            onRepeat = {},
            onLongPress = { longPresses++ },
            onFeedback = {}
        )

        longSession.down()
        assertTrue(longScheduler.hasPending())
        longScheduler.runNext()
        longSession.up()

        assertEquals(0, longTaps)
        assertEquals(1, longPresses)
        assertFalse(longScheduler.hasPending())
    }

    @Test
    fun repeatPolicyAcceleratesWithoutBecomingTooAggressive() {
        assertEquals(80L, BackspaceRepeatPolicy.intervalMs(1))
        assertEquals(80L, BackspaceRepeatPolicy.intervalMs(11))
        assertEquals(55L, BackspaceRepeatPolicy.intervalMs(12))
        assertEquals(55L, BackspaceRepeatPolicy.intervalMs(29))
        assertEquals(40L, BackspaceRepeatPolicy.intervalMs(30))
    }
}
