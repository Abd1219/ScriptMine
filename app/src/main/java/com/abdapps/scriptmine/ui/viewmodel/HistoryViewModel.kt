package com.abdapps.scriptmine.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdapps.scriptmine.data.model.SavedScript
import com.abdapps.scriptmine.data.repository.ScriptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: ScriptRepository
) : ViewModel() {

    private val _scripts = MutableStateFlow<List<SavedScript>>(emptyList())
    val scripts: StateFlow<List<SavedScript>> = _scripts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadScripts()
    }

    private fun loadScripts() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllScripts().collect { scriptList ->
                _scripts.value = scriptList
                _isLoading.value = false
            }
        }
    }

    fun deleteScript(script: SavedScript) {
        viewModelScope.launch {
            try {
                repository.deleteScript(script)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getScriptsByTemplate(): Map<String, List<SavedScript>> {
        return _scripts.value.groupBy { it.templateType }
    }
}