package com.abdapps.scriptmine.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.abdapps.scriptmine.data.repository.ScriptRepository

class ViewModelFactory(
    private val repository: ScriptRepository,
    private val context: Context? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditScriptViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditScriptViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(SoporteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SoporteViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(SplitterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SplitterViewModel(
                repository, 
                com.abdapps.scriptmine.utils.LocationHelper(context ?: throw IllegalArgumentException("Context required for SplitterViewModel"))
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}