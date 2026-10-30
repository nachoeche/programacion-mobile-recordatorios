package com.ignacio.recordatorios.data

object RepositoryProvider {
    // Compartido entre pantallas mientras el proceso siga activo.
    val repository: ReminderRepository = InMemoryReminderRepository()
}
