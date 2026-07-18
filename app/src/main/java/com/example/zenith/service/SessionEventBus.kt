package com.example.zenith.service

import kotlinx.coroutines.flow.MutableSharedFlow

object SessionEventBus {
    val events = MutableSharedFlow<SessionEvent> (extraBufferCapacity = 1)

    sealed class SessionEvent {
        object PauseForCall : SessionEvent()
        object ResumeAfterCall : SessionEvent()
    }
}