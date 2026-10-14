package com.example.snake.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.snake.game.EndReason

@Composable
fun MenuScreen(
    highScore: Int,
    wrapWalls: Boolean,
    onWrapWallsChange: (Boolean) -> Unit,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "SNAKE",
            color = SnakeColors.SnakeHead,
            fontSize = 56.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 6.sp,
        )
        Spacer(Modifier.height(8.dp))
        Text("High score: $highScore", color = SnakeColors.TextMuted, fontSize = 18.sp)
        Spacer(Modifier.height(40.dp))
        Button(onClick = onPlay) {
            Text("Play", fontSize = 20.sp, modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp))
        }
        Spacer(Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Wrap-around walls", color = SnakeColors.TextPrimary, fontSize = 16.sp)
                Text(
                    "Pass through a wall to come out the other side",
                    color = SnakeColors.TextMuted,
                    fontSize = 13.sp,
                )
            }
            Switch(checked = wrapWalls, onCheckedChange = onWrapWallsChange)
        }
        Spacer(Modifier.height(32.dp))
        Text(
            "Swipe anywhere to steer\nTap to pause",
            color = SnakeColors.TextMuted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun PauseOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().background(SnakeColors.Scrim),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Paused", color = SnakeColors.TextPrimary, fontSize = 36.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Tap to resume", color = SnakeColors.TextMuted, fontSize = 16.sp)
        }
    }
}

@Composable
fun GameOverOverlay(
    score: Int,
    highScore: Int,
    isNewHighScore: Boolean,
    reason: EndReason?,
    onPlayAgain: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().background(SnakeColors.Scrim),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .padding(horizontal = 32.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Game Over", color = SnakeColors.Food, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                text = when (reason) {
                    EndReason.WALL -> "You hit the wall"
                    EndReason.SELF -> "You ran into yourself"
                    EndReason.BOARD_FULL -> "You filled the whole board!"
                    null -> ""
                },
                color = SnakeColors.TextMuted,
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(24.dp))
            Text("Score", color = SnakeColors.TextMuted, fontSize = 14.sp)
            Text("$score", color = SnakeColors.TextPrimary, fontSize = 52.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            if (isNewHighScore) {
                Text("New high score!", color = SnakeColors.SnakeHead, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            } else {
                Text("High score: $highScore", color = SnakeColors.TextMuted, fontSize = 18.sp)
            }
            Spacer(Modifier.height(28.dp))
            Button(onClick = onPlayAgain) {
                Text("Play Again", fontSize = 18.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
            }
            TextButton(onClick = onMenu) {
                Text("Menu", color = SnakeColors.TextMuted)
            }
        }
    }
}
