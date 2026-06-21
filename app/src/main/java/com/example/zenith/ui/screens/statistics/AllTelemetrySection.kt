package com.example.zenith.ui.screens.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenith.ui.theme.MutedGray

data class AllTimeMetrics(
    val totalSessions: Int,
    val totalHours: Float,
    val completionRate: Int,
    val bestStreak: Int
)

@Composable
fun AllTelemetrySection(
    metrics: AllTimeMetrics,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header
        Text(
            text = "ALL-TIME TELEMETRY",
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = MutedGray.copy(0.7f)
            )
        )

        Spacer(modifier = modifier.height(16.dp))

        TelemetryRow(
            label = "Total Sessions",
            value = metrics.totalSessions.toString()
        )
        TelemetryDivider()

        TelemetryRow(
            label = "Total Focus Hours",
            value = "${metrics.totalHours}h"
        )
        TelemetryDivider()

        TelemetryRow(
            label = "Completion Rate",
            value = "${metrics.completionRate}%"
        )
        TelemetryDivider()

        TelemetryRow(
            label = "Best Streak",
            value = "${metrics.bestStreak} days"
        )
    }
}

@Composable
private fun TelemetryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label Styling (Left Side)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MutedGray.copy(0.8f)
            )
        )

        // Value Styling (Right Side)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE2E8F0) // Crispy Off-White
            )
        )
    }
}

@Composable
private fun TelemetryDivider(){
    HorizontalDivider(
        thickness = 1.dp,
        color = Color.White.copy(alpha = 0.05f)
    )
}