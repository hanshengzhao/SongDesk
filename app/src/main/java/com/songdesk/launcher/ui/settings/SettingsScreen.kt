package com.songdesk.launcher.ui.settings

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.songdesk.launcher.ui.common.GlassCard
import com.songdesk.launcher.ui.common.GlassChip
import com.songdesk.launcher.ui.common.SectionLabel
import com.songdesk.launcher.ui.theme.AccentIce
import com.songdesk.launcher.ui.theme.AccentOrange
import com.songdesk.launcher.ui.theme.AccentTeal
import com.songdesk.launcher.ui.theme.TextPrimary
import com.songdesk.launcher.ui.theme.TextSecondary
import com.songdesk.launcher.ui.theme.Wallpaper

/** 设置页：壁纸四选一 + 强调色三选一 + 嗅探/演示开关 + 关于 */
@Composable
fun SettingsScreen(
    accentIndex: Int,
    wallpaperIndex: Int,
    sniffing: Boolean,
    navDemo: Boolean,
    voiceOn: Boolean,
    onVoiceChange: (Boolean) -> Unit,
    aiKey: String,
    onAiKeyChange: (String) -> Unit,
    onOpenAgent: () -> Unit,
    onSniffingChange: (Boolean) -> Unit,
    onNavDemoChange: (Boolean) -> Unit,
    onAccentChange: (Int) -> Unit,
    onWallpaperChange: (Int) -> Unit,
    onBack: () -> Unit,
    onExit: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(onClick = onBack, shape = CircleShape, color = MaterialTheme.colorScheme.surface) {
                Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Spacer(Modifier.size(16.dp))
            Text(
                "设置",
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Medium),
                color = TextPrimary,
            )
        }
        Spacer(Modifier.height(20.dp))

        GlassCard(Modifier.fillMaxSize(), contentFill = true) {
            SectionLabel("壁纸")
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Wallpaper.entries.forEachIndexed { i: Int, wp: Wallpaper ->
                    WallpaperThumb(
                        wp = wp,
                        selected = wallpaperIndex == i,
                        onClick = { onWallpaperChange(i) },
                    )
                }
            }

            Spacer(Modifier.height(22.dp))
            SectionLabel("强调色")
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                AccentSwatch(AccentIce, 0, accentIndex, onAccentChange)
                AccentSwatch(AccentTeal, 1, accentIndex, onAccentChange)
                AccentSwatch(AccentOrange, 2, accentIndex, onAccentChange)
            }

            Spacer(Modifier.height(24.dp))
            SectionLabel("Dock 自定义")
            Spacer(Modifier.height(8.dp))
            Text(
                "长按 Dock 图标排序与替换 · 即将推出",
                style = TextStyle(fontSize = 14.sp),
                color = TextSecondary,
            )

            Spacer(Modifier.height(22.dp))
            SectionLabel("AI 助理（DeepSeek / 智谱GLM）")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = aiKey,
                onValueChange = onAiKeyChange,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                placeholder = { Text("填入 API Key（DeepSeek / 智谱）", color = TextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassChip("打开 AI 助理", selected = false) { onOpenAgent() }
            }

            Spacer(Modifier.height(22.dp))
            SectionLabel("导航演示")
            Spacer(Modifier.height(8.dp))
            Text(
                "模拟高德车机版导航广播（含红绿灯读秒），用于验证主页导航卡",
                style = TextStyle(fontSize = 14.sp),
                color = TextSecondary,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassChip("开启演示", selected = navDemo) { onNavDemoChange(true) }
                GlassChip("关闭", selected = !navDemo) { onNavDemoChange(false) }
            }

            Spacer(Modifier.height(22.dp))
            SectionLabel("语音播报")
            Spacer(Modifier.height(8.dp))
            Text(
                "任务与场景执行后语音提示（真车走原车语音，其他设备走本地合成）",
                style = TextStyle(fontSize = 14.sp),
                color = TextSecondary,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassChip("开启", selected = voiceOn) { onVoiceChange(true) }
                GlassChip("关闭", selected = !voiceOn) { onVoiceChange(false) }
            }

            Spacer(Modifier.height(22.dp))
            SectionLabel("车机协议嗅探")
            Spacer(Modifier.height(8.dp))
            Text(
                "上车后开启：抓到的原车广播字段会自动上传云端（主题 songdesk-sniff-v1），开发者远程拉取即可校准车况协议，无需连接电脑",
                style = TextStyle(fontSize = 14.sp),
                color = TextSecondary,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassChip("开启抓包+上传", selected = sniffing) { onSniffingChange(true) }
                GlassChip("关闭", selected = !sniffing) { onSniffingChange(false) }
                GlassChip("发送测试", selected = false) {
                    com.songdesk.launcher.car.telemetry.SniffUploader.sendTest(context)
                }
            }

            Spacer(Modifier.height(24.dp))
            SectionLabel("关于")
            Spacer(Modifier.height(8.dp))
            Text("宋桌面 SongDesk 1.0", style = TextStyle(fontSize = 16.sp), color = TextPrimary)
            Text(
                "为比亚迪宋PLUS DM-i（DiLink 3.0）打造 · 深空玻璃风",
                style = TextStyle(fontSize = 13.sp),
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                "车机接口研究致敬迪粉桌面（miktone）",
                style = TextStyle(fontSize = 13.sp),
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp),
            )

            Spacer(Modifier.height(24.dp))
            Row {
                GlassChip("退出桌面", selected = false) { onExit() }
            }
        }
    }
}

@Composable
private fun AccentSwatch(
    color: Color,
    index: Int,
    current: Int,
    onPick: (Int) -> Unit,
) {
    Box(
        Modifier
            .size(56.dp)
            .background(color = color, shape = CircleShape)
            .clickable { onPick(index) },
        contentAlignment = Alignment.Center,
    ) {
        if (current == index) {
            Box(
                Modifier
                    .size(20.dp)
                    .background(color = TextPrimary, shape = CircleShape)
            )
        }
    }
}

/** 壁纸缩略图：点击切换全局壁纸 */
@Composable
private fun WallpaperThumb(
    wp: com.songdesk.launcher.ui.theme.Wallpaper,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val resId = remember(wp) {
        ctx.resources.getIdentifier(wp.resName, "drawable", ctx.packageName)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(width = 168.dp, height = 94.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onClick)
        ) {
            if (resId != 0) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(resId),
                    contentDescription = wp.label,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            if (selected) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            color = Color(0x332E90FA),
                            shape = RoundedCornerShape(14.dp),
                        )
                )
                Box(
                    Modifier
                        .size(20.dp)
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(color = TextPrimary, shape = CircleShape)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(wp.label, style = TextStyle(fontSize = 12.sp), color = TextSecondary)
    }
}
