package com.songdesk.launcher.car.telemetry

import android.content.Context
import kotlinx.coroutines.flow.StateFlow

/** 单项遥测数据（null = 该项本源不提供） */
data class Telemetry(
    val batteryPct: Int? = null,
    val rangeKm: Int? = null,
    val charging: Boolean? = null,
    val gear: String? = null,
    val insideTempC: Int? = null,
    val outsideTempC: Int? = null,
    val doorsLocked: Boolean? = null,
) {
    val hasRealData: Boolean
        get() = batteryPct != null || rangeKm != null || charging != null ||
            gear != null || insideTempC != null || outsideTempC != null || doorsLocked != null
}

/**
 * 遥测数据源。优先级：REAL(广播/原车接口) > SYSTEM(安卓系统层) > MOCK。
 * start() 注册监听，stop() 反注册；只能在主线程调用。
 */
interface TelemetryProvider {
    val priority: Int
    fun start(onUpdate: (Telemetry) -> Unit)
    fun stop()
}

/** 提供方公共基类：合并更新 */
abstract class BaseProvider(override val priority: Int) : TelemetryProvider {
    protected var latest = Telemetry()
    protected fun emit(patch: Telemetry, onUpdate: (Telemetry) -> Unit) {
        latest = Telemetry(
            batteryPct = patch.batteryPct ?: latest.batteryPct,
            rangeKm = patch.rangeKm ?: latest.rangeKm,
            charging = patch.charging ?: latest.charging,
            gear = patch.gear ?: latest.gear,
            insideTempC = patch.insideTempC ?: latest.insideTempC,
            outsideTempC = patch.outsideTempC ?: latest.outsideTempC,
            doorsLocked = patch.doorsLocked ?: latest.doorsLocked,
        )
        onUpdate(latest)
    }
}

/** 获取 context 的便捷扩展（provider 都需要） */
fun interface ContextProvider {
    fun context(): Context
}
