package com.example.snake.game

enum class GameStatus { RUNNING, GAME_OVER }

enum class EndReason { WALL, SELF, BOARD_FULL }

/**
 * Immutable snapshot of a game. Safe to hand straight to the UI.
 *
 * @property snake body cells, head first.
 * @property direction direction of the most recent move.
 * @property food current food cell, or null once the board is completely full.
 * @property pendingTurns validated turns waiting to be applied, one per upcoming tick.
 */
data class GameState(
    val snake: List<Point>,
    val direction: Direction,
    val food: Point?,
    val score: Int = 0,
    val foodEaten: Int = 0,
    val status: GameStatus = GameStatus.RUNNING,
    val endReason: EndReason? = null,
    val pendingTurns: List<Direction> = emptyList(),
) {
    val head: Point get() = snake.first()
    val isRunning: Boolean get() = status == GameStatus.RUNNING
}
