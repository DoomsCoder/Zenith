package com.example.zenith.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination : NavKey {
    @Serializable
    data object Focus : Destination
    @Serializable
    data object Stats: Destination

    @Serializable
    data object Settings: Destination
    companion object {
        val entries = listOf(Focus, Stats)
    }
}
