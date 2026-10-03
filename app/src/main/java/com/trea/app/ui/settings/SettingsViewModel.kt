package com.trea.app.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.trea.app.data.music.MusicPlayerController
import com.trea.app.data.music.MusicScanner
import com.trea.app.data.preferences.ThemeMode
import com.trea.app.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val nickname: String = UserPreferencesRepository.DEFAULT_NICKNAME,
    val themeMode: ThemeMode = ThemeMode.DYNAMIC,
    val musicFolderUri: String? = null,
    val musicFolderName: String? = null
)

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository,
    private val playerController: MusicPlayerController,
    private val musicScanner: MusicScanner
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.nicknameFlow,
        preferencesRepository.themeModeFlow,
        preferencesRepository.musicFolderUriFlow,
        preferencesRepository.musicFolderNameFlow
    ) { nickname, themeMode, musicFolderUri, musicFolderName ->
        SettingsUiState(
            nickname = nickname,
            themeMode = themeMode,
            musicFolderUri = musicFolderUri,
            musicFolderName = musicFolderName
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setNickname(nickname: String) {
        viewModelScope.launch {
            preferencesRepository.setNickname(nickname)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setMusicFolder(uri: Uri, displayName: String?) {
        viewModelScope.launch {
            val uriStr = uri.toString()
            preferencesRepository.setMusicFolder(uriStr, displayName)
            playerController.updateFolderInfo(uriStr, displayName)

            // Rescan folder
            playerController.setScanning(true)
            try {
                val songs = musicScanner.scanFolder(uri)
                playerController.setPlaylist(songs, startIndex = 0, autoPlay = false)
            } catch (_: Exception) {
            } finally {
                playerController.setScanning(false)
            }
        }
    }

    companion object {
        fun provideFactory(
            preferencesRepository: UserPreferencesRepository,
            playerController: MusicPlayerController,
            musicScanner: MusicScanner
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(preferencesRepository, playerController, musicScanner) as T
            }
        }
    }
}
