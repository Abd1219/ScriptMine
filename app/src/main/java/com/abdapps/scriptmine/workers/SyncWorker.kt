package com.abdapps.scriptmine.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.abdapps.scriptmine.sync.SyncManager
import com.abdapps.scriptmine.utils.NetworkMonitor
import com.abdapps.scriptmine.utils.SyncStrategy
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker for background synchronization
 * Handles periodic and one-time sync operations with intelligent retry logic
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncManager: SyncManager,
    private val networkMonitor: NetworkMonitor
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        private const val TAG = "SyncWorker"
        const val WORK_NAME_PERIODIC = "periodic_sync"
        const val WORK_NAME_IMMEDIATE = "immediate_sync"
        const val WORK_NAME_RETRY = "retry_sync"
        
        // Input data keys
        const val KEY_SYNC_TYPE = "sync_type"
        const val KEY_SCRIPT_ID = "script_id"
        const val KEY_FORCE_SYNC = "force_sync"
        
        // Sync types
        const val SYNC_TYPE_FULL = "full"
        const val SYNC_TYPE_INCREMENTAL = "incremental"
        const val SYNC_TYPE_SINGLE_SCRIPT = "single_script"
        
        /**
         * Creates a periodic sync work request
         */
        fun createPeriodicSyncRequest(): PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .setRequiresStorageNotLow(true)
                .build()
            
            return PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30, TimeUnit.SECONDS
                )
                .setInputData(
                    Data.Builder()
                        .putString(KEY_SYNC_TYPE, SYNC_TYPE_INCREMENTAL)
                        .build()
                )
                .build()
        }
        
        /**
         * Creates an immediate sync work request
         */
        fun createImmediateSyncRequest(
            syncType: String = SYNC_TYPE_FULL,
            forceSync: Boolean = false
        ): OneTimeWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            return OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    10, TimeUnit.SECONDS
                )
                .setInputData(
                    Data.Builder()
                        .putString(KEY_SYNC_TYPE, syncType)
                        .putBoolean(KEY_FORCE_SYNC, forceSync)
                        .build()
                )
                .build()
        }
        
        /**
         * Creates a single script sync work request
         */
        fun createSingleScriptSyncRequest(scriptId: Long): OneTimeWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            return OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setInputData(
                    Data.Builder()
                        .putString(KEY_SYNC_TYPE, SYNC_TYPE_SINGLE_SCRIPT)
                        .putLong(KEY_SCRIPT_ID, scriptId)
                        .build()
                )
                .build()
        }
    }
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Starting background sync work")
            
            // Check network conditions
            val networkState = networkMonitor.getCurrentNetworkState()
            if (!networkState.isConnected) {
                Log.w(TAG, "No network connection, skipping sync")
                return@withContext Result.retry()
            }
            
            // Get sync parameters
            val syncType = inputData.getString(KEY_SYNC_TYPE) ?: SYNC_TYPE_INCREMENTAL
            val forceSync = inputData.getBoolean(KEY_FORCE_SYNC, false)
            val scriptId = inputData.getLong(KEY_SCRIPT_ID, -1L)
            
            Log.d(TAG, "Sync type: $syncType, Force: $forceSync, ScriptId: $scriptId")
            
            // Check if we should proceed based on network strategy
            if (!forceSync && !shouldProceedWithSync(networkState)) {
                Log.d(TAG, "Network conditions not suitable for sync, skipping")
                return@withContext Result.success()
            }
            
            // Perform sync based on type
            val result = when (syncType) {
                SYNC_TYPE_SINGLE_SCRIPT -> {
                    if (scriptId > 0) {
                        syncSingleScript(scriptId)
                    } else {
                        Log.e(TAG, "Invalid script ID for single script sync")
                        Result.failure()
                    }
                }
                
                SYNC_TYPE_FULL -> {
                    performFullSync()
                }
                
                SYNC_TYPE_INCREMENTAL -> {
                    performIncrementalSync()
                }
                
                else -> {
                    Log.e(TAG, "Unknown sync type: $syncType")
                    Result.failure()
                }
            }
            
            // Update progress and return result
            setProgress(createProgressData(100, "Sync completed"))
            Log.d(TAG, "Background sync completed with result: $result")
            
            result
            
        } catch (e: Exception) {
            Log.e(TAG, "Background sync failed", e)
            
            // Determine if we should retry based on the error
            if (shouldRetryOnError(e)) {
                Result.retry()
            } else {
                Result.failure(createErrorData(e.message ?: "Unknown error"))
            }
        }
    }
    
    /**
     * Performs a full synchronization
     */
    private suspend fun performFullSync(): Result {
        return try {
            setProgress(createProgressData(0, "Starting full sync"))
            
            val syncResult = syncManager.performFullSync()
            
            if (syncResult.isSuccess) {
                val result = syncResult.getOrThrow()
                Log.d(TAG, "Full sync successful: $result")
                
                Result.success(
                    Data.Builder()
                        .putInt("uploaded", result.uploaded)
                        .putInt("downloaded", result.downloaded)
                        .putInt("conflicts_resolved", result.conflictsResolved)
                        .build()
                )
            } else {
                val error = syncResult.exceptionOrNull()
                Log.e(TAG, "Full sync failed", error)
                Result.failure(createErrorData(error?.message ?: "Full sync failed"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception during full sync", e)
            Result.failure(createErrorData(e.message ?: "Full sync exception"))
        }
    }
    
    /**
     * Performs an incremental synchronization (pending items only)
     */
    private suspend fun performIncrementalSync(): Result {
        return try {
            setProgress(createProgressData(0, "Starting incremental sync"))
            
            val pendingCount = syncManager.getPendingSyncCount()
            
            if (pendingCount == 0) {
                Log.d(TAG, "No pending items for incremental sync")
                return Result.success()
            }
            
            Log.d(TAG, "Incremental sync: $pendingCount pending items")
            
            // For incremental sync, we just upload pending changes
            val syncResult = syncManager.performFullSync()
            
            if (syncResult.isSuccess) {
                val result = syncResult.getOrThrow()
                Log.d(TAG, "Incremental sync successful: uploaded ${result.uploaded} items")
                
                Result.success(
                    Data.Builder()
                        .putInt("uploaded", result.uploaded)
                        .putString("sync_type", "incremental")
                        .build()
                )
            } else {
                val error = syncResult.exceptionOrNull()
                Log.e(TAG, "Incremental sync failed", error)
                Result.failure(createErrorData(error?.message ?: "Incremental sync failed"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception during incremental sync", e)
            Result.failure(createErrorData(e.message ?: "Incremental sync exception"))
        }
    }
    
    /**
     * Syncs a single script
     */
    private suspend fun syncSingleScript(scriptId: Long): Result {
        return try {
            setProgress(createProgressData(0, "Syncing script $scriptId"))
            
            val result = syncManager.syncScript(scriptId)
            
            if (result.isSuccess) {
                Log.d(TAG, "Single script sync successful: $scriptId")
                Result.success(
                    Data.Builder()
                        .putLong("script_id", scriptId)
                        .putString("sync_type", "single")
                        .build()
                )
            } else {
                val error = result.exceptionOrNull()
                Log.e(TAG, "Single script sync failed: $scriptId", error)
                Result.failure(createErrorData(error?.message ?: "Single script sync failed"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception during single script sync: $scriptId", e)
            Result.failure(createErrorData(e.message ?: "Single script sync exception"))
        }
    }
    
    /**
     * Determines if sync should proceed based on network conditions
     */
    private fun shouldProceedWithSync(networkState: com.abdapps.scriptmine.utils.NetworkState): Boolean {
        val strategy = networkMonitor.getRecommendedSyncStrategy()
        
        return when (strategy) {
            SyncStrategy.FULL_SYNC,
            SyncStrategy.INCREMENTAL_SYNC,
            SyncStrategy.ESSENTIAL_ONLY -> true
            SyncStrategy.OFFLINE_ONLY -> false
        }
    }
    
    /**
     * Determines if we should retry based on the error type
     */
    private fun shouldRetryOnError(error: Throwable): Boolean {
        return when {
            error.message?.contains("network", ignoreCase = true) == true -> true
            error.message?.contains("timeout", ignoreCase = true) == true -> true
            error.message?.contains("connection", ignoreCase = true) == true -> true
            runAttemptCount < 3 -> true
            else -> false
        }
    }
    
    /**
     * Creates progress data for WorkManager
     */
    private fun createProgressData(progress: Int, message: String): Data {
        return Data.Builder()
            .putInt("progress", progress)
            .putString("message", message)
            .build()
    }
    
    /**
     * Creates error data for WorkManager
     */
    private fun createErrorData(errorMessage: String): Data {
        return Data.Builder()
            .putString("error", errorMessage)
            .putLong("timestamp", System.currentTimeMillis())
            .build()
    }
}