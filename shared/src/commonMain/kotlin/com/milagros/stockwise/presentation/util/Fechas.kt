package com.milagros.stockwise.presentation.util

import kotlin.math.abs
import kotlin.math.roundToLong

private val MESES = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre",
)

// "2026-10" -> "Octubre 2026"
fun mesLargo(mes: String): String =
    "${MESES.getOrElse(mes.substring(5, 7).toInt() - 1) { "?" }} ${mes.substring(0, 4)}"

// "2026-10" -> "Oct"
fun mesCorto(mes: String): String = mesLargo(mes).take(3)

// Monto compacto para etiquetas chicas: 1700.0 -> "$1,7k"; 2500000.0 -> "$2,5M"; 850.0 -> "$850"
fun Double.formatearCompacto(): String {
    val signo = if (this < 0) "-" else ""
    val valor = abs(this)
    val (base, sufijo) = when {
        valor >= 1_000_000 -> valor / 1_000_000 to "M"
        valor >= 1_000 -> valor / 1_000 to "k"
        else -> return "$signo$${valor.roundToLong()}"
    }
    val decimas = (base * 10).roundToLong()
    val texto = if (decimas % 10 == 0L) "${decimas / 10}" else "${decimas / 10},${decimas % 10}"
    return "$signo$$texto$sufijo"
}
