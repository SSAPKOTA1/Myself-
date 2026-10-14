package com.example.snake.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import com.example.snake.game.GameConfig
import com.example.snake.game.GameState

/** Draws the board, food and snake, scaled to the largest cell size that fits the available space. */
@Composable
fun BoardCanvas(state: GameState, config: GameConfig, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cell = minOf(size.width / config.width, size.height / config.height)
        val boardSize = Size(cell * config.width, cell * config.height)
        val origin = Offset((size.width - boardSize.width) / 2f, (size.height - boardSize.height) / 2f)

        fun topLeftOf(x: Int, y: Int) = Offset(origin.x + x * cell, origin.y + y * cell)

        drawRoundRect(
            color = SnakeColors.Board,
            topLeft = origin,
            size = boardSize,
            cornerRadius = CornerRadius(cell * 0.4f),
        )
        drawRoundRect(
            color = if (config.wrapWalls) SnakeColors.BoardBorderWrap else SnakeColors.BoardBorderSolid,
            topLeft = origin,
            size = boardSize,
            cornerRadius = CornerRadius(cell * 0.4f),
            style = Stroke(width = maxOf(2f, cell * 0.1f)),
        )

        state.food?.let { food ->
            drawCircle(
                color = SnakeColors.Food,
                radius = cell * 0.36f,
                center = topLeftOf(food.x, food.y) + Offset(cell / 2f, cell / 2f),
            )
        }

        val inset = cell * 0.06f
        val segmentSize = Size(cell - 2 * inset, cell - 2 * inset)
        val lastIndex = (state.snake.size - 1).coerceAtLeast(1)
        state.snake.forEachIndexed { index, p ->
            drawRoundRect(
                color = lerp(SnakeColors.SnakeHead, SnakeColors.SnakeTail, index / lastIndex.toFloat()),
                topLeft = topLeftOf(p.x, p.y) + Offset(inset, inset),
                size = segmentSize,
                cornerRadius = CornerRadius(cell * 0.28f),
            )
        }
    }
}
