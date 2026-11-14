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
import androidx.compose.ui.geometry.Offset
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

/** HMI 背景：近纯黑 + 顶部中央微弱蓝色光晕（对齐新设计稿） */
@Composable
fun AppBackdrop(wallpaper: Wallpaper?, content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF07090F))
    ) {
        // 顶部中央蓝色光晕
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF1B3A5C).copy(alpha = 0.55f), Color.Transparent),
                        center = Offset(960f, -150f),
                        radius = 1100f,
                    )
                )
        )
        // 底部微光
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF12325A).copy(alpha = 0.25f), Color.Transparent),
                        center = Offset(960f, 1250f),
                        radius = 900f,
                    )
                )
        )
        content()
    }
}
