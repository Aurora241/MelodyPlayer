package com.example.melodyplayer.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.melodyplayer.model.Song
import androidx.media3.common.Player
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.ImageLoader
import android.graphics.drawable.BitmapDrawable
import androidx.palette.graphics.Palette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerScreen(
    navController: NavController,
    playerVM: PlayerViewModel
) {
    val currentSong by playerVM.currentSong.collectAsState()
    val isPlaying by playerVM.isPlaying.collectAsState()
    val playlist by playerVM.playlist.collectAsState()
    val playbackPosition by playerVM.playbackPosition.collectAsState()
    val playbackDuration by playerVM.duration.collectAsState()
    val repeatMode by playerVM.repeatMode.collectAsState()
    val playbackError by playerVM.playbackError.collectAsState()
    val favoriteSongs by playerVM.favoriteSongs.collectAsState()

    var showPlaylist by remember { mutableStateOf(false) }
    var showCollectionDialog by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableStateOf(0f) }
    var isSeeking by remember { mutableStateOf(false) }

    var dominantColor by remember { mutableStateOf(Color(0xFF1DB954)) }
    var vibrantColor by remember { mutableStateOf(Color(0xFF1DB954)) }

    val context = LocalContext.current

    // Extract palette
    LaunchedEffect(currentSong?.imageUrl) {
        currentSong?.imageUrl?.let { url ->
            try {
                val loader = ImageLoader(context)
                val req = ImageRequest.Builder(context)
                    .data(url)
                    .allowHardware(false)
                    .build()

                val result = loader.execute(req) as SuccessResult
                val bmp = (result.drawable as? BitmapDrawable)?.bitmap

                bmp?.let {
                    Palette.from(it).generate { p ->
                        if (p != null) {
                            dominantColor = Color(p.getDominantColor(0xFF1DB954.toInt()))
                            vibrantColor = Color(p.getVibrantColor(0xFF1DB954.toInt()))
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    val duration = playbackDuration.coerceAtLeast(0L)
    val progress = if (duration > 0) playbackPosition.toFloat() / duration else 0f
    val displayedProgress = if (isSeeking) sliderValue else progress
    val elapsed = if (isSeeking) (sliderValue * duration).toLong() else playbackPosition

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(playbackError) {
        playbackError?.let {
            snackbarHostState.showSnackbar(it)
            playerVM.clearError()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        dominantColor.copy(0.7f),
                        dominantColor.copy(0.5f),
                        Color(0xFF0D0D0D),
                        Color(0xFF0D0D0D)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {

            // TOP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White, modifier = Modifier.size(32.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("ĐANG PHÁT TỪ DANH SÁCH PHÁT", color = Color.White.copy(0.7f), fontSize = 11.sp)
                    Text("Daily Mix 2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                IconButton(onClick = {}) {
                    Icon(Icons.Default.MoreVert, null, tint = Color.White)
                }
            }

            // CENTER SECTION
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Box(
                        modifier = Modifier
                            .size(340.dp)
                            .padding(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .shadow(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = currentSong?.imageUrl ?: "",
                            contentDescription = "",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(Modifier.height(30.dp))

                    // FIXED TITLE + ARTIST
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(34.dp)
                                .clipToBounds(),
                            contentAlignment = Alignment.Center
                        ) {
                            MarqueeText(
                                text = currentSong?.title ?: "No song",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = currentSong?.artist ?: "Unknown Artist",
                            fontSize = 16.sp,
                            color = Color.White.copy(0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // PROGRESS BAR
            Column(
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Slider(
                    value = displayedProgress,
                    onValueChange = {
                        sliderValue = it
                        isSeeking = true
                    },
                    onValueChangeFinished = {
                        isSeeking = false
                        playerVM.seekTo((sliderValue * duration).toLong())
                    },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(0.3f)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(elapsed), color = Color.White.copy(0.7f), fontSize = 12.sp)
                    Text(formatTime(duration), color = Color.White.copy(0.7f), fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            // CONTROLS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isFavorite = currentSong?.let { "${it.title}||${it.artist}" in favoriteSongs } == true

                IconButton(
                    onClick = {
                        currentSong?.let { playerVM.toggleFavorite(it) }
                        if (!isFavorite) showCollectionDialog = true
                    }
                ) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        null,
                        tint = if (isFavorite) vibrantColor else Color.White.copy(0.7f)
                    )
                }

                IconButton(onClick = { playerVM.prevSong() }) {
                    Icon(Icons.Default.SkipPrevious, null, tint = Color.White, modifier = Modifier.size(44.dp))
                }

                FloatingActionButton(
                    onClick = { playerVM.togglePlayPause() },
                    containerColor = Color.White,
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        null,
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }

                IconButton(onClick = { playerVM.nextSong() }) {
                    Icon(Icons.Default.SkipNext, null, tint = Color.White, modifier = Modifier.size(44.dp))
                }

                IconButton(onClick = { playerVM.cycleRepeatMode() }) {
                    val icon = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat
                    val tint = if (repeatMode != Player.REPEAT_MODE_OFF) vibrantColor else Color.White.copy(0.7f)
                    Icon(icon, null, tint = tint)
                }
            }
            Spacer(Modifier.height(10.dp))

            // BOTTOM CONTROLS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Devices, null, tint = vibrantColor)
                Icon(Icons.Default.Share, null, tint = Color.White.copy(0.7f))
                IconButton(onClick = { showPlaylist = !showPlaylist }) {
                    Icon(Icons.Default.QueueMusic, null, tint = Color.White.copy(0.7f))
                }
            }
        }

        // PLAYLIST OVERLAY
        if (showPlaylist) {
            PlaylistOverlay(
                playlist = playlist,
                currentSong = currentSong,
                onClose = { showPlaylist = false },
                onSelect = {
                    playerVM.playSong(it)
                    showPlaylist = false
                }
            )
        }

        // ADD COLLECTION
        if (showCollectionDialog) {
            AddToCollectionDialog(
                currentSong = currentSong,
                snackbarHostState = snackbarHostState,
                onDismiss = { showCollectionDialog = false },
                onAddToCollection = { _, _ ->
                    showCollectionDialog = false
                }
            )
        }
    }
}

/* ============================================================
   PLAYLIST OVERLAY
   ============================================================ */
@Composable
fun PlaylistOverlay(
    playlist: List<Song>,
    currentSong: Song?,
    onClose: () -> Unit,
    onSelect: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAA000000))
            .clickable { onClose() }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .background(Color(0xFF1E1E1E), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .clickable(enabled = false) {}
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Đang phát (${playlist.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, null, tint = Color.White) }
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(playlist) { index, song ->
                    PlaylistItemCard(
                        song = song,
                        isPlaying = currentSong == song,
                        onClick = { onSelect(index) }
                    )
                }
            }
        }
    }
}

/* ============================================================
   PLAYLIST ITEM CARD
   ============================================================ */
@Composable
fun PlaylistItemCard(song: Song, isPlaying: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) Color(0xFF1DB954).copy(0.2f) else Color(0xFF2C2C2C)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(song.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artist, color = Color.White.copy(0.7f), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            if (isPlaying) {
                Icon(Icons.Default.Equalizer, null, tint = Color(0xFF1DB954))
            }
        }
    }
}

/* ============================================================
   ADD TO COLLECTION DIALOG
   ============================================================ */
@Composable
fun AddToCollectionDialog(
    currentSong: Song?,
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit,
    onAddToCollection: (Song, String) -> Unit
) {
    val collections = listOf("Yêu thích", "Playlist của tôi", "Nhạc buồn", "Nhạc vui", "Tập trung", "Thư giãn")
    var selected by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Thêm vào bộ sưu tập", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                collections.forEach {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = it }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selected == it, onClick = { selected = it })
                        Spacer(Modifier.width(8.dp))
                        Text(it, color = Color.White)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentSong != null && selected != null) {
                        onAddToCollection(currentSong, selected!!)
                    }
                }
            ) {
                Text("Thêm", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = Color.White)
            }
        },
        containerColor = Color(0xFF1E1E1E)
    )
}

/* ============================================================
   TIME FORMAT
   ============================================================ */
fun formatTime(ms: Long): String {
    val s = ms / 1000
    return "%02d:%02d".format(s / 60, s % 60)
}
