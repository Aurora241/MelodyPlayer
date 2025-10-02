package com.example.melodyplayer.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onSongClick: (Song) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }

    // 🔹 Lấy dữ liệu Firestore
    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val snapshot = db.collection("songs").get().await()
        songs = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Song::class.java)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Melody Player", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { /* mở menu nếu cần */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("auth") {
                            popUpTo("home") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Đăng xuất", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF121212)
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF121212)) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Trang chủ") },
                    label = { Text("Trang chủ") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.QueueMusic, contentDescription = "Playlist") },
                    label = { Text("Playlist") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Cài đặt") },
                    label = { Text("Cài đặt") }
                )
            }
        },
        containerColor = Color.Black
    ) { padding ->
        when (selectedTab) {
            0 -> HomeContent(songs, onSongClick, padding)
            1 -> PlaylistContent(padding)
            2 -> SettingsContent(padding, navController)
        }
    }
}

@Composable
fun HomeContent(songs: List<Song>, onSongClick: (Song) -> Unit, padding: PaddingValues) {
    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Text(
            "Playlist nổi bật",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            items(songs) { song ->
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E1E1E))
                        .clickable { onSongClick(song) },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = song.coverUrl,
                        contentDescription = song.title,
                        modifier = Modifier.size(120.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Gợi ý cho bạn",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(12.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(songs) { song ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E1E1E))
                        .clickable { onSongClick(song) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = song.coverUrl,
                        contentDescription = song.title,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(song.title, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(song.artist, color = Color.Gray, fontSize = 14.sp)
                    }

                    IconButton(onClick = { onSongClick(song) }) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistContent(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text("Playlist của bạn sẽ hiển thị ở đây 🎶", color = Color.White)
    }
}

@Composable
fun SettingsContent(padding: PaddingValues, navController: NavController) {
    Box(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Cài đặt ứng dụng ⚙️", color = Color.White)

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("auth") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
            ) {
                Text("Đăng xuất", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
