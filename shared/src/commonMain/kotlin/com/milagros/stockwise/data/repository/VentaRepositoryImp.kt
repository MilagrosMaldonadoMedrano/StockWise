package com.milagros.stockwise.data.repository

import com.milagros.stockwise.data.remote.EstadisticaDto
import com.milagros.stockwise.data.remote.toDomain
import com.milagros.stockwise.domain.model.EstadisticaProducto
import com.milagros.stockwise.domain.repository.StockInsuficienteException
import com.milagros.stockwise.domain.repository.VentaRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class VentaRepositoryImp(
    private val client: SupabaseClient,
) : VentaRepository {

    override suspend fun registrarVenta(productoId: String, cantidad: Int) {
        try {
            // Llama a la función registrar_venta de la base (una transacción: stock + venta)
            client.postgrest.rpc(
                function = "registrar_venta",
                parameters = buildJsonObject {
                    put("p_producto_id", productoId)
                    put("p_cantidad", cantidad)
                },
            )
        } catch (e: PostgrestRestException) {
            // La función lanza 'STOCK_INSUFICIENTE' si otro dispositivo vendió antes
            if (e.message?.contains("STOCK_INSUFICIENTE") == true) throw StockInsuficienteException() else throw e
        }
    }

    override suspend fun getEstadisticas(): List<EstadisticaProducto> =
        client.from("estadisticas_productos")
            .select { order("ganancia", Order.DESCENDING) }
            .decodeList<EstadisticaDto>()
            .map { it.toDomain() }
}
