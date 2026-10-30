package com.ignacio.recordatorios.data.settings

import android.content.Context
import java.io.IOException
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.reminderDataStore by preferencesDataStore(name = "user_settings")

class PreferencesRepository(private val context: Context) {
    private object Keys {
        val dark = booleanPreferencesKey("dark_mode")
        val completed = booleanPreferencesKey("show_completed")
        val sort = stringPreferencesKey("sort_order")
    }
    val preferences: Flow<UserPreferences> = context.reminderDataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { prefs ->
            UserPreferences(
                darkMode = prefs[Keys.dark] ?: false,
                showCompleted = prefs[Keys.completed] ?: true,
                sortOrder = SortOrder.entries.firstOrNull { it.name == prefs[Keys.sort] } ?: SortOrder.NEWEST
            )
        }
    suspend fun setDarkMode(value: Boolean) { context.reminderDataStore.edit { it[Keys.dark] = value } }
    suspend fun setShowCompleted(value: Boolean) { context.reminderDataStore.edit { it[Keys.completed] = value } }
    suspend fun setSortOrder(value: SortOrder) { context.reminderDataStore.edit { it[Keys.sort] = value.name } }
}
