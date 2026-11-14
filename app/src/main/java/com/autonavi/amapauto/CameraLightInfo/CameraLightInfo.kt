package com.autonavi.amapauto.CameraLightInfo

import android.os.Parcel
import android.os.Parcelable

/**
 * 高德车机版红绿灯信息 Parcelable。
 *
 * ⚠️ 跨进程反序列化要求：包名/类名/字段读写顺序必须与高德车机版完全一致，
 * 本文件是从迪粉桌面 APK 中携带的同名类 1:1 迁移（逆向所得），勿改动字段顺序。
 */
class CameraLightInfo() : Parcelable {
    var waitNum: Int = 0
    var direction: Int = 0
    var status: Int = 0
    var countDown: Int = 0
    var showType: Int = 0

    constructor(parcel: Parcel) : this() {
        readFrom(parcel)
    }

    private fun readFrom(parcel: Parcel) {
        waitNum = parcel.readInt()
        direction = parcel.readInt()
        status = parcel.readInt()
        countDown = parcel.readInt()
        showType = parcel.readInt()
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(waitNum)
        parcel.writeInt(direction)
        parcel.writeInt(status)
        parcel.writeInt(countDown)
        parcel.writeInt(showType)
    }

    override fun toString(): String =
        "{\"w\":$waitNum,\"d\":$direction,\"s\":$status,\"c\":$countDown,\"t\":$showType}"

    companion object CREATOR : Parcelable.Creator<CameraLightInfo> {
        override fun createFromParcel(parcel: Parcel): CameraLightInfo = CameraLightInfo(parcel)
        override fun newArray(size: Int): Array<CameraLightInfo?> = arrayOfNulls(size)
    }
}
