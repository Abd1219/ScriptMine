package com.abdapps.scriptmine.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdapps.scriptmine.data.model.SavedScript
import com.abdapps.scriptmine.data.model.ScriptTemplate
import com.abdapps.scriptmine.data.repository.ScriptRepository
import com.abdapps.scriptmine.utils.ScriptGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.util.Date

class EditScriptViewModel(
    private val repository: ScriptRepository
) : ViewModel() {

    private val _formData = MutableStateFlow<Map<String, String>>(emptyMap())
    val formData: StateFlow<Map<String, String>> = _formData.asStateFlow()

    private val _generatedScript = MutableStateFlow("")
    val generatedScript: StateFlow<String> = _generatedScript.asStateFlow()

    private val _normalizedScript = MutableStateFlow("")
    val normalizedScript: StateFlow<String> = _normalizedScript.asStateFlow()

    private val _currentTemplate = MutableStateFlow<ScriptTemplate?>(null)
    val currentTemplate: StateFlow<ScriptTemplate?> = _currentTemplate.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _currentScriptId = MutableStateFlow<Long?>(null)

    fun setTemplate(template: ScriptTemplate) {
        _currentTemplate.value = template
        _formData.value = emptyMap()
        _generatedScript.value = ""
    }

    fun updateField(key: String, value: String) {
        val currentData = _formData.value.toMutableMap()
        currentData[key] = value
        _formData.value = currentData
        generateScript()
    }

    fun loadScript(scriptId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val script = repository.getScriptById(scriptId)
                script?.let {
                    _currentScriptId.value = it.id
                    val template = ScriptTemplate.values().find { template -> 
                        template.name == it.templateType 
                    }
                    template?.let { t ->
                        _currentTemplate.value = t
                        _formData.value = Json.decodeFromString(it.formData)
                        _generatedScript.value = it.generatedScript
                    }
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateScript() {
        val template = _currentTemplate.value ?: return
        val data = _formData.value
        _generatedScript.value = ScriptGenerator.generateScript(template, data)
        
        // Generate normalized script for SOPORTE template
        if (template == ScriptTemplate.SOPORTE) {
            _normalizedScript.value = ScriptGenerator.generateNormalizedScript(template, data)
        } else {
            _normalizedScript.value = ""
        }
    }

    fun saveScript() {
        viewModelScope.launch {
            val template = _currentTemplate.value ?: return@launch
            val data = _formData.value
            val script = _generatedScript.value
            val clientName = generateClientName(template, data)

            _isSaving.value = true
            _saveSuccess.value = false

            try {
                val savedScript = SavedScript(
                    id = _currentScriptId.value ?: 0,
                    templateType = template.name,
                    clientName = clientName,
                    formData = Json.encodeToString(data),
                    generatedScript = script,
                    createdAt = if (_currentScriptId.value == null) Date() else Date(),
                    updatedAt = Date()
                )

                if (_currentScriptId.value == null) {
                    val newId = repository.insertScript(savedScript)
                    _currentScriptId.value = newId
                } else {
                    repository.updateScript(savedScript)
                }
                
                _saveSuccess.value = true
                
                // Reset success state after 2 seconds
                kotlinx.coroutines.delay(2000)
                _saveSuccess.value = false
                
            } catch (e: Exception) {
                // Handle error
                _saveSuccess.value = false
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun clearForm() {
        _formData.value = emptyMap()
        _generatedScript.value = ""
        _currentScriptId.value = null
    }

    private suspend fun generateClientName(template: ScriptTemplate, data: Map<String, String>): String {
        return when (template) {
            ScriptTemplate.TIPIFICACION -> {
                data["cliente"]?.takeIf { it.isNotBlank() } ?: "Tipificación ${generateSequentialNumber("TIPIFICACION")}"
            }
            ScriptTemplate.INTERVENCION -> {
                data["cliente"]?.takeIf { it.isNotBlank() } ?: "Intervención ${generateSequentialNumber("INTERVENCION")}"
            }
            ScriptTemplate.SOPORTE -> {
                "Soporte ${generateSequentialNumber("SOPORTE")}"
            }
            ScriptTemplate.SPLITTER -> {
                data["clienteSplitter"]?.takeIf { it.isNotBlank() } ?: "Splitter ${generateSequentialNumber("SPLITTER")}"
            }
            ScriptTemplate.CIERRE_MANUAL -> {
                data["cliente"]?.takeIf { it.isNotBlank() } ?: "Cierre ${generateSequentialNumber("CIERRE_MANUAL")}"
            }
        }
    }

    private suspend fun generateSequentialNumber(templateType: String): String {
        return try {
            val existingScripts = repository.getScriptsByTemplate(templateType).first()
            val count = existingScripts.size + 1
            String.format("%03d", count) // Formato 001, 002, 003, etc.
        } catch (e: Exception) {
            "001"
        }
    }
}