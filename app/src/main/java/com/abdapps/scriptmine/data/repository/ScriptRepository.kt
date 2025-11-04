package com.abdapps.scriptmine.data.repository

import com.abdapps.scriptmine.data.database.ScriptDao
import com.abdapps.scriptmine.data.model.SavedScript
import kotlinx.coroutines.flow.Flow

class ScriptRepository(
    private val scriptDao: ScriptDao
) {
    fun getAllScripts(): Flow<List<SavedScript>> = scriptDao.getAllScripts()

    fun getScriptsByTemplate(templateType: String): Flow<List<SavedScript>> = 
        scriptDao.getScriptsByTemplate(templateType)

    suspend fun getScriptById(id: Long): SavedScript? = scriptDao.getScriptById(id)

    suspend fun insertScript(script: SavedScript): Long = scriptDao.insertScript(script)

    suspend fun updateScript(script: SavedScript) = scriptDao.updateScript(script)

    suspend fun deleteScript(script: SavedScript) = scriptDao.deleteScript(script)

    suspend fun deleteScriptById(id: Long) = scriptDao.deleteScriptById(id)
}