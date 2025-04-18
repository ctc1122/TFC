package Utilidades;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class GestorConexion {

    static String DEFAULT_URI = "mongodb://localhost:27017";
    static String DEFAULT_DB = "Tienda";
    
    public GestorConexion() {
    }

    public static MongoDatabase conectarBD() {
        return conectarBD(DEFAULT_URI, DEFAULT_DB);
    }
    
    public static MongoDatabase conectarBD(int puerto) {
        String uri = "mongodb://localhost:" + puerto;
        return conectarBD(uri, DEFAULT_DB);
    }
    
    public static MongoDatabase conectarBD(String nombreBD) {
        return conectarBD(DEFAULT_URI, nombreBD);
    }
    
    public static MongoDatabase conectarBD(int puerto, String nombreBD) {
        String uri = "mongodb://localhost:" + puerto;
        return conectarBD(uri, nombreBD);
    }
    
    public static MongoDatabase conectarBD(String uri, String nombreBD) {
        MongoClientURI clientURI = new MongoClientURI(uri);
        MongoClient mongoClient = new MongoClient(clientURI);
        MongoDatabase mongoDatabase = mongoClient.getDatabase(nombreBD);
        
        return mongoDatabase;
    }
}
