package com.example.zenith.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SessionEventBus {
    private val _events = MutableSharedFlow<SessionEvent>(
        replay = 1,
        extraBufferCapacity = 1
    )
    val events = _events.asSharedFlow()

    suspend fun emit(event: SessionEvent) {
        _events.emit(event)
    }

    // Prevents the same event from firing twice on screen rotation
    @OptIn(ExperimentalCoroutinesApi::class)
    fun clearLastEvent() {
        _events.resetReplayCache()
    }

    sealed class SessionEvent {
        object PauseForCall : SessionEvent()
        object ResumeAfterCall : SessionEvent()

        object UserManualPause : SessionEvent()

        object UserManualResume : SessionEvent()
    }
}