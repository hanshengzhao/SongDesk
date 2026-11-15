package com.songdesk.launcher.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.runtime.remember
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
import com.songdesk.launcher.ui.theme.ClockHuge
import com.songdesk.launcher.ui.theme.OkGreen
import com.songdesk.launcher.ui.theme.TextPrimary
import com.songdesk.launcher.ui.theme.TextSecondary

private fun mmss(ms: Long): String {
    val s = ms / 1000
    return "%02d:%02d".format(s / 60, s % 60)
}

/** 主面板三列：时钟天气 | 正在播放（黑胶） | 车辆状态（圆环） */
@Composable
fun MainPanelContent(
    bridge: CarBridge,
    weather: WeatherData?,
    modifier: Modifier = Modifier,
) {
    val state by bridge.state.collectAsState()
    val np by NowPlayingStore.state.collectAsState()

    Row(modifier = modifier.fillMaxWidth()) {
        // ============ 列1：时钟 + 天气 ============
        Column(
            Modifier.weight(1.1f).fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(rememberClock("HH:mm"), style = ClockHuge, color = TextPrimary)
            Text(
                rememberClock("MM月dd日 EEEE"),
                style = TextStyle(fontSize = 15.sp),
                color = TextSecondary,
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(44.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(weather?.emoji ?: "🌤", fontSize = 22.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        weather?.let { "${it.tempC}°C ${it.condition}" } ?: "天气 --",
                        style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
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
            // 空气质量 + 车内外温度
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoTile(
                    label = "空气质量",
                    value = if (weather?.aqiLabel != null) weather.aqiLabel else "优",
                    tint = com.songdesk.launcher.ui.theme.OkGreen,
                )
                InfoTile(
                    label = "车内温度",
                    value = state.insideTempC?.let { "${it}°C" } ?: "--",
                    tint = TextPrimary,
                )
                InfoTile(
                    label = "车外温度",
                    value = state.outsideTempC?.let { "${it}°C" } ?: "--",
                    tint = TextPrimary,
                )
            }
            Spacer(Modifier.height(14.dp))
            // 胎压 + 本次行程
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoTile(
                    label = "胎压 左前/右前",
                    value = "${state.tirePressures["左前"]}/${state.tirePressures["右前"]} kPa",
                    tint = TextPrimary,
                )
                InfoTile(
                    label = "胎压 左后/右后",
                    value = "${state.tirePressures["左后"]}/${state.tirePressures["右后"]} kPa",
                    tint = TextPrimary,
                )
            }
            Spacer(Modifier.height(14.dp))
            // 本次行程
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoTile(label = "本次行程", value = "12.4 km", tint = TextPrimary)
                InfoTile(label = "行驶时长", value = "18 分钟", tint = TextPrimary)
                InfoTile(label = "平均电耗", value = "13.6 kWh", tint = com.songdesk.launcher.ui.theme.OkGreen)
            }
        }

        Spacer(Modifier.width(8.dp))

        // ============ 列2：正在播放（黑胶唱片） ============
        Column(
            Modifier.weight(1f).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🎵", fontSize = 14.sp)
                Text("正在播放", style = TextStyle(fontSize = 14.sp), color = TextSecondary)
            }
            Spacer(Modifier.height(12.dp))
            // 黑胶唱片
            VinylDisc(
                modifier = Modifier.size(150.dp),
                accent = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                if (np.title.isBlank()) "未在播放" else "《${np.title}》",
                style = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                np.artist,
                style = TextStyle(fontSize = 13.sp),
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp),
            )
            if (np.durationMs > 0) {
                Spacer(Modifier.height(10.dp))
                // 进度条
                Box(
                    Modifier
                        .fillMaxWidth(0.8f)
                        .height(4.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(2.dp),
                        )
                ) {
                    val frac = if (np.durationMs > 0) (np.positionMs.toFloat() / np.durationMs).coerceIn(0f, 1f) else 0f
                    Box(
                        Modifier
                            .fillMaxWidth(frac)
                            .height(4.dp)
                            .background(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(2.dp))
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "${mmss(np.positionMs)} / ${mmss(np.durationMs)}",
                    style = TextStyle(fontSize = 12.sp),
                    color = TextSecondary,
                )
            }
            Spacer(Modifier.height(14.dp))
            // 播控
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                RoundIconBtn(Icons.Filled.SkipPrevious, "上一首", 48.dp) {
                    NowPlayingStore.activeController?.transportControls?.skipToPrevious()
                }
                RoundIconBtn(
                    if (np.playing && np.title.isNotBlank()) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    "播放/暂停",
                    64.dp,
                    filled = true,
                ) {
                    if (np.playing) NowPlayingStore.activeController?.transportControls?.pause()
                    else NowPlayingStore.activeController?.transportControls?.play()
                }
                RoundIconBtn(Icons.Filled.SkipNext, "下一首", 48.dp) {
                    NowPlayingStore.activeController?.transportControls?.skipToNext()
                }
            }
            Spacer(Modifier.height(14.dp))
            // 音量控制（AudioManager）
            val ctx = androidx.compose.ui.platform.LocalContext.current
            val am = remember { ctx.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager }
            val vol = remember { androidx.compose.runtime.mutableIntStateOf(
                am.getStreamVolume(android.media.AudioManager.STREAM_MUSIC) * 100 /
                    (am.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC).coerceAtLeast(1))
            ) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(0.8f),
            ) {
                Text("音量", style = TextStyle(fontSize = 12.sp), color = TextSecondary)
                MiniRound("−") {
                    val nv = (vol.intValue - 10).coerceIn(0, 100)
                    vol.intValue = nv
                    com.songdesk.launcher.commands.SystemActions.setVolume(ctx, nv)
                }
                Box(
                    Modifier.weight(1f).height(6.dp)
                        .background(color = Color.White.copy(alpha = 0.10f), shape = RoundedCornerShape(3.dp))
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(vol.intValue / 100f)
                            .height(6.dp)
                            .background(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(3.dp))
                    )
                }
                MiniRound("＋") {
                    val nv = (vol.intValue + 10).coerceIn(0, 100)
                    vol.intValue = nv
                    com.songdesk.launcher.commands.SystemActions.setVolume(ctx, nv)
                }
                Text("${vol.intValue}%", style = TextStyle(fontSize = 12.sp), color = TextSecondary)
            }
        }

        Spacer(Modifier.width(8.dp))

        // ============ 列3：车辆状态（圆环仪表） ============
        Column(
            Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🚗", fontSize = 14.sp)
                Text("车辆状态", style = TextStyle(fontSize = 14.sp), color = TextSecondary)
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // 电量环
                RingGauge(
                    progress = state.batteryPct / 100f,
                    color = com.songdesk.launcher.ui.theme.OkGreen,
                    size = 92.dp,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${state.batteryPct}%",
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                        )
                        Text("电量", style = TextStyle(fontSize = 11.sp), color = TextSecondary)
                    }
                }
                Column {
                    Text("纯电续航", style = TextStyle(fontSize = 12.sp), color = TextSecondary)
                    Text(
                        "${state.rangeKm} km",
                        style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            // 门锁/车窗胶囊
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
                StatusPill(
                    icon = "🌬",
                    text = "内循环 ${if (state.internalRecirc) "开" else "关"}",
                    ok = state.internalRecirc,
                    onClick = { bridge.toggleInternalRecirc() },
                )
                StatusPill(
                    icon = "❄",
                    text = "主驾通风 ${if (state.driverSeatVent > 0) "${state.driverSeatVent}档" else "关"}",
                    ok = state.driverSeatVent > 0,
                    onClick = {
                        val nv = if (state.driverSeatVent > 0) 0 else 1
                        bridge.setSeatVent(SeatId.DRIVER, nv)
                    },
                )
                StatusPill(
                    icon = "♨",
                    text = "方向盘 ${if (state.steeringHeat > 0) "加热${state.steeringHeat}" else "未加热"}",
                    ok = state.steeringHeat > 0,
                    onClick = {
                        val nv = if (state.steeringHeat > 0) 0 else 1
                        bridge.setSteeringHeat(nv)
                    },
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // 空调温度环
                RingGauge(
                    progress = (state.tempC - 18) / 10f,
                    color = MaterialTheme.colorScheme.primary,
                    size = 92.dp,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${state.tempC}.0°",
                            style = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                        )
                        Text("空调温度", style = TextStyle(fontSize = 11.sp), color = TextSecondary)
                    }
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MiniRound("−") { bridge.setFanLevel((state.fanLevel - 1).coerceIn(0, 7)) }
                        Text(
                            "风量 · ${state.fanLevel}",
                            style = TextStyle(fontSize = 13.sp),
                            color = TextSecondary,
                        )
                        MiniRound("＋") { bridge.setFanLevel((state.fanLevel + 1).coerceIn(0, 7)) }
                    }
                }
            }
        }
    }
}

/** 状态胶囊（车门/车窗） */
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
                color = if (ok) OkGreen else com.songdesk.launcher.ui.theme.WarnAmber,
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

/** 圆形播控按钮（filled=true 时强调色填充） */
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
        color = if (filled) MaterialTheme.colorScheme.primary
        else Color.White.copy(alpha = 0.10f),
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

/** 信息小瓦片：标签在上、值在下 */
@Composable
private fun InfoTile(label: String, value: String, tint: Color) {
    Column(
        Modifier
            .background(color = Color.White.copy(alpha = 0.05f), shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(label, style = TextStyle(fontSize = 11.sp), color = TextSecondary)
        Text(value, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium), color = tint)
    }
}
