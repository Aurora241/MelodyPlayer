package com.example.melodyplayer.ui.screens

import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.melodyplayer.model.Song
import com.example.melodyplayer.player.PlayerViewModel
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    navController: NavController,
    playerVM: PlayerViewModel,
    title: String,
    songsJson: String
) {
    // 🔹 Parse danh sách bài hát ban đầu
    val initialSongs = remember {
        try {
            Json.decodeFromString<List<Song>>(songsJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    val songs = remember { mutableStateListOf(*initialSongs.toTypedArray()) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(title, fontWeight = FontWeight.Bold, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.MusicNote, null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Thêm bài hát",
                            tint = Color(0xFF1DB954)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D0F1F))
            )
        },
        containerColor = Color(0xFF0D0F1F)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFF0D0F1F)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(songs) { song ->
                SongItem(
                    song = song,
                    onPlay = {
                        playerVM.setPlaylist(songs, songs.indexOf(song))
                        navController.navigate("player")
                    },
                    onDelete = {
                        songs.remove(song)
                    }
                )
            }
        }
    }

    // 🔹 Hộp thoại thêm bài hát có sẵn trong app
    if (showAddDialog) {
        AddExistingSongDialog(
            allSongs = playerVM.playlist.collectAsState().value,
            currentSongs = songs,
            onDismiss = { showAddDialog = false },
            onAdd = { selectedSong ->
                if (!songs.contains(selectedSong)) songs.add(selectedSong)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SongItem(
    song: Song,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F3A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1DB954), Color(0xFF127A3D))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = song.imageUrl ?: "",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White.copy(0.8f)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(song.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(song.artist, color = Color.Gray, fontSize = 13.sp)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = Color(0xFFFF5252))
            }
        }
    }
}

@Composable
fun AddExistingSongDialog(
    allSongs: List<Song>,
    currentSongs: List<Song>,
    onDismiss: () -> Unit,
    onAdd: (Song) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1F3A),
        title = {
            Text("Chọn bài hát có sẵn", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            if (allSongs.isEmpty()) {
                Text("Chưa có bài hát nào trong ứng dụng.", color = Color.Gray)
            } else {
                LazyColumn(modifier = Modifier.height(400.dp)) {
                    items(allSongs) { song ->
                        val isAdded = currentSongs.contains(song)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isAdded) { onAdd(song) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.MusicNote,
                                null,
                                tint = if (isAdded) Color.Gray else Color(0xFF1DB954)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(song.title, color = Color.White)
                                Text(song.artist, color = Color.Gray, fontSize = 12.sp)
                            }
                            if (isAdded) Text("✓", color = Color(0xFF1DB954))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", color = Color.LightGray)
            }
        }
    )
}
