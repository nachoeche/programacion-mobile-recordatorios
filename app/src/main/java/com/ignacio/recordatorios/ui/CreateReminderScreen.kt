package com.ignacio.recordatorios.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ignacio.recordatorios.data.SampleData
import com.ignacio.recordatorios.model.Priority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReminderScreen(
    viewModel: CreateReminderViewModel,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var categoryMenu by remember { mutableStateOf(false) }
    var priorityMenu by remember { mutableStateOf(false) }
    LaunchedEffect(state.saved) { if (state.saved) onGuardar() }

    Scaffold(topBar = { TopAppBar(title = { Text("Nuevo recordatorio") }) }) { padding ->
        Column(modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(
                value = state.title, onValueChange = viewModel::titleChanged,
                modifier = Modifier.fillMaxWidth().then(if (state.attemptedSave && state.titleError != null)
                    Modifier.semantics { error(state.titleError!!) } else Modifier),
                label = { Text("Título") }, singleLine = true,
                isError = state.attemptedSave && state.titleError != null,
                supportingText = { if (state.attemptedSave && state.titleError != null) Text(state.titleError!!) }
            )
            OutlinedTextField(
                value = state.description, onValueChange = viewModel::descriptionChanged,
                modifier = Modifier.fillMaxWidth().then(if (state.descriptionError != null)
                    Modifier.semantics { error(state.descriptionError!!) } else Modifier),
                label = { Text("Descripción (opcional)") }, minLines = 3,
                isError = state.descriptionError != null,
                supportingText = { Text(state.descriptionError ?: "${state.description.length}/200 caracteres") }
            )
            ExposedDropdownMenuBox(expanded = categoryMenu, onExpandedChange = { categoryMenu = it }) {
                OutlinedTextField(value = state.category ?: "", onValueChange = {}, readOnly = true,
                    label = { Text("Categoría") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryMenu) },
                    isError = state.attemptedSave && state.categoryError != null,
                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable))
                ExposedDropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
                    SampleData.categories.forEach { cat -> DropdownMenuItem(text = { Text(cat.name) },
                        onClick = { viewModel.categoryChanged(cat.name); categoryMenu = false }) }
                }
            }
            if (state.attemptedSave && state.categoryError != null) Text(state.categoryError!!, color = MaterialTheme.colorScheme.error)
            ExposedDropdownMenuBox(expanded = priorityMenu, onExpandedChange = { priorityMenu = it }) {
                OutlinedTextField(value = state.priority?.label() ?: "", onValueChange = {}, readOnly = true,
                    label = { Text("Prioridad") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(priorityMenu) },
                    isError = state.attemptedSave && state.priorityError != null,
                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable))
                ExposedDropdownMenu(expanded = priorityMenu, onDismissRequest = { priorityMenu = false }) {
                    listOf(Priority.Low, Priority.Medium, Priority.High).forEach { p ->
                        DropdownMenuItem(text = { Text(p.label()) }, onClick = {
                            viewModel.priorityChanged(p); priorityMenu = false
                        })
                    }
                }
            }
            if (state.attemptedSave && state.priorityError != null) Text(state.priorityError!!, color = MaterialTheme.colorScheme.error)
            state.generalError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            TextButton(onClick = viewModel::simulateSaveFailure, enabled = !state.isSubmitting) {
                Text("Probar error de guardado")
            }
            if (state.isSubmitting) LinearProgressIndicator(Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onCancelar, enabled = !state.isSubmitting) { Text("Cancelar") }
                Button(onClick = viewModel::save, enabled = state.canSave) { Text("Guardar") }
            }
        }
    }
}
