package com.ignacio.recordatorios.data

import com.ignacio.recordatorios.model.Reminder
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryReminderRepository : ReminderRepository {
    private val mutex = Mutex()
    private val reminders = SampleData.reminders.toMutableList()

    override fun observeAll(): Flow<List<Reminder>> = flow { emit(getAll()) }
    override fun observeById(id: Int): Flow<Reminder?> = flow { emit(getById(id)) }
    override suspend fun update(reminder: Reminder) { mutex.withLock { val idx = reminders.indexOfFirst { it.id == reminder.id }; if (idx >= 0) reminders[idx] = reminder } }
    override suspend fun delete(id: Int) { mutex.withLock { reminders.removeAll { it.id == id } } }
    override suspend fun sync(): SyncResult = SyncResult(0)
    override suspend fun getAll(): List<Reminder> {
        delay(550)
        return mutex.withLock { reminders.toList() }
    }

    override suspend fun getById(id: Int): Reminder? =
        mutex.withLock { reminders.firstOrNull { it.id == id } }

    override suspend fun nextId(): Int =
        mutex.withLock { (reminders.maxOfOrNull { it.id } ?: 0) + 1 }

    override suspend fun save(reminder: Reminder): Reminder {
        delay(650)
        mutex.withLock {
            require(reminders.none { it.id == reminder.id }) { "ID duplicado" }
            reminders.add(reminder)
        }
        return reminder
    }
}
