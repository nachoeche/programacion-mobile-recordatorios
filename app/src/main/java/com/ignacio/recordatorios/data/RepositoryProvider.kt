package com.ignacio.recordatorios.data

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.ignacio.recordatorios.data.local.ReminderDatabase
import com.ignacio.recordatorios.data.settings.PreferencesRepository

object RepositoryProvider {
    private val repositories = mutableMapOf<String, ReminderRepository>()
    @Volatile private var settings: PreferencesRepository? = null
    fun repository(context: Context, uid: String): ReminderRepository = synchronized(this) {
        repositories.getOrPut(uid) {
            CloudReminderRepository(ReminderDatabase.forUser(context, uid).reminderDao(),
                FirebaseFirestore.getInstance(), uid)
        }
    }
    fun preferences(context: Context): PreferencesRepository = settings ?: synchronized(this) {
        settings ?: PreferencesRepository(context.applicationContext).also { settings = it }
    }
}
