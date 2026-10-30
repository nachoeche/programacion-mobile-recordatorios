package com.ignacio.recordatorios.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(viewModel: ReminderListViewModel, onCrearRecordatorio: () -> Unit,
    onRecordatorioClick: (Int) -> Unit, onSettings: () -> Unit, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val syncMessage by viewModel.syncMessage.collectAsStateWithLifecycle()
    Scaffold(topBar = { TopAppBar(title = { Text("Mis recordatorios") }, actions = {
        TextButton(onClick = onSettings) { Text("Ajustes") }
    }) }, floatingActionButton = { FloatingActionButton(onClick = onCrearRecordatorio) { Text("+") } }) { padding ->
        Column(modifier.fillMaxSize().padding(padding)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                TextButton(onClick = viewModel::simulateEmpty) { Text("Vacío") }
                TextButton(onClick = viewModel::simulateError) { Text("Error") }
                TextButton(onClick = viewModel::load) { Text("Restaurar") }
                TextButton(onClick = viewModel::sync) { Text("Sync") }
            }
            syncMessage?.let { Text(it, Modifier.padding(horizontal = 16.dp)) }
            when (val s = state) {
                ReminderListUiState.Loading -> CircularProgressIndicator(Modifier.padding(24.dp))
                ReminderListUiState.Empty -> StatusMessage("Todavía no hay recordatorios.", "Crear recordatorio", onCrearRecordatorio)
                is ReminderListUiState.Error -> StatusMessage(s.message, "Reintentar", viewModel::load)
                is ReminderListUiState.Data -> {
                    Text("${s.reminders.size} recordatorios registrados", Modifier.padding(16.dp))
                    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(s.reminders, key = { it.id }) { reminder ->
                            ReminderCard(reminder, onClick = { onRecordatorioClick(reminder.id) })
                        }
                    }
                }
            }
        }
    }
}
