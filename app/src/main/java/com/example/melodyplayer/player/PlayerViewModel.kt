package com.example.melodyplayer.player

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.melodyplayer.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PlayerViewModel(app: Application) : AndroidViewModel(app) {
    private val player = ExoPlayer.Builder(app.applicationContext).build()

    private val _playlist = MutableStateFlow<List<Song>>(emptyList())
    val playlist: StateFlow<List<Song>> = _playlist

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private var currentIndex = 0

    fun setPlaylist(list: List<Song>, startIndex: Int = 0) {
        _playlist.value = list
        currentIndex = startIndex.coerceIn(list.indices)
        playSong(currentIndex)
    }

    fun playSong(index: Int) {
        val list = _playlist.value
        if (list.isEmpty() || index < 0 || index >= list.size) return
        val song = list[index]
        _currentSong.value = song
        currentIndex = index

        try {
            if (song.audioUrl.isNotBlank()) {
                val mediaItem = MediaItem.fromUri(Uri.parse(song.audioUrl))
                player.setMediaItem(mediaItem)
                player.prepare()
                player.play()
                _isPlaying.value = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
        } else {
            player.play()
            _isPlaying.value = true
        }
    }

    fun nextSong() {
        if (_playlist.value.isNotEmpty()) {
            val next = if (currentIndex < _playlist.value.lastIndex) currentIndex + 1 else 0
            playSong(next)
        }
    }

    fun prevSong() {
        if (_playlist.value.isNotEmpty()) {
            val prev = if (currentIndex > 0) currentIndex - 1 else _playlist.value.lastIndex
            playSong(prev)
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}
