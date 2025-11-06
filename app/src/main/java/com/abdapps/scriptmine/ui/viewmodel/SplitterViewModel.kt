package com.abdapps.scriptmine.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdapps.scriptmine.data.model.SavedScript
import com.abdapps.scriptmine.data.repository.ScriptRepository
import com.abdapps.scriptmine.utils.LocationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject

@HiltViewModel
class SplitterViewModel @Inject constructor(
    private val repository: ScriptRepository,
    private val locationHelper: LocationHelper
) : ViewModel() {

    // Form fields StateFlows
    private val _cuentaSplitter = MutableStateFlow("")
    val cuentaSplitter: StateFlow<String> = _cuentaSplitter.asStateFlow()

    private val _clienteSplitter = MutableStateFlow("")
    val clienteSplitter: StateFlow<String> = _clienteSplitter.asStateFlow()

    private val _splitter = MutableStateFlow("")
    val splitter: StateFlow<String> = _splitter.asStateFlow()

    private val _qr = MutableStateFlow("")
    val qr: StateFlow<String> = _qr.asStateFlow()

    private val _posicion = MutableStateFlow("")
    val posicion: StateFlow<String> = _posicion.asStateFlow()

    private val _potenciaEnSplitter = MutableStateFlow("")
    val potenciaEnSplitter: StateFlow<String> = _potenciaEnSplitter.asStateFlow()

    private val _potenciaEnDomicilio = MutableStateFlow("")
    val potenciaEnDomicilio: StateFlow<String> = _potenciaEnDomicilio.asStateFlow()

    private val _candado = MutableStateFlow("")
    val candado: StateFlow<String> = _candado.asStateFlow()

    private val _coordenadasDeSplitter = MutableStateFlow("")
    val coordenadasDeSplitter: StateFlow<String> = _coordenadasDeSplitter.asStateFlow()

    private val _coordenadasDelClienteSplitter = MutableStateFlow("")
    val coordenadasDelClienteSplitter: StateFlow<String> = _coordenadasDelClienteSplitter.asStateFlow()

    // Loading and success states
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _isGettingLocation = MutableStateFlow(false)
    val isGettingLocation: StateFlow<Boolean> = _isGettingLocation.asStateFlow()

    // Script preview - updated manually when fields change
    private val _scriptPreview = MutableStateFlow("")
    val scriptPreview: StateFlow<String> = _scriptPreview.asStateFlow()

    // Update functions for each field
    fun updateCuentaSplitter(value: String) {
        _cuentaSplitter.value = value
        updateScriptPreview()
    }

    fun updateClienteSplitter(value: String) {
        _clienteSplitter.value = value
        updateScriptPreview()
    }

    fun updateSplitter(value: String) {
        _splitter.value = value
        updateScriptPreview()
    }

    fun updateQr(value: String) {
        _qr.value = value
        updateScriptPreview()
    }

    fun updatePosicion(value: String) {
        _posicion.value = value
        updateScriptPreview()
    }

    fun updatePotenciaEnSplitter(value: String) {
        _potenciaEnSplitter.value = value
        updateScriptPreview()
    }

    fun updatePotenciaEnDomicilio(value: String) {
        _potenciaEnDomicilio.value = value
        updateScriptPreview()
    }

    fun updateCandado(value: String) {
        _candado.value = value
        updateScriptPreview()
    }

    private fun updateScriptPreview() {
        _scriptPreview.value = generateScriptPreview(
            _cuentaSplitter.value,
            _clienteSplitter.value,
            _splitter.value,
            _qr.value,
            _posicion.value,
            _potenciaEnSplitter.value,
            _potenciaEnDomicilio.value,
            _candado.value,
            _coordenadasDeSplitter.value,
            _coordenadasDelClienteSplitter.value
        )
    }

    // Generate script preview with the specified format
    private fun generateScriptPreview(
        cuenta: String,
        cliente: String,
        splitter: String,
        qr: String,
        posicion: String,
        potenciaSplitter: String,
        potenciaDomicilio: String,
        candado: String,
        coordSplitter: String,
        coordCliente: String
    ): String {
        return buildString {
            appendLine("Cuenta: ${cuenta.ifEmpty { "[Pendiente]" }}")
            appendLine("Cliente: ${cliente.ifEmpty { "[Pendiente]" }}")
            appendLine()
            appendLine("DATOS DE CONEXIÓN")
            appendLine("SPLITTER: ${splitter.ifEmpty { "[Pendiente]" }}")
            appendLine("QR: ${qr.ifEmpty { "[Pendiente]" }}")
            appendLine("Posición: ${posicion.ifEmpty { "[Pendiente]" }}")
            appendLine("Potencia en splitter: ${potenciaSplitter.ifEmpty { "[Pendiente]" }}")
            appendLine("Potencia en domicilio: ${potenciaDomicilio.ifEmpty { "[Pendiente]" }}")
            appendLine("Candado: ${candado.ifEmpty { "[Pendiente]" }}")
            appendLine("Coordenadas de splitter: ${coordSplitter.ifEmpty { "[Pendiente]" }}")
            append("Coordenadas del cliente: ${coordCliente.ifEmpty { "[Pendiente]" }}")
        }
    }

    // Get coordinates for specified field
    fun onGetCoordinates(fieldName: String) {
        viewModelScope.launch {
            _isGettingLocation.value = true
            try {
                val coordinates = locationHelper.getCurrentLocation()
                coordinates?.let { coords ->
                    when (fieldName) {
                        "coordenadasDeSplitter" -> {
                            _coordenadasDeSplitter.value = coords
                            updateScriptPreview()
                        }
                        "coordenadasDelClienteSplitter" -> {
                            _coordenadasDelClienteSplitter.value = coords
                            updateScriptPreview()
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle error - could add error state here
            } finally {
                _isGettingLocation.value = false
            }
        }
    }

    // Save script to repository
    fun onSaveScript() {
        viewModelScope.launch {
            _isSaving.value = true
            _saveSuccess.value = false

            try {
                val formDataMap = mapOf(
                    "cuentaSplitter" to _cuentaSplitter.value,
                    "clienteSplitter" to _clienteSplitter.value,
                    "splitter" to _splitter.value,
                    "qr" to _qr.value,
                    "posicion" to _posicion.value,
                    "potenciaEnSplitter" to _potenciaEnSplitter.value,
                    "potenciaEnDomicilio" to _potenciaEnDomicilio.value,
                    "candado" to _candado.value,
                    "coordenadasDeSplitter" to _coordenadasDeSplitter.value,
                    "coordenadasDelClienteSplitter" to _coordenadasDelClienteSplitter.value
                )

                val clientName = generateSplitterClientName()

                val script = SavedScript(
                    templateType = "SPLITTER",
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
        _cuentaSplitter.value = ""
        _clienteSplitter.value = ""
        _splitter.value = ""
        _qr.value = ""
        _posicion.value = ""
        _potenciaEnSplitter.value = ""
        _potenciaEnDomicilio.value = ""
        _candado.value = ""
        _coordenadasDeSplitter.value = ""
        _coordenadasDelClienteSplitter.value = ""
        updateScriptPreview()
    }

    private suspend fun generateSplitterClientName(): String {
        return if (_clienteSplitter.value.isNotBlank()) {
            _clienteSplitter.value
        } else {
            try {
                val existingScripts = repository.getScriptsByTemplate("SPLITTER").first()
                val count = existingScripts.size + 1
                "Splitter ${String.format("%03d", count)}"
            } catch (e: Exception) {
                "Splitter 001"
            }
        }
    }
}