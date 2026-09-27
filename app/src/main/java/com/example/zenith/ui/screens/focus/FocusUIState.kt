package com.example.zenith.ui.screens.focus

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * SessionState defines the lifecycle of a focus block.
 */
@Parcelize
enum class SessionState : Parcelable {
    IDLE, RUNNING, PAUSED, FINISHED, ABANDONED
}

/**
 * FocusViewState represents the entire UI state for the Focus Screen.
 * Using a single data class ensures a "Single Source of Truth."
 */
@Parcelize
data class FocusViewState (
    val sessionState: SessionState = SessionState.IDLE,
    val missionText: String = "",
    val selectedDurationMinutes: Int = 25,
    val remainingFocusSeconds: Int = 0,
    val totalFocusSeconds: Int = 0,
    val totalBreakBankSeconds: Int = 300,
    val remainingBreakBankSeconds: Int = 300,
    val isBreakAllowanceSet: Boolean = false,
    val isPausedByCall: Boolean = false,
    val stateBeforeCall: SessionState? = null,
    val snapshotBeforeAbandon: FocusViewState? = null,
    
    // Penalty tracking
    val lastPenaltySeconds: Int = 0,
    val isIntegrityCompromised: Boolean = false,
    val showPenaltyFlash: Boolean = false,
    val isProgressFrozen: Boolean = false
) : Parcelable
