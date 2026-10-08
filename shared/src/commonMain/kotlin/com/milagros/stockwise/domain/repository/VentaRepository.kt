package com.milagros.stockwise.domain.repository

import com.milagros.stockwise.domain.model.EstadisticaMensual

interface VentaRepository {
    // Descuenta stock y guarda la venta en una sola operación (todo o nada).
    // Lanza StockInsuficienteException si no alcanza el stock.
    suspend fun registrarVenta(productoId: String, cantidad: Int)

    // Una fila por producto y por mes con ventas
    suspend fun getEstadisticasMensuales(): List<EstadisticaMensual>
}

class StockInsuficienteException : IllegalStateException("No hay stock suficiente para esa venta")
