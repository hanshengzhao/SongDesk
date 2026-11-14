package com.songdesk.launcher.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.songdesk.launcher.ui.theme.GlassFill
import com.songdesk.launcher.ui.theme.GlassStroke
import com.songdesk.launcher.ui.theme.TextPrimary
import com.songdesk.launcher.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** 玻璃卡片：半透明填充 + 渐变描边（顶部高光）+ 24dp 圆角；contentFill=true 时内容撑满 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    contentFill: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(GlassFill)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.05f)),
                ),
                shape = RoundedCornerShape(24.dp),
            ),
    ) {
        // 顶部高光线
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, Color.White.copy(alpha = 0.30f), Color.Transparent),
                    )
                ),
        )
        Column(
            if (contentFill) Modifier.fillMaxSize().padding(20.dp) else Modifier.padding(20.dp),
            content = content,
        )
    }
}

/** 药丸形玻璃容器（状态条/Dock） */
@Composable
fun GlassRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = GlassFill,
        border = BorderStroke(1.dp, GlassStroke),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

/** 圆角玻璃按钮：选中态用主题强调色（onClick 须为末参以支持尾随 lambda） */
@Composable
fun GlassChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f) else GlassFill,
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.55f) else GlassStroke,
        ),
    ) {
        Box(
            Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text,
                style = TextStyle(fontSize = 14.sp),
                color = if (selected) MaterialTheme.colorScheme.primary else TextPrimary,
            )
        }
    }
}

/** 卡片小标题（次级色） */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier,
        style = TextStyle(fontSize = 13.sp),
        color = TextSecondary,
    )
}

/** 时钟状态：每 20s 刷新一次 */
@Composable
fun rememberClock(pattern: String): String {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(20_000)
            now = LocalDateTime.now()
        }
    }
    return now.format(DateTimeFormatter.ofPattern(pattern))
}
