package com.example.zenith.ui.screens.statistics

import androidx.lifecycle.SavedStateHandle
import com.example.zenith.ui.common.UiStateMachine
import com.example.zenith.ui.common.asUiStateMachine
import kotlinx.coroutines.flow.StateFlow

class StatisticsViewModel(
    savedState: SavedStateHandle
) {
    private val uiStateMachine: UiStateMachine<StatisticsUIState> =
        savedState.asUiStateMachine(StatisticsUIState())

    val uiState: StateFlow<StatisticsUIState> by uiStateMachine

    init {
        if (!uiStateMachine.isStateRestored){
            refreshStats()
        }
    }

    private fun refreshStats() {
        // Logic to fetch from database...
    }
}