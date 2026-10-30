package com.ignacio.recordatorios.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ignacio.recordatorios.data.settings.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(private val repo: PreferencesRepository): ViewModel() {
    val state: StateFlow<UserPreferences> = repo.preferences.stateIn(viewModelScope,
        SharingStarted.WhileSubscribed(5000), UserPreferences())
    fun dark(v: Boolean) { viewModelScope.launch { repo.setDarkMode(v) } }
    fun completed(v: Boolean) { viewModelScope.launch { repo.setShowCompleted(v) } }
    fun sort(v: SortOrder) { viewModelScope.launch { repo.setSortOrder(v) } }
    companion object {
        fun factory(repo: PreferencesRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModel(repo) as T
        }
    }
}
