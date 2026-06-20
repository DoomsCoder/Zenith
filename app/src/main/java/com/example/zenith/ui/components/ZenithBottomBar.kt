package com.example.zenith.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenith.ui.navigation.Destination
import com.example.zenith.ui.theme.Charcoal
import com.example.zenith.ui.theme.MutedGray
import com.example.zenith.ui.theme.SoftIndigo
import com.example.zenith.ui.theme.ZenithTheme

@Composable
fun ZenithBottomBar(
    currentDestination: Destination,
    onNavigate: (Destination) -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        shape = RoundedCornerShape(24.dp),
        color = Charcoal,
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(0.05f)
            ),
        tonalElevation = 0.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            modifier = Modifier.height(64.dp),
            tonalElevation = 0.dp
        ) {
            Destination.entries.forEach { destination ->
                val isSelected = currentDestination == destination

                val label = when (destination) {
                    Destination.Focus -> "FOCUS"
                    Destination.Stats -> "STATS"
                    else -> ""
                }
                val icon: ImageVector = when (destination) {
                    Destination.Focus -> Icons.Outlined.Timer
                    Destination.Stats -> Icons.Outlined.BarChart
                    else -> Icons.Outlined.Timer
                }

                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onNavigate(destination) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SoftIndigo,
                        selectedTextColor = SoftIndigo,
                        unselectedIconColor = MutedGray,
                        unselectedTextColor = MutedGray,
                        indicatorColor = Color.Transparent
                    ),
                    icon = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Nordic Indicator Line: Only visible when selected
                            Box(
                                modifier = Modifier
                                    .width(16.dp)
                                    .height(2.dp)
                                    .background(
                                        color = if (isSelected) SoftIndigo else Color.Transparent,
                                        shape = RoundedCornerShape(1.dp)
                                    )
                            )
                            Spacer(Modifier.height(4.dp))
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                    },
                    label = {
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                            )
                    }
                )
            }
    }

    }
}

@Preview(showBackground = true)
@Composable
fun ZenithBottomBarPreview() {
    ZenithTheme {
        ZenithBottomBar(
            currentDestination = Destination.Focus,
            onNavigate = {}
        )
    }
}
