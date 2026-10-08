package com.milagros.stockwise.presentation.estadisticas

import com.milagros.stockwise.domain.model.EstadisticaMensual
import com.milagros.stockwise.domain.model.EstadisticaProducto

// Totales de un mes (una barra del gráfico)
data class ResumenMes(
    val mes: String, // "YYYY-MM"
    val ingresos: Double,
    val ganancia: Double,
    val unidades: Long,
)

const val MESES_EN_GRAFICO = 6

// Función pura: arma el dashboard a partir de las filas producto x mes y el mes elegido (null = todos).
fun armarDashboard(filas: List<EstadisticaMensual>, mesSeleccionado: String?): EstadisticasUiState.Exito {
    val porMes = filas.groupBy { it.mes }.mapValues { (mes, delMes) ->
        ResumenMes(mes, delMes.sumOf { it.ingresos }, delMes.sumOf { it.ganancia }, delMes.sumOf { it.unidades })
    }

    val delPeriodo = if (mesSeleccionado == null) filas else filas.filter { it.mes == mesSeleccionado }
    val ranking = delPeriodo
        .groupBy { it.productoId }
        .map { (id, delProducto) ->
            EstadisticaProducto(
                productoId = id,
                nombre = delProducto.first().nombre,
                unidades = delProducto.sumOf { it.unidades },
                ingresos = delProducto.sumOf { it.ingresos },
                ganancia = delProducto.sumOf { it.ganancia },
            )
        }
        .sortedByDescending { it.ganancia }

    return EstadisticasUiState.Exito(
        meses = ultimosMeses(porMes),
        mesSeleccionado = mesSeleccionado,
        productos = ranking,
    )
}

// Los últimos 6 meses consecutivos hasta el más reciente con ventas.
// Los meses sin ventas aparecen en 0: en un eje de tiempo, saltear meses distorsiona la lectura.
internal fun ultimosMeses(porMes: Map<String, ResumenMes>): List<ResumenMes> {
    val ultimo = porMes.keys.maxOrNull() ?: return emptyList()
    var anio = ultimo.substring(0, 4).toInt()
    var mes = ultimo.substring(5, 7).toInt()
    val resultado = ArrayDeque<ResumenMes>()
    repeat(MESES_EN_GRAFICO) {
        val clave = "$anio-${mes.toString().padStart(2, '0')}"
        resultado.addFirst(porMes[clave] ?: ResumenMes(clave, 0.0, 0.0, 0))
        mes -= 1
        if (mes == 0) {
            mes = 12
            anio -= 1
        }
    }
    return resultado.toList()
}
