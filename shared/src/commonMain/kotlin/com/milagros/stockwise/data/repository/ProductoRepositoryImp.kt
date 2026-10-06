package com.milagros.stockwise.data.repository
import com.milagros.stockwise.domain.repository.ProductoRepository
import com.milagros.stockwise.data.remote.ProductoDto
import com.milagros.stockwise.data.remote.toDomain
import com.milagros.stockwise.data.remote.toRequest
import com.milagros.stockwise.domain.model.Producto
import com.milagros.stockwise.domain.repository.SkuDuplicadoException
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProductoRepositoryImp(
    private val client: SupabaseClient,
) : ProductoRepository {

    // Nombre de la tabla en Supabase
    private val table get() = client.from("productos")

    // Copia en memoria de los productos; las pantallas la observan
    private val _productos = MutableStateFlow<List<Producto>?>(null)
    override val productos: StateFlow<List<Producto>?> = _productos.asStateFlow()

    override suspend fun refreshProductos() {
        _productos.value = table.select { order("nombre", Order.ASCENDING) }
            .decodeList<ProductoDto>()
            .map { it.toDomain() }
    }

    override suspend fun getProducto(id: String): Producto =
        table.select { filter { eq("id", id) } }
            .decodeSingle<ProductoDto>()
            .toDomain()

    override suspend fun createProducto(producto: Producto): Producto {
        val creado = traducirErrores(producto) {
            table.insert(producto.toRequest()) { select() }
                .decodeSingle<ProductoDto>()
                .toDomain()
        }
        _productos.update { lista -> lista?.plus(creado)?.ordenadaPorNombre() }
        return creado
    }

    override suspend fun updateProducto(producto: Producto): Producto {
        val id = requireNotNull(producto.id) { "No se puede editar un producto sin id" }
        val actualizado = traducirErrores(producto) {
            table.update(producto.toRequest()) {
                select()
                filter { eq("id", id) }
            }.decodeSingle<ProductoDto>().toDomain()
        }
        _productos.update { lista ->
            lista?.map { if (it.id == id) actualizado else it }?.ordenadaPorNombre()
        }
        return actualizado
    }

    override suspend fun deleteProducto(id: String) {
        table.delete { filter { eq("id", id) } }
        _productos.update { lista -> lista?.filterNot { it.id == id } }
    }

    // Traduce errores de Supabase a errores del dominio
    private suspend fun <T> traducirErrores(producto: Producto, bloque: suspend () -> T): T =
        try {
            bloque()
        } catch (e: PostgrestRestException) {
            // 23505 = código de Postgres para "valor duplicado en columna unique" (el sku)
            if (e.code == "23505") throw SkuDuplicadoException(producto.sku) else throw e
        }

    // Mismo orden que la consulta a Supabase, para que la lista no "salte" al editar
    private fun List<Producto>.ordenadaPorNombre() = sortedBy { it.nombre.lowercase() }
}
