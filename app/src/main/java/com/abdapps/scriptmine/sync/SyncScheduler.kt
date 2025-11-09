package com.abdapps.scriptmine.sync

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.work.*
import com.abdapps.scriptmine.utils.NetworkMonitor
import com.abdapps.scriptmine.utils.NetworkState
import com.abdapps.scriptmine.utils.SyncStrategy
import com.abdapps.scriptmine.workers.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages scheduling and coordination of background sync operations
 * Handles periodic sync, network-based triggers, and app lifecycle events
 */
@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkMonitor: NetworkMonitor,
    private val workManager: WorkManager
) : DefaultLifecycleObserver {
    
    companion object {
        private const val TAG = "SyncScheduler"
        private const val PERIODIC_SYNC_INTERVAL_MINUTES = 15L
        private const val NETWORK_SYNC_DELAY_MS = 5000L
        private const val APP_BACKGROUND_SYNC_DELAY_MS = 2000L
    }
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var networkObserverJob: Job? = null
    private var isPeriodicSyncEnabled = true
    private var lastNetworkState: NetworkState? = null
    
    /**
     * Initializes the sync scheduler
     * Should be called from Application.onCreate()
     */
    fun initialize() {
        Log.d(TAG, "Initializing SyncScheduler")
        
        // Schedule periodic sync
        schedulePeriodicSync()
        
        // Start observing network changes
        startNetworkObserver()
        
        Log.d(TAG, "SyncScheduler initialized")
    }
    
    /**
     * Schedules periodic background sync
     */
    fun schedulePeriodicSync() {
        if (!isPeriodicSyncEnabled) {
            Log.d(TAG, "Periodic sync is disabled")
            return
        }
        
        val periodicSyncRequest = SyncWorker.createPeriodicSyncRequest()
        
        workManager.enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSyncRequest
        )
        
        Log.d(TAG, "Periodic sync scheduled (${PERIODIC_SYNC_INTERVAL_MINUTES}min interval)")
    }
    
    /**
     * Cancels periodic sync
     */
    fun cancelPeriodicSync() {
        workManager.cancelUniqueWork(SyncWorker.WORK_NAME_PERIODIC)
        Log.d(TAG, "Periodic sync cancelled")
    }
    
    /**
     * Enables or disables periodic sync
     */
    fun setPeriodicSyncEnabled(enabled: Boolean) {
        isPeriodicSyncEnabled = enabled
        
        if (enabled) {
            schedulePeriodicSync()
        } else {
            cancelPeriodicSync()
        }
        
        Log.d(TAG, "Periodic sync ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Triggers immediate sync
     */
    fun triggerImmediateSync(forceSync: Boolean = false) {
        val immediateSyncRequest = SyncWorker.createImmediateSyncRequest(
            syncType = SyncWorker.SYNC_TYPE_FULL,
            forceSync = forceSync
        )
        
        workManager.enqueueUniqueWork(
            SyncWorker.WORK_NAME_IMMEDIATE,
            ExistingWorkPolicy.REPLACE,
            immediateSyncRequest
        )
        
        Log.d(TAG, "Immediate sync triggered (force: $forceSync)")
    }
    
    /**
     * Triggers incremental sync (pending items only)
     */
    fun triggerIncrementalSync() {
        val incrementalSyncRequest = SyncWorker.createImmediateSyncRequest(
            syncType = SyncWorker.SYNC_TYPE_INCREMENTAL
        )
        
        workManager.enqueueUniqueWork(
            "incremental_sync",
            ExistingWorkPolicy.KEEP,
            incrementalSyncRequest
        )
        
        Log.d(TAG, "Incremental sync triggered")
    }
    
    /**
     * Triggers sync for a specific script
     */
    fun triggerScriptSync(scriptId: Long) {
        val scriptSyncRequest = SyncWorker.createSingleScriptSyncRequest(scriptId)
        
        workManager.enqueueUniqueWork(
            "script_sync_$scriptId",
            ExistingWorkPolicy.REPLACE,
            scriptSyncRequest
        )
        
        Log.d(TAG, "Script sync triggered for ID: $scriptId")
    }
    
    /**
     * Schedules a delayed sync (useful for batching operations)
     */
    fun scheduleDelayedSync(delayMinutes: Long = 5) {
        val delayedSyncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(
                Data.Builder()
                    .putString(SyncWorker.KEY_SYNC_TYPE, SyncWorker.SYNC_TYPE_INCREMENTAL)
                    .build()
            )
            .build()
        
        workManager.enqueueUniqueWork(
            "delayed_sync",
            ExistingWorkPolicy.REPLACE,
            delayedSyncRequest
        )
        
        Log.d(TAG, "Delayed sync scheduled (${delayMinutes}min delay)")
    }
    
    /**
     * Starts observing network changes for automatic sync triggers
     */
    private fun startNetworkObserver() {
        networkObserverJob?.cancel()
        
        networkObserverJob = scope.launch {
            networkMonitor.observeNetworkChanges().collectLatest { networkState ->
                handleNetworkChange(networkState)
            }
        }
        
        Log.d(TAG, "Network observer started")
    }
    
    /**
     * Handles network state changes
     */
    private suspend fun handleNetworkChange(networkState: NetworkState) {
        val previousState = lastNetworkState
        lastNetworkState = networkState
        
        // Skip if this is the first observation
        if (previousState == null) {
            Log.d(TAG, "Initial network state: ${networkState.getDescription()}")
            return
        }
        
        // Check if we went from offline to online
        val wentOnline = !previousState.isConnected && networkState.isConnected
        
        // Check if network quality improved significantly
        val networkImproved = networkState.isConnected && 
                networkState.connectionType.ordinal > previousState.connectionType.ordinal
        
        if (wentOnline) {
            Log.d(TAG, "Network came online, scheduling sync")
            
            // Delay sync to allow network to stabilize
            delay(NETWORK_SYNC_DELAY_MS)
            
            // Trigger sync based on network conditions
            when (networkMonitor.getRecommendedSyncStrategy()) {
                SyncStrategy.FULL_SYNC -> triggerImmediateSync()
                SyncStrategy.INCREMENTAL_SYNC -> triggerIncrementalSync()
                SyncStrategy.ESSENTIAL_ONLY -> triggerIncrementalSync()
                SyncStrategy.OFFLINE_ONLY -> { /* Do nothing */ }
            }
            
        } else if (networkImproved) {
            Log.d(TAG, "Network quality improved, considering sync")
            
            // Only trigger if we have a good connection now
            if (networkMonitor.isGoodForSync()) {
                delay(NETWORK_SYNC_DELAY_MS)
                triggerIncrementalSync()
            }
        }
        
        Log.d(TAG, "Network state: ${networkState.getDescription()}")
    }
    
    /**
     * Gets the status of all sync work
     */
    suspend fun getSyncWorkStatus(): SyncWorkStatus {
        val periodicWork = workManager.getWorkInfosForUniqueWork(SyncWorker.WORK_NAME_PERIODIC).get()
        val immediateWork = workManager.getWorkInfosForUniqueWork(SyncWorker.WORK_NAME_IMMEDIATE).get()
        
        return SyncWorkStatus(
            isPeriodicSyncScheduled = periodicWork.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING },
            isImmediateSyncRunning = immediateWork.any { it.state == WorkInfo.State.RUNNING },
            periodicSyncEnabled = isPeriodicSyncEnabled,
            lastPeriodicSyncTime = periodicWork.firstOrNull()?.outputData?.getLong("timestamp", 0L) ?: 0L,
            pendingWorkCount = workManager.getWorkInfosByTag("sync").get().count { 
                it.state == WorkInfo.State.ENQUEUED 
            }
        )
    }
    
    /**
     * Cancels all sync work
     */
    fun cancelAllSyncWork() {
        workManager.cancelAllWorkByTag("sync")
        workManager.cancelUniqueWork(SyncWorker.WORK_NAME_PERIODIC)
        workManager.cancelUniqueWork(SyncWorker.WORK_NAME_IMMEDIATE)
        
        Log.d(TAG, "All sync work cancelled")
    }
    
    // Lifecycle callbacks
    
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d(TAG, "App started - resuming sync scheduling")
        
        // Resume network observer if needed
        if (networkObserverJob?.isActive != true) {
            startNetworkObserver()
        }
    }
    
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d(TAG, "App stopped - triggering background sync")
        
        // Trigger a sync when app goes to background
        scope.launch {
            delay(APP_BACKGROUND_SYNC_DELAY_MS)
            
            if (networkMonitor.isCurrentlyOnline()) {
                triggerIncrementalSync()
            }
        }
    }
    
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        Log.d(TAG, "App destroyed - cleaning up")
        
        networkObserverJob?.cancel()
        scope.cancel()
    }
}

/**
 * Status information about sync work
 */
data class SyncWorkStatus(
    val isPeriodicSyncScheduled: Boolean = false,
    val isImmediateSyncRunning: Boolean = false,
    val periodicSyncEnabled: Boolean = true,
    val lastPeriodicSyncTime: Long = 0L,
    val pendingWorkCount: Int = 0
) {
    val hasActiveSyncWork: Boolean
        get() = isPeriodicSyncScheduled || isImmediateSyncRunning || pendingWorkCount > 0
}