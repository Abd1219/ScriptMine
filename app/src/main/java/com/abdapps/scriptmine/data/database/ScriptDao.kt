package com.abdapps.scriptmine.data.database

import androidx.room.*
import com.abdapps.scriptmine.data.model.SavedScript
import kotlinx.coroutines.flow.Flow

@Dao
interface ScriptDao {
    @Query("SELECT * FROM saved_scripts ORDER BY updatedAt DESC")
    fun getAllScripts(): Flow<List<SavedScript>>

    @Query("SELECT * FROM saved_scripts WHERE templateType = :templateType ORDER BY updatedAt DESC")
    fun getScriptsByTemplate(templateType: String): Flow<List<SavedScript>>

    @Query("SELECT * FROM saved_scripts WHERE id = :id")
    suspend fun getScriptById(id: Long): SavedScript?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScript(script: SavedScript): Long

    @Update
    suspend fun updateScript(script: SavedScript)

    @Delete
    suspend fun deleteScript(script: SavedScript)

    @Query("DELETE FROM saved_scripts WHERE id = :id")
    suspend fun deleteScriptById(id: Long)
}