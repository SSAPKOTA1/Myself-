package com.example.snake.game

/**
 * Bookkeeping for a fixed-rate loop that does not drift.
 *
 * A naive `delay(interval)` after each tick adds the tick's own processing time and scheduler jitter to every
 * step, so the game slowly runs slow. Instead each deadline is derived from the previous *deadline*, not from
 * when the previous tick happened to run. If the loop falls more than a whole step behind (the process was
 * frozen, say), it resynchronises instead of firing a burst of catch-up ticks.
 *
 * Usage: [start], then repeat { sleep for [nanosUntilDue]; do the tick; [advance] }.
 * All times are monotonic nanoseconds (e.g. `System.nanoTime()`).
 */
class FixedStepTimer {
    private var dueAt = 0L

    /** Schedules the first tick one [intervalNanos] after [nowNanos]. Also used to resume after a pause. */
    fun start(nowNanos: Long, intervalNanos: Long) {
        dueAt = nowNanos + intervalNanos
    }

    fun nanosUntilDue(nowNanos: Long): Long = (dueAt - nowNanos).coerceAtLeast(0L)

    /** Call after running a tick. [intervalNanos] may change between calls when the game speeds up. */
    fun advance(nowNanos: Long, intervalNanos: Long) {
        dueAt += intervalNanos
        if (dueAt <= nowNanos) dueAt = nowNanos + intervalNanos
    }
}
