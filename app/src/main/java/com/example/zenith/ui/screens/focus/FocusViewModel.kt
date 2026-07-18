package com.example.zenith.ui.screens.focus

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.zenith.data.AppDatabase
import com.example.zenith.service.FocusService
import com.example.zenith.service.SessionEventBus
import com.example.zenith.ui.common.UiStateMachine
import com.example.zenith.ui.common.asUiStateMachine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FocusViewModel(
    application: Application,
    savedState: SavedStateHandle
) : AndroidViewModel(application) {
    private val db by lazy { AppDatabase.getDatabase(application) }
    private val sessionDao by lazy { db.focusSessionDao() }

    // State Machine
    private val uiStateMachine: UiStateMachine<FocusViewState> =
        savedState.asUiStateMachine(FocusViewState())

    // UI observes this property
    val uiState: StateFlow<FocusViewState> by uiStateMachine

    private var focusTimerJob: Job? = null
    private var pauseTimerJob: Job? = null
    private var abandonResetJob: Job? = null

    init {
        viewModelScope.launch {
            SessionEventBus.events.collect { event ->
                when(event) {
                    SessionEventBus.SessionEvent.PauseForCall -> handleCallPause()
                    SessionEventBus.SessionEvent.ResumeAfterCall -> handleCallResume()
                }

            }
        }
        if (!uiStateMachine.isStateRestored){
            syncWithDatabase()
        } else {
            if (uiState.value.sessionState == SessionState.RUNNING) {
                syncWithDatabase()
            }
        }
    }

    private fun handleCallPause() {
        if (uiState.value.sessionState == SessionState.IDLE) return

        focusTimerJob?.cancel()
        pauseTimerJob?.cancel()

        uiStateMachine.update { copy(isPausedByCall = true) }
    }

    private fun handleCallResume() {
        if (uiState.value.isPausedByCall) {
            uiStateMachine.update { copy(isPausedByCall = false) }
            when (uiState.value.sessionState) {
                SessionState.RUNNING -> {
                    startFocusTimer()
                }
                SessionState.PAUSED -> {
                    pausedSession()
                }
                else -> {}
            }
        }
    }

    private fun syncWithDatabase() {
        viewModelScope.launch {
            val lastSession = sessionDao.getLatestSession().first() ?: return@launch
            val now = System.currentTimeMillis()
            val elapsedSec = ((now - lastSession.timestamp) / 1000).toInt()
            val plannedSec = lastSession.plannedDurationMinutes * 60

            if (!lastSession.isCompleted && elapsedSec < plannedSec) {
                uiStateMachine.update {
                    copy(
                        sessionState = SessionState.RUNNING,
                        missionText = lastSession.missionName,
                        selectedDurationMinutes = lastSession.plannedDurationMinutes,
                        totalFocusSeconds = plannedSec,
                        remainingFocusSeconds = plannedSec - elapsedSec
                    )
                }
                startFocusTimer()
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
                remainingPausedSeconds = 300
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
            // FIXED: Use uiState.value inside loop
            while (uiState.value.remainingFocusSeconds > 0) {
                delay(1000)
                uiStateMachine.update {
                    copy(remainingFocusSeconds = remainingFocusSeconds - 1)
                }
            }
            finishSession()
        }
    }

    fun pausedSession() {
        focusTimerJob?.cancel()
        uiStateMachine.update { copy(sessionState = SessionState.PAUSED) }

        pauseTimerJob?.cancel()
        pauseTimerJob = viewModelScope.launch {
            while (uiState.value.remainingPausedSeconds > 0) {
                delay(1000)
                uiStateMachine.update { copy(remainingPausedSeconds = remainingPausedSeconds - 1) }
            }
            resumeSession()
        }
    }

    fun resumeSession() {
        pauseTimerJob?.cancel()
        uiStateMachine.update {
            copy(
                sessionState = SessionState.RUNNING,
                remainingPausedSeconds = 300
            )
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
            pausedSession()
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
            SessionState.RUNNING -> pausedSession()
            SessionState.PAUSED -> resumeSession()
            SessionState.FINISHED, SessionState.ABANDONED -> resetToDefaults()
        }
    }
}