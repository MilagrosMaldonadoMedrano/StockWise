package com.milagros.stockwise.domain.model

data class Producto(
    val id: String? = null,
    val nombre: String,
    val sku: String? = null,
    val categoria: String? = null,
    val cantidad: Int,
    val stockMinimo: Int,
    val precio: Double,
) {
    val isLowStock: Boolean get() = cantidad <= stockMinimo
}