package com.example.melodyplayer.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.melodyplayer.navigation.Routes
import com.example.melodyplayer.model.Song
import com.example.melodyplayer.player.MiniPlayer
import com.example.melodyplayer.player.PlayerViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.melodyplayer.player.MiniPlayer


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    playerVM: PlayerViewModel
) {
    var selectedTab by remember { mutableStateOf(0) }
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val currentSong by playerVM.currentSong.collectAsState()
    val isPlaying by playerVM.isPlaying.collectAsState()

    // Load Firestore
    LaunchedEffect(Unit) {
        try {
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection("songs").get().await()
            songs = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Song::class.java)
            }
        } finally {
            isLoading = false
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it; scope.launch { drawerState.close() } },
                navController = navController
            )
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Melody Player", fontSize = 20.sp, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Routes.SEARCH) }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF0D0D0D)
                    )
                )
            },
            containerColor = Color(0xFF0D0D0D)
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (selectedTab) {
                    0 -> HomeScreenUI(
                        songs = songs,
                        isLoading = isLoading,
                        navController = navController,
                        playerVM = playerVM
                    )
                    1 -> PlaylistContent()
                    2 -> SettingsContent(navController)
                }

                // MiniPlayer
                if (currentSong != null) {
                    MiniPlayer(
                        song = currentSong,
                        isPlaying = isPlaying,
                        onPlayPause = { playerVM.togglePlayPause() },
                        onNext = { playerVM.nextSong() },
                        onPrev = { playerVM.prevSong() },
                        onClick = { navController.navigate(Routes.PLAYER) },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreenUI(
    songs: List<Song>,
    isLoading: Boolean,
    navController: NavController,
    playerVM: PlayerViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Playlist nổi bật
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Playlist nổi bật", color = Color.White, fontSize = 22.sp)
                TextButton(onClick = { navController.navigate(Routes.PLAYLIST_ALL) }) {
                    Text("Xem tất cả", color = Color(0xFF1DB954))
                }
            }
        }

        item {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF1DB954))
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(songs.take(10)) { song ->
                        PlaylistCard(
                            song = song,
                            onSongClick = {
                                playerVM.setPlaylist(songs, songs.indexOf(song))
                            }
                        )
                    }
                }
            }
        }

        // Danh sách gợi ý
        items(songs) { song ->
            SongListItem(
                song = song,
                onSongClick = {
                    playerVM.setPlaylist(songs, songs.indexOf(song))
                }
            )
        }
    }
}
