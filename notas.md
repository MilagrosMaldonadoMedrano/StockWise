Seguridad
La tabla productos tiene Row Level Security (RLS) activado con una política abierta para permitir acceso sin autenticación durante el challenge. En un entorno de producción se implementaría autenticación y políticas más restrictivas para proteger los datos.
La anon key está incluida en el código a propósito: es pública por diseño y viaja dentro del APK. La seguridad real depende de las políticas RLS. La service_role key nunca se usa ni se publica en el repositorio.

Arquitectura
Se implementó MVVM (Model–View–ViewModel), recomendada por Google, junto con una Clean Architecture liviana para separar responsabilidades y facilitar el testing.

domain: define contratos y modelos puros.
data: implementa esos contratos usando Supabase.
presentation: maneja estado y UI con Compose Multiplatform.

Esto permite cambiar la fuente de datos sin modificar la lógica de negocio ni la interfaz.

Flujo unidireccional (UDF)
La pantalla envía eventos al ViewModel, y el ViewModel devuelve un único estado (StateFlow<UiState>). La UI nunca modifica datos directamente.

UiState se define como sealed interface con los estados: Cargando, Vacío, Éxito y Error. Esto evita estados contradictorios.

State hoisting: separación entre Screen y Content. Content no tiene estado, lo que facilita la previsualización y el testing.

Koin: el ViewModel depende de la interfaz del repositorio, no de Supabase. En los tests se inyecta un repositorio falso.

DTO y mappers: el DTO está separado del modelo de dominio. Si cambia la tabla en Supabase, solo se modifica la capa data. Se usa @SerialName para traducir snake_case a camelCase.

Stack técnico
Se eligió Supabase en lugar de Firebase porque el SDK de Firebase no es oficialmente multiplataforma para KMP, mientras que supabase-kt sí lo es. Además, Supabase usa PostgreSQL (SQL real) y soporta RLS.

Uso de IA
Durante el desarrollo se utilizó inteligencia artificial para diagnóstico y soporte técnico.

El Gradle Sync mostraba “failed” pese al mensaje “BUILD SUCCESSFUL”. El diagnóstico se obtuvo del archivo idea.log, donde se detectó que el Gradle wrapper se había actualizado automáticamente por sugerencia del IDE. Se revirtió a la versión anterior para estabilizar la compilación.



// CI (GitHub Actions)
Se configuró integración continua con GitHub Actions (.github/workflows/ci.yml).
En cada push a main se ejecutan dos trabajos en paralelo:
- Android (ubuntu-latest): compila el APK debug y lo publica como artifact descargable.
- iOS (macos-latest): compila y linkea el framework del módulo shared para el simulador.
  Motivo: el desarrollo se hizo en Windows, sin Mac. El CI es la forma de verificar
  que el código compartido compila para iOS (Kotlin/Native), donde no existen las APIs de Java.
  Limitación: la compilación iOS está verificada, pero la app no se probó visualmente en iPhone/simulador.

// Problemas encontrados (CI)
- gradlew estaba guardado en git sin permiso de ejecución (Windows no maneja ese permiso),
  lo que hace fallar el CI en Linux/Mac con "Permission denied".
  Se corrigió con: git update-index --chmod=+x gradlew
- Se usa el mismo JDK que el proyecto (Azul Zulu 21) para que el CI compile igual que localmente.

// Uso de IA (CI)
Claude Code generó el workflow. Antes de commitearlo se verificó que la tarea de iOS
(linkDebugFrameworkIosSimulatorArm64) existiera en el proyecto, y la IA detectó
el problema de permisos de gradlew antes de que el CI fallara.


// Única fuente de verdad (Single Source of Truth)
El repositorio guarda los productos en un StateFlow<List<Producto>?> y es la única fuente de verdad.
Es "single" en Koin: todas las pantallas comparten la misma instancia.
Create/update/delete actualizan esa lista después de confirmar en Supabase, así cualquier
pantalla que la observe se actualiza sola, sin volver a pedir todo a la red.
null = "todavía no se cargó" (spinner), lista vacía = "no hay productos".
El ViewModel deriva el UiState con combine() + stateIn(WhileSubscribed(5000)).

// Errores de IA corregidos / detectados
- El catch(e: Exception) original atrapaba CancellationException: mostraba errores falsos
  al salir de la pantalla y rompía la cancelación de corrutinas. Se re-lanza explícitamente.


// Parte 3: pantalla de detalle
- Ruta type-safe ProductoDetailRoute(id: String). El id llega al ViewModel con Koin: parametersOf(productoId).
- El detalle NO vuelve a consultar Supabase: busca el producto en el StateFlow del repositorio
  (única fuente de verdad). Abre al instante y se actualiza solo si otra pantalla lo modifica.
  Si la lista no está cargada (ej. Android restaura la app directo en el detalle), la pide.
- Protección contra doble toque: dropUnlessResumed en "volver" (evita sacar también la lista y
  dejar la pantalla vacía) y chequeo de lifecycle RESUMED antes de navegar al detalle.
- Precio formateado a mano (formatearPrecio) porque String.format es de Java y no existe en iOS.
- Íconos: la librería material-icons de Compose Multiplatform está discontinuada; se usa un
  vector XML propio en composeResources/drawable (con autoMirrored para idiomas RTL).

// QA manual
- Probado en emulador (Medium Phone API 37): lista → detalle → volver, doble toque en volver
  sin pantalla vacía, sin crashes en logcat.

// Uso de IA
- Claude Code instaló la app en el emulador vía adb, navegó tocando la pantalla y verificó con
  capturas y logcat que no hubiera crashes (incluido el caso de doble toque).
  // Parte 4: ajuste de stock
- AjustarStockUseCase en domain/usecase: regla de negocio "el stock nunca queda negativo".
  Lanza StockNegativoException sin ir a la red. La base también lo valida (check cantidad >= 0):
  defensa en profundidad.
- Mientras se guarda un ajuste, los botones se deshabilitan (evita updates duplicados por toques rápidos).
  El botón "-" se deshabilita con stock 0.
- Errores: el usuario ve mensajes amigables; los errores técnicos de Supabase (URL, headers) no se muestran.
- Snackbar: el mensaje vive en el UiState y se borra con onMensajeMostrado() (no se repite al rotar).
- Al ajustar en el detalle, la lista se actualiza sola: se confirma la decisión de única fuente de verdad.
- Limitación conocida: el update manda el producto completo. Si dos personas ajustan el mismo
  producto a la vez, gana la última. En producción se usaría un incremento atómico en la base (RPC).

// Errores de IA corregidos
- ProductoRequestDto (generado con IA) no tenía @SerialName("stock_minimo"): leer funcionaba,
  pero todo update fallaba con PGRST204. Se detectó al probar el ajuste de stock contra Supabase real.
- El primer manejo de errores mostraba e.message al usuario, que incluía URL y headers de la request.

// Tests
- FakeProductoRepository en commonTest: repositorio en memoria, sin red. Posible porque el
  caso de uso depende de la interfaz ProductoRepository (inyección de dependencias).
- 9 tests: AjustarStockUseCase (4) y formatearPrecio (5). Corren en Android (JVM) e iOS (Kotlin/Native) en CI.

// Git
- Desde la parte 4: una rama por funcionalidad (feat/...), Pull Request con descripción,
  CI verde antes de hacer merge a main.
  // Git: flujo de trabajo
- Hasta el detalle de producto (parte 3) los commits fueron directo a main.
- Desde el ajuste de stock (parte 4) se adopta feature branch workflow:
  una rama por funcionalidad (feat/..., fix/..., chore/...), Pull Request con descripción,
  CI verde (Android + iOS) antes de hacer merge a main, y borrado de la rama después del merge.
- Motivo: main siempre queda en un estado que compila y funciona, y cada cambio queda
  documentado y validado por CI en su PR.

<<<<<<< Updated upstream
=======
// Git: aprendizajes
- git switch falla si hay cambios sin commitear que el cambio de rama pisaría:
  git protege el trabajo en lugar de perderlo. Solución: git stash (guardar aparte),
  cambiar de rama, git stash pop (recuperar).
- No se puede borrar la rama en la que uno está parado.
- Después de mergear un PR en GitHub, hay que actualizar main local (git switch main + git pull)
  ANTES de crear la rama siguiente, para que salga del main actualizado.



## Qué incluye
- Botón eliminar en el detalle con diálogo de confirmación (Material 3).
- Al eliminar: vuelve a la lista, el producto desaparece sin recargar (única fuente de verdad)
  y se muestra un snackbar con el nombre del producto eliminado.
- El mensaje entre pantallas usa el SavedStateHandle de la pantalla anterior (patrón oficial de navigation).
- Si falla la red: no navega, muestra el error y conserva el producto.
- Nuevo estado Eliminado en el UiState del detalle, para no mostrar "producto no encontrado" como error.

## Cómo se probó
- 4 tests nuevos de ProductoDetailViewModel (13 en total, pasando).
- Emulador: cancelar, rotar con el diálogo abierto, eliminar un producto de prueba, error de red.


// Parte 5: eliminar producto
- Botón eliminar en el detalle + AlertDialog de confirmación (un borrado no se puede deshacer).
- El estado "diálogo abierto" vive en la UI (rememberSaveable), no en el ViewModel:
  es estado puramente visual, no de negocio. Sobrevive a la rotación.
- Nuevo estado Eliminado(nombre) en ProductoDetailUiState. Problema: al borrar, el repositorio saca
  el producto de su lista y el detalle mostraba "no existe" como error. Solución: el ViewModel recuerda
  qué producto está eliminando; si desaparece de la lista y lo estaba eliminando, el estado es Eliminado.
- Snackbar en la lista tras eliminar: patrón "devolver un resultado" de navigation.
  El detalle escribe el mensaje en previousBackStackEntry.savedStateHandle y la lista lo observa
  con getStateFlow; al mostrarlo lo borra para que no se repita.
- Sin caso de uso para eliminar: no hay regla de negocio, el ViewModel usa el repositorio directo.
- Si la red falla: no navega, snackbar de error, el producto se conserva.

// Tests
- Primeros tests de ViewModel (ProductoDetailViewModelTest, 4 tests).
- Dispatchers.setMain(UnconfinedTestDispatcher()): viewModelScope usa Main, que no existe en tests.
- uiState usa WhileSubscribed: en el test hay que observarlo (backgroundScope.launch { collect {} })
  para que se calcule.

// Uso de IA
- La IA generó los tests con backgroundScope.launch sin dispatcher: el collector no arrancaba antes
  de las aserciones (el estado quedaba en Cargando). Se corrigió con UnconfinedTestDispatcher(testScheduler)
  antes de correrlos.
- Para no borrar datos reales, la prueba de eliminación se hizo con un producto de prueba creado
  en el Table Editor de Supabase.
>>>>>>> Stashed changes

## Qué incluye
- Pantalla de formulario reutilizada para crear (FAB "+" en la lista) y editar (lápiz en el detalle).
- Validación con función pura: obligatorios, enteros no negativos, precio con coma o punto, nombre ≤ 100.
- Errores por campo; se muestran tras el primer intento de guardar y se actualizan al escribir.
- SKU duplicado: Postgres 23505 se traduce en el repositorio a SkuDuplicadoException (dominio)
  y se muestra debajo del campo SKU.
- Al guardar vuelve a la pantalla anterior con snackbar ("creado" / "Cambios guardados").
- Teclado numérico, navegación entre campos con el teclado e imePadding.

## Cómo se probó
- 15 tests nuevos (28 en total), pasando.
- Emulador contra Supabase: validación, SKU duplicado, crear, editar y eliminar un producto de prueba.


// Parte 6: formulario crear/editar
- Una sola pantalla y ViewModel para crear y editar: ProductoFormRoute(id: String? = null).
  id null = crear; con id = editar (carga los datos desde la única fuente de verdad).
- El formulario es un "borrador": tiene su propio MutableStateFlow y no deriva del repositorio.
  Lo escrito no afecta la lista compartida hasta guardar.
- Estado como data class (no sealed): en un formulario varias cosas pasan a la vez
  (editando + errores + guardando).
- Campos guardados como texto y convertidos al validar (así se puede mostrar "12a" con error).
- Validación como función pura validarProducto() -> Valido(producto) | Invalido(errores).
  Acepta coma o punto en el precio; SKU/categoría vacíos -> null; límite de numeric(12,2).
- UX: errores visibles recién tras el primer intento de guardar, luego en vivo.
  Teclado numérico, "siguiente" entre campos, "listo" guarda, imePadding para el teclado.
- SKU duplicado: Postgres devuelve 23505 (unique violation). El repositorio (data) lo traduce a
  SkuDuplicadoException (domain): presentation no conoce Supabase. Se muestra debajo del campo SKU
  y se mantiene hasta que se cambie el SKU.
- Navegación: helpers volverConMensaje / mensajeRecibido / siEstaActiva para no repetir
  el patrón de "devolver un resultado" en 3 pantallas.

// Tests
- 15 nuevos (28 propios en total). FakeProductoRepository ahora imita la base:
  genera ids y rechaza SKUs duplicados.

// QA
- Probado en emulador contra Supabase real: validación, SKU duplicado (CF-002), crear, editar
  categoría, eliminar. Se usó un producto de prueba que se borró al final.

// Uso de IA
- Antes de escribir el manejo del SKU duplicado, Claude Code verificó dentro del .aar de
  supabase-kt que PostgrestRestException expone "code", en lugar de suponer la API.