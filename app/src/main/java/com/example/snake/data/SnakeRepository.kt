package com.example.snake.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/** One DataStore per process; this delegate must only be declared once. */
val Context.snakeDataStore: DataStore<Preferences> by preferencesDataStore(name = "snake")

/** High score and the wrap-around setting, persisted across app restarts. */
class SnakeRepository(private val store: DataStore<Preferences>) {

    val highScore: Flow<Int> = safeData().map { it[HIGH_SCORE] ?: 0 }

    val wrapWalls: Flow<Boolean> = safeData().map { it[WRAP_WALLS] ?: false }

    /** Stores [score] only if it beats the saved value, so a late or duplicate call can never lower it. */
    suspend fun saveHighScoreIfHigher(score: Int) {
        store.edit { prefs ->
            if (score > (prefs[HIGH_SCORE] ?: 0)) prefs[HIGH_SCORE] = score
        }
    }

    suspend fun setWrapWalls(enabled: Boolean) {
        store.edit { it[WRAP_WALLS] = enabled }
    }

    // A corrupt/unreadable file should not crash the game; fall back to defaults.
    private fun safeData(): Flow<Preferences> = store.data.catch { e ->
        if (e is IOException) emit(emptyPreferences()) else throw e
    }

    private companion object {
        val HIGH_SCORE = intPreferencesKey("high_score")
        val WRAP_WALLS = booleanPreferencesKey("wrap_walls")
    }
}
