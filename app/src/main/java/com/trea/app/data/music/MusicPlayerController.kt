package com.trea.app.data.music

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

data class MusicPlayerState(
    val currentSong: Song? = null,
    val currentSongIndex: Int = -1,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playMode: PlayMode = PlayMode.SEQUENTIAL,
    val playlist: List<Song> = emptyList(),
    val isScanning: Boolean = false,
    val scannedFolderUri: String? = null,
    val scannedFolderName: String? = null
)

class MusicPlayerController(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var progressJob: Job? = null

    private val _state = MutableStateFlow(MusicPlayerState())
    val state: StateFlow<MusicPlayerState> = _state.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) {
                startProgressTracker()
            } else {
                stopProgressTracker()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    val duration = mediaController?.duration ?: 0L
                    val safeDuration = if (duration > 0) duration else (_state.value.currentSong?.durationMs ?: 0L)
                    _state.update {
                        it.copy(
                            durationMs = safeDuration,
                            currentPositionMs = mediaController?.currentPosition?.coerceAtLeast(0L) ?: 0L
                        )
                    }
                }
                Player.STATE_ENDED -> {
                    _state.update { it.copy(isPlaying = false) }
                    stopProgressTracker()
                }
                else -> Unit
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val controller = mediaController ?: return
            val currentIndex = controller.currentMediaItemIndex
            val songs = _state.value.playlist
            if (currentIndex in songs.indices) {
                val song = songs[currentIndex]
                _state.update {
                    it.copy(
                        currentSong = song,
                        currentSongIndex = currentIndex,
                        durationMs = if (song.durationMs > 0) song.durationMs else 0L,
                        currentPositionMs = 0L
                    )
                }
            }
        }
    }

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                mediaController?.addListener(playerListener)
                syncStateFromController()
            } catch (_: Exception) {
                // Service may not be ready or failed to bind
            }
        }, { r -> scope.launch(Dispatchers.Main) { r.run() } })
    }

    private fun syncStateFromController() {
        val controller = mediaController ?: return
        val isPlaying = controller.isPlaying
        val pos = controller.currentPosition.coerceAtLeast(0L)
        val dur = controller.duration.coerceAtLeast(0L)
        _state.update {
            it.copy(
                isPlaying = isPlaying,
                currentPositionMs = pos,
                durationMs = if (dur > 0) dur else it.durationMs
            )
        }
        if (isPlaying) {
            startProgressTracker()
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch(Dispatchers.Main) {
            while (isActive) {
                val controller = mediaController
                if (controller != null && controller.isPlaying) {
                    val pos = controller.currentPosition.coerceAtLeast(0L)
                    val dur = controller.duration.coerceAtLeast(0L)
                    _state.update {
                        it.copy(
                            currentPositionMs = pos,
                            durationMs = if (dur > 0) dur else it.durationMs
                        )
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    @OptIn(UnstableApi::class)
    fun setPlaylist(songs: List<Song>, startIndex: Int = 0, autoPlay: Boolean = false) {
        val controller = mediaController ?: return
        _state.update {
            it.copy(
                playlist = songs,
                currentSongIndex = if (startIndex in songs.indices) startIndex else -1,
                currentSong = if (startIndex in songs.indices) songs[startIndex] else null
            )
        }

        val mediaItems = songs.map { song ->
            val metadataBuilder = MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)

            song.coverPath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    metadataBuilder.setArtworkUri(Uri.fromFile(file))
                }
            }

            MediaItem.Builder()
                .setUri(Uri.parse(song.uriString))
                .setMediaId(song.id)
                .setMediaMetadata(metadataBuilder.build())
                .build()
        }

        controller.setMediaItems(mediaItems, if (startIndex in mediaItems.indices) startIndex else 0, 0L)
        controller.prepare()

        applyPlayModeToController(_state.value.playMode)

        if (autoPlay && mediaItems.isNotEmpty()) {
            controller.play()
        }
    }

    fun playSong(song: Song) {
        val index = _state.value.playlist.indexOfFirst { it.id == song.id }
        if (index != -1) {
            playSongAtIndex(index)
        }
    }

    fun playSongAtIndex(index: Int) {
        val controller = mediaController ?: return
        if (index in _state.value.playlist.indices) {
            controller.seekTo(index, 0L)
            controller.play()
            _state.update {
                it.copy(
                    currentSongIndex = index,
                    currentSong = it.playlist[index]
                )
            }
        }
    }

    fun togglePlay() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            if (controller.playbackState == Player.STATE_ENDED) {
                controller.seekTo(0, 0L)
            }
            controller.play()
        }
    }

    fun playNext() {
        val controller = mediaController ?: return
        if (controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
            controller.play()
        } else if (_state.value.playlist.isNotEmpty()) {
            // Loop back to start
            controller.seekTo(0, 0L)
            controller.play()
        }
    }

    fun playPrevious() {
        val controller = mediaController ?: return
        if (controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
            controller.play()
        } else if (_state.value.playlist.isNotEmpty()) {
            controller.seekTo(0, 0L)
            controller.play()
        }
    }

    fun seekTo(positionMs: Long) {
        val controller = mediaController ?: return
        val clamped = positionMs.coerceIn(0L, _state.value.durationMs.coerceAtLeast(0L))
        controller.seekTo(clamped)
        _state.update { it.copy(currentPositionMs = clamped) }
    }

    fun togglePlayMode() {
        val nextMode = _state.value.playMode.next()
        _state.update { it.copy(playMode = nextMode) }
        applyPlayModeToController(nextMode)
    }

    private fun applyPlayModeToController(mode: PlayMode) {
        val controller = mediaController ?: return
        when (mode) {
            PlayMode.SEQUENTIAL -> {
                controller.repeatMode = Player.REPEAT_MODE_ALL
                controller.shuffleModeEnabled = false
            }
            PlayMode.SINGLE_LOOP -> {
                controller.repeatMode = Player.REPEAT_MODE_ONE
                controller.shuffleModeEnabled = false
            }
            PlayMode.SHUFFLE -> {
                controller.repeatMode = Player.REPEAT_MODE_ALL
                controller.shuffleModeEnabled = true
            }
        }
    }

    fun updateFolderInfo(uriStr: String?, name: String?) {
        _state.update {
            it.copy(
                scannedFolderUri = uriStr,
                scannedFolderName = name
            )
        }
    }

    fun setScanning(scanning: Boolean) {
        _state.update { it.copy(isScanning = scanning) }
    }

    fun release() {
        stopProgressTracker()
        mediaController?.removeListener(playerListener)
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        mediaController = null
        controllerFuture = null
    }
}
