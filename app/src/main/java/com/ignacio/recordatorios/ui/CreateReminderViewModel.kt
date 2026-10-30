package com.ignacio.recordatorios.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ignacio.recordatorios.data.ReminderRepository
import com.ignacio.recordatorios.data.SampleData
import com.ignacio.recordatorios.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Cada campo controlado se guarda en una sola fuente de verdad.
data class CreateReminderUiState(
    val title: String = "",
    val description: String = "",
    val category: String? = null,
    val priority: Priority? = null,
    val isSubmitting: Boolean = false,
    val saved: Boolean = false,
    val generalError: String? = null,
    val attemptedSave: Boolean = false
) {
    val titleError: String? get() = if (title.trim().length < 4) "Mínimo 4 caracteres" else null
    val descriptionError: String? get() = if (description.length > 200) "Máximo 200 caracteres" else null
    val categoryError: String? get() = if (category == null || SampleData.categories.none { it.name == category })
        "Seleccioná una categoría válida" else null
    val priorityError: String? get() = if (priority == null) "Seleccioná una prioridad" else null
    val canSave: Boolean get() = titleError == null && descriptionError == null &&
        categoryError == null && priorityError == null && !isSubmitting && !saved
}

class CreateReminderViewModel(private val repository: ReminderRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateReminderUiState())
    val uiState: StateFlow<CreateReminderUiState> = _uiState

    fun titleChanged(value: String) { _uiState.update { it.copy(title = value, generalError = null) } }
    fun descriptionChanged(value: String) { _uiState.update { it.copy(description = value, generalError = null) } }
    fun categoryChanged(value: String) { _uiState.update { it.copy(category = value, generalError = null) } }
    fun priorityChanged(value: Priority) { _uiState.update { it.copy(priority = value, generalError = null) } }

    fun simulateSaveFailure() {
        _uiState.update { it.copy(generalError = "Error simulado al guardar. Intentá nuevamente.") }
    }

    fun save() {
        if (!_uiState.value.canSave) {
            _uiState.update { it.copy(attemptedSave = true) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, generalError = null) }
            try {
                val form = _uiState.value
                val reminder = Reminder(
                    id = repository.nextId(), title = form.title.trim(),
                    description = form.description.trim().ifBlank { null },
                    category = Category(requireNotNull(form.category)),
                    priority = requireNotNull(form.priority), status = ReminderStatus.Pending
                )
                repository.save(reminder)
                _uiState.update { it.copy(isSubmitting = false, saved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false,
                    generalError = e.message ?: "No se pudo guardar") }
            }
        }
    }

    companion object {
        fun factory(repository: ReminderRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CreateReminderViewModel(repository) as T
            }
    }
}
