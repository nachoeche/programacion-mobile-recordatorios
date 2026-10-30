package com.ignacio.recordatorios.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ReminderEntity::class], version = 1, exportSchema = false)
abstract class ReminderDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    companion object {
        private val instances = mutableMapOf<String, ReminderDatabase>()
        fun forUser(context: Context, uid: String): ReminderDatabase = synchronized(this) {
            require(uid.matches(Regex("[A-Za-z0-9_-]{1,128}"))) { "Identificador de cuenta invalido" }
            instances.getOrPut(uid) {
                Room.databaseBuilder(context.applicationContext, ReminderDatabase::class.java,
                    "recordatorios-user-$uid.db").build()
            }
        }
    }
}
