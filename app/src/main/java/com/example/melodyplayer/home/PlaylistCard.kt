package com.example.melodyplayer.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.melodyplayer.model.Song

@Composable
fun PlaylistCard(
    song: Song,
    onSongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(width = 160.dp, height = 200.dp)
            .clickable { onSongClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(song.title, color = Color.White, fontSize = 16.sp, maxLines = 1)
            Text(song.artist, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
        }
    }
}
