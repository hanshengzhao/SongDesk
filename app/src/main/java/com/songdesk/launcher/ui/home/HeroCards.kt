package com.songdesk.launcher.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.songdesk.launcher.car.CarBridge
import com.songdesk.launcher.ui.common.GlassCard
import com.songdesk.launcher.ui.common.GlassChip
import com.songdesk.launcher.ui.common.rememberClock
import com.songdesk.launcher.ui.theme.ClockHuge
import com.songdesk.launcher.ui.theme.TextPrimary
import com.songdesk.launcher.ui.theme.TextSecondary

/** 首排大卡：左 = 时钟/日期 + 实时天气，右 = 车况速览 */
@Composable
fun HeroCards(
    bridge: CarBridge,
    weather: com.songdesk.launcher.data.WeatherData?,
    modifier: Modifier = Modifier,
    onCarClick: () -> Unit,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassCard(Modifier.weight(2f), contentFill = true) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(rememberClock("HH:mm"), style = ClockHuge, color = TextPrimary)
                    Text(
                        rememberClock("MM月dd日 EEEE"),
                        style = TextStyle(fontSize = 15.sp),
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                // 天气大图标 + 当前温度
                if (weather != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(weather.emoji, fontSize = 44.sp)
                        Text(
                            "${weather.tempC}° ${weather.condition}",
                            style = TextStyle(fontSize = 16.sp),
                            color = TextPrimary,
                        )
                    }
                }
            }
            Text(
                weather?.let { "${it.city} · 今日 ${it.minC}~${it.maxC}°${if (it.alertTitle != null) "  ⚠${it.alertTitle}" else ""}" }
                    ?: "",
                style = TextStyle(fontSize = 13.sp),
                color = if (weather?.alertTitle != null) com.songdesk.launcher.ui.theme.WarnAmber else TextSecondary,
            )
        }
        CarStatusCard(bridge, Modifier.weight(1f), onCarClick)
    }
}

@Composable
private fun CarStatusCard(
    bridge: CarBridge,
    modifier: Modifier = Modifier,
    onCarClick: () -> Unit,
) {
    val state by bridge.state.collectAsState()
    GlassCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("车辆状态", style = TextStyle(fontSize = 13.sp), color = TextSecondary)
            Spacer(Modifier.width(8.dp))
            // 数据来源徽标：诚实区分模拟/系统/实车
            Text(
                when (state.source) {
                    com.songdesk.launcher.car.DataSource.REAL -> "● 实车"
                    com.songdesk.launcher.car.DataSource.SYSTEM -> "● 系统"
                    com.songdesk.launcher.car.DataSource.MOCK -> "● 模拟"
                },
                style = TextStyle(fontSize = 11.sp),
                color = if (state.source == com.songdesk.launcher.car.DataSource.REAL)
                    com.songdesk.launcher.ui.theme.OkGreen else TextSecondary,
            )
            Spacer(Modifier.weight(1f))
            GlassChip(text = "车控 ›", selected = false, onClick = onCarClick)
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Battery5Bar,
                contentDescription = null,
                tint = if (state.charging) com.songdesk.launcher.ui.theme.OkGreen else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "${state.batteryPct}%${if (state.charging) " ⚡充电中" else ""}  ·  ${state.rangeKm}km",
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Lock,
                contentDescription = null,
                tint = if (state.doorsLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                buildString {
                    append(if (state.doorsLocked) "已锁" else "未锁")
                    append(" · 车窗${if (state.allWindowsClosed) "全关" else "未关"}")
                    append(" · 档位 ${state.gear}")
                    if (state.insideTempC != null) append(" · 车内 ${state.insideTempC}°")
                    if (state.outsideTempC != null) append(" / 车外 ${state.outsideTempC}°")
                },
                style = TextStyle(fontSize = 14.sp),
                color = TextSecondary,
            )
        }
        Spacer(Modifier.weight(1f))
        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
            color = if (state.acOn) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
            else com.songdesk.launcher.ui.theme.GlassFill,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    "AC",
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                    color = if (state.acOn) MaterialTheme.colorScheme.primary else TextSecondary,
                )
                Spacer(Modifier.width(8.dp))
                androidx.compose.material3.Surface(
                    onClick = { bridge.setAcTemp(state.tempC - 1) },
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.10f),
                ) {
                    Text("−", fontSize = 20.sp, color = TextPrimary, modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp))
                }
                Text(
                    "${state.tempC}°",
                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
                    color = if (state.acOn) MaterialTheme.colorScheme.primary else TextSecondary,
                    modifier = Modifier.padding(horizontal = 14.dp),
                )
                androidx.compose.material3.Surface(
                    onClick = { bridge.setAcTemp(state.tempC + 1) },
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.10f),
                ) {
                    Text("＋", fontSize = 16.sp, color = TextPrimary, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp))
                }
            }
        }
    }
}
