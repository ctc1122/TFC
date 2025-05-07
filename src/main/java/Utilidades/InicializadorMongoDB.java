package Utilidades;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.Data.PatronExcepcion;

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
            
            // Crear usuario administrador utilizando la clase Usuario
            try {
                // Crear usuario administrador usando la clase Usuario (versión normal)
                Usuario adminUser = new Usuario("Administrador", "admin@sistema.com", "admin12345", "123456789");
                
                // Crear documento MongoDB a partir del objeto Usuario
                Document adminDocument = new Document()
                    .append("nombre", adminUser.getNombre())
                    .append("email", adminUser.getEmail())
                    .append("contraseña", adminUser.getContraseña())
                    .append("telefono", adminUser.getTelefono())
                    .append("rol", "ADMIN")
                    .append("fechaCreacion", new java.util.Date());
                
                // Insertar en la colección usuarios
                empresaDB.getCollection("usuarios").insertOne(adminDocument);
                
                // También crear un usuario admin en la colección de la Clínica
                clinicaDB.getCollection("usuarios").insertOne(adminDocument);
                
                System.out.println("Usuario administrador creado exitosamente");
            } catch (PatronExcepcion e) {
                System.err.println("Error al crear usuario administrador: " + e.getMessage());
            }
            
            // También crear un usuario admin en la base de datos Clinica usando el constructor con contraseña admin
            try {
                // Intentar crear usuario administrador con contraseña de administrador
                Usuario adminClinica = new Usuario("AdminClinica", "admin.clinica@sistema.com", "adminClinica123", "987654321", "adminVeterinaria");
                
                // Crear documento MongoDB a partir del objeto Usuario
                Document adminClinicaDocument = new Document()
                    .append("nombre", adminClinica.getNombre())
                    .append("email", adminClinica.getEmail())
                    .append("contraseña", adminClinica.getContraseña())
                    .append("telefono", adminClinica.getTelefono())
                    .append("rol", "ADMIN")
                    .append("esAdministrador", true)
                    .append("fechaCreacion", new java.util.Date());
                
                // Insertar en la colección usuarios de la clínica
                clinicaDB.getCollection("usuarios").insertOne(adminClinicaDocument);
                
                System.out.println("Usuario administrador de clínica creado exitosamente");
            } catch (Exception e) {
                System.err.println("Error al crear usuario administrador de clínica: " + e.getMessage());
            }

            System.out.println("Bases de datos y colecciones creadas exitosamente en MongoDB ");
            mongoClient.close();
        } catch (Exception e) {
            System.err.println("Error al inicializar las bases de datos: " + e.getMessage());
        }
    }
} 