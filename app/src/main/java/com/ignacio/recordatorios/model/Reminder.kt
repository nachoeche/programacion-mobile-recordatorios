
package com.ignacio.recordatorios.model

data class Reminder(
    val id: Int,
    val title: String,
    val description: String? = null,
    val category: Category,
    val priority: Priority,
    val status: ReminderStatus,
    val location: String? = null
) {
    init {
        require(id > 0) {
            "El ID debe ser mayor a cero"
        }

        require(title.isNotBlank()) {
            "El titulo no puede estar vacio"
        }
    }
}
