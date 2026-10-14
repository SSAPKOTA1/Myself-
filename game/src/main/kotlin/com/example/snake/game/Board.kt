package com.example.snake.game

import kotlin.random.Random

/** The playing field geometry: bounds, wrap-around and empty-cell lookup. Holds no game state. */
class Board(val width: Int, val height: Int) {
    val cellCount: Int get() = width * height

    operator fun contains(p: Point): Boolean = p.x in 0 until width && p.y in 0 until height

    /** Maps a point that left the board back onto the opposite side. */
    fun wrap(p: Point): Point = Point(Math.floorMod(p.x, width), Math.floorMod(p.y, height))

    fun emptyCells(occupied: Collection<Point>): List<Point> {
        val taken = occupied.toHashSet()
        val result = ArrayList<Point>(cellCount - taken.size)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val cell = Point(x, y)
                if (cell !in taken) result += cell
            }
        }
        return result
    }

    /** A uniformly random cell not in [occupied], or null if the board is full. */
    fun randomEmptyCell(occupied: Collection<Point>, random: Random): Point? {
        val empty = emptyCells(occupied)
        return if (empty.isEmpty()) null else empty[random.nextInt(empty.size)]
    }
}
