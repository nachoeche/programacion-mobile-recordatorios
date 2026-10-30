package com.ignacio.recordatorios.data

import android.content.Context
import com.ignacio.recordatorios.BuildConfig
import com.ignacio.recordatorios.data.local.ReminderDatabase
import com.ignacio.recordatorios.data.remote.RemoteClient
import com.ignacio.recordatorios.data.settings.PreferencesRepository

object RepositoryProvider {
    @Volatile private var current: ReminderRepository? = null
    @Volatile private var settings: PreferencesRepository? = null
    fun repository(context: Context): ReminderRepository = current ?: synchronized(this) {
        current ?: RoomReminderRepository(ReminderDatabase.get(context).reminderDao(),
            RemoteClient.create(BuildConfig.API_BASE_URL)).also { current = it }
    }
    fun preferences(context: Context): PreferencesRepository = settings ?: synchronized(this) {
        settings ?: PreferencesRepository(context.applicationContext).also { settings = it }
    }
}
