package com.example.safenova.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "safenova_settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val SHAKE_SOS_ENABLED = booleanPreferencesKey("shake_sos_enabled")
        val SHAKE_SENSITIVITY = floatPreferencesKey("shake_sensitivity")
        val POWER_BUTTON_SOS_ENABLED = booleanPreferencesKey("power_button_sos_enabled")
        val STEALTH_MODE_ENABLED = booleanPreferencesKey("stealth_mode_enabled")
        val AUTO_RECORD_AUDIO = booleanPreferencesKey("auto_record_audio")
        val LOCATION_SHARING_ENABLED = booleanPreferencesKey("location_sharing_enabled")
    }

    val shakeSosEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SHAKE_SOS_ENABLED] ?: true
    }

    val shakeSensitivity: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[SHAKE_SENSITIVITY] ?: 0.7f
    }

    val powerButtonEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[POWER_BUTTON_SOS_ENABLED] ?: true
    }

    val stealthModeEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[STEALTH_MODE_ENABLED] ?: false
    }

    val autoRecordAudio: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[AUTO_RECORD_AUDIO] ?: true
    }

    val locationSharingEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[LOCATION_SHARING_ENABLED] ?: true
    }

    suspend fun saveShakeSosEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[SHAKE_SOS_ENABLED] = enabled }
    }

    suspend fun saveShakeSensitivity(value: Float) {
        context.dataStore.edit { prefs -> prefs[SHAKE_SENSITIVITY] = value }
    }

    suspend fun savePowerButtonEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[POWER_BUTTON_SOS_ENABLED] = enabled }
    }

    suspend fun saveStealthModeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[STEALTH_MODE_ENABLED] = enabled }
    }

    suspend fun saveAutoRecordAudio(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[AUTO_RECORD_AUDIO] = enabled }
    }

    suspend fun saveLocationSharingEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[LOCATION_SHARING_ENABLED] = enabled }
    }
}
