package com.example.zenith.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination : NavKey {
    val showSystemBars: Boolean get() = true

    @Serializable
    data object Focus : Destination
    @Serializable
    data object Stats: Destination

    @Serializable
    data object Settings: Destination {
        override val showSystemBars: Boolean
            get() = false
    }
    @Serializable
    data object EngineConfig: Destination {
        override val showSystemBars: Boolean
            get() = false
    }
    @Serializable
    data object SessionHistory: Destination {
        override val showSystemBars: Boolean
            get() = false
    }
    companion object {
        val entries = listOf(Focus, Stats)
    }
}
