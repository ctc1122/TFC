package com.example.pruebamongodbcss.Utilidades;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class GestorConexion {

    static String URI = "mongodb+srv://cristofertorresct:Cris087.@cluster0.f2pdq.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";

    public GestorConexion() {
    }

    public static MongoDatabase conectarBD() {
        MongoClientURI clientURI = new MongoClientURI(URI);
        MongoClient mongoClient = new MongoClient(clientURI);
        MongoDatabase mongoDatabase = mongoClient.getDatabase("Tienda");
        
        return mongoDatabase;
    }
    
    // Método alternativo para conectar a MongoDB local (para uso con Docker)
    public static MongoDatabase conectarBDLocal() {
        try {
            // Conectar a la instancia local de MongoDB en Docker
            MongoClient mongoClient = new MongoClient("localhost", 27017);
            return mongoClient.getDatabase("Tienda");
        } catch (Exception e) {
            System.err.println("Error al conectar a MongoDB local: " + e.getMessage());
            return null;
        }
    }
} 