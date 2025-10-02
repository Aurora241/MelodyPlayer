package com.example.melodyplayer.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.melodyplayer.navigation.Routes
import com.google.firebase.auth.FirebaseAuth

@Composable
fun DrawerContent(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .padding(vertical = 20.dp)
    ) {
        // Header
        Text(
            text = "Melody Player",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Menu items
        DrawerItem(label = "Trang chủ", selected = selectedTab == 0) { onTabSelected(0) }
        DrawerItem(label = "Playlist", selected = selectedTab == 1) { onTabSelected(1) }
        DrawerItem(label = "Cài đặt", selected = selectedTab == 2) { onTabSelected(2) }

        Spacer(modifier = Modifier.weight(1f))

        Divider(color = Color.Gray, thickness = 0.5.dp)

        // Logout (tuỳ nếu bạn muốn)
        TextButton(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                // chuyển về màn Auth — bạn có thể muốn clear backstack ở đây
                navController.navigate(Routes.AUTH) {
                    // tùy chỉnh popUpTo nếu cần
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("Đăng xuất", color = Color(0xFF1DB954))
        }
    }
}

@Composable
private fun DrawerItem(label: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color(0xFF1DB954) else Color.White,
            modifier = Modifier.padding(vertical = 6.dp)
        )
    }
}

@Composable
fun PlaylistContent() {
    // Placeholder: bạn có thể thay bằng UI thực tế hiển thị playlist
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Playlist content (chưa có dữ liệu)", color = Color.White)
    }
}

@Composable
fun SettingsContent(navController: NavController) {
    // Một trang cài đặt đơn giản (thay bằng nội dung bạn cần)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .padding(16.dp)
    ) {
        Text(text = "Cài đặt", style = MaterialTheme.typography.titleLarge, color = Color.White)

        Spacer(modifier = Modifier.height(16.dp))

        var darkMode by remember { mutableStateOf(true) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Giao diện tối", color = Color.White, modifier = Modifier.weight(1f))
            Switch(checked = darkMode, onCheckedChange = { darkMode = it })
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Ví dụ: nút để mở trang About hoặc profile
        TextButton(onClick = { /* navController.navigate("about") */ }) {
            Text("Giới thiệu", color = Color(0xFF1DB954))
        }
    }
}
