package com.example.zenith.ui.screens.focus

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.zenith.ui.theme.SoftIndigo
import com.example.zenith.ui.theme.ZenithTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CompletionOverlay(
    missionName: String,
    durationText: String,
    timestamp: String,
    onDismiss: () -> Unit
) {
    // --- SECTION 1: THE ANIMATION ENGINE ---
    // We use Animatable to control the exact timing like your Figma code
    val ringProgress = remember { Animatable(0f) }
    val barProgress = remember { Animatable(1f) }
    var showContent by remember { mutableStateOf(false) }
    var showDot by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        LaunchedEffect(Unit) {
            // A. Start the Ring filling up (1.6 seconds)
            launch {
                ringProgress.animateTo(1f, animationSpec = tween(1600, easing = LinearOutSlowInEasing))
                showDot = true // Pop the dot in right when the ring finishes
            }

            // B. Fade in the text with a tiny 100ms delay
            delay(100)
            showContent = true

            // C. Start the 4-second auto-dismiss countdown
            barProgress.animateTo(0f, animationSpec = tween(4000, easing = LinearEasing))

            onDismiss() // Automatically close when bar reaches 0
        }

        // --- SECTION 2: THE BACKGROUND FOUNDATION ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Solid dark background (0xFF0E0E0E) blocks out the previous screen entirely
                .background(Color(0xFF0E0E0E))
                .clickable { onDismiss() }, // Tap anywhere to skip
            contentAlignment = Alignment.Center
        ) {
            // Subtle Indigo Glow in the background
            Box(
                modifier = Modifier
                    .size(400.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(SoftIndigo.copy(alpha = 0.08f), Color.Transparent)
                        )
                    )
            )

            // --- SECTION 3: THE INFORMATION STACK ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // A. The Multi-Layer Animated Ring
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(144.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // 1. Ghost Track (Very faint)
                        drawCircle(Color.White.copy(0.02f), radius = 68.dp.toPx(), style = Stroke(1.dp.toPx()))
                        // 2. Main Track
                        drawCircle(Color.White.copy(0.05f), radius = 58.dp.toPx(), style = Stroke(1.5.dp.toPx()))
                        // 3. Sharp Indigo Progress Arc
                        drawArc(
                            color = SoftIndigo,
                            startAngle = -90f,
                            sweepAngle = 360f * ringProgress.value,
                            useCenter = false,
                            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round),
                            size = androidx.compose.ui.geometry.Size(116.dp.toPx(), 116.dp.toPx()),
                            topLeft = androidx.compose.ui.geometry.Offset(14.dp.toPx(), 14.dp.toPx())
                        )
                    }
                    // Pop-in Center Dot
                    if (showDot) {
                        Box(modifier = Modifier
                            .size(7.dp)
                            .background(SoftIndigo.copy(0.75f), CircleShape))
                    }
                }

                Spacer(Modifier.height(30.dp))

                // B. The Data Labels (Fades in)
                AnimatedVisibility(visible = showContent, enter = fadeIn(tween(600)) + slideInVertically { 20 }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "SESSION COMPLETE",
                            color = SoftIndigo.copy(0.75f),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 4.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(Modifier.height(22.dp))
                        Text("FOCUSED FOR", color = Color.White.copy(0.3f), fontSize = 9.sp, letterSpacing = 2.sp, fontFamily = FontFamily.Monospace)

                        Spacer(Modifier.height(8.dp))
                        Text(
                            durationText,
                            color = Color.White.copy(0.9f),
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 58.sp, fontWeight = FontWeight.Light, fontFamily = FontFamily.Monospace)
                        )

                        if (missionName.isNotBlank()) {
                            Spacer(Modifier.height(20.dp))
                            Text("\"$missionName\"", color = Color.White.copy(0.3f), fontStyle = FontStyle.Italic, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 40.dp))
                        }

                        Spacer(Modifier.height(12.dp))
                        Text(timestamp, color = Color.White.copy(0.2f), fontSize = 9.sp, letterSpacing = 2.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            // --- SECTION 4: THE INTERACTIVE FOOTER ---
            Column(
                modifier = Modifier.align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // "Tap to Continue" hint (appears after 2 seconds)
                AnimatedVisibility(visible = barProgress.value < 0.5f, enter = fadeIn()) {
                    Text("TAP TO CONTINUE", color = Color.White.copy(0.25f), fontSize = 9.sp, letterSpacing = 2.sp, fontFamily = FontFamily.Monospace)
                }

                Spacer(Modifier.height(100.dp))

                // Horizontal auto-dismiss bar at the absolute bottom
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(Color.White.copy(0.04f))) {
                    Box(modifier = Modifier
                        .fillMaxWidth(barProgress.value)
                        .fillMaxHeight()
                        .background(SoftIndigo.copy(0.45f)))
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun CompletionOverlayPreview() {
    ZenithTheme {
        CompletionOverlay(
            missionName = "Deep Work Session",
            durationText = "00:45",
            timestamp = "TODAY • 14:30",
            onDismiss = {}
        )
    }
}

