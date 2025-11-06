package com.abdapps.scriptmine.data.repository

import android.util.Log
import com.abdapps.scriptmine.data.database.ScriptDao
import com.abdapps.scriptmine.data.model.SavedScript
import com.abdapps.scriptmine.data.model.SyncStatus
import com.abdapps.scriptmine.sync.SyncManager
import com.abdapps.scriptmine.utils.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hybrid repository that implements offline-first architecture
 * Combines local Room database with Firebase Firestore synchronization
 */
@Singleton
class HybridScriptRepository @Inject constructor(
    val localDao: ScriptDao,
    private val firebaseRepo: FirebaseScriptRepository,
    private val syncManager: SyncManager,
    private val networkMonitor: NetworkMonitor
) {
    
    companion object {
        private const val TAG = "HybridScriptRepository"
    }
    
    /**
     * Gets all scripts from local database (offline-first)
     * Automatically triggers sync if network is available
     */
    fun getAllScripts(): Flow<List<SavedScript>> {
        // Trigger background sync if conditions are good
        triggerBackgroundSyncIfNeeded()
        
        return localDao.getAllScripts().map { scripts ->
            Log.d(TAG, "Retrieved ${scripts.size} scripts from local database")
            scripts
        }
    }
    
    /**
     * Gets scripts by template type from local database
     */
    fun getScriptsByTemplate(templateType: String): Flow<List<SavedScript>> {
        triggerBackgroundSyncIfNeeded()
        
        return localDao.getScriptsByTemplate(templateType).map { scripts ->
            Log.d(TAG, "Retrieved ${scripts.size} scripts for template: $templateType")
            scripts
        }
    }
    
    /**
     * Gets a specific script by ID from local database
     */
    suspend fun getScriptById(id: Long): SavedScript? {
        return try {
            val script = localDao.getScriptById(id)
            Log.d(TAG, "Retrieved script by ID: $id")
            script
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving script by ID: $id", e)
            null
        }
    }
    
    /**
     * Inserts a new script (offline-first approach)
     * 1. Always save locally first
     * 2. Attempt immediate sync if online
     */
    suspend fun insertScript(script: SavedScript): Long {
        return try {
            // 1. Always save locally first (offline-first principle)
            val localId = localDao.insertScript(
                script.copy(
                    syncStatus = SyncStatus.PENDING,
                    createdAt = Date(),
                    updatedAt = Date(),
                    version = 1
                )
            )
            
            Log.d(TAG, "Script saved locally with ID: $localId")
            
            // 2. Attempt immediate sync if network conditions are good
            if (networkMonitor.isGoodForSync()) {
                Log.d(TAG, "Network conditions good, attempting immediate sync")
                syncManager.syncScript(localId)
            } else {
                Log.d(TAG, "Network conditions not optimal, sync will happen later")
            }
            
            localId
            
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting script", e)
            throw e
        }
    }
    
    /**
     * Updates an existing script (offline-first approach)
     */
    suspend fun updateScript(script: SavedScript) {
        try {
            // Update locally with pending sync status and incremented version
            val updatedScript = script.copy(
                syncStatus = SyncStatus.PENDING,
                updatedAt = Date(),
                version = script.version + 1
            )
            
            localDao.updateScript(updatedScript)
            Log.d(TAG, "Script updated locally: ${script.id}")
            
            // Attempt sync if online
            if (networkMonitor.isGoodForSync()) {
                Log.d(TAG, "Attempting immediate sync after update")
                syncManager.syncScript(script.id)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating script: ${script.id}", e)
            throw e
        }
    }
    
    /**
     * Deletes a script (soft delete for sync compatibility)
     */
    suspend fun deleteScript(script: SavedScript) {
        try {
            // Soft delete to maintain sync integrity
            localDao.softDelete(script.id, Date(), SyncStatus.PENDING)
            Log.d(TAG, "Script soft deleted: ${script.id}")
            
            // Attempt to sync deletion if online
            if (networkMonitor.isGoodForSync()) {
                Log.d(TAG, "Attempting to sync deletion")
                syncManager.syncScript(script.id)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting script: ${script.id}", e)
            throw e
        }
    }
    
    /**
     * Deletes a script by ID (soft delete)
     */
    suspend fun deleteScriptById(id: Long) {
        try {
            localDao.softDelete(id, Date(), SyncStatus.PENDING)
            Log.d(TAG, "Script soft deleted by ID: $id")
            
            if (networkMonitor.isGoodForSync()) {
                syncManager.syncScript(id)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting script by ID: $id", e)
            throw e
        }
    }
    
    /**
     * Performs manual synchronization with Firebase
     * @return Result indicating sync success and statistics
     */
    suspend fun syncWithFirebase(): Result<String> {
        return try {
            Log.d(TAG, "Starting manual sync with Firebase")
            
            val result = syncManager.performFullSync()
            
            if (result.isSuccess) {
                val syncResult = result.getOrThrow()
                val message = "Sync completed: ${syncResult.totalProcessed} items processed"
                Log.d(TAG, message)
                Result.success(message)
            } else {
                val error = result.exceptionOrNull() ?: Exception("Unknown sync error")
                Log.e(TAG, "Manual sync failed", error)
                Result.failure(error)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception during manual sync", e)
            Result.failure(e)
        }
    }
    
    /**
     * Gets the count of scripts pending synchronization
     */
    suspend fun getPendingSyncCount(): Int {
        return try {
            val count = syncManager.getPendingSyncCount()
            Log.d(TAG, "Pending sync count: $count")
            count
        } catch (e: Exception) {
            Log.e(TAG, "Error getting pending sync count", e)
            0
        }
    }
    
    /**
     * Checks if synchronization is currently in progress
     */
    fun isSyncing(): Boolean {
        return syncManager.isSyncing()
    }
    
    /**
     * Gets sync status flow for UI updates
     */
    fun getSyncStatus() = syncManager.syncStatus
    
    /**
     * Gets sync progress flow for UI updates
     */
    fun getSyncProgress() = syncManager.syncProgress
    
    /**
     * Gets network state flow for UI updates
     */
    fun getNetworkState() = networkMonitor.networkState
    
    /**
     * Gets scripts by user (for multi-user support)
     */
    fun getScriptsByUser(userId: String): Flow<List<SavedScript>> {
        return localDao.getScriptsByUser(userId)
    }
    
    /**
     * Gets scripts with conflicts that need manual resolution
     */
    suspend fun getConflictedScripts(): List<SavedScript> {
        return try {
            val scripts = localDao.getConflictedScripts()
            Log.d(TAG, "Found ${scripts.size} scripts with conflicts")
            scripts
        } catch (e: Exception) {
            Log.e(TAG, "Error getting conflicted scripts", e)
            emptyList()
        }
    }
    
    /**
     * Forces immediate sync for a specific script
     */
    suspend fun forceSyncScript(scriptId: Long): Result<Unit> {
        return try {
            Log.d(TAG, "Forcing sync for script: $scriptId")
            syncManager.syncScript(scriptId)
        } catch (e: Exception) {
            Log.e(TAG, "Error forcing sync for script: $scriptId", e)
            Result.failure(e)
        }
    }
    
    /**
     * Triggers background sync if network conditions are suitable
     */
    private fun triggerBackgroundSyncIfNeeded() {
        try {
            val strategy = networkMonitor.getRecommendedSyncStrategy()
            
            when (strategy) {
                com.abdapps.scriptmine.utils.SyncStrategy.FULL_SYNC,
                com.abdapps.scriptmine.utils.SyncStrategy.INCREMENTAL_SYNC -> {
                    if (!syncManager.isSyncing()) {
                        Log.d(TAG, "Triggering background sync with strategy: $strategy")
                        // Note: This would typically be done in a background coroutine
                        // For now, we just log the intent
                    }
                }
                else -> {
                    Log.d(TAG, "Network conditions not suitable for sync: $strategy")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking sync conditions", e)
        }
    }
    
    /**
     * Provides repository statistics for debugging and monitoring
     */
    suspend fun getRepositoryStats(): RepositoryStats {
        return try {
            val totalScripts = localDao.getAllScripts().map { it.size }
            val pendingSync = getPendingSyncCount()
            val conflicted = getConflictedScripts().size
            val networkState = networkMonitor.getCurrentNetworkState()
            
            RepositoryStats(
                totalScripts = 0, // Would need to collect from flow
                pendingSync = pendingSync,
                conflicted = conflicted,
                isOnline = networkState.isConnected,
                isSyncing = isSyncing(),
                networkType = networkState.connectionType.displayName
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting repository stats", e)
            RepositoryStats()
        }
    }
}

/**
 * Repository statistics for monitoring and debugging
 */
data class RepositoryStats(
    val totalScripts: Int = 0,
    val pendingSync: Int = 0,
    val conflicted: Int = 0,
    val isOnline: Boolean = false,
    val isSyncing: Boolean = false,
    val networkType: String = "Unknown"
) {
    override fun toString(): String {
        return "RepositoryStats(total=$totalScripts, pending=$pendingSync, conflicts=$conflicted, online=$isOnline, syncing=$isSyncing, network=$networkType)"
    }
}