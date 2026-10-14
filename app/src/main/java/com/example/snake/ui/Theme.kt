package com.example.snake.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object SnakeColors {
    val Background = Color(0xFF0E1116)
    val Board = Color(0xFF161B22)
    val BoardBorderSolid = Color(0xFF8B949E) // walls are deadly
    val BoardBorderWrap = Color(0xFF30363D) // walls wrap around
    val SnakeHead = Color(0xFF7EE787)
    val SnakeTail = Color(0xFF1F7A33)
    val Food = Color(0xFFFF6B6B)
    val TextPrimary = Color(0xFFE6EDF3)
    val TextMuted = Color(0xFF8B949E)
    val Scrim = Color(0xCC0E1116)
}

private val DarkColors = darkColorScheme(
    primary = SnakeColors.SnakeHead,
    onPrimary = SnakeColors.Background,
    background = SnakeColors.Background,
    onBackground = SnakeColors.TextPrimary,
    surface = SnakeColors.Board,
    onSurface = SnakeColors.TextPrimary,
    onSurfaceVariant = SnakeColors.TextMuted,
)

@Composable
fun SnakeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
