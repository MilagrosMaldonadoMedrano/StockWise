package com.milagros.stockwise.presentation.formulario

import com.milagros.stockwise.domain.model.Producto

sealed interface ResultadoValidacion {
    data class Valido(val producto: Producto) : ResultadoValidacion
    data class Invalido(val errores: ProductoFormErrores) : ResultadoValidacion
}

const val NOMBRE_MAX = 100
const val PRECIO_MAX = 9_999_999_999.99 // límite de numeric(12,2) en la base

// Función pura (sin estado ni red): convierte el texto del formulario en un Producto
// o devuelve qué campos están mal. Por ser pura es muy fácil de testear.
fun validarProducto(campos: ProductoFormCampos, id: String? = null): ResultadoValidacion {
    val nombre = campos.nombre.trim()
    val cantidad = campos.cantidad.trim().toIntOrNull()
    val stockMinimo = campos.stockMinimo.trim().toIntOrNull()
    // Acepta coma o punto como separador decimal ("1234,50" o "1234.50")
    val precio = campos.precio.trim().replace(',', '.').toDoubleOrNull()

    val errores = ProductoFormErrores(
        nombre = when {
            nombre.isEmpty() -> "El nombre es obligatorio"
            nombre.length > NOMBRE_MAX -> "Máximo $NOMBRE_MAX caracteres"
            else -> null
        },
        cantidad = errorEnteroNoNegativo(campos.cantidad, cantidad),
        stockMinimo = errorEnteroNoNegativo(campos.stockMinimo, stockMinimo),
        precio = when {
            campos.precio.isBlank() -> "Obligatorio"
            precio == null -> "Ingresá un número (ej. 1234,50)"
            precio < 0 -> "No puede ser negativo"
            precio > PRECIO_MAX -> "Precio demasiado alto"
            else -> null
        },
    )
    if (errores.hayErrores) return ResultadoValidacion.Invalido(errores)

    return ResultadoValidacion.Valido(
        Producto(
            id = id,
            nombre = nombre,
            sku = campos.sku.trim().ifEmpty { null },
            categoria = campos.categoria.trim().ifEmpty { null },
            cantidad = cantidad!!,
            stockMinimo = stockMinimo!!,
            precio = precio!!,
        )
    )
}

private fun errorEnteroNoNegativo(texto: String, valor: Int?): String? = when {
    texto.isBlank() -> "Obligatorio"
    valor == null -> "Ingresá un número entero"
    valor < 0 -> "No puede ser negativo"
    else -> null
}

// Inverso: un Producto existente pasado a texto para cargar el formulario en modo edición
fun Producto.toCampos() = ProductoFormCampos(
    nombre = nombre,
    sku = sku.orEmpty(),
    categoria = categoria.orEmpty(),
    cantidad = cantidad.toString(),
    stockMinimo = stockMinimo.toString(),
    // 6200.0 -> "6200"; 1234.5 -> "1234,5"
    precio = if (precio % 1.0 == 0.0) precio.toLong().toString() else precio.toString().replace('.', ','),
)
