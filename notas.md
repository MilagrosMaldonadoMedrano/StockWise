// Seguridad
La tabla "productos" tiene RLS activado con una política abierta para permitir acceso sin login durante el challenge.  
En un entorno de producción se implementaría autenticación y políticas más restrictivas para proteger los datos.

/////
Se eligió MVVM (recomendada por Google) + Clean Architecture liviana para separar responsabilidades y facilitar testing.

La capa domain define contratos y modelos puros; data implementa esos contratos con Supabase;
presentation maneja estado y UI con Compose Multiplatform.
Esto permite cambiar la fuente de datos sin modificar la lógica de negocio ni la UI.



