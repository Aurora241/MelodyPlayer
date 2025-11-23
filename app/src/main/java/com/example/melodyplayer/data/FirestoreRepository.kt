package com.example.melodyplayer.data

import android.net.Uri
import android.util.Log
import com.example.melodyplayer.model.Song
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // 1. Lấy danh sách bài hát (Code cũ của bạn)
    suspend fun getAllSongs(): List<Song> {
        return try {
            val snap = db.collection("songs").get().await()
            snap.documents.mapNotNull { it.toObject(Song::class.java) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 2. [MỚI] Upload file (nhạc hoặc ảnh) lên Storage -> Trả về link URL
    suspend fun uploadFileToStorage(uri: Uri, folderName: String): String? {
        return try {
            // Tạo tên file ngẫu nhiên để không bị trùng
            val fileName = "${UUID.randomUUID()}"
            // Tạo đường dẫn: ví dụ "music/abc-xyz-123"
            val storageRef = storage.reference.child("$folderName/$fileName")

            // Bắt đầu upload
            storageRef.putFile(uri).await()

            // Upload xong -> Lấy đường dẫn tải về (Download URL)
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Lỗi upload: ${e.message}")
            null
        }
    }

    // 3. [MỚI] Lưu thông tin bài hát vào Firestore
    suspend fun addSongToFirestore(song: Song): Boolean {
        return try {
            // Tạo document mới trong collection "songs"
            db.collection("songs")
                .add(song)
                .await()
            true // Thành công
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Lỗi lưu DB: ${e.message}")
            false // Thất bại
        }
    }
}