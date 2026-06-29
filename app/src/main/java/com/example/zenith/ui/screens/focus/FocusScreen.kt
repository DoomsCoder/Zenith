package com.example.zenith.ui.screens.focus

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zenith.ui.theme.MutedGray
import com.example.zenith.ui.theme.OffWhite
import com.example.zenith.ui.theme.SoftIndigo
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(viewModel: FocusViewModel = viewModel()) {
    // --- 1. STATE MANAGEMENT ---
    var showCustomSheet by rememberSaveable { mutableStateOf(false) }
    var customPickerValue by rememberSaveable { mutableIntStateOf(45) }

    val state by viewModel.uiState.collectAsState()

    // Hold to abandon state
    var pressingProgress by rememberSaveable { mutableFloatStateOf(0f) }
    var isHolding by rememberSaveable { mutableStateOf(false) }
    val abandonColor = Color(0xFFFFC8AF).copy(0.38f)

    val completionTimestamp = remember(state.sessionState) {
        if (state.sessionState == SessionState.FINISHED) {
            val now = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            "TODAY • ${now.format(formatter)}"
        } else ""
    }

    val focusManager = LocalFocusManager.current

    // --- 2. SMART FORMATTER LOGIC (Hours/Minutes/Seconds) ---
    val displayTime =
        remember(state.sessionState, state.remainingFocusSeconds, state.selectedDurationMinutes) {
            val totalSeconds = if (state.sessionState == SessionState.IDLE) {
                state.selectedDurationMinutes * 60L
            } else {
                state.remainingFocusSeconds.toLong()
            }
            val h = totalSeconds / 3600
            val m = (totalSeconds % 3600) / 60
            val s = totalSeconds % 60

            if (h > 0) {
                // Format: 1:00:00 (Hours included)
                "${h}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
            } else {
                // Format: 25:00 (Standard)
                "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
            }
        }


    val deepSlate = Color(0xFF121212)

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(deepSlate)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f))
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                    Text(
                        text = "CURRENT MISSION",
                        color = MutedGray.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    BasicTextField(
                        value = state.missionText,
                        onValueChange = { viewModel.updateMission(it) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(SoftIndigo),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (state.missionText.isEmpty()) {
                                Text(
                                    text = "What are you focusing on right now?",
                                    color = OffWhite.copy(0.2f),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // --- ZONE 2: STATUS & CANVAS TIMER ---

            val statusText = when (state.sessionState) {
                SessionState.IDLE -> "SYSTEM STATUS: READY"
                SessionState.RUNNING -> "DEEP FOCUS ACTIVE"
                SessionState.PAUSED -> "SESSION PAUSED"
                SessionState.FINISHED -> "SESSION COMPLETE"
                SessionState.ABANDONED -> "SESSION ABANDONED"
            }

            val statusColor = when (state.sessionState) {
                SessionState.RUNNING -> SoftIndigo
                SessionState.ABANDONED, SessionState.FINISHED -> abandonColor.copy(1f)
                else -> MutedGray.copy(0.6f)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(statusColor, RoundedCornerShape(50))
                )
                Spacer(Modifier.width(8.dp))

                AnimatedContent(
                    targetState = statusText,
                    transitionSpec = {
                        // New text slide-up speed configuration
                        slideInVertically(animationSpec = tween(600)) { height -> height } +
                                fadeIn(animationSpec = tween(600)) togetherWith

                                // Old text slide-up speed configuration
                                slideOutVertically(animationSpec = tween(600)) { height -> -height } +
                                fadeOut(animationSpec = tween(600))
                    },
                    label = "StatusAnimation"
                ) { targetText ->
                    Text(
                        text = targetText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                    )
                }

            }

            Spacer(Modifier.height(40.dp))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
                val progress = state.progress
                // THE WATCH-FACE DIAL
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.minDimension / 2
                    // --- 1. DRAW THE TICKS (Sitting inside the Halo) ---
                    // We reduce the outer radius for ticks to create a gap for the arc
                    val tickOuterRadius = radius - 14.dp.toPx()

                    for (i in 0 until 60) {
                        val angleInDegrees = (i * 6) - 90
                        val angleInRadians = Math.toRadians(angleInDegrees.toDouble())

                        val tickProgress = i / 60f
                        val isTickActive = tickProgress <= progress

                        val tickLength = if (i % 5 == 0) 10.dp.toPx() else 5.dp.toPx()
                        val strokeWidth = if (i % 5 == 0) 2.dp.toPx() else 1.dp.toPx()

                        // Dim the ticks significantly to let the outer arc be the "Hero"
                        val isTickNotActive =
                            if (i % 5 == 0) MutedGray.copy(0.3f) else MutedGray.copy(0.1f)
                        val color =
                            if (isTickActive && progress > 0f) SoftIndigo.copy(0.6f) else isTickNotActive

                        // Calculate Start and End points based on the new inset radius
                        val startX =
                            center.x + (tickOuterRadius - tickLength) * cos(angleInRadians).toFloat()
                        val startY =
                            center.y + (tickOuterRadius - tickLength) * sin(angleInRadians).toFloat()
                        val endX = center.x + tickOuterRadius * cos(angleInRadians).toFloat()
                        val endY = center.y + tickOuterRadius * sin(angleInRadians).toFloat()

                        drawLine(
                            color = color,
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = strokeWidth
                        )
                    }

                    // --- 2. DRAW THE THICK OUTER HALO (Progress Arc) ---
                    if (progress > 0f) {
                        val arcStrokeWidth = 4.dp.toPx() // Thicker for better visibility
                        drawArc(
                            color = SoftIndigo,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            style = Stroke(width = arcStrokeWidth, cap = StrokeCap.Round),
                            // We inset the size by half the stroke width to keep it perfectly within canvas bounds
                            size = size.copy(
                                width = size.width - arcStrokeWidth,
                                height = size.height - arcStrokeWidth
                            ),
                            topLeft = Offset(arcStrokeWidth / 2, arcStrokeWidth / 2)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = displayTime,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = if (state.selectedDurationMinutes >= 60) 40.sp else 48.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Text(
                        text = "REMAINING",
                        color = MutedGray.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 4.sp,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // --- ZONE 3: PRESET CAPSULES ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.width(280.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("25m", "50m", "Custom").forEach { label ->
                        val isInteractionAllowed = state.sessionState == SessionState.IDLE

                        val chipText = if (label == "Custom") {
                            if (state.selectedDurationMinutes !in listOf(25, 50)) {
                                "${state.selectedDurationMinutes}m"
                            } else {
                                "Custom"
                            }
                        } else {
                            label
                        }
                        val isSelected = (label == "25m" && state.selectedDurationMinutes == 25) ||
                                (label == "50m" && state.selectedDurationMinutes == 50) ||
                                (label == "Custom" && state.selectedDurationMinutes !in listOf(
                                    25,
                                    50
                                ))

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .alpha(if (isInteractionAllowed) 1f else 0.3f)
                                .clickable(enabled = isInteractionAllowed) {
                                    if (label == "Custom") showCustomSheet = true
                                    else viewModel.setDuration(if (label == "25m") 25 else 50)
                                },
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) SoftIndigo.copy(0.6f) else Color.White.copy(
                                    0.05f
                                )
                            ),
                            color = if (isSelected) SoftIndigo.copy(0.12f) else Color.Transparent
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = chipText,
                                    color = if (isSelected) SoftIndigo else MutedGray.copy(0.6f),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                        }
                    }
                }

            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- ZONE 4: THE INTERACTIVE ENGINE BUTTON ---

            val isIntentClear = isHolding && pressingProgress > 0.03f

            val buttonText = when (state.sessionState) {
                SessionState.IDLE -> "INITIATE FOCUS SESSION"
                SessionState.RUNNING -> if (pressingProgress > 0.15f) "HOLD TO ABANDON..." else "PAUSE SESSION"
                SessionState.PAUSED -> if (pressingProgress > 0.15f) "HOLD TO ABANDON..." else "RESUME SESSION"
                SessionState.FINISHED, SessionState.ABANDONED -> "INITIATE FOCUS SESSION"
            }

            val syncedButtonColor by animateColorAsState(
                targetValue = if (isIntentClear) Color(0xFF2A2A2A) else SoftIndigo,
                animationSpec = tween(150),
                label = "ButtonColor"
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PauseLabelPill(
                    secondsRemaining = state.remainingPausedSeconds,
                    isVisible = state.sessionState == SessionState.PAUSED
                )
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val currentWidth = maxWidth

                    if (state.sessionState == SessionState.PAUSED) {
                        PauseRing(
                            secondsRemaining = state.remainingPausedSeconds,
                            totalSeconds = 300,
                            buttonWidth = currentWidth
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (state.sessionState == SessionState.IDLE && state.missionText.isBlank()) MutedGray.copy(
                                    0.6f
                                ) else syncedButtonColor
                            )
                            .pointerInput(state.sessionState) {
                                detectTapGestures(
                                    onPress = {
                                        if (state.sessionState == SessionState.IDLE) {
                                            val released = tryAwaitRelease()
                                            if (released && state.missionText.isNotBlank()) viewModel.startSession()
                                        } else {
                                            val pressStartTime = System.currentTimeMillis()
                                            isHolding = true
                                            try {
                                                tryAwaitRelease() // ✋ Code stops here until you lift your finger
                                            } finally {
                                                isHolding = false
                                                val holdDuration = System.currentTimeMillis() - pressStartTime

                                                // ✅ GESTURE LOGIC: The Decision Engine
                                                if (holdDuration >= 3000) {
                                                    // 1. Success: Long press completed
                                                    viewModel.abandonSession()
                                                } else if (holdDuration < 300) {
                                                    // 2. Success: Normal quick tap
                                                    viewModel.toggleFocusSession()
                                                }
                                                // 3. Failed: User held for 1 or 2 seconds then changed their mind.
                                                // We do NOTHING. The button stays as it was.

                                                pressingProgress = 0f
                                            }
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Animating the progress bar while holding
                        LaunchedEffect(isHolding) {
                            if (isHolding) {
                                val start = System.currentTimeMillis()
                                while (isHolding) {
                                    pressingProgress = (System.currentTimeMillis() - start) / 3000f
                                    if (pressingProgress >= 1f) {
                                        viewModel.abandonSession()
                                        isHolding = false
                                        break
                                    }
                                    delay(16)
                                }
                            } else {
                                pressingProgress = 0f
                            }
                        }

                        // THE PROGRESS BAR (Bottom 2px bar)
                        if (isIntentClear) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth(pressingProgress)
                                    .height(2.dp)
                                    .background(Color.White)
                            )
                        }

                        AnimatedContent(
                            targetState = buttonText,
                            transitionSpec = {
                                // New text slide-up speed configuration
                                slideInVertically(animationSpec = tween(600)) { height -> height } +
                                        fadeIn(animationSpec = tween(600)) togetherWith

                                        // Old text slide-up speed configuration
                                        slideOutVertically(animationSpec = tween(600)) { height -> -height } +
                                        fadeOut(animationSpec = tween(600))
                            },
                            label = "ButtonTextTransition"
                        ) { targetText ->
                            Text(
                                text = targetText,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = if (isIntentClear) abandonColor else OffWhite
                            )
                        }
                    }
                }


                // --- ZONE 5: SLIDING ABANDON SECTION ---
                AnimatedVisibility(
                    visible = state.sessionState == SessionState.RUNNING || state.sessionState == SessionState.PAUSED,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 20.dp)
                    ) {
                        // Hairline Divider
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(1.dp)
                                .background(MutedGray.copy(0.2f))
                        )

                        Spacer(Modifier.height(20.dp))

                        Text(
                            text = "ABANDON SESSION",
                            color = abandonColor,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 3.sp
                            ),
                            modifier = Modifier
                                .clickable { viewModel.abandonSession() }
                                .padding(8.dp)
                        )

                        Spacer(Modifier.height(4.dp))

                        val hintText =
                            if (state.sessionState == SessionState.RUNNING) "or hold PAUSE for 3s" else "or hold RESUME for 3s"
                        Text(
                            text = hintText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            ),
                            color = OffWhite.copy(0.6f)
                        )
                    }
                }
            }

//            Spacer(Modifier.height(32.dp))

            Spacer(modifier = Modifier.height(200.dp))
        }

        // --- ZONE 6: OVERLAYS (Floating on top) ---
        // A. SUCCESS OVERLAY
        if (state.sessionState == SessionState.FINISHED) {
            CompletionOverlay(
                missionName = state.missionText,
                durationText = formatTime(state.selectedDurationMinutes * 60L),
                timestamp = completionTimestamp, // This formatting will move to VM later
                onDismiss = { viewModel.resetToDefaults() }
            )
        }

        // B. ABANDON TOAST
        if (state.sessionState == SessionState.ABANDONED) {
            val elapsedSeconds =
                (state.totalFocusSeconds - state.remainingFocusSeconds).toLong()
            AbandonToast(
                elapsedText = formatTime(elapsedSeconds), // We'll link this to VM elapsed time later
                onDismiss = { viewModel.resetToDefaults() },
                onUndo = { viewModel.undoAbandon() }
            )
        }
    }


    // --- CUSTOM PICKER BOTTOM SHEET ---
    if (showCustomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCustomSheet = false },
            containerColor = Color(0xFF1A1A1A),
            scrimColor = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 32.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "CUSTOM DURATION",
                        color = MutedGray,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                    )
                    IconButton(onClick = { showCustomSheet = false }) {
                        Icon(
                            Icons.Default.Close,
                            "Close",
                            tint = Color.White.copy(0.5f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(48.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (customPickerValue > 5) customPickerValue -= 5 },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Text(
                            "-",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Text(
                            "$customPickerValue",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Text(
                            "MIN",
                            color = MutedGray.copy(0.9f),
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace)
                        )
                    }
                    IconButton(
                        onClick = { if (customPickerValue < 480) customPickerValue += 5 },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Text(
                            "+",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Spacer(modifier = Modifier.height(64.dp))
                Button(
                    onClick = { viewModel.setDuration(customPickerValue); showCustomSheet = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftIndigo)
                ) {
                    Text(
                        "SET DURATION",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = OffWhite
                    )
                }
            }
        }
    }
}

private fun formatTime(totalSeconds: Long): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60

    return if (h > 0) {
        // Format: 1:00:00 (Hours included)
        "${h}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
    } else {
        // Format: 25:00 (Standard)
        "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
    }
}