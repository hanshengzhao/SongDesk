package com.songdesk.launcher.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.songdesk.launcher.car.CarBridge
import com.songdesk.launcher.data.AppEntry
import com.songdesk.launcher.data.AppRepository
import com.songdesk.launcher.data.WeatherData
import androidx.compose.foundation.layout.fillMaxWidth

private val MAPS_CANDIDATES = arrayOf(
    "com.autonavi.amapauto",   // 高德车机版
    "com.autonavi.minimap",    // 高德手机版
    "com.baidu.BaiduMap",      // 百度地图
    "com.baidu.baidumaps",     // 百度地图车机版
)
private val MUSIC_CANDIDATES = arrayOf(
    "com.tencent.qqmusiccar",  // QQ音乐车机版
    "com.android.mediacenter", // 华为音乐
    "com.netease.cloudmusic",  // 网易云
    "com.kugou.android",       // 酷狗
)
private val PHONE_CANDIDATES = arrayOf(
    "com.android.dialer",
    "com.samsung.android.dialer",
    "com.android.contacts",
)

/** 主页 = 状态条 + 双英雄卡 + 导航卡(导航中) + 快捷任务 + 应用网格 */
@Composable
fun HomeScreen(
    repo: AppRepository,
    bridge: CarBridge,
    weather: WeatherData?,
    guideState: com.songdesk.launcher.car.GuideState,
    onCar: () -> Unit,
) {
    val context = LocalContext.current
    val apps = remember { repo.loadLaunchableApps() }

    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp)) {
        StatusStrip(weather)
        Spacer(Modifier.height(16.dp))
        HeroCards(bridge, weather, Modifier.height(200.dp), onCarClick = onCar)
        Spacer(Modifier.height(16.dp))
        if (guideState.navigating) {
            NavCard(guideState, Modifier.fillMaxWidth().height(120.dp))
            Spacer(Modifier.height(16.dp))
        }
        QuickTasks(bridge, repo, Modifier.height(132.dp))
        Spacer(Modifier.height(16.dp))
        AppGrid(apps, Modifier.weight(1f), repo) { app: AppEntry -> repo.launchPackage(app.packageName) }
        Spacer(Modifier.height(12.dp))
    }
}

/** 外部应用启动辅助（供 Dock 使用） */
fun launchDockAction(
    repo: AppRepository,
    action: DockAction,
    context: android.content.Context,
): Boolean {
    val ok = when (action) {
        DockAction.MAPS -> repo.launchFirst(*MAPS_CANDIDATES)
        DockAction.MUSIC -> repo.launchFirst(*MUSIC_CANDIDATES)
        DockAction.PHONE -> repo.launchFirst(*PHONE_CANDIDATES)
        DockAction.CAR, DockAction.SETTINGS -> true
    }
    if (!ok && action in setOf(DockAction.MAPS, DockAction.MUSIC, DockAction.PHONE)) {
        android.widget.Toast.makeText(context, "未找到可用应用", android.widget.Toast.LENGTH_SHORT).show()
    }
    return ok
}
