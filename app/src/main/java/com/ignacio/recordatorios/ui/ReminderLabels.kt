package com.ignacio.recordatorios.ui

import com.ignacio.recordatorios.model.Priority
import com.ignacio.recordatorios.model.ReminderStatus

fun Priority.label(): String = when (this) {
    Priority.Low -> "Baja"
    Priority.Medium -> "Media"
    Priority.High -> "Alta"
}

fun ReminderStatus.label(): String = when (this) {
    ReminderStatus.Pending -> "Pendiente"
    ReminderStatus.InProgress -> "En progreso"
    ReminderStatus.Completed -> "Completado"
}
