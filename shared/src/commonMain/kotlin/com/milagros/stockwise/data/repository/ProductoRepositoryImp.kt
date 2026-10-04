package com.milagros.stockwise.data.repository
import com.milagros.stockwise.domain.repository.ProductoRepository
import com.milagros.stockwise.data.remote.ProductoDto
import com.milagros.stockwise.data.remote.toDomain
import com.milagros.stockwise.data.remote.toRequest
import com.milagros.stockwise.domain.model.Producto
import io.github.jan.supabase.SupabaseClient
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
        val creado = table.insert(producto.toRequest()) { select() }
            .decodeSingle<ProductoDto>()
            .toDomain()
        _productos.update { lista -> lista?.plus(creado)?.ordenadaPorNombre() }
        return creado
    }

    override suspend fun updateProducto(producto: Producto): Producto {
        val id = requireNotNull(producto.id) { "No se puede editar un producto sin id" }
        val actualizado = table.update(producto.toRequest()) {
            select()
            filter { eq("id", id) }
        }.decodeSingle<ProductoDto>().toDomain()
        _productos.update { lista ->
            lista?.map { if (it.id == id) actualizado else it }?.ordenadaPorNombre()
        }
        return actualizado
    }

    override suspend fun deleteProducto(id: String) {
        table.delete { filter { eq("id", id) } }
        _productos.update { lista -> lista?.filterNot { it.id == id } }
    }

    // Mismo orden que la consulta a Supabase, para que la lista no "salte" al editar
    private fun List<Producto>.ordenadaPorNombre() = sortedBy { it.nombre.lowercase() }
}
