package com.example.pruebamongodbcss.Modulos.Empresa;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import Utilidades.GestorConexion;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Servicio para gestionar la interacción con la base de datos para usuarios y veterinarios.
 */
public class ServicioEmpresa {

    private final MongoDatabase empresaDB;
    private final MongoCollection<Document> usuariosCollection;
    private final MongoCollection<Document> veterinariosCollection;
    private ModeloUsuario usuarioActual;

    /**
     * Constructor del servicio de empresa.
     */
    public ServicioEmpresa() {
        // Conectar a la base de datos
        this.empresaDB = GestorConexion.conectarEmpresa();
        
        // Asegurar que existen las colecciones necesarias
        List<String> collectionNames = new ArrayList<>();
        empresaDB.listCollectionNames().into(collectionNames);
        
        if (!collectionNames.contains("usuarios")) {
            empresaDB.createCollection("usuarios");
        }
        
        if (!collectionNames.contains("veterinarios")) {
            empresaDB.createCollection("veterinarios");
        }
        
        // Obtener referencia a las colecciones
        this.usuariosCollection = empresaDB.getCollection("usuarios");
        this.veterinariosCollection = empresaDB.getCollection("veterinarios");
    }

    /**
     * Establece el usuario actual de la sesión.
     */
    public void setUsuarioActual(ModeloUsuario usuario) {
        this.usuarioActual = usuario;
    }
    
    /**
     * Obtiene el usuario actual de la sesión.
     */
    public ModeloUsuario getUsuarioActual() {
        return usuarioActual;
    }
    
    /**
     * Verifica si el usuario actual es administrador.
     */
    public boolean esUsuarioAdmin() {
        return usuarioActual != null && usuarioActual.esAdmin();
    }
    
    // ********** MÉTODOS PARA USUARIOS **********
    
    /**
     * Autenticar un usuario.
     */
    public ModeloUsuario autenticarUsuario(String nombreUsuario, String password) {
        Document query = new Document("usuario", nombreUsuario);
        Document doc = usuariosCollection.find(query).first();
        
        if (doc != null) {
            ModeloUsuario usuario = new ModeloUsuario(doc);
            if (usuario.getPassword().equals(password) && usuario.isActivo()) {
                return usuario;
            }
        }
        
        return null;
    }
    
    /**
     * Guardar un usuario en la base de datos.
     */
    public ObjectId guardarUsuario(ModeloUsuario usuario) {
        Document doc = usuario.toDocument();
        
        if (usuario.getId() == null) {
            // Nuevo usuario
            usuariosCollection.insertOne(doc);
            return doc.getObjectId("_id");
        } else {
            // Actualizar usuario
            usuariosCollection.replaceOne(
                Filters.eq("_id", usuario.getId()),
                doc
            );
            return usuario.getId();
        }
    }
    
    /**
     * Verificar si un nombre de usuario ya existe.
     */
    public boolean existeUsuario(String nombreUsuario) {
        Document query = new Document("usuario", nombreUsuario);
        return usuariosCollection.countDocuments(query) > 0;
    }
    
    /**
     * Obtener un usuario por su ID.
     */
    public ModeloUsuario obtenerUsuarioPorId(ObjectId id) {
        Document doc = usuariosCollection.find(Filters.eq("_id", id)).first();
        return doc != null ? new ModeloUsuario(doc) : null;
    }
    
    /**
     * Buscar usuarios por rol.
     */
    public List<ModeloUsuario> buscarUsuariosPorRol(ModeloUsuario.RolUsuario rol) {
        return buscarUsuarios(Filters.eq("rol", rol.name()));
    }
    
    /**
     * Buscar usuarios activos.
     */
    public List<ModeloUsuario> buscarUsuariosActivos() {
        return buscarUsuarios(Filters.eq("activo", true));
    }
    
    /**
     * Buscar usuarios con un filtro personalizado.
     */
    public List<ModeloUsuario> buscarUsuarios(Bson filtro) {
        List<ModeloUsuario> usuarios = new ArrayList<>();
        FindIterable<Document> docs = usuariosCollection.find(filtro).sort(Sorts.ascending("nombre", "apellido"));
        
        try (MongoCursor<Document> cursor = docs.iterator()) {
            while (cursor.hasNext()) {
                usuarios.add(new ModeloUsuario(cursor.next()));
            }
        }
        
        return usuarios;
    }
    
    /**
     * Obtener todos los usuarios.
     */
    public List<ModeloUsuario> obtenerTodosUsuarios() {
        return buscarUsuarios(new Document());
    }
    
    /**
     * Eliminar un usuario.
     */
    public boolean eliminarUsuario(ObjectId id) {
        DeleteResult result = usuariosCollection.deleteOne(Filters.eq("_id", id));
        return result.getDeletedCount() > 0;
    }
    
    // ********** MÉTODOS PARA VETERINARIOS **********
    
    /**
     * Guardar un veterinario en la base de datos.
     */
    public ObjectId guardarVeterinario(ModeloVeterinario veterinario) {
        Document doc = veterinario.toDocument();
        
        if (veterinario.getId() == null) {
            // Nuevo veterinario
            veterinariosCollection.insertOne(doc);
            return doc.getObjectId("_id");
        } else {
            // Actualizar veterinario
            veterinariosCollection.replaceOne(
                Filters.eq("_id", veterinario.getId()),
                doc
            );
            return veterinario.getId();
        }
    }
    
    /**
     * Verificar si un DNI ya existe en la colección de veterinarios.
     */
    public boolean existeVeterinarioPorDNI(String dni) {
        Document query = new Document("dni", dni);
        return veterinariosCollection.countDocuments(query) > 0;
    }
    
    /**
     * Obtener un veterinario por su ID.
     */
    public ModeloVeterinario obtenerVeterinarioPorId(ObjectId id) {
        Document doc = veterinariosCollection.find(Filters.eq("_id", id)).first();
        return doc != null ? new ModeloVeterinario(doc) : null;
    }
    
    /**
     * Buscar veterinarios por nombre o apellidos.
     */
    public List<ModeloVeterinario> buscarVeterinariosPorNombre(String nombre) {
        Bson filtro = Filters.or(
            Filters.regex("nombre", ".*" + nombre + ".*", "i"),
            Filters.regex("apellidos", ".*" + nombre + ".*", "i")
        );
        return buscarVeterinarios(filtro);
    }
    
    /**
     * Buscar veterinarios por especialidad.
     */
    public List<ModeloVeterinario> buscarVeterinariosPorEspecialidad(String especialidad) {
        Bson filtro = Filters.regex("especialidad", ".*" + especialidad + ".*", "i");
        return buscarVeterinarios(filtro);
    }
    
    /**
     * Buscar veterinarios activos.
     */
    public List<ModeloVeterinario> buscarVeterinariosActivos() {
        return buscarVeterinarios(Filters.eq("activo", true));
    }
    
    /**
     * Buscar veterinarios con un filtro personalizado.
     */
    public List<ModeloVeterinario> buscarVeterinarios(Bson filtro) {
        List<ModeloVeterinario> veterinarios = new ArrayList<>();
        FindIterable<Document> docs = veterinariosCollection.find(filtro).sort(Sorts.ascending("apellidos", "nombre"));
        
        try (MongoCursor<Document> cursor = docs.iterator()) {
            while (cursor.hasNext()) {
                veterinarios.add(new ModeloVeterinario(cursor.next()));
            }
        }
        
        return veterinarios;
    }
    
    /**
     * Obtener todos los veterinarios.
     */
    public List<ModeloVeterinario> obtenerTodosVeterinarios() {
        return buscarVeterinarios(new Document());
    }
    
    /**
     * Eliminar un veterinario.
     */
    public boolean eliminarVeterinario(ObjectId id) {
        // Verificar si hay usuarios con referencia a este veterinario
        long usuariosAsociados = usuariosCollection.countDocuments(
            Filters.eq("veterinarioId", id)
        );
        
        if (usuariosAsociados > 0) {
            // No permitir eliminar si hay usuarios asociados
            return false;
        }
        
        DeleteResult result = veterinariosCollection.deleteOne(Filters.eq("_id", id));
        return result.getDeletedCount() > 0;
    }
    
    // ********** CARGAR DATOS DE PRUEBA **********
    
    /**
     * Cargar datos de prueba para la demo.
     */
    public void cargarDatosPrueba() {
        // Verificar si ya hay datos de prueba
        if (usuariosCollection.countDocuments() > 1 || veterinariosCollection.countDocuments() > 0) {
            return; // Ya hay datos, no hacer nada
        }
        
        // Cargar veterinarios de prueba
        List<ModeloVeterinario> veterinarios = new ArrayList<>();
        
        // Veterinario 1
        ModeloVeterinario veterinario1 = new ModeloVeterinario();
        veterinario1.setNombre("Ana");
        veterinario1.setApellidos("Martínez García");
        veterinario1.setDni("12345678A");
        veterinario1.setNumeroTitulo("VET001");
        veterinario1.setEspecialidad("Medicina Interna");
        veterinario1.setEmail("ana.martinez@clinicaveterinaria.com");
        veterinario1.setTelefono("600111222");
        veterinario1.setFechaContratacion(new Date());
        veterinario1.setActivo(true);
        
        // Veterinario 2
        ModeloVeterinario veterinario2 = new ModeloVeterinario();
        veterinario2.setNombre("Carlos");
        veterinario2.setApellidos("Rodríguez López");
        veterinario2.setDni("87654321B");
        veterinario2.setNumeroTitulo("VET002");
        veterinario2.setEspecialidad("Cirugía");
        veterinario2.setEmail("carlos.rodriguez@clinicaveterinaria.com");
        veterinario2.setTelefono("600222333");
        veterinario2.setFechaContratacion(new Date());
        veterinario2.setActivo(true);
        
        // Veterinario 3
        ModeloVeterinario veterinario3 = new ModeloVeterinario();
        veterinario3.setNombre("Laura");
        veterinario3.setApellidos("González Fernández");
        veterinario3.setDni("23456789C");
        veterinario3.setNumeroTitulo("VET003");
        veterinario3.setEspecialidad("Dermatología");
        veterinario3.setEmail("laura.gonzalez@clinicaveterinaria.com");
        veterinario3.setTelefono("600333444");
        veterinario3.setFechaContratacion(new Date());
        veterinario3.setActivo(true);
        
        // Veterinario 4
        ModeloVeterinario veterinario4 = new ModeloVeterinario();
        veterinario4.setNombre("Jorge");
        veterinario4.setApellidos("Sánchez Ruiz");
        veterinario4.setDni("34567890D");
        veterinario4.setNumeroTitulo("VET004");
        veterinario4.setEspecialidad("Traumatología");
        veterinario4.setEmail("jorge.sanchez@clinicaveterinaria.com");
        veterinario4.setTelefono("600444555");
        veterinario4.setFechaContratacion(new Date());
        veterinario4.setActivo(true);
        
        // Guardar veterinarios
        veterinario1.setId(guardarVeterinario(veterinario1));
        veterinarios.add(veterinario1);
        
        veterinario2.setId(guardarVeterinario(veterinario2));
        veterinarios.add(veterinario2);
        
        veterinario3.setId(guardarVeterinario(veterinario3));
        veterinarios.add(veterinario3);
        
        veterinario4.setId(guardarVeterinario(veterinario4));
        veterinarios.add(veterinario4);
        
        // Usuario Admin (si no existe)
        if (usuariosCollection.countDocuments(Filters.eq("rol", "ADMIN")) == 0) {
            ModeloUsuario adminUsuario = new ModeloUsuario();
            adminUsuario.setUsuario("admin");
            adminUsuario.setPassword("admin");
            adminUsuario.setNombre("Administrador");
            adminUsuario.setApellido("Sistema");
            adminUsuario.setEmail("admin@clinicaveterinaria.com");
            adminUsuario.setTelefono("600000000");
            adminUsuario.setRol(ModeloUsuario.RolUsuario.ADMIN);
            adminUsuario.setFechaCreacion(new Date());
            adminUsuario.setActivo(true);
            
            guardarUsuario(adminUsuario);
        }
        
        // Usuario para cada veterinario
        for (ModeloVeterinario vet : veterinarios) {
            ModeloUsuario usuario = new ModeloUsuario();
            
            // Generar nombre de usuario a partir del nombre y primer apellido
            String nombreUsuario = (vet.getNombre().toLowerCase().charAt(0) + 
                                    vet.getApellidos().split(" ")[0].toLowerCase());
            
            usuario.setUsuario(nombreUsuario);
            usuario.setPassword("password");
            usuario.setNombre(vet.getNombre());
            usuario.setApellido(vet.getApellidos());
            usuario.setEmail(vet.getEmail());
            usuario.setTelefono(vet.getTelefono());
            usuario.setRol(ModeloUsuario.RolUsuario.VETERINARIO);
            usuario.setFechaCreacion(new Date());
            usuario.setActivo(true);
            usuario.setVeterinarioId(vet.getId());
            
            guardarUsuario(usuario);
        }
    }
} 