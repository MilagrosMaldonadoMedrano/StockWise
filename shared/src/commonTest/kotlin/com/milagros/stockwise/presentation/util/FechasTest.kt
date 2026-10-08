package com.milagros.stockwise.presentation.util

import kotlin.test.Test
import kotlin.test.assertEquals

class FechasTest {

    @Test
    fun mesLargoYCorto() {
        assertEquals("Octubre 2026", mesLargo("2026-10"))
        assertEquals("Ene", mesCorto("2027-01"))
    }

    @Test
    fun montoCompacto() {
        assertEquals("$850", 850.0.formatearCompacto())
        assertEquals("$1,7k", 1700.0.formatearCompacto())
        assertEquals("$12k", 12000.0.formatearCompacto())
        assertEquals("$2,5M", 2_500_000.0.formatearCompacto())
        assertEquals("-$1,2k", (-1200.0).formatearCompacto())
    }
}
