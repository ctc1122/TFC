package com.example.pruebamongodbcss.Data;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.example.pruebamongodbcss.Modulos.Empresa.ModeloVeterinario;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;

import Utilidades.GestorConexion;

/**
 * Servicio unificado para gestionar todos los usuarios y veterinarios del sistema
 */
public class ServicioUsuarios {

    private MongoDatabase empresaDB;
    private MongoCollection<Document> usuariosCollection;
    private MongoCollection<Document> veterinariosCollection;
    private Usuario usuarioActual;

    /**
     * Constructor del servicio de usuarios
     */
    public ServicioUsuarios() {
        // Conectar a la base de datos
        this.empresaDB = GestorConexion.conectarEmpresa();
        
        // Asegurar que existen las colecciones de usuarios y veterinarios
        List<String> collectionNames = new ArrayList<>();
        empresaDB.listCollectionNames().into(collectionNames);
        
        if (!collectionNames.contains("usuarios")) {
            empresaDB.createCollection("usuarios");
        }
        if (!collectionNames.contains("veterinarios")) {
            empresaDB.createCollection("veterinarios");
        }
        
        // Obtener referencias a las colecciones
        this.usuariosCollection = empresaDB.getCollection("usuarios");
        this.veterinariosCollection = empresaDB.getCollection("veterinarios");
    }

    /**
     * Establece el usuario actual de la sesión
     */
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }
    
    /**
     * Obtiene el usuario actual de la sesión
     */
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }
    
    /**
     * Verifica si el usuario actual es administrador
     */
    public boolean esUsuarioAdmin() {
        return usuarioActual != null && usuarioActual.esAdmin();
    }
    
    /**
     * Autenticar un usuario
     */
    public Usuario autenticarUsuario(String nombreUsuario, String password) {
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
     * Guardar un usuario en la base de datos
     */
    public ObjectId guardarUsuario(Usuario usuario) {
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
     * Verificar si un nombre de usuario ya existe
     */
    public boolean existeUsuario(String nombreUsuario) {
        Document query = new Document("usuario", nombreUsuario);
        return usuariosCollection.countDocuments(query) > 0;
    }
    
    /**
     * Obtener un usuario por su ID
     */
    public Usuario obtenerUsuarioPorId(ObjectId id) {
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
     * Buscar usuarios por rol
     */
    public List<Usuario> buscarUsuariosPorRol(Usuario.Rol rol) {
        return buscarUsuarios(Filters.eq("rol", rol.name()));
    }
    
    /**
     * Buscar veterinarios
     */
    public List<Usuario> buscarVeterinarios() {
        return buscarUsuarios(Filters.eq("rol", Usuario.Rol.VETERINARIO.name()));
    }
    
    /**
     * Buscar usuarios activos
     */
    public List<Usuario> buscarUsuariosActivos() {
        return buscarUsuarios(Filters.eq("activo", true));
    }
    
    /**
     * Buscar usuarios con un filtro personalizado
     */
    public List<Usuario> buscarUsuarios(Bson filtro) {
        List<Usuario> usuarios = new ArrayList<>();
        
        FindIterable<Document> findIterable = usuariosCollection.find(filtro);
        MongoCursor<Document> cursor = findIterable.iterator();
        
        try {
            while (cursor.hasNext()) {
                try {
                    usuarios.add(new Usuario(cursor.next()));
                } catch (Exception e) {
                    System.err.println("Error al crear usuario desde documento: " + e.getMessage());
                }
            }
        } finally {
            cursor.close();
        }
        
        return usuarios;
    }
    
    /**
     * Obtener todos los usuarios
     */
    public List<Usuario> obtenerTodosUsuarios() {
        return buscarUsuarios(new Document());
    }
    
    /**
     * Eliminar un usuario
     */
    public boolean eliminarUsuario(ObjectId id) {
        Bson filter = Filters.eq("_id", id);
        DeleteResult result = usuariosCollection.deleteOne(filter);
        return result.wasAcknowledged() && result.getDeletedCount() > 0;
    }
    
    /**
     * Resetear la contraseña de un usuario
     */
    public boolean resetearContrasena(ObjectId id, String nuevaContrasena) {
        try {
            Document update = new Document("$set", new Document("password", nuevaContrasena));
            usuariosCollection.updateOne(Filters.eq("_id", id), update);
            return true;
        } catch (Exception e) {
            System.err.println("Error al resetear contraseña: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Activar o desactivar un usuario
     */
    public boolean cambiarEstadoUsuario(ObjectId id, boolean activo) {
        try {
            Document update = new Document("$set", new Document("activo", activo));
            usuariosCollection.updateOne(Filters.eq("_id", id), update);
            return true;
        } catch (Exception e) {
            System.err.println("Error al cambiar estado del usuario: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Buscar usuarios por nombre, apellido o usuario
     */
    public List<Usuario> buscarUsuariosPorTexto(String texto) {
        Bson filtro = Filters.or(
            Filters.regex("nombre", ".*" + texto + ".*", "i"),
            Filters.regex("apellido", ".*" + texto + ".*", "i"),
            Filters.regex("usuario", ".*" + texto + ".*", "i")
        );
        return buscarUsuarios(filtro);
    }
    
    /**
     * Buscar veterinarios por especialidad
     */
    public List<Usuario> buscarUsuariosVeterinariosPorEspecialidad(String especialidad) {
        Bson filtro = Filters.and(
            Filters.eq("rol", Usuario.Rol.VETERINARIO.name()),
            Filters.regex("especialidad", ".*" + especialidad + ".*", "i")
        );
        return buscarUsuarios(filtro);
    }
    
    /**
     * Verifica que la conexión esté activa y la renueva si es necesario
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
                this.veterinariosCollection = empresaDB.getCollection("veterinarios");
                
                System.out.println("Conexión renovada correctamente.");
            } catch (Exception e2) {
                System.err.println("Error al renovar la conexión: " + e2.getMessage());
                throw e2; // Re-lanzar para manejo superior
            }
        }
    }

    /**
     * Cargar datos de prueba (usuarios y veterinarios)
     */
    public void cargarDatosPrueba() {
        try {
            System.out.println("Iniciando carga de datos de prueba...");
            
            // Verificar y renovar la conexión si es necesario
            renovarConexionSiNecesario();
            
            System.out.println("Conectado a la base de datos: " + empresaDB.getName());
            
            // Verificar conexión a la colección
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
            ModeloVeterinario vet1 = new ModeloVeterinario();
            vet1.setNombre("Carlos");
            vet1.setApellidos("Rodríguez López");
            vet1.setDni("12345678A");
            vet1.setNumeroTitulo("VET12345");
            vet1.setEspecialidad("Cirugía");
            vet1.setEmail("carlos@clinica.com");
            vet1.setTelefono("666234567");
            
            ObjectId vet1Id = guardarVeterinario(vet1);
            
            ModeloVeterinario vet2 = new ModeloVeterinario();
            vet2.setNombre("Ana");
            vet2.setApellidos("García Martín");
            vet2.setDni("87654321B");
            vet2.setNumeroTitulo("VET54321");
            vet2.setEspecialidad("Dermatología");
            vet2.setEmail("ana@clinica.com");
            vet2.setTelefono("666345678");
            
            ObjectId vet2Id = guardarVeterinario(vet2);
            
            // Crear usuarios para veterinarios
            Usuario userVet1 = new Usuario();
            userVet1.setNombre("Carlos");
            userVet1.setApellido("Rodríguez");
            userVet1.setUsuario("carlos");
            userVet1.setPassword("carlos");
            userVet1.setEmail("carlos@clinica.com");
            userVet1.setTelefono("666234567");
            userVet1.setRol(Usuario.Rol.VETERINARIO);
            userVet1.setVeterinarioId(vet1Id);
            
            guardarUsuario(userVet1);
            
            Usuario userVet2 = new Usuario();
            userVet2.setNombre("Ana");
            userVet2.setApellido("García");
            userVet2.setUsuario("ana");
            userVet2.setPassword("ana");
            userVet2.setEmail("ana@clinica.com");
            userVet2.setTelefono("666345678");
            userVet2.setRol(Usuario.Rol.VETERINARIO);
            userVet2.setVeterinarioId(vet2Id);
            
            guardarUsuario(userVet2);
            
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
    
    // Métodos para gestión de veterinarios
    
    /**
     * Guardar un veterinario en la base de datos
     */
    public ObjectId guardarVeterinario(ModeloVeterinario veterinario) {
        Document doc = veterinario.toDocument();
        
        if (veterinario.getId() == null) {
            // Nuevo veterinario
            veterinariosCollection.insertOne(doc);
            if (doc.containsKey("_id")) {
                veterinario.setId(doc.getObjectId("_id"));
                return veterinario.getId();
            }
        } else {
            // Actualizar veterinario existente
            Bson filter = Filters.eq("_id", veterinario.getId());
            veterinariosCollection.replaceOne(filter, doc);
            return veterinario.getId();
        }
        
        return null;
    }
    
    /**
     * Eliminar un veterinario de la base de datos
     */
    public boolean eliminarVeterinario(ObjectId id) {
        Bson filter = Filters.eq("_id", id);
        DeleteResult result = veterinariosCollection.deleteOne(filter);
        return result.wasAcknowledged() && result.getDeletedCount() > 0;
    }
    
    /**
     * Obtener un veterinario por su ID
     */
    public ModeloVeterinario obtenerVeterinarioPorId(ObjectId id) {
        Document doc = veterinariosCollection.find(Filters.eq("_id", id)).first();
        return doc != null ? new ModeloVeterinario(doc) : null;
    }
    
    /**
     * Buscar veterinarios por nombre
     */
    public List<ModeloVeterinario> buscarVeterinariosPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return obtenerTodosVeterinarios();
        }
        
        Bson filtro = Filters.or(
            Filters.regex("nombre", ".*" + nombre + ".*", "i"),
            Filters.regex("apellidos", ".*" + nombre + ".*", "i")
        );
        
        return buscarVeterinarios(filtro);
    }
    
    /**
     * Buscar veterinarios por especialidad
     */
    public List<ModeloVeterinario> buscarVeterinariosPorEspecialidad(String especialidad) {
        return buscarVeterinarios(Filters.regex("especialidad", ".*" + especialidad + ".*", "i"));
    }
    
    /**
     * Buscar veterinarios activos
     */
    public List<ModeloVeterinario> buscarVeterinariosActivos() {
        return buscarVeterinarios(Filters.eq("activo", true));
    }
    
    /**
     * Buscar veterinarios con un filtro personalizado
     */
    public List<ModeloVeterinario> buscarVeterinarios(Bson filtro) {
        List<ModeloVeterinario> veterinarios = new ArrayList<>();
        
        FindIterable<Document> findIterable = veterinariosCollection.find(filtro);
        MongoCursor<Document> cursor = findIterable.iterator();
        
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                veterinarios.add(new ModeloVeterinario(doc));
            }
        } finally {
            cursor.close();
        }
        
        return veterinarios;
    }
    
    /**
     * Obtener todos los veterinarios
     */
    public List<ModeloVeterinario> obtenerTodosVeterinarios() {
        return buscarVeterinarios(new Document());
    }

    /**
     * Verifica la conexión a MongoDB y la renueva si es necesario
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
     * Obtiene acceso a la colección de veterinarios
     */
    public MongoCollection<Document> getVeterinariosCollection() {
        return veterinariosCollection;
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
            this.veterinariosCollection = empresaDB.getCollection("veterinarios");
            
            System.out.println("Conexión a MongoDB reiniciada correctamente");
        } catch (Exception e) {
            System.err.println("Error al reiniciar conexión a MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 