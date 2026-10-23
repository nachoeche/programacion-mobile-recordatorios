
package com.ignacio.recordatorios.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.ignacio.recordatorios.data.SampleData
import com.ignacio.recordatorios.model.Priority
import com.ignacio.recordatorios.model.Reminder
import com.ignacio.recordatorios.model.ReminderStatus

@Composable
fun ReminderScreen() {

    val reminders = SampleData.reminders

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Mis recordatorios",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${reminders.size} recordatorios registrados",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = reminders,
                key = { it.id }
            ) { reminder ->
                ReminderCard(reminder)
            }
        }
    }
}

@Composable
fun ReminderCard(reminder: Reminder) {

    val categoryColor = SampleData.categoryColors[
        reminder.category.name
    ]?.let { Color(it) } ?: Color.Gray

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = reminder.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = reminder.category.name,
                    color = categoryColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Prioridad: ${priorityLabel(reminder.priority)}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Estado: ${statusLabel(reminder.status)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun priorityLabel(priority: Priority): String {
    return when (priority) {
        Priority.Low -> "Baja"
        Priority.Medium -> "Media"
        Priority.High -> "Alta"
    }
}

private fun statusLabel(status: ReminderStatus): String {
    return when (status) {
        ReminderStatus.Pending -> "Pendiente"
        ReminderStatus.InProgress -> "En progreso"
        ReminderStatus.Completed -> "Completado"
    }
}
