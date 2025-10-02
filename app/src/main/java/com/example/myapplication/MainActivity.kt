package com.example.melodyplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.melodyplayer.auth.AuthScreen
import com.example.melodyplayer.home.HomeScreen
import com.example.melodyplayer.home.Song
import com.example.melodyplayer.ui.theme.MelodyPlayerTheme
import com.google.gson.Gson
import androidx.navigation.NavType
import androidx.navigation.navArgument
import java.net.URLEncoder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MelodyPlayerTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "auth"
                ) {
                    // 🔹 Màn hình đăng nhập
                    composable("auth") {
                        AuthScreen(
                            onLoginSuccess = {
                                navController.navigate("home") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            }
                        )
                    }

                    // 🔹 Màn hình trang chủ
                    composable("home") {
                        HomeScreen(
                            navController = navController,
                            onSongClick = { song ->
                                // 👉 Encode object thành JSON string an toàn
                                val songJson = URLEncoder.encode(
                                    Gson().toJson(song),
                                    StandardCharsets.UTF_8.toString()
                                )
                                navController.navigate("player/$songJson")
                            }
                        )
                    }

                    // 🔹 Màn hình phát nhạc
                    composable(
                        route = "player/{song}",
                        arguments = listOf(navArgument("song") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val songJson = backStackEntry.arguments?.getString("song")
                        val decoded = URLDecoder.decode(songJson, StandardCharsets.UTF_8.toString())
                        val song = Gson().fromJson(decoded, Song::class.java)

                        MusicPlayerScreen(
                            navController = navController,
                            song = song
                        )
                    }
                }
            }
        }
    }
}
