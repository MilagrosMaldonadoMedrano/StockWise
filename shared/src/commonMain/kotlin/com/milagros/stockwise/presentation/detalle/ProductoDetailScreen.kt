package com.milagros.stockwise.presentation.detalle

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.milagros.stockwise.domain.model.Producto
import com.milagros.stockwise.presentation.util.formatearPrecio
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import stockwise.shared.generated.resources.Res
import stockwise.shared.generated.resources.ic_arrow_back

// Versión "con estado": obtiene el ViewModel (pasándole el id) y observa su estado
@Composable
fun ProductoDetailScreen(
    productoId: String,
    onVolver: () -> Unit,
    viewModel: ProductoDetailViewModel = koinViewModel { parametersOf(productoId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProductoDetailContent(
        state = state,
        onVolver = onVolver,
        onReintentar = viewModel::cargarProductos,
    )
}

// Versión "sin estado": solo dibuja lo que recibe
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoDetailContent(
    state: ProductoDetailUiState,
    onVolver: () -> Unit,
    onReintentar: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(painterResource(Res.drawable.ic_arrow_back), contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                ProductoDetailUiState.Cargando -> CircularProgressIndicator()

                is ProductoDetailUiState.Error -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ocurrió un error", style = MaterialTheme.typography.titleMedium)
                    Text(state.mensaje, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onReintentar) { Text("Reintentar") }
                }

                is ProductoDetailUiState.Exito -> ProductoDetalle(state.producto)
            }
        }
    }
}

@Composable
private fun ProductoDetalle(producto: Producto) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column {
            Text(producto.nombre, style = MaterialTheme.typography.headlineMedium)
            producto.categoria?.let {
                Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Stock: el dato principal de la pantalla
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Stock actual", style = MaterialTheme.typography.labelLarge)
                Text(
                    "${producto.cantidad} u.",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                )
                if (producto.tieneStockBajo) {
                    Text(
                        "Stock bajo (mínimo: ${producto.stockMinimo} u.)",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DatoFila("Precio", producto.precio.formatearPrecio())
                DatoFila("Stock mínimo", "${producto.stockMinimo} u.")
                DatoFila("SKU", producto.sku ?: "—")
            }
        }
    }
}

@Composable
private fun DatoFila(etiqueta: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(etiqueta, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, fontWeight = FontWeight.Medium)
    }
}
