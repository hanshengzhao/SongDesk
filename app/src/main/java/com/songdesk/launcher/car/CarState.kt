package com.songdesk.launcher.car

/** 四门车窗 + 天窗 */
enum class WindowId { FL, FR, RL, RR, SUNROOF }

enum class SeatId { DRIVER, PASSENGER }

/** 数据来源标记：UI 需要诚实告诉用户当前数字是真是假 */
enum class DataSource { MOCK, SYSTEM, REAL }

/**
 * 车辆状态快照。
 * - climate 部分（空调/车窗/座椅）由用户操作驱动
 * - telemetry 部分（电量/续航/档位/温度）由 TelemetryProvider 驱动
 */
data class CarState(
    // ---- telemetry ----
    val batteryPct: Int = 62,
    val rangeKm: Int = 453,
    val charging: Boolean = false,
    val gear: String = "P",               // P R N D
    val insideTempC: Int? = null,
    val outsideTempC: Int? = null,
    val doorsLocked: Boolean = true,
    val source: DataSource = DataSource.MOCK,
    // ---- climate ----
    val acOn: Boolean = true,
    val tempC: Int = 23,          // 18..28
    val fanLevel: Int = 2,        // 0..7
    val blowFace: Boolean = true,
    val blowFoot: Boolean = false,
    val defrostFront: Boolean = false,
    val defrostRear: Boolean = false,
    val windowOpen: Map<WindowId, Boolean> = WindowId.entries.associateWith { false },
    val driverSeatHeat: Int = 0,      // 0..3
    val passengerSeatHeat: Int = 0,
) {
    val allWindowsClosed: Boolean get() = windowOpen.values.none { it }
}
