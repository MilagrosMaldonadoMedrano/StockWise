package com.milagros.stockwise.presentation.estadisticas

import com.milagros.stockwise.domain.model.EstadisticaMensual
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DashboardTest {

    private fun fila(id: String, nombre: String, mes: String, ganancia: Double, unidades: Long = 1) =
        EstadisticaMensual(id, nombre, mes, unidades = unidades, ingresos = ganancia * 2, ganancia = ganancia)

    @Test
    fun graficoTieneSeisMesesConsecutivosHastaElUltimoConVentas() {
        val dashboard = armarDashboard(listOf(fila("1", "Café", "2026-10", 100.0)), mesSeleccionado = null)

        assertEquals(
            listOf("2026-05", "2026-06", "2026-07", "2026-08", "2026-09", "2026-10"),
            dashboard.meses.map { it.mes },
        )
    }

    @Test
    fun mesesSinVentasAparecenEnCero() {
        val filas = listOf(fila("1", "Café", "2026-07", 100.0), fila("1", "Café", "2026-10", 300.0))

        val meses = armarDashboard(filas, null).meses.associateBy { it.mes }

        assertEquals(0.0, meses.getValue("2026-08").ganancia)
        assertEquals(0.0, meses.getValue("2026-09").ganancia)
        assertEquals(300.0, meses.getValue("2026-10").ganancia)
    }

    @Test
    fun cruzaElCambioDeAnio() {
        val dashboard = armarDashboard(listOf(fila("1", "Café", "2027-02", 100.0)), null)

        assertEquals(
            listOf("2026-09", "2026-10", "2026-11", "2026-12", "2027-01", "2027-02"),
            dashboard.meses.map { it.mes },
        )
    }

    @Test
    fun barraDelMesSumaTodosLosProductos() {
        val filas = listOf(fila("1", "Café", "2026-10", 100.0), fila("2", "Yerba", "2026-10", 250.0))

        val octubre = armarDashboard(filas, null).meses.last()

        assertEquals(350.0, octubre.ganancia)
        assertEquals(2L, octubre.unidades)
    }

    @Test
    fun rankingDeTodosLosMesesSumaPorProductoYOrdenaPorGanancia() {
        val filas = listOf(
            fila("1", "Café", "2026-09", 100.0),
            fila("1", "Café", "2026-10", 100.0),
            fila("2", "Yerba", "2026-10", 150.0),
        )

        val ranking = armarDashboard(filas, null).productos

        assertEquals(listOf("Café", "Yerba"), ranking.map { it.nombre })
        assertEquals(200.0, ranking.first().ganancia)
    }

    @Test
    fun mesSinVentasDejaElRankingVacio() {
        val dashboard = armarDashboard(listOf(fila("1", "Café", "2026-10", 100.0)), mesSeleccionado = "2026-08")

        assertTrue(dashboard.productos.isEmpty())
        assertEquals(0.0, dashboard.gananciaTotal)
    }
}
