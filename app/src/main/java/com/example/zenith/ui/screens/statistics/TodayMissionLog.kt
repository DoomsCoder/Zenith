package com.example.zenith.ui.screens.statistics

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenith.ui.theme.MutedGray
import com.example.zenith.ui.theme.OffWhite
import com.example.zenith.ui.theme.SoftIndigo

data class SessionDummyData(
    val id: Int,
    val missionName: String,
    val plannedDurationMinutes: Int,
    val actualDurationMinutes: Int,
    val isCompleted: Boolean,
    val date: String,
    val phonePickups: Int,
    val appSwitches: Int,
    val focusScoreImpact: Int
)

@Composable
fun TodayMissionLogSection(
    sessions: List<SessionDummyData>,
    onStartSessionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Accordion State: Tracks which card ID is expanded. null means all are closed.
    var expandedSessionId by remember { mutableStateOf<Int?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TODAY'S MISSION LOG",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = MutedGray.copy(alpha = 0.7f)
                )
            )

            if (sessions.isNotEmpty()) {
                Text(
                    text = "${sessions.size} sessions",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = MutedGray.copy(0.5f)
                    )
                )
            }
        }

        if (sessions.isEmpty()) {
            EmptyStateCard(onStartSessionClick)
        } else {
            sessions.forEach { sessions ->
                ExpandableSessionCard(
                    session = sessions,
                    isExpanded = expandedSessionId == sessions.id,
                    onToggle = {
                        expandedSessionId =
                            if (expandedSessionId == sessions.id) null else sessions.id
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ExpandableSessionCard(
    session: SessionDummyData,
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(0.03f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            //Collapsed Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session.missionName,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        color = OffWhite
                    ),
                )

                Text(
                    text = "${session.actualDurationMinutes} min",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = MutedGray
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MutedGray.copy(0.3f),
                    modifier = Modifier.size(16.dp)
                )
            }

            val statusText = if (session.isCompleted) "✓ Complete" else "✗ Abandoned"
            val statusColor = if (session.isCompleted) SoftIndigo else Color(0xFFEF5350).copy(0.7f)

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(0.05f))
                Spacer(modifier = Modifier.height(16.dp))
                TelemetryRow("MISSION NAME", session.missionName)
                TelemetryRow("DATE & TIME", session.date)
                TelemetryRow("PLANNED DURATION", "${session.plannedDurationMinutes} min")
                TelemetryRow("ACTUAL DURATION", "${session.actualDurationMinutes} min")
                TelemetryRow(
                    "STATUS", value = statusText,
                    valueColor = statusColor
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "TELEMETRY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = MutedGray.copy(alpha = 0.5f)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(16.dp))

                TelemetryRow(
                    "Phone Pickups",
                    session.phonePickups.toString(),
                    getTelemetryColor(session.phonePickups)
                )
                TelemetryRow(
                    "App Switches",
                    session.appSwitches.toString(),
                    getTelemetryColor(session.appSwitches)
                )
                TelemetryRow(
                    "Focus Score Impact",
                    "${if (session.focusScoreImpact >= 0) "+" else ""}${session.focusScoreImpact} pts",
                    if (session.focusScoreImpact >= 0) Color(0xFF4CAF50) else Color(0xFFEF5350)
                )
            }
        }
    }
}

@Composable
fun TelemetryRow(label: String, value: String, valueColor: Color = Color.White) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.4f),
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                color = MutedGray.copy(alpha = 0.6f)
            )
        )
        Text(
            text = value,
            modifier = Modifier.weight(0.6f),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        )
    }
}

@Composable
fun EmptyStateCard(onStartSessionClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(
            1.dp,
            color = Color.White.copy(0.06f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp, horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.03f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Adjust,
                        null,
                        tint = MutedGray.copy(alpha = 0.2f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "No sessions yet today. Your focus\nrecord starts the moment you begin.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    color = MutedGray.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SoftIndigo.copy(0.3f))
            ) {
                Text(
                    " START FIRST SESSION ",
                    modifier = Modifier.clickable { onStartSessionClick() },
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = SoftIndigo
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

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun TodayMissionLogPreview() {
    val dummySessions = listOf(
        SessionDummyData(
            id = 1,
            missionName = "AnkiDroid PR #20849",
            plannedDurationMinutes = 120,
            actualDurationMinutes = 120,
            isCompleted = true,
            date = "Jun 16, 3:05 PM",
            phonePickups = 0,
            appSwitches = 1,
            focusScoreImpact = 240
        ),
        SessionDummyData(
            id = 2,
            missionName = "OpenAI Buildathon Wealnex Setup",
            plannedDurationMinutes = 45,
            actualDurationMinutes = 14,
            isCompleted = false,
            date = "Jun 16, 4:20 PM",
            phonePickups = 3,
            appSwitches = 5,
            focusScoreImpact = -50
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(top = 24.dp)
    ) {
        TodayMissionLogSection(
            sessions = emptyList(),
            onStartSessionClick = {}
        )
    }
}