package com.ignacio.recordatorios.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "reminders", indices = [Index(value = ["clientKey"], unique = true)])
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientKey: String,
    val remoteId: Int? = null,
    val title: String,
    val description: String?,
    val category: String,
    val priority: String,
    val status: String,
    val location: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING",
    val deleted: Boolean = false
)
