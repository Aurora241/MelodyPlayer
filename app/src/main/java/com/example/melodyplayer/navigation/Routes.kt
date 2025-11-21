package com.example.melodyplayer.navigation

object Routes {
    const val AUTH = "auth"
    const val LOGIN = "login"
    const val HOME = "home"
    const val PLAYER = "player"
    const val SEARCH = "search"
    const val SETTINGS = "settings"

    // 👇 Đổi tên này cho khớp với HomeScreen
    const val PLAYLIST = "playlist"

    // ✅ Route cơ bản
    const val COLLECTIONS = "collections"

    // 👇 THÊM DÒNG NÀY: Route chi tiết có tham số (để MainActivity hứng dữ liệu)
    const val COLLECTION_DETAIL = "collection/{title}/{songsJson}"
}