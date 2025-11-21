package com.example.melodyplayer.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Song(
    // --- Thông tin cơ bản ---
    val title: String = "",
    val artist: String = "",
    val duration: Int = 0, // Thời lượng bài hát (tính bằng giây)

    // --- Nguồn nhạc (chỉ cần 1 trong 2) ---
    val resId: String? = null,      // Dùng cho nhạc offline từ thư mục 'raw'
    val audioUrl: String? = null,  // Dùng cho nhạc online từ một đường link

    // --- Ảnh bìa ---
    val imageUrl: String? = null,   // Link ảnh của bài hát

    // --- Lời bài hát (MỚI) ---
    val lyrics: String? = null      // Chuỗi chứa lời bài hát (xuống dòng bằng \n)
)