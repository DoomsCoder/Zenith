package com.example.zenith.ui.screens.statistics

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenith.ui.theme.MutedGray
import com.example.zenith.ui.theme.SoftIndigo
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max
import com.patrykandpatrick.vico.core.common.shape.Shape as VicoShape

data class DailyFocusMetrics(
    val date: LocalDate,
    val totalMinutes: Int,
    val sessionCount: Int
)

private val datesKey = ExtraStore.Key<List<LocalDate>>()

@SuppressLint("DefaultLocale")
@Composable
fun ThisWeeksFocusChart(
    metrics: List<DailyFocusMetrics>
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val activeDays = metrics.count { it.totalMinutes > 0 }

    var selectedColumnIndex by remember { mutableStateOf<Int?>(null) }
    var chartSize by remember { mutableStateOf(IntSize.Zero) }

    //Sync data with Vico's Model Provider
    LaunchedEffect(metrics) {
        modelProducer.runTransaction {
            columnSeries {
                // Requirement: 0 minutes shows a tiny bar (0.8f) so it doesn't disappear
                series(metrics.map { max(0.8f, it.totalMinutes.toFloat()) })
            }
            extras { it[datesKey] = metrics.map { m -> m.date } }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { selectedColumnIndex = null}
    ) {
        // Header Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "THIS WEEK'S FOCUS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = MutedGray.copy(0.7f)
                )
            )
            Text(
                text = "$activeDays / 7 days active",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = MutedGray.copy(0.4f)
                )
            )
        }

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),

            contentAlignment = Alignment.Center
        ){
            if (selectedColumnIndex != null) {
                val m = metrics.getOrNull(selectedColumnIndex!!)
                if (m != null) {
                    val dateStr = m.date.format(DateTimeFormatter.ofPattern("MMM dd"))
                    Surface(
                        color = Color(0xFF1A1A1A), // Dark Charcoal
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "$dateStr · ${m.totalMinutes} min · ${m.sessionCount} sessions",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
            }
        }

        // We create the components list here so they react to 'selectedColumnIndex'


        // Chart Section
        CartesianChartHost(
            chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                            remember(selectedColumnIndex) {
                                metrics.mapIndexed { index, _ ->
                                    val isToday = index == 6
                                    val baseColor = if (isToday) Color(0xFF6366F1) else SoftIndigo

                                    // Fading Logic
                                    val finalAlpha = when {
                                        selectedColumnIndex == null -> 1.0f
                                        selectedColumnIndex == index -> 1.0f
                                        else -> 0.3f
                                    }

                                    LineComponent(
                                        color = baseColor.copy(alpha = finalAlpha).toArgb(),
                                        thicknessDp = 34f,
                                        shape = VicoShape.rounded(25,25,0,0),
                                        strokeThicknessDp = if (isToday) 1f else 0f,
                                        strokeColor = if (isToday) Color.White.copy(0.2f).toArgb() else 0
                                    )
                                }
                            }
                        ),
                        columnCollectionSpacing = 8.dp
                    ),
                // Named Parameter: Bottom Axis
                bottomAxis = rememberBottomAxis(
                    guideline = null,
                    tick = null,
                    line = null,
                    valueFormatter = { x, chartValues, _ ->
                        val date = chartValues.model.extraStore[datesKey][x.toInt()]
                        if (date == LocalDate.now()) "TODAY" else date.format(DateTimeFormatter.ofPattern("dd"))
                    },
                    label = rememberTextComponent(
                        color = MutedGray.copy(alpha = 0.7f),
                        textSize = 10.sp
                    )
                )
            ),
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .onGloballyPositioned{ chartSize = it.size}
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val segmentWidth = chartSize.width / 7f
                        val tappedIndex = (offset.x / segmentWidth)
                            .toInt()
                            .coerceIn(0, 6)

                        selectedColumnIndex = if (selectedColumnIndex == tappedIndex) null else tappedIndex
                    }
                }
        )

        val totalHrs = metrics.sumOf { it.totalMinutes } / 60f

        Text(
            text = String.format("Total this week: %.1f hrs", totalHrs),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                color = MutedGray.copy(0.6f)
            )
        )
    }
}