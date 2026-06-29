package com.example.zenith.ui.screens.focus

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenith.service.FocusService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * SessionState defines the lifecycle of a focus block.
 */
enum class SessionState {
    IDLE, RUNNING, PAUSED, FINISHED, ABANDONED
}

/**
 * FocusViewState represents the entire UI state for the Focus Screen.
 * Using a single data class ensures a "Single Source of Truth."
 */
data class FocusViewState (
    val sessionState: SessionState = SessionState.IDLE,
    val missionText: String = "",
    val selectedDurationMinutes: Int = 25,
    val remainingFocusSeconds: Int = 0,
    val totalFocusSeconds: Int = 0,
    val remainingPausedSeconds: Int = 300,
    val lastSessionDuration: String = "",
    val lastSessionTimestamp:String = "",
    val snapshotBeforeAbandon: FocusViewState? = null
) {
    /**
     * Computed property to calculate the progress for the Canvas Dial.
     * Logic: (Current / Total).
     * Handles division by zero by returning 1f (Full circle).
     */
    val progress: Float
        get() = if (totalFocusSeconds > 0) {
            (totalFocusSeconds - remainingFocusSeconds).toFloat() / totalFocusSeconds.toFloat()
        } else {
            0f
        }
}

/**
 * FocusViewModel acts as the brain for Zenith's Focus Screen.
 */
class FocusViewModel(application: Application): AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(FocusViewState())
    val uiState: StateFlow<FocusViewState> = _uiState.asStateFlow()

    private var focusTimerJob: Job? = null
    private var pauseTimerJob: Job? = null
    private var abandonResetJob: Job? = null

    /**
     * Updates the user's focus intent (Zone 1).
     */
    fun updateMission(text: String) {
        _uiState.update { it.copy(missionText = text) }
    }

    /**
     * Sets the target duration for the Pomodoro/Sprint (Zone 3).
     * This also resets the timers so the UI reflects the new duration immediately.
     */
    fun setDuration(minutes: Int) {
        val totalSeconds = minutes * 60
        _uiState.update {
            it.copy(
                selectedDurationMinutes = minutes,
                remainingFocusSeconds = totalSeconds,
                totalFocusSeconds = totalSeconds
            )
        }
    }

    /**
     * Transitions from IDLE to RUNNING.
     */
    fun startSession() {

        val mission = _uiState.value.missionText
        val minutes = _uiState.value.selectedDurationMinutes
        if (mission.isBlank()) return

        val totalSeconds = minutes * 60
        _uiState.update {
            it.copy(
                sessionState = SessionState.RUNNING,
                totalFocusSeconds = totalSeconds,
                remainingFocusSeconds = totalSeconds,
                remainingPausedSeconds = 300
            )
        }

        val intent = Intent(getApplication(), FocusService::class.java).apply {
            putExtra("MISSION_NAME",mission)
            putExtra("PLANNED_MINUTES",minutes)
        }

        getApplication<Application>().startForegroundService(intent)
        startFocusTimer()
    }

    private fun startFocusTimer() {
        focusTimerJob?.cancel()
        focusTimerJob = viewModelScope.launch {
            while (_uiState.value.remainingFocusSeconds > 0) {
                delay(1000)
                _uiState.update {
                    it.copy(
                        remainingFocusSeconds = it.remainingFocusSeconds - 1
                    )
                }
            }
            finishSession()
        }
    }

    /**
     * Transitions from RUNNING to PAUSED.
     * Starts the 5-minute Bio-Break countdown.
     */
    fun pausedSession() {
        focusTimerJob?.cancel()
        _uiState.update { it.copy(sessionState = SessionState.PAUSED) }

        pauseTimerJob?.cancel()
        pauseTimerJob = viewModelScope.launch {
            while (_uiState.value.remainingPausedSeconds > 0) {
                delay(1000)
                _uiState.update { it.copy(remainingPausedSeconds = it.remainingPausedSeconds - 1) }
            }
            // Strict rule: Bio-break over, resume automatically!
            resumeSession()
        }

    }

    /**
     * Transitions from PAUSED back to RUNNING.
     */
    fun resumeSession() {
        pauseTimerJob?.cancel()
        _uiState.update {
            it.copy(
                sessionState = SessionState.RUNNING,
                remainingPausedSeconds = 300
            )
        }
        startFocusTimer()
    }

    /**
     * Force resets the engine back to IDLE.
     */
    fun abandonSession() {
        focusTimerJob?.cancel()
        pauseTimerJob?.cancel()

        stopFocusService()

        val currentState = _uiState.value
        _uiState.update {
            it.copy(
                sessionState = SessionState.ABANDONED,
                snapshotBeforeAbandon = currentState
            )
        }
        // This ensures the app resets even if the user navigates away.
        abandonResetJob?.cancel()
        abandonResetJob = viewModelScope.launch {
            delay(4000) // Give them 4 seconds to see the toast/undo
            if (_uiState.value.sessionState == SessionState.ABANDONED) {
                resetToDefaults()
            }
        }
        // TODO: Save incomplete session to Room
        // TODO: DistarctionEvent and Session data logic in Room

    }

    fun undoAbandon() {
        abandonResetJob?.cancel()
        val snapshot = _uiState.value.snapshotBeforeAbandon ?: return

        _uiState.update {
            it.copy(
                missionText = snapshot.missionText,
                selectedDurationMinutes = snapshot.selectedDurationMinutes,
                remainingFocusSeconds = snapshot.remainingFocusSeconds,
                totalFocusSeconds = snapshot.totalFocusSeconds,

                // Restore the state!
                // If it was Paused, go back to Pause. Otherwise, go back to Running.
                sessionState = if (snapshot.sessionState == SessionState.PAUSED)
                    SessionState.PAUSED else SessionState.RUNNING,

                snapshotBeforeAbandon = null // Clear the snapshot memory
            )
        }

        if (_uiState.value.sessionState == SessionState.RUNNING) {
            startFocusTimer()

            // Also restart the background service!
            val intent = Intent(getApplication(), FocusService::class.java).apply {
                putExtra("MISSION_NAME", _uiState.value.missionText)
                putExtra("PLANNED_MINUTES", _uiState.value.selectedDurationMinutes)
            }
            getApplication<Application>().startForegroundService(intent)
        } else if (_uiState.value.sessionState == SessionState.PAUSED) {
            // If it was paused, we need to restart the Bio-Break countdown!
            pausedSession()
        }
    }

    /**
     * Naturally completes the session.
     */
    fun finishSession() {
        focusTimerJob?.cancel()
        pauseTimerJob?.cancel()

        stopFocusService()
        _uiState.update {
            it.copy(
                sessionState = SessionState.FINISHED
            )
        }
        // TODO: Save complete session to Room

    }

    internal fun resetToDefaults() {
        _uiState.update {
            it.copy(
                missionText = "",
                selectedDurationMinutes = 25,
                remainingFocusSeconds = 25 * 60,
                totalFocusSeconds = 25 * 60,
                sessionState = SessionState.IDLE
            )
        }
    }

    private fun stopFocusService() {
        val intent = Intent(getApplication(), FocusService::class.java)
        getApplication<Application>().stopService(intent)
    }

    /**
     * Helper to route the UI click to the correct lifecycle method.
     */
    fun toggleFocusSession() {
        when (_uiState.value.sessionState) {
            SessionState.IDLE -> startSession()
            SessionState.RUNNING -> pausedSession()
            SessionState.PAUSED -> resumeSession()
            SessionState.FINISHED, SessionState.ABANDONED -> {
                resetToDefaults()
            }
        }
    }
}