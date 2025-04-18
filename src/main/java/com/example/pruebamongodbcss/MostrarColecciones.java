package com.example.pruebamongodbcss;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import Utilidades.GestorConexion;

public class MostrarColecciones {
    
    public static void main(String[] args) {
        System.out.println("Conectando a MongoDB para mostrar colecciones...");
        
        try {
            // Conectar a la base de datos
            MongoDatabase db = GestorConexion.conectarBD();
            
            if (db == null) {
                System.out.println("No se pudo conectar a la base de datos local, intentando remota...");
                db = GestorConexion.conectarBD();
            }
            
            if (db == null) {
                System.err.println("Error: No se pudo conectar a ninguna base de datos MongoDB.");
                return;
            }
            
            System.out.println("Conexión a la base de datos establecida correctamente.");
            
            // Mostrar todas las colecciones
            System.out.println("\n=== COLECCIONES EN LA BASE DE DATOS " + db.getName() + " ===");
            for (String coleccion : db.listCollectionNames()) {
                System.out.println(" - " + coleccion);
            }
            
            // Mostrar todos los usuarios
            MongoCollection<Document> usuarios = db.getCollection("usuarios");
            
            System.out.println("\n=== USUARIOS REGISTRADOS ===");
            if (usuarios.countDocuments() == 0) {
                System.out.println("No hay usuarios registrados.");
            } else {
                for (Document doc : usuarios.find()) {
                    System.out.println("\nUsuario: " + doc.getString("usuario"));
                    System.out.println("Nombre: " + doc.getString("nombre") + " " + doc.getString("apellido"));
                    System.out.println("Rol: " + doc.getString("rol"));
                    System.out.println("Fecha creación: " + doc.getDate("fechaCreacion"));
                    System.out.println("----------------------------------");
                }
            }
            
            // Mostrar contenido de otras colecciones (opcional)
            mostrarOtrasColecciones(db);
            
        } catch (Exception e) {
            System.err.println("Error al mostrar colecciones: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void mostrarOtrasColecciones(MongoDatabase db) {
        try {
            // Lista de colecciones importantes para mostrar (puedes agregar más)
            String[] coleccionesImportantes = {"pacientes", "citas", "productos"};
            
            for (String nombreColeccion : coleccionesImportantes) {
                try {
                    MongoCollection<Document> coleccion = db.getCollection(nombreColeccion);
                    
                    if (coleccion.countDocuments() > 0) {
                        System.out.println("\n=== CONTENIDO DE COLECCIÓN: " + nombreColeccion.toUpperCase() + " ===");
                        for (Document doc : coleccion.find().limit(5)) { // Mostrar máximo 5 documentos
                            System.out.println(doc.toJson());
                            System.out.println("----------------------------------");
                        }
                        
                        // Si hay más de 5 documentos
                        long total = coleccion.countDocuments();
                        if (total > 5) {
                            System.out.println("[...y " + (total - 5) + " documentos más]");
                        }
                    }
                } catch (Exception e) {
                    // Simplemente ignoramos si la colección no existe o hay error
                }
            }
        } catch (Exception e) {
            System.err.println("Error al mostrar otras colecciones: " + e.getMessage());
        }
    }
} 