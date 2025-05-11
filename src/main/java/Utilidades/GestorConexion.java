package Utilidades;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class GestorConexion {
    // URI para MongoDB Atlas
    //static String MONGO_URI = "mongodb+srv://cristofertorresct:admin@cluster0.f2pdq.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
    
    // URI para MongoDB local - usando el puerto 27017 (estándar)
    private static String MONGO_URI_DOCKER = "mongodb://localhost:27017/";
    
    // Intentar con puerto alternativo si falla el estándar
    private static String MONGO_URI_ALT = "mongodb://localhost:27018/";
    
    // Nombres de las bases de datos
    private static String DB_CLINICA = "Clinica";
    private static String DB_INVENTARIO = "Inventario";
    private static String DB_TERMINOS = "Terminos";
    private static String DB_EMPRESA = "Empresa";
    
    // Cliente MongoDB compartido (singleton)
    private static MongoClient mongoClient = null;
    
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
            if (mongoClient == null) {
                try {
                    System.out.println("Intentando conectar a MongoDB en puerto 27017...");
                    MongoClientURI clientURI = new MongoClientURI(MONGO_URI_DOCKER);
                    mongoClient = new MongoClient(clientURI);
                    // Prueba simple para verificar la conexión
                    mongoClient.getAddress();
                    System.out.println("Conexión exitosa a MongoDB en puerto 27017");
                } catch (Exception e) {
                    // Si falla el primer intento, probar con el puerto alternativo
                    System.out.println("Error al conectar en puerto 27017: " + e.getMessage());
                    System.out.println("Intentando conectar a MongoDB en puerto 27018...");
                    
                    try {
                        MongoClientURI clientURIAlt = new MongoClientURI(MONGO_URI_ALT);
                        mongoClient = new MongoClient(clientURIAlt);
                        // Prueba simple para verificar la conexión
                        mongoClient.getAddress();
                        System.out.println("Conexión exitosa a MongoDB en puerto 27018");
                    } catch (Exception e2) {
                        System.err.println("Error en segundo intento: " + e2.getMessage());
                        throw e2;
                    }
                }
            }
            
            MongoDatabase mongoDatabase = mongoClient.getDatabase(nombreBD);
            System.out.println("Base de datos seleccionada: " + nombreBD);
            return mongoDatabase;
        } catch (Exception e) {
            System.err.println("Error fatal al conectar a MongoDB: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    // Método para cerrar la conexión cuando la aplicación termina
    public static void cerrarConexion() {
        if (mongoClient != null) {
            try {
                System.out.println("Cerrando conexión a MongoDB...");
                mongoClient.close();
                mongoClient = null;
                System.out.println("Conexión a MongoDB cerrada correctamente");
            } catch (Exception e) {
                System.err.println("Error al cerrar conexión MongoDB: " + e.getMessage());
                e.printStackTrace();
                mongoClient = null;
            }
        }
    }
}
