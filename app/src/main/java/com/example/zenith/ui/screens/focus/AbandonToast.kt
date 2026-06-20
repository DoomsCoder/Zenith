package com.example.zenith.ui.screens.focus

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun AbandonToast(
    elapsedText: String,
    onDismiss: () -> Unit
) {
    // 1. ANIMATION STATE
    var isVisible by remember { mutableStateOf(false) }
    val progressAnim = remember { Animatable(1f) }

    // Logic: Slide in, wait, animate bar, then dismiss
    LaunchedEffect(Unit) {
        isVisible = true
        // Animate the bottom progress bar over 3.5 seconds (Linear)
        progressAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(3500, easing = LinearEasing)
        )
        // Automatically hide and dismiss
        isVisible = false
        delay(300) // Wait for exit animation
        onDismiss()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = isVisible,
            // 2. SPRING TRANSITION (Matches Figma damping: 26, stiffness: 310)
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it / 2 },
                animationSpec = tween(300)
            ) + fadeOut(),
            modifier = Modifier
                .padding(bottom = 92.dp)
                .padding(horizontal = 20.dp)
        ) {
            Surface(
                color = Color(0xF7141212), // rgba(20,18,18,0.97)
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFFFB99B).copy(0.13f)),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                // We wrap content in a Box to allow absolute positioning of the progress bar
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .padding(start = 16.dp, top = 13.dp, end = 16.dp, bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(13.dp)
                    ) {
                        // A. ICON CIRCLE
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFFFB99B).copy(0.07f), CircleShape)
                                .border(1.dp, Color(0xFFFFB99B).copy(0.18f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✕", color = Color(0xFFFFB99B).copy(0.6f), fontSize = 12.sp)
                        }

                        // B. TEXT BLOCK
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Session ended early",
                                color = Color(0xFFFFC8AF).copy(0.72f),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "$elapsedText elapsed",
                                color = Color.White.copy(0.22f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // C. DISMISS BUTTON
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color.White.copy(0.04f), CircleShape)
                                .border(1.dp, Color.White.copy(0.07f), CircleShape)
                                .clickable { isVisible = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✕", color = Color.White.copy(0.2f), fontSize = 8.sp)
                        }
                    }

                    // D. AUTO-DISMISS BAR (Figma bottom bar)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth(progressAnim.value)
                            .height(1.5.dp)
                            .background(Color(0xFFFFB99B).copy(0.3f))
                    )
                }
            }
        }
    }
}