package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.DifficultyLevel
import com.example.model.MascotBuddy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kuis_ceria_prefs")

data class UserPreferences(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val autoReadTts: Boolean = false,
    val difficulty: DifficultyLevel = DifficultyLevel.ALL,
    val mascot: MascotBuddy = MascotBuddy.BERUANG
)

class SettingsDataStore(private val context: Context) {
    private object Keys {
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val AUTO_READ_TTS = booleanPreferencesKey("auto_read_tts")
        val DIFFICULTY = stringPreferencesKey("difficulty")
        val MASCOT = stringPreferencesKey("mascot")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            musicEnabled = prefs[Keys.MUSIC_ENABLED] ?: true,
            autoReadTts = prefs[Keys.AUTO_READ_TTS] ?: false,
            difficulty = DifficultyLevel.fromName(prefs[Keys.DIFFICULTY] ?: DifficultyLevel.ALL.name),
            mascot = MascotBuddy.fromName(prefs[Keys.MASCOT] ?: MascotBuddy.BERUANG.name)
        )
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MUSIC_ENABLED] = enabled }
    }

    suspend fun setAutoReadTts(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_READ_TTS] = enabled }
    }

    suspend fun setDifficulty(level: DifficultyLevel) {
        context.dataStore.edit { it[Keys.DIFFICULTY] = level.name }
    }

    suspend fun setMascot(mascot: MascotBuddy) {
        context.dataStore.edit { it[Keys.MASCOT] = mascot.name }
    }
}
