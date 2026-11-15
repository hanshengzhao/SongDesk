package com.songdesk.launcher.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
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
import com.songdesk.launcher.car.SeatId
import com.songdesk.launcher.data.WeatherData
import com.songdesk.launcher.media.NowPlayingStore
import com.songdesk.launcher.ui.common.RingGauge
import com.songdesk.launcher.ui.common.VinylDisc
import com.songdesk.launcher.ui.common.rememberClock
import com.songdesk.launcher.ui.common.GlassChip
import com.songdesk.launcher.ui.theme.ClockHuge
import com.songdesk.launcher.ui.theme.OkGreen
import com.songdesk.launcher.ui.theme.TextPrimary
import com.songdesk.launcher.ui.theme.TextSecondary
import com.songdesk.launcher.ui.theme.WarnAmber

private fun mmss(ms: Long): String {
    val total = ms / 1000
    return "%02d:%02d".format(total / 60, total % 60)
}

/**
 * 主面板三列：时钟天气 | 正在播放（大黑胶 240dp 视觉锚点） | 车辆状态（130dp 圆环）。
 * 碎瓦片全部并入行内指标，消除空旷感。
 */
@Composable
fun MainPanelContent(
    bridge: CarBridge,
    weather: WeatherData?,
    modifier: Modifier = Modifier,
) {
    val state by bridge.state.collectAsState()
    val np by NowPlayingStore.state.collectAsState()

    Row(modifier = modifier.fillMaxWidth()) {
        // ============ 列1：时钟 + 天气 + 环境 ============
        Column(
            Modifier.weight(1.15f).fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(rememberClock("HH:mm"), style = ClockHuge, color = TextPrimary)
            Text(
                rememberClock("MM月dd日 EEEE"),
                style = TextStyle(fontSize = 16.sp),
                color = TextSecondary,
                modifier = Modifier.padding(top = 10.dp),
            )
            Spacer(Modifier.height(26.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(52.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(weather?.emoji ?: "🌤", fontSize = 26.sp)
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        weather?.let { "${it.tempC}°C  ${it.condition}" } ?: "天气 --",
                        style = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.SemiBold),
                        color = TextPrimary,
                    )
                    Text(
                        weather?.let { "今日 ${it.minC}° ~ ${it.maxC}°" } ?: "",
                        style = TextStyle(fontSize = 13.sp),
                        color = TextSecondary,
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            // 环境指标行
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (weather?.aqiLabel != null) {
                    Text("空气 ${weather.aqiLabel}", style = TextStyle(fontSize = 13.sp), color = OkGreen)
                    Dot()
                }
                Text("车内 ${state.insideTempC?.let { "${it}°" } ?: "--"}", style = TextStyle(fontSize = 13.sp), color = TextSecondary)
                Dot()
                Text("车外 ${state.outsideTempC?.let { "${it}°" } ?: "--"}", style = TextStyle(fontSize = 13.sp), color = TextSecondary)
                Dot()
                Text(
                    "胎压 ${state.tirePressures.values.minOrNull() ?: 240}~${state.tirePressures.values.maxOrNull() ?: 240} kPa",
                    style = TextStyle(fontSize = 13.sp), color = TextSecondary,
                )
            }
            Spacer(Modifier.height(18.dp))
            // 本次行程
            Row(verticalAlignment = Alignment.CenterVertically) {
                TripStat("12.4", "km")
                Spacer(Modifier.width(20.dp))
                TripStat("18", "min")
                Spacer(Modifier.width(20.dp))
                TripStat("13.6", "kWh/100km")
            }
        }

        Spacer(Modifier.width(14.dp))

        // ============ 列2：正在播放（大黑胶 240dp） ============
        Column(
            Modifier.weight(1.2f).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🎵", fontSize = 14.sp)
                Text("正在播放", style = TextStyle(fontSize = 14.sp), color = TextSecondary)
            }
            Spacer(Modifier.height(16.dp))
            VinylDisc(
                modifier = Modifier.size(240.dp),
                accent = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                if (np.title.isBlank()) "未在播放" else "《${np.title}》",
                style = TextStyle(fontSize = 21.sp, fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            np.artist.takeIf { it.isNotBlank() }?.let {
                Text(it, style = TextStyle(fontSize = 13.sp), color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
            }
            if (np.durationMs > 0) {
                Spacer(Modifier.height(12.dp))
                Box(
                    Modifier
                        .fillMaxWidth(0.72f)
                        .height(5.dp)
                        .background(color = Color.White.copy(alpha = 0.12f), shape = RoundedCornerShape(3.dp))
                ) {
                    val frac = if (np.durationMs > 0) (np.positionMs.toFloat() / np.durationMs).coerceIn(0f, 1f) else 0f
                    Box(
                        Modifier
                            .fillMaxWidth(frac)
                            .height(5.dp)
                            .background(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(3.dp))
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "${mmss(np.positionMs)} / ${mmss(np.durationMs)}",
                    style = TextStyle(fontSize = 12.sp),
                    color = TextSecondary,
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                RoundIconBtn(Icons.Filled.SkipPrevious, "上一首", 56.dp) {
                    NowPlayingStore.activeController?.transportControls?.skipToPrevious()
                }
                RoundIconBtn(
                    if (np.playing && np.title.isNotBlank()) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    "播放/暂停",
                    76.dp,
                    filled = true,
                ) {
                    if (np.playing) NowPlayingStore.activeController?.transportControls?.pause()
                    else NowPlayingStore.activeController?.transportControls?.play()
                }
                RoundIconBtn(Icons.Filled.SkipNext, "下一首", 56.dp) {
                    NowPlayingStore.activeController?.transportControls?.skipToNext()
                }
            }
        }

        Spacer(Modifier.width(14.dp))

        // ============ 列3：车辆状态 ============
        Column(
            Modifier.weight(1.05f).fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🚗", fontSize = 14.sp)
                Text("车辆状态", style = TextStyle(fontSize = 14.sp), color = TextSecondary)
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                RingGauge(
                    progress = state.batteryPct / 100f,
                    color = OkGreen,
                    size = 130.dp,
                    strokeWidth = 9.dp,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${state.batteryPct}%",
                            style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                        )
                        Text("电量", style = TextStyle(fontSize = 12.sp), color = TextSecondary)
                    }
                }
                Column {
                    Text("纯电续航", style = TextStyle(fontSize = 12.sp), color = TextSecondary)
                    Text(
                        "${state.rangeKm} km",
                        style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (state.charging) "⚡ 充电中" else "综合 ${state.rangeKm * 2} km",
                        style = TextStyle(fontSize = 12.sp),
                        color = if (state.charging) OkGreen else TextSecondary,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusPill(
                    icon = "🔒",
                    text = "车门 ${if (state.doorsLocked) "已锁止" else "未锁"}",
                    ok = state.doorsLocked,
                )
                StatusPill(
                    icon = "🪟",
                    text = "车窗 ${if (state.allWindowsClosed) "已关闭" else "未关"}",
                    ok = state.allWindowsClosed,
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                RingGauge(
                    progress = (state.tempC - 18) / 10f,
                    color = MaterialTheme.colorScheme.primary,
                    size = 130.dp,
                    strokeWidth = 9.dp,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                "${state.tempC}",
                                style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                                color = TextPrimary,
                            )
                            Text("°C", style = TextStyle(fontSize = 13.sp), color = TextSecondary, modifier = Modifier.padding(bottom = 4.dp))
                        }
                        Text("空调温度", style = TextStyle(fontSize = 12.sp), color = TextSecondary)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("风量", style = TextStyle(fontSize = 13.sp), color = TextSecondary)
                        MiniRound("−") { bridge.setFanLevel((state.fanLevel - 1).coerceIn(0, 7)) }
                        Text(
                            "${state.fanLevel}",
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                            color = TextPrimary,
                            modifier = Modifier.width(16.dp),
                        )
                        MiniRound("＋") { bridge.setFanLevel((state.fanLevel + 1).coerceIn(0, 7)) }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("内循环", style = TextStyle(fontSize = 13.sp), color = TextSecondary)
                        GlassChip(
                            text = if (state.internalRecirc) "开" else "关",
                            selected = state.internalRecirc,
                        ) { bridge.toggleInternalRecirc() }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("座椅通风", style = TextStyle(fontSize = 13.sp), color = TextSecondary)
                SeatVentPicker("主驾", state.driverSeatVent) { bridge.setSeatVent(SeatId.DRIVER, it) }
                SeatVentPicker("副驾", state.passengerSeatVent) { bridge.setSeatVent(SeatId.PASSENGER, it) }
            }
        }
    }
}

@Composable
private fun Dot() {
    Box(
        Modifier
            .padding(horizontal = 8.dp)
            .size(4.dp)
            .background(color = TextSecondary.copy(alpha = 0.5f), shape = CircleShape)
    )
}

@Composable
private fun TripStat(value: String, unit: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(value, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold), color = TextPrimary)
        Spacer(Modifier.width(4.dp))
        Text(unit, style = TextStyle(fontSize = 12.sp), color = TextSecondary)
    }
}

@Composable
private fun SeatVentPicker(label: String, level: Int, onPick: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = TextStyle(fontSize = 13.sp), color = TextSecondary)
        listOf(0, 1, 2, 3).forEach { lv ->
            Text(
                if (lv == 0) "关" else "$lv",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = if (level == lv) FontWeight.Bold else FontWeight.Normal,
                ),
                color = if (level == lv) MaterialTheme.colorScheme.primary else TextSecondary,
                modifier = Modifier
                    .clickable { onPick(lv) }
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }
}

/** 圆形小按钮（− / ＋） */
@Composable
private fun MiniRound(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.10f),
    ) {
        Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
            Text(text, fontSize = 16.sp, color = TextPrimary)
        }
    }
}


/** 主面板圆形播控按钮 */
@Composable
private fun RoundIconBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    sizeDp: androidx.compose.ui.unit.Dp,
    filled: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (filled) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.10f),
    ) {
        Box(Modifier.size(sizeDp), contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = desc,
                tint = if (filled) Color(0xFF07090F) else TextPrimary,
                modifier = Modifier.size(sizeDp * 0.42f),
            )
        }
    }
}


/** 状态胶囊（车门/车窗/内循环） */
@Composable
private fun StatusPill(icon: String, text: String, ok: Boolean, onClick: (() -> Unit)? = null) {
    Surface(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.06f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
        ) {
            Text(icon, fontSize = 13.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                text,
                style = TextStyle(fontSize = 13.sp),
                color = if (ok) OkGreen else WarnAmber,
            )
        }
    }
}
