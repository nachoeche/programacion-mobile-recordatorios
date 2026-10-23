
package com.ignacio.recordatorios.model

sealed class ReminderStatus {
    data object Pending : ReminderStatus()
    data object InProgress : ReminderStatus()
    data object Completed : ReminderStatus()
}
