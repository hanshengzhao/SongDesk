package com.songdesk.launcher.speech

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Parcel
import android.speech.tts.TextToSpeech
import java.util.ArrayDeque
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 语音播报（v5）：
 * - 真车：绑定原车语音服务 com.byd.remote.Server（com.byd.autovoice），
 *   走 NaviTTSAIDL transaction 1 播报文本（协议逆向自迪粉桌面 BtBaseService/j3.a）
 * - 模拟器/绑定失败：回落安卓本地 TextToSpeech
 * - 播报请求排队，逐条播出
 */
object Speech {

    @Volatile var voiceOn: Boolean = true

    private const val DESCRIPTOR = "com.byd.autovoice.service.navitts.NaviTTSAIDL"
    private const val TTS_TRANSACTION = 1

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val queue = ArrayDeque<String>()
    private var appContext: Context? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var remoteBinder: IBinder? = null
    private var bound = false
    private var speaking = false

    fun init(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        runCatching {
            tts = TextToSpeech(appContext!!) { status -> ttsReady = status == TextToSpeech.SUCCESS; drain() }
        }
        bindRemote()
    }

    /** 真车绑定：原车语音服务的 NaviTTSAIDL */
    private fun bindRemote() {
        val c = appContext ?: return
        runCatching {
            val intent = Intent("com.byd.remote.Server").setPackage("com.byd.autovoice")
            if (c.packageManager.resolveService(intent, 0) != null) {
                bound = c.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            remoteBinder = binder
            drain()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            remoteBinder = null
        }
    }

    fun speak(text: String) {
        if (!voiceOn || text.isBlank()) return
        synchronized(queue) { queue.addLast(text) }
        drain()
    }

    private fun drain() {
        if (speaking) return
        val next = synchronized(queue) { queue.pollFirst() } ?: return
        speaking = true

        // 1) 真车 AIDL 播报
        val remote = remoteBinder
        if (remote != null) {
            val ok = runCatching {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeString(next)
                    data.writeStrongBinder(null)
                    remote.transact(TTS_TRANSACTION, data, reply, 0)
                    reply.readException()
                    true
                } finally {
                    data.recycle(); reply.recycle()
                }
            }.getOrDefault(false)
            if (ok) {
                speaking = false
                // 远端播报时长未知，节流防止叠音
                scope.launch { kotlinx.coroutines.delay(3000); drain() }
                return
            }
        }

        // 2) 本地 TTS 兜底
        val engine = tts
        if (engine != null && ttsReady) {
            engine.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) { speaking = false; drain() }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) { speaking = false; drain() }
            })
            engine.speak(next, TextToSpeech.QUEUE_ADD, null, "songdesk-$next.hashCode()")
            return
        }
        speaking = false
    }
}
