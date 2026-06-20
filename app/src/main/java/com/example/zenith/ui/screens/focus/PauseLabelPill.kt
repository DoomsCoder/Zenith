package com.example.zenith.ui.screens.focus

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenith.ui.theme.SoftIndigo

@Composable
fun PauseLabelPill(
    secondsRemaining: Int,
    isVisible: Boolean
) {
    val isUrgent = secondsRemaining <= 90

    val amberColor = Color(0xFFFFC396)

    val pillColor by animateColorAsState(
        targetValue = if (isUrgent) amberColor.copy(0.88f) else Color.White.copy(0.7f),
        animationSpec = tween(600)
    )
    val borderColor by animateColorAsState(
        targetValue = if (isUrgent) amberColor.copy(0.18f) else SoftIndigo.copy(0.4f),
        animationSpec = tween(600)
    )
    val bgColor by animateColorAsState(
        targetValue = if (isUrgent) amberColor.copy(0.06f) else SoftIndigo.copy(0.2f),
        animationSpec = tween(600)
    )

    val infiniteTransition = rememberInfiniteTransition(label = "DotPulse")
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Scale"
    )
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .padding(bottom = 12.dp)
                .wrapContentWidth(),
            shape = RoundedCornerShape(20.dp),
            color = bgColor,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // A. THE DOT (Pulsing if urgent)
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .scale(if (isUrgent) dotScale else 1f)
                        .background(
                            color = if (isUrgent) amberColor.copy(dotAlpha) else SoftIndigo.copy(0.7f),
                            shape = CircleShape
                        )
                )


                // B. LABEL
                Text(
                    text = "AUTO-RESUMES IN",
                    color = if (isUrgent) amberColor.copy(0.65f) else Color.White.copy(0.4f),
                    fontSize = 9.sp,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )

                // C. COUNTDOWN
                // We use AnimatedContent to get that "Cross-fade" per second
                AnimatedContent(
                    targetState = secondsRemaining,
                    transitionSpec = {
                        (fadeIn(tween(200)) + slideInVertically { it / 2 }) togetherWith
                                (fadeOut(tween(200)) + slideOutVertically { -it / 2 })
                    },
                    label = "TimerText"
                ) { targetSeconds ->
                    val m = targetSeconds / 60
                    val s = targetSeconds % 60
                    Text(
                        text = "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}",
                        color = pillColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }

}