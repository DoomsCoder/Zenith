package com.example.zenith.ui.screens.statistics

import android.annotation.SuppressLint
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
import com.example.zenith.ui.theme.SoftIndigo

@SuppressLint("DefaultLocale")
@Composable
fun AllTelemetrySection(
    metrics: AllTimeMetrics,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "All-time telemetry",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = SoftIndigo
            )
        )

        Spacer(modifier = modifier.height(16.dp))

        TelemetryRow(
            label = "Total sessions",
            value = metrics.totalSessions.toString()
        )
        TelemetryDivider()

        TelemetryRow(
            label = "Total focus hours",
            value = String.format("%.1fh", metrics.totalHours)
        )
        TelemetryDivider()

        TelemetryRow(
            label = "Completion rate",
            value = "${metrics.completionRate}%"
        )
        TelemetryDivider()

        TelemetryRow(
            label = "Best streak",
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
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MutedGray.copy(0.8f)
            )
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
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
