package com.abdapps.scriptmine.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdapps.scriptmine.auth.SessionManager
import com.abdapps.scriptmine.auth.UserPreferences
import com.abdapps.scriptmine.performance.CacheManager
import com.abdapps.scriptmine.sync.SyncScheduler
import com.abdapps.scriptmine.sync.SyncTriggers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for managing app settings and user preferences
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val syncScheduler: SyncScheduler,
    private val syncTriggers: SyncTriggers,
    private val cacheManager: CacheManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    /**
     * Loads current settings from SessionManager
     */
    private fun loadSettings() {
        viewModelScope.launch {
            sessionManager.userPreferences.collect { preferences ->
                _uiState.value = _uiState.value.copy(
                    autoSyncEnabled = preferences.autoSyncEnabled,
                    syncOnWifiOnly = preferences.syncOnWifiOnly,
                    notificationsEnabled = preferences.notificationsEnabled,
                    syncFrequencyMinutes = preferences.syncFrequencyMinutes,
                    lastSyncTime = formatLastSyncTime(sessionManager.getLastSyncTime())
                )
            }
        }
    }
    
    /**
     * Sets auto sync enabled state
     */
    fun setAutoSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val currentPrefs = sessionManager.userPreferences.value
            sessionManager.updateUserPreferences(
                currentPrefs.copy(autoSyncEnabled = enabled)
            )
            
            if (enabled) {
                syncScheduler.schedulePeriodicSync()
            } else {
                syncScheduler.cancelPeriodicSync()
            }
            
            _uiState.value = _uiState.value.copy(autoSyncEnabled = enabled)
        }
    }
    
    /**
     * Sets sync on WiFi only state
     */
    fun setSyncOnWifiOnly(wifiOnly: Boolean) {
        viewModelScope.launch {
            val currentPrefs = sessionManager.userPreferences.value
            sessionManager.updateUserPreferences(
                currentPrefs.copy(syncOnWifiOnly = wifiOnly)
            )
            
            _uiState.value = _uiState.value.copy(syncOnWifiOnly = wifiOnly)
        }
    }
    
    /**
     * Sets sync frequency in minutes
     */
    fun setSyncFrequency(minutes: Int) {
        viewModelScope.launch {
            val currentPrefs = sessionManager.userPreferences.value
            sessionManager.updateUserPreferences(
                currentPrefs.copy(syncFrequencyMinutes = minutes)
            )
            
            // Reschedule sync with new frequency
            if (currentPrefs.autoSyncEnabled) {
                syncScheduler.cancelPeriodicSync()
                syncScheduler.schedulePeriodicSync()
            }
            
            _uiState.value = _uiState.value.copy(syncFrequencyMinutes = minutes)
        }
    }
    
    /**
     * Sets notifications enabled state
     */
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val currentPrefs = sessionManager.userPreferences.value
            sessionManager.updateUserPreferences(
                currentPrefs.copy(notificationsEnabled = enabled)
            )
            
            _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
        }
    }
    
    /**
     * Triggers manual sync
     */
    fun triggerManualSync() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, syncError = null)
            
            val result = syncTriggers.triggerManualSync()
            
            if (result.isSuccess) {
                // Update last sync time
                sessionManager.recordLastSync()
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    lastSyncTime = formatLastSyncTime(Date())
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    syncError = "Sync failed: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }
    
    /**
     * Exports all user data
     */
    fun exportData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, exportError = null)
            
            try {
                // TODO: Implement data export functionality
                // This would export scripts to a JSON file
                
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportSuccess = true
                )
                
                // Clear success flag after delay
                kotlinx.coroutines.delay(2000)
                _uiState.value = _uiState.value.copy(exportSuccess = false)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportError = "Export failed: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Clears app cache
     */
    fun clearCache() {
        viewModelScope.launch {
            try {
                cacheManager.clearAll()
                
                _uiState.value = _uiState.value.copy(
                    cacheCleared = true
                )
                
                // Clear flag after delay
                kotlinx.coroutines.delay(2000)
                _uiState.value = _uiState.value.copy(cacheCleared = false)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cacheError = "Failed to clear cache: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Resets sync status
     */
    fun resetSync() {
        viewModelScope.launch {
            try {
                // Cancel all sync work
                syncScheduler.cancelAllSyncWork()
                
                // Reschedule if auto sync is enabled
                val currentPrefs = sessionManager.userPreferences.value
                if (currentPrefs.autoSyncEnabled) {
                    syncScheduler.schedulePeriodicSync()
                }
                
                // Trigger manual sync
                syncTriggers.triggerManualSync()
                
                _uiState.value = _uiState.value.copy(
                    syncReset = true
                )
                
                // Clear flag after delay
                kotlinx.coroutines.delay(2000)
                _uiState.value = _uiState.value.copy(syncReset = false)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    syncError = "Failed to reset sync: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Gets cache statistics
     */
    fun getCacheStats(): CacheManager.CacheStats {
        return cacheManager.getCacheStats()
    }
    
    /**
     * Formats last sync time for display
     */
    private fun formatLastSyncTime(date: Date?): String? {
        if (date == null) return null
        
        val now = Date()
        val diff = now.time - date.time
        
        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000} minutes ago"
            diff < 86400000 -> "${diff / 3600000} hours ago"
            else -> SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(date)
        }
    }
    
    /**
     * Clears error messages
     */
    fun clearErrors() {
        _uiState.value = _uiState.value.copy(
            syncError = null,
            exportError = null,
            cacheError = null
        )
    }
}

/**
 * UI state for settings screen
 */
data class SettingsUiState(
    val autoSyncEnabled: Boolean = true,
    val syncOnWifiOnly: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val syncFrequencyMinutes: Int = 15,
    val lastSyncTime: String? = null,
    val isSyncing: Boolean = false,
    val syncError: String? = null,
    val isExporting: Boolean = false,
    val exportSuccess: Boolean = false,
    val exportError: String? = null,
    val cacheCleared: Boolean = false,
    val cacheError: String? = null,
    val syncReset: Boolean = false
)