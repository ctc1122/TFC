package Utilidades;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Script para importar datos UMLS a una base de datos MariaDB
 * Conecta al puerto 3306 (puerto estándar de MariaDB/MySQL)
 */
public class importarUMLSsql {
    
    // Configuración de conexión a MariaDB
    private static final String DB_URL = "jdbc:mariadb://localhost:3306/";
    private static final String DB_NAME = "umls_database";
    private static final String FULL_URL = DB_URL + DB_NAME;
    private static final String USERNAME = "root";
    private static final String PASSWORD = "miclave"; // Cambiar según configuración
    
    // Driver de MariaDB
    private static final String DRIVER = "org.mariadb.jdbc.Driver";
    
    private static Connection connection = null;
    
    public static void main(String[] args) {
        System.out.println("=== IMPORTADOR UMLS A MARIADB ===");
        System.out.println("Conectando a MariaDB en puerto 3306...");
        
        try {
            // Cargar el driver de MariaDB
            Class.forName(DRIVER);
            System.out.println("Driver MariaDB cargado correctamente");
            
            // Establecer conexión
            conectarBaseDatos();
            
            // Crear base de datos si no existe
            crearBaseDatos();
            
            // Crear tablas UMLS
            crearTablasUMLS();
            
            // Menú de opciones
            mostrarMenu();
            
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
            // Primero conectar sin especificar base de datos para crearla si no existe
            String urlSinBD = DB_URL + "?useSSL=false&allowPublicKeyRetrieval=true";
            connection = DriverManager.getConnection(urlSinBD, USERNAME, PASSWORD);
            System.out.println("Conexión exitosa a MariaDB en puerto 3306");
        } catch (SQLException e) {
            System.err.println("Error al conectar a MariaDB: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Crea la base de datos UMLS si no existe
     */
    private static void crearBaseDatos() throws SQLException {
        String createDB = "CREATE DATABASE IF NOT EXISTS " + DB_NAME + 
                         " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createDB);
            System.out.println("Base de datos '" + DB_NAME + "' verificada/creada");
            
            // Cambiar a la base de datos creada
            stmt.executeUpdate("USE " + DB_NAME);
            System.out.println("Usando base de datos: " + DB_NAME);
        }
    }
    
    /**
     * Crea las tablas necesarias para UMLS
     */
    private static void crearTablasUMLS() throws SQLException {
        System.out.println("Creando tablas UMLS...");
        
        // Tabla de conceptos (MRCONSO)
        String createConceptos = """
            CREATE TABLE IF NOT EXISTS conceptos (
                id INT AUTO_INCREMENT PRIMARY KEY,
                cui VARCHAR(10) NOT NULL,
                lat VARCHAR(3),
                ts VARCHAR(1),
                lui VARCHAR(10),
                stt VARCHAR(3),
                sui VARCHAR(10),
                ispref VARCHAR(1),
                aui VARCHAR(10),
                saui VARCHAR(50),
                scui VARCHAR(50),
                sdui VARCHAR(50),
                sab VARCHAR(20),
                tty VARCHAR(20),
                code VARCHAR(50),
                str TEXT,
                srl VARCHAR(10),
                suppress VARCHAR(1),
                cvf VARCHAR(50),
                INDEX idx_cui (cui),
                INDEX idx_sab (sab),
                INDEX idx_code (code)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;
        
        // Tabla de definiciones (MRDEF)
        String createDefiniciones = """
            CREATE TABLE IF NOT EXISTS definiciones (
                id INT AUTO_INCREMENT PRIMARY KEY,
                cui VARCHAR(10) NOT NULL,
                aui VARCHAR(10),
                atui VARCHAR(10),
                satui VARCHAR(50),
                sab VARCHAR(20),
                def TEXT,
                suppress VARCHAR(1),
                cvf VARCHAR(50),
                INDEX idx_cui (cui),
                INDEX idx_sab (sab)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;
        
        // Tabla de relaciones (MRREL)
        String createRelaciones = """
            CREATE TABLE IF NOT EXISTS relaciones (
                id INT AUTO_INCREMENT PRIMARY KEY,
                cui1 VARCHAR(10) NOT NULL,
                aui1 VARCHAR(10),
                stype1 VARCHAR(50),
                rel VARCHAR(10),
                cui2 VARCHAR(10) NOT NULL,
                aui2 VARCHAR(10),
                stype2 VARCHAR(50),
                rela VARCHAR(100),
                rui VARCHAR(10),
                srui VARCHAR(50),
                sab VARCHAR(20),
                sl VARCHAR(20),
                rg VARCHAR(10),
                dir VARCHAR(1),
                suppress VARCHAR(1),
                cvf VARCHAR(50),
                INDEX idx_cui1 (cui1),
                INDEX idx_cui2 (cui2),
                INDEX idx_rel (rel),
                INDEX idx_sab (sab)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createConceptos);
            System.out.println("✓ Tabla 'conceptos' creada");
            
            stmt.executeUpdate(createDefiniciones);
            System.out.println("✓ Tabla 'definiciones' creada");
            
            stmt.executeUpdate(createRelaciones);
            System.out.println("✓ Tabla 'relaciones' creada");
            
            System.out.println("Todas las tablas UMLS han sido creadas exitosamente");
        }
    }
    
    /**
     * Muestra el menú de opciones
     */
    private static void mostrarMenu() {
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.println("\n=== MENÚ IMPORTADOR UMLS ===");
            System.out.println("1. Importar conceptos desde archivo MRCONSO.RRF");
            System.out.println("2. Importar definiciones desde archivo MRDEF.RRF");
            System.out.println("3. Importar relaciones desde archivo MRREL.RRF");
            System.out.println("4. Consultar estadísticas de la base de datos");
            System.out.println("5. Buscar concepto por CUI");
            System.out.println("6. Salir");
            System.out.print("Selecciona una opción: ");
            
            int opcion = scanner.nextInt();
            scanner.nextLine(); // Consumir el salto de línea
            
            switch (opcion) {
                case 1:
                    System.out.print("Ingresa la ruta del archivo MRCONSO.RRF: ");
                    String rutaConceptos = scanner.nextLine();
                    importarConceptos(rutaConceptos);
                    break;
                case 2:
                    System.out.print("Ingresa la ruta del archivo MRDEF.RRF: ");
                    String rutaDefiniciones = scanner.nextLine();
                    importarDefiniciones(rutaDefiniciones);
                    break;
                case 3:
                    System.out.print("Ingresa la ruta del archivo MRREL.RRF: ");
                    String rutaRelaciones = scanner.nextLine();
                    importarRelaciones(rutaRelaciones);
                    break;
                case 4:
                    mostrarEstadisticas();
                    break;
                case 5:
                    System.out.print("Ingresa el CUI a buscar: ");
                    String cui = scanner.nextLine();
                    buscarConcepto(cui);
                    break;
                case 6:
                    System.out.println("Saliendo del programa...");
                    return;
                default:
                    System.out.println("Opción no válida");
            }
        }
    }
    
    /**
     * Importa conceptos desde archivo MRCONSO.RRF
     */
    private static void importarConceptos(String rutaArchivo) {
        System.out.println("Importando conceptos desde: " + rutaArchivo);
        
        String sql = """
            INSERT INTO conceptos (cui, lat, ts, lui, stt, sui, ispref, aui, saui, scui, 
                                 sdui, sab, tty, code, str, srl, suppress, cvf) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo));
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            connection.setAutoCommit(false);
            String linea;
            int contador = 0;
            
            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\|");
                
                if (campos.length >= 18) {
                    for (int i = 0; i < 18; i++) {
                        pstmt.setString(i + 1, campos[i].isEmpty() ? null : campos[i]);
                    }
                    pstmt.addBatch();
                    contador++;
                    
                    if (contador % 1000 == 0) {
                        pstmt.executeBatch();
                        connection.commit();
                        System.out.println("Procesados " + contador + " conceptos...");
                    }
                }
            }
            
            pstmt.executeBatch();
            connection.commit();
            connection.setAutoCommit(true);
            
            System.out.println("✓ Importación completada: " + contador + " conceptos importados");
            
        } catch (IOException | SQLException e) {
            System.err.println("Error al importar conceptos: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Importa definiciones desde archivo MRDEF.RRF
     */
    private static void importarDefiniciones(String rutaArchivo) {
        System.out.println("Importando definiciones desde: " + rutaArchivo);
        
        String sql = """
            INSERT INTO definiciones (cui, aui, atui, satui, sab, def, suppress, cvf) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo));
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            connection.setAutoCommit(false);
            String linea;
            int contador = 0;
            
            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\|");
                
                if (campos.length >= 8) {
                    for (int i = 0; i < 8; i++) {
                        pstmt.setString(i + 1, campos[i].isEmpty() ? null : campos[i]);
                    }
                    pstmt.addBatch();
                    contador++;
                    
                    if (contador % 1000 == 0) {
                        pstmt.executeBatch();
                        connection.commit();
                        System.out.println("Procesadas " + contador + " definiciones...");
                    }
                }
            }
            
            pstmt.executeBatch();
            connection.commit();
            connection.setAutoCommit(true);
            
            System.out.println("✓ Importación completada: " + contador + " definiciones importadas");
            
        } catch (IOException | SQLException e) {
            System.err.println("Error al importar definiciones: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Importa relaciones desde archivo MRREL.RRF
     */
    private static void importarRelaciones(String rutaArchivo) {
        System.out.println("Importando relaciones desde: " + rutaArchivo);
        
        String sql = """
            INSERT INTO relaciones (cui1, aui1, stype1, rel, cui2, aui2, stype2, rela, 
                                  rui, srui, sab, sl, rg, dir, suppress, cvf) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo));
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            connection.setAutoCommit(false);
            String linea;
            int contador = 0;
            
            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\|");
                
                if (campos.length >= 16) {
                    for (int i = 0; i < 16; i++) {
                        pstmt.setString(i + 1, campos[i].isEmpty() ? null : campos[i]);
                    }
                    pstmt.addBatch();
                    contador++;
                    
                    if (contador % 1000 == 0) {
                        pstmt.executeBatch();
                        connection.commit();
                        System.out.println("Procesadas " + contador + " relaciones...");
                    }
                }
            }
            
            pstmt.executeBatch();
            connection.commit();
            connection.setAutoCommit(true);
            
            System.out.println("✓ Importación completada: " + contador + " relaciones importadas");
            
        } catch (IOException | SQLException e) {
            System.err.println("Error al importar relaciones: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Muestra estadísticas de la base de datos
     */
    private static void mostrarEstadisticas() {
        System.out.println("\n=== ESTADÍSTICAS DE LA BASE DE DATOS ===");
        
        try (Statement stmt = connection.createStatement()) {
            // Contar conceptos
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM conceptos");
            if (rs.next()) {
                System.out.println("Total de conceptos: " + rs.getInt(1));
            }
            
            // Contar definiciones
            rs = stmt.executeQuery("SELECT COUNT(*) FROM definiciones");
            if (rs.next()) {
                System.out.println("Total de definiciones: " + rs.getInt(1));
            }
            
            // Contar relaciones
            rs = stmt.executeQuery("SELECT COUNT(*) FROM relaciones");
            if (rs.next()) {
                System.out.println("Total de relaciones: " + rs.getInt(1));
            }
            
            // Mostrar vocabularios más comunes
            System.out.println("\nVocabularios más comunes:");
            rs = stmt.executeQuery(
                "SELECT sab, COUNT(*) as total FROM conceptos GROUP BY sab ORDER BY total DESC LIMIT 10"
            );
            while (rs.next()) {
                System.out.println("  " + rs.getString("sab") + ": " + rs.getInt("total"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener estadísticas: " + e.getMessage());
        }
    }
    
    /**
     * Busca un concepto por CUI
     */
    private static void buscarConcepto(String cui) {
        System.out.println("\nBuscando concepto: " + cui);
        
        String sql = "SELECT * FROM conceptos WHERE cui = ? LIMIT 10";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, cui);
            ResultSet rs = pstmt.executeQuery();
            
            boolean encontrado = false;
            while (rs.next()) {
                encontrado = true;
                System.out.println("CUI: " + rs.getString("cui"));
                System.out.println("Término: " + rs.getString("str"));
                System.out.println("Vocabulario: " + rs.getString("sab"));
                System.out.println("Código: " + rs.getString("code"));
                System.out.println("---");
            }
            
            if (!encontrado) {
                System.out.println("No se encontró el concepto con CUI: " + cui);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al buscar concepto: " + e.getMessage());
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