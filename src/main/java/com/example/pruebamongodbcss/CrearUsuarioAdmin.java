package com.example.pruebamongodbcss;

import java.util.Date;

import org.bson.Document;

import com.example.pruebamongodbcss.Utilidades.GestorConexion;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class CrearUsuarioAdmin {
    
    public static void main(String[] args) {
        System.out.println("Iniciando creación de usuario administrador...");
        
        try {
            // Conectar a la base de datos local
            MongoDatabase db = GestorConexion.conectarBDLocal();
            
            if (db == null) {
                System.out.println("No se pudo conectar a la base de datos local, intentando remota...");
                db = GestorConexion.conectarBD();
            }
            
            if (db == null) {
                System.err.println("Error: No se pudo conectar a ninguna base de datos MongoDB.");
                return;
            }
            
            System.out.println("Conexión a la base de datos establecida correctamente.");
            
            // Verificar si existe la colección de usuarios
            boolean coleccionExiste = false;
            for (String coleccion : db.listCollectionNames()) {
                if (coleccion.equals("usuarios")) {
                    coleccionExiste = true;
                    break;
                }
            }
            
            // Obtener la colección de usuarios (o crearla si no existe)
            MongoCollection<Document> usuarios = db.getCollection("usuarios");
            
            if (!coleccionExiste) {
                System.out.println("La colección 'usuarios' no existe, creándola...");
                db.createCollection("usuarios");
                System.out.println("Colección 'usuarios' creada correctamente.");
            }
            
            // Verificar si el admin ya existe
            Document adminExistente = usuarios.find(Filters.eq("usuario", "admin")).first();
            
            if (adminExistente != null) {
                System.out.println("El usuario administrador ya existe.");
            } else {
                // Crear documento de usuario administrador
                Document adminDoc = new Document()
                    .append("usuario", "admin")
                    .append("password", "admin")
                    .append("rol", "ADMIN")
                    .append("nombre", "Administrador")
                    .append("apellido", "Sistema")
                    .append("fechaCreacion", new Date());
                
                // Insertar el documento
                usuarios.insertOne(adminDoc);
                
                System.out.println("Usuario administrador creado correctamente.");
            }
            
            // Mostrar los usuarios existentes
            System.out.println("Usuarios en la base de datos:");
            for (Document doc : usuarios.find()) {
                System.out.println(" - " + doc.getString("usuario") + " (Rol: " + doc.getString("rol") + ")");
            }
            
        } catch (Exception e) {
            System.err.println("Error al crear usuario administrador: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 