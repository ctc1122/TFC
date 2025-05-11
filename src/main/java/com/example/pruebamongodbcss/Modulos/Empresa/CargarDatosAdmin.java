package com.example.pruebamongodbcss.Modulos.Empresa;

import org.bson.Document;

import com.example.pruebamongodbcss.Data.Usuario;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import Utilidades.GestorConexion;

/**
 * Clase para cargar directamente los datos del administrador
 * Se puede ejecutar independientemente
 */
public class CargarDatosAdmin {
    
    public static void main(String[] args) {
        cargarAdmin();
    }
    
    /**
     * Carga el usuario administrador directamente
     */
    public static void cargarAdmin() {
        try {
            // Conexión a la base de datos
            MongoDatabase empresaDB = GestorConexion.conectarEmpresa();
            MongoCollection<Document> usuariosCollection = empresaDB.getCollection("usuarios");
            
            // Verificar si ya existe un administrador
            Document query = new Document("usuario", "admin");
            Document adminDoc = usuariosCollection.find(query).first();
            
            // Si no existe, crear uno nuevo
            if (adminDoc == null) {
                Usuario admin = new Usuario();
                admin.setNombre("Administrador");
                admin.setApellido("Sistema");
                admin.setUsuario("admin");
                admin.setPassword("admin");
                admin.setEmail("admin@clinica.com");
                admin.setTelefono("666555444");
                admin.setRol(Usuario.Rol.ADMINISTRADOR);
                admin.setActivo(true);
                
                // Convertir a documento y guardar
                Document docAdmin = admin.toDocument();
                usuariosCollection.insertOne(docAdmin);
                
                System.out.println("Administrador creado exitosamente");
            } else {
                System.out.println("El administrador ya existe, no es necesario crear uno nuevo");
            }
            
        } catch (Exception e) {
            System.err.println("Error al cargar administrador: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 