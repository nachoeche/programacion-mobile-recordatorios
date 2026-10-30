package com.ignacio.recordatorios.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ignacio.recordatorios.data.SampleData
import com.ignacio.recordatorios.model.Reminder

@Composable
fun ReminderCard(reminder: Reminder, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val color = SampleData.categoryColors[reminder.category.name]?.let { Color(it) }
        ?: MaterialTheme.colorScheme.primary
    Card(modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(reminder.title, fontWeight = FontWeight.Bold)
            Text(reminder.category.name, color = color)
            Text("Prioridad: ${reminder.priority.label()}")
            Text("Estado: ${reminder.status.label()}")
        }
    }
}

@Composable
fun StatusMessage(
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
        if (actionLabel != null && onAction != null) {
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}
