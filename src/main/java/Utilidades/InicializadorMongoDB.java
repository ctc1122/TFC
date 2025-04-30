package Utilidades;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class InicializadorMongoDB {
    public static void main(String[] args) {
        try {
            // Conectar a MongoDB Atlas
            MongoClientURI uri = new MongoClientURI(GestorConexion.MONGO_URI_DOCKER);
            MongoClient mongoClient = new MongoClient(uri);

            // Inicializar base de datos Clinica
            MongoDatabase clinicaDB = mongoClient.getDatabase("Clinica");
            clinicaDB.createCollection("usuarios");
            clinicaDB.createCollection("pacientes");
            clinicaDB.createCollection("citas");
            
            // Crear índices para Clinica
            clinicaDB.getCollection("usuarios").createIndex(new Document("email", 1));
            clinicaDB.getCollection("pacientes").createIndex(new Document("dni", 1));

            // Inicializar base de datos Inventario
            MongoDatabase inventarioDB = mongoClient.getDatabase("Inventario");
            inventarioDB.createCollection("productos");
            inventarioDB.createCollection("proveedores");
            inventarioDB.createCollection("stock");
            
            // Crear índices para Inventario
            inventarioDB.getCollection("productos").createIndex(new Document("codigo", 1));

            // Inicializar base de datos Terminos
            MongoDatabase terminosDB = mongoClient.getDatabase("Terminos");
            terminosDB.createCollection("terminos_medicos");
            terminosDB.createCollection("categorias");
            
            // Crear índices para Terminos
            terminosDB.getCollection("terminos_medicos").createIndex(new Document("nombre", 1));

            // Inicializar base de datos Empresa
            MongoDatabase empresaDB = mongoClient.getDatabase("Empresa");
            empresaDB.createCollection("usuarios");
            
            // Crear usuario administrador
            Document adminUser = new Document()
                .append("usuario", "admin")
                .append("password", "admin")
                .append("rol", "ADMIN")
                .append("nombre", "Administrador")
                .append("apellido", "Sistema")
                .append("fechaCreacion", new java.util.Date());
            
            empresaDB.getCollection("usuarios").insertOne(adminUser);

            System.out.println("Bases de datos y colecciones creadas exitosamente en MongoDB ");
            mongoClient.close();
        } catch (Exception e) {
            System.err.println("Error al inicializar las bases de datos: " + e.getMessage());
        }
    }
} 