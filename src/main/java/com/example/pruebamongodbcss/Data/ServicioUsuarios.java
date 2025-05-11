package com.example.pruebamongodbcss.Data;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;

import Utilidades.GestorConexion;

/**
 * Servicio unificado para gestionar todos los usuarios del sistema.
 * Esta clase proporciona métodos para realizar operaciones CRUD sobre usuarios,
 * incluyendo veterinarios y otros roles, además de funcionalidades específicas
 * como autenticación, búsqueda por criterios y carga de datos de prueba.
 */
public class ServicioUsuarios {

    private MongoDatabase empresaDB;
    private MongoCollection<Document> usuariosCollection;
    private Usuario usuarioActual;

    /**
     * Constructor del servicio de usuarios.
     * Inicializa la conexión con la base de datos y asegura que exista la colección de usuarios.
     */
    public ServicioUsuarios() {
        try {
            // Conectar a la base de datos
            this.empresaDB = GestorConexion.conectarEmpresa();
            
            // Asegurar que existen las colecciones de usuarios
            List<String> collectionNames = new ArrayList<>();
            empresaDB.listCollectionNames().into(collectionNames);
            
            if (!collectionNames.contains("usuarios")) {
                empresaDB.createCollection("usuarios");
            }
            
            // Obtener referencias a las colecciones
            this.usuariosCollection = empresaDB.getCollection("usuarios");
        } catch (Exception e) {
            System.err.println("Error al inicializar ServicioUsuarios: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Establece el usuario actual de la sesión.
     * @param usuario Usuario a establecer como actual
     */
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }
    
    /**
     * Obtiene el usuario actual de la sesión.
     * @return Usuario actual
     */
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }
    
    /**
     * Verifica si el usuario actual es administrador.
     * @return true si el usuario actual es administrador
     */
    public boolean esUsuarioAdmin() {
        return usuarioActual != null && usuarioActual.esAdmin();
    }
    
    /**
     * Autenticar un usuario con nombre de usuario y contraseña.
     * @param nombreUsuario Nombre de usuario
     * @param password Contraseña
     * @return Usuario autenticado o null si no se encuentra
     */
    public Usuario autenticarUsuario(String nombreUsuario, String password) {
        // Verificar y renovar la conexión si es necesario
        renovarConexionSiNecesario();
        
        Document query = new Document("usuario", nombreUsuario)
                        .append("password", password)
                        .append("activo", true);
        
        Document doc = usuariosCollection.find(query).first();
        
        if (doc != null) {
            try {
                Usuario usuario = new Usuario(doc);
                return usuario;
            } catch (Exception e) {
                System.err.println("Error al autenticar usuario: " + e.getMessage());
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * Guardar un usuario en la base de datos.
     * Si el usuario no tiene ID, se crea uno nuevo.
     * Si ya tiene ID, se actualiza el existente.
     * @param usuario Usuario a guardar
     * @return ID del usuario guardado
     */
    public ObjectId guardarUsuario(Usuario usuario) {
        // Verificar y renovar la conexión si es necesario
        renovarConexionSiNecesario();
        
        Document doc = usuario.toDocument();
        
        if (usuario.getId() == null) {
            // Nuevo usuario
            usuariosCollection.insertOne(doc);
            if (doc.containsKey("_id")) {
                usuario.setId(doc.getObjectId("_id"));
                return usuario.getId();
            }
        } else {
            // Actualizar usuario
            Bson filter = Filters.eq("_id", usuario.getId());
            usuariosCollection.replaceOne(filter, doc);
            return usuario.getId();
        }
        
        return null;
    }
    
    /**
     * Verificar si un nombre de usuario ya existe en la base de datos.
     * @param nombreUsuario Nombre de usuario a verificar
     * @return true si el usuario ya existe
     */
    public boolean existeUsuario(String nombreUsuario) {
        // Verificar y renovar la conexión si es necesario
        renovarConexionSiNecesario();
        
        Document query = new Document("usuario", nombreUsuario);
        return usuariosCollection.countDocuments(query) > 0;
    }
    
    /**
     * Obtener un usuario por su ID.
     * @param id ID del usuario
     * @return Usuario encontrado o null
     */
    public Usuario obtenerUsuarioPorId(ObjectId id) {
        // Verificar y renovar la conexión si es necesario
        renovarConexionSiNecesario();
        
        Document doc = usuariosCollection.find(Filters.eq("_id", id)).first();
        if (doc != null) {
            try {
                return new Usuario(doc);
            } catch (Exception e) {
                System.err.println("Error al obtener usuario por ID: " + e.getMessage());
            }
        }
        return null;
    }
    
    /**
     * Buscar usuarios por rol.
     * @param rol Rol a buscar
     * @return Lista de usuarios con el rol especificado
     */
    public List<Usuario> buscarUsuariosPorRol(Usuario.Rol rol) {
        return buscarUsuarios(Filters.eq("rol", rol.name()));
    }
    
    /**
     * Buscar veterinarios.
     * @return Lista de usuarios con rol VETERINARIO
     */
    public List<Usuario> buscarVeterinarios() {
        return buscarUsuarios(Filters.eq("rol", Usuario.Rol.VETERINARIO.name()));
    }
    
    /**
     * Buscar usuarios activos.
     * @return Lista de usuarios activos
     */
    public List<Usuario> buscarUsuariosActivos() {
        return buscarUsuarios(Filters.eq("activo", true));
    }
    
    /**
     * Buscar veterinarios por especialidad.
     * @param especialidad Especialidad a buscar
     * @return Lista de veterinarios con la especialidad indicada
     */
    public List<Usuario> buscarVeterinariosPorEspecialidad(String especialidad) {
        if (especialidad == null || especialidad.trim().isEmpty()) {
            return buscarVeterinarios();
        }
        
        Bson filtro = Filters.and(
            Filters.eq("rol", Usuario.Rol.VETERINARIO.name()),
            Filters.regex("especialidad", ".*" + especialidad + ".*", "i")
        );
        return buscarUsuarios(filtro);
    }
    
    /**
     * Buscar usuarios con un filtro personalizado.
     * @param filtro Filtro a aplicar
     * @return Lista de usuarios que cumplen el filtro
     */
    public List<Usuario> buscarUsuarios(Bson filtro) {
        // Verificar y renovar la conexión si es necesario
        renovarConexionSiNecesario();
        
        List<Usuario> usuarios = new ArrayList<>();
        
        try {
            FindIterable<Document> findIterable = usuariosCollection.find(filtro);
            MongoCursor<Document> cursor = findIterable.iterator();
            
            while (cursor.hasNext()) {
                try {
                    usuarios.add(new Usuario(cursor.next()));
                } catch (Exception e) {
                    System.err.println("Error al crear usuario desde documento: " + e.getMessage());
                }
            }
            cursor.close();
        } catch (Exception e) {
            System.err.println("Error al buscar usuarios: " + e.getMessage());
            e.printStackTrace();
            
            // Intentar reiniciar la conexión y reintentar
            try {
                reiniciarConexion();
                FindIterable<Document> findIterable = usuariosCollection.find(filtro);
                try (MongoCursor<Document> cursor = findIterable.iterator()) {
                    while (cursor.hasNext()) {
                        try {
                            usuarios.add(new Usuario(cursor.next()));
                        } catch (Exception ex) {
                            System.err.println("Error al crear usuario en segundo intento: " + ex.getMessage());
                        }
                    }
                }
            } catch (Exception e2) {
                System.err.println("Error en segundo intento de búsqueda: " + e2.getMessage());
            }
        }
        
        return usuarios;
    }
    
    /**
     * Obtener todos los usuarios de la base de datos.
     * @return Lista con todos los usuarios
     */
    public List<Usuario> obtenerTodosUsuarios() {
        return buscarUsuarios(new Document());
    }
    
    /**
     * Eliminar un usuario por su ID.
     * @param id ID del usuario a eliminar
     * @return true si el usuario fue eliminado correctamente
     */
    public boolean eliminarUsuario(ObjectId id) {
        // Verificar y renovar la conexión si es necesario
        renovarConexionSiNecesario();
        
        Bson filter = Filters.eq("_id", id);
        DeleteResult result = usuariosCollection.deleteOne(filter);
        return result.wasAcknowledged() && result.getDeletedCount() > 0;
    }
    
    /**
     * Resetear la contraseña de un usuario.
     * @param id ID del usuario
     * @param nuevaContrasena Nueva contraseña
     * @return true si la contraseña fue actualizada correctamente
     */
    public boolean resetearContrasena(ObjectId id, String nuevaContrasena) {
        try {
            // Verificar y renovar la conexión si es necesario
            renovarConexionSiNecesario();
            
            Document update = new Document("$set", new Document("password", nuevaContrasena));
            usuariosCollection.updateOne(Filters.eq("_id", id), update);
            return true;
        } catch (Exception e) {
            System.err.println("Error al resetear contraseña: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Activar o desactivar un usuario.
     * @param id ID del usuario
     * @param activo Estado de activación (true/false)
     * @return true si el estado fue actualizado correctamente
     */
    public boolean cambiarEstadoUsuario(ObjectId id, boolean activo) {
        try {
            // Verificar y renovar la conexión si es necesario
            renovarConexionSiNecesario();
            
            Document update = new Document("$set", new Document("activo", activo));
            usuariosCollection.updateOne(Filters.eq("_id", id), update);
            return true;
        } catch (Exception e) {
            System.err.println("Error al cambiar estado del usuario: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Buscar usuarios por texto en nombre, apellido o usuario.
     * @param texto Texto a buscar
     * @return Lista de usuarios que coinciden con el texto
     */
    public List<Usuario> buscarUsuariosPorTexto(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return obtenerTodosUsuarios();
        }
        
        Bson filtro = Filters.or(
            Filters.regex("nombre", ".*" + texto + ".*", "i"),
            Filters.regex("apellido", ".*" + texto + ".*", "i"),
            Filters.regex("usuario", ".*" + texto + ".*", "i")
        );
        return buscarUsuarios(filtro);
    }
    
    /**
     * Carga datos de prueba (usuarios con diferentes roles).
     * Solo carga datos si la colección de usuarios está vacía.
     */
    public void cargarDatosPrueba() {
        try {
            System.out.println("Iniciando carga de datos de prueba...");
            
            // Verificar y renovar la conexión si es necesario
            renovarConexionSiNecesario();
            
            System.out.println("Conectado a la base de datos: " + empresaDB.getName());
            
            // Verificar si ya existen usuarios
            long countUsuarios = 0;
            try {
                countUsuarios = usuariosCollection.countDocuments();
                System.out.println("Documentos en colección usuarios: " + countUsuarios);
            } catch (Exception e) {
                System.err.println("Error al contar documentos: " + e.getMessage());
                // Intentar renovar la conexión una vez más
                renovarConexionSiNecesario();
                // Reintentar la operación
                countUsuarios = usuariosCollection.countDocuments();
                System.out.println("Documentos en colección usuarios (reintento): " + countUsuarios);
            }
            
            // Verificar si ya existen usuarios
            if (countUsuarios > 0) {
                System.out.println("Los datos de prueba ya están cargados. Omitiendo...");
                return;
            }
            
            System.out.println("Cargando datos de prueba...");
            
            // Crear administrador
            try {
                Usuario admin = new Usuario();
                admin.setNombre("Administrador");
                admin.setApellido("Sistema");
                admin.setUsuario("admin");
                admin.setPassword("admin");
                admin.setEmail("admin@clinica.com");
                admin.setTelefono("666555444");
                admin.setRol(Usuario.Rol.ADMINISTRADOR);
                admin.setActivo(true);
                ObjectId adminId = guardarUsuario(admin);
                System.out.println("Administrador creado con ID: " + (adminId != null ? adminId.toString() : "null"));
            } catch (Exception e) {
                System.err.println("Error al crear administrador: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Crear veterinarios
            crearVeterinarioPrueba(
                "Carlos", "Rodríguez López", "carlos", "carlos",
                "carlos@clinica.com", "666234567", "Cirugía", "VET12345", "09:00", "17:00"
            );
            
            crearVeterinarioPrueba(
                "Ana", "García Martín", "ana", "ana",
                "ana@clinica.com", "666345678", "Dermatología", "VET54321", "10:00", "18:00"
            );
            
            // Crear recepcionista
            Usuario recepcionista = new Usuario(
                "Laura", 
                "Sánchez", 
                "laura", 
                "laura", 
                "laura@clinica.com", 
                "666456789"
            );
            recepcionista.setRol(Usuario.Rol.RECEPCIONISTA);
            guardarUsuario(recepcionista);
            
            // Crear auxiliar
            Usuario auxiliar = new Usuario(
                "Juan", 
                "Pérez", 
                "juan", 
                "juan", 
                "juan@clinica.com", 
                "666567890"
            );
            auxiliar.setRol(Usuario.Rol.AUXILIAR);
            guardarUsuario(auxiliar);
            
            // Verificar que los usuarios se han creado correctamente
            List<Usuario> usuarios = obtenerTodosUsuarios();
            System.out.println("Total usuarios creados: " + usuarios.size());
            for (Usuario u : usuarios) {
                System.out.println("Usuario: " + u.getUsuario() + ", Rol: " + u.getRol() + ", ID: " + u.getId());
            }
            
            System.out.println("Datos de prueba cargados correctamente");
        } catch (Exception e) {
            System.err.println("Error al cargar datos de prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Método auxiliar para crear un veterinario de prueba.
     */
    private ObjectId crearVeterinarioPrueba(String nombre, String apellido, String usuario, String password,
                                         String email, String telefono, String especialidad, String numeroColegiado,
                                         String horaInicio, String horaFin) {
        try {
            Usuario veterinario = new Usuario();
            veterinario.setNombre(nombre);
            veterinario.setApellido(apellido);
            veterinario.setUsuario(usuario);
            veterinario.setPassword(password);
            veterinario.setEmail(email);
            veterinario.setTelefono(telefono);
            veterinario.setRol(Usuario.Rol.VETERINARIO);
            veterinario.setEspecialidad(especialidad);
            veterinario.setNumeroColegiado(numeroColegiado);
            veterinario.setHoraInicio(horaInicio);
            veterinario.setHoraFin(horaFin);
            veterinario.setDisponible(true);
            veterinario.setActivo(true);
            
            return guardarUsuario(veterinario);
        } catch (Exception e) {
            System.err.println("Error al crear veterinario de prueba: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Verifica la conexión a MongoDB y la renueva si es necesario.
     * @return true si la conexión es válida (después de intentar renovarla si fuera necesario)
     */
    public boolean verificarConexion() {
        try {
            // Intentar una operación simple para verificar la conexión
            empresaDB.runCommand(new Document("ping", 1));
            return true;
        } catch (Exception e) {
            System.err.println("Error al verificar conexión a MongoDB: " + e.getMessage());
            
            // Intentar renovar la conexión
            try {
                reiniciarConexion();
                // Verificar de nuevo
                empresaDB.runCommand(new Document("ping", 1));
                return true;
            } catch (Exception e2) {
                System.err.println("No se pudo restablecer la conexión: " + e2.getMessage());
                return false;
            }
        }
    }
    
    /**
     * Obtiene acceso a la colección de usuarios
     */
    public MongoCollection<Document> getUsuariosCollection() {
        return usuariosCollection;
    }
    
    /**
     * Reinicia la conexión a MongoDB si hay problemas
     */
    public void reiniciarConexion() {
        try {
            System.out.println("Reiniciando conexión a MongoDB...");
            
            // Cerrar la conexión actual completamente
            GestorConexion.cerrarConexion();
            
            // Esperar un momento para que las conexiones se cierren correctamente
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Volver a conectar
            this.empresaDB = GestorConexion.conectarEmpresa();
            
            // Actualizar las referencias a las colecciones
            this.usuariosCollection = empresaDB.getCollection("usuarios");
            
            System.out.println("Conexión a MongoDB reiniciada correctamente");
        } catch (Exception e) {
            System.err.println("Error al reiniciar conexión a MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Verifica que la conexión esté activa y la renueva si es necesario.
     */
    private void renovarConexionSiNecesario() {
        try {
            // Verificar si la conexión es válida intentando ejecutar un comando simple
            empresaDB.runCommand(new Document("ping", 1));
        } catch (Exception e) {
            System.err.println("La conexión a MongoDB no es válida. Intentando renovar...");
            try {
                // Intentar cerrar la conexión existente
                GestorConexion.cerrarConexion();
                
                // Renovar la conexión
                this.empresaDB = GestorConexion.conectarEmpresa();
                this.usuariosCollection = empresaDB.getCollection("usuarios");
                
                System.out.println("Conexión renovada correctamente.");
            } catch (Exception e2) {
                System.err.println("Error al renovar la conexión: " + e2.getMessage());
                throw e2; // Re-lanzar para manejo superior
            }
        }
    }
} 