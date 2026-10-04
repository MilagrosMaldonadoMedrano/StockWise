package com.milagros.stockwise.presentation.util

import kotlin.math.roundToLong

// Formato argentino: 1234.5 -> "$ 1.234,50".
// No se usa String.format porque es de Java y no existe en iOS (Kotlin/Native).
fun Double.formatearPrecio(): String {
    val centavos = (this * 100).roundToLong()
    val entero = (centavos / 100).toString()
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()
    val decimales = (centavos % 100).toString().padStart(2, '0')
    return "$ $entero,$decimales"
}
