package com.example.snake.game

import org.junit.Assert.assertEquals
import org.junit.Test

class FixedStepTimerTest {
    private val ms = 1_000_000L

    @Test
    fun `first tick is one interval after start`() {
        val timer = FixedStepTimer()
        timer.start(nowNanos = 5_000 * ms, intervalNanos = 200 * ms)
        assertEquals(200 * ms, timer.nanosUntilDue(5_000 * ms))
        assertEquals(50 * ms, timer.nanosUntilDue(5_150 * ms))
    }

    @Test
    fun `lateness does not accumulate into drift`() {
        val timer = FixedStepTimer()
        val interval = 200 * ms
        timer.start(0, interval)
        // Every tick fires 7ms late and takes 3ms to process; deadlines must stay on the 200ms grid.
        var now = 0L
        repeat(100) {
            now += timer.nanosUntilDue(now) + 7 * ms // wake up late
            now += 3 * ms // do the tick
            timer.advance(now, interval)
        }
        // After 100 ticks the next deadline is exactly 101 intervals from the start. A naive delay loop
        // would be roughly 100 * 10ms = 1s late.
        assertEquals(101 * interval, now + timer.nanosUntilDue(now))
    }

    @Test
    fun `a long stall resynchronises instead of bursting`() {
        val timer = FixedStepTimer()
        val interval = 200 * ms
        timer.start(0, interval)
        val now = 10_000 * ms // process was frozen for 10 seconds
        timer.advance(now, interval)
        assertEquals(interval, timer.nanosUntilDue(now))
    }

    @Test
    fun `interval can shorten as the game speeds up`() {
        val timer = FixedStepTimer()
        timer.start(0, 200 * ms)
        timer.advance(200 * ms, 190 * ms)
        assertEquals(190 * ms, timer.nanosUntilDue(200 * ms))
    }

    @Test
    fun `nanosUntilDue never goes negative`() {
        val timer = FixedStepTimer()
        timer.start(0, 100 * ms)
        assertEquals(0L, timer.nanosUntilDue(500 * ms))
    }
}
