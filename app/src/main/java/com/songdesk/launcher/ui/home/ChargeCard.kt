package com.songdesk.launcher.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.songdesk.launcher.data.ChargeRecord
import com.songdesk.launcher.ui.common.GlassCard
import com.songdesk.launcher.ui.common.GlassChip
import com.songdesk.launcher.ui.common.SectionLabel
import com.songdesk.launcher.ui.theme.OkGreen
import com.songdesk.launcher.ui.theme.TextPrimary
import com.songdesk.launcher.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 充电/能耗卡：主页展示最近充电与累计统计，入口进充电记录页 */
@Composable
fun ChargeCard(
    records: List<ChargeRecord>,
    modifier: Modifier = Modifier,
    onOpenCharge: () -> Unit,
) {
    GlassCard(modifier, contentFill = true) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel("充电 / 能耗")
            Spacer(Modifier.weight(1f))
            GlassChip("充电记录 ›", selected = false) { onOpenCharge() }
        }
        Spacer(Modifier.height(14.dp))
        if (records.isEmpty()) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                Text(
                    "还没有充电记录 · 从「充电记录」页录入一次充电，这里会汇总次数/花费/电量",
                    style = TextStyle(fontSize = 13.sp),
                    color = TextSecondary,
                )
            }
        } else {
            val last = records.first()
            val totalCost = records.sumOf { it.costYuan }
            val totalKwh = records.sumOf { (it.socTo - it.socFrom).toDouble() * 0.6 } // 估算 60kWh 电池
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault()).format(Date(last.ts)),
                        style = TextStyle(fontSize = 12.sp), color = TextSecondary,
                    )
                    Text(
                        "${last.socFrom}% → ${last.socTo}%",
                        style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
                        color = TextPrimary,
                    )
                }
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "¥${"%.1f".format(last.costYuan)}",
                        style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
                        color = OkGreen,
                    )
                    Text(
                        "累计 ${records.size} 次 · ${"%.0f".format(totalCost)} 元",
                        style = TextStyle(fontSize = 12.sp), color = TextSecondary,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "累计充入约 ${"%.1f".format(totalKwh)} kWh · 均价 ${if (totalKwh > 0) "%.2f".format(totalCost / totalKwh) else "--"} 元/kWh",
                style = TextStyle(fontSize = 12.sp), color = TextSecondary,
            )
        }
    }
}
