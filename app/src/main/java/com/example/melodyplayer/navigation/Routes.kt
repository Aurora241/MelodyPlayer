package com.example.melodyplayer.navigation

object Routes {
    const val AUTH = "auth"
    const val LOGIN = "login"
    const val HOME = "home"
    const val PLAYER = "player"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val PLAYLIST_ALL = "playlist_all"

    // THÊM MỚI: Route cho màn hình bộ sưu tập, có tham số là chuỗi json
    const val COLLECTION = "collection/{songsJson}"
}