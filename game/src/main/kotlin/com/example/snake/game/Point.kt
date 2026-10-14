package com.example.snake.game

/** A cell on the board. x grows to the right, y grows downwards. */
data class Point(val x: Int, val y: Int) {
    operator fun plus(direction: Direction) = Point(x + direction.dx, y + direction.dy)
}

enum class Direction(val dx: Int, val dy: Int) {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    val opposite: Direction
        get() = when (this) {
            UP -> DOWN
            DOWN -> UP
            LEFT -> RIGHT
            RIGHT -> LEFT
        }
}
