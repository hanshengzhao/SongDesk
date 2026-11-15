package com.songdesk.launcher.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.songdesk.launcher.car.CarBridge
import com.songdesk.launcher.car.GuideState
import com.songdesk.launcher.data.AppEntry
import com.songdesk.launcher.data.AppRepository
import com.songdesk.launcher.data.WeatherData
import com.songdesk.launcher.ui.common.GlassChip
import com.songdesk.launcher.ui.common.rememberClock
import com.songdesk.launcher.ui.theme.TextPrimary
import com.songdesk.launcher.ui.theme.TextSecondary

/** 主页 = 大容器（状态条+三列主面板+红绿灯悬浮徽章）+ 快捷任务行 + Dock */
@Composable
fun HomeScreen(
    repo: AppRepository,
    bridge: CarBridge,
    weather: WeatherData?,
    guideState: GuideState,
    hiddenPkgs: Set<String> = emptySet(),
    onVoiceCommand: (String) -> Unit = {},
    onOpenAppCenter: () -> Unit = {},
    onCar: () -> Unit,
) {
    val context = LocalContext.current
    val apps = remember { repo.loadLaunchableApps().filterNot { it.packageName in hiddenPkgs } }

    Box(Modifier.fillMaxSize()) {
        // 顶部蓝晕（背景）
        Box(
            Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(Color(0xFF14304F).copy(alpha = 0.35f), Color.Transparent)
                    )
                )
        )

        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
            // ============ 大容器 ============
            Box(Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color(0xCC0C121E),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp)) {
                        // ---- 状态条 ----
                        StatusRow(onControlCenter = onCar)
                        Spacer(Modifier.height(10.dp))
                        // ---- 三列主面板 ----
                        MainPanelContent(
                            bridge = bridge,
                            weather = weather,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                // ---- 红绿灯徽章：悬浮主卡右缘 ----
                if (guideState.lightCountDown != null) {
                    LightBadge(
                        seconds = guideState.lightCountDown!!,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .offset(x = 26.dp),
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ---- 导航卡（导航中显示：道路/距离/红绿灯读秒已在徽章） ----
            if (guideState.navigating) {
                NavCard(guideState, Modifier.fillMaxWidth().height(120.dp))
                Spacer(Modifier.height(14.dp))
            }

            // ---- 快捷任务卡行 ----
            QuickTasks(bridge, repo, Modifier.height(164.dp))

            Spacer(Modifier.height(12.dp))
        }
    }
}

/** 状态条：车型名 | 时间日期 —— 控制中心 */
@Composable
private fun StatusRow(onControlCenter: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("宋PLUS DM-i", style = TextStyle(fontSize = 14.sp), color = TextSecondary)
        Spacer(Modifier.width(12.dp))
        Text(rememberClock("HH:mm"), style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold), color = TextPrimary)
        Spacer(Modifier.width(10.dp))
        Text(rememberClock("MM月dd日 EEEE"), style = TextStyle(fontSize = 14.sp), color = TextSecondary)
        Spacer(Modifier.weight(1f))
        GlassChip(text = "⚙ 控制中心", selected = false, onClick = onControlCenter)
    }
}

/** 红绿灯倒计时悬浮徽章 */
@Composable
private fun LightBadge(seconds: Int, modifier: Modifier = Modifier) {
    val color = when {
        seconds <= 5 -> Color(0xFFEF4444)
        seconds <= 10 -> com.songdesk.launcher.ui.theme.WarnAmber
        else -> com.songdesk.launcher.ui.theme.OkGreen
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .border(2.dp, color, RoundedCornerShape(50))
            .background(color = Color(0xE6121A28), shape = RoundedCornerShape(50))
            .padding(horizontal = 18.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                "$seconds",
                style = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold),
                color = color,
            )
            Text("s", style = TextStyle(fontSize = 13.sp), color = TextSecondary, modifier = Modifier.padding(bottom = 4.dp))
        }
        Text("绿灯倒计时", style = TextStyle(fontSize = 11.sp), color = TextSecondary)
    }
}

/** 主面板三列（时钟天气 | 正在播放 | 车辆状态）——由 MainPanel.kt 提供 */

/** Dock 任务启动（地图/音乐/电话降级链） */
fun launchDockAction(
    repo: com.songdesk.launcher.data.AppRepository,
    action: DockAction,
    context: android.content.Context,
): Boolean {
    val maps = arrayOf("com.autonavi.amapauto", "com.autonavi.minimap", "com.baidu.BaiduMap", "com.baidu.baidumaps")
    val music = arrayOf("com.tencent.qqmusiccar", "com.android.mediacenter", "com.netease.cloudmusic", "com.kugou.android")
    val phone = arrayOf("com.android.dialer", "com.samsung.android.dialer", "com.android.contacts")
    val ok = when (action) {
        DockAction.MAPS -> repo.launchFirst(*maps)
        DockAction.MUSIC -> repo.launchFirst(*music)
        DockAction.PHONE -> repo.launchFirst(*phone)
        DockAction.APP_CENTER, DockAction.CAR, DockAction.SETTINGS -> true
    }
    if (!ok) {
        android.widget.Toast.makeText(context, "未找到可用的应用", android.widget.Toast.LENGTH_SHORT).show()
    }
    return ok
}
