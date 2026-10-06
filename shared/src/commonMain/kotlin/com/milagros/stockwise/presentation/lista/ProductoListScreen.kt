package com.milagros.stockwise.presentation.lista

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.milagros.stockwise.domain.model.Producto
import org.koin.compose.viewmodel.koinViewModel

// Versión "con estado": obtiene el ViewModel y observa su estado
@Composable
fun ProductoListScreen(
    onProductoClick: (id: String) -> Unit,
    mensaje: String?,                // mensaje que llega de otra pantalla (ej. "producto eliminado")
    onMensajeMostrado: () -> Unit,
    viewModel: ProductoListViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProductoListContent(
        state = state,
        onReintentar = viewModel::cargarProductos,
        onProductoClick = onProductoClick,
        mensaje = mensaje,
        onMensajeMostrado = onMensajeMostrado,
    )
}

// Versión "sin estado": solo dibuja lo que recibe
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoListContent(
    state: ProductoListUiState,
    onReintentar: () -> Unit,
    onProductoClick: (id: String) -> Unit,
    mensaje: String?,
    onMensajeMostrado: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            snackbarHostState.showSnackbar(mensaje)
            onMensajeMostrado()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("StockWise") },
                actions = { TextButton(onClick = onReintentar) { Text("Actualizar") } },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                ProductoListUiState.Cargando -> CircularProgressIndicator()

                ProductoListUiState.Vacio -> Text("Todavía no hay productos cargados")

                is ProductoListUiState.Error -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ocurrió un error", style = MaterialTheme.typography.titleMedium)
                    Text(state.mensaje, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onReintentar) { Text("Reintentar") }
                }

                is ProductoListUiState.Exito -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.productos, key = { it.id ?: it.nombre }) { producto ->
                        ProductoCard(
                            producto = producto,
                            onClick = { producto.id?.let(onProductoClick) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductoCard(producto: Producto, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(producto.nombre, style = MaterialTheme.typography.titleMedium)
                producto.categoria?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${producto.cantidad} u.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (producto.tieneStockBajo) {
                    Text(
                        "Stock bajo",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}