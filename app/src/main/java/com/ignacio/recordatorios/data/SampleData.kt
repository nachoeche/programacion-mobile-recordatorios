
package com.ignacio.recordatorios.data

import com.ignacio.recordatorios.model.Category
import com.ignacio.recordatorios.model.Priority
import com.ignacio.recordatorios.model.Reminder
import com.ignacio.recordatorios.model.ReminderStatus

object SampleData {

    // SET: Categorias sin duplicados
    val categories = setOf(
        Category("Personal"),
        Category("Estudio"),
        Category("Trabajo"),
        Category("Personal")
    )

    // MAP: Relacion entre categoria y color
    val categoryColors = mapOf(
        "Personal" to 0xFF2196F3,
        "Estudio" to 0xFF9C27B0,
        "Trabajo" to 0xFF4CAF50
    )

    // LIST: Recordatorios simulados
    val reminders = listOf(
        Reminder(
            id = 1,
            title = "Comprar alimentos",
            description = "Comprar frutas y verduras",
            category = Category("Personal"),
            priority = Priority.High,
            status = ReminderStatus.Pending
        ),
        Reminder(
            id = 2,
            title = "Estudiar Kotlin",
            description = "Repasar el modulo 1",
            category = Category("Estudio"),
            priority = Priority.Medium,
            status = ReminderStatus.InProgress
        ),
        Reminder(
            id = 3,
            title = "Enviar informe",
            category = Category("Trabajo"),
            priority = Priority.High,
            status = ReminderStatus.Completed
        ),
        Reminder(
            id = 4,
            title = "Hacer ejercicio",
            category = Category("Personal"),
            priority = Priority.Low,
            status = ReminderStatus.Pending
        ),
        Reminder(
            id = 5,
            title = "Preparar examen",
            category = Category("Estudio"),
            priority = Priority.High,
            status = ReminderStatus.Pending
        )
    )

    // Recordatorios pendientes
    val pendingReminders = reminders.filter {
        it.status == ReminderStatus.Pending
    }

    // Recordatorios de prioridad alta
    val highPriorityReminders = reminders.filter {
        it.priority == Priority.High
    }

    // Cantidad de recordatorios completados
    val completedCount = reminders.count {
        it.status == ReminderStatus.Completed
    }
}
