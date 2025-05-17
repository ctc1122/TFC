package com.example.pruebamongodbcss.Modulos.Farmacia;

import java.util.ArrayList;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class TestConexionCIMA {
    private static final String CONNECTION_STRING = "mongodb://localhost:27020";
    private static final String DATABASE_NAME = "cima";

    public static void main(String[] args) {
        try {
            // Crear cliente MongoDB
            System.out.println("Conectando a MongoDB...");
            MongoClient mongoClient = MongoClients.create(CONNECTION_STRING);
            
            // Obtener base de datos
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            
            // Probar conexión
            Document pingResult = database.runCommand(new Document("ping", 1));
            System.out.println("Conexión exitosa: " + pingResult.toJson());
            
            // Listar colecciones
            System.out.println("\nColecciones disponibles:");
            database.listCollectionNames().into(new ArrayList<>()).forEach(collectionName -> {
                System.out.println("- " + collectionName);
            });
            
            // Cerrar conexión
            mongoClient.close();
            System.out.println("\nConexión cerrada correctamente.");
            
        } catch (Exception e) {
            System.err.println("Error al conectar con MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 