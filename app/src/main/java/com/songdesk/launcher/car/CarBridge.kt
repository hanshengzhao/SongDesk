package com.songdesk.launcher.car

import kotlinx.coroutines.flow.StateFlow

/**
 * 车辆数据/控制抽象。
 * - [MockCarBridge]：模拟器/无车环境，本地回显
 * - [DiLink3Bridge]：宋PLUS DM-i 21款（DiLink 3.0）真实实现
 */
interface CarBridge {
    val state: StateFlow<CarState>

    fun setAcOn(on: Boolean)
    fun setAcTemp(temp: Int)
    fun setPassengerTemp(temp: Int)
    fun setFanLevel(level: Int)
    fun setBlowZone(face: Boolean, foot: Boolean)
    fun setDefrost(front: Boolean, rear: Boolean)
    fun setWindowOpen(id: WindowId, open: Boolean)
    fun closeAllWindows()
    fun setSeatHeat(seat: SeatId, level: Int)
    fun toggleInternalRecirc()
    fun setSeatVent(seat: SeatId, level: Int)
    fun setSteeringHeat(level: Int)
    fun toggleTrunk()
    fun findCar()
}
