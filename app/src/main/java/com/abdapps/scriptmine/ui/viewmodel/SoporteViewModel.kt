package com.abdapps.scriptmine.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdapps.scriptmine.data.model.SavedScript
import com.abdapps.scriptmine.data.repository.ScriptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.util.Date

class SoporteViewModel(
    private val repository: ScriptRepository
) : ViewModel() {

    // Form fields StateFlows
    private val _horaInicio = MutableStateFlow("")
    val horaInicio: StateFlow<String> = _horaInicio.asStateFlow()

    private val _horaTermino = MutableStateFlow("")
    val horaTermino: StateFlow<String> = _horaTermino.asStateFlow()

    private val _tiempoEspera = MutableStateFlow("")
    val tiempoEspera: StateFlow<String> = _tiempoEspera.asStateFlow()

    private val _actividadesSoporte = MutableStateFlow("")
    val actividadesSoporte: StateFlow<String> = _actividadesSoporte.asStateFlow()

    private val _observacionesSoporte = MutableStateFlow("")
    val observacionesSoporte: StateFlow<String> = _observacionesSoporte.asStateFlow()

    // Loading and success states
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    // Script preview combining all fields in real-time
    val scriptPreview: StateFlow<String> = combine(
        _horaInicio,
        _horaTermino,
        _tiempoEspera,
        _actividadesSoporte,
        _observacionesSoporte
    ) { horaInicio, horaTermino, tiempoEspera, actividades, observaciones ->
        generateScriptPreview(horaInicio, horaTermino, tiempoEspera, actividades, observaciones)
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    // Normalized script preview - removes accents, line breaks, punctuation, lowercase, no special characters
    val normalizedScriptPreview: StateFlow<String> = scriptPreview.combine(
        scriptPreview
    ) { preview, _ ->
        normalizeText(preview)
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    // Update functions for each field
    fun updateHoraInicio(value: String) {
        _horaInicio.value = value
    }

    fun updateHoraTermino(value: String) {
        _horaTermino.value = value
    }

    fun updateTiempoEspera(value: String) {
        _tiempoEspera.value = value
    }

    fun updateActividadesSoporte(value: String) {
        _actividadesSoporte.value = value
    }

    fun updateObservacionesSoporte(value: String) {
        _observacionesSoporte.value = value
    }

    // Generate script preview with the specified format
    private fun generateScriptPreview(
        horaInicio: String,
        horaTermino: String,
        tiempoEspera: String,
        actividades: String,
        observaciones: String
    ): String {
        return buildString {
            appendLine("Hora de inicio: ${horaInicio.ifEmpty { "[Pendiente]" }}")
            appendLine("Hora de Termino: ${horaTermino.ifEmpty { "[Pendiente]" }}")
            appendLine("Tiempo de espera para accesos: ${tiempoEspera.ifEmpty { "[Pendiente]" }}")
            appendLine("Actividades realizadas en sitio: ${actividades.ifEmpty { "[Pendiente]" }}")
            append("Observaciones y contratiempos durante la actividad: ${observaciones.ifEmpty { "[Pendiente]" }}")
        }
    }

    // Normalize text: remove accents, line breaks, punctuation, convert to lowercase, remove special characters
    private fun normalizeText(text: String): String {
        if (text.isEmpty()) return ""
        
        return text
            // Remove line breaks and replace with spaces
            .replace("\n", " ")
            .replace("\r", " ")
            // Convert to lowercase
            .lowercase()
            // Remove accents and special characters
            .replace("á", "a").replace("à", "a").replace("ä", "a").replace("â", "a")
            .replace("é", "e").replace("è", "e").replace("ë", "e").replace("ê", "e")
            .replace("í", "i").replace("ì", "i").replace("ï", "i").replace("î", "i")
            .replace("ó", "o").replace("ò", "o").replace("ö", "o").replace("ô", "o")
            .replace("ú", "u").replace("ù", "u").replace("ü", "u").replace("û", "u")
            .replace("ñ", "n")
            .replace("ç", "c")
            // Remove punctuation and special characters
            .replace(Regex("[.,;:!?¡¿\"'()\\[\\]{}\\-_+=*&%$#@|\\\\/<>~`^]"), "")
            // Replace multiple spaces with single space
            .replace(Regex("\\s+"), " ")
            // Trim whitespace
            .trim()
    }

    // Save script to repository
    fun onSaveScript() {
        viewModelScope.launch {
            _isSaving.value = true
            _saveSuccess.value = false

            try {
                val formDataMap = mapOf(
                    "horaInicio" to _horaInicio.value,
                    "horaTermino" to _horaTermino.value,
                    "tiempoEspera" to _tiempoEspera.value,
                    "actividadesSoporte" to _actividadesSoporte.value,
                    "observacionesSoporte" to _observacionesSoporte.value
                )

                val clientName = generateSoporteClientName()

                val script = SavedScript(
                    templateType = "SOPORTE",
                    clientName = clientName,
                    formData = Json.encodeToString(formDataMap),
                    generatedScript = scriptPreview.value,
                    createdAt = Date(),
                    updatedAt = Date()
                )

                repository.insertScript(script)
                _saveSuccess.value = true

                // Reset success state after 2 seconds
                kotlinx.coroutines.delay(2000)
                _saveSuccess.value = false

            } catch (e: Exception) {
                _saveSuccess.value = false
                // Handle error - could add error state here
            } finally {
                _isSaving.value = false
            }
        }
    }

    // Clear all form fields
    fun onClearForm() {
        _horaInicio.value = ""
        _horaTermino.value = ""
        _tiempoEspera.value = ""
        _actividadesSoporte.value = ""
        _observacionesSoporte.value = ""
    }

    private suspend fun generateSoporteClientName(): String {
        return try {
            val existingScripts = repository.getScriptsByTemplate("SOPORTE").first()
            val count = existingScripts.size + 1
            "Soporte ${String.format("%03d", count)}"
        } catch (e: Exception) {
            "Soporte 001"
        }
    }
}