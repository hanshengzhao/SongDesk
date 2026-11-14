package com.songdesk.launcher.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.songdesk.launcher.car.GuideState
import com.songdesk.launcher.ui.common.GlassCard
import com.songdesk.launcher.ui.theme.OkGreen
import com.songdesk.launcher.ui.theme.TextPrimary
import com.songdesk.launcher.ui.theme.TextSecondary
import com.songdesk.launcher.ui.theme.WarnAmber

private fun lightColor(countDown: Int?): Color = when {
    countDown == null -> TextSecondary
    countDown <= 5 -> Color(0xFFEF4444)   // 红：快到了
    countDown <= 10 -> WarnAmber          // 黄：注意
    else -> OkGreen                       // 绿：从容
}

/** 导航卡：红绿灯读秒大徽章 + 道路/距离（仅导航中显示） */
@Composable
fun NavCard(guide: GuideState, modifier: Modifier = Modifier) {
    GlassCard(modifier, contentFill = true) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("当前道路", style = TextStyle(fontSize = 12.sp), color = TextSecondary)
                Text(
                    guide.roadName.ifBlank { "导航中" },
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val remain = guide.remainDisM
                if (remain != null) {
                    Text(
                        "前方 ${if (remain >= 1000) "%.1fkm".format(remain / 1000f) else "${remain}m"}" +
                            if (guide.nextRoadName.isNotBlank()) " → ${guide.nextRoadName}" else "",
                        style = TextStyle(fontSize = 13.sp),
                        color = TextSecondary,
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            guide.lightCountDown?.let { cd ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(64.dp)
                            .background(color = lightColor(cd).copy(alpha = 0.18f), shape = CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "$cd",
                            style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                            color = lightColor(cd),
                        )
                    }
                    Text("红绿灯", style = TextStyle(fontSize = 11.sp), color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}
