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

    private final MongoDatabase empresaDB;
    private final MongoCollection<Document> usuariosCollection;
    private final MongoCollection<Document> veterinariosCollection;
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
     * Cargar datos de prueba (usuarios y veterinarios)
     */
    public void cargarDatosPrueba() {
        try {
            // Verificar si ya existen usuarios
            if (usuariosCollection.countDocuments() > 0) {
                System.out.println("Los datos de prueba ya están cargados. Omitiendo...");
                return;
            }
            
            System.out.println("Cargando datos de prueba...");
            
            // Crear administrador
            Usuario admin = new Usuario(
                "Administrador", 
                "Sistema", 
                "admin", 
                "admin12345", 
                "admin@clinica.com", 
                "666555444", 
                "admin12345"
            );
            admin.setRol(Usuario.Rol.ADMINISTRADOR);
            guardarUsuario(admin);
            
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
            
            System.out.println("Datos de prueba cargados correctamente");
        } catch (Exception e) {
            System.err.println("Error al cargar datos de prueba: " + e.getMessage());
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
} 