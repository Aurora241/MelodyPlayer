package com.example.melodyplayer.player // Hoặc package phù hợp

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.melodyplayer.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "favorites_storage")
private val FAVORITE_SONGS_KEY = stringSetPreferencesKey("favorite_songs_set")

class FavoritesDataStore(context: Context) {

    private val appContext = context.applicationContext

    // Lấy danh sách các bài hát yêu thích (dưới dạng Set<String>)
    val favoriteSongs: Flow<Set<String>> = appContext.dataStore.data.map { preferences ->
        preferences[FAVORITE_SONGS_KEY] ?: emptySet()
    }

    // Thêm một bài hát vào danh sách yêu thích
    suspend fun addFavorite(song: Song) {
        appContext.dataStore.edit { preferences ->
            val currentFavorites = preferences[FAVORITE_SONGS_KEY] ?: emptySet()
            preferences[FAVORITE_SONGS_KEY] = currentFavorites + createSongKey(song)
        }
    }

    // Xóa một bài hát khỏi danh sách yêu thích
    suspend fun removeFavorite(song: Song) {
        appContext.dataStore.edit { preferences ->
            val currentFavorites = preferences[FAVORITE_SONGS_KEY] ?: emptySet()
            preferences[FAVORITE_SONGS_KEY] = currentFavorites - createSongKey(song)
        }
    }

    // Tạo một key duy nhất cho mỗi bài hát để lưu trữ
    private fun createSongKey(song: Song): String {
        // Kết hợp title và artist để tạo key, tránh trùng lặp
        return "${song.title}||${song.artist}"
    }
}