
package com.ignacio.recordatorios.model

data class Category(
    val name: String
) {
    init {
        require(name.isNotBlank()) {
            "La categoria no puede estar vacia"
        }
    }
}