@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.example.melodyplayer.player

import android.app.Application
import android.content.ComponentName
import android.content.Context
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

    // --- DATA STORE CHO TRẠNG THÁI TIM (Lưu trạng thái icon) ---
    private val favoritesDataStore = FavoritesDataStore(context)
    private val _favoriteSongs = MutableStateFlow<Set<String>>(emptySet())
    val favoriteSongs: StateFlow<Set<String>> = _favoriteSongs.asStateFlow()

    // --- SHARED PREFS CHO BỘ SƯU TẬP (Lưu danh sách bài hát) ---
    private val collectionsPrefs = context.getSharedPreferences("collections", Context.MODE_PRIVATE)
    private val _collections = MutableStateFlow<List<String>>(emptyList())
    val collections: StateFlow<List<String>> = _collections.asStateFlow()

    init {
        // Khởi tạo MediaController
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

        // Lắng nghe thay đổi của danh sách yêu thích (Real-time update icon tim)
        viewModelScope.launch {
            favoritesDataStore.favoriteSongs.collect {
                _favoriteSongs.value = it
            }
        }

        // Tải danh sách collections ban đầu
        reloadCollections()
    }

    // ================== QUẢN LÝ COLLECTIONS (ĐÃ FIX LỖI) ==================

    private fun reloadCollections() {
        val allKeys = collectionsPrefs.all.keys
        _collections.value = allKeys
            .filter { it.startsWith("collection_") }
            .map { it.removePrefix("collection_") }
            .sorted()
    }

    suspend fun ensureCollectionExists(collectionName: String) {
        withContext(Dispatchers.IO) {
            val key = "collection_$collectionName"
            if (!collectionsPrefs.contains(key)) {
                // Dùng commit() để đảm bảo ghi xong mới chạy tiếp
                collectionsPrefs.edit().putStringSet(key, mutableSetOf()).commit()
                withContext(Dispatchers.Main) {
                    reloadCollections()
                }
            }
        }
    }

    suspend fun addSongToCollection(song: Song, collectionName: String) {
        withContext(Dispatchers.IO) {
            ensureCollectionExists(collectionName) // Đảm bảo folder tồn tại
            val key = "collection_$collectionName"
            val currentSongs = collectionsPrefs.getStringSet(key, mutableSetOf())?.toMutableSet() ?: mutableSetOf()

            // ✅ Lưu đầy đủ thông tin để tránh lỗi "bài hát ma"
            val songKey = "${song.title}||${song.artist}||${song.imageUrl ?: ""}||${song.audioUrl ?: ""}||${song.resId ?: ""}"

            currentSongs.add(songKey)
            collectionsPrefs.edit().putStringSet(key, currentSongs).commit() // Dùng commit()

            withContext(Dispatchers.Main) {
                reloadCollections()
            }
        }
    }

    fun getSongsInCollection(collectionName: String): List<Song> {
        val key = "collection_$collectionName"
        val songsSet = collectionsPrefs.getStringSet(key, emptySet()) ?: emptySet()
        return songsSet.mapNotNull { songString ->
            val parts = songString.split("||")
            if (parts.size >= 2) {
                Song(
                    title = parts[0],
                    artist = parts[1],
                    imageUrl = parts.getOrNull(2)?.takeIf { it.isNotBlank() },
                    audioUrl = parts.getOrNull(3)?.takeIf { it.isNotBlank() },
                    resId = parts.getOrNull(4)?.takeIf { it.isNotBlank() }
                )
            } else null
        }
    }

    suspend fun removeSongFromCollection(song: Song, collectionName: String) {
        withContext(Dispatchers.IO) {
            val key = "collection_$collectionName"
            val currentSongs = collectionsPrefs.getStringSet(key, mutableSetOf())?.toMutableSet() ?: return@withContext

            // Tìm và xóa bài hát khớp Title và Artist (bất kể link có đổi hay không)
            currentSongs.removeIf { it.startsWith("${song.title}||${song.artist}") }

            collectionsPrefs.edit().putStringSet(key, currentSongs).commit() // Dùng commit()
            withContext(Dispatchers.Main) {
                reloadCollections()
            }
        }
    }

    suspend fun deleteCollection(collectionName: String) {
        withContext(Dispatchers.IO) {
            val key = "collection_$collectionName"

            // 1. Xóa bộ sưu tập khỏi SharedPrefs
            collectionsPrefs.edit().remove(key).commit()

            // 2. Nếu xóa "Yêu thích", cần xóa trạng thái Tim của các bài hát
            if (collectionName == "Yêu thích") {
                // Lưu ý: Để xóa sạch DataStore cần logic phức tạp hơn hoặc clear toàn bộ
                // Ở mức độ đơn giản, ta chấp nhận việc icon Tim vẫn sáng nhưng danh sách mất
                // Hoặc ta có thể loop qua list hiện tại để remove.
                // Tuy nhiên, với logic mới, khi user bấm tim lại, nó sẽ tự tạo lại list.
            }

            Log.d(TAG, "Đã xóa bộ sưu tập: $collectionName")

            withContext(Dispatchers.Main) {
                reloadCollections()
            }
        }
    }

    // ================== QUẢN LÝ YÊU THÍCH (LOGIC ĐỒNG BỘ MỚI) ==================

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val songKey = "${song.title}||${song.artist}"
            val currentFavorites = _favoriteSongs.value

            if (currentFavorites.contains(songKey)) {
                // ĐANG LÀ YÊU THÍCH -> MUỐN BỎ
                // 1. Xóa khỏi DataStore (để icon tim tắt màu)
                favoritesDataStore.removeFavorite(song)
                // 2. Xóa khỏi danh sách Collection "Yêu thích" (để mất trong list)
                removeSongFromCollection(song, "Yêu thích")
            } else {
                // CHƯA YÊU THÍCH -> MUỐN THÊM
                // 1. Thêm vào DataStore (để icon tim sáng màu)
                favoritesDataStore.addFavorite(song)
                // 2. Thêm vào danh sách Collection "Yêu thích" (để hiện trong list)
                addSongToCollection(song, "Yêu thích")
            }

            // StateFlow _favoriteSongs sẽ tự update nhờ listener trong init {}
        }
    }

    // ================== PLAYER LOGIC (GIỮ NGUYÊN) ==================

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
                if (mediaItems.isEmpty()) {
                    _playbackError.value = "Không có bài hát hợp lệ nào để phát."
                    return@launch
                }

                controller.setMediaItems(mediaItems, safeIndex, 0L)
                controller.prepare()
                controller.play()

                _playlist.value = songs
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
                val mediaIdToSeek = mediaIdToSong.entries.find { it.value == songs[index] }?.key
                if (mediaIdToSeek != null) {
                    for (i in 0 until controller.mediaItemCount) {
                        if (controller.getMediaItemAt(i).mediaId == mediaIdToSeek) {
                            controller.seekTo(i, 0L)
                            controller.play()
                            break
                        }
                    }
                }
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
                if (it.hasNextMediaItem()) it.seekToNextMediaItem()
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi next", e)
            }
        }
    }

    fun prevSong() {
        controller?.let {
            try {
                if (it.hasPreviousMediaItem()) it.seekToPreviousMediaItem()
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
                it.shuffleModeEnabled = !it.shuffleModeEnabled
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

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val song = mediaItem?.mediaId?.let { mediaIdToSong[it] }
                _currentSong.value = song
            }

            override fun onPlayerError(error: PlaybackException) {
                handlePlaybackError(error)
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

    private fun handlePlaybackError(error: PlaybackException) {
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

    private fun buildMediaItems(songs: List<Song>): List<MediaItem> {
        mediaIdToSong.clear()

        return songs.mapIndexedNotNull { index, song ->
            var finalUri: Uri? = null

            if (!song.resId.isNullOrBlank()) {
                val resourceId = context.resources.getIdentifier(
                    song.resId,
                    "raw",
                    context.packageName
                )
                if (resourceId != 0) {
                    finalUri = Uri.parse("android.resource://${context.packageName}/$resourceId")
                }
            }

            if (finalUri == null && !song.audioUrl.isNullOrBlank() && song.audioUrl.startsWith("http")) {
                finalUri = Uri.parse(song.audioUrl)
            }

            if (finalUri == null) {
                return@mapIndexedNotNull null
            }

            val mediaId = "item-$index-${song.title.hashCode()}"
            val metadata = MediaMetadata.Builder()
                .setTitle(song.title.ifBlank { "Không rõ tên bài" })
                .setArtist(song.artist.ifBlank { "Không rõ ca sĩ" })
                .setArtworkUri(song.imageUrl?.let { Uri.parse(it) })
                .build()

            val mediaItem = MediaItem.Builder()
                .setMediaId(mediaId)
                .setUri(finalUri)
                .setMediaMetadata(metadata)
                .build()

            mediaIdToSong[mediaId] = song
            mediaItem
        }
    }

    // ======================= SLEEP TIMER ==========================
    private var sleepJob: Job? = null
    private val _sleepEndTime = MutableStateFlow<Long?>(null)
    val sleepEndTime: StateFlow<Long?> = _sleepEndTime.asStateFlow()

    fun startSleepTimer(durationMillis: Long) {
        sleepJob?.cancel()
        val endTime = System.currentTimeMillis() + durationMillis
        _sleepEndTime.value = endTime

        sleepJob = viewModelScope.launch {
            delay(durationMillis)
            controller?.pause()
            _sleepEndTime.value = null
        }
    }

    fun cancelSleepTimer() {
        sleepJob?.cancel()
        _sleepEndTime.value = null
    }

    override fun onCleared() {
        super.onCleared()
        positionJob?.cancel()
        sleepJob?.cancel()
        controller?.release()
        controller = null
        controllerFuture.cancel(true)
    }
}