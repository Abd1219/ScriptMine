package com.abdapps.scriptmine.sync

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.abdapps.scriptmine.data.repository.HybridScriptRepository
import com.abdapps.scriptmine.utils.NetworkMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages immediate sync triggers based on various events and conditions
 * Provides intelligent sync triggering to optimize user experience and data consistency
 */
@Singleton
class SyncTriggers @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncScheduler: SyncScheduler,
    private val hybridRepository: HybridScriptRepository,
    private val networkMonitor: NetworkMonitor
) : DefaultLifecycleObserver {
    
    companion object {
        private const val TAG = "SyncTriggers"
        private const val PENDING_THRESHOLD = 5 // Trigger sync when 5+ items pending
        private const val PENDING_CHECK_INTERVAL_MS = 30000L // Check every 30 seconds
        private const val NETWORK_STABILIZATION_DELAY_MS = 3000L
    }
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var pendingMonitorJob: Job? = null
    private var networkTriggerJob: Job? = null
    private var pullToRefreshEnabled = true
    
    /**
     * Initializes sync triggers
     */
    fun initialize() {
        Log.d(TAG, "Initializing SyncTriggers")
        
        startPendingItemsMonitor()
        startNetworkTriggers()
        
        Log.d(TAG, "SyncTriggers initialized")
    }
    
    /**
     * Triggers immediate full sync (user-initiated)
     */
    suspend fun triggerManualSync(): Result<String> {
        return try {
            Log.d(TAG, "Manual sync triggered by user")
            
            if (!networkMonitor.isCurrentlyOnline()) {
                return Result.failure(Exception("No network connection available"))
            }
            
            syncScheduler.triggerImmediateSync(forceSync = true)
            
            // Wait a moment and check if sync started
            delay(1000)
            
            Result.success("Sync started successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to trigger manual sync", e)
            Result.failure(e)
        }
    }
    
    /**
     * Triggers pull-to-refresh sync
     */
    suspend fun triggerPullToRefreshSync(): Result<String> {
        return try {
            if (!pullToRefreshEnabled) {
                return Result.failure(Exception("Pull-to-refresh sync is disabled"))
            }
            
            Log.d(TAG, "Pull-to-refresh sync triggered")
            
            if (!networkMonitor.isCurrentlyOnline()) {
                return Result.failure(Exception("No network connection"))
            }
            
            // Use incremental sync for pull-to-refresh (faster)
            syncScheduler.triggerIncrementalSync()
            
            Result.success("Refresh sync started")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to trigger pull-to-refresh sync", e)
            Result.failure(e)
        }
    }
    
    /**
     * Triggers sync when a script is created/modified
     */
    fun triggerOnScriptChange(scriptId: Long) {
        if (networkMonitor.isGoodForSync()) {
            Log.d(TAG, "Triggering sync for script change: $scriptId")
            syncScheduler.triggerScriptSync(scriptId)
        } else {
            Log.d(TAG, "Network not suitable for immediate sync, will sync later")
            syncScheduler.scheduleDelayedSync(delayMinutes = 2)
        }
    }
    
    /**
     * Triggers sync when multiple scripts are modified (batch operation)
     */
    fun triggerOnBatchScriptChanges(scriptIds: List<Long>) {
        if (scriptIds.isEmpty()) return
        
        Log.d(TAG, "Triggering sync for batch script changes: ${scriptIds.size} scripts")
        
        if (networkMonitor.isGoodForSync()) {
            // For batch changes, use incremental sync instead of individual script syncs
            syncScheduler.triggerIncrementalSync()
        } else {
            // Schedule delayed sync for batch operations
            syncScheduler.scheduleDelayedSync(delayMinutes = 5)
        }
    }
    
    /**
     * Triggers sync when app comes to foreground
     */
    fun triggerOnAppForeground() {
        scope.launch {
            // Small delay to let the app settle
            delay(2000)
            
            if (networkMonitor.isCurrentlyOnline()) {
                val pendingCount = hybridRepository.getPendingSyncCount()
                
                if (pendingCount > 0) {
                    Log.d(TAG, "App foreground: $pendingCount pending items, triggering sync")
                    syncScheduler.triggerIncrementalSync()
                } else {
                    Log.d(TAG, "App foreground: no pending items")
                }
            }
        }
    }
    
    /**
     * Triggers sync when network becomes available
     */
    private fun triggerOnNetworkAvailable() {
        scope.launch {
            // Wait for network to stabilize
            delay(NETWORK_STABILIZATION_DELAY_MS)
            
            val pendingCount = hybridRepository.getPendingSyncCount()
            
            if (pendingCount > 0) {
                Log.d(TAG, "Network available: $pendingCount pending items, triggering sync")
                
                when {
                    networkMonitor.isGoodForHeavySync() -> {
                        syncScheduler.triggerImmediateSync()
                    }
                    networkMonitor.isGoodForSync() -> {
                        syncScheduler.triggerIncrementalSync()
                    }
                    else -> {
                        syncScheduler.scheduleDelayedSync(delayMinutes = 10)
                    }
                }
            }
        }
    }
    
    /**
     * Starts monitoring pending items count
     */
    private fun startPendingItemsMonitor() {
        pendingMonitorJob?.cancel()
        
        pendingMonitorJob = scope.launch {
            while (isActive) {
                try {
                    val pendingCount = hybridRepository.getPendingSyncCount()
                    
                    if (pendingCount >= PENDING_THRESHOLD && networkMonitor.isGoodForSync()) {
                        Log.d(TAG, "Pending threshold reached: $pendingCount items, triggering sync")
                        syncScheduler.triggerIncrementalSync()
                        
                        // Wait longer after triggering sync to avoid spam
                        delay(PENDING_CHECK_INTERVAL_MS * 3)
                    } else {
                        delay(PENDING_CHECK_INTERVAL_MS)
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error in pending items monitor", e)
                    delay(PENDING_CHECK_INTERVAL_MS)
                }
            }
        }
        
        Log.d(TAG, "Pending items monitor started")
    }
    
    /**
     * Starts network-based sync triggers
     */
    private fun startNetworkTriggers() {
        networkTriggerJob?.cancel()
        
        networkTriggerJob = scope.launch {
            var previouslyOnline = networkMonitor.isCurrentlyOnline()
            
            networkMonitor.observeNetworkChanges()
                .distinctUntilChanged { old, new -> 
                    old.isConnected == new.isConnected 
                }
                .filter { it.isConnected && !previouslyOnline }
                .collectLatest { networkState ->
                    previouslyOnline = networkState.isConnected
                    
                    if (networkState.isConnected) {
                        Log.d(TAG, "Network became available: ${networkState.getDescription()}")
                        triggerOnNetworkAvailable()
                    }
                }
        }
        
        Log.d(TAG, "Network triggers started")
    }
    
    /**
     * Enables or disables pull-to-refresh sync
     */
    fun setPullToRefreshEnabled(enabled: Boolean) {
        pullToRefreshEnabled = enabled
        Log.d(TAG, "Pull-to-refresh sync ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Gets current trigger status
     */
    suspend fun getTriggerStatus(): SyncTriggerStatus {
        val pendingCount = hybridRepository.getPendingSyncCount()
        val networkState = networkMonitor.getCurrentNetworkState()
        val syncWorkStatus = syncScheduler.getSyncWorkStatus()
        
        return SyncTriggerStatus(
            pendingItemsCount = pendingCount,
            isNetworkAvailable = networkState.isConnected,
            networkQuality = networkState.getDescription(),
            pullToRefreshEnabled = pullToRefreshEnabled,
            hasActiveSyncWork = syncWorkStatus.hasActiveSyncWork,
            canTriggerSync = networkState.isConnected && !syncWorkStatus.isImmediateSyncRunning
        )
    }
    
    // Lifecycle callbacks
    
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d(TAG, "App started")
        
        // Restart monitors if needed
        if (pendingMonitorJob?.isActive != true) {
            startPendingItemsMonitor()
        }
        
        if (networkTriggerJob?.isActive != true) {
            startNetworkTriggers()
        }
        
        // Trigger foreground sync
        triggerOnAppForeground()
    }
    
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d(TAG, "App stopped")
        
        // Optionally pause some monitors to save battery
        // but keep essential ones running
    }
    
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        Log.d(TAG, "App destroyed - cleaning up triggers")
        
        pendingMonitorJob?.cancel()
        networkTriggerJob?.cancel()
        scope.cancel()
    }
}

/**
 * Status information about sync triggers
 */
data class SyncTriggerStatus(
    val pendingItemsCount: Int = 0,
    val isNetworkAvailable: Boolean = false,
    val networkQuality: String = "Unknown",
    val pullToRefreshEnabled: Boolean = true,
    val hasActiveSyncWork: Boolean = false,
    val canTriggerSync: Boolean = false
)