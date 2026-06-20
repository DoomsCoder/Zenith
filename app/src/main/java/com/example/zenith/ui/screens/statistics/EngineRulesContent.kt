package com.example.zenith.ui.screens.statistics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenith.ui.theme.MutedGray
import com.example.zenith.ui.theme.OffWhite
import com.example.zenith.ui.theme.SoftIndigo

private val Charcoal = Color(0xFF1A1A1A)
private val SoftRed = Color(0xFFEF5350)
private val SoftGreen = Color(0xFF4CAF50)

@Composable
fun EngineRulesContent(onClose: () -> Unit) {

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF121212))
            .verticalScroll(scrollState)
            .padding(24.dp)
            .padding(bottom = 48.dp)
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ENGINE RULES",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = SoftIndigo,
                        letterSpacing = 2.sp
                    )
                )

                Spacer(Modifier.height(12.dp))
                Text(
                    text = "How your Focus Score is calculated and what each tier means.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MutedGray.copy(alpha = 0.6f)
                    )
                )
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier.offset(x = 12.dp, y = (-8).dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = MutedGray)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 1. SCORING EVENTS TABLE ---
        Text(
            "SCORING EVENTS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MutedGray.copy(alpha = 0.4f),
                letterSpacing = 1.sp
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Charcoal,
            border = BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.3f))
        ) {
            Column {
                val events = listOf(
                    "Complete session" to "+100 pts",
                    "Every focus minute" to "+2 pts",
                    "Abandon session" to "-50 pts",
                    "Phone pickup" to "-10 pts",
                    "App switch" to "-15 pts",
                    "7-day streak bonus" to "+200 pts"
                )

                events.forEachIndexed { index, event ->
                    ScoringRow(
                        label = event.first,
                        pts = event.second,
                        isNegative = event.second.startsWith("-")
                    )
                    if (index < events.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = Color.DarkGray.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 2. TIER THRESHOLDS TABLE ---
        Text(
            "TIER THRESHOLDS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MutedGray.copy(alpha = 0.4f),
                letterSpacing = 1.sp
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Charcoal,
            border = BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.3f))
        ) {
            Column {
                TierRow(id = "T1", name = "INITIALIZING", range = "0 - 499")
                TierDivider()
                TierRow(id = "T2", name = "BUILDING FOCUS", range = "500 - 1,499")
                TierDivider()
                TierRow(id = "T3", name = "DEEP WORKER", range = "1,500 - 2,999", isCurrent = true)
                TierDivider()
                TierRow(id = "T4", name = "FLOW STATE", range = "3,000 - 4,999")
                TierDivider()
                TierRow(id = "T5", name = "ZENITH ACHIEVED", range = "5,000+")
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // --- 3. STREAK LOGIC INFO BOX (System Directive) ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Charcoal,
            shape = RoundedCornerShape(4.dp)
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                // Indigo Left Indicator
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(SoftIndigo)
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("The Simple Rule: ")
                            }
                            withStyle(SpanStyle(color = MutedGray, fontStyle = FontStyle.Italic)) {
                                append("One completed session = day saved.\n")
                            }
                            withStyle(SpanStyle(color = MutedGray.copy(alpha = 0.8f))) {
                                append("Did the user complete at least ONE session today? ")
                            }
                            withStyle(SpanStyle(color = SoftGreen, fontWeight = FontWeight.Bold)) {
                                append("Yes = streak lives. ")
                            }
                            withStyle(SpanStyle(color = SoftRed, fontWeight = FontWeight.Bold)) {
                                append("No = streak breaks.")
                            }
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 20.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoringRow(label: String, pts: String, isNegative: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = OffWhite.copy(alpha = 0.9f))
        )
        Text(
            text = pts,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (isNegative) SoftRed else SoftIndigo
            )
        )
    }
}

@Composable
private fun TierDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = Color.DarkGray.copy(alpha = 0.3f)
    )
}

@Composable
private fun TierRow(id: String, name: String, range: String, isCurrent: Boolean = false) {
    val rowBg = if (isCurrent) SoftIndigo.copy(alpha = 0.1f) else Color.Transparent
    val contentColor = if (isCurrent) SoftIndigo else MutedGray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = id,
            modifier = Modifier.width(28.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                color = contentColor.copy(alpha = 0.6f)
            )
        )

        Text(
            text = name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        )

        if (isCurrent) {
            Surface(
                color = SoftIndigo,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Text(
                    text = "YOU",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                )
            }
        }

        Text(
            text = range,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                color = MutedGray.copy(alpha = 0.4f)
            )
        )
    }
}