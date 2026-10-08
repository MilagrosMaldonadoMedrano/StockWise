package com.milagros.stockwise.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Lo que LLEGA de Supabase (tiene id)
@Serializable
data class ProductoDto(
    val id: String? = null,      // null cuando todavía no se guardó
    val nombre: String,
    val sku: String? = null,
    val categoria: String? = null,
    val cantidad: Int,
    @SerialName("stock_minimo") val stockMinimo: Int,
    val precio: Double,
    val costo: Double = 0.0,
)

// Lo que ENVIAMOS al crear/editar (sin id: lo genera la base)
@Serializable
data class ProductoRequestDto(
    val nombre: String,
    val sku: String? = null,
    val categoria: String? = null,
    val cantidad: Int,
    @SerialName("stock_minimo") val stockMinimo: Int,
    val precio: Double,
    val costo: Double,
)

// Una fila de la vista estadisticas_productos
@Serializable
data class EstadisticaDto(
    val id: String,
    val nombre: String,
    val unidades: Long,
    val ingresos: Double,
    val ganancia: Double,
)