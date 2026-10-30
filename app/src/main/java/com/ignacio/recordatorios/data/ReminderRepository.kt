package com.ignacio.recordatorios.data

import com.ignacio.recordatorios.model.Reminder

interface ReminderRepository {
    suspend fun getAll(): List<Reminder>
    suspend fun getById(id: Int): Reminder?
    suspend fun save(reminder: Reminder): Reminder
    suspend fun nextId(): Int
}
