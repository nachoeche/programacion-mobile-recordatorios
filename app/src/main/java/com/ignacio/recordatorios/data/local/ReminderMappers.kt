package com.ignacio.recordatorios.data.local

import com.ignacio.recordatorios.model.*
import java.util.UUID

fun ReminderEntity.toDomain() = Reminder(
    id = id, title = title, description = description,
    category = Category(category),
    priority = when(priority) { "LOW" -> Priority.Low; "HIGH" -> Priority.High; else -> Priority.Medium },
    status = when(status) { "COMPLETED" -> ReminderStatus.Completed; "IN_PROGRESS" -> ReminderStatus.InProgress; else -> ReminderStatus.Pending },
    location = location
)
fun Reminder.toEntity(previous: ReminderEntity? = null) = ReminderEntity(
    id = previous?.id ?: 0,
    clientKey = previous?.clientKey ?: UUID.randomUUID().toString(),
    remoteId = previous?.remoteId,
    title = title, description = description, category = category.name,
    priority = when(priority) { Priority.Low -> "LOW"; Priority.Medium -> "MEDIUM"; Priority.High -> "HIGH" },
    status = when(status) { ReminderStatus.Pending -> "PENDING"; ReminderStatus.InProgress -> "IN_PROGRESS"; ReminderStatus.Completed -> "COMPLETED" },
    location = location,
    createdAt = previous?.createdAt ?: System.currentTimeMillis(),
    syncStatus = "PENDING"
)
