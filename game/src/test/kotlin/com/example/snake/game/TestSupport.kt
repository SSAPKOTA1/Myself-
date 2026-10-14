package com.example.snake.game

/** A small board keeps hand-built scenarios readable. */
fun smallConfig(wrap: Boolean = false, width: Int = 8, height: Int = 8) =
    GameConfig(width = width, height = height, wrapWalls = wrap)

fun stateOf(
    snake: List<Point>,
    direction: Direction,
    food: Point? = null,
    score: Int = 0,
    foodEaten: Int = 0,
) = GameState(snake = snake, direction = direction, food = food, score = score, foodEaten = foodEaten)

fun gameOf(config: GameConfig, state: GameState) = SnakeGame(config, initialState = state)

/** Straight horizontal snake, head first, heading right. */
fun lineRight(headX: Int, y: Int, length: Int = 3) = List(length) { Point(headX - it, y) }
