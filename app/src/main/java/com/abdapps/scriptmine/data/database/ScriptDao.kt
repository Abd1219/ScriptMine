package com.abdapps.scriptmine.data.database

import androidx.room.*
import com.abdapps.scriptmine.data.model.SavedScript
import com.abdapps.scriptmine.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ScriptDao {
    // Existing methods
    @Query("SELECT * FROM saved_scripts WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun getAllScripts(): Flow<List<SavedScript>>

    @Query("SELECT * FROM saved_scripts WHERE templateType = :templateType AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getScriptsByTemplate(templateType: String): Flow<List<SavedScript>>

    @Query("SELECT * FROM saved_scripts WHERE id = :id AND isDeleted = 0")
    suspend fun getScriptById(id: Long): SavedScript?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScript(script: SavedScript): Long

    @Update
    suspend fun updateScript(script: SavedScript)

    @Delete
    suspend fun deleteScript(script: SavedScript)

    @Query("DELETE FROM saved_scripts WHERE id = :id")
    suspend fun deleteScriptById(id: Long)
    
    // Sync-specific methods
    @Query("SELECT * FROM saved_scripts WHERE syncStatus = :status")
    suspend fun getScriptsByStatus(status: SyncStatus): List<SavedScript>
    
    @Query("SELECT * FROM saved_scripts WHERE syncStatus IN (:statuses)")
    suspend fun getUnsyncedScripts(statuses: List<SyncStatus> = listOf(SyncStatus.PENDING, SyncStatus.ERROR)): List<SavedScript>
    
    @Query("UPDATE saved_scripts SET syncStatus = :status, lastSyncAt = :syncTime WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, status: SyncStatus, syncTime: Date = Date())
    
    @Query("SELECT * FROM saved_scripts WHERE firebaseId = :firebaseId")
    suspend fun getScriptByFirebaseId(firebaseId: String): SavedScript?
    
    @Query("UPDATE saved_scripts SET isDeleted = 1, updatedAt = :deleteTime, syncStatus = :syncStatus WHERE id = :id")
    suspend fun softDelete(id: Long, deleteTime: Date = Date(), syncStatus: SyncStatus = SyncStatus.PENDING)
    
    @Query("SELECT * FROM saved_scripts WHERE userId = :userId AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getScriptsByUser(userId: String): Flow<List<SavedScript>>
    
    @Query("SELECT * FROM saved_scripts WHERE syncStatus = :conflictStatus")
    suspend fun getConflictedScripts(conflictStatus: SyncStatus = SyncStatus.CONFLICT): List<SavedScript>
    
    @Query("UPDATE saved_scripts SET firebaseId = :firebaseId, syncStatus = :status, lastSyncAt = :syncTime WHERE id = :id")
    suspend fun updateFirebaseId(id: Long, firebaseId: String, status: SyncStatus = SyncStatus.SYNCED, syncTime: Date = Date())
    
    @Query("SELECT COUNT(*) FROM saved_scripts WHERE syncStatus IN (:pendingStatuses)")
    suspend fun getPendingSyncCount(pendingStatuses: List<SyncStatus> = listOf(SyncStatus.PENDING, SyncStatus.ERROR)): Int
}