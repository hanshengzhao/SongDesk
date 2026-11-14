package com.songdesk.launcher.ui.charge

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.songdesk.launcher.data.ChargeRecord
import com.songdesk.launcher.data.ChargeStore
import com.songdesk.launcher.ui.common.GlassCard
import com.songdesk.launcher.ui.common.GlassChip
import com.songdesk.launcher.ui.common.SectionLabel
import com.songdesk.launcher.ui.theme.OkGreen
import com.songdesk.launcher.ui.theme.TextPrimary
import com.songdesk.launcher.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 充电记录页：手动记录充电（SOC/费用），列表 + 删除 */
@Composable
fun ChargeScreen(store: ChargeStore, onBack: () -> Unit) {
    var records by remember { mutableStateOf(store.load()) }
    var socFrom by remember { mutableStateOf(20) }
    var socTo by remember { mutableStateOf(80) }
    var cost by remember { mutableStateOf(30.0) }

    fun addRecord() {
        records = store.add(ChargeRecord(System.currentTimeMillis(), socFrom, socTo, cost))
    }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(onClick = onBack, shape = CircleShape, color = MaterialTheme.colorScheme.surface) {
                Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = TextPrimary)
                }
            }
            Spacer(Modifier.width(16.dp))
            Text("充电记录", style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Medium), color = TextPrimary)
        }
        Spacer(Modifier.height(16.dp))

        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // ---- 左：录入表单 ----
            GlassCard(Modifier.weight(1f)) {
                SectionLabel("记录一次充电")
                Spacer(Modifier.height(14.dp))
                NumberRow("开始 SOC", socFrom, 0, 100) { socFrom = it }
                Spacer(Modifier.height(10.dp))
                NumberRow("结束 SOC", socTo, 0, 100) { socTo = it }
                Spacer(Modifier.height(10.dp))
                NumberRow("花费（元）", cost.toInt(), 0, 500) { cost = it.toDouble() }
                Spacer(Modifier.height(16.dp))
                GlassChip("保存记录", selected = false) {
                    if (socTo > socFrom) addRecord()
                }
                Spacer(Modifier.height(16.dp))
                if (records.isNotEmpty()) {
                    SectionLabel("统计")
                    Spacer(Modifier.height(6.dp))
                    val totalCost = records.sumOf { it.costYuan }
                    val totalKwh = records.sumOf { (it.socTo - it.socFrom).toDouble() * 0.6 } // 估算：60kWh 电池
                    Text(
                        "共 ${records.size} 次 · 合计 ${"%.0f".format(totalCost)} 元 · 约充入 ${"%.1f".format(totalKwh)} kWh",
                        style = TextStyle(fontSize = 14.sp), color = TextPrimary,
                    )
                }
            }

            // ---- 右：历史列表 ----
            GlassCard(Modifier.weight(1f), contentFill = true) {
                SectionLabel("历史记录")
                Spacer(Modifier.height(10.dp))
                if (records.isEmpty()) {
                    Text("还没有记录，从左侧录入一次充电吧", style = TextStyle(fontSize = 14.sp), color = TextSecondary)
                }
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    records.forEach { r ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault()).format(Date(r.ts)),
                                    style = TextStyle(fontSize = 14.sp), color = TextPrimary,
                                )
                                Text(
                                    "${r.socFrom}% → ${r.socTo}%（+${r.socTo - r.socFrom}%）",
                                    style = TextStyle(fontSize = 13.sp), color = TextSecondary,
                                )
                            }
                            Text(
                                "¥${"%.1f".format(r.costYuan)}",
                                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                                color = OkGreen,
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "删",
                                style = TextStyle(fontSize = 13.sp), color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.clickable { records = store.remove(r.ts) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberRow(label: String, value: Int, min: Int, max: Int, onPick: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = TextStyle(fontSize = 14.sp), color = TextSecondary, modifier = Modifier.width(110.dp))
        Spacer(Modifier.weight(1f))
        listOf(-10, -1, 1, 10).forEach { d ->
            Text(
                if (d > 0) "+$d" else "$d",
                style = TextStyle(fontSize = 13.sp),
                color = if (d > 0) OkGreen else TextSecondary,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clickable { onPick((value + d).coerceIn(min, max)) },
            )
        }
        Text("$value", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold), color = TextPrimary, modifier = Modifier.width(56.dp))
    }
}
