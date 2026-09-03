package br.com.teclado.ime

import android.os.Handler
import android.os.Looper

class AndroidTouchScheduler(
    private val handler: Handler = Handler(Looper.getMainLooper())
) : TouchScheduler {
    override fun schedule(delayMs: Long, block: () -> Unit): ScheduledTouchTask {
        val runnable = Runnable(block)
        handler.postDelayed(runnable, delayMs)
        return ScheduledTouchTask { handler.removeCallbacks(runnable) }
    }
}
