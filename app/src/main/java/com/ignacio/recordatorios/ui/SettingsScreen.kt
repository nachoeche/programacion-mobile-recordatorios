package com.ignacio.recordatorios.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ignacio.recordatorios.data.settings.SortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: SettingsViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    Scaffold(topBar = { TopAppBar(title = { Text("Preferencias") }, navigationIcon = {
        TextButton(onClick = onBack) { Text("Volver") }
    }) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Modo oscuro"); Switch(checked = state.darkMode, onCheckedChange = vm::dark)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Mostrar completados"); Switch(checked = state.showCompleted, onCheckedChange = vm::completed)
            }
            Text("Orden del listado")
            SortOrder.entries.forEach { option ->
                Row { RadioButton(selected = state.sortOrder == option, onClick = { vm.sort(option) });
                    Text(when(option) { SortOrder.NEWEST -> "Más nuevos"; SortOrder.OLDEST -> "Más antiguos"; SortOrder.TITLE -> "Título" },
                        Modifier.padding(top = 12.dp)) }
            }
        }
    }
}
