package com.songdesk.launcher.car

import android.content.Context
import com.songdesk.launcher.car.telemetry.BroadcastTelemetryProvider
import com.songdesk.launcher.car.telemetry.MockTelemetryProvider
import com.songdesk.launcher.car.telemetry.SystemTelemetryProvider
import com.songdesk.launcher.car.telemetry.Telemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 车辆数据仓库：聚合多级遥测源。
 * 合并规则：按字段记录写入优先级——高优先级源（真车广播）写入后，低优先级源（Mock）
 * 不再覆盖该字段；气候部分始终由用户操作驱动。
 */
class CarRepository(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val broadcastProvider = BroadcastTelemetryProvider(appContext)
    private val systemProvider = SystemTelemetryProvider(appContext)
    private val mockProvider = MockTelemetryProvider(scope)

    private val _state = MutableStateFlow(CarState())
    val state: StateFlow<CarState> get() = _state

    val broadcastSource: BroadcastTelemetryProvider get() = broadcastProvider

    /** 各遥测字段的当前写入优先级 */
    private val fieldPrio = mutableMapOf<String, Int>()

    fun start() {
        listOf(mockProvider, systemProvider, broadcastProvider).forEach { p ->
            p.start { t -> mergeTelemetry(p.priority, t) }
        }
    }

    fun stop() {
        listOf(mockProvider, systemProvider, broadcastProvider).forEach { it.stop() }
        scope.cancel()
    }

    private fun mergeTelemetry(priority: Int, t: Telemetry) {
        _state.value = _state.value.copy(
            batteryPct = pick("battery", t.batteryPct, priority) ?: _state.value.batteryPct,
            rangeKm = pick("range", t.rangeKm, priority) ?: _state.value.rangeKm,
            charging = pick("charging", t.charging, priority) ?: _state.value.charging,
            gear = pick("gear", t.gear, priority) ?: _state.value.gear,
            insideTempC = pick("inTemp", t.insideTempC, priority) ?: _state.value.insideTempC,
            outsideTempC = pick("outTemp", t.outsideTempC, priority) ?: _state.value.outsideTempC,
            doorsLocked = pick("locked", t.doorsLocked, priority) ?: _state.value.doorsLocked,
            source = currentSource(),
        )
    }

    /** 高优先级源写入后，低优先级不得覆盖同字段 */
    private fun <T> pick(key: String, value: T?, priority: Int): T? {
        if (value == null) return null
        if ((fieldPrio[key] ?: Int.MIN_VALUE) > priority) return null
        fieldPrio[key] = priority
        return value
    }

    private fun currentSource(): DataSource = when {
        fieldPrio.values.any { it >= PRIO_REAL } -> DataSource.REAL
        fieldPrio.values.any { it >= PRIO_SYSTEM } -> DataSource.SYSTEM
        else -> DataSource.MOCK
    }

    /** 气候类命令写回状态（桥接层调用） */
    fun applyClimate(transform: (CarState) -> CarState) {
        _state.value = transform(_state.value)
    }

    private companion object {
        const val PRIO_REAL = 20
        const val PRIO_SYSTEM = 10
    }
}
