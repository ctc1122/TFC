# Módulo Empresa - Sistema de Gestión de Usuarios

Este módulo proporciona funcionalidades para la gestión de usuarios y veterinarios en el sistema. A continuación, se describe cada componente y su función.

## Estructura Simplificada

El módulo se ha simplificado para usar un modelo unificado de usuario que incluye tanto usuarios regulares como veterinarios. Esta estructura facilita el mantenimiento y reduce la duplicación de código.

### Clases principales:

1. **Usuario** (`com.example.pruebamongodbcss.Data.Usuario`)
   - Clase central para representar un usuario del sistema
   - Incluye todos los campos necesarios para cualquier tipo de usuario (regular o veterinario)
   - Contiene la enumeración `Rol` con los diferentes tipos: ADMINISTRADOR, VETERINARIO, RECEPCIONISTA, AUXILIAR, NORMAL
   - Gestiona la conversión desde/hacia documentos MongoDB

2. **ServicioUsuarios** (`com.example.pruebamongodbcss.Data.ServicioUsuarios`)
   - Servicio unificado para todas las operaciones relacionadas con usuarios
   - Proporciona métodos para autenticación, búsqueda, y operaciones CRUD
   - Maneja la reconexión con la base de datos en caso de problemas
   - Incluye funcionalidad para carga de datos de prueba

3. **GestionUsuariosController** (`com.example.pruebamongodbcss.Modulos.Empresa.GestionUsuariosController`)
   - Controlador principal para la interfaz gráfica de gestión de usuarios
   - Organizado en pestañas separadas para usuarios regulares y veterinarios
   - Incluye funcionalidades para búsqueda, creación, edición y eliminación

4. **RegistroUsuarioController** (`com.example.pruebamongodbcss.Modulos.Empresa.RegistroUsuarioController`)
   - Controlador para el formulario de registro de usuarios
   - Se utiliza tanto para crear nuevos usuarios como para editar existentes

5. **EmpresaMain** (`com.example.pruebamongodbcss.Modulos.Empresa.EmpresaMain`)
   - Punto de entrada principal para el módulo Empresa
   - Inicializa la interfaz de usuario para la gestión de usuarios

## Uso del módulo

### Gestión de usuarios

1. **Crear un nuevo usuario**:
   - Utilice `GestionUsuariosController` para acceder a la interfaz gráfica
   - Haga clic en "Nuevo Usuario" para abrir el formulario de registro
   - Complete los campos requeridos y guarde

2. **Crear un nuevo veterinario**:
   - Utilice `GestionUsuariosController` y vaya a la pestaña de Veterinarios
   - Haga clic en "Nuevo Veterinario" para abrir el formulario
   - Complete los campos, incluyendo especialidad y número de colegiado

3. **Editar un usuario**:
   - Seleccione un usuario de la lista y haga clic en "Editar" o doble clic
   - Modifique los campos necesarios y guarde los cambios

4. **Eliminar un usuario**:
   - Seleccione un usuario de la lista y haga clic en "Eliminar"
   - Confirme la acción cuando se solicite

5. **Resetear contraseña**:
   - Seleccione un usuario y haga clic en "Resetear Contraseña"
   - Ingrese la nueva contraseña cuando se solicite

### Programación

Si necesita utilizar estas clases en su código:

```java
// Obtener una instancia del servicio
ServicioUsuarios servicio = new ServicioUsuarios();

// Autenticar un usuario
Usuario usuario = servicio.autenticarUsuario("nombre_usuario", "contraseña");

// Buscar usuarios
List<Usuario> veterinarios = servicio.buscarUsuariosPorRol(Usuario.Rol.VETERINARIO);
List<Usuario> resultados = servicio.buscarUsuariosPorTexto("texto_busqueda");

// Crear un nuevo usuario
Usuario nuevoUsuario = new Usuario();
nuevoUsuario.setNombre("Nombre");
nuevoUsuario.setApellido("Apellido");
nuevoUsuario.setUsuario("nombre_usuario");
nuevoUsuario.setPassword("contraseña");
nuevoUsuario.setEmail("email@ejemplo.com");
nuevoUsuario.setTelefono("666123456");
nuevoUsuario.setRol(Usuario.Rol.NORMAL);
servicio.guardarUsuario(nuevoUsuario);

// Crear un veterinario
Usuario nuevoVeterinario = new Usuario();
// Completar campos básicos como en el ejemplo anterior
nuevoVeterinario.setRol(Usuario.Rol.VETERINARIO);
nuevoVeterinario.setEspecialidad("Cirugía");
nuevoVeterinario.setNumeroColegiado("VET12345");
nuevoVeterinario.setHoraInicio("09:00");
nuevoVeterinario.setHoraFin("17:00");
servicio.guardarUsuario(nuevoVeterinario);
```

## Notas sobre la implementación

1. **Modelo unificado**: Se ha eliminado la duplicación entre `Usuario` y `ModeloVeterinario`. Ahora, un veterinario es simplemente un `Usuario` con el rol VETERINARIO y campos adicionales completados.

2. **Servicio unificado**: `ServicioUsuarios` ahora maneja todas las operaciones, eliminando la necesidad de servicios separados.

3. **Gestión de conexión**: El servicio incluye funcionalidad para verificar y renovar la conexión a MongoDB si es necesario.

4. **Datos de prueba**: Puede cargar datos de prueba llamando a `servicio.cargarDatosPrueba()`. Esto solo cargará datos si la colección está vacía.

5. **Seguridad**: Solo los administradores pueden acceder al módulo de Empresa. Esto se verifica en `EmpresaMain`.

## Clases Eliminadas

Las siguientes clases han sido eliminadas del módulo Empresa para simplificar la arquitectura:

- `ModeloUsuario`: Sustituida por `Usuario`
- `ModeloVeterinario`: Sustituida por `Usuario` con rol VETERINARIO
- `ServicioModeloUsuario`: Sustituida por `ServicioUsuarios`
- `EmpresaController`: Sustituida por `GestionUsuariosController`
- `UsuarioFormController`: Sustituida por `RegistroUsuarioController`
- `VeterinarioFormController`: Sustituida por `RegistroUsuarioController`
- `CargarDatosAdmin` y `CargarDatosPrueba`: Funcionalidad integrada en `ServicioUsuarios`

Esta simplificación reduce la duplicación de código, mejora la mantenibilidad y facilita las operaciones de gestión de usuarios en el sistema. 