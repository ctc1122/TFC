package Utilidades;

import org.bson.Document;

import com.example.pruebamongodbcss.Data.Usuario;
import com.mongodb.client.MongoDatabase;

public class InicializadorMongoDB {
    public static void main(String[] args) {
        try {
            // Inicializar base de datos Clinica
            MongoDatabase clinicaDB = GestorConexion.conectarBD("Clinica");
            clinicaDB.createCollection("usuarios");
            clinicaDB.createCollection("pacientes");
            clinicaDB.createCollection("citas");
            
            // Crear índices para Clinica
            clinicaDB.getCollection("usuarios").createIndex(new Document("email", 1));
            clinicaDB.getCollection("pacientes").createIndex(new Document("dni", 1));

            // Inicializar base de datos Inventario
            MongoDatabase inventarioDB = GestorConexion.conectarBD("Inventario");
            inventarioDB.createCollection("productos");
            inventarioDB.createCollection("proveedores");
            inventarioDB.createCollection("stock");
            inventarioDB.createCollection("farmacia");
            
            // Crear índices para Inventario
            inventarioDB.getCollection("productos").createIndex(new Document("codigo", 1));


            // Inicializar base de datos Empresa
            MongoDatabase empresaDB = GestorConexion.conectarBD("Empresa");
            empresaDB.createCollection("usuarios");
            
            // Crear usuario administrador utilizando la nueva estructura de la clase Usuario
            try {
                // Crear usuario administrador usando la nueva estructura
                Usuario adminUser = new Usuario(
                    "Administrador", 
                    "Sistema",
                    "admin", 
                    "admin", 
                    "admin@sistema.com", 
                    "123456789",
                    "admin12345" // Contraseña de administrador
                );
                
                // Crear documento MongoDB a partir del objeto Usuario actualizado
                Document adminDocument = new Document()
                    .append("nombre", adminUser.getNombre())
                    .append("apellido", adminUser.getApellido())
                    .append("usuario", adminUser.getUsuario())
                    .append("password", adminUser.getPassword())
                    .append("email", adminUser.getEmail())
                    .append("telefono", adminUser.getTelefono())
                    .append("rol", adminUser.getRol().toString())
                    .append("fechaCreacion", adminUser.getFechaCreacion());
                
                // Insertar en la colección usuarios
                empresaDB.getCollection("usuarios").insertOne(adminDocument);
                
                // También crear un usuario admin en la colección de la Clínica
                clinicaDB.getCollection("usuarios").insertOne(adminDocument);
                
                System.out.println("Usuario administrador creado exitosamente");
            } catch (Exception e) {
                System.err.println("Error al crear usuario administrador: " + e.getMessage());
            }
            


            System.out.println("Bases de datos y colecciones creadas exitosamente en MongoDB ");

        } catch (Exception e) {
            System.err.println("Error al inicializar las bases de datos: " + e.getMessage());
        }

 
    }


    public static void importardatosCIMA(){



    }
} 