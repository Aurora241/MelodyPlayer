package com.example.melodyplayer.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.melodyplayer.model.Song
import com.example.melodyplayer.player.PlayerViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.abs

// 🎨 Hàm tiện ích: Tạo màu chủ đề dựa trên tên bộ sưu tập
fun getCollectionThemeColor(collectionName: String): Color {
    return if (collectionName == "Yêu thích") {
        Color(0xFF1DB954) // Màu xanh đặc trưng cho Yêu thích
    } else {
        // Danh sách màu sắc rực rỡ cho các bộ sưu tập khác
        val colors = listOf(
            Color(0xFFFF5252), // Đỏ
            Color(0xFF448AFF), // Xanh dương
            Color(0xFFFFAB40), // Cam
            Color(0xFFE040FB), // Tím
            Color(0xFF00E5FF), // Cyan
            Color(0xFFFFD740), // Vàng
            Color(0xFF69F0AE)  // Xanh mint
        )
        // Chọn màu cố định dựa trên HashCode của tên
        val index = abs(collectionName.hashCode()) % colors.size
        colors[index]
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    navController: NavController,
    playerVM: PlayerViewModel
) {
    val collections by playerVM.collections.collectAsState()

    // State cho các dialog
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    var showAddToCollectionDialog by remember { mutableStateOf<Song?>(null) }
    var showCreateCollectionDialog by remember { mutableStateOf(false) } // State tạo mới

    val scope = rememberCoroutineScope()

    // ❌ ĐÃ XÓA KHỐI LaunchedEffect GÂY LỖI HỒI SINH BỘ SƯU TẬP TẠI ĐÂY

    Scaffold(
        containerColor = Color(0xFF121212),
        // ✅ Nút Floating Action Button để tạo bộ sưu tập mới
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateCollectionDialog = true },
                containerColor = Color(0xFF1DB954),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tạo bộ sưu tập mới")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 🎵 Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    // Gradient nền
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF1DB954),
                                        Color(0xFF121212)
                                    ),
                                    startY = 0f,
                                    endY = 800f
                                )
                            )
                    )

                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Nút quay lại
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Quay lại",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // Tiêu đề
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Outlined.LibraryMusic,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Bộ sưu tập của tôi",
                                color = Color.White,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "${collections.size} bộ sưu tập",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // 🎧 Nếu chưa có bộ sưu tập
            if (collections.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Outlined.LibraryMusic,
                                contentDescription = null,
                                tint = Color.Gray.copy(0.4f),
                                modifier = Modifier.size(80.dp)
                            )
                            Text(
                                "Chưa có bộ sưu tập nào",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Bấm nút + để tạo bộ sưu tập mới",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // 📀 Danh sách collection
                items(collections) { collectionName ->
                    val songCount = playerVM.getSongsInCollection(collectionName).size
                    val themeColor = getCollectionThemeColor(collectionName)

                    // Trong CollectionsScreen.kt

                    CollectionCard(
                        collectionName = collectionName,
                        songCount = songCount,
                        themeColor = themeColor,
                        onClick = {
                            // [SỬA] Lấy danh sách nhạc thật từ ViewModel
                            val songs = playerVM.getSongsInCollection(collectionName)

                            // [SỬA] Lưu danh sách đó vào biến tạm trong ViewModel
                            playerVM.setSelectedCollectionSongs(songs)

                            // [SỬA] Chỉ truyền TÊN BỘ SƯU TẬP qua URL
                            val encodedTitle = Uri.encode(collectionName)

                            // Gọi đến route COLLECTION_DETAIL (đã sửa trong Routes.kt thành "collection/{title}")
                            navController.navigate("collection/$encodedTitle") {
                                launchSingleTop = true
                            }
                        },
                        onDelete = {
                            showDeleteDialog = collectionName
                        }
                    )
                }

                item {
                    Spacer(Modifier.height(100.dp))
                }
            }
        }
    }

    // ✅ Dialog Tạo Bộ Sưu Tập Mới
    if (showCreateCollectionDialog) {
        var newCollectionName by remember { mutableStateOf("") }
        var errorText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateCollectionDialog = false },
            containerColor = Color(0xFF282828),
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    "Tạo bộ sưu tập mới",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = newCollectionName,
                        onValueChange = {
                            newCollectionName = it
                            errorText = ""
                        },
                        label = { Text("Tên bộ sưu tập") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1DB954),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color(0xFF1DB954),
                            unfocusedLabelColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorText.isNotEmpty()) {
                        Text(
                            text = errorText,
                            color = Color(0xFFFF5252),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCollectionName.isBlank()) {
                            errorText = "Tên không được để trống"
                        } else if (collections.contains(newCollectionName)) {
                            errorText = "Bộ sưu tập đã tồn tại"
                        } else {
                            scope.launch {
                                playerVM.ensureCollectionExists(newCollectionName)
                                showCreateCollectionDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
                ) {
                    Text("Tạo", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateCollectionDialog = false }) {
                    Text("Hủy", color = Color.White)
                }
            }
        )
    }

    // ❌ Dialog xác nhận xóa
    showDeleteDialog?.let { collectionName ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            containerColor = Color(0xFF282828),
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    "Xóa bộ sưu tập?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    "Bạn có chắc muốn xóa \"$collectionName\"?\nTất cả bài hát trong danh sách này sẽ bị xóa khỏi bộ sưu tập.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch { playerVM.deleteCollection(collectionName) }
                        showDeleteDialog = null
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

    // ➕ Dialog thêm vào bộ sưu tập
    showAddToCollectionDialog?.let { song ->
        AddToCollectionDialog(
            song = song,
            collections = collections,
            onDismiss = { showAddToCollectionDialog = null },
            onAddToCollection = { selectedCollection ->
                scope.launch {
                    playerVM.ensureCollectionExists(selectedCollection)
                    playerVM.addSongToCollection(song, selectedCollection)
                    showAddToCollectionDialog = null
                }
            }
        )
    }
}

// Dialog thêm bài hát
@Composable
fun AddToCollectionDialog(
    song: Song,
    collections: List<String>,
    onDismiss: () -> Unit,
    onAddToCollection: (String) -> Unit
) {
    var selectedCollection by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF282828),
        shape = RoundedCornerShape(16.dp),
        title = {
            Text("Thêm vào bộ sưu tập", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                if (collections.isEmpty()) {
                    Text("Chưa có bộ sưu tập nào.", color = Color.Gray, fontSize = 14.sp)
                } else {
                    LazyColumn {
                        itemsIndexed(collections) { _, collection ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedCollection = collection }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedCollection == collection,
                                    onClick = { selectedCollection = collection },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF1DB954), unselectedColor = Color.Gray)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(collection, color = Color.White, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedCollection?.let { onAddToCollection(it) } },
                enabled = selectedCollection != null,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954), disabledContainerColor = Color.Gray.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(24.dp)
            ) { Text("Thêm", color = Color.White, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shape = RoundedCornerShape(24.dp)) { Text("Hủy", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    )
}

@Composable
fun CollectionCard(
    collectionName: String,
    songCount: Int,
    themeColor: Color,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Box
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                themeColor.copy(alpha = 0.8f),
                                themeColor,
                                themeColor.copy(alpha = 0.5f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.LibraryMusic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    collectionName,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "$songCount bài hát",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }

            // ❌ ĐÃ XÓA HÌNH TRÁI TIM

            // Menu 3 chấm
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, null, tint = Color.White)
                                Spacer(Modifier.width(16.dp))
                                Text("Phát tất cả", color = Color.White)
                            }
                        },
                        onClick = {
                            showMenu = false
                            onClick()
                        }
                    )

                    Divider(color = Color.Gray.copy(0.2f))

                    // ✅ Nút xóa luôn hiển thị
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Delete, null, tint = Color(0xFFFF5252))
                                Spacer(Modifier.width(16.dp))
                                Text("Xóa bộ sưu tập", color = Color(0xFFFF5252))
                            }
                        },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}