package com.songdesk.launcher.car

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** 本地回显实现：命令直接改状态，模拟器上完整可演示 */
open class MockCarBridge(initial: CarState = CarState()) : CarBridge {

    protected val _state = MutableStateFlow(initial)
    override val state: StateFlow<CarState> get() = _state

    private fun update(transform: (CarState) -> CarState) {
        _state.value = transform(_state.value)
    }

    override fun setAcOn(on: Boolean) = update { it.copy(acOn = on) }

    override fun setAcTemp(temp: Int) = update { it.copy(tempC = temp.coerceIn(18, 28)) }

    override fun setPassengerTemp(temp: Int) = update { it.copy(passengerTempC = temp.coerceIn(18, 28)) }

    override fun setFanLevel(level: Int) = update { it.copy(fanLevel = level.coerceIn(0, 7)) }

    override fun setBlowZone(face: Boolean, foot: Boolean) =
        update { it.copy(blowFace = face, blowFoot = foot) }

    override fun setDefrost(front: Boolean, rear: Boolean) =
        update { it.copy(defrostFront = front, defrostRear = rear) }

    override fun setWindowOpen(id: WindowId, open: Boolean) =
        update { s -> s.copy(windowOpen = s.windowOpen + (id to open)) }

    override fun closeAllWindows() =
        update { s -> s.copy(windowOpen = WindowId.entries.associateWith { false }) }

    override fun toggleInternalRecirc() = update { it.copy(internalRecirc = !it.internalRecirc) }

    override fun setSeatVent(seat: SeatId, level: Int) = update { s ->
        val v = level.coerceIn(0, 3)
        when (seat) {
            SeatId.DRIVER -> s.copy(driverSeatVent = v)
            SeatId.PASSENGER -> s.copy(passengerSeatVent = v)
        }
    }

    override fun setSteeringHeat(level: Int) = update { it.copy(steeringHeat = level.coerceIn(0, 2)) }

    override fun toggleTrunk() = update { it.copy(trunkOpen = !it.trunkOpen) }

    override fun findCar() { /* 真车：闪灯鸣笛；Mock 无操作 */ }

    override fun setSeatHeat(seat: SeatId, level: Int) = update {
        val v = level.coerceIn(0, 3)
        when (seat) {
            SeatId.DRIVER -> it.copy(driverSeatHeat = v)
            SeatId.PASSENGER -> it.copy(passengerSeatHeat = v)
        }
    }
}
