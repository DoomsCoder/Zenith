package com.example.zenith.ui.screens.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.ui.graphics.vector.ImageVector

data class SettingsCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

val settingsCategories = listOf(
    SettingsCategory(
        id = "engine",
        title = "Engine config",
        subtitle = "Strictness level · Call shield · Mercy buffer",
        icon = Icons.Outlined.Memory
    ),
    SettingsCategory(
        id = "whitelist",
        title = "Whitelist manager",
        subtitle = "Allowed apps · Productivity exceptions",
        icon = Icons.Outlined.FilterList
    ),
    SettingsCategory(
        id = "notifications",
        title = "Notifications & roasts",
        subtitle = "Roast intensity · Throttling · Sound alerts",
        icon = Icons.Outlined.Notifications
    ),
    SettingsCategory(
        id = "sensory",
        title = "Sensory punishment",
        subtitle = "Haptic feedback · Vibration patterns",
        icon = Icons.Outlined.FlashOn
    ),
    SettingsCategory(
        id = "data",
        title = "Data & privacy",
        subtitle = "Export history · Factory reset · Local database",
        icon = Icons.Outlined.Storage
    ),
    SettingsCategory(
        id = "about",
        title = "About Zenith",
        subtitle = "Version 1.0.4 · Credits · Debug info · License",
        icon = Icons.Outlined.Info
    )
)
