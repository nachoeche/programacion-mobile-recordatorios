package com.ignacio.recordatorios.data

import com.ignacio.recordatorios.model.Reminder
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryReminderRepository : ReminderRepository {
    private val mutex = Mutex()
    private val reminders = SampleData.reminders.toMutableList()

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
