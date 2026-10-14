package com.example.snake.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SnakeGameTurnTest {

    private fun newGame(direction: Direction = Direction.RIGHT): SnakeGame {
        val snake = when (direction) {
            Direction.RIGHT -> listOf(Point(4, 4), Point(3, 4), Point(2, 4))
            Direction.LEFT -> listOf(Point(4, 4), Point(5, 4), Point(6, 4))
            Direction.UP -> listOf(Point(4, 4), Point(4, 5), Point(4, 6))
            Direction.DOWN -> listOf(Point(4, 4), Point(4, 3), Point(4, 2))
        }
        return gameOf(smallConfig(), stateOf(snake, direction, food = Point(0, 0)))
    }

    @Test
    fun `cannot reverse directly into the neck`() {
        for (d in Direction.entries) {
            val game = newGame(d)
            assertFalse("$d -> ${d.opposite}", game.turn(d.opposite))
            game.tick()
            assertEquals("$d keeps going", d, game.state.direction)
            assertTrue(game.state.isRunning)
        }
    }

    @Test
    fun `perpendicular turns are accepted`() {
        assertTrue(newGame(Direction.RIGHT).turn(Direction.UP))
        assertTrue(newGame(Direction.RIGHT).turn(Direction.DOWN))
        assertTrue(newGame(Direction.UP).turn(Direction.LEFT))
        assertTrue(newGame(Direction.UP).turn(Direction.RIGHT))
    }

    @Test
    fun `repeating the current direction is a no-op`() {
        val game = newGame()
        assertFalse(game.turn(Direction.RIGHT))
        assertTrue(game.state.pendingTurns.isEmpty())
    }

    @Test
    fun `two quick swipes cannot reverse the snake within a single tick`() {
        // Moving right: "up" then "down" would flip the snake into itself if the second were checked
        // against the old heading. It must be judged against the queued "up" and rejected.
        val game = newGame()
        assertTrue(game.turn(Direction.UP))
        assertFalse(game.turn(Direction.DOWN))
        game.tick()
        assertTrue(game.state.isRunning)
        assertEquals(Direction.UP, game.state.direction)
    }

    @Test
    fun `up then left while moving right executes as a safe two-step U-turn`() {
        val game = newGame()
        assertTrue(game.turn(Direction.UP))
        assertTrue(game.turn(Direction.LEFT))
        game.tick()
        assertEquals(Point(4, 3), game.state.head)
        game.tick()
        assertEquals(Point(3, 3), game.state.head)
        assertTrue(game.state.isRunning)
    }

    @Test
    fun `no more than two turns are queued`() {
        val game = newGame()
        assertTrue(game.turn(Direction.UP))
        assertTrue(game.turn(Direction.LEFT))
        assertFalse(game.turn(Direction.DOWN))
        assertEquals(listOf(Direction.UP, Direction.LEFT), game.state.pendingTurns)
    }

    @Test
    fun `reversal is still blocked after a turn has been applied`() {
        val game = newGame()
        game.turn(Direction.UP)
        game.tick()
        assertFalse(game.turn(Direction.DOWN))
        assertTrue(game.turn(Direction.LEFT))
    }
}
