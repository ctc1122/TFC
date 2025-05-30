package com.example.pruebamongodbcss;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

import Utilidades1.GestorConexion;

public class EjecucionesBBDD {
    public static void main(String[] args) {
        // Crear usuario admin
        MongoClientURI clientURI = new MongoClientURI("mongodb://localhost:27017");
        MongoClient mongoClient = new MongoClient(clientURI);
        MongoDatabase db = mongoClient.getDatabase("inventario");
        db.drop();
        System.out.println("Base de datos 'testdb' eliminada correctamente");
    }
}
