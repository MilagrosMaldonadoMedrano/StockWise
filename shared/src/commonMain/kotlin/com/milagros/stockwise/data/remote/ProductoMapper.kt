package com.milagros.stockwise.data.remote

import com.milagros.stockwise.domain.model.EstadisticaMensual
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

fun EstadisticaMensualDto.toDomain() = EstadisticaMensual(
    productoId = productoId,
    nombre = nombre,
    mes = mes,
    unidades = unidades,
    ingresos = ingresos,
    ganancia = ganancia,
)
