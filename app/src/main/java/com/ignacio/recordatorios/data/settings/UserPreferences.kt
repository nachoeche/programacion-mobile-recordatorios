package com.ignacio.recordatorios.data.settings

data class UserPreferences(
    val darkMode: Boolean = false,
    val showCompleted: Boolean = true,
    val sortOrder: SortOrder = SortOrder.NEWEST
)
enum class SortOrder { NEWEST, OLDEST, TITLE }
