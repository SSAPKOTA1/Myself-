package com.example.snake.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SwipeDetectorTest {
    private val t = 40f

    @Test
    fun `dominant axis wins`() {
        assertEquals(Direction.RIGHT, SwipeDetector.directionOf(100f, 20f, t))
        assertEquals(Direction.LEFT, SwipeDetector.directionOf(-100f, -20f, t))
        assertEquals(Direction.DOWN, SwipeDetector.directionOf(10f, 80f, t))
        assertEquals(Direction.UP, SwipeDetector.directionOf(-10f, -80f, t))
    }

    @Test
    fun `short movements are ignored`() {
        assertNull(SwipeDetector.directionOf(10f, -5f, t))
        assertNull(SwipeDetector.directionOf(0f, 0f, t))
    }

    @Test
    fun `threshold is inclusive`() {
        assertEquals(Direction.UP, SwipeDetector.directionOf(0f, -40f, t))
    }
}
