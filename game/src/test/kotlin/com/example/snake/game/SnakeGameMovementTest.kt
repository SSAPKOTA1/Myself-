package com.example.snake.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SnakeGameMovementTest {

    @Test
    fun `new game starts with length 3 in the center moving right`() {
        val game = SnakeGame(GameConfig(), Random(1))
        val s = game.state
        assertEquals(3, s.snake.size)
        assertEquals(Point(10, 15), s.head)
        assertEquals(listOf(Point(10, 15), Point(9, 15), Point(8, 15)), s.snake)
        assertEquals(Direction.RIGHT, s.direction)
        assertEquals(0, s.score)
        assertTrue(s.isRunning)
        assertNotNull(s.food)
        assertFalse(s.food in s.snake)
    }

    @Test
    fun `default board is 20 by 30`() {
        val config = GameConfig()
        assertEquals(20, config.width)
        assertEquals(30, config.height)
        assertFalse("wrap-around must be off by default", config.wrapWalls)
    }

    @Test
    fun `tick moves the snake one cell in its direction without changing length`() {
        val game = gameOf(smallConfig(), stateOf(lineRight(3, 4), Direction.RIGHT, food = Point(0, 0)))
        game.tick()
        assertEquals(listOf(Point(4, 4), Point(3, 4), Point(2, 4)), game.state.snake)
        game.tick()
        assertEquals(listOf(Point(5, 4), Point(4, 4), Point(3, 4)), game.state.snake)
    }

    @Test
    fun `a queued turn is applied on the next tick`() {
        val game = gameOf(smallConfig(), stateOf(lineRight(3, 4), Direction.RIGHT, food = Point(0, 0)))
        assertTrue(game.turn(Direction.UP))
        game.tick()
        assertEquals(Point(3, 3), game.state.head)
        assertEquals(Direction.UP, game.state.direction)
        game.tick()
        assertEquals(Point(3, 2), game.state.head)
    }

    @Test
    fun `each direction moves the head by one cell`() {
        for (d in Direction.entries) {
            // Snake pointing along d, far from every wall.
            val snake = listOf(Point(4, 4), Point(4 - d.dx, 4 - d.dy), Point(4 - 2 * d.dx, 4 - 2 * d.dy))
            val game = gameOf(smallConfig(), stateOf(snake, d, food = Point(0, 0)))
            game.tick()
            assertEquals(Point(4 + d.dx, 4 + d.dy), game.state.head)
        }
    }

    @Test
    fun `restart gives a fresh running game`() {
        val game = gameOf(smallConfig(), stateOf(lineRight(7, 4), Direction.RIGHT, food = Point(0, 0), score = 50))
        game.tick()
        assertFalse(game.state.isRunning)
        game.restart()
        assertTrue(game.state.isRunning)
        assertEquals(0, game.state.score)
        assertEquals(3, game.state.snake.size)
    }

    @Test
    fun `tick after game over does nothing`() {
        val game = gameOf(smallConfig(), stateOf(lineRight(7, 4), Direction.RIGHT, food = Point(0, 0)))
        game.tick()
        val over = game.state
        assertFalse(over.isRunning)
        assertEquals(over, game.tick())
        assertFalse(game.turn(Direction.UP))
    }
}
