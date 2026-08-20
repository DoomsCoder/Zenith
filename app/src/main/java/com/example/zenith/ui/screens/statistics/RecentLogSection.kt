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
import com.example.zenith.ui.theme.SoftIndigo

@Composable
fun RecentLogSection(
    sessions: List<SessionHistoryItem>,
    onViewAllSessionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Recent log",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = SoftIndigo
            )
        )

        Spacer(Modifier.height(16.dp))

        sessions.forEachIndexed { index, session ->
            RecentSessionRow(session = session)

            if (index < sessions.size - 1) {
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
                text = "View all sessions",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = SoftIndigo
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = SoftIndigo,
                modifier = Modifier.size(16.dp)
            )
        }
    }

}

@Composable
private fun RecentSessionRow(session: SessionHistoryItem) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = session.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "${session.durationMinutes}m",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MutedGray
                )
            )
            Spacer(Modifier.width(8.dp))

            val statusIcon = if (session.isCompleted) Icons.Rounded.Check else Icons.Rounded.Close
            val statusColor = if (session.isCompleted) SoftIndigo else Color.DarkGray

            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(16.dp)
            )

        }
    }
}
