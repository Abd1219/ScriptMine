package com.abdapps.scriptmine.sync

import android.util.Log
import com.abdapps.scriptmine.data.database.ScriptDao
import com.abdapps.scriptmine.data.model.SavedScript
import com.abdapps.scriptmine.data.model.SyncStatus
import com.abdapps.scriptmine.data.repository.FirebaseScriptRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages synchronization between local database and Firebase Firestore
 * Implements offline-first approach with intelligent sync strategies
 */
@Singleton
class SyncManager @Inject constructor(
    private val localDao: ScriptDao,
    private val firebaseRepo: FirebaseScriptRepository,
    private val conflictResolver: ConflictResolver,
    private val auth: FirebaseAuth
) {
    
    companion object {
        private const val TAG = "SyncManager"
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 1000L
    }
    
    // Sync status tracking
    private val _syncStatus = MutableStateFlow(SyncState.IDLE)
    val syncStatus: Flow<SyncState> = _syncStatus.asStateFlow()
    
    private val _syncProgress = MutableStateFlow(SyncProgress())
    val syncProgress: Flow<SyncProgress> = _syncProgress.asStateFlow()
    
    /**
     * Synchronizes a single script with Firebase
     * @param localId Local database ID of the script
     * @return Result indicating success or failure
     */
    suspend fun syncScript(localId: Long): Result<Unit> {
        return try {
            val localScript = localDao.getScriptById(localId)
                ?: return Result.failure(Exception("Script not found: $localId"))
            
            Log.d(TAG, "Starting sync for script: $localId")
            
            // Update sync status to SYNCING
            localDao.updateSyncStatus(localId, SyncStatus.SYNCING)
            
            // Attempt to upload to Firebase
            val uploadResult = firebaseRepo.uploadScript(localScript)
            
            if (uploadResult.isSuccess) {
                val firebaseId = uploadResult.getOrThrow()
                
                // Update local script with Firebase ID and SYNCED status
                localDao.updateFirebaseId(localId, firebaseId, SyncStatus.SYNCED)
                
                Log.d(TAG, "Script synced successfully: $localId -> $firebaseId")
                Result.success(Unit)
            } else {
                // Mark as ERROR status
                localDao.updateSyncStatus(localId, SyncStatus.ERROR)
                
                val error = uploadResult.exceptionOrNull()
                Log.e(TAG, "Failed to sync script: $localId", error)
                Result.failure(error ?: Exception("Unknown sync error"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception during script sync: $localId", e)
            localDao.updateSyncStatus(localId, SyncStatus.ERROR)
            Result.failure(e)
        }
    }
    
    /**
     * Performs a full synchronization between local and Firebase
     * @return Result indicating overall sync success
     */
    suspend fun performFullSync(): Result<SyncResult> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.w(TAG, "Cannot perform full sync: User not authenticated")
                return Result.failure(Exception("User not authenticated"))
            }
            
            _syncStatus.value = SyncState.SYNCING
            val syncResult = SyncResult()
            
            Log.d(TAG, "Starting full sync for user: ${currentUser.uid}")
            
            // Phase 1: Upload pending local changes
            val uploadResult = uploadPendingScripts()
            syncResult.uploaded = uploadResult.getOrElse { 0 }
            
            // Phase 2: Download remote changes
            val downloadResult = downloadRemoteChanges(currentUser.uid)
            syncResult.downloaded = downloadResult.getOrElse { 0 }
            
            // Phase 3: Resolve conflicts
            val conflictResult = resolveConflicts()
            syncResult.conflictsResolved = conflictResult.getOrElse { 0 }
            
            _syncStatus.value = SyncState.IDLE
            _syncProgress.value = SyncProgress()
            
            Log.d(TAG, "Full sync completed: $syncResult")
            Result.success(syncResult)
            
        } catch (e: Exception) {
            Log.e(TAG, "Full sync failed", e)
            _syncStatus.value = SyncState.ERROR
            Result.failure(e)
        }
    }
    
    /**
     * Uploads all pending local scripts to Firebase
     */
    private suspend fun uploadPendingScripts(): Result<Int> {
        return try {
            val pendingScripts = localDao.getUnsyncedScripts()
            var uploadedCount = 0
            
            _syncProgress.value = _syncProgress.value.copy(
                totalItems = pendingScripts.size,
                currentItem = 0,
                phase = "Uploading local changes"
            )
            
            pendingScripts.forEachIndexed { index, script ->
                _syncProgress.value = _syncProgress.value.copy(currentItem = index + 1)
                
                val result = syncScript(script.id)
                if (result.isSuccess) {
                    uploadedCount++
                }
            }
            
            Log.d(TAG, "Uploaded $uploadedCount/${pendingScripts.size} pending scripts")
            Result.success(uploadedCount)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading pending scripts", e)
            Result.failure(e)
        }
    }
    
    /**
     * Downloads and processes remote changes from Firebase
     */
    private suspend fun downloadRemoteChanges(userId: String): Result<Int> {
        return try {
            val remoteResult = firebaseRepo.downloadUserScripts(userId)
            if (remoteResult.isFailure) {
                return Result.failure(remoteResult.exceptionOrNull() ?: Exception("Download failed"))
            }
            
            val remoteScripts = remoteResult.getOrThrow()
            var processedCount = 0
            
            _syncProgress.value = _syncProgress.value.copy(
                totalItems = remoteScripts.size,
                currentItem = 0,
                phase = "Processing remote changes"
            )
            
            remoteScripts.forEachIndexed { index, remoteScript ->
                _syncProgress.value = _syncProgress.value.copy(currentItem = index + 1)
                
                val localScript = localDao.getScriptByFirebaseId(remoteScript.id)
                
                when {
                    localScript == null -> {
                        // New remote script - insert locally
                        val savedScript = remoteScript.toSavedScript()
                        localDao.insertScript(savedScript)
                        processedCount++
                        Log.d(TAG, "Inserted new remote script: ${remoteScript.id}")
                    }
                    
                    localScript.version < remoteScript.version -> {
                        // Remote is newer - update local
                        val updatedScript = remoteScript.toSavedScript(localScript.id)
                        localDao.updateScript(updatedScript)
                        processedCount++
                        Log.d(TAG, "Updated local script from remote: ${remoteScript.id}")
                    }
                    
                    localScript.version > remoteScript.version -> {
                        // Local is newer - upload to Firebase
                        syncScript(localScript.id)
                        processedCount++
                        Log.d(TAG, "Uploaded newer local script: ${localScript.id}")
                    }
                    
                    else -> {
                        // Same version - check for conflicts
                        if (conflictResolver.hasConflict(localScript, remoteScript)) {
                            localDao.updateSyncStatus(localScript.id, SyncStatus.CONFLICT)
                            Log.d(TAG, "Conflict detected for script: ${localScript.id}")
                        }
                    }
                }
            }
            
            Log.d(TAG, "Processed $processedCount remote changes")
            Result.success(processedCount)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading remote changes", e)
            Result.failure(e)
        }
    }
    
    /**
     * Resolves conflicts using the ConflictResolver
     */
    private suspend fun resolveConflicts(): Result<Int> {
        return try {
            val conflictedScripts = localDao.getConflictedScripts()
            var resolvedCount = 0
            
            _syncProgress.value = _syncProgress.value.copy(
                totalItems = conflictedScripts.size,
                currentItem = 0,
                phase = "Resolving conflicts"
            )
            
            conflictedScripts.forEachIndexed { index, localScript ->
                _syncProgress.value = _syncProgress.value.copy(currentItem = index + 1)
                
                if (localScript.firebaseId != null) {
                    val remoteResult = firebaseRepo.downloadScript(localScript.firebaseId)
                    
                    if (remoteResult.isSuccess) {
                        val remoteScript = remoteResult.getOrThrow()
                        val resolved = conflictResolver.resolve(localScript, remoteScript)
                        
                        localDao.updateScript(resolved)
                        
                        // If resolution favors local, upload to Firebase
                        if (resolved.syncStatus == SyncStatus.PENDING) {
                            syncScript(resolved.id)
                        }
                        
                        resolvedCount++
                        Log.d(TAG, "Resolved conflict for script: ${localScript.id}")
                    }
                }
            }
            
            Log.d(TAG, "Resolved $resolvedCount conflicts")
            Result.success(resolvedCount)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving conflicts", e)
            Result.failure(e)
        }
    }
    
    /**
     * Gets the count of scripts pending synchronization
     */
    suspend fun getPendingSyncCount(): Int {
        return try {
            localDao.getPendingSyncCount()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting pending sync count", e)
            0
        }
    }
    
    /**
     * Checks if synchronization is currently in progress
     */
    fun isSyncing(): Boolean {
        return _syncStatus.value == SyncState.SYNCING
    }
    
    /**
     * Cancels any ongoing synchronization (for future implementation)
     */
    fun cancelSync() {
        if (_syncStatus.value == SyncState.SYNCING) {
            _syncStatus.value = SyncState.CANCELLED
            Log.d(TAG, "Sync cancelled by user")
        }
    }
}

/**
 * Represents the current state of synchronization
 */
enum class SyncState {
    IDLE,       // No sync in progress
    SYNCING,    // Sync in progress
    ERROR,      // Sync failed
    CANCELLED   // Sync was cancelled
}

/**
 * Tracks progress of synchronization operations
 */
data class SyncProgress(
    val totalItems: Int = 0,
    val currentItem: Int = 0,
    val phase: String = "",
    val message: String = ""
) {
    val progressPercentage: Float
        get() = if (totalItems > 0) (currentItem.toFloat() / totalItems) * 100f else 0f
}

/**
 * Result of a full synchronization operation
 */
data class SyncResult(
    var uploaded: Int = 0,
    var downloaded: Int = 0,
    var conflictsResolved: Int = 0
) {
    val totalProcessed: Int
        get() = uploaded + downloaded + conflictsResolved
        
    override fun toString(): String {
        return "SyncResult(uploaded=$uploaded, downloaded=$downloaded, conflicts=$conflictsResolved, total=$totalProcessed)"
    }
}