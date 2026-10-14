package com.example.snake.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.snake.Phase
import com.example.snake.SnakeViewModel
import com.example.snake.game.Direction
import com.example.snake.game.SwipeDetector

@Composable
fun SnakeApp(viewModel: SnakeViewModel) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()

    BackHandler(enabled = ui.phase != Phase.MENU) {
        if (ui.phase == Phase.PLAYING && !ui.paused) viewModel.pause() else viewModel.toMenu()
    }

    SnakeTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SnakeColors.Background)
                .safeDrawingPadding()
                // Gestures cover the whole screen. Buttons and the Switch consume their own touches first,
                // and the ViewModel ignores swipes/taps outside active play.
                .swipeAndTap(onSwipe = viewModel::onSwipe, onTap = viewModel::onTap),
        ) {
            when (ui.phase) {
                Phase.MENU -> MenuScreen(
                    highScore = ui.highScore,
                    wrapWalls = ui.wrapWalls,
                    onWrapWallsChange = viewModel::setWrapWalls,
                    onPlay = viewModel::play,
                )

                Phase.PLAYING, Phase.GAME_OVER -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ScoreBar(score = ui.game.score, highScore = ui.highScore)
                        BoardCanvas(
                            state = ui.game,
                            config = ui.config,
                            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                    if (ui.paused) PauseOverlay()
                    if (ui.phase == Phase.GAME_OVER) {
                        GameOverOverlay(
                            score = ui.game.score,
                            highScore = ui.highScore,
                            isNewHighScore = ui.isNewHighScore,
                            reason = ui.game.endReason,
                            onPlayAgain = viewModel::play,
                            onMenu = viewModel::toMenu,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreBar(score: Int, highScore: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "SCORE  $score",
            color = SnakeColors.TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        Text(text = "BEST  $highScore", color = SnakeColors.TextMuted, fontSize = 16.sp)
    }
}

/**
 * Swipe anywhere to steer, tap to pause/resume.
 *
 * A swipe fires as soon as the finger has travelled [SWIPE_THRESHOLD] along one axis and then restarts from
 * there, so a continuous drag can chain several turns (e.g. right, then down) without lifting the finger.
 */
private fun Modifier.swipeAndTap(onSwipe: (Direction) -> Unit, onTap: () -> Unit): Modifier =
    this
        .pointerInput(Unit) { detectTapGestures(onTap = { onTap() }) }
        .pointerInput(Unit) {
            val thresholdPx = SWIPE_THRESHOLD.toPx()
            var travelled = Offset.Zero
            detectDragGestures(
                onDragStart = { travelled = Offset.Zero },
                onDrag = { change, dragAmount ->
                    change.consume()
                    travelled += dragAmount
                    SwipeDetector.directionOf(travelled.x, travelled.y, thresholdPx)?.let { direction ->
                        onSwipe(direction)
                        travelled = Offset.Zero
                    }
                },
            )
        }

private val SWIPE_THRESHOLD = 20.dp
