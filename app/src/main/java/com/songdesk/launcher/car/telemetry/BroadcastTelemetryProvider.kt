package com.songdesk.launcher.car.telemetry

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log

/**
 * BYD 广播遥测源（真车生效）。
 *
 * 监听已知/疑似的车况广播 action（来自对迪粉桌面 v2.6 的逆向与社区资料），
 * 防御式解析 extras——协议字段在不同 DiLink 版本上有差异，解析不到就记录原始内容，
 * 供上车后校准（logcat tag = [TAG]）。
 */
class BroadcastTelemetryProvider(private val context: Context) :
    BaseProvider(priority = PRIORITY_REAL) {

    private lateinit var onUpdate: (Telemetry) -> Unit
    private var sniff: Boolean = false

    /** 候选 action：真车上逐个验证，未知字段全部记录 */
    private val actions = listOf(
        "android.byd.intent.action.DATA_CHANGED",
        "com.byd.action.CAR_INFO",
        "com.byd.autovoice.CAR_STATE",
        "byd.intent.action.APP_BRIGHTNESS",
        "com.byd.action.AVCSERVICE",
    )

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) {
            if (sniff) {
                val line = "action=" + i.action + " extras=" + extrasToString(i)
                Log.i(TAG, "SNIFF $line")
                SniffUploader.enqueue(line)
            }
            val ex = i.extras ?: return
            // 防御式解析：字段名以真车实测为准
            val soc = ex.getInt("soc", ex.getInt("SOC", ex.getInt("battery", -1)))
            val range = ex.getInt("range", ex.getInt("rangeKm", -1))
            val gear = ex.getString("gear") ?: ex.getString("GEAR")
            val inside = ex.getInt("insideTemp", ex.getInt("inTemp", -999))
            val outside = ex.getInt("outsideTemp", ex.getInt("outTemp", -999))
            val locked = when (ex.get("locked")) {
                true -> true; false -> false; else -> null
            }
            emit(
                Telemetry(
                    batteryPct = soc.takeIf { it in 0..100 },
                    rangeKm = range.takeIf { it >= 0 },
                    gear = gear?.takeIf { it in "PRND" },
                    insideTempC = inside.takeIf { it != -999 },
                    outsideTempC = outside.takeIf { it != -999 },
                    doorsLocked = locked,
                ),
                onUpdate
            )
        }
    }

    override fun start(onUpdate: (Telemetry) -> Unit) {
        this.onUpdate = onUpdate
        val f = IntentFilter()
        actions.forEach { f.addAction(it) }
        // BYD 车机系统应用的广播来自其他应用，必须 RECEIVER_EXPORTED 才收得到
        androidx.core.content.ContextCompat.registerReceiver(
            context, receiver, f, androidx.core.content.ContextCompat.RECEIVER_EXPORTED,
        )
    }

    override fun stop() {
        runCatching { context.unregisterReceiver(receiver) }
    }

    /** 打开/关闭协议嗅探（上车校准用） */
    fun setSniffing(enabled: Boolean) { sniff = enabled }

    /** extras 序列化（避免深层嵌套字符串模板） */
    private fun extrasToString(i: Intent): String {
        val ex = i.extras ?: return "{}"
        val body = ex.keySet().joinToString(", ") { k ->
            val v = when {
                i.getStringExtra(k) != null -> i.getStringExtra(k)!!
                i.getIntExtra(k, Int.MIN_VALUE) != Int.MIN_VALUE -> i.getIntExtra(k, Int.MIN_VALUE).toString()
                i.getLongExtra(k, Long.MIN_VALUE) != Long.MIN_VALUE -> i.getLongExtra(k, Long.MIN_VALUE).toString()
                i.getBooleanExtra(k, false) != false -> i.getBooleanExtra(k, false).toString()
                else -> "?"
            }
            "$k=$v"
        }
        return "{$body}"
    }

    companion object {
        const val TAG = "SongDeskSniff"
        const val PRIORITY_REAL = 20
    }
}
