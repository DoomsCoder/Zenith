package com.example.zenith.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "zenith_settings")

class SettingsRepository(private val context: Context) {

    private val KEY_STRICTNESS = intPreferencesKey("strictness_level")
    private val KEY_CALL_SHIELD = booleanPreferencesKey("call_shield")
    private val KEY_MERCY_BUFFER = intPreferencesKey("mercy_buffer")

    private val KEY_ROAST_INTENSITY = intPreferencesKey("roast_intensity")
    private val KEY_THROTTLING = intPreferencesKey("notification_throttling")
    private val KEY_HAPTICS = booleanPreferencesKey("haptics_enabled")
    private val KEY_VIBRATION_STRENGTH = intPreferencesKey("vibration_strength")

    private val KEY_SHOW_TRENDS = booleanPreferencesKey("show_focus_trends")
    private val KEY_AUTO_DND = booleanPreferencesKey("auto_dnd_enabled")

    val userPreferenceFlow: Flow<UserPreferences> = context.dataStore.data.map { pref ->
        UserPreferences(
            strictnessLevel = pref[KEY_STRICTNESS] ?: 1, // 0:Low, 1:Standard, 2:Zenith
            isCallShieldEnabled = pref[KEY_CALL_SHIELD] ?: true,
            mercyBuffer = pref[KEY_MERCY_BUFFER] ?: 0,
            isAutoDndEnabled = pref[KEY_AUTO_DND] ?: false,
            roastIntensity = pref[KEY_ROAST_INTENSITY] ?: 1,
            notificationThrottlingSeconds = pref[KEY_THROTTLING] ?: 30,
            isHapticsEnabled = pref[KEY_HAPTICS] ?: true,
            vibrationStrength = pref[KEY_VIBRATION_STRENGTH] ?: 100,
            showFocusTrends = pref[KEY_SHOW_TRENDS] ?: true
        )
    }

    suspend fun updateCallShield(enabled: Boolean) {
        context.dataStore.edit { it[KEY_CALL_SHIELD] = enabled }
    }

    suspend fun updateStrictness(level: Int) {
        context.dataStore.edit { it[KEY_STRICTNESS] = level }
    }

    suspend fun updateHaptics(enabled: Boolean) {
        context.dataStore.edit { it[KEY_HAPTICS] = enabled }
    }

    suspend fun updateAutoDnd(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AUTO_DND] = enabled }
    }

    suspend fun updateMercyBuffer(buffer: Int) {
        context.dataStore.edit { it[KEY_MERCY_BUFFER] = buffer }
    }
}

data class UserPreferences(
    val strictnessLevel: Int,
    val isCallShieldEnabled: Boolean,
    val mercyBuffer: Int,
    val roastIntensity: Int,
    val isAutoDndEnabled: Boolean,
    val notificationThrottlingSeconds: Int,
    val isHapticsEnabled: Boolean,
    val vibrationStrength: Int,
    val showFocusTrends: Boolean
)