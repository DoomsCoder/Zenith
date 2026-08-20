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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun AbandonToast(
    elapsedText: String,
    onDismiss: () -> Unit,
    onUndo: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    val progressAnim = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        isVisible = true
        progressAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(3500, easing = LinearEasing)
        )
        isVisible = false
        delay(300)
        onDismiss()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = isVisible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 110.dp)
                .padding(horizontal = 20.dp),
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it / 2 },
                animationSpec = tween(300)
            ) + fadeOut(),
        ) {
            Surface(
                color = Color(0xF7141212),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFFFB99B).copy(0.13f)),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .padding(start = 16.dp, top = 13.dp, end = 16.dp, bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(13.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFFFB99B).copy(0.07f), CircleShape)
                                .border(1.dp, Color(0xFFFFB99B).copy(0.18f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✕", color = Color(0xFFFFB99B).copy(0.6f), fontSize = 12.sp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Session ended early",
                                color = Color(0xFFFFC8AF).copy(0.72f),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = "$elapsedText elapsed",
                                color = Color.White.copy(0.22f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        Text(
                            text = "Undo",
                            color = Color(0xFFFFB998),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .clickable{
                                    onUndo()
                                }
                                .padding(8.dp)
                        )

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
