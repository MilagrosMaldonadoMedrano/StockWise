package com.milagros.stockwise.data.remote

import com.milagros.stockwise.domain.model.EstadisticaProducto
import com.milagros.stockwise.domain.model.Producto

fun ProductoDto.toDomain() = Producto(
    id = id,
    nombre = nombre,
    sku = sku,
    categoria = categoria,
    cantidad = cantidad,
    stockMinimo = stockMinimo,
    precio = precio,
    costo = costo,
)

fun Producto.toRequest() = ProductoRequestDto(
    nombre = nombre,
    sku = sku?.ifBlank { null },
    categoria = categoria?.ifBlank { null },
    cantidad = cantidad,
    stockMinimo = stockMinimo,
    precio = precio,
    costo = costo,
)

fun EstadisticaDto.toDomain() = EstadisticaProducto(
    productoId = id,
    nombre = nombre,
    unidades = unidades,
    ingresos = ingresos,
    ganancia = ganancia,
)
