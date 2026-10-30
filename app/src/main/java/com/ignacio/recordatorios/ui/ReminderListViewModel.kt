package com.ignacio.recordatorios.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ignacio.recordatorios.data.ReminderRepository
import com.ignacio.recordatorios.data.settings.PreferencesRepository
import com.ignacio.recordatorios.data.settings.SortOrder
import com.ignacio.recordatorios.model.Reminder
import com.ignacio.recordatorios.model.ReminderStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface ReminderListUiState {
    data object Loading : ReminderListUiState
    data object Empty : ReminderListUiState
    data class Data(val reminders: List<Reminder>) : ReminderListUiState
    data class Error(val message: String) : ReminderListUiState
}
class ReminderListViewModel(private val repository: ReminderRepository,
                            private val settings: PreferencesRepository): ViewModel() {
    private val _uiState = MutableStateFlow<ReminderListUiState>(ReminderListUiState.Loading)
    val uiState: StateFlow<ReminderListUiState> = _uiState
    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage
    private var simulate: ReminderListUiState? = null
    private var loadJob: kotlinx.coroutines.Job? = null
    init { load() }
    fun load() {
        simulate = null
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            combine(repository.observeAll(), settings.preferences) { items, p ->
                val visible = if (p.showCompleted) items else items.filter { it.status != ReminderStatus.Completed }
                when(p.sortOrder) {
                    SortOrder.NEWEST -> visible.sortedByDescending { it.id }
                    SortOrder.OLDEST -> visible.sortedBy { it.id }
                    SortOrder.TITLE -> visible.sortedBy { it.title.lowercase() }
                }
            }.collect { items ->
                _uiState.value = simulate ?: if (items.isEmpty()) ReminderListUiState.Empty else ReminderListUiState.Data(items)
            }
        }
    }
    fun simulateEmpty() { simulate = ReminderListUiState.Empty; _uiState.value = simulate!! }
    fun simulateError() { simulate = ReminderListUiState.Error("Error de red simulado. Intentá nuevamente."); _uiState.value = simulate!! }
    fun sync() { viewModelScope.launch {
        _syncMessage.value = "Sincronizando..."
        val result = repository.sync()
        _syncMessage.value = result.error ?: "Sincronización finalizada: ${result.sent} operaciones procesadas"
    } }
    companion object {
        fun factory(repo: ReminderRepository, prefs: PreferencesRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = ReminderListViewModel(repo, prefs) as T
        }
    }
}
