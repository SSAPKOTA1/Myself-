package com.example.snake.game

/** Tunable rules of the game. The defaults match the spec: 20x30 board, +10 per food, speed-up every 5 food. */
data class GameConfig(
    val width: Int = 20,
    val height: Int = 30,
    val wrapWalls: Boolean = false,
    val initialLength: Int = 3,
    val pointsPerFood: Int = 10,
    val foodPerSpeedUp: Int = 5,
    val baseTickMillis: Long = 200,
    val minTickMillis: Long = 80,
    val speedUpStepMillis: Long = 10,
) {
    init {
        require(initialLength >= 2) { "Snake needs at least a head and a neck" }
        // Head sits at width / 2 facing right; the body extends to the left and must fit on the board.
        require(width / 2 >= initialLength - 1) { "Board too narrow for the starting snake" }
        require(width - width / 2 >= 2) { "Board needs room in front of the starting snake" }
        require(height >= 3) { "Board too short" }
        require(foodPerSpeedUp > 0) { "foodPerSpeedUp must be positive" }
        require(minTickMillis in 1..baseTickMillis) { "Need 0 < minTickMillis <= baseTickMillis" }
    }

    /** Time between moves after [foodEaten] food items; shrinks every [foodPerSpeedUp] items, down to [minTickMillis]. */
    fun tickMillisFor(foodEaten: Int): Long {
        val speedUps = foodEaten / foodPerSpeedUp
        return (baseTickMillis - speedUps * speedUpStepMillis).coerceAtLeast(minTickMillis)
    }
}
