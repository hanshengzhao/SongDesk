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
    /** 四轮胎压 kPa（FL/FR/RL/RR） */
    val tirePressures: Map<String, Int> = mapOf("左前" to 241, "右前" to 243, "左后" to 238, "右后" to 240),
    val cabinFilterOk: Boolean = true,
    val internalRecirc: Boolean = false,
    // ---- climate ----
    val acOn: Boolean = true,
    val tempC: Int = 23,          // 18..28 主驾
    val passengerTempC: Int = 23, // 18..28 副驾
    val fanLevel: Int = 2,        // 0..7
    val blowFace: Boolean = true,
    val blowFoot: Boolean = false,
    val defrostFront: Boolean = false,
    val defrostRear: Boolean = false,
    val windowOpen: Map<WindowId, Boolean> = WindowId.entries.associateWith { false },
    val driverSeatHeat: Int = 0,      // 0..3
    val passengerSeatHeat: Int = 0,
    val driverSeatVent: Int = 0,      // 0..3 通风
    val passengerSeatVent: Int = 0,
    val steeringHeat: Int = 0,        // 0..2 方向盘加热
    val trunkOpen: Boolean = false,
    val tripKm: Float = 12.4f,        // 本次行程
    val tripMinutes: Int = 18,
    val tripAvgKwh: Float = 13.6f,    // 平均电耗 kWh/100km
) {
    val allWindowsClosed: Boolean get() = windowOpen.values.none { it }
}
