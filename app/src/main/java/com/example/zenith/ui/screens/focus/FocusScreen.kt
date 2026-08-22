package com.example.zenith.ui.screens.focus

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zenith.logic.FocusMath
import com.example.zenith.ui.theme.MutedGray
import com.example.zenith.ui.theme.OffWhite
import com.example.zenith.ui.theme.SoftIndigo
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(viewModel: FocusViewModel = viewModel()) {

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) Log.d("Zenith", "Call Shield will not work without permission")
    }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.READ_PHONE_STATE)
    }
    
    val state by viewModel.uiState.collectAsState()
    var showCustomSheet by rememberSaveable { mutableStateOf(false) }
    var customPickerValue by rememberSaveable { mutableIntStateOf(45) }

    var pressingProgress by rememberSaveable { mutableFloatStateOf(0f) }
    var isHolding by rememberSaveable { mutableStateOf(false) }
    val abandonColor = Color(0xFFFFC8AF).copy(0.38f)
    val penaltyRed = Color(0xFFEF5350)

    val completionTimestamp = remember(state.sessionState) {
        if (state.sessionState == SessionState.FINISHED) {
            val now = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            "Today • ${now.format(formatter)}"
        } else ""
    }

    val focusManager = LocalFocusManager.current

    val displayTime = remember(state.sessionState, state.remainingFocusSeconds, state.selectedDurationMinutes) {
        val totalSeconds = if (state.sessionState == SessionState.IDLE) {
            state.selectedDurationMinutes * 60L
        } else {
            state.remainingFocusSeconds.toLong()
        }
        formatTime(totalSeconds)
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
                        text = "Current mission",
                        color = SoftIndigo.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
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
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(SoftIndigo),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (state.missionText.isEmpty()) {
                                Text(
                                    text = "What are you focusing on?",
                                    color = OffWhite.copy(0.2f),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            val statusText = when {
                state.isPausedByCall -> "Call detected — Paused"
                state.isIntegrityCompromised -> "Integrity compromised"
                state.sessionState == SessionState.IDLE -> "System status: Ready"
                state.sessionState == SessionState.RUNNING -> "Deep focus active"
                state.sessionState == SessionState.PAUSED -> "Bio-break active"
                state.sessionState == SessionState.FINISHED -> "Session complete"
                state.sessionState == SessionState.ABANDONED -> "Session abandoned"
                else -> "System status: Ready"
            }

            val statusColor = when {
                state.isPausedByCall -> Color(0xFFFFA726)
                state.isIntegrityCompromised -> penaltyRed
                state.sessionState == SessionState.RUNNING -> SoftIndigo
                state.sessionState == SessionState.ABANDONED || state.sessionState == SessionState.FINISHED -> abandonColor.copy(1f)
                else -> MutedGray.copy(0.6f)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).background(statusColor, RoundedCornerShape(50)))
                Spacer(Modifier.width(8.dp))
                AnimatedContent(
                    targetState = statusText,
                    transitionSpec = {
                        slideInVertically(animationSpec = tween(600)) { height -> height } +
                                fadeIn(animationSpec = tween(600)) togetherWith
                                slideOutVertically(animationSpec = tween(600)) { height -> -height } +
                                fadeOut(animationSpec = tween(600))
                    },
                    label = "StatusAnimation"
                ) { targetText ->
                    Text(
                        text = targetText,
                        color = statusColor,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
                val remaining = state.remainingFocusSeconds
                val total = state.totalFocusSeconds
                
                val progress = if (total > 0) ((total - remaining).toFloat() / total).coerceIn(0f, 1f) else 0f
                val debtProgress = if (total > 0 && remaining > total) ((remaining - total).toFloat() / total).coerceIn(0f, 1f) else 0f
                
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.minDimension / 2
                    val tickOuterRadius = radius - 14.dp.toPx()
                    
                    for (i in 0 until 60) {
                        val angleInDegrees = (i * 6) - 90
                        val angleInRadians = Math.toRadians(angleInDegrees.toDouble())
                        
                        val tickProgress = i / 60f
                        
                        val color = when {
                            debtProgress > 0f -> {
                                val reverseTickProgress = (60 - i) % 60 / 60f
                                if (reverseTickProgress <= debtProgress && i != 0) penaltyRed.copy(0.6f) 
                                else MutedGray.copy(0.1f)
                            }
                            progress > 0f -> {
                                if (tickProgress <= progress) SoftIndigo.copy(0.6f) 
                                else if (i % 5 == 0) MutedGray.copy(0.3f) else MutedGray.copy(0.1f)
                            }
                            else -> if (i % 5 == 0) MutedGray.copy(0.3f) else MutedGray.copy(0.1f)
                        }

                        val tickLength = if (i % 5 == 0) 10.dp.toPx() else 5.dp.toPx()
                        val strokeWidth = if (i % 5 == 0) 2.dp.toPx() else 1.dp.toPx()

                        val startX = center.x + (tickOuterRadius - tickLength) * cos(angleInRadians).toFloat()
                        val startY = center.y + (tickOuterRadius - tickLength) * sin(angleInRadians).toFloat()
                        val endX = center.x + tickOuterRadius * cos(angleInRadians).toFloat()
                        val endY = center.y + tickOuterRadius * sin(angleInRadians).toFloat()
                        
                        drawLine(color = color, start = Offset(startX, startY), end = Offset(endX, endY), strokeWidth = strokeWidth)
                    }

                    val arcStrokeWidth = 4.dp.toPx()
                    val arcSize = size.copy(width = size.width - arcStrokeWidth, height = size.height - arcStrokeWidth)
                    val arcTopLeft = Offset(arcStrokeWidth / 2, arcStrokeWidth / 2)

                    if (debtProgress > 0f) {
                        drawArc(
                            color = penaltyRed,
                            startAngle = -90f,
                            sweepAngle = -360f * debtProgress,
                            useCenter = false,
                            style = Stroke(width = arcStrokeWidth, cap = StrokeCap.Round),
                            size = arcSize,
                            topLeft = arcTopLeft
                        )
                    } else if (progress > 0f) {
                        drawArc(
                            color = SoftIndigo,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            style = Stroke(width = arcStrokeWidth, cap = StrokeCap.Round),
                            size = arcSize,
                            topLeft = arcTopLeft
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = displayTime, style = TextStyle(color = if (state.isIntegrityCompromised) penaltyRed else Color.White, fontSize = if (state.selectedDurationMinutes >= 60) 40.sp else 48.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace))
                    Text(text = "Remaining", color = MutedGray.copy(alpha = 0.4f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium))
                }
            }
            
            // Debt Pill
            AnimatedVisibility(visible = state.isIntegrityCompromised) {
                Surface(
                    color = penaltyRed.copy(0.15f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, penaltyRed.copy(0.5f)),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = "+${formatTime(state.lastPenaltySeconds.toLong())} debt added",
                        color = penaltyRed,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // OG Preset Capsules Style
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Row(modifier = Modifier.width(280.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("25m", "50m", "Custom").forEach { label ->
                        val isInteractionAllowed = state.sessionState == SessionState.IDLE
                        val chipText = if (label == "Custom" && state.selectedDurationMinutes !in listOf(25, 50)) "${state.selectedDurationMinutes}m" else label
                        val isSelected = (label == "25m" && state.selectedDurationMinutes == 25) || (label == "50m" && state.selectedDurationMinutes == 50) || (label == "Custom" && state.selectedDurationMinutes !in listOf(25, 50))

                        Surface(
                            modifier = Modifier.weight(1f).height(40.dp).alpha(if (isInteractionAllowed) 1f else 0.3f).clickable(enabled = isInteractionAllowed) {
                                if (label == "Custom") showCustomSheet = true else viewModel.setDuration(if (label == "25m") 25 else 50)
                            },
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(width = 1.dp, color = if (isSelected) SoftIndigo.copy(0.6f) else Color.White.copy(0.05f)),
                            color = if (isSelected) SoftIndigo.copy(0.12f) else Color.Transparent
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = chipText, color = if (isSelected) SoftIndigo else MutedGray.copy(0.6f), style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            val isIntentClear = isHolding && pressingProgress > 0.03f
            val buttonText = when (state.sessionState) {
                SessionState.IDLE -> "Initiate focus session"
                SessionState.RUNNING -> if (pressingProgress > 0.15f) "Hold to abandon..." else "Pause session"
                SessionState.PAUSED -> if (pressingProgress > 0.15f) "Hold to abandon..." else "Resume session"
                SessionState.FINISHED, SessionState.ABANDONED -> "Initiate focus session"
            }

            val syncedButtonColor by animateColorAsState(targetValue = if (isIntentClear) Color(0xFF2A2A2A) else SoftIndigo, animationSpec = tween(150), label = "ButtonColor")

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PauseLabelPill(secondsRemaining = state.remainingBreakBankSeconds, isVisible = state.sessionState == SessionState.PAUSED && state.isBreakAllowanceSet)
                BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp), contentAlignment = Alignment.Center) {
                    val currentWidth = maxWidth
                    if (state.sessionState == SessionState.PAUSED && state.isBreakAllowanceSet) {
                        PauseRing(secondsRemaining = state.remainingBreakBankSeconds, totalSeconds = state.totalBreakBankSeconds, buttonWidth = currentWidth)
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(16.dp)).background(if (state.sessionState == SessionState.IDLE && state.missionText.isBlank()) MutedGray.copy(0.6f) else syncedButtonColor)
                            .pointerInput(state.sessionState) {
                                detectTapGestures(
                                    onPress = {
                                        if (state.sessionState == SessionState.IDLE) {
                                            val released = tryAwaitRelease()
                                            if (released && state.missionText.isNotBlank()) viewModel.startSession()
                                        } else {
                                            val pressStartTime = System.currentTimeMillis()
                                            isHolding = true
                                            try { tryAwaitRelease() } finally {
                                                isHolding = false
                                                val holdDuration = System.currentTimeMillis() - pressStartTime
                                                if (holdDuration >= 3000) viewModel.abandonSession()
                                                else if (holdDuration < 300) viewModel.toggleFocusSession()
                                                pressingProgress = 0f
                                            }
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        LaunchedEffect(isHolding) {
                            if (isHolding) {
                                val start = System.currentTimeMillis()
                                while (isHolding) {
                                    pressingProgress = (System.currentTimeMillis() - start) / 3000f
                                    if (pressingProgress >= 1f) { viewModel.abandonSession(); isHolding = false; break }
                                    delay(16)
                                }
                            } else pressingProgress = 0f
                        }
                        if (isIntentClear) Box(modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(pressingProgress).height(2.dp).background(Color.White))
                        AnimatedContent(targetState = buttonText, transitionSpec = { slideInVertically(animationSpec = tween(600)) { height -> height } + fadeIn(animationSpec = tween(600)) togetherWith slideOutVertically(animationSpec = tween(600)) { height -> -height } + fadeOut(animationSpec = tween(600)) }, label = "ButtonTextTransition") { targetText ->
                            Text(text = targetText, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = if (isIntentClear) abandonColor else OffWhite)
                        }
                    }
                }

                AnimatedVisibility(visible = state.sessionState == SessionState.RUNNING || state.sessionState == SessionState.PAUSED, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 20.dp)) {
                        Box(modifier = Modifier.width(40.dp).height(1.dp).background(MutedGray.copy(0.2f)))
                        Spacer(Modifier.height(20.dp))
                        Text(text = "Abandon session", color = abandonColor, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium), modifier = Modifier.clickable { viewModel.abandonSession() }.padding(8.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(200.dp))
        }
        
        // Red Flash Overlay
        AnimatedVisibility(
            visible = state.showPenaltyFlash,
            enter = fadeIn(tween(100)),
            exit = fadeOut(tween(500))
        ) {
            Box(Modifier.fillMaxSize().background(penaltyRed.copy(0.25f)))
        }

        if (state.sessionState == SessionState.FINISHED) {
            CompletionOverlay(missionName = state.missionText, durationText = formatTime(state.selectedDurationMinutes * 60L), timestamp = completionTimestamp, onDismiss = { viewModel.resetToDefaults() })
        }

        if (state.sessionState == SessionState.ABANDONED) {
            val elapsedSeconds = (state.totalFocusSeconds - state.remainingFocusSeconds).toLong()
            AbandonToast(elapsedText = formatTime(elapsedSeconds), onDismiss = { viewModel.resetToDefaults() }, onUndo = { viewModel.undoAbandon() })
        }
    }

    // Dynamic Break Allowance Sheet
    if (state.sessionState == SessionState.PAUSED && !state.isBreakAllowanceSet) {
        val missionMins = state.selectedDurationMinutes
        val standardMins = FocusMath.calculateBreakBank(missionMins, isRelaxed = false)
        val relaxedMins = FocusMath.calculateBreakBank(missionMins, isRelaxed = true)

        ModalBottomSheet(
            onDismissRequest = { viewModel.resumeSession() },
            containerColor = Color(0xFF1A1A1A),
            scrimColor = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
                Text(
                    "Break allowance", 
                    color = SoftIndigo, 
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    "Choose total break time for this mission.", 
                    color = Color.White.copy(0.6f), 
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(24.dp))
                
                listOf(
                    Triple(standardMins, "Standard", Icons.Default.Timer),
                    Triple(relaxedMins, "Relaxed", Icons.Default.Coffee),
                    Triple(0, "Monk mode", Icons.Default.Lock)
                ).forEach { (mins, label, icon) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setBreakAllowance(mins) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(0.05f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(icon, null, tint = SoftIndigo, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            val titleText = if (mins == 0) "$label (No breaks)" else label
                            Text(titleText, color = Color.White, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
                            if (mins > 0) {
                                Text("$mins minutes total bank", color = MutedGray, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }

    // Custom Duration Sheet
    if (showCustomSheet) {
        ModalBottomSheet(onDismissRequest = { showCustomSheet = false }, containerColor = Color(0xFF1A1A1A), scrimColor = Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Custom duration", color = SoftIndigo, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    IconButton(onClick = { showCustomSheet = false }) { Icon(Icons.Default.Close, "Close", tint = Color.White.copy(0.5f)) }
                }
                Spacer(modifier = Modifier.height(48.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (customPickerValue > 5) customPickerValue -= 5 }, modifier = Modifier.size(64.dp)) {
                        Text("-", color = Color.White, fontSize = 32.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 32.dp)) {
                        if (customPickerValue >= 60) {
                            val hours = customPickerValue / 60
                            val mins = customPickerValue % 60
                            Text(
                                text = if (mins > 0) "${hours}H ${mins}M" else "${hours}H",
                                color = SoftIndigo,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        Text("$customPickerValue", style = TextStyle(color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace))
                        Text("Min", color = MutedGray.copy(0.9f), style = MaterialTheme.typography.labelSmall)
                    }
                    IconButton(onClick = { if (customPickerValue < 480) customPickerValue += 5 }, modifier = Modifier.size(64.dp)) {
                        Text("+", color = Color.White, fontSize = 32.sp)
                    }
                }
                Spacer(modifier = Modifier.height(64.dp))
                Button(onClick = { viewModel.setDuration(customPickerValue); showCustomSheet = false }, modifier = Modifier.fillMaxWidth().height(64.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = SoftIndigo)) {
                    Text("Set duration", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = OffWhite)
                }
            }
        }
    }
}

private fun formatTime(totalSeconds: Long): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) "${h}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
    else "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}
