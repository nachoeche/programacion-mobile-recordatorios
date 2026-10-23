
package com.ignacio.recordatorios.model

sealed class Priority {
    data object Low : Priority()
    data object Medium : Priority()
    data object High : Priority()
}
