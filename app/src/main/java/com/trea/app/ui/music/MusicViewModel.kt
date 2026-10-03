package com.trea.app.ui.music

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.trea.app.data.music.MusicPlayerController
import com.trea.app.data.music.MusicPlayerState
import com.trea.app.data.music.MusicScanner
import com.trea.app.data.music.Song
import com.trea.app.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MusicViewModel(
    private val playerController: MusicPlayerController,
    private val musicScanner: MusicScanner,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val playerState: StateFlow<MusicPlayerState> = playerController.state

    init {
        loadSavedFolderAndScan(autoPlay = false)
    }

    fun loadSavedFolderAndScan(autoPlay: Boolean = false) {
        viewModelScope.launch {
            val uriStr = preferencesRepository.musicFolderUriFlow.firstOrNull()
            val folderName = preferencesRepository.musicFolderNameFlow.firstOrNull()
            if (!uriStr.isNullOrEmpty()) {
                val uri = Uri.parse(uriStr)
                playerController.updateFolderInfo(uriStr, folderName)
                scanFolderInternal(uri, autoPlay)
            }
        }
    }

    fun onFolderSelected(uri: Uri, displayName: String?) {
        viewModelScope.launch {
            val uriStr = uri.toString()
            preferencesRepository.setMusicFolder(uriStr, displayName)
            playerController.updateFolderInfo(uriStr, displayName)
            scanFolderInternal(uri, autoPlay = false)
        }
    }

    private fun scanFolderInternal(uri: Uri, autoPlay: Boolean) {
        viewModelScope.launch {
            playerController.setScanning(true)
            try {
                val songs = musicScanner.scanFolder(uri)
                playerController.setPlaylist(songs, startIndex = 0, autoPlay = autoPlay)
            } catch (_: Exception) {
                // Ignore or handle scan errors gracefully
            } finally {
                playerController.setScanning(false)
            }
        }
    }

    fun togglePlay() {
        playerController.togglePlay()
    }

    fun playNext() {
        playerController.playNext()
    }

    fun playPrevious() {
        playerController.playPrevious()
    }

    fun seekTo(positionMs: Long) {
        playerController.seekTo(positionMs)
    }

    fun togglePlayMode() {
        playerController.togglePlayMode()
    }

    fun playSong(song: Song) {
        playerController.playSong(song)
    }

    fun playSongAtIndex(index: Int) {
        playerController.playSongAtIndex(index)
    }

    companion object {
        fun provideFactory(
            playerController: MusicPlayerController,
            musicScanner: MusicScanner,
            preferencesRepository: UserPreferencesRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MusicViewModel(playerController, musicScanner, preferencesRepository) as T
            }
        }
    }
}
