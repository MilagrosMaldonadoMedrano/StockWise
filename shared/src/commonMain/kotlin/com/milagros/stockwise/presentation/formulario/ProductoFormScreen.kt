package com.milagros.stockwise.presentation.formulario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import stockwise.shared.generated.resources.Res
import stockwise.shared.generated.resources.ic_arrow_back

// Versión "con estado": obtiene el ViewModel (con el id si es edición) y observa su estado
@Composable
fun ProductoFormScreen(
    productoId: String?,
    onVolver: () -> Unit,
    onGuardado: (mensaje: String) -> Unit,
    viewModel: ProductoFormViewModel = koinViewModel { parametersOf(productoId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProductoFormContent(
        state = state,
        onVolver = onVolver,
        onCamposChange = viewModel::onCamposChange,
        onGuardar = viewModel::guardar,
        onReintentarCarga = viewModel::reintentarCarga,
        onGuardado = onGuardado,
        onMensajeMostrado = viewModel::onMensajeMostrado,
    )
}

// Versión "sin estado": solo dibuja lo que recibe
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoFormContent(
    state: ProductoFormUiState,
    onVolver: () -> Unit,
    onCamposChange: (ProductoFormCampos) -> Unit,
    onGuardar: () -> Unit,
    onReintentarCarga: () -> Unit,
    onGuardado: (mensaje: String) -> Unit,
    onMensajeMostrado: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.mensaje) {
        state.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            onMensajeMostrado()
        }
    }

    // Guardado OK: se avisa una sola vez para volver a la pantalla anterior
    state.resultado?.let { resultado ->
        LaunchedEffect(resultado) { onGuardado(resultado) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (state.esEdicion) "Editar producto" else "Nuevo producto") },
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
            when {
                state.cargando -> CircularProgressIndicator()

                state.errorCarga != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ocurrió un error", style = MaterialTheme.typography.titleMedium)
                    Text(state.errorCarga, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onReintentarCarga) { Text("Reintentar") }
                }

                else -> Formulario(state, onCamposChange, onGuardar)
            }
        }
    }
}

@Composable
private fun Formulario(
    state: ProductoFormUiState,
    onCamposChange: (ProductoFormCampos) -> Unit,
    onGuardar: () -> Unit,
) {
    val campos = state.campos
    val errores = state.errores
    val habilitado = !state.guardando
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding() // el teclado no tapa los campos de abajo
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CampoTexto(
            valor = campos.nombre,
            onValorChange = { onCamposChange(campos.copy(nombre = it)) },
            etiqueta = "Nombre *",
            error = errores.nombre,
            habilitado = habilitado,
            capitalizacion = KeyboardCapitalization.Sentences,
        )
        CampoTexto(
            valor = campos.sku,
            onValorChange = { onCamposChange(campos.copy(sku = it)) },
            etiqueta = "SKU (opcional)",
            error = errores.sku,
            habilitado = habilitado,
            capitalizacion = KeyboardCapitalization.Characters,
        )
        CampoTexto(
            valor = campos.categoria,
            onValorChange = { onCamposChange(campos.copy(categoria = it)) },
            etiqueta = "Categoría (opcional)",
            error = null,
            habilitado = habilitado,
            capitalizacion = KeyboardCapitalization.Sentences,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CampoTexto(
                valor = campos.cantidad,
                onValorChange = { onCamposChange(campos.copy(cantidad = it)) },
                etiqueta = "Cantidad *",
                error = errores.cantidad,
                habilitado = habilitado,
                tipoTeclado = KeyboardType.Number,
                modifier = Modifier.weight(1f),
            )
            CampoTexto(
                valor = campos.stockMinimo,
                onValorChange = { onCamposChange(campos.copy(stockMinimo = it)) },
                etiqueta = "Stock mínimo *",
                error = errores.stockMinimo,
                habilitado = habilitado,
                tipoTeclado = KeyboardType.Number,
                modifier = Modifier.weight(1f),
            )
        }
        CampoTexto(
            valor = campos.precio,
            onValorChange = { onCamposChange(campos.copy(precio = it)) },
            etiqueta = "Precio *",
            error = errores.precio,
            habilitado = habilitado,
            tipoTeclado = KeyboardType.Decimal,
            prefijo = "$ ",
            ultimo = true,
            onListo = {
                focusManager.clearFocus()
                onGuardar()
            },
        )

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                focusManager.clearFocus()
                onGuardar()
            },
            enabled = habilitado,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.guardando) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(if (state.esEdicion) "Guardar cambios" else "Crear producto")
            }
        }
    }
}

@Composable
private fun CampoTexto(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    error: String?,
    habilitado: Boolean,
    modifier: Modifier = Modifier.fillMaxWidth(),
    tipoTeclado: KeyboardType = KeyboardType.Text,
    capitalizacion: KeyboardCapitalization = KeyboardCapitalization.None,
    prefijo: String? = null,
    ultimo: Boolean = false,
    onListo: () -> Unit = {},
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(etiqueta) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        enabled = habilitado,
        singleLine = true,
        prefix = prefijo?.let { { Text(it) } },
        // Teclado numérico para números y botón "siguiente"/"listo" para moverse entre campos
        keyboardOptions = KeyboardOptions(
            keyboardType = tipoTeclado,
            capitalization = capitalizacion,
            imeAction = if (ultimo) ImeAction.Done else ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(onDone = { onListo() }),
        modifier = modifier,
    )
}
