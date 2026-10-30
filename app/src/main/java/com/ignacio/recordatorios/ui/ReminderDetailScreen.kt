package com.ignacio.recordatorios.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ignacio.recordatorios.data.ReminderRepository
import com.ignacio.recordatorios.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    data object Loading: DetailUiState
    data class Data(val reminder: Reminder): DetailUiState
    data class Error(val message: String): DetailUiState
}
class ReminderDetailViewModel(private val repo: ReminderRepository, private val id: Int): ViewModel() {
    val uiState: StateFlow<DetailUiState> = repo.observeById(id)
        .map { if (it == null) DetailUiState.Error("No se encontró el recordatorio") else DetailUiState.Data(it) as DetailUiState }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DetailUiState.Loading)
    fun delete(onDone: () -> Unit) { viewModelScope.launch { repo.delete(id); onDone() } }
    fun changeStatus(status: ReminderStatus) { viewModelScope.launch {
        val item = repo.getById(id) ?: return@launch
        repo.update(item.copy(status = status))
    } }
    companion object { fun factory(repo: ReminderRepository, id: Int) = object: ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST") override fun <T: ViewModel> create(modelClass: Class<T>): T = ReminderDetailViewModel(repo, id) as T
    } }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailScreen(viewModel: ReminderDetailViewModel, onVolver: () -> Unit,
    onEditar: (Int) -> Unit, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(topBar = { TopAppBar(title = { Text("Detalle") }, navigationIcon = {
        TextButton(onClick = onVolver) { Text("Volver") }
    }) }) { pad ->
        Column(modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when(val s = state) {
                DetailUiState.Loading -> CircularProgressIndicator()
                is DetailUiState.Error -> Text(s.message)
                is DetailUiState.Data -> {
                    Text(s.reminder.title, style = MaterialTheme.typography.headlineMedium)
                    Text("ID: ${s.reminder.id}")
                    Text("Descripción: ${s.reminder.description ?: "Sin descripción"}")
                    Text("Categoría: ${s.reminder.category.name}")
                    Text("Prioridad: ${s.reminder.priority.label()}")
                    Text("Estado: ${s.reminder.status.label()}")
                    Text("Ubicación: ${s.reminder.location ?: "Sin ubicación"}")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onEditar(s.reminder.id) }) { Text("Editar") }
                        OutlinedButton(onClick = { viewModel.delete(onVolver) }) { Text("Eliminar") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { viewModel.changeStatus(ReminderStatus.Pending) }) { Text("Pendiente") }
                        TextButton(onClick = { viewModel.changeStatus(ReminderStatus.InProgress) }) { Text("En curso") }
                        TextButton(onClick = { viewModel.changeStatus(ReminderStatus.Completed) }) { Text("Completar") }
                    }
                }
            }
        }
    }
}
