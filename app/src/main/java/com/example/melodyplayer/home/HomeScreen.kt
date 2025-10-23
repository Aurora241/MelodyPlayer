package com.example.melodyplayer.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.melodyplayer.model.Song
import com.example.melodyplayer.navigation.Routes
import com.example.melodyplayer.player.MiniPlayer
import com.example.melodyplayer.player.PlayerViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.Normalizer
import java.util.*

private val Context.dataStore by preferencesDataStore(name = "user_added_songs_storage")
private val LOCAL_SONGS_KEY = stringPreferencesKey("local_songs_list_json")

// Màu sắc gradient đẹp mắt
private val ColorPalette = listOf(
    listOf(Color(0xFFFF6B9D), Color(0xFFC44569)),
    listOf(Color(0xFF4FACFE), Color(0xFF00F2FE)),
    listOf(Color(0xFFFA709A), Color(0xFFFEE140)),
    listOf(Color(0xFF30CFD0), Color(0xFF330867)),
    listOf(Color(0xFFA8EDEA), Color(0xFFFED6E3)),
    listOf(Color(0xFFFF9A56), Color(0xFFFF6A88)),
    listOf(Color(0xFF667EEA), Color(0xFF764BA2))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    playerVM: PlayerViewModel
) {
    var allSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        isLoading = true
        val defaultSongs = getDefaultSongs()
        val localSongs = getLocalSongs(context)
        val combinedSongs = defaultSongs + localSongs
        allSongs = combinedSongs
        isLoading = false
    }

    var showAddDialog by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentSong by playerVM.currentSong.collectAsState()
    val isPlaying by playerVM.isPlaying.collectAsState()
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredSongs by remember(searchQuery, allSongs) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                emptyList()
            } else {
                val normalizedQuery = searchQuery.unaccent().lowercase(Locale.getDefault())
                allSongs.filter { song ->
                    val normalizedTitle = song.title.unaccent().lowercase(Locale.getDefault())
                    val normalizedArtist = song.artist.unaccent().lowercase(Locale.getDefault())
                    normalizedTitle.contains(normalizedQuery) || normalizedArtist.contains(normalizedQuery)
                }
            }
        }
    }

    // Greeting thay đổi theo thời gian
    var greeting by remember { mutableStateOf("") }
    var greetingIcon by remember { mutableStateOf("☀️") }

    LaunchedEffect(Unit) {
        while (true) {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            when (hour) {
                in 0..11 -> {
                    greeting = "Chào buổi sáng"
                    greetingIcon = "☀️"
                }
                in 12..17 -> {
                    greeting = "Chào buổi chiều"
                    greetingIcon = "🌤️"
                }
                else -> {
                    greeting = "Chào buổi tối"
                    greetingIcon = "🌙"
                }
            }
            delay(60000L)
        }
    }

    if (showAddDialog) {
        AddSongDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, artist, audioUri, imageUri ->
                scope.launch {
                    val newSong = Song(
                        title = title,
                        artist = artist,
                        audioUrl = audioUri.toString(),
                        imageUrl = imageUri?.toString()
                    )
                    val currentLocalSongs = getLocalSongs(context)
                    saveLocalSongs(context, currentLocalSongs + newSong)
                    val updatedSongs = allSongs + newSong
                    allSongs = updatedSongs
                    playerVM.setPlaylist(updatedSongs, updatedSongs.lastIndex)
                    showAddDialog = false
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModernDrawer(
                onItemClick = { scope.launch { drawerState.close() } },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Routes.AUTH) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                if (isSearching) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onBackClick = {
                            isSearching = false
                            searchQuery = ""
                        }
                    )
                } else {
                    MainTopBar(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onAddClick = { showAddDialog = true },
                        onSearchClick = { isSearching = true }
                    )
                }
            },
            containerColor = Color(0xFF0D0F1F)
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF1DB954))
                    }
                } else {
                    if (isSearching) {
                        SearchResults(
                            query = searchQuery,
                            filteredSongs = filteredSongs,
                            allSongs = allSongs,
                            navController = navController,
                            playerVM = playerVM
                        )
                    } else {
                        MainContent(
                            greeting = greeting,
                            greetingIcon = greetingIcon,
                            songs = allSongs,
                            navController = navController,
                            playerVM = playerVM
                        )
                    }
                }

                currentSong?.let {
                    MiniPlayer(
                        song = it,
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
private fun AddSongDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Uri, Uri?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var selectedAudioUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            selectedAudioUri = uri
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            selectedImageUri = uri
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1F3A),
        title = {
            Text(
                "Thêm bài hát",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 20.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2A3050))
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.AddPhotoAlternate,
                            null,
                            tint = Color(0xFF7A8BA0),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tên bài hát") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF1DB954),
                        unfocusedBorderColor = Color(0xFF4A5568)
                    )
                )

                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Nghệ sĩ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF1DB954),
                        unfocusedBorderColor = Color(0xFF4A5568)
                    )
                )

                Button(
                    onClick = { audioPickerLauncher.launch("audio/*") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedAudioUri == null) Color(0xFF2A3050) else Color(0xFF1DB954)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        if (selectedAudioUri == null) Icons.Default.MusicNote else Icons.Default.CheckCircle,
                        null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (selectedAudioUri == null) "Chọn tệp nhạc" else "Đã chọn")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedAudioUri?.let { audioUri ->
                        onConfirm(title.trim(), artist.trim(), audioUri, selectedImageUri)
                    }
                },
                enabled = title.isNotBlank() && artist.isNotBlank() && selectedAudioUri != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1DB954)
                )
            ) {
                Text("Thêm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = Color(0xFFB3B3B3))
            }
        }
    )
}

private suspend fun saveLocalSongs(context: Context, songs: List<Song>) {
    val jsonString = Json.encodeToString(songs)
    context.dataStore.edit { preferences ->
        preferences[LOCAL_SONGS_KEY] = jsonString
    }
}

private suspend fun getLocalSongs(context: Context): List<Song> {
    return try {
        val jsonString = context.dataStore.data.map { it[LOCAL_SONGS_KEY] ?: "[]" }.first()
        Json.decodeFromString(jsonString)
    } catch (e: Exception) {
        emptyList()
    }
}

//================================================================================//
// TOP BAR & NAVIGATION
//================================================================================//

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(
    onMenuClick: () -> Unit,
    onAddClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                "MelodyPlayer",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, null, tint = Color.White)
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, null, tint = Color.White)
            }
            IconButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, null, tint = Color(0xFF1DB954))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF0D0F1F)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Tìm kiếm...", color = Color.White.copy(0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF1DB954),
                    focusedBorderColor = Color(0xFF1DB954),
                    unfocusedBorderColor = Color.White.copy(0.3f)
                ),
                shape = RoundedCornerShape(24.dp),
                leadingIcon = {
                    Icon(Icons.Default.Search, null, tint = Color.White.copy(0.7f))
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Clear, null, tint = Color.White)
                        }
                    }
                }
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF0D0F1F)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernDrawer(onItemClick: () -> Unit, onLogout: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }

    ModalDrawerSheet(
        drawerContainerColor = Color(0xFF0D0F1F),
        drawerContentColor = Color.White
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1DB954), Color(0xFF0D0F1F))
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        tint = Color(0xFF1DB954),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    FirebaseAuth.getInstance().currentUser?.email?.split("@")?.first() ?: "User",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Music Lover 🎵",
                    fontSize = 14.sp,
                    color = Color.White.copy(0.7f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        listOf(
            Triple("Trang chủ", Icons.Default.Home, 0),
            Triple("Tìm kiếm", Icons.Default.Search, 1),
            Triple("Thư viện", Icons.Default.LibraryMusic, 2)
        ).forEach { (title, icon, index) ->
            NavigationDrawerItem(
                label = { Text(title, fontSize = 16.sp) },
                icon = { Icon(icon, null) },
                selected = selectedTab == index,
                onClick = {
                    selectedTab = index
                    onItemClick()
                },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = Color(0xFF1DB954).copy(0.2f),
                    selectedTextColor = Color(0xFF1DB954),
                    selectedIconColor = Color(0xFF1DB954)
                ),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        Spacer(Modifier.weight(1f))

        NavigationDrawerItem(
            label = { Text("Đăng xuất", fontSize = 16.sp) },
            icon = { Icon(Icons.Default.ExitToApp, null) },
            selected = false,
            onClick = onLogout,
            colors = NavigationDrawerItemDefaults.colors(
                unselectedTextColor = Color(0xFFFF6B9D),
                unselectedIconColor = Color(0xFFFF6B9D)
            ),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
        )
    }
}

//================================================================================//
// MAIN CONTENT
//================================================================================//

@Composable
private fun MainContent(
    greeting: String,
    greetingIcon: String,
    songs: List<Song>,
    navController: NavController,
    playerVM: PlayerViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        item {
            // Greeting với icon động
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF1DB954).copy(0.2f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Text(
                    greetingIcon,
                    fontSize = 40.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    greeting,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    "Sẵn sàng khám phá",
                    fontSize = 16.sp,
                    color = Color.White.copy(0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        item { Spacer(Modifier.height(20.dp)) }

        // Quick Access (6 bài hát đầu)
        item {
            Text(
                "Truy cập nhanh",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                songs.take(6).chunked(2).forEachIndexed { rowIndex, rowSongs ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowSongs.forEachIndexed { colIndex, song ->
                            val colorIndex = rowIndex * 2 + colIndex
                            QuickAccessCard(
                                song = song,
                                colors = ColorPalette[colorIndex % ColorPalette.size],
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    playerVM.setPlaylist(songs, songs.indexOf(song))
                                    navController.navigate(Routes.PLAYER)
                                }
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }

        // Featured Banner
        item {
            FeaturedCard(
                onClick = {
                    if (songs.isNotEmpty()) {
                        val playlist = songs.shuffled().take(10)
                        val songsJson = Json.encodeToString(playlist)
                        val encodedTitle = Uri.encode("Bộ sưu tập")

                        // ✅ Truyền songsJson qua savedStateHandle để tránh crash
                        navController.currentBackStackEntry?.savedStateHandle?.set("songsJson", songsJson)
                        navController.navigate("${Routes.COLLECTION}/$encodedTitle")

                    }
                }
            )
        }

        item { Spacer(Modifier.height(24.dp)) }

        // Playlists
        // Playlists
        item {
            SectionTitle("Dành cho bạn", "Xem tất cả", onActionClick = {
                if (songs.isNotEmpty()) {
                    val title = "Dành cho bạn"
                    val playlist = songs.take(10)
                    val songsJson = Json.encodeToString(playlist)
                    val encodedTitle = Uri.encode(title)

                    // ✅ Truyền JSON qua SavedStateHandle
                    navController.currentBackStackEntry?.savedStateHandle?.set("songsJson", songsJson)
                    navController.navigate("${Routes.COLLECTION}/$encodedTitle")

                }
            })

        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                items(songs.take(10)) { song ->
                    SongCard(
                        song = song,
                        colors = ColorPalette[songs.indexOf(song) % ColorPalette.size],
                        onClick = {
                            playerVM.setPlaylist(songs, songs.indexOf(song))
                            navController.navigate(Routes.PLAYER)
                        }
                    )
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }

        item {
            SectionTitle("Trending 🔥", "Xem tất cả", onActionClick = {
                if (songs.isNotEmpty()) {
                    val title = "Trending 🔥"
                    val playlist = songs.takeLast(10)
                    val songsJson = Json.encodeToString(playlist)
                    val encodedTitle = Uri.encode(title)

                    // ✅ Truyền JSON qua SavedStateHandle
                    navController.currentBackStackEntry?.savedStateHandle?.set("songsJson", songsJson)
                    navController.navigate("${Routes.COLLECTION}/$encodedTitle")

                }
            })

        }
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                items(songs.takeLast(10)) { song ->
                    SongCard(
                        song = song,
                        colors = ColorPalette[songs.indexOf(song) % ColorPalette.size],
                        onClick = {
                            playerVM.setPlaylist(songs, songs.indexOf(song))
                            navController.navigate(Routes.PLAYER)
                        }
                    )
                }
            }
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun QuickAccessCard(
    song: Song,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(70.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(colors)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(Color.Black.copy(0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                song.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

@Composable
private fun FeaturedCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(horizontal = 20.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFFF6B9D),
                            Color(0xFFFFB86C),
                            Color(0xFFF8B500)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "MỚI",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF6B9D)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Bộ sưu tập",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    "Cập nhật hàng tuần ✨",
                    fontSize = 14.sp,
                    color = Color.White.copy(0.9f)
                )
            }

            val rotation by rememberInfiniteTransition(label = "rotation").animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
                label = "rotation"
            )

            Icon(
                Icons.Default.MusicNote,
                null,
                modifier = Modifier
                    .size(70.dp)
                    .align(Alignment.CenterEnd)
                    .rotate(rotation),
                tint = Color.White.copy(0.3f)
            )
        }
    }
}

// THAY ĐỔI 1: Cập nhật chữ ký hàm SectionTitle
@Composable
private fun SectionTitle(title: String, action: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        // THAY ĐỔI 2: Sử dụng onActionClick cho TextButton
        TextButton(onClick = onActionClick) {
            Text(
                action,
                fontSize = 14.sp,
                color = Color(0xFF1DB954)
            )
        }
    }
}

@Composable
private fun SongCard(
    song: Song,
    colors: List<Color>,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        Card(
            modifier = Modifier.size(140.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.radialGradient(colors)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    null,
                    modifier = Modifier.size(60.dp),
                    tint = Color.White.copy(0.9f)
                )

                FloatingActionButton(
                    onClick = onClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(40.dp),
                    containerColor = Color.White
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        null,
                        tint = colors[0],
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            song.title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            song.artist,
            color = Color(0xFFB3B3B3),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

//================================================================================//
// SEARCH UI
//================================================================================//

@Composable
private fun SearchResults(
    query: String,
    filteredSongs: List<Song>,
    allSongs: List<Song>,
    navController: NavController,
    playerVM: PlayerViewModel
) {
    if (query.isNotBlank() && filteredSongs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    Icons.Default.SearchOff,
                    null,
                    tint = Color(0xFF4A5568),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Không tìm thấy",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Thử từ khóa khác",
                    color = Color(0xFF7A8BA0),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    } else if (query.isNotBlank()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredSongs) { song ->
                SearchResultCard(
                    song = song,
                    onClick = {
                        val index = allSongs.indexOf(song)
                        if (index != -1) {
                            playerVM.setPlaylist(allSongs, index)
                            navController.navigate(Routes.PLAYER)
                        }
                    }
                )
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    Icons.Default.Search,
                    null,
                    tint = Color(0xFF4A5568),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Tìm kiếm bài hát",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Nhập tên bài hát hoặc nghệ sĩ",
                    color = Color(0xFF7A8BA0),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun SearchResultCard( // <<<<<<< LỖI ĐÃ ĐƯỢC SỬA Ở ĐÂY
    song: Song,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(Color(0xFF1A1F3A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                Icon(
                    Icons.Default.MusicNote,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    song.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    song.artist,
                    color = Color(0xFFB3B3B3),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Icon(
                Icons.Default.PlayArrow,
                null,
                tint = Color(0xFF1DB954),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

//================================================================================//
// HELPERS
//================================================================================//

private val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()
private fun String.unaccent(): String {
    val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
    return REGEX_UNACCENT.replace(temp, "").replace("đ", "d").replace("Đ", "D")
}

private fun getDefaultSongs() = listOf(
    Song(title = "Sao Cha Không Về (Bố Già OST)", artist = "Ali Hoàng Dương", resId = "ali_hoang_duong_bo_gia_ost_official_mv", imageUrl = "https://photo-resize-zmp3.zmdcdn.me/w240_r1x1_jpeg/cover/9/4/c/f/94cfd542388334468222405786214375.jpg"),
    Song(title = "Ex's Hate Me", artist = "B-Ray x Masew ft. Amee", resId = "b_ray_x_masew_ft_amee_official_mv", imageUrl = "https://i.scdn.co/image/ab67616d0000b273b3e64e525a4072ea6517a861"),
    Song(title = "Bước Qua Mùa Cô Đơn", artist = "Vũ.", resId = "buoc_qua_mua_co_don_vu_official_mv", imageUrl = "https://i.scdn.co/image/ab67616d0000b273ebc69a5332f136364f72a443"),
    Song(title = "Bước Qua Nhau", artist = "Vũ.", resId = "buoc_qua_nhau_vu_official_mv", imageUrl = "https://i.scdn.co/image/ab67616d0000b273f081363a233b378051cc0a06"),
    Song(title = "Bật Tình Yêu Lên", artist = "Hòa Minzy x Tăng Duy Tân", resId = "bat_tinh_yeu_len_hoa_minzy_x_tang_duy_tan_mv_lyrics", imageUrl = "https://photo-resize-zmp3.zmdcdn.me/w240_r1x1_jpeg/cover/0/d/9/f/0d9f783518e32c0211117565a9477b14.jpg"),
    Song(title = "Bốn Chữ Lắm", artist = "Trúc Nhân ft. Trương Thảo Nhi", resId = "bon_chu_lam_mv_truc_nhan_truong_thao_nhi_chat_luong_4k", imageUrl = "https://i.scdn.co/image/ab67616d0000b27387a32997e7225244199c15bd"),
    Song(title = "Chiều Hôm Ấy", artist = "JayKii", resId = "chieu_hom_ay_official_mv", imageUrl = "https://i.scdn.co/image/ab67616d0000b27301c2057d54d96853289e658e"),
    Song(title = "Chúng Ta Không Thuộc Về Nhau", artist = "Sơn Tùng M-TP", resId = "chung_ta_khong_thuoc_ve_nhau_official_music_video_son_tung_m_tp", imageUrl = "https://i.scdn.co/image/ab67616d0000b273b5f502280d06f7b88f36c8d7"),
    Song(title = "Chúng Ta Của Hiện Tại", artist = "Sơn Tùng M-TP", resId = "chung_ta_cua_hien_tai", imageUrl = "https://i.scdn.co/image/ab67616d0000b2738***e0d3e91185839335f9226"),
    Song(title = "Còn Yêu, Đâu Ai Rời Đi", artist = "Đức Phúc", resId = "con_yeu_dau_ai_roi_di_duc_phuc_official_mv", imageUrl = "https://i.scdn.co/image/ab67616d0000b273b64c8c7c93608226487e502c"),
    Song(title = "Cơn Mưa Ngang Qua", artist = "Sơn Tùng M-TP", resId = "con_mua_ngang_qua_mtp_son_tung_k", imageUrl = "https://i.scdn.co/image/ab67616d0000b273d2a70a83155f932e033d2e09"),
    Song(title = "Em Của Ngày Hôm Qua", artist = "Sơn Tùng M-TP", resId = "em_cua_ngay_hom_qua", imageUrl = "https://i.scdn.co/image/ab67616d0000b27341e34c9597a47a82698e6583"),
    Song(title = "Em Hát Ai Nghe", artist = "Orange", resId = "em_hat_ai_nghe_official_mv", imageUrl = "https://i.scdn.co/image/ab67616d0000b273f7d19e99553f14371607c87c"),
    Song(title = "Fake Love x Wolves x Nothing Stopping Me", artist = "Remix", resId = "fake_love_x_wolves_x_nothing_stopping_me_track_edm_remix_hot_tiktok_2024", imageUrl = "https://i.ytimg.com/vi/aLzWmaeA2EM/maxresdefault.jpg"),
    Song(title = "Người Tình Mùa Đông", artist = "Hòa Minzy", resId = "hoa_minzy_nguoi_tinh_mua_dong_official_music_video", imageUrl = "https://i.scdn.co/image/ab67616d0000b2734fd5c4f2e96409893d9326ac"),
    Song(title = "Hơn Cả Yêu", artist = "Đức Phúc", resId = "hon_ca_yeu_duc_phuc_official_music_video", imageUrl = "https://i.scdn.co/image/ab67616d0000b273187289563c6310243169bb93"),
    Song(title = "Rồi Mình Kể Nhau Nghe Chuyện Đêm", artist = "Hương Ly (Cover)", resId = "huong_ly_cover_vietz_remix_roi_minh_ke_nhau_nghe_chuyen_dem", imageUrl = "https://i.ytimg.com/vi/i78L5gq_w_o/maxresdefault.jpg"),
    Song(title = "Hết Thương Cạn Nhớ", artist = "Đức Phúc", resId = "het_thuong_can_nho_duc_phuc_official_music_video", imageUrl = "https://i.scdn.co/image/ab67616d0000b27343e74889c25f6174a7b97368"),
    Song(title = "Sóng Gió", artist = "ICM x Jack", resId = "icm_x_jack_official_music_video", imageUrl = "https://i.scdn.co/image/ab67616d0000b273e3a45a305b455b9a89ab74d3"),
    Song(title = "Hồng Nhan", artist = "Jack (G5R)", resId = "jack_hong_nhan_official_mv_g5r", imageUrl = "https://i.scdn.co/image/ab67616d0000b2736ab88574033b00627f753550"),
    Song(title = "Cuối Cùng Thì", artist = "Jack (J97)", resId = "jack_j97_cuoi_cung_thi_special_stage_video", imageUrl = "https://i.scdn.co/image/ab67616d0000b273c5cf8c3f4e172a39a0614f17"),
    Song(title = "Thiên Lý Ơi", artist = "Jack (J97)", resId = "jack_j97_thien_ly_oi_official_music_video", imageUrl = "https://i.scdn.co/image/ab67616d0000b273c66f5f3e48356f2f3554e287"),
    Song(title = "Bạc Phận", artist = "Jack x K-ICM", resId = "jack_x_k_icm", imageUrl = "https://i.scdn.co/image/ab67616d0000b273b0a24143a4e98f6f57849e79"),
    Song(title = "Khi Em Lớn", artist = "Orange", resId = "khi_em_lon_official_mv", imageUrl = "https://i.scdn.co/image/ab67616d0000b27346c7667d89617d91e021d96b"),
    Song(title = "Bước Qua Đời Nhau", artist = "Khắc Việt", resId = "khac_viet_lyrics_video", imageUrl = "https://i.scdn.co/image/ab67616d0000b273f7a77e7428172c7247332f14"),
    Song(title = "Love Is Gone", artist = "SLANDER ft. Dylan Matthew", resId = "love_is_gone_lyrics_ft_dylan_matthew", imageUrl = "https://i.scdn.co/image/ab67616d0000b273832965955b24844331408853"),
    Song(title = "Lạ Lùng", artist = "Vũ.", resId = "la_lung_vu_original", imageUrl = "https://i.scdn.co/image/ab67616d0000b273d22170386a63503d422fed75"),
    Song(title = "Tình Yêu Chậm Trễ", artist = "MONSTAR", resId = "monstar_official_music_video", imageUrl = "https://i.scdn.co/image/ab67616d0000b2735741639f727c62c640e0c053"),
    Song(title = "Thu Cuối", artist = "Mr.T ft. Yanbi & Hằng Bingboong", resId = "mrt_ft_yanbi_x_hang_bing_boong_nhac_remix_bat_hu_di_cung_thoi_gian", imageUrl = "https://i.scdn.co/image/ab67616d0000b273105e45a271d4a8e63b36e57a"),
    Song(title = "Ngày Đầu Tiên", artist = "Đức Phúc", resId = "ngay_dau_tien_duc_phuc_official_music_video_valentine_2022", imageUrl = "https://i.scdn.co/image/ab67616d0000b2735c02641e71569a917e33519c"),
    Song(title = "Những Lời Hứa Bỏ Quên", artist = "Vũ. x Dear Jane", resId = "nhung_loi_hua_bo_quen_vu_x_dear_jane_official_mv_tu_album_bao_tang_cua_nuoi_tiec", imageUrl = "https://i.scdn.co/image/ab67616d0000b2738b8e058c4c785505c1b5003f"),
    Song(title = "Nơi Này Có Anh", artist = "Sơn Tùng M-TP", resId = "noi_nay_co_anh_official_music_video_son_tung_m_tp", imageUrl = "https://i.scdn.co/image/ab67616d0000b27358a23be6a908a8670ed1d8f5"),
    Song(title = "24H", artist = "LyLy ft. Magazine", resId = "official_music_video_lyly_ft_magazine", imageUrl = "https://i.scdn.co/image/ab67616d0000b273e9790586b0f16d516223e753"),
    Song(title = "Anh Không Đòi Quà", artist = "Only C ft. Karik", resId = "only_c_ft_karik_official_music_video", imageUrl = "https://i.scdn.co/image/ab67616d0000b2730a08e123380482b6b026a0ce"),
    Song(title = "Đừng Kết Thúc Hôm Nay", artist = "Orange (Prod. by Madihu)", resId = "orange_dung_ket_thuc_hom_nay_official_mv_prod_by_madihu", imageUrl = "https://i.scdn.co/image/ab67616d0000b27361846c483a910f1352e89f81"),
    Song(title = "Đừng Tốt Với Em", artist = "Orange x DT Tập Rap", resId = "orange_x_dt_tap_rap_dung_tot_voi_em_official_visualizer_album_cam_on", imageUrl = "https://i.scdn.co/image/ab67616d0000b2736413d7c5a04a3f4c6f3768f5"),
    Song(title = "Mẹ Em Nhắc Anh", artist = "Orange x Hamlet Trương", resId = "orange_x_hamlet_truong_me_em_nhac_anh_official_mv", imageUrl = "https://i.scdn.co/image/ab67616d0000b273600e625a58d62657e289873b"),
    Song(title = "Khi Em Lớn (OST Bộ Tứ Báo Thủ)", artist = "Orange", resId = "orange_ost_bo_tu_bao_thu_dao_dien_tran_thanh", imageUrl = "https://i.scdn.co/image/ab67616d0000b27346c7667d89617d91e021d96b"),
    Song(title = "Chạy Ngay Đi (Run Now)", artist = "Sơn Tùng M-TP", resId = "run_now_son_tung_m_tp_official_music_video", imageUrl = "https://i.scdn.co/image/ab67616d0000b273d12d44933a005085e9e8f3b0"),
    Song(title = "Thị Mầu", artist = "Hòa Minzy", resId = "roi_bo_hoa_minzy_official_lyrics_video", imageUrl = "https://i.scdn.co/image/ab67616d0000b273d3121544485542f7c24f61f9"),
    Song(title = "Yêu Thương Ngày Đó", artist = "Soobin Hoàng Sơn", resId = "soobin_hoang_son", imageUrl = "https://i.scdn.co/image/ab67616d0000b273e93a61b6522a7f53a1a67cf3"),
    Song(title = "Có Chắc Yêu Là Đây", artist = "Sơn Tùng M-TP", resId = "son_tung_m_tp_co_chac_yeu_la_day_official_music_video", imageUrl = "https://i.scdn.co/image/ab67616d0000b273ee78b449103c83713f64c676"),
    Song(title = "Ta Còn Yêu Nhau", artist = "Đức Phúc", resId = "ta_con_yeu_nhau_official_mv_story_duc_phuc", imageUrl = "https://i.scdn.co/image/ab67616d0000b2738986950229497e70e3f22557"),
    Song(title = "Thư Chưa Gửi Anh", artist = "Hòa Minzy", resId = "thu_chua_gui_anh_official_mv_hoa_minzy", imageUrl = "https://i.scdn.co/image/ab67616d0000b273dd708b8b39a3f29b0a6e4d41"),
    Song(title = "Yêu Được Không", artist = "Đức Phúc x ViruSs", resId = "yeu_duoc_khong_duc_phuc_x_viruss_official_music_video", imageUrl = "https://i.scdn.co/image/ab67616d0000b27393d256193952f4460d3d2c65"),
    Song(title = "Chạy Về Khóc Với Anh", artist = "ERIK", resId = "yeu_duong_kho_qua_thi_chay_ve_khoc_voi_anh_official_music_video_genshin_impact", imageUrl = "https://i.scdn.co/image/ab67616d0000b27357c91f16ed8f902641a2936a"),
    Song(title = "Về Bên Anh", artist = "Jack (G5R)", resId = "official_mv_ve_ben_anh_jack_g5r", imageUrl = "https://i.ytimg.com/vi/Q28O3_54VNo/maxresdefault.jpg"),
    Song(title = "Ánh Nắng Của Anh", artist = "Đức Phúc", resId = "anh_nang_cua_anh_ost_cho_em_den_ngay_mai_duc_phuc_official_mv_nhac_tre_hay_moi_nhat", imageUrl = "https://i.scdn.co/image/ab67616d0000b2734a74e5329c366f00122e2a39"),
    Song(title = "Âm Thầm Bên Em", artist = "Sơn Tùng M-TP", resId = "am_tham_ben_em", imageUrl = "https://i.scdn.co/image/ab67616d0000b2730623381a4b4b2451f28e21c3"),
    Song(title = "Đau Nhất Là Lặng Im", artist = "ERIK", resId = "dau_nhat_la_lang_im_official_music_video", imageUrl = "https://i.scdn.co/image/ab67616d0000b273523f7215c929d2b2c9c73e97"),
    Song(title = "Đông Kiếm Em", artist = "Vũ.", resId = "dong_kiem_em_vu_original", imageUrl = "https://i.scdn.co/image/ab67616d0000b2739433604b9e283f5247854611")
)