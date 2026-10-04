package com.milagros.stockwise.presentation.util

import kotlin.test.Test
import kotlin.test.assertEquals

class FormatoTest {

    @Test
    fun precioConMiles_usaPuntoComoSeparador() {
        assertEquals("$ 6.200,00", 6200.0.formatearPrecio())
    }

    @Test
    fun precioConDecimales_usaComaYDosDecimales() {
        assertEquals("$ 1.234,50", 1234.5.formatearPrecio())
    }

    @Test
    fun precioChico_noAgregaSeparadorDeMiles() {
        assertEquals("$ 999,99", 999.99.formatearPrecio())
    }

    @Test
    fun precioCero() {
        assertEquals("$ 0,00", 0.0.formatearPrecio())
    }

    @Test
    fun precioMillonario_separaCadaTresCifras() {
        assertEquals("$ 1.500.000,00", 1_500_000.0.formatearPrecio())
    }
}
