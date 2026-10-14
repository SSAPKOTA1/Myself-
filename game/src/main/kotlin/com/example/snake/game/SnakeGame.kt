package com.example.snake.game

import kotlin.random.Random

/**
 * The rules engine: movement, growth, scoring, collisions and food spawning.
 * Pure Kotlin with an injectable [Random], so every behaviour can be tested deterministically.
 *
 * Call [turn] when the player swipes and [tick] once per game step. [state] is an immutable snapshot.
 */
class SnakeGame(
    val config: GameConfig = GameConfig(),
    private val random: Random = Random.Default,
    initialState: GameState? = null,
) {
    private val board = Board(config.width, config.height)

    var state: GameState = initialState ?: newState()
        private set

    /** Milliseconds between ticks at the current speed. */
    val tickMillis: Long get() = config.tickMillisFor(state.foodEaten)

    fun restart(): GameState {
        state = newState()
        return state
    }

    /**
     * Queues a direction change. Returns false (and ignores the input) if it would reverse into the neck
     * or repeats the current heading.
     *
     * The check is made against the *last queued* heading rather than the last executed one, so two quick
     * swipes (e.g. up then left while moving right) become a legal two-step U-turn, while a reversal can
     * never sneak in between ticks. At most [MAX_QUEUED_TURNS] turns wait at once.
     */
    fun turn(direction: Direction): Boolean {
        val s = state
        if (!s.isRunning || s.pendingTurns.size >= MAX_QUEUED_TURNS) return false
        val reference = s.pendingTurns.lastOrNull() ?: s.direction
        if (direction == reference || direction == reference.opposite) return false
        state = s.copy(pendingTurns = s.pendingTurns + direction)
        return true
    }

    /** Advances the game by one step and returns the new state. No-op once the game is over. */
    fun tick(): GameState {
        val s = state
        if (!s.isRunning) return s

        val direction = s.pendingTurns.firstOrNull() ?: s.direction
        val remainingTurns = s.pendingTurns.drop(1)

        var next = s.head + direction
        if (next !in board) {
            if (config.wrapWalls) next = board.wrap(next) else return gameOver(s, EndReason.WALL, direction, remainingTurns)
        }

        val eats = next == s.food
        // The tail cell is vacated this step unless we grow, so moving into it is legal when not eating.
        val bodyInTheWay = if (eats) s.snake else s.snake.dropLast(1)
        if (next in bodyInTheWay) return gameOver(s, EndReason.SELF, direction, remainingTurns)

        val newSnake = ArrayList<Point>(s.snake.size + 1)
        newSnake += next
        newSnake += if (eats) s.snake else s.snake.dropLast(1)

        var nextState = s.copy(snake = newSnake, direction = direction, pendingTurns = remainingTurns)
        if (eats) {
            val food = board.randomEmptyCell(newSnake, random)
            nextState = nextState.copy(
                score = s.score + config.pointsPerFood,
                foodEaten = s.foodEaten + 1,
                food = food,
                status = if (food == null) GameStatus.GAME_OVER else GameStatus.RUNNING,
                endReason = if (food == null) EndReason.BOARD_FULL else null,
            )
        }
        state = nextState
        return nextState
    }

    private fun gameOver(s: GameState, reason: EndReason, direction: Direction, turns: List<Direction>): GameState {
        state = s.copy(
            direction = direction,
            pendingTurns = turns,
            status = GameStatus.GAME_OVER,
            endReason = reason,
        )
        return state
    }

    private fun newState(): GameState {
        val head = Point(config.width / 2, config.height / 2)
        val snake = List(config.initialLength) { i -> Point(head.x - i, head.y) }
        return GameState(
            snake = snake,
            direction = Direction.RIGHT,
            food = board.randomEmptyCell(snake, random),
        )
    }

    companion object {
        const val MAX_QUEUED_TURNS = 2
    }
}
