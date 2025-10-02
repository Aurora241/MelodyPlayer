package com.example.melodyplayer.model

data class Song(
    val title: String = "",
    val artist: String = "",
    val coverUrl: String? = null,   // ảnh online (nếu có)
    val coverResId: Int? = null,    // ảnh trong drawable (icon mặc định)
    val audioUrl: String = "",      // link mp3
    val duration: Int = 0           // thời lượng (giây)
)
