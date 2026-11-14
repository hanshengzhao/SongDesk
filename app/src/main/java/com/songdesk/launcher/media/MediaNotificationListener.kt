package com.songdesk.launcher.media

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** 正在播放的音乐状态（来自媒体样式通知，需授予"通知使用权"） */
data class NowPlaying(
    val pkg: String = "",
    val title: String = "",
    val artist: String = "",
    val playing: Boolean = false,
)

object NowPlayingStore {
    private val _state = MutableStateFlow(NowPlaying())
    val state: StateFlow<NowPlaying> get() = _state

    /** 当前会话控制器：音乐卡用它发送播放/暂停/切歌 */
    internal var controller: MediaController? = null

    /** 演示模式：进程内合成媒体会话（模拟器无音乐 App 时展示卡片） */
    @Volatile var demoController: MediaController? = null

    /** 有效控制器：真实优先，演示兜底 */
    val activeController: MediaController?
        get() = controller ?: demoController

    internal fun update(s: NowPlaying) { _state.value = s }

    fun current(): NowPlaying = _state.value

    fun transport(playPause: () -> Unit, next: () -> Unit): Boolean {
        val c = activeController ?: return false
        return runCatching {
            val ps = c.playbackState ?: return@runCatching false
            when (ps.state) {
                android.media.session.PlaybackState.STATE_PLAYING -> playPause()
                else -> next()
            }
            true
        }.getOrDefault(false)
    }
}

class MediaNotificationListener : NotificationListenerService() {

    private val controllers = mutableMapOf<String, MediaController>()

    private val callback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = pushLatest()
        override fun onPlaybackStateChanged(state: android.media.session.PlaybackState?) = pushLatest()
    }

    override fun onListenerConnected() {
        activeNotifications.forEach { tryAttach(it) }
        pushLatest()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        tryAttach(sbn)
        pushLatest()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        controllers.remove(sbn.packageName)
        pushLatest()
    }

    private fun tryAttach(sbn: StatusBarNotification) {
        val token = sbn.notification.extras.getParcelable(android.app.Notification.EXTRA_MEDIA_SESSION)
            as? MediaSession.Token ?: return
        if (controllers.containsKey(sbn.packageName)) return
        runCatching {
            val mc = MediaController(this, token)
            mc.registerCallback(callback)
            controllers[sbn.packageName] = mc
        }
    }

    private fun pushLatest() {
        NowPlayingStore.controller = controllers.values.firstOrNull()
        val playing = controllers.values.firstOrNull {
            it.playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING
        } ?: controllers.values.firstOrNull() ?: run {
            NowPlayingStore.update(NowPlaying())
            return
        }
        val md = playing.metadata
        NowPlayingStore.update(
            NowPlaying(
                pkg = playing.packageName,
                title = md?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "",
                artist = md?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "",
                playing = playing.playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING,
            )
        )
    }
}

/** 通知使用权辅助 */
object NotificationAccess {
    fun isEnabled(): Boolean? = null // 无法可靠查询，UI 用"是否收到过媒体数据"近似

    fun openSettings(context: Context) {
        context.startActivity(
            Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
