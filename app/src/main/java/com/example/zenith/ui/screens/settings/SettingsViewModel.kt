package com.example.zenith.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenith.data.DistractionEventDao
import com.example.zenith.data.FocusSessionDao
import com.example.zenith.data.SettingsRepository
import com.example.zenith.data.UserPreferences
import com.example.zenith.data.WhitelistedAppDao
import com.example.zenith.service.FocusService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val repository: SettingsRepository,
    private val focusSessionDao: FocusSessionDao,
    distractionEventDao: DistractionEventDao,
    private val whitelistedAppDao: WhitelistedAppDao
) : ViewModel() {

    val settingsState: StateFlow<UserPreferences?> = repository.userPreferenceFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val totalSessions: StateFlow<Int> = focusSessionDao.getTotalSessionCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalPickups: StateFlow<Int> = distractionEventDao.getTotalPickupCounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalAppSwitches: StateFlow<Int> = distractionEventDao.getTotalAppSwitchesCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun toggleCallShield(enabled: Boolean) {
        viewModelScope.launch { repository.updateCallShield(enabled) }
    }

    fun toggleHaptics(enabled: Boolean) {
        viewModelScope.launch { repository.updateHaptics(enabled) }
    }

    fun setStrictness(level: Int) {
        viewModelScope.launch { repository.updateStrictness(level) }
    }

    fun toggleAutoDnd(enabled: Boolean) {
        viewModelScope.launch { repository.updateAutoDnd(enabled) }
    }

    fun setMercyBuffer(buffer: Int) {
        viewModelScope.launch { repository.updateMercyBuffer(buffer) }
    }

    fun setRoastIntensity(intensity: Int) {
        viewModelScope.launch { repository.updateRoastIntensity(intensity) }
    }

    fun setThrottling(seconds: Int) {
        viewModelScope.launch { repository.updateThrottling(seconds) }
    }

    fun setVibrationStrength(strength: Int) {
        viewModelScope.launch { repository.updateVibrationStrength(strength) }
    }

    fun setVibrationPattern(pattern: Int) {
        viewModelScope.launch { repository.updateVibrationPattern(pattern) }
    }

    fun clearMissionHistory() {
        viewModelScope.launch {
            try {
                // Stop service if running to release DB locks
                val intent = Intent(context, FocusService::class.java).apply {
                    action = FocusService.ACTION_STOP
                }
                context.startService(intent)
                delay(500.milliseconds)
                focusSessionDao.deleteAllSessions()
                Toast.makeText(context, "MISSION HISTORY PURGED", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "History purge failed", e)
            }
        }
    }

    fun resetEngineConfig() {
        viewModelScope.launch {
            repository.resetToDefaults()
        }
    }

    fun factoryReset() {
        viewModelScope.launch {
            try {
                // 1. Kill the Engine immediately to free all DB locks
                val intent = Intent(context, FocusService::class.java).apply {
                    action = FocusService.ACTION_STOP
                    putExtra(FocusService.EXTRA_IS_FINISHED, false)
                }
                context.startService(intent)
                
                // Longer delay to ensure Service and its Coroutines are totally dead
                delay(1000.milliseconds)

                // 2. Clear Settings (DataStore)
                repository.resetToDefaults()

                // 3. Clear all DB tables manually via DAOs
                // This is safer and more reliable than clearAllTables()
                focusSessionDao.deleteAllSessions()
                whitelistedAppDao.deleteAll()
                
                Toast.makeText(context, "SYSTEM RESET SUCCESSFUL", Toast.LENGTH_LONG).show()
                
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Factory reset encountered an error", e)
                Toast.makeText(context, "Reset partially failed. Please restart the app.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}