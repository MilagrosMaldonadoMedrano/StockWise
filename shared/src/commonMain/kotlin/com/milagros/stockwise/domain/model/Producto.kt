package com.milagros.stockwise.domain.model

data class Producto(
    val id: String? = null,
    val nombre: String,
    val sku: String? = null,
    val categoria: String? = null,
    val cantidad: Int,
    val stockMinimo: Int,
    val precio: Double,
    val costo: Double = 0.0, // costo de compra por unidad
) {
    val tieneStockBajo: Boolean get() = cantidad <= stockMinimo

    // Ganancia por cada unidad vendida al precio actual
    val gananciaUnitaria: Double get() = precio - costo
}
