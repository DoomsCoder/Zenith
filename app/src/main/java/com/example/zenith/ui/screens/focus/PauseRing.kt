package com.example.zenith.ui.screens.focus

import android.graphics.BlurMaskFilter
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.zenith.ui.theme.SoftIndigo

@Composable
fun PauseRing(
    secondsRemaining: Int,
    totalSeconds: Int,
    buttonWidth: Dp,
    buttonHeight: Dp = 64.dp,
    borderRadius: Dp = 16.dp
) {
    val isUrgent = secondsRemaining <= 90

    // 1. COLORS & PROGRESS
    val ringColor by animateColorAsState(if (isUrgent) Color(0xFFFFC396).copy(0.8f) else SoftIndigo.copy(0.85f), tween(600))
    val glowColor = if (isUrgent) Color(0xFFFFC396).copy(0.2f) else SoftIndigo.copy(0.25f)

    val progressTarget = if (totalSeconds > 0) secondsRemaining.toFloat() / totalSeconds.toFloat() else 0f
    val animatedProgress by animateFloatAsState(progressTarget, tween(1000), label = "PauseProgress")

// 2. CANVAS SIZE (Safety Margin of 12dp on all sides)
    val canvasW = buttonWidth + 24.dp
    val canvasH = buttonHeight + 24.dp

    Box(modifier = Modifier.size(canvasW, canvasH)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // THE MATH: 3.5dp outset from the button's edge
            val ringW = buttonWidth.toPx() + 7.dp.toPx()
            val ringH = buttonHeight.toPx() + 7.dp.toPx()
            val rPx = (borderRadius + 3.5.dp).toPx()

            // Centering the ring inside the 24dp larger canvas
            val left = (size.width - ringW) / 2f
            val top = (size.height - ringH) / 2f
            val right = left + ringW
            val bottom = top + ringH

            // THE PATH: Starts exactly at Top-Center
            val manualPath = Path().apply {
                moveTo(size.width / 2f, top)
                lineTo(right - rPx, top)
                arcTo(Rect(right - 2 * rPx, top, right, top + 2 * rPx), -90f, 90f, false)
                lineTo(right, bottom - rPx)
                arcTo(Rect(right - 2 * rPx, bottom - 2 * rPx, right, bottom), 0f, 90f, false)
                lineTo(left + rPx, bottom)
                arcTo(Rect(left, bottom - 2 * rPx, left + 2 * rPx, bottom), 90f, 90f, false)
                lineTo(left, top + rPx)
                arcTo(Rect(left, top, left + 2 * rPx, top + 2 * rPx), 180f, 90f, false)
                lineTo(size.width / 2f, top)
                close()
            }

            val pathMeasure = PathMeasure()
            pathMeasure.setPath(manualPath, false)
            val animatedPath = Path()
            pathMeasure.getSegment(0f, pathMeasure.length * animatedProgress, animatedPath)

            // LAYER 1: Ghost Track
            drawPath(manualPath, ringColor.copy(0.08f), style = Stroke(1.5.dp.toPx()))

            // LAYER 2: Real Blur Glow (Using Native Canvas)
            drawIntoCanvas { canvas ->
                val paint = Paint().asFrameworkPaint().apply {
                    color = glowColor.toArgb()
                    style = android.graphics.Paint.Style.STROKE
                    setStrokeWidth(6.dp.toPx())
                    strokeCap = android.graphics.Paint.Cap.ROUND
                    // Proper Blur
                    maskFilter = BlurMaskFilter(4.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                }
                canvas.nativeCanvas.drawPath(animatedPath.asAndroidPath(), paint)
            }

            // LAYER 3: Sharp Arc
            drawPath(animatedPath, ringColor, style = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}