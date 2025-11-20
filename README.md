# 🎵 MelodyPlayer

Ứng dụng nghe nhạc hiện đại viết bằng **Kotlin -- Jetpack Compose --
Media3 -- Firebase**\
Hỗ trợ chatbot AI, giọng nói, mini-player nổi và nhiều tính năng nâng
cao.

![banner](https://dummyimage.com/1200x260/333/fff&text=MelodyPlayer+-+Music+App+Android)

## 📌 Badges

```{=html}
<p align="left">
```
`<img src="https://img.shields.io/badge/Android-14-3DDC84?logo=android&logoColor=white" />`{=html}
`<img src="https://img.shields.io/badge/Kotlin-1.9-blue?logo=kotlin" />`{=html}
`<img src="https://img.shields.io/badge/Jetpack%20Compose-UI-blueviolet?logo=jetpackcompose" />`{=html}
`<img src="https://img.shields.io/badge/Firebase-Auth%20%7C%20Firestore-FFCA28?logo=firebase" />`{=html}
`<img src="https://img.shields.io/badge/Media3-Playback-orange" />`{=html}
```{=html}
</p>
```
# 📚 Mục lục

-   [Giới thiệu](#giới-thiệu)
-   [Kiến trúc chính](#kiến-trúc-chính)
-   [Chức năng người dùng](#chức-năng-người-dùng)
-   [Công nghệ sử dụng](#công-nghệ-sử-dụng)
-   [Cấu trúc thư mục](#cấu-trúc-thư-mục)
-   [Cài đặt & khởi động dự án](#cài-đặt--khởi-động-dự-án)
-   [Ảnh màn hình](#ảnh-màn-hình)
-   [Tác giả](#tác-giả)

# 🎧 Giới thiệu

MelodyPlayer là ứng dụng nghe nhạc dành cho Android, xây dựng với
Jetpack Compose, tích hợp Media3, lưu trữ dữ liệu với Firebase, hỗ trợ
phát nhạc nền, tìm kiếm, chatbot AI và nhiều tiện ích khác.

# 🏛️ Kiến trúc chính

## Điều hướng

-   MainActivity quản lý NavHost các màn hình chính.

## Trình phát nền

-   PlayerViewModel điều khiển Media3.
-   Theo dõi tiến trình, trạng thái và danh sách phát.

## Dữ liệu

-   Nhạc mặc định, nhạc Firestore, nhạc người dùng thêm.
-   DataStore + SharedPreferences.

# 🎼 Chức năng người dùng

## Xác thực & bảo mật

-   Firebase Auth, CAPTCHA, OTP API, lưu đăng nhập.

## Thư viện & khám phá nhạc

-   Home: danh sách nhạc, thêm nhạc, voice search.
-   Playlist Firestore.
-   Collections + Chi tiết.

## Trình phát nhạc

-   Play/Pause/Next/Prev, seek, loop, shuffle.
-   Mini-player nổi & Player đầy đủ.

## Tìm kiếm

-   Search Firestore theo tiêu đề hoặc nghệ sĩ.
-   Voice Search tích hợp.

## Chatbot AI

-   Bong bóng chat mọi màn hình.
-   Gemini API bằng Ktor.

## Cài đặt

-   Thông tin tài khoản, logout.

# 🧩 Công nghệ sử dụng

-   Jetpack Compose, Material 3\
-   Media3\
-   Firebase Auth / Firestore\
-   DataStore, SharedPreferences\
-   OkHttp, Ktor Client

# 📂 Cấu trúc thư mục

app/src/main/java/com/example/melodyplayer/: - MainActivity.kt - auth/ -
home/ - player/ - search/ - playlist/ - otp/ - chatbot/ - setting/

# 🚀 Cài đặt & khởi động dự án

## Clone repo

    git clone https://github.com/Aurora241/MelodyPlayer.git
    cd MelodyPlayer

## Firebase

-   Bật Auth, tạo Firestore, thêm google-services.json

## Run

-   Mở Android Studio → Run

# 🖼️ Ảnh màn hình

(Thêm screenshot nếu cần)

# 👤 Tác giả

Aurora241
