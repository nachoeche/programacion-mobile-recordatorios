package com.ignacio.recordatorios.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ignacio.recordatorios.data.ReminderRepository
import com.ignacio.recordatorios.model.Reminder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Data(val reminder: Reminder) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

class ReminderDetailViewModel(private val repository: ReminderRepository, private val id: Int) : ViewModel() {
    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState
    init { load() }
    fun load() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                val item = repository.getById(id)
                _uiState.value = if (item == null) DetailUiState.Error("No se encontró el recordatorio")
                else DetailUiState.Data(item)
            } catch (e: Exception) { _uiState.value = DetailUiState.Error(e.message ?: "Error de detalle") }
        }
    }
    companion object {
        fun factory(repo: ReminderRepository, id: Int) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = ReminderDetailViewModel(repo, id) as T
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailScreen(viewModel: ReminderDetailViewModel, onVolver: () -> Unit, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(topBar = { TopAppBar(title = { Text("Detalle") }, navigationIcon = {
        TextButton(onClick = onVolver) { Text("Volver") }
    }) }) { padding ->
        Column(modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (val s = state) {
                DetailUiState.Loading -> CircularProgressIndicator()
                is DetailUiState.Error -> StatusMessage(s.message, "Reintentar", viewModel::load)
                is DetailUiState.Data -> {
                    Text(s.reminder.title, style = MaterialTheme.typography.headlineMedium)
                    Text("ID: ${s.reminder.id}")
                    Text("Descripción: ${s.reminder.description ?: "Sin descripción"}")
                    Text("Categoría: ${s.reminder.category.name}")
                    Text("Prioridad: ${s.reminder.priority.label()}")
                    Text("Estado: ${s.reminder.status.label()}")
                    Text("Ubicación: ${s.reminder.location ?: "Sin ubicación"}")
                }
            }
        }
    }
}
