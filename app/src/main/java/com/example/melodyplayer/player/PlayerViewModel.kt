package com.example.melodyplayer.player

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.melodyplayer.model.Song
import com.example.melodyplayer.player.service.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await
import kotlin.math.max

@OptIn(UnstableApi::class)
class PlayerViewModel(app: Application) : AndroidViewModel(app) {

    private val TAG = "PlayerViewModel"
    private val context = app.applicationContext
    private val controllerFuture: ListenableFuture<MediaController>
    private var controller: MediaController? = null
    private var positionJob: Job? = null

    private val mediaIdToSong = mutableMapOf<String, Song>()

    private val _playlist = MutableStateFlow<List<Song>>(emptyList())
    val playlist: StateFlow<List<Song>> = _playlist.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> = _playbackPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _bufferedPosition = MutableStateFlow(0L)
    val bufferedPosition: StateFlow<Long> = _bufferedPosition.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _playbackError = MutableStateFlow<String?>(null)
    val playbackError: StateFlow<String?> = _playbackError.asStateFlow()

    fun clearError() {
        _playbackError.value = null
    }

    init {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                try {
                    val controllerInstance = controllerFuture.get()
                    setupController(controllerInstance)
                } catch (e: Exception) {
                    Log.e(TAG, "Không thể khởi tạo MediaController", e)
                    _playbackError.value = "Không thể kết nối đến trình phát"
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) {
            _playbackError.value = "Danh sách phát trống"
            return
        }

        viewModelScope.launch {
            val controller = awaitController() ?: return@launch
            ensureServiceRunning()

            val safeIndex = startIndex.coerceIn(0, songs.lastIndex)

            try {
                val mediaItems = buildMediaItems(songs)

                controller.setMediaItems(mediaItems, safeIndex, 0L)
                controller.prepare()
                controller.play()

                _playlist.value = songs
                _currentSong.value = songs[safeIndex]
                _playbackError.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi thiết lập danh sách phát: ${e.message}", e)
                _playbackError.value = "Lỗi: ${e.message}"
            }
        }
    }

    fun playSong(index: Int) {
        val songs = _playlist.value
        if (songs.isEmpty() || index !in songs.indices) return

        viewModelScope.launch {
            val controller = awaitController() ?: return@launch
            ensureServiceRunning()
            try {
                controller.seekTo(index, 0L)
                controller.play()
                _currentSong.value = songs[index]
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi chuyển bài", e)
                _playbackError.value = "Không thể chuyển bài hát"
            }
        }
    }

    fun togglePlayPause() {
        controller?.let {
            try {
                if (it.isPlaying) it.pause() else it.play()
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi play/pause", e)
            }
        }
    }

    fun nextSong() {
        controller?.let {
            try {
                it.seekToNextMediaItem()
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi next", e)
            }
        }
    }

    fun prevSong() {
        controller?.let {
            try {
                it.seekToPreviousMediaItem()
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi prev", e)
            }
        }
    }

    fun seekTo(positionMs: Long) {
        controller?.let {
            try {
                it.seekTo(positionMs)
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi seek", e)
            }
        }
    }

    fun toggleShuffle() {
        controller?.let {
            try {
                val enabled = !it.shuffleModeEnabled
                it.shuffleModeEnabled = enabled
                _shuffleEnabled.value = enabled
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi toggle shuffle", e)
            }
        }
    }

    fun cycleRepeatMode() {
        controller?.let {
            try {
                val newMode = when (it.repeatMode) {
                    Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                    Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                    else -> Player.REPEAT_MODE_OFF
                }
                it.repeatMode = newMode
                _repeatMode.value = newMode
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi cycle repeat", e)
            }
        }
    }

    private suspend fun awaitController(): MediaController? {
        controller?.let { return it }
        return try {
            val controllerInstance = controllerFuture.await()
            if (controller == null) setupController(controllerInstance)
            controllerInstance
        } catch (e: Exception) {
            Log.e(TAG, "Không thể kết nối MediaController", e)
            _playbackError.value = "Không thể kết nối đến trình phát"
            null
        }
    }

    private fun setupController(controllerInstance: MediaController) {
        controller = controllerInstance
        _isPlaying.value = controllerInstance.isPlaying
        _shuffleEnabled.value = controllerInstance.shuffleModeEnabled
        _repeatMode.value = controllerInstance.repeatMode

        controllerInstance.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_IDLE && controllerInstance.playerError != null) {
                    onPlayerError(controllerInstance.playerError!!)
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val song = mediaItem?.mediaId?.let { mediaIdToSong[it] }
                _currentSong.value = song
            }

            override fun onPlayerError(error: PlaybackException) {
                onPlayerError(error)
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _shuffleEnabled.value = shuffleModeEnabled
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                _repeatMode.value = repeatMode
            }
        })

        positionJob?.cancel()
        positionJob = viewModelScope.launch {
            while (isActive) {
                val duration = controllerInstance.duration
                val buffered = controllerInstance.bufferedPosition
                _playbackPosition.value = max(0L, controllerInstance.currentPosition)
                _duration.value = if (duration != C.TIME_UNSET && duration > 0) duration else 0L
                _bufferedPosition.value = if (buffered != C.TIME_UNSET && buffered > 0) buffered else 0L
                delay(500)
            }
        }
    }

    private fun onPlayerError(error: PlaybackException) {
        Log.e(TAG, "Lỗi phát nhạc: ${error.message}", error)
        _playbackError.value = error.localizedMessage ?: "Đã xảy ra lỗi khi phát nhạc"
    }

    private fun ensureServiceRunning() {
        try {
            val intent = Intent(context, PlaybackService::class.java)
            ContextCompat.startForegroundService(context, intent)
        } catch (e: Exception) {
            Log.w(TAG, "Không thể khởi chạy service phát nhạc", e)
        }
    }

    /**
     * Xây dựng MediaItems từ raw resources
     * audioUrl phải là tên file không có đuôi .mp3 (vd: "bon_chu_lam")
     */
    /**
     * Xây dựng MediaItems từ Firestore hoặc từ raw resources
     * Ưu tiên phát từ raw nếu có, fallback sang audioUrl online nếu có
     */
    private fun buildMediaItems(songs: List<Song>): List<MediaItem> {
        mediaIdToSong.clear()

        return songs.mapIndexedNotNull { index, song ->
            try {
                val resId = when {
                    // Nếu có resId trong model
                    song.resId != null -> song.resId

                    // Nếu có audioUrl (dạng tên file không đuôi)
                    !song.audioUrl.isNullOrBlank() -> {
                        val name = song.audioUrl!!.substringBefore(".").lowercase()
                        context.resources.getIdentifier(name, "raw", context.packageName)
                            .takeIf { it != 0 }
                    }

                    else -> null
                }

                val uri = when {
                    resId != null -> Uri.parse("android.resource://${context.packageName}/$resId")
                    !song.audioUrl.isNullOrBlank() && song.audioUrl!!.startsWith("http") -> Uri.parse(song.audioUrl)
                    else -> null
                }

                if (uri == null) {
                    Log.e(TAG, "❌ Bỏ qua bài hát '${song.title}' — không tìm thấy file hoặc URL hợp lệ")
                    return@mapIndexedNotNull null
                }

                val metadata = MediaMetadata.Builder()
                    .setTitle(song.title.ifBlank { "Không rõ tên bài" })
                    .setArtist(song.artist.ifBlank { "Không rõ ca sĩ" })
                    .setAlbumTitle("Melody Player")
                    .build()

                val mediaItem = MediaItem.Builder()
                    .setMediaId("item-$index-${song.title}")
                    .setUri(uri)
                    .setMediaMetadata(metadata)
                    .build()

                mediaIdToSong[mediaItem.mediaId] = song
                Log.d(TAG, "✅ Tải thành công: ${song.title} (${uri})")

                mediaItem
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi build MediaItem cho '${song.title}': ${e.message}")
                null
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        positionJob?.cancel()
        controller?.release()
        controller = null
        controllerFuture.cancel(true)
    }
}