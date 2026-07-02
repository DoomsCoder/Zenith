package com.example.zenith.ui.common

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A state machine that automatically persists UI state to [SavedStateHandle].
 */
interface UiStateMachine <UI_STATE : Parcelable> : ReadOnlyProperty<Any?, StateFlow<UI_STATE>> {

    val isStateRestored: Boolean

    fun update(block: UI_STATE.() -> UI_STATE)
}

class UiStateMachineImpl<UI_STATE : Parcelable>(
    private val savedStateHandle: SavedStateHandle,
    private val key: String,
    fallback: UI_STATE
) : UiStateMachine<UI_STATE> {

    override val isStateRestored: Boolean
    private val uiState: MutableStateFlow<UI_STATE>

    init {
        val restoredState: UI_STATE? = savedStateHandle[key]
        val currentUiState = restoredState ?: fallback

        isStateRestored = restoredState != null
        uiState = MutableStateFlow(currentUiState)
    }

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>
    ): StateFlow<UI_STATE> = uiState

    override fun update(block: UI_STATE.() -> UI_STATE) {
        val newState = uiState.value.block()
        uiState.value = newState
        savedStateHandle[key] = newState
    }
}

/**
 * Builder extension to create a [UiStateMachine] from [SavedStateHandle].
 */
fun <UI_STATE : Parcelable> SavedStateHandle.asUiStateMachine(
    fallback: UI_STATE,
    key: String = "ui_state",
): UiStateMachine<UI_STATE> = UiStateMachineImpl(
    savedStateHandle = this,
    key = key,
    fallback = fallback
)