package com.example.zenith.ui.screens.statistics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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

data class RecentSessionSummary(
    val title: String,
    val durationMinutes: Int,
    val isCompleted: Boolean
)
@Composable
fun RecentLogSection(
    onViewAllSessionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Dummy data
    val recentSessions = listOf(
        RecentSessionSummary("Deep Work - PR Review", 25, true),
        RecentSessionSummary("Chapter 4 Reading", 18, false),
        RecentSessionSummary("System Architecture Design", 45, true)
    )

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Section Header
        Text(
            text = "RECENT LOG",
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = MutedGray.copy(0.7f)
            )
        )

        Spacer(Modifier.height(16.dp))

        recentSessions.forEachIndexed { index, session ->
            RecentSessionRow(session = session)

            if (index < recentSessions.size - 1) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = Color.White.copy(0.05f)
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = modifier
                .align(Alignment.End)
                .clickable{ onViewAllSessionsClick() }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VIEW ALL SESSIONS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF6366F1) // SoftIndigo
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = Color(0xFF6366F1),
                modifier = Modifier.size(16.dp)
            )
        }
    }

}

@Composable
private fun RecentSessionRow(session: RecentSessionSummary) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Side: Title
        Text(
            text = session.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFFE2E8F0), // Crispy Off White
                fontWeight = FontWeight.Medium
            )
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            // Right Side: Duration & Status
            Text(
                text = "${session.durationMinutes}m",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    color = MutedGray.copy(0.6f)
                )
            )
            Spacer(Modifier.width(8.dp))

            val statusIcon = if (session.isCompleted) Icons.Rounded.Check else Icons.Rounded.Close
            val statusColor = if (session.isCompleted) Color(0xFF6366F1) else Color.DarkGray

            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(16.dp)
            )

        }
    }
}