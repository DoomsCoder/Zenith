package com.example.zenith.ui.screens.focus

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TimerCircle(
    label: String,
    timeDisplay: String,
    color: Color
) {
//    val strokeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
//    val textColor = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = Modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {

        Canvas(modifier = Modifier.fillMaxSize() ) {
            drawCircle(
                color = color,
                style = Stroke(width = 4.dp.toPx()),
                radius = size.minDimension / 2
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.5f),
                letterSpacing = 2.sp
            )
            Text(
                text = timeDisplay,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Light,
                    fontSize = 64.sp
                ),
                color = color
            )
        }
    }
}