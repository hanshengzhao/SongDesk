package com.songdesk.launcher.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.songdesk.launcher.car.CarBridge
import com.songdesk.launcher.media.NowPlayingStore
import com.songdesk.launcher.ui.common.GlassCard
import com.songdesk.launcher.ui.common.GlassChip
import com.songdesk.launcher.ui.common.rememberClock
import com.songdesk.launcher.ui.theme.ClockHuge
import com.songdesk.launcher.ui.theme.GlassStroke
import com.songdesk.launcher.ui.theme.TextPrimary
import com.songdesk.launcher.ui.theme.TextSecondary
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.CircleShape

/**
 * 主面板：单张大卡承载时钟天气 / 音乐播控 / 车况温控三列，
 * 竖分隔线分区——消除卡片海，视觉重心集中。
 */
@Composable
fun HeroCards(
    bridge: CarBridge,
    weather: com.songdesk.launcher.data.WeatherData?,
    modifier: Modifier = Modifier,
    onCarClick: () -> Unit,
    onLaunchMusic: () -> Unit = {},
) {
    val state by bridge.state.collectAsState()
    val np by com.songdesk.launcher.media.NowPlayingStore.state.collectAsState()

    GlassCard(modifier, contentFill = true) {
        Row(Modifier.fillMaxSize()) {
            // ---- 列1：时钟 + 天气 ----
            Column(
                Modifier.weight(1.15f).fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(rememberClock("HH:mm"), style = ClockHuge, color = TextPrimary)
                Text(
                    rememberClock("MM月dd日 EEEE"),
                    style = TextStyle(fontSize = 14.sp),
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Spacer(Modifier.height(14.dp))
                // 天气
                val w = weather
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        w?.emoji ?: "🌤",
                        fontSize = 40.sp,
                        color = if (w != null) MaterialTheme.colorScheme.primary else TextSecondary,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            w?.let { "${it.tempC}° ${it.condition}" } ?: "天气 --",
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
                            color = TextPrimary,
                        )
                        Text(
                            w?.let { "${it.city} · ${it.minC}~${it.maxC}°" } ?: "",
                            style = TextStyle(fontSize = 13.sp),
                            color = TextSecondary,
                        )
                    }
                }
                if (w?.alertTitle != null) {
                    Text(
                        "⚠ ${w.alertTitle}",
                        style = TextStyle(fontSize = 13.sp),
                        color = com.songdesk.launcher.ui.theme.WarnAmber,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }

            VerticalDivider()

            // ---- 列2：音乐播控 ----
            Column(
                Modifier.weight(0.9f).fillMaxHeight().padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val np = np
                if (np.title.isBlank()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            androidx.compose.material.icons.Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(56.dp)
                                .clickable { onLaunchMusic() },
                        )
                        Spacer(Modifier.height(10.dp))
                        Text("点击播放音乐", style = TextStyle(fontSize = 14.sp), color = TextSecondary)
                    }
                } else {
                    Text("正在播放", style = TextStyle(fontSize = 12.sp), color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        np.title,
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                    Text(
                        np.artist,
                        style = TextStyle(fontSize = 13.sp),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        PanelButton(androidx.compose.material.icons.Icons.Filled.SkipPrevious, "上一首") {
                            NowPlayingStore.activeController?.transportControls?.skipToPrevious()
                        }
                        PanelButton(
                            if (np.playing) androidx.compose.material.icons.Icons.Filled.Pause
                            else androidx.compose.material.icons.Icons.Filled.PlayArrow,
                            if (np.playing) "暂停" else "播放",
                        ) {
                            if (np.playing) NowPlayingStore.activeController?.transportControls?.pause()
                            else NowPlayingStore.activeController?.transportControls?.play()
                        }
                        PanelButton(androidx.compose.material.icons.Icons.Filled.SkipNext, "下一首") {
                            NowPlayingStore.activeController?.transportControls?.skipToNext()
                        }
                    }
                }
            }

            VerticalDivider()

            // ---- 列3：车况 + 温控 ----
            Column(
                Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        androidx.compose.material.icons.Icons.Filled.Battery5Bar,
                        contentDescription = null,
                        tint = if (state.charging) com.songdesk.launcher.ui.theme.OkGreen else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${state.batteryPct}%${if (state.charging) " ⚡" else ""}  ·  ${state.rangeKm}km",
                        style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
                        color = TextPrimary,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        when (state.source) {
                            com.songdesk.launcher.car.DataSource.REAL -> "实车"
                            com.songdesk.launcher.car.DataSource.SYSTEM -> "系统"
                            com.songdesk.launcher.car.DataSource.MOCK -> "演示"
                        },
                        style = TextStyle(fontSize = 11.sp),
                        color = TextSecondary,
                        modifier = Modifier
                            .background(
                                color = Color.White.copy(alpha = 0.08f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        androidx.compose.material.icons.Icons.Filled.Lock,
                        contentDescription = null,
                        tint = if (state.doorsLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        buildString {
                            append(if (state.doorsLocked) "已锁" else "未锁")
                            append(" · ${if (state.allWindowsClosed) "车窗全关" else "车窗未关"}")
                            if (state.insideTempC != null) append(" · 车内 ${state.insideTempC}°")
                        },
                        style = TextStyle(fontSize = 13.sp),
                        color = TextSecondary,
                    )
                }
                Spacer(Modifier.height(12.dp))
                // 快速温控
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                    color = if (state.acOn) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                    else Color.White.copy(alpha = 0.05f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Text(
                            "AC",
                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                            color = if (state.acOn) MaterialTheme.colorScheme.primary else TextSecondary,
                        )
                        Spacer(Modifier.weight(1f))
                        Surface(
                            onClick = { bridge.setAcTemp(state.tempC - 1) },
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.10f),
                        ) {
                            Text("−", fontSize = 18.sp, color = TextPrimary, modifier = Modifier.padding(horizontal = 12.dp))
                        }
                        Text(
                            "${state.tempC}°",
                            style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                            color = if (state.acOn) MaterialTheme.colorScheme.primary else TextSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                        Surface(
                            onClick = { bridge.setAcTemp(state.tempC + 1) },
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.10f),
                        ) {
                            Text("＋", fontSize = 15.sp, color = TextPrimary, modifier = Modifier.padding(horizontal = 10.dp))
                        }
                        Spacer(Modifier.weight(1f))
                        GlassChip(text = "车控 ›", selected = false, onClick = onCarClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        Modifier
            .width(1.dp)
            .fillMaxHeight(0.82f)
            .background(GlassStroke)
    )
}

/** 主面板圆钮 */
@Composable
private fun PanelButton(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)) {
        Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = desc, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        }
    }
}
