package com.abdapps.scriptmine.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdapps.scriptmine.data.repository.HybridScriptRepository
import com.abdapps.scriptmine.sync.SyncProgress
import com.abdapps.scriptmine.sync.SyncState
import com.abdapps.scriptmine.utils.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing sync status across the application
 */
@HiltViewModel
class SyncStatusViewModel @Inject constructor(
    private val hybridRepository: HybridScriptRepository
) : ViewModel() {
    
    // Sync state from SyncManager
    val syncState: StateFlow<SyncState> = hybridRepository.getSyncStatus()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SyncState.IDLE
        )
    
    // Sync progress from SyncManager
    val syncProgress: StateFlow<SyncProgress> = hybridRepository.getSyncProgress()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SyncProgress()
        )
    
    // Network state from NetworkMonitor
    val networkState: StateFlow<NetworkState> = hybridRepository.getNetworkState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NetworkState()
        )
    
    // Pending sync count
    private val _pendingSyncCount = MutableStateFlow(0)
    val pendingSyncCount: StateFlow<Int> = _pendingSyncCount.asStateFlow()
    
    // Sync result message
    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()
    
    init {
        // Update pending sync count periodically
        viewModelScope.launch {
            while (true) {
                updatePendingSyncCount()
                kotlinx.coroutines.delay(30000) // Update every 30 seconds
            }
        }
    }
    
    /**
     * Triggers manual synchronization
     */
    fun triggerManualSync() {
        viewModelScope.launch {
            try {
                val result = hybridRepository.syncWithFirebase()
                
                if (result.isSuccess) {
                    _syncMessage.value = result.getOrNull()
                } else {
                    _syncMessage.value = "Sync failed: ${result.exceptionOrNull()?.message}"
                }
                
                // Clear message after 5 seconds
                kotlinx.coroutines.delay(5000)
                _syncMessage.value = null
                
            } catch (e: Exception) {
                _syncMessage.value = "Sync error: ${e.message}"
                kotlinx.coroutines.delay(5000)
                _syncMessage.value = null
            }
        }
    }
    
    /**
     * Forces sync for a specific script
     */
    fun forceSyncScript(scriptId: Long) {
        viewModelScope.launch {
            try {
                val result = hybridRepository.forceSyncScript(scriptId)
                
                if (result.isSuccess) {
                    _syncMessage.value = "Script synced successfully"
                } else {
                    _syncMessage.value = "Failed to sync script"
                }
                
                kotlinx.coroutines.delay(3000)
                _syncMessage.value = null
                
            } catch (e: Exception) {
                _syncMessage.value = "Sync error: ${e.message}"
                kotlinx.coroutines.delay(3000)
                _syncMessage.value = null
            }
        }
    }
    
    /**
     * Updates the pending sync count
     */
    private suspend fun updatePendingSyncCount() {
        try {
            val count = hybridRepository.getPendingSyncCount()
            _pendingSyncCount.value = count
        } catch (e: Exception) {
            // Ignore errors, keep previous count
        }
    }
    
    /**
     * Clears the sync message
     */
    fun clearSyncMessage() {
        _syncMessage.value = null
    }
    
    /**
     * Gets repository statistics for debugging
     */
    suspend fun getRepositoryStats() = hybridRepository.getRepositoryStats()
}