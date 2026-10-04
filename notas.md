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