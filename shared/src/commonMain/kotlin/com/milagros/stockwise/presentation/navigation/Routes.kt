package com.milagros.stockwise.presentation.navigation

import kotlinx.serialization.Serializable

// Cada pantalla tiene una ruta type-safe: si cambia un parámetro, el compilador avisa.

@Serializable
data object ProductoListRoute

@Serializable
data class ProductoDetailRoute(val id: String)
