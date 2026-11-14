package com.autonavi.amapauto.CameraLightInfo

import android.os.Parcel
import android.os.Parcelable

/** 高德车机版红绿灯列表容器（同上：1:1 迁移，勿改字段顺序） */
class CameraLightInfoWrapper() : Parcelable {
    var trafficLights: List<CameraLightInfo> = emptyList()

    constructor(parcel: Parcel) : this() {
        readFrom(parcel)
    }

    private fun readFrom(parcel: Parcel) {
        val size = parcel.readInt()
        val list = ArrayList<CameraLightInfo>(size.coerceAtLeast(0))
        repeat(size.coerceAtLeast(0)) { list.add(parcel.readParcelable(CameraLightInfo::class.java.classLoader)!!) }
        trafficLights = list
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(trafficLights.size)
        for (light in trafficLights) parcel.writeParcelable(light, flags)
    }

    companion object CREATOR : Parcelable.Creator<CameraLightInfoWrapper> {
        override fun createFromParcel(parcel: Parcel): CameraLightInfoWrapper = CameraLightInfoWrapper(parcel)
        override fun newArray(size: Int): Array<CameraLightInfoWrapper?> = arrayOfNulls(size)
    }
}
