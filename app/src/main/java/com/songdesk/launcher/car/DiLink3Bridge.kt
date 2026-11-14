package com.songdesk.launcher.car

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.StateFlow

/**
 * 宋PLUS DM-i 21款（DiLink 3.0）桥接：
 * - 状态流来自 [CarRepository]（多级遥测聚合）
 * - 控制命令写本地状态（UI 即时回显）+ 真车上广播原车语音通道
 * - 广播协议字段为推断，上车后用协议嗅探器（SongDeskSniff tag）校准
 */
class DiLink3Bridge(context: Context, private val repository: CarRepository) : CarBridge {

    private val appContext = context.applicationContext

    override val state: StateFlow<CarState> get() = repository.state

    /** 原车空调应用是否存在（存在 = 极可能在真车上） */
    val onRealCar: Boolean = runCatching {
        appContext.packageManager.getPackageInfo("com.byd.airconditioning", 0)
    }.isSuccess

    private fun sendAutoVoice(command: String) {
        if (!onRealCar) return
        val intent = Intent("com.byd.action.AUTOVOICE_COMMON_OPERATION").apply {
            setPackage("com.byd.autovoice")
            putExtra("command", command)
        }
        runCatching { appContext.sendBroadcast(intent) }
    }

    override fun setAcOn(on: Boolean) {
        repository.applyClimate { it.copy(acOn = on) }
        sendAutoVoice(if (on) "空调开" else "空调关")
    }

    override fun setAcTemp(temp: Int) {
        repository.applyClimate { it.copy(tempC = temp.coerceIn(18, 28)) }
        sendAutoVoice("温度:${temp.coerceIn(18, 28)}")
    }

    override fun setFanLevel(level: Int) {
        repository.applyClimate { it.copy(fanLevel = level.coerceIn(0, 7)) }
        sendAutoVoice("风量:${level.coerceIn(0, 7)}")
    }

    override fun setBlowZone(face: Boolean, foot: Boolean) =
        repository.applyClimate { it.copy(blowFace = face, blowFoot = foot) }

    override fun setDefrost(front: Boolean, rear: Boolean) {
        repository.applyClimate { it.copy(defrostFront = front, defrostRear = rear) }
        if (front) sendAutoVoice("前除霜开")
        if (rear) sendAutoVoice("后除霜开")
    }

    override fun setWindowOpen(id: WindowId, open: Boolean) {
        repository.applyClimate { s -> s.copy(windowOpen = s.windowOpen + (id to open)) }
        if (open) sendAutoVoice("一键开窗") // 逐窗指令协议待上车确认，v2 粗粒度
    }

    override fun closeAllWindows() {
        repository.applyClimate { s -> s.copy(windowOpen = WindowId.entries.associateWith { false }) }
        sendAutoVoice("一键关窗")
    }

    override fun setSeatHeat(seat: SeatId, level: Int) = repository.applyClimate { s ->
        val v = level.coerceIn(0, 3)
        when (seat) {
            SeatId.DRIVER -> s.copy(driverSeatHeat = v)
            SeatId.PASSENGER -> s.copy(passengerSeatHeat = v)
        }
    }
}
