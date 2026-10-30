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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditReminderViewModel(private val repo: ReminderRepository, private val id: Int): ViewModel() {
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title
    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    init { viewModelScope.launch { val item = repo.getById(id); if(item != null) {
        _title.value = item.title; _description.value = item.description ?: ""
    } else _error.value = "No se encontró el recordatorio" } }
    fun title(v: String) { _title.value = v }
    fun description(v: String) { _description.value = v }
    fun save(onSaved: () -> Unit) { viewModelScope.launch {
        if (_title.value.trim().length < 4 || _description.value.length > 200) {
            _error.value = "Título mínimo 4 caracteres y descripción máximo 200"; return@launch
        }
        try { val item = repo.getById(id) ?: error("No existe el recordatorio")
            repo.update(item.copy(title = _title.value.trim(), description = _description.value.trim().ifEmpty { null }))
            onSaved()
        } catch(e: Exception) { _error.value = e.message ?: "No se pudo guardar" }
    } }
    companion object { fun factory(repo: ReminderRepository, id: Int) = object: ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST") override fun <T: ViewModel> create(modelClass: Class<T>): T = EditReminderViewModel(repo, id) as T
    } }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun EditReminderScreen(vm: EditReminderViewModel, onDone: () -> Unit, onCancel: () -> Unit) {
    val title by vm.title.collectAsStateWithLifecycle()
    val description by vm.description.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    Scaffold(topBar = { TopAppBar(title = { Text("Editar recordatorio") }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = title, onValueChange = vm::title, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = vm::description,
                label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onCancel) { Text("Cancelar") }
                Button(onClick = { vm.save(onDone) }, enabled = title.trim().length >= 4 && description.length <= 200) { Text("Guardar") }
            }
        }
    }
}
