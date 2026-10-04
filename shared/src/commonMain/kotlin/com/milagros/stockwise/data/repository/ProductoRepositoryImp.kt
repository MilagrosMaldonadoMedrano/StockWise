package com.milagros.stockwise.data.repository
import com.milagros.stockwise.domain.repository.ProductoRepository
import com.milagros.stockwise.data.remote.ProductoDto
import com.milagros.stockwise.data.remote.toDomain
import com.milagros.stockwise.data.remote.toRequest
import com.milagros.stockwise.domain.model.Producto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class ProductoRepositoryImp(
    private val client: SupabaseClient,
) : ProductoRepository {

    // Nombre de la tabla en Supabase
    private val table get() = client.from("productos")

    override suspend fun getProductos(): List<Producto> =
        table.select { order("nombre", Order.ASCENDING) }
            .decodeList<ProductoDto>()
            .map { it.toDomain() }

    override suspend fun getProducto(id: String): Producto =
        table.select { filter { eq("id", id) } }
            .decodeSingle<ProductoDto>()
            .toDomain()

    override suspend fun createProducto(producto: Producto): Producto =
        table.insert(producto.toRequest()) { select() }
            .decodeSingle<ProductoDto>()
            .toDomain()

    override suspend fun updateProducto(producto: Producto): Producto {
        val id = requireNotNull(producto.id) { "No se puede editar un producto sin id" }
        return table.update(producto.toRequest()) {
            select()
            filter { eq("id", id) }
        }.decodeSingle<ProductoDto>().toDomain()
    }

    override suspend fun deleteProducto(id: String) {
        table.delete { filter { eq("id", id) } }
    }
}
