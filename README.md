🎵 MelodyPlayer

Ứng dụng nghe nhạc hiện đại viết bằng Kotlin – Jetpack Compose – Media3 – Firebase
Hỗ trợ chatbot AI, giọng nói, mini-player nổi và nhiều tính năng nâng cao.

📌 Badges
<p align="left"> <img src="https://img.shields.io/badge/Android-14-3DDC84?logo=android&logoColor=white" /> <img src="https://img.shields.io/badge/Kotlin-1.9-blue?logo=kotlin" /> <img src="https://img.shields.io/badge/Jetpack%20Compose-UI-blueviolet?logo=jetpackcompose" /> <img src="https://img.shields.io/badge/Firebase-Auth%20%7C%20Firestore-FFCA28?logo=firebase" /> <img src="https://img.shields.io/badge/Media3-Playback-orange" /> </p>
📚 Mục lục

Giới thiệu

Kiến trúc chính

Chức năng người dùng

Xác thực & bảo mật

Thư viện & khám phá

Trình phát nhạc

Tìm kiếm

Chatbot AI

Cài đặt

Công nghệ sử dụng

Cấu trúc thư mục

Cài đặt & khởi động dự án

Ảnh màn hình

Tác giả

🎧 Giới thiệu

MelodyPlayer là ứng dụng nghe nhạc dành cho Android, xây dựng với Jetpack Compose, tích hợp Media3, lưu trữ dữ liệu với Firebase, hỗ trợ phát nhạc nền, tìm kiếm, chatbot AI và nhiều tiện ích khác.
Ứng dụng hướng đến thiết kế hiện đại, đơn giản nhưng mạnh mẽ.

🏛️ Kiến trúc chính
🔹 Điều hướng

MainActivity sử dụng NavHost quản lý các màn hình:

Auth (Login/Signup)

Home

Player

Search

Playlist

Collections

Collection Detail

Settings

🔹 Trình phát nền (Media3)

Điều khiển qua PlayerViewModel

Kết nối MediaController với PlaybackService

Theo dõi:

Trạng thái phát

Tiến trình

Lặp/ngẫu nhiên

Danh sách phát

🔹 Dữ liệu

Nhạc mặc định từ getDefaultSongs

Nhạc người dùng thêm

Nhạc Firestore (cloud)

Lưu trữ yêu thích qua DataStore

Bộ sưu tập qua SharedPreferences

🎼 Chức năng người dùng
🔐 Xác thực & bảo mật

Đăng nhập / đăng ký bằng email + mật khẩu

Firebase Auth

CAPTCHA tự tạo

Hiển thị/ẩn mật khẩu

OTP (qua backend API)

Lưu đăng nhập tự động

🎵 Thư viện & khám phá nhạc

Màn hình Home:

Lời chào theo thời gian

Danh sách nhạc mặc định + người dùng tải lên

Tìm kiếm tiêu đề / nghệ sĩ

Thêm bài hát từ bộ nhớ máy

Tìm kiếm bằng giọng nói

Playlist từ Firestore

Collections & chi tiết bộ sưu tập

▶️ Trình phát nhạc

Play / Pause / Next / Previous

Seek

Lặp 1 / lặp tất cả

Phát ngẫu nhiên

Mini-player nổi

Màn hình Player đầy đủ:

Ảnh bìa

Tiêu đề

Nghệ sĩ

Thanh tiến trình

Nút hành động nhanh

Phát nhạc từ:

URI cục bộ

Firestore

Tài nguyên tích hợp

🔍 Tìm kiếm

Tìm theo tiêu đề hoặc nghệ sĩ

Tải dữ liệu từ Firestore

Voice Search gợi ý từ khóa

🤖 Chatbot AI tích hợp

Bong bóng chat xuất hiện trên mọi màn hình

Giao tiếp với Gemini API

Animation mở/đóng

Lưu lịch sử hội thoại (local)

⚙️ Cài đặt

Hiển thị thông tin người dùng

Tùy chọn (placeholder)

Đăng xuất về màn hình Auth

🧩 Công nghệ sử dụng
Nhóm	Công nghệ
UI	Jetpack Compose, Material 3, Animation
Media	AndroidX Media3
Backend	Firebase Auth, Firestore, OTP API (OkHttp), Gemini API (Ktor)
Storage	DataStore Preferences, SharedPreferences
Khác	Coroutines, ViewModel, Navigation-Compose
📂 Cấu trúc thư mục
app/
 └─ src/main/java/com/example/melodyplayer/
     ├─ MainActivity.kt
     ├─ auth/
     ├─ home/
     ├─ player/
     ├─ search/
     ├─ playlist/
     ├─ otp/
     ├─ chatbot/
     └─ setting/

🚀 Cài đặt & khởi động dự án
1. Clone repo
git clone https://github.com/Aurora241/MelodyPlayer.git
cd MelodyPlayer

2. Mở bằng Android Studio

Chọn Android Studio Hedgehog+

Sync Gradle lần đầu

3. Tạo Firebase Project

Bật Firebase Auth (Email/Password)

Tạo Firestore

Thêm SHA-1 / SHA-256 nếu dùng Google Sign-In (tùy chọn)

4. Chạy ứng dụng

Chọn thiết bị / emulator

Nhấn Run

🖼️ Ảnh màn hình

Bạn có thể gửi ảnh, tôi thêm vào README cho đẹp.

👤 Tác giả

Aurora241
Phát triển bởi Kotlin / Compose với sự hỗ trợ của AI kể từ 2024.
