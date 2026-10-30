package com.ignacio.recordatorios.data

import com.ignacio.recordatorios.model.Reminder
import kotlinx.coroutines.flow.Flow

data class SyncResult(val sent: Int, val error: String? = null)

interface ReminderRepository {
    fun observeAll(): Flow<List<Reminder>>
    fun observeById(id: Int): Flow<Reminder?>
    suspend fun getAll(): List<Reminder>
    suspend fun getById(id: Int): Reminder?
    suspend fun nextId(): Int
    suspend fun save(reminder: Reminder): Reminder
    suspend fun update(reminder: Reminder)
    suspend fun delete(id: Int)
    suspend fun sync(): SyncResult
}
