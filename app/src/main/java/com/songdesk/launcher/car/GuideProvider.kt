package com.songdesk.launcher.car

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.autonavi.amapauto.CameraLightInfo.CameraLightInfoWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** 导航引导状态（高德车机版广播驱动） */
data class GuideState(
    val navigating: Boolean = false,
    val roadName: String = "",       // 当前道路
    val nextRoadName: String = "",   // 下一道路/转向
    val remainDisM: Int? = null,     // 距下一转向剩余距离（米）
    val lightCountDown: Int? = null, // 红绿灯读秒（秒）
    val lightStatus: Int = -1,       // 1 绿 2 黄 3 红（高德 icon 语义，实测校准）
    val source: DataSource = DataSource.MOCK,
)

/**
 * 高德车机版导航/红绿灯数据源。
 * 协议来源：对迪粉桌面 GDmapService 的逆向——
 *  - action AUTONAVI_STANDARD_BROADCAST_SEND
 *  - KEY_TYPE=70088 → extras "EXTRA_DATA" → CameraLightInfoWrapper（红绿灯列表，countDown=读秒）
 *  - KEY_TYPE=1001/1002/1003 → 路名/转向/剩余距离（官方开放字段）
 */
class GuideProvider(private val context: Context) {

    private val _state = MutableStateFlow(GuideState())
    val state: StateFlow<GuideState> get() = _state

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) {
            val type = i.getIntExtra("KEY_TYPE", -1)
            val cur = _state.value
            when (type) {
                70088 -> {
                    val wrapper = runCatching {
                        i.extras?.getParcelable<CameraLightInfoWrapper>("EXTRA_DATA")
                    }.getOrNull()
                    val light = wrapper?.trafficLights?.maxByOrNull { it.countDown }
                    _state.value = cur.copy(
                        navigating = true,
                        lightCountDown = light?.countDown?.takeIf { it in 0..199 },
                        lightStatus = light?.status ?: cur.lightStatus,
                        source = DataSource.REAL,
                    )
                }
                1001 -> {
                    val road = i.getStringExtra("KEY_ROAD_NAME") ?: return
                    _state.value = cur.copy(navigating = true, roadName = road, source = DataSource.REAL)
                }
                1002 -> {
                    val next = i.getStringExtra("KEY_NEXT_ROAD_NAME") ?: return
                    _state.value = cur.copy(navigating = true, nextRoadName = next, source = DataSource.REAL)
                }
                1003 -> {
                    val dis = i.getIntExtra("KEY_SEG_REMAIN_DIS", -1)
                    if (dis >= 0) _state.value = cur.copy(navigating = true, remainDisM = dis, source = DataSource.REAL)
                }
                1000 -> _state.value = GuideState(source = cur.source) // 导航结束复位
            }
        }
    }

    fun start() {
        val f = IntentFilter("AUTONAVI_STANDARD_BROADCAST_SEND")
        ContextCompat.registerReceiver(
            context, receiver, f, ContextCompat.RECEIVER_EXPORTED,
        )
    }

    fun stop() {
        runCatching { context.unregisterReceiver(receiver) }
    }

    /** 导航演示：合成高德车机版广播（模拟器/无高德时开发用） */
    fun sendDemoBroadcast(countDown: Int, road: String, remainM: Int) {
        val i = Intent("AUTONAVI_STANDARD_BROADCAST_SEND").apply {
            setPackage(context.packageName)
            putExtra("KEY_TYPE", 70088)
            putExtra("EXTRA_DATA", CameraLightInfoWrapper().apply {
                trafficLights = listOf(
                    com.autonavi.amapauto.CameraLightInfo.CameraLightInfo().apply {
                        this.countDown = countDown
                        status = 1
                        direction = 1
                    }
                )
            })
        }
        context.sendBroadcast(i)
        val i2 = Intent("AUTONAVI_STANDARD_BROADCAST_SEND").apply {
            setPackage(context.packageName)
            putExtra("KEY_TYPE", 1001)
            putExtra("KEY_ROAD_NAME", road)
        }
        context.sendBroadcast(i2)
        val i3 = Intent("AUTONAVI_STANDARD_BROADCAST_SEND").apply {
            setPackage(context.packageName)
            putExtra("KEY_TYPE", 1003)
            putExtra("KEY_SEG_REMAIN_DIS", remainM)
        }
        context.sendBroadcast(i3)
    }
}
