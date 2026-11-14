package com.songdesk.launcher.commands

import android.content.Context
import android.media.AudioManager
import android.widget.Toast

/**
 * 系统级动作（音量/亮度/WiFi/蓝牙）。
 * 车机固件普遍放宽了这些权限（迪粉桌面同名指令即如此实现），
 * 但仍做防御式处理：失败静默 + toast 提示。
 */
object SystemActions {

    fun setVolume(context: Context, percent: Int) {
        runCatching {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            am.setStreamVolume(AudioManager.STREAM_MUSIC, (percent.coerceIn(0, 100) * max / 100), 0)
            toast(context, "音量 $percent%")
        }.onFailure { toast(context, "音量设置失败") }
    }

    fun setBrightness(context: Context, percent: Int) {
        runCatching {
            android.provider.Settings.System.putInt(
                context.contentResolver,
                android.provider.Settings.System.SCREEN_BRIGHTNESS,
                (percent.coerceIn(5, 100)) * 255 / 100,
            )
            toast(context, "屏幕亮度 $percent%")
        }.onFailure { toast(context, "亮度设置失败（需要修改系统设置权限）") }
    }

    fun setWifi(context: Context, on: Boolean) {
        runCatching {
            @Suppress("DEPRECATION")
            val wm = context.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
            val ok = wm.setWifiEnabled(on)
            toast(context, if (ok) (if (on) "WiFi 已开启" else "WiFi 已关闭") else "WiFi 切换失败")
        }.onFailure { toast(context, "WiFi 切换失败") }
    }

    fun setBluetooth(context: Context, on: Boolean) {
        runCatching {
            val adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
            val ok = if (on) adapter.enable() else adapter.disable()
            toast(context, if (ok) (if (on) "蓝牙开启中" else "蓝牙关闭中") else "蓝牙切换失败")
        }.onFailure { toast(context, "蓝牙切换失败") }
    }

    private fun toast(c: Context, msg: String) = Toast.makeText(c, msg, Toast.LENGTH_SHORT).show()
}
