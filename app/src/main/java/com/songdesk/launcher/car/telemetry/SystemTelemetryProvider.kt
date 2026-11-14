package com.songdesk.launcher.car.telemetry

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

/**
 * 安卓系统层遥测：12V 小电瓶电量/充电状态。
 * 任何安卓车机都有，可用于判断"车是否通电"，但不是动力电池 SOC。
 */
class SystemTelemetryProvider(private val context: Context) :
    BaseProvider(priority = PRIORITY_SYSTEM) {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) {
            val level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val status = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val plugged = i.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val pct = if (level >= 0 && scale > 0) (level * 100) / scale else null
            emit(
                Telemetry(
                    batteryPct = pct,
                    charging = if (plugged > 0) true else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) false else null,
                ),
                onUpdate
            )
        }
    }

    private lateinit var onUpdate: (Telemetry) -> Unit

    override fun start(onUpdate: (Telemetry) -> Unit) {
        this.onUpdate = onUpdate
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun stop() {
        runCatching { context.unregisterReceiver(receiver) }
    }

    companion object { const val PRIORITY_SYSTEM = 10 }
}
