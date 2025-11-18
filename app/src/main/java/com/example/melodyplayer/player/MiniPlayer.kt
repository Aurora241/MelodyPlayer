package com.example.melodyplayer.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.ImageLoader
import android.graphics.drawable.BitmapDrawable
import androidx.palette.graphics.Palette
import com.example.melodyplayer.model.Song
import kotlinx.coroutines.launch

@Composable
fun MiniPlayer(
    song: Song?,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (song == null) return

    // Nếu artist rỗng -> fallback
    val artistLabel = song.artist.takeIf { it.isNotBlank() } ?: "Unknown Artist"

    // Màu sắc động từ album art
    var dominantColor by remember { mutableStateOf(Color(0xFF1DB954)) }
    var darkMutedColor by remember { mutableStateOf(Color(0xFF0D2D20)) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Trích xuất màu từ album art
    LaunchedEffect(song.imageUrl) {
        scope.launch {
            try {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(song.imageUrl)
                    .allowHardware(false)
                    .build()

                val result = (loader.execute(request) as? SuccessResult)?.drawable
                val bitmap = (result as? BitmapDrawable)?.bitmap

                bitmap?.let {
                    Palette.from(it).generate { palette ->
                        palette?.let { p ->
                            dominantColor = Color(p.getDominantColor(0xFF1DB954.toInt()))
                            darkMutedColor = Color(p.getDarkMutedColor(0xFF0D2D20.toInt()))
                        }
                    }
                }
            } catch (e: Exception) {
                dominantColor = Color(0xFF1DB954)
                darkMutedColor = Color(0xFF0D2D20)
            }
        }
    }

    // Animation cho album art xoay
    val rotation by rememberInfiniteTransition(label = "").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            darkMutedColor.copy(alpha = 0.9f),
                            Color(0xFF1A1A1A)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // ===== ALBUM COVER + INFO =====
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Album Cover - Rotating
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2A2A2A))
                            .rotate(if (isPlaying) rotation else 0f),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = song.imageUrl,
                            contentDescription = "Album Art",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                        )

                        // Tâm đĩa
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1A1A1A))
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF404040))
                                    .align(Alignment.Center)
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    // Song Info với Marquee Text
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 6.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Title chạy marquee - import từ MarqueeText.kt
                        MarqueeText(
                            text = song.title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            alignment = Alignment.Start,
                            modifier = Modifier
                                .height(20.dp)
                                .padding(bottom = 2.dp)
                        )

                        // Artist luôn hiện
                        Text(
                            text = artistLabel,
                            color = dominantColor.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.height(18.dp)
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                // ===== CONTROLS =====
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Previous Button
                    IconButton(
                        onClick = onPrev,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    // Play/Pause Button (Highlighted)
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    // Next Button
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }

            // ===== PROGRESS BAR =====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.BottomCenter)
                    .background(dominantColor.copy(alpha = 0.5f))
            )
        }
    }
}