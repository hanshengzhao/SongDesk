package com.songdesk.launcher.car.telemetry

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Mock 遥测：模拟一段日常通勤——电量缓降、续航跟随、档位周期变化。
 * 仅在没有真实数据时兜底展示，让 UI 演示不空转。
 */
class MockTelemetryProvider(private val scope: CoroutineScope) :
    BaseProvider(priority = PRIORITY_MOCK) {

    private var job: Job? = null
    private lateinit var onUpdate: (Telemetry) -> Unit
    private var soc = 62
    private var range = 453
    private var tick = 0

    override fun start(onUpdate: (Telemetry) -> Unit) {
        this.onUpdate = onUpdate
        job = scope.launch {
            emit(Telemetry(batteryPct = soc, rangeKm = range, gear = "P", doorsLocked = true), onUpdate)
            while (isActive) {
                delay(8_000)
                tick++
                if (soc > 18 && tick % 3 == 0) { soc -= 1; range = (soc * 7.3).toInt() }
                val gear = when ((tick / 2) % 6) { 0, 5 -> "P"; 1, 2 -> "D"; 3 -> "N"; else -> "D" }
                emit(
                    Telemetry(
                        batteryPct = soc,
                        rangeKm = range,
                        charging = tick % 17 == 0,
                        gear = gear,
                        insideTempC = 21 + (tick % 3),
                        outsideTempC = 14 + (tick % 4),
                    ),
                    onUpdate
                )
            }
        }
    }

    override fun stop() { job?.cancel(); job = null }

    companion object { const val PRIORITY_MOCK = 0 }
}
