@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.example.melodyplayer.player.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.example.melodyplayer.MainActivity
import com.example.melodyplayer.R

class PlaybackService : MediaSessionService() {

    private lateinit var player: ExoPlayer
    private var mediaSession: MediaSession? = null
    private var notificationManager: PlayerNotificationManager? = null

    override fun onCreate() {
        super.onCreate()

        // 1. Tạo ExoPlayer rỗng
        player = ExoPlayer.Builder(this).build()

        // 2. Tạo MediaSession
        mediaSession = MediaSession.Builder(this, player)
            .setId("MelodyPlayerSession")
            .build()

        // 3. Tạo Notification Channel
        createNotificationChannel()

        // 4. Cấu hình PlayerNotificationManager
        notificationManager = PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            CHANNEL_ID
        )
            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    val intent = Intent(this@PlaybackService, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    return PendingIntent.getActivity(
                        this@PlaybackService, // Đã sửa lỗi 'PlaybackBảoService' ở đây
                        0,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }

                override fun getCurrentContentTitle(player: Player): CharSequence {
                    return player.mediaMetadata.title ?: "Không có tiêu đề"
                }

                override fun getCurrentContentText(player: Player): CharSequence? {
                    return player.mediaMetadata.artist
                }

                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ) = null
            })
            .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationPosted(
                    notificationId: Int,
                    notification: Notification,
                    ongoing: Boolean
                ) {
                    if (ongoing) {
                        startForeground(notificationId, notification)
                    } else {
                        stopForeground(STOP_FOREGROUND_DETACH)
                    }
                }

                override fun onNotificationCancelled(
                    notificationId: Int,
                    dismissedByUser: Boolean
                ) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            })
            .build().apply {
                setSmallIcon(R.drawable.ic_launcher_foreground) // ❗️ Đảm bảo bạn có drawable này
                setUseNextAction(true)
                setUsePreviousAction(true)
                setPlayer(player)
            }
    }

    // ✅ ĐÂY LÀ HÀM BẮT BUỘC PHẢI CÓ
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        notificationManager?.setPlayer(null)
        notificationManager = null
        mediaSession?.release()
        mediaSession = null
        player.release()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = ContextCompat.getSystemService(this, NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.playback_channel_name), // ❗️ Đảm bảo bạn có string này
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.playback_channel_description) // ❗️ Và cả string này
            }
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "melody_playback"
        private const val NOTIFICATION_ID = 1001
    }
}