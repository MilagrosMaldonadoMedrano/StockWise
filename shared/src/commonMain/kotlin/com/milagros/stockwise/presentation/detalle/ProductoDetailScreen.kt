package com.milagros.stockwise.presentation.detalle

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.milagros.stockwise.presentation.util.formatearPrecio
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import stockwise.shared.generated.resources.Res
import stockwise.shared.generated.resources.ic_add
import stockwise.shared.generated.resources.ic_arrow_back
<<<<<<< Updated upstream
=======
import stockwise.shared.generated.resources.ic_delete
import stockwise.shared.generated.resources.ic_edit
>>>>>>> Stashed changes
import stockwise.shared.generated.resources.ic_remove

// Versión "con estado": obtiene el ViewModel (pasándole el id) y observa su estado
@Composable
fun ProductoDetailScreen(
    productoId: String,
    onVolver: () -> Unit,
<<<<<<< Updated upstream
=======
    onEditar: () -> Unit,
    onEliminado: (nombre: String) -> Unit,
    mensajeNavegacion: String?,            // mensaje que llega de otra pantalla (ej. "Cambios guardados")
    onMensajeNavegacionMostrado: () -> Unit,
>>>>>>> Stashed changes
    viewModel: ProductoDetailViewModel = koinViewModel { parametersOf(productoId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProductoDetailContent(
        state = state,
        onVolver = onVolver,
        onEditar = onEditar,
        mensajeNavegacion = mensajeNavegacion,
        onMensajeNavegacionMostrado = onMensajeNavegacionMostrado,
        onReintentar = viewModel::cargarProductos,
        onIncrementar = viewModel::incrementarStock,
        onDecrementar = viewModel::decrementarStock,
        onMensajeMostrado = viewModel::onMensajeMostrado,
    )
}

// Versión "sin estado": solo dibuja lo que recibe
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoDetailContent(
    state: ProductoDetailUiState,
    onVolver: () -> Unit,
    onEditar: () -> Unit,
    mensajeNavegacion: String?,
    onMensajeNavegacionMostrado: () -> Unit,
    onReintentar: () -> Unit,
    onIncrementar: () -> Unit,
    onDecrementar: () -> Unit,
    onMensajeMostrado: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Muestra el mensaje una sola vez y avisa al ViewModel para que lo borre
    val mensaje = (state as? ProductoDetailUiState.Exito)?.mensaje
    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            snackbarHostState.showSnackbar(mensaje)
            onMensajeMostrado()
        }
    }
    LaunchedEffect(mensajeNavegacion) {
        if (mensajeNavegacion != null) {
            snackbarHostState.showSnackbar(mensajeNavegacion)
            onMensajeNavegacionMostrado()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalle") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(painterResource(Res.drawable.ic_arrow_back), contentDescription = "Volver")
                    }
                },
<<<<<<< Updated upstream
=======
                actions = {
                    // Solo se puede eliminar cuando el producto está cargado y no hay otra operación en curso
                    if (state is ProductoDetailUiState.Exito) {
                        IconButton(
                            onClick = onEditar,
                            enabled = !state.ajustando && !state.eliminando,
                        ) {
                            Icon(painterResource(Res.drawable.ic_edit), contentDescription = "Editar")
                        }
                        IconButton(
                            onClick = { mostrarConfirmacion = true },
                            enabled = !state.ajustando && !state.eliminando,
                        ) {
                            Icon(painterResource(Res.drawable.ic_delete), contentDescription = "Eliminar")
                        }
                    }
                },
>>>>>>> Stashed changes
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

                is ProductoDetailUiState.Exito -> ProductoDetalle(
                    producto = state.producto,
                    ajustando = state.ajustando,
                    onIncrementar = onIncrementar,
                    onDecrementar = onDecrementar,
                )
            }
        }
    }
}

@Composable
private fun ProductoDetalle(
    producto: Producto,
    ajustando: Boolean,
    onIncrementar: () -> Unit,
    onDecrementar: () -> Unit,
) {
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
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalIconButton(
                        onClick = onDecrementar,
                        enabled = !ajustando && producto.cantidad > 0,
                    ) {
                        Icon(painterResource(Res.drawable.ic_remove), contentDescription = "Restar 1")
                    }
                    FilledTonalIconButton(
                        onClick = onIncrementar,
                        enabled = !ajustando,
                    ) {
                        Icon(painterResource(Res.drawable.ic_add), contentDescription = "Sumar 1")
                    }
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
