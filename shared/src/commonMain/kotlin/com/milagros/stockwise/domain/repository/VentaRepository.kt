package com.milagros.stockwise.domain.repository

import com.milagros.stockwise.domain.model.EstadisticaProducto

interface VentaRepository {
    // Descuenta stock y guarda la venta en una sola operación (todo o nada).
    // Lanza StockInsuficienteException si no alcanza el stock.
    suspend fun registrarVenta(productoId: String, cantidad: Int)

    // Una fila por producto vendido
    suspend fun getEstadisticas(): List<EstadisticaProducto>
}

class StockInsuficienteException : IllegalStateException("No hay stock suficiente para esa venta")
