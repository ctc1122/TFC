package Utilidades;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Script para importar archivo Diagnosticos.sql a MariaDB
 * Conecta al puerto 3306 (puerto estándar de MariaDB/MySQL)
 */
public class importarUMLSsql {
    
    // Configuración de conexión a MariaDB
    private static final String DB_URL = "jdbc:mariadb://localhost:3306/";
    private static final String DB_NAME = "umls_database";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "miclave";
    
    // Driver de MariaDB
    private static final String DRIVER = "org.mariadb.jdbc.Driver";
    
    private static Connection connection = null;
    
    // Ruta del archivo SQL a importar - CAMBIAR ESTA RUTA
    private static final String ARCHIVO_SQL = "./docker/Diagnosticos.sql";
    
    public static void main(String[] args) {
        System.out.println("=== IMPORTADOR SQL A MARIADB ===");
        System.out.println("Conectando a MariaDB en puerto 3306...");
        
        try {
            // Cargar el driver de MariaDB
            Class.forName(DRIVER);
            System.out.println("Driver MariaDB cargado correctamente");
            
            // Establecer conexión
            conectarBaseDatos();
            
            // Crear base de datos si no existe
            crearBaseDatos();
            
            // Importar archivo SQL directamente
            importarArchivoSQL(ARCHIVO_SQL);
            
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Driver MariaDB no encontrado");
            System.err.println("Asegúrate de tener el JAR de MariaDB en el classpath");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error de conexión a MariaDB: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cerrarConexion();
        }
    }
    
    /**
     * Establece conexión con MariaDB
     */
    private static void conectarBaseDatos() throws SQLException {
        try {
            String urlSinBD = DB_URL + "?useSSL=false&allowPublicKeyRetrieval=true";
            connection = DriverManager.getConnection(urlSinBD, USERNAME, PASSWORD);
            System.out.println("Conexión exitosa a MariaDB en puerto 3306");
        } catch (SQLException e) {
            System.err.println("Error al conectar a MariaDB: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Crea la base de datos si no existe
     */
    private static void crearBaseDatos() throws SQLException {
        String createDB = "CREATE DATABASE IF NOT EXISTS " + DB_NAME + 
                         " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createDB);
            System.out.println("Base de datos '" + DB_NAME + "' verificada/creada");
            
            stmt.executeUpdate("USE " + DB_NAME);
            System.out.println("Usando base de datos: " + DB_NAME);
        }
    }
    
    /**
     * Importa el archivo SQL ejecutando las sentencias
     */
    private static void importarArchivoSQL(String rutaArchivo) {
        System.out.println("Importando archivo SQL desde: " + rutaArchivo);
        
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo));
             Statement stmt = connection.createStatement()) {
            
            StringBuilder sqlBuilder = new StringBuilder();
            String linea;
            int sentenciasEjecutadas = 0;
            int errores = 0;
            
            System.out.println("Iniciando importación del archivo SQL...");
            
            while ((linea = br.readLine()) != null) {
                // Ignorar líneas vacías y comentarios
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("--") || linea.startsWith("#")) {
                    continue;
                }
                
                // Agregar línea al builder
                sqlBuilder.append(linea).append(" ");
                
                // Si la línea termina con ; es el final de una sentencia
                if (linea.endsWith(";")) {
                    String sentenciaSQL = sqlBuilder.toString().trim();
                    
                    try {
                        // Ejecutar la sentencia SQL
                        stmt.execute(sentenciaSQL);
                        sentenciasEjecutadas++;
                        
                        if (sentenciasEjecutadas % 100 == 0) {
                            System.out.println("Ejecutadas " + sentenciasEjecutadas + " sentencias SQL...");
                        }
                        
                    } catch (SQLException e) {
                        errores++;
                        if (errores <= 5) {
                            System.err.println("Error en sentencia " + sentenciasEjecutadas + ": " + e.getMessage());
                            System.err.println("SQL: " + sentenciaSQL.substring(0, Math.min(100, sentenciaSQL.length())) + "...");
                        }
                    }
                    
                    // Limpiar el builder para la siguiente sentencia
                    sqlBuilder.setLength(0);
                }
            }
            
            // Si queda algo en el builder (sentencia sin ;)
            if (sqlBuilder.length() > 0) {
                String sentenciaSQL = sqlBuilder.toString().trim();
                if (!sentenciaSQL.isEmpty()) {
                    try {
                        stmt.execute(sentenciaSQL);
                        sentenciasEjecutadas++;
                    } catch (SQLException e) {
                        errores++;
                        System.err.println("Error en última sentencia: " + e.getMessage());
                    }
                }
            }
            
            System.out.println("\n=== IMPORTACIÓN COMPLETADA ===");
            System.out.println("✓ Sentencias SQL ejecutadas exitosamente: " + sentenciasEjecutadas);
            if (errores > 0) {
                System.out.println("⚠ Sentencias con errores: " + errores);
            }
            System.out.println("✓ Importación del archivo SQL finalizada");
            
        } catch (IOException e) {
            System.err.println("Error al leer el archivo SQL: " + e.getMessage());
            System.err.println("Verifica que la ruta del archivo sea correcta: " + rutaArchivo);
        } catch (SQLException e) {
            System.err.println("Error de base de datos: " + e.getMessage());
        }
    }
    
    /**
     * Cierra la conexión a la base de datos
     */
    private static void cerrarConexion() {
        if (connection != null) {
            try {
                System.out.println("Cerrando conexión a MariaDB...");
                connection.close();
                System.out.println("Conexión cerrada correctamente");
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }
} 