package com.example.snake.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SnakeGameGrowthTest {

    @Test
    fun `eating food grows the snake by one and adds 10 points`() {
        val game = gameOf(smallConfig(), stateOf(lineRight(3, 4), Direction.RIGHT, food = Point(4, 4)))
        game.tick()
        val s = game.state
        assertEquals(4, s.snake.size)
        assertEquals(listOf(Point(4, 4), Point(3, 4), Point(2, 4), Point(1, 4)), s.snake)
        assertEquals(10, s.score)
        assertEquals(1, s.foodEaten)
    }

    @Test
    fun `new food appears after eating and is not on the snake`() {
        val game = SnakeGame(smallConfig(), Random(7), stateOf(lineRight(3, 4), Direction.RIGHT, food = Point(4, 4)))
        game.tick()
        val food = game.state.food
        assertNotNull(food)
        assertFalse(food in game.state.snake)
    }

    @Test
    fun `moving without eating leaves score and length unchanged`() {
        val game = gameOf(smallConfig(), stateOf(lineRight(3, 4), Direction.RIGHT, food = Point(0, 0)))
        game.tick()
        assertEquals(0, game.state.score)
        assertEquals(3, game.state.snake.size)
    }

    @Test
    fun `score accumulates over several foods`() {
        val game = gameOf(smallConfig(width = 12), stateOf(lineRight(3, 4), Direction.RIGHT, food = Point(4, 4)))
        game.tick()
        // Put the next food directly ahead via a fresh state to keep the scenario deterministic.
        val s = game.state
        val next = SnakeGame(game.config, initialState = s.copy(food = Point(5, 4)))
        next.tick()
        assertEquals(20, next.state.score)
        assertEquals(2, next.state.foodEaten)
        assertEquals(5, next.state.snake.size)
    }

    @Test
    fun `speed increases every 5 foods up to a maximum`() {
        val config = GameConfig()
        assertEquals(200, config.tickMillisFor(0))
        assertEquals(200, config.tickMillisFor(4))
        assertEquals(190, config.tickMillisFor(5))
        assertEquals(190, config.tickMillisFor(9))
        assertEquals(180, config.tickMillisFor(10))
        assertEquals(80, config.tickMillisFor(60))
        assertEquals("speed must not exceed the maximum", 80, config.tickMillisFor(500))
    }

    @Test
    fun `game exposes the current tick interval as foods are eaten`() {
        val game = gameOf(smallConfig(), stateOf(lineRight(3, 4), Direction.RIGHT, food = Point(4, 4), foodEaten = 4))
        assertEquals(200, game.tickMillis)
        game.tick() // eats the 5th food
        assertEquals(190, game.tickMillis)
    }

    @Test
    fun `filling the whole board ends the game with no food left`() {
        // 4x3 board, snake fills 11 cells in a zigzag, the last free cell (3,2) holds the food.
        val snake = listOf(
            Point(2, 2), Point(1, 2), Point(0, 2),
            Point(0, 1), Point(1, 1), Point(2, 1), Point(3, 1),
            Point(3, 0), Point(2, 0), Point(1, 0), Point(0, 0),
        )
        val config = smallConfig(width = 4, height = 3)
        val game = gameOf(config, stateOf(snake, Direction.RIGHT, food = Point(3, 2)))
        game.tick()
        assertEquals(12, game.state.snake.size)
        assertNull(game.state.food)
        assertFalse(game.state.isRunning)
        assertEquals(EndReason.BOARD_FULL, game.state.endReason)
    }
}
