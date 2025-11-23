package com.example.melodyplayer.data

import com.example.melodyplayer.model.Song
import android.util.Log

class SongRepository(
    private val firestoreRepo: FirestoreRepository = FirestoreRepository()
) {

    /**
     * Lấy toàn bộ bài hát từ Firestore
     */
    suspend fun getAllSongs(): List<Song> {
        return try {
            val list = firestoreRepo.getAllSongs()
            Log.d("SongRepository", "Đã load Firestore: ${list.size} bài")
            list
        } catch (e: Exception) {
            Log.e("SongRepository", "Lỗi load songs: ${e.message}")
            emptyList()
        }
    }

    /**
     * Tìm bài hát (Tự động lấy list từ Firestore rồi lọc)
     * Hàm này dành cho ChatViewModel gọi
     */
    suspend fun searchSongs(keyword: String): List<Song> {
        val allSongs = getAllSongs() // Tải danh sách về trước
        return filterSongs(allSongs, keyword)
    }

    /**
     * Logic lọc bài hát (Tách riêng để dễ test)
     */
    private fun filterSongs(allSongs: List<Song>, keyword: String): List<Song> {
        val query = keyword.trim()
        if (query.isEmpty()) return emptyList()

        return allSongs.filter { song ->
            val title = song.title ?: ""
            val artist = song.artist ?: ""

            title.contains(query, ignoreCase = true) ||
                    artist.contains(query, ignoreCase = true)
        }
    }
}