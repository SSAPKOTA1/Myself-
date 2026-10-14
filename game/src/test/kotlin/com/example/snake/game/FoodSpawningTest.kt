package com.example.snake.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodSpawningTest {

    @Test
    fun `initial food never lands on the snake`() {
        repeat(500) { seed ->
            val game = SnakeGame(GameConfig(), Random(seed))
            assertFalse("seed $seed", game.state.food in game.state.snake)
        }
    }

    @Test
    fun `random empty cell is never occupied`() {
        val board = Board(6, 6)
        val occupied = (0 until 6).flatMap { x -> (0 until 5).map { y -> Point(x, y) } } // rows 0-4 full
        repeat(300) { seed ->
            val cell = board.randomEmptyCell(occupied, Random(seed))
            assertNotNull(cell)
            assertFalse(cell in occupied)
            assertEquals(5, cell!!.y)
        }
    }

    @Test
    fun `the only free cell is always chosen`() {
        val board = Board(3, 3)
        val free = Point(2, 1)
        val occupied = board.emptyCells(emptyList()) - free
        repeat(50) { seed -> assertEquals(free, board.randomEmptyCell(occupied, Random(seed))) }
    }

    @Test
    fun `no cell is returned when the board is full`() {
        val board = Board(3, 3)
        assertNull(board.randomEmptyCell(board.emptyCells(emptyList()), Random(1)))
    }

    @Test
    fun `food never spawns on the snake across many simulated games`() {
        // Drive games with a random bot (random turns, including illegal ones) and check the invariant every tick.
        val config = GameConfig(width = 8, height = 8, wrapWalls = true)
        var eaten = 0
        repeat(200) { seed ->
            val rng = Random(seed)
            val game = SnakeGame(config, rng)
            var ticks = 0
            while (game.state.isRunning && ticks++ < 2000) {
                if (rng.nextInt(3) == 0) game.turn(Direction.entries[rng.nextInt(4)])
                game.tick()
                val s = game.state
                s.food?.let { food ->
                    assertFalse("seed $seed tick $ticks: food on snake", food in s.snake)
                    assertTrue(food in Board(config.width, config.height))
                }
                assertEquals("snake overlaps itself", s.snake.size, s.snake.toSet().size)
                eaten = maxOf(eaten, s.foodEaten)
            }
        }
        assertTrue("simulation should have eaten some food to be meaningful", eaten > 0)
    }

    @Test
    fun `a bot that hunts food fills a small board without ever overlapping food and snake`() {
        // Greedy bot on a wrap-around board: exercises long snakes, where free cells are scarce.
        val config = GameConfig(width = 6, height = 6, wrapWalls = true)
        repeat(50) { seed ->
            val rng = Random(seed)
            val game = SnakeGame(config, rng)
            var ticks = 0
            while (game.state.isRunning && ticks++ < 5000) {
                val s = game.state
                val food = s.food ?: break
                val preferred = Direction.entries
                    .filter { it != s.direction.opposite }
                    .sortedBy { d -> manhattanWrapped(s.head + d, food, config) }
                // Pick the best direction that does not immediately crash, if any.
                val safe = preferred.firstOrNull { d ->
                    val n = Board(config.width, config.height).wrap(s.head + d)
                    n !in s.snake.dropLast(1)
                } ?: preferred.first()
                game.turn(safe)
                game.tick()
                game.state.food?.let { assertFalse(it in game.state.snake) }
            }
        }
    }

    private fun manhattanWrapped(a: Point, b: Point, c: GameConfig): Int {
        val dx = Math.floorMod(a.x - b.x, c.width).let { minOf(it, c.width - it) }
        val dy = Math.floorMod(a.y - b.y, c.height).let { minOf(it, c.height - it) }
        return dx + dy
    }
}
