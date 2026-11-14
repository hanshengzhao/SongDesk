package com.songdesk.launcher.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 圆环仪表：背景弧 + 进度弧（渐变）+ 中央内容。
 * progress: 0..1
 */
@Composable
fun RingGauge(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
    strokeWidth: Dp = 7.dp,
    trackColor: Color = Color.White.copy(alpha = 0.10f),
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit,
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            // 轨道
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
            // 进度
            val sweep = 360f * progress.coerceIn(0f, 1f)
            if (sweep > 2f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0f to color.copy(alpha = 0.55f),
                        0.7f to color,
                        1f to color,
                    ),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
        }
        content()
    }
}

/** 黑胶唱片：深紫径向渐变 + 纹理弧线 + 中心孔 */
@Composable
fun VinylDisc(modifier: Modifier = Modifier, accent: Color) {
    Canvas(modifier = modifier) {
        val r = size.minDimension / 2
        val c = Offset(size.width / 2, size.height / 2)
        // 盘面径向渐变
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF2A2F4A),
                    Color(0xFF1A1D33),
                    Color(0xFF101222),
                    Color(0xFF232848),
                ),
                center = c,
                radius = r,
            ),
            radius = r,
            center = c,
        )
        // 纹理弧线
        for (i in 1..5) {
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = r * (0.35f + i * 0.11f),
                center = c,
                style = Stroke(1.2f),
            )
        }
        // 高光弧（左上）
        drawArc(
            brush = Brush.sweepGradient(
                0f to Color.Transparent,
                0.12f to accent.copy(alpha = 0.45f),
                0.2f to Color.Transparent,
            ),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(strokeDp(), strokeDp()),
            size = Size(size.width - strokeDp() * 2, size.height - strokeDp() * 2),
            style = Stroke(strokeDp() * 0.9f, cap = StrokeCap.Round),
        )
        // 中心孔
        drawCircle(Color(0xFF07090F), radius = r * 0.14f, center = c)
        drawCircle(Color.White.copy(alpha = 0.25f), radius = r * 0.14f, center = c, style = Stroke(1.5f))
    }
}

private fun strokeDp(): Float = 6f
