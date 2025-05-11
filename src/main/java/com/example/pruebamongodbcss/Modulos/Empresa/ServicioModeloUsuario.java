package com.example.pruebamongodbcss.Modulos.Empresa;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.example.pruebamongodbcss.Data.ServicioUsuarios;
import com.example.pruebamongodbcss.Data.Usuario;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import Utilidades.GestorConexion;

/**
 * Servicio para gestionar los ModeloUsuario, utilizando internamente la clase Usuario
 */
public class ServicioModeloUsuario {
    
    private MongoDatabase empresaDB;
    private MongoCollection<Document> usuariosCollection;
    private ServicioUsuarios servicioUsuarios; // Servicio original para delegación
    
    /**
     * Constructor del servicio
     */
    public ServicioModeloUsuario() {
        try {
            // Conectar a la base de datos
            this.empresaDB = GestorConexion.conectarEmpresa();
            
            // Asegurar que existe la colección de usuarios
            List<String> collectionNames = new ArrayList<>();
            empresaDB.listCollectionNames().into(collectionNames);
            
            if (!collectionNames.contains("usuarios")) {
                empresaDB.createCollection("usuarios");
            }
            
            // Obtener referencia a la colección
            this.usuariosCollection = empresaDB.getCollection("usuarios");
            
            // Crear servicio delegado para funciones complejas
            this.servicioUsuarios = new ServicioUsuarios();
            
        } catch (Exception e) {
            System.err.println("Error al inicializar ServicioModeloUsuario: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Obtiene todos los usuarios como ModeloUsuario
     */
    public List<ModeloUsuario> obtenerTodosUsuarios() {
        List<ModeloUsuario> usuarios = new ArrayList<>();
        
        try {
            // Verificar la conexión primero
            if (!servicioUsuarios.verificarConexion()) {
                System.err.println("La conexión a MongoDB no es válida. Intentando reiniciarla...");
                reiniciarConexion();
            }
            
            // Usar el método directo con la colección
            FindIterable<Document> documentos = usuariosCollection.find();
            MongoCursor<Document> cursor = documentos.iterator();
            
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                try {
                    ModeloUsuario usuario = new ModeloUsuario(doc);
                    usuarios.add(usuario);
                } catch (Exception e) {
                    System.err.println("Error al crear ModeloUsuario: " + e.getMessage());
                }
            }
            
            cursor.close();
            
        } catch (Exception e) {
            System.err.println("Error al obtener usuarios: " + e.getMessage());
            e.printStackTrace();
            
            // Como alternativa, intentar obtener a través del servicio delegado
            try {
                reiniciarConexion();
                List<Usuario> usuariosOriginales = servicioUsuarios.obtenerTodosUsuarios();
                for (Usuario u : usuariosOriginales) {
                    usuarios.add(new ModeloUsuario(u));
                }
            } catch (Exception e2) {
                System.err.println("Error en segundo intento: " + e2.getMessage());
            }
        }
        
        return usuarios;
    }
    
    /**
     * Guarda un ModeloUsuario en la base de datos
     */
    public ObjectId guardarUsuario(ModeloUsuario modeloUsuario) {
        return servicioUsuarios.guardarUsuario(modeloUsuario.getUsuario());
    }
    
    /**
     * Elimina un usuario por su ID
     */
    public boolean eliminarUsuario(ObjectId id) {
        return servicioUsuarios.eliminarUsuario(id);
    }
    
    /**
     * Busca usuarios por texto en nombre, apellido o usuario
     */
    public List<ModeloUsuario> buscarUsuariosPorTexto(String texto) {
        List<ModeloUsuario> resultado = new ArrayList<>();
        List<Usuario> usuariosEncontrados = servicioUsuarios.buscarUsuariosPorTexto(texto);
        
        for (Usuario u : usuariosEncontrados) {
            resultado.add(new ModeloUsuario(u));
        }
        
        return resultado;
    }
    
    /**
     * Busca usuarios por rol
     */
    public List<ModeloUsuario> buscarUsuariosPorRol(Usuario.Rol rol) {
        List<ModeloUsuario> resultado = new ArrayList<>();
        List<Usuario> usuariosEncontrados = servicioUsuarios.buscarUsuariosPorRol(rol);
        
        for (Usuario u : usuariosEncontrados) {
            resultado.add(new ModeloUsuario(u));
        }
        
        return resultado;
    }
    
    /**
     * Carga datos de prueba
     */
    public void cargarDatosPrueba() {
        // Verificar la conexión antes de intentar cargar datos
        if (!servicioUsuarios.verificarConexion()) {
            System.err.println("La conexión a MongoDB no es válida. Intentando reiniciarla...");
            reiniciarConexion();
        }
        servicioUsuarios.cargarDatosPrueba();
    }
    
    /**
     * Verifica si la conexión a MongoDB está funcionando
     */
    public boolean verificarConexion() {
        try {
            // Intentar una operación simple para verificar la conexión
            empresaDB.runCommand(new Document("ping", 1));
            return true;
        } catch (Exception e) {
            System.err.println("Error al verificar conexión a MongoDB: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Reinicia la conexión a MongoDB
     */
    public void reiniciarConexion() {
        try {
            // Reiniciar el servicio delegado
            servicioUsuarios.reiniciarConexion();
            
            // Reiniciar nuestra propia conexión
            GestorConexion.cerrarConexion();
            this.empresaDB = GestorConexion.conectarEmpresa();
            this.usuariosCollection = empresaDB.getCollection("usuarios");
            
        } catch (Exception e) {
            System.err.println("Error al reiniciar conexión: " + e.getMessage());
        }
    }
} 