package com.example.zenith.ui.screens.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.TrackChanges
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
        title = "Engine Config",
        subtitle = "Strictness level · Call shield · Mercy buffer",
        icon = Icons.Outlined.Memory
    ),
    SettingsCategory(
        id = "presets",
        title = "Mission Presets",
        subtitle = "Pomodoro · Deep work · Custom mission flows",
        icon = Icons.Outlined.TrackChanges
    ),
    SettingsCategory(
        id = "whitelist",
        title = "Whitelist Manager",
        subtitle = "Allowed apps · Productivity exceptions",
        icon = Icons.Outlined.FilterList
    ),
    SettingsCategory(
        id = "notifications",
        title = "Notifications & Roasts",
        subtitle = "Roast intensity · Throttling · Sound alerts",
        icon = Icons.Outlined.Notifications
    ),
    SettingsCategory(
        id = "sensory",
        title = "Sensory Punishment",
        subtitle = "Haptic feedback · Vibration patterns",
        icon = Icons.Outlined.FlashOn
    ),
    SettingsCategory(
        id = "schedule",
        title = "Focus Schedule",
        subtitle = "Auto-start · Daily goals · Quiet hours",
        icon = Icons.Outlined.CalendarToday
    ),
    SettingsCategory(
        id = "appearance",
        title = "Appearance",
        subtitle = "Focus trends · Branded themes · Dark mode",
        icon = Icons.Outlined.Palette
    ),
    SettingsCategory(
        id = "data",
        title = "Data & Privacy",
        subtitle = "Export history · Factory reset · Local database",
        icon = Icons.Outlined.Storage
    ),
    SettingsCategory(
        id = "integrations",
        title = "Integrations",
        subtitle = "Google Calendar · Obsidian · Productivity sync",
        icon = Icons.Outlined.Link
    ),
    SettingsCategory(
        id = "about",
        title = "About Zenith",
        subtitle = "Version 1.0.4 · Credits · Debug info · License",
        icon = Icons.Outlined.Info
    )
)
