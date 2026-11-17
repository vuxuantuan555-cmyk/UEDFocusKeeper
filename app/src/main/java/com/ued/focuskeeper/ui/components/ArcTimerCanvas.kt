package com.ued.focuskeeper.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ued.focuskeeper.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom View vẽ bằng Canvas (Jetpack Compose drawArc).
 * Hiển thị đồng hồ đếm ngược dạng vòng tròn.
 */
@Composable
fun ArcTimerCanvas(
    remainingSeconds: Long,
    totalSeconds: Long,
    isRunning: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 260.dp
) {
    val textMeasurer = rememberTextMeasurer()

    // Pulse animation khi đang chạy
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f
    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val timeText = String.format("%02d:%02d", mins, secs)

    val arcColor = when {
        progress < 0.25f -> RedAccent
        progress < 0.5f -> YellowAccent
        else -> BluePrimary
    }

    val subLabel = when {
        isRunning -> "đang chạy"
        remainingSeconds == totalSeconds -> "sẵn sàng"
        else -> "tạm dừng"
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val radius = this.size.width * 0.38f
            val strokeWidth = 14.dp.toPx()

            // ── Background ring ───────────────────────────────────────────
            drawArc(
                color = BorderColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(cx - radius, cy - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // ── Progress arc (drawArc) ────────────────────────────────────
            val sweepAngle = 360f * progress
            val glowAlpha = if (isRunning) pulseAlpha else 0.6f

            // Glow layer
            drawArc(
                color = arcColor.copy(alpha = glowAlpha * 0.35f),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(cx - radius, cy - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth + 10.dp.toPx(), cap = StrokeCap.Round)
            )
            // Main arc
            drawArc(
                color = arcColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(cx - radius, cy - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // ── Dot at arc tip ────────────────────────────────────────────
            if (sweepAngle > 0f) {
                val tipAngle = Math.toRadians((-90.0 + sweepAngle)).toFloat()
                val tx = cx + radius * cos(tipAngle)
                val ty = cy + radius * sin(tipAngle)
                // Glow dot
                drawCircle(
                    color = arcColor.copy(alpha = 0.4f),
                    radius = 14.dp.toPx(),
                    center = Offset(tx, ty)
                )
                // Core dot
                drawCircle(
                    color = arcColor,
                    radius = 8.dp.toPx(),
                    center = Offset(tx, ty)
                )
            }

            // ── Center time text ──────────────────────────────────────────
            val timeStyle = TextStyle(
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
            )
            val measured = textMeasurer.measure(timeText, timeStyle)
            drawText(
                textMeasurer = textMeasurer,
                text = timeText,
                style = timeStyle,
                topLeft = Offset(
                    cx - measured.size.width / 2f,
                    cy - measured.size.height / 2f - 12.dp.toPx()
                )
            )

            // ── Sub label ─────────────────────────────────────────────────
            val subStyle = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted
            )
            val subMeasured = textMeasurer.measure(subLabel, subStyle)
            drawText(
                textMeasurer = textMeasurer,
                text = subLabel,
                style = subStyle,
                topLeft = Offset(
                    cx - subMeasured.size.width / 2f,
                    cy + 26.dp.toPx()
                )
            )
        }
    }
}
