//package com.example.melodyplayer.chatbot
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.melodyplayer.data.SongRepository
//import com.example.melodyplayer.model.Song
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//
//data class ChatMessage(
//    val text: String,
//    val isUser: Boolean
//)
//
//class ChatViewModel(
//    private val songRepository: SongRepository = SongRepository(),
//    private val useGeminiForSearchSummary: Boolean = true
//) : ViewModel() {
//
//    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
//    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
//
//    // Regex tìm kiếm
//    companion object {
//        private val SEARCH_PATTERN = Regex(
//            pattern = "(tìm|tim|search|nghe)\\s*((bài|bai)\\s*hát|nhạc)?",
//            option = RegexOption.IGNORE_CASE
//        )
//        private val WHITESPACE_PATTERN = "\\s+".toRegex()
//    }
//
//    fun sendMessage(apiKey: String, message: String) {
//        val trimmedMessage = message.trim()
//        if (trimmedMessage.isEmpty()) return
//
//        // 1. Hiển thị tin nhắn người dùng
//        appendMessage(ChatMessage(trimmedMessage, isUser = true))
//
//        val keyword = extractSearchKeyword(trimmedMessage)
//        val containsSearchKeyword = SEARCH_PATTERN.containsMatchIn(trimmedMessage)
//
//        when {
//            // Trường hợp 1: Có từ khóa tìm kiếm + Tên bài hát (Ví dụ: "Tìm bài Sơn Tùng")
//            containsSearchKeyword && keyword.isNotBlank() -> {
//                viewModelScope.launch {
//                    handleSongSearch(apiKey = apiKey, keyword = keyword)
//                }
//            }
//            // Trường hợp 2: Chỉ có từ khóa tìm kiếm mà không có nội dung (Ví dụ: "Tìm nhạc")
//            containsSearchKeyword -> {
//                appendMessage(
//                    ChatMessage(
//                        text = "Bạn muốn tìm bài hát nào? Hãy nhập tên bài hát hoặc ca sĩ nhé!",
//                        isUser = false
//                    )
//                )
//            }
//            // Trường hợp 3: Chat bình thường với Gemini
//            else -> {
//                viewModelScope.launch {
//                    handleGeneralMessage(apiKey = apiKey, prompt = trimmedMessage)
//                }
//            }
//        }
//    }
//
//    private suspend fun handleSongSearch(apiKey: String, keyword: String) {
//        // Gọi hàm suspend searchSongs mới trong Repository
//        val results = songRepository.searchSongs(keyword)
//
//        // Hiển thị kết quả tìm kiếm dạng text cứng
//        val formattedResponse = formatSearchResponse(keyword, results)
//        appendMessage(ChatMessage(formattedResponse, isUser = false))
//
//        // Nếu tìm thấy và muốn Gemini tóm tắt thêm
//        if (useGeminiForSearchSummary && results.isNotEmpty() && apiKey.isNotBlank()) {
//            val prompt = buildGeminiPrompt(keyword, results)
//            // Gọi Gemini nhẹ nhàng để tạo câu dẫn
//            val response = GeminiApi.sendMessage(apiKey, prompt)
//            appendMessage(ChatMessage(response, isUser = false))
//        }
//    }
//
//    private suspend fun handleGeneralMessage(apiKey: String, prompt: String) {
//        if (apiKey.isBlank()) {
//            appendMessage(ChatMessage("Vui lòng cấu hình API key để trò chuyện!", isUser = false))
//            return
//        }
//        val response = GeminiApi.sendMessage(apiKey, prompt)
//        appendMessage(ChatMessage(response, isUser = false))
//    }
//
//    private fun formatSearchResponse(keyword: String, songs: List<Song>): String {
//        if (songs.isEmpty()) {
//            return "Mình không tìm thấy bài nào liên quan đến \"$keyword\" trong thư viện."
//        }
//
//        return buildString {
//            appendLine("Kết quả tìm kiếm cho \"$keyword\":")
//            songs.forEachIndexed { index, song ->
//                val title = song.title ?: "Không tên"
//                val artist = song.artist ?: "Không rõ"
//                appendLine("${index + 1}. $title - $artist")
//            }
//        }.trim()
//    }
//
//    private fun buildGeminiPrompt(keyword: String, songs: List<Song>): String {
//        // Tạo prompt để Gemini đóng vai trợ lý giới thiệu nhạc
//        return buildString {
//            appendLine("Người dùng vừa tìm kiếm: \"$keyword\".")
//            appendLine("Hệ thống đã tìm thấy ${songs.size} bài hát sau trong cơ sở dữ liệu:")
//            songs.take(5).forEach { song -> // Chỉ lấy tối đa 5 bài để prompt không quá dài
//                appendLine("- ${song.title ?: "Unknown"} của ${song.artist ?: "Unknown"}")
//            }
//            appendLine("Hãy viết một câu ngắn gọn, vui vẻ (dưới 30 từ) để mời người dùng thưởng thức các bài hát này.")
//        }.trim()
//    }
//
//    private fun appendMessage(message: ChatMessage) {
//        _messages.update { current -> current + message }
//    }
//
//    private fun extractSearchKeyword(message: String): String {
//        val cleaned = SEARCH_PATTERN.replace(message, " ")
//        return cleaned.replace(WHITESPACE_PATTERN, " ").trim()
//    }
//}