package com.example.snake.game

import kotlin.math.abs

/** Turns a finger movement into a [Direction]. Kept free of UI types so it can be unit tested. */
object SwipeDetector {
    /**
     * @return the dominant axis of the movement ([dx], [dy] in pixels, y pointing down),
     * or null if it is shorter than [thresholdPx] on both axes.
     */
    fun directionOf(dx: Float, dy: Float, thresholdPx: Float): Direction? {
        val ax = abs(dx)
        val ay = abs(dy)
        if (maxOf(ax, ay) < thresholdPx) return null
        return if (ax >= ay) {
            if (dx > 0) Direction.RIGHT else Direction.LEFT
        } else {
            if (dy > 0) Direction.DOWN else Direction.UP
        }
    }
}
