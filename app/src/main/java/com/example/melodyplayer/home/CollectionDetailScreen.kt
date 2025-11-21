package com.example.melodyplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.melodyplayer.model.Song
import com.example.melodyplayer.navigation.Routes
import com.example.melodyplayer.player.PlayerViewModel
import kotlinx.coroutines.launch

@Composable
fun CollectionDetailScreen(
    navController: NavController,
    title: String,
    songs: List<Song>,
    playerVM: PlayerViewModel
) {
    var showDeleteDialog by remember { mutableStateOf<Song?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            // ✅ Thanh tiêu đề với gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1DB954),
                                Color(0xFF169C46)
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        }
    ) { padding ->

        // ✅ Nếu không có bài hát
        if (songs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Không có bài hát nào trong bộ sưu tập này",
                    color = Color.White.copy(0.8f),
                    fontSize = 16.sp
                )
            }
        } else {
            // ✅ Danh sách bài hát
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFF121212))
            ) {
                items(songs) { song ->
                    var showMenu by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable {
                                // ✅ Phát bài hát khi người dùng chọn
                                playerVM.setPlaylist(songs, songs.indexOf(song))
                                navController.navigate(Routes.PLAYER)
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C)),
                        shape = MaterialTheme.shapes.medium,
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = song.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = song.artist,
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 13.sp
                                )
                            }

                            // ✅ Menu 3 chấm
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "Menu",
                                        tint = Color.Gray
                                    )
                                }

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                    modifier = Modifier.background(Color(0xFF282828))
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    tint = Color.White
                                                )
                                                Spacer(Modifier.width(16.dp))
                                                Text("Phát ngay", color = Color.White)
                                            }
                                        },
                                        onClick = {
                                            showMenu = false
                                            playerVM.setPlaylist(songs, songs.indexOf(song))
                                            navController.navigate(Routes.PLAYER)
                                        }
                                    )

                                    Divider(color = Color.Gray.copy(0.2f))

                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFF5252)
                                                )
                                                Spacer(Modifier.width(16.dp))
                                                Text("Xóa khỏi bộ sưu tập", color = Color(0xFFFF5252))
                                            }
                                        },
                                        onClick = {
                                            showMenu = false
                                            showDeleteDialog = song
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(60.dp)) }
            }
        }
    }

    // ✅ Dialog xác nhận xóa bài hát
    showDeleteDialog?.let { song ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            containerColor = Color(0xFF282828),
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    "Xóa bài hát?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    "Bạn có chắc muốn xóa \"${song.title}\" khỏi \"$title\"?",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            playerVM.removeSongFromCollection(song, title)
                            showDeleteDialog = null
                            // ✅ Không cần popBackStack vì UI tự refresh
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5252)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Xóa", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = null },
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Hủy", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}