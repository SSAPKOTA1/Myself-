package com.example.snake

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.snake.data.SnakeRepository
import com.example.snake.data.snakeDataStore
import com.example.snake.game.Direction
import com.example.snake.game.FixedStepTimer
import com.example.snake.game.GameConfig
import com.example.snake.game.GameState
import com.example.snake.game.SnakeGame
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class Phase { MENU, PLAYING, GAME_OVER }

/** Everything the UI needs to draw a frame. */
data class UiState(
    val phase: Phase = Phase.MENU,
    val paused: Boolean = false,
    val config: GameConfig = GameConfig(),
    val game: GameState,
    val highScore: Int = 0,
    val isNewHighScore: Boolean = false,
    /** The wrap-around setting chosen on the menu; applied when the next game starts. */
    val wrapWalls: Boolean = false,
)

/**
 * Owns the [SnakeGame] and the coroutine that ticks it. All game access happens on the main thread
 * (viewModelScope), so no locking is needed.
 */
class SnakeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SnakeRepository(application.snakeDataStore)

    private var game = SnakeGame(GameConfig())
    private var loopJob: Job? = null

    private val _ui = MutableStateFlow(UiState(game = game.state, config = game.config))
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            repository.highScore.collect { best -> _ui.update { it.copy(highScore = maxOf(it.highScore, best)) } }
        }
        viewModelScope.launch {
            repository.wrapWalls.collect { wrap -> _ui.update { it.copy(wrapWalls = wrap) } }
        }
    }

    fun play() {
        game = SnakeGame(GameConfig(wrapWalls = _ui.value.wrapWalls))
        _ui.update {
            it.copy(
                phase = Phase.PLAYING,
                paused = false,
                config = game.config,
                game = game.state,
                isNewHighScore = false,
            )
        }
        startLoop()
    }

    fun toMenu() {
        loopJob?.cancel()
        _ui.update { it.copy(phase = Phase.MENU, paused = false) }
    }

    fun setWrapWalls(enabled: Boolean) {
        _ui.update { it.copy(wrapWalls = enabled) }
        viewModelScope.launch { repository.setWrapWalls(enabled) }
    }

    fun onSwipe(direction: Direction) {
        val s = _ui.value
        if (s.phase == Phase.PLAYING && !s.paused) game.turn(direction)
    }

    fun onTap() {
        val s = _ui.value
        if (s.phase != Phase.PLAYING) return
        if (s.paused) resume() else pause()
    }

    fun pause() {
        val s = _ui.value
        if (s.phase != Phase.PLAYING || s.paused) return
        loopJob?.cancel()
        _ui.update { it.copy(paused = true) }
    }

    private fun resume() {
        _ui.update { it.copy(paused = false) }
        startLoop()
    }

    /**
     * The game loop. Deadlines come from [FixedStepTimer], which derives each tick time from the previous
     * *deadline* rather than from when the previous tick ran, so processing time and scheduler jitter
     * don't accumulate into a slowly slowing game.
     */
    private fun startLoop() {
        loopJob?.cancel()
        loopJob = viewModelScope.launch {
            val timer = FixedStepTimer()
            timer.start(System.nanoTime(), game.tickMillis * NANOS_PER_MILLI)
            while (isActive) {
                val wait = timer.nanosUntilDue(System.nanoTime())
                if (wait > 0) {
                    // Round up so we never wake a hair early and spin through an extra iteration.
                    delay((wait + NANOS_PER_MILLI - 1) / NANOS_PER_MILLI)
                    continue
                }
                val state = game.tick()
                _ui.update { it.copy(game = state) }
                if (!state.isRunning) {
                    finishGame(state)
                    break
                }
                // The interval is re-read each tick because it shortens as the player eats.
                timer.advance(System.nanoTime(), game.tickMillis * NANOS_PER_MILLI)
            }
        }
    }

    private fun finishGame(finalState: GameState) {
        val previousBest = _ui.value.highScore
        val isNewBest = finalState.score > previousBest
        _ui.update {
            it.copy(
                phase = Phase.GAME_OVER,
                paused = false,
                isNewHighScore = isNewBest,
                highScore = maxOf(previousBest, finalState.score),
            )
        }
        if (isNewBest) {
            viewModelScope.launch { repository.saveHighScoreIfHigher(finalState.score) }
        }
    }

    private companion object {
        const val NANOS_PER_MILLI = 1_000_000L
    }
}
