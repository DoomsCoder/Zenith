package com.example.zenith.ui.screens.statistics

import android.os.Parcelable
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenith.ui.theme.MutedGray
import com.example.zenith.ui.theme.OffWhite
import com.example.zenith.ui.theme.SoftIndigo
import kotlinx.parcelize.Parcelize

@Composable
fun TodayMissionLogSection(
    sessions: List<SessionHistoryItem>,
    onStartSessionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedSessionId by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's mission log",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = SoftIndigo
                )
            )

            if (sessions.isNotEmpty()) {
                Text(
                    text = "${sessions.size} sessions",
                    style = MaterialTheme.typography.bodySmall.copy(color = MutedGray)
                )
            }
        }

        if (sessions.isEmpty()) {
            EmptyStateCard(onStartSessionClick)
        } else {
            sessions.forEach { session ->
                ExpandableSessionCard(
                    session = session,
                    isExpanded = expandedSessionId == session.id,
                    onToggle = {
                        expandedSessionId = if (expandedSessionId == session.id) null else session.id
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ExpandableSessionCard(
    session: SessionHistoryItem,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onToggle() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.03f)),
        border = BorderStroke(1.dp, Color.White.copy(0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session.title,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    ),
                )

                Text(
                    text = "${session.durationMinutes} min",
                    style = MaterialTheme.typography.bodySmall.copy(color = MutedGray)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MutedGray.copy(0.3f),
                    modifier = Modifier.size(16.dp)
                )
            }

            if (isExpanded) {
                val successGreen = Color(0xFF4CAF50)
                val errorRed = Color(0xFFEF5350)
                
                val statusText = if (session.isCompleted) "Completed" else "Abandoned"
                val statusColor = if (session.isCompleted) successGreen else errorRed

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(0.05f))
                Spacer(modifier = Modifier.height(16.dp))
                
                TelemetryRow("Mission name", session.title)
                TelemetryRow("Date & time", session.dataTimeStr)
                TelemetryRow("Planned duration", "${session.plannedMinutes} min")
                TelemetryRow("Actual duration", "${session.durationMinutes} min")
                TelemetryRow("Status", value = statusText, valueColor = statusColor)

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Telemetry",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MutedGray.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.White.copy(0.05f))
                Spacer(modifier = Modifier.height(16.dp))

                TelemetryRow("Phone pickups", session.pickups.toString(), getTelemetryColor(session.pickups))
                TelemetryRow("App switches", session.appSwitches.toString(), getTelemetryColor(session.appSwitches))
                
                val impactColor = if (session.scoreImpact >= 0) successGreen else errorRed
                TelemetryRow(
                    "Focus score impact",
                    "${if (session.scoreImpact >= 0) "+" else ""}${session.scoreImpact} pts",
                    impactColor
                )
            }
        }
    }
}

@Composable
fun TelemetryRow(label: String, value: String, valueColor: Color = Color.White) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.5f),
            style = MaterialTheme.typography.bodySmall.copy(color = MutedGray)
        )
        Text(
            text = value,
            modifier = Modifier.weight(0.5f),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        )
    }
}

@Composable
fun EmptyStateCard(onStartSessionClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, color = Color.White.copy(0.06f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp, horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.03f),
                border = BorderStroke(1.dp, Color.White.copy(0.05f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Adjust, null, tint = MutedGray.copy(alpha = 0.2f), modifier = Modifier.size(32.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "No sessions yet today. Your focus record starts the moment you begin.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MutedGray.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onStartSessionClick,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SoftIndigo)
            ) {
                Text(
                    "Start first session",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}

private fun getTelemetryColor(count: Int): Color {
    return when (count) {
        0 -> Color(0xFF4CAF50)
        in 1..2 -> Color(0xFFFFA726)
        else -> Color(0xFFEF5350)
    }
}
