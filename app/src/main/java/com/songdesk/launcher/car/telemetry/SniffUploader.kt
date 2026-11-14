package com.songdesk.launcher.car.telemetry

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

/**
 * 嗅探数据云上传：把抓到的原车广播字段 POST 到公开消息主题，
 * 开发者可远程拉取校准（`GET <endpoint>/json?poll=1`），车主无需在车机上 adb。
 *
 * 默认端点为 ntfy.sh 公共主题（免注册）；主题内容仅含广播字段，
 * 不含位置等隐私数据。
 */
object SniffUploader {

    const val DEFAULT_ENDPOINT = "https://ntfy.sh/songdesk-sniff-v1"

    @Volatile var endpoint: String = DEFAULT_ENDPOINT
    @Volatile private var enabled: Boolean = false

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val buffer = mutableListOf<String>()
    private var lastFlushAt = 0L

    fun setEnabled(on: Boolean) {
        enabled = on
        if (!on) synchronized(buffer) { buffer.clear() }
    }

    fun enqueue(line: String) {
        if (!enabled) return
        synchronized(buffer) { buffer += "${System.currentTimeMillis() / 1000} $line" }
        // 满 8 条立即上传，否则最多延迟 15 秒
        val shouldFlush = synchronized(buffer) { buffer.size >= 8 }
        if (shouldFlush || System.currentTimeMillis() - lastFlushAt > 15_000) flushAsync()
    }

    /** 手动触发一次上传（测试通道用） */
    fun sendTest(context: android.content.Context) {
        scope.launch {
            post(context.getString(com.songdesk.launcher.R.string.app_name) + " 嗅探测试 @ ${System.currentTimeMillis() / 1000}")
            android.widget.Toast.makeText(context, "测试消息已发送", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun flushAsync() {
        lastFlushAt = System.currentTimeMillis()
        val body = synchronized(buffer) {
            if (buffer.isEmpty()) null else buffer.joinToString("\n").also { buffer.clear() }
        } ?: return
        scope.launch { runCatching { post(body) } }
    }

    private fun post(text: String) {
        val conn = URL(endpoint).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.connectTimeout = 6000
        conn.readTimeout = 6000
        conn.setRequestProperty("Title", "SongDesk 原车广播嗅探")
        conn.setRequestProperty("Tags", "car")
        conn.outputStream.use { it.write(text.toByteArray(Charsets.UTF_8)) }
        conn.inputStream.close()
    }
}
