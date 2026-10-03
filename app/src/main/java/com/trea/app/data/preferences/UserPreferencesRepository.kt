package com.trea.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "trea_preferences")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val KEY_NICKNAME = stringPreferencesKey("nickname")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_MUSIC_FOLDER_URI = stringPreferencesKey("music_folder_uri")
        val KEY_MUSIC_FOLDER_NAME = stringPreferencesKey("music_folder_name")
        const val DEFAULT_NICKNAME = "树小乐"
    }

    val nicknameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_NICKNAME] ?: DEFAULT_NICKNAME
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val modeStr = preferences[KEY_THEME_MODE]
        try {
            if (modeStr != null) ThemeMode.valueOf(modeStr) else ThemeMode.DYNAMIC
        } catch (_: Exception) {
            ThemeMode.DYNAMIC
        }
    }

    val musicFolderUriFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_MUSIC_FOLDER_URI]
    }

    val musicFolderNameFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_MUSIC_FOLDER_NAME]
    }

    suspend fun setNickname(nickname: String) {
        val trimmed = nickname.trim().ifEmpty { DEFAULT_NICKNAME }
        context.dataStore.edit { preferences ->
            preferences[KEY_NICKNAME] = trimmed
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun setMusicFolder(uri: String?, displayName: String?) {
        context.dataStore.edit { preferences ->
            if (uri != null) {
                preferences[KEY_MUSIC_FOLDER_URI] = uri
            } else {
                preferences.remove(KEY_MUSIC_FOLDER_URI)
            }
            if (displayName != null) {
                preferences[KEY_MUSIC_FOLDER_NAME] = displayName
            } else {
                preferences.remove(KEY_MUSIC_FOLDER_NAME)
            }
        }
    }
}
