package com.example.snake.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SnakeGameCollisionTest {

    private val far = Point(0, 0)

    @Test
    fun `hitting the right wall ends the game`() {
        val game = gameOf(smallConfig(), stateOf(lineRight(7, 4), Direction.RIGHT, food = far))
        game.tick()
        assertEquals(GameStatus.GAME_OVER, game.state.status)
        assertEquals(EndReason.WALL, game.state.endReason)
    }

    @Test
    fun `hitting each of the four walls ends the game`() {
        val cases = listOf(
            Direction.RIGHT to listOf(Point(7, 4), Point(6, 4), Point(5, 4)),
            Direction.LEFT to listOf(Point(0, 4), Point(1, 4), Point(2, 4)),
            Direction.UP to listOf(Point(4, 0), Point(4, 1), Point(4, 2)),
            Direction.DOWN to listOf(Point(4, 7), Point(4, 6), Point(4, 5)),
        )
        for ((dir, snake) in cases) {
            val game = gameOf(smallConfig(), stateOf(snake, dir, food = Point(2, 2)))
            game.tick()
            assertEquals("$dir", EndReason.WALL, game.state.endReason)
        }
    }

    @Test
    fun `the snake does not move on the tick it dies`() {
        val snake = lineRight(7, 4)
        val game = gameOf(smallConfig(), stateOf(snake, Direction.RIGHT, food = far))
        game.tick()
        assertEquals(snake, game.state.snake)
    }

    @Test
    fun `with wrap-around the snake reappears on the opposite side`() {
        val cases = listOf(
            Direction.RIGHT to (listOf(Point(7, 4), Point(6, 4), Point(5, 4)) to Point(0, 4)),
            Direction.LEFT to (listOf(Point(0, 4), Point(1, 4), Point(2, 4)) to Point(7, 4)),
            Direction.UP to (listOf(Point(4, 0), Point(4, 1), Point(4, 2)) to Point(4, 7)),
            Direction.DOWN to (listOf(Point(4, 7), Point(4, 6), Point(4, 5)) to Point(4, 0)),
        )
        for ((dir, setup) in cases) {
            val (snake, expectedHead) = setup
            val game = gameOf(smallConfig(wrap = true), stateOf(snake, dir, food = Point(2, 2)))
            game.tick()
            assertTrue("$dir should keep running", game.state.isRunning)
            assertEquals("$dir", expectedHead, game.state.head)
        }
    }

    @Test
    fun `with wrap-around the snake can eat food on the far side`() {
        val game = gameOf(smallConfig(wrap = true), stateOf(lineRight(7, 4), Direction.RIGHT, food = Point(0, 4)))
        game.tick()
        assertEquals(4, game.state.snake.size)
        assertEquals(10, game.state.score)
    }

    @Test
    fun `running into its own body ends the game`() {
        // Head (2,2) heading left; body wraps around above it. Turning up runs into (2,1).
        val snake = listOf(Point(2, 2), Point(3, 2), Point(3, 1), Point(2, 1), Point(1, 1))
        val game = gameOf(smallConfig(), stateOf(snake, Direction.LEFT, food = Point(7, 7)))
        assertTrue(game.turn(Direction.UP))
        game.tick()
        assertEquals(GameStatus.GAME_OVER, game.state.status)
        assertEquals(EndReason.SELF, game.state.endReason)
    }

    @Test
    fun `moving into the cell the tail is leaving is allowed`() {
        // A 2x2 loop: the head chases the tail. The tail cell (2,1) is vacated on this very tick.
        val snake = listOf(Point(2, 2), Point(3, 2), Point(3, 1), Point(2, 1))
        val game = gameOf(smallConfig(), stateOf(snake, Direction.LEFT, food = Point(7, 7)))
        game.turn(Direction.UP)
        game.tick()
        assertTrue(game.state.isRunning)
        assertEquals(Point(2, 1), game.state.head)
    }

    @Test
    fun `moving into the tail cell is fatal on the tick the snake grows`() {
        // Same loop, but food sits on the tail cell: the snake grows, the tail stays put, so it is a collision.
        val snake = listOf(Point(2, 2), Point(3, 2), Point(3, 1), Point(2, 1))
        val game = gameOf(smallConfig(), stateOf(snake, Direction.LEFT, food = Point(2, 1)))
        game.turn(Direction.UP)
        game.tick()
        assertEquals(EndReason.SELF, game.state.endReason)
    }

    @Test
    fun `self collision also applies when wrapping around`() {
        val snake = listOf(Point(0, 3), Point(1, 3), Point(2, 3), Point(7, 3), Point(6, 3), Point(5, 3))
        // Head at x=0 heading LEFT wraps to x=7, which the snake body occupies.
        val game = gameOf(smallConfig(wrap = true), stateOf(snake, Direction.LEFT, food = Point(4, 4)))
        game.tick()
        assertEquals(EndReason.SELF, game.state.endReason)
    }
}
