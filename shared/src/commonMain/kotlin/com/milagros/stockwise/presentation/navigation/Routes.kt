package com.milagros.stockwise.presentation.navigation

import kotlinx.serialization.Serializable

// Cada pantalla tiene una ruta type-safe: si cambia un parámetro, el compilador avisa.

@Serializable
data object ProductoListRoute

@Serializable
data class ProductoDetailRoute(val id: String)

// id == null -> crear un producto nuevo; con id -> editar ese producto
@Serializable
data class ProductoFormRoute(val id: String? = null)

@Serializable
data object EstadisticasRoute
