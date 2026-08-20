package com.example.zenith.ui.screens.focus

import android.app.Application
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.zenith.service.FocusService
import com.example.zenith.service.SessionEventBus
import com.example.zenith.ui.common.UiStateMachine
import com.example.zenith.ui.common.asUiStateMachine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FocusViewModel(
    application: Application,
    savedState: SavedStateHandle
) : AndroidViewModel(application) {

    // State Machine
    private val uiStateMachine: UiStateMachine<FocusViewState> =
        savedState.asUiStateMachine(FocusViewState())

    // UI observes this property
    val uiState: StateFlow<FocusViewState> by uiStateMachine

    private var focusTimerJob: Job? = null
    private var pauseTimerJob: Job? = null
    private var abandonResetJob: Job? = null
    private var surgeJob: Job? = null

    init {
        viewModelScope.launch {
            SessionEventBus.events.collect { event ->
                when(event) {
                    SessionEventBus.SessionEvent.PauseForCall -> handleCallPause()
                    SessionEventBus.SessionEvent.ResumeAfterCall -> handleCallResume()
                    SessionEventBus.SessionEvent.PauseProgress -> uiStateMachine.update { copy(isProgressFrozen = true) }
                    SessionEventBus.SessionEvent.ResumeProgress -> uiStateMachine.update { copy(isProgressFrozen = false) }
                    is SessionEventBus.SessionEvent.PenaltyApplied -> applyTimeDebt(event.seconds)
                    SessionEventBus.SessionEvent.MissionExecuted -> handleMissionExecution()
                    else -> {}
                }
                SessionEventBus.clearLastEvent()
            }
        }
        if (uiStateMachine.isStateRestored) {
            if (uiState.value.sessionState == SessionState.RUNNING) {
                startFocusTimer()
            } else if (uiState.value.sessionState == SessionState.PAUSED) {
                startPauseTimer()
            }
        }
    }

    private fun applyTimeDebt(seconds: Int) {
        surgeJob?.cancel()
        surgeJob = viewModelScope.launch {
            uiStateMachine.update { 
                copy(
                    lastPenaltySeconds = seconds, 
                    isIntegrityCompromised = true,
                    showPenaltyFlash = true
                ) 
            }
            
            // Limit penalty to 2x original mission
            val maxAllowedSeconds = uiState.value.selectedDurationMinutes * 60 * 2
            
            // Incremental surge animation
            repeat(seconds) {
                if (uiState.value.remainingFocusSeconds < maxAllowedSeconds) {
                    uiStateMachine.update { copy(remainingFocusSeconds = remainingFocusSeconds + 1) }
                    delay(10) // Rapid tick up
                }
            }
            
            delay(3000)
            uiStateMachine.update { copy(isIntegrityCompromised = false, showPenaltyFlash = false) }
        }
    }

    private fun handleMissionExecution() {
        focusTimerJob?.cancel()
        pauseTimerJob?.cancel()
        uiStateMachine.update { copy(sessionState = SessionState.IDLE) }
        resetToDefaults()
        Toast.makeText(getApplication(), "MISSION FAILED: Integrity compromised for too long.", Toast.LENGTH_LONG).show()
    }

    private fun handleCallPause() {
        if (uiState.value.sessionState == SessionState.IDLE) return

        focusTimerJob?.cancel()
        pauseTimerJob?.cancel()

        uiStateMachine.update {
            copy(
                isPausedByCall = true,
                stateBeforeCall = sessionState
            )
        }
    }

    private fun handleCallResume() {
        if (uiState.value.isPausedByCall) {
            uiStateMachine.update {
                copy(
                    isPausedByCall = false,
                    stateBeforeCall = null
                )
            }
            when (uiState.value.sessionState) {
                SessionState.RUNNING -> {
                    startFocusTimer()
                }
                SessionState.PAUSED -> {
                    startPauseTimer()
                }
                else -> {}
            }
        }
    }

    fun updateMission(text: String) {
        uiStateMachine.update { copy(missionText = text) }
    }

    fun setDuration(minutes: Int) {
        val totalSeconds = minutes * 60
        uiStateMachine.update {
            copy(
                selectedDurationMinutes = minutes,
                remainingFocusSeconds = totalSeconds,
                totalFocusSeconds = totalSeconds
            )
        }
    }

    fun startSession() {
        val mission = uiState.value.missionText
        val minutes = uiState.value.selectedDurationMinutes
        if (mission.isBlank()) return

        val totalSeconds = minutes * 60
        uiStateMachine.update {
            copy(
                sessionState = SessionState.RUNNING,
                totalFocusSeconds = totalSeconds,
                remainingFocusSeconds = totalSeconds,
                isBreakAllowanceSet = false,
                totalBreakBankSeconds = 0,
                remainingBreakBankSeconds = 0,
                isIntegrityCompromised = false
            )
        }

        val intent = Intent(getApplication(), FocusService::class.java).apply {
            putExtra("MISSION_NAME", mission)
            putExtra("PLANNED_MINUTES", minutes)
        }
        getApplication<Application>().startForegroundService(intent)
        startFocusTimer()
    }

    private fun startFocusTimer() {
        focusTimerJob?.cancel()
        focusTimerJob = viewModelScope.launch {
            while (uiState.value.remainingFocusSeconds > 0) {
                delay(1000)
                if (!uiState.value.isProgressFrozen && !uiState.value.isPausedByCall) {
                    uiStateMachine.update {
                        copy(remainingFocusSeconds = remainingFocusSeconds - 1)
                    }
                }
            }
            finishSession()
        }
    }

    fun setBreakAllowance(minutes: Int) {
        val seconds = minutes * 60
        uiStateMachine.update {
            copy(
                isBreakAllowanceSet = true,
                totalBreakBankSeconds = seconds,
                remainingBreakBankSeconds = seconds
            )
        }
        if (minutes > 0) {
            pausedSession()
        } else {
            resumeSession()
        }
    }

    fun pausedSession() {
        if (uiState.value.isBreakAllowanceSet && uiState.value.remainingBreakBankSeconds <= 0) return

        focusTimerJob?.cancel()
        uiStateMachine.update { copy(sessionState = SessionState.PAUSED) }

        viewModelScope.launch {
            SessionEventBus.emit(SessionEventBus.SessionEvent.UserManualPause)
        }
        startPauseTimer()
    }

    private fun startPauseTimer() {
        pauseTimerJob?.cancel()
        pauseTimerJob = viewModelScope.launch {
            while (uiState.value.remainingBreakBankSeconds > 0) {
                delay(1000)
                uiStateMachine.update { copy(remainingBreakBankSeconds = remainingBreakBankSeconds - 1) }
            }
            resumeSession()
        }
    }

    fun resumeSession() {
        pauseTimerJob?.cancel()
        uiStateMachine.update {
            copy(sessionState = SessionState.RUNNING)
        }

        viewModelScope.launch {
            SessionEventBus.emit(SessionEventBus.SessionEvent.UserManualResume)
        }
        startFocusTimer()
    }

    fun abandonSession() {
        focusTimerJob?.cancel()
        pauseTimerJob?.cancel()
        sendServiceCommand(isFinished = false)

        val currentState = uiState.value
        uiStateMachine.update {
            copy(
                sessionState = SessionState.ABANDONED,
                snapshotBeforeAbandon = currentState
            )
        }

        abandonResetJob?.cancel()
        abandonResetJob = viewModelScope.launch {
            delay(4000)
            if (uiState.value.sessionState == SessionState.ABANDONED) {
                resetToDefaults()
            }
        }
    }

    fun undoAbandon() {
        abandonResetJob?.cancel()
        val snapshot = uiState.value.snapshotBeforeAbandon ?: return

        uiStateMachine.update {
            copy(
                missionText = snapshot.missionText,
                selectedDurationMinutes = snapshot.selectedDurationMinutes,
                remainingFocusSeconds = snapshot.remainingFocusSeconds,
                totalFocusSeconds = snapshot.totalFocusSeconds,
                isBreakAllowanceSet = snapshot.isBreakAllowanceSet,
                totalBreakBankSeconds = snapshot.totalBreakBankSeconds,
                remainingBreakBankSeconds = snapshot.remainingBreakBankSeconds,
                sessionState = if (snapshot.sessionState == SessionState.PAUSED)
                    SessionState.PAUSED else SessionState.RUNNING,
                snapshotBeforeAbandon = null
            )
        }

        if (uiState.value.sessionState == SessionState.RUNNING) {
            startFocusTimer()
            val intent = Intent(getApplication(), FocusService::class.java).apply {
                putExtra("MISSION_NAME", uiState.value.missionText)
                putExtra("PLANNED_MINUTES", uiState.value.selectedDurationMinutes)
            }
            getApplication<Application>().startForegroundService(intent)
        } else if (uiState.value.sessionState == SessionState.PAUSED) {
            startPauseTimer()
        }
    }

    fun finishSession() {
        focusTimerJob?.cancel()
        pauseTimerJob?.cancel()
        sendServiceCommand(isFinished = true)
        uiStateMachine.update { copy(sessionState = SessionState.FINISHED) }
    }

    internal fun resetToDefaults() {
        uiStateMachine.update {
            FocusViewState(
                missionText = "",
                selectedDurationMinutes = 25,
                remainingFocusSeconds = 25 * 60,
                totalFocusSeconds = 25 * 60,
                sessionState = SessionState.IDLE
            )
        }
    }

    private fun sendServiceCommand(isFinished: Boolean) {
        val intent = Intent(getApplication(), FocusService::class.java).apply {
            action = FocusService.ACTION_STOP
            putExtra(FocusService.EXTRA_IS_FINISHED, isFinished)
        }
        getApplication<Application>().startForegroundService(intent)
    }

    fun toggleFocusSession() {
        when (uiState.value.sessionState) {
            SessionState.IDLE -> startSession()
            SessionState.RUNNING -> {
                if (!uiState.value.isBreakAllowanceSet) {
                    // Trigger Break Allowance Sheet in UI
                    uiStateMachine.update { copy(sessionState = SessionState.PAUSED) }
                } else {
                    pausedSession()
                }
            }
            SessionState.PAUSED -> resumeSession()
            SessionState.FINISHED, SessionState.ABANDONED -> resetToDefaults()
        }
    }
}
