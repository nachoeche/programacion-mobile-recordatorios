package com.ignacio.recordatorios.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ignacio.recordatorios.data.ReminderRepository
import com.ignacio.recordatorios.model.Reminder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface ReminderListUiState {
    data object Loading : ReminderListUiState
    data object Empty : ReminderListUiState
    data class Data(val reminders: List<Reminder>) : ReminderListUiState
    data class Error(val message: String) : ReminderListUiState
}

class ReminderListViewModel(private val repository: ReminderRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<ReminderListUiState>(ReminderListUiState.Loading)
    val uiState: StateFlow<ReminderListUiState> = _uiState

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = ReminderListUiState.Loading
            try {
                val reminders = repository.getAll()
                _uiState.value = if (reminders.isEmpty()) ReminderListUiState.Empty
                else ReminderListUiState.Data(reminders)
            } catch (e: Exception) {
                _uiState.value = ReminderListUiState.Error(e.message ?: "Error al cargar")
            }
        }
    }

    // Simulaciones para capturas de la consigna, sin destruir datos.
    fun simulateEmpty() { _uiState.value = ReminderListUiState.Empty }
    fun simulateError() { _uiState.value = ReminderListUiState.Error("Error de red simulado. Intentá nuevamente.") }

    companion object {
        fun factory(repository: ReminderRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ReminderListViewModel(repository) as T
            }
    }
}
