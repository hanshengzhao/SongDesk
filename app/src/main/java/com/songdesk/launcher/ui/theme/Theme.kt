package com.songdesk.launcher.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

private fun songDeskScheme(accent: Color) = darkColorScheme(
    primary = accent,
    onPrimary = DeepBg,
    background = DeepBg,
    onBackground = TextPrimary,
    surface = GlassFill,
    onSurface = TextPrimary,
    surfaceVariant = GlassFillStrong,
    onSurfaceVariant = TextSecondary,
    outline = GlassStroke,
    secondary = accent.copy(alpha = 0.25f),
    onSecondary = TextPrimary,
)

@Composable
fun SongDeskTheme(accent: Color, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = songDeskScheme(accent),
        content = content,
    )
}

/** 壁纸背景：程序生成图 + 上下压暗层保证 UI 可读 */
@Composable
fun AppBackdrop(wallpaper: Wallpaper, content: @Composable () -> Unit) {
    val ctx = LocalContext.current
    val resId = remember(wallpaper) {
        ctx.resources.getIdentifier(wallpaper.resName, "drawable", ctx.packageName)
    }
    Box(Modifier.fillMaxSize().background(DeepBg)) {
        if (resId != 0) {
            Image(
                painter = painterResource(resId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        // 可读性压暗：顶部轻、中部最透、底部加重（Dock 区域）
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.30f),
                        0.35f to Color.Black.copy(alpha = 0.16f),
                        0.75f to Color.Black.copy(alpha = 0.38f),
                        1f to Color.Black.copy(alpha = 0.55f),
                    )
                )
        )
        content()
    }
}
