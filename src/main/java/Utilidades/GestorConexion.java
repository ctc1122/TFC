package Utilidades;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class GestorConexion {
    // URI para MongoDB Atlas
    //static String MONGO_URI = "mongodb+srv://cristofertorresct:admin@cluster0.f2pdq.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
    
    static String MONGO_URI_DOCKER = "mongodb://localhost:27017/";
    // Nombres de las bases de datos
    
    static String DB_CLINICA = "Clinica";
    static String DB_INVENTARIO = "Inventario";
    static String DB_TERMINOS = "Terminos";
    static String DB_EMPRESA = "Empresa";
    
    public GestorConexion() {
    }

    // Métodos específicos para cada base de datos
    public static MongoDatabase conectarClinica() {
        return conectarBD(DB_CLINICA);
    }
    
    public static MongoDatabase conectarEmpresa() {
        return conectarBD(DB_EMPRESA);
    }

    public static MongoDatabase conectarInventario() {
        return conectarBD(DB_INVENTARIO);
    }
    
    public static MongoDatabase conectarTerminos() {
        return conectarBD(DB_TERMINOS);
    }
    
    // Método genérico para conexión
    public static MongoDatabase conectarBD(String nombreBD) {
        try {
            MongoClientURI clientURI = new MongoClientURI(MONGO_URI_DOCKER);
            MongoClient mongoClient = new MongoClient(clientURI);
            MongoDatabase mongoDatabase = mongoClient.getDatabase(nombreBD);
            System.out.println("Conectado exitosamente a MongoDB Atlas: " + nombreBD);
            return mongoDatabase;
        } catch (Exception e) {
            System.err.println("Error al conectar a MongoDB Atlas: " + e.getMessage());
            throw e;
        }
    }
}
