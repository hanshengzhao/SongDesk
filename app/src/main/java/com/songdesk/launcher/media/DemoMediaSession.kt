package com.songdesk.launcher.media

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState

/**
 * 演示媒体会话：进程内创建 MediaSession 并发出 MediaStyle 通知，
 * 让音乐通知监听器抓到 token——模拟器上无需安装音乐 App 即可演示音乐卡。
 */
object DemoMediaSession {

    private const val CHANNEL_ID = "songdesk_demo_media"
    private const val DEMO_DURATION_MS = 252_000L  // 4:12
    private var session: MediaSession? = null
    private var playing = true
    private var anchorMs = 0L

    fun toggle(context: Context): Boolean {
        val app = context.applicationContext
        if (session != null) {
            session?.release(); session = null
            val nm = app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(2001)
            return false
        }
        val s = MediaSession(app, "SongDeskDemo")
        s.setMetadata(
            MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, "深空电台")
                .putString(MediaMetadata.METADATA_KEY_ARTIST, "湾流计划 · 蓝牙音乐")
                .putLong(MediaMetadata.METADATA_KEY_DURATION, DEMO_DURATION_MS)
                .build()
        )
        anchorMs = System.currentTimeMillis()
        s.setPlaybackState(buildState().build())
        s.setCallback(object : MediaSession.Callback() {
            override fun onPlay() { playing = true; anchorMs = System.currentTimeMillis() - pausedAt; push(app, s) }
            override fun onPause() { pausedAt = position(); playing = false; push(app, s) }
            override fun onSkipToNext() { anchorMs = System.currentTimeMillis(); push(app, s) }
            override fun onSkipToPrevious() { anchorMs = System.currentTimeMillis(); push(app, s) }
        })
        s.isActive = true
        session = s
        playing = true
        push(app, s)
        return true
    }

    private var pausedAt = 0L

    private fun position(): Long =
        ((System.currentTimeMillis() - anchorMs) % DEMO_DURATION_MS)

    private fun buildState(): PlaybackState.Builder =
        PlaybackState.Builder()
            .setState(
                if (playing) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED,
                position(),
                if (playing) 1f else 0f,
            )
            .setActions(
                PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or
                    PlaybackState.ACTION_SKIP_TO_NEXT or PlaybackState.ACTION_SKIP_TO_PREVIOUS
            )

    private fun push(context: Context, s: MediaSession) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "媒体演示", NotificationManager.IMPORTANCE_LOW)
        )
        val stateIcon = if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(stateIcon)
            .setContentTitle("深空电台")
            .setContentText("湾流计划 · 蓝牙音乐")
            .setOngoing(playing)
            .build()
        // 直接把 session token 放进 extras（等价于 MediaStyle 的做法），监听器据此 attach
        notification.extras.putParcelable(android.app.Notification.EXTRA_MEDIA_SESSION, s.sessionToken)
        nm.notify(2001, notification)
        NowPlayingStore.update(
            NowPlaying(
                pkg = context.packageName,
                title = "深空电台",
                artist = "湾流计划 · 蓝牙音乐",
                playing = playing,
                positionMs = position(),
                durationMs = DEMO_DURATION_MS,
            )
        )
    }
}
