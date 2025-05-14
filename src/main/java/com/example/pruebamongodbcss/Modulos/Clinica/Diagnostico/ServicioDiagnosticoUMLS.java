package com.example.pruebamongodbcss.Modulos.Clinica.Diagnostico;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Servicio para interactuar con la tabla de diagnósticos en MariaDB.
 */
public class ServicioDiagnosticoUMLS {
    private static final String URL = "jdbc:mariadb://localhost:3306/umls";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "miclave";
    
    /**
     * Establece la conexión con la base de datos MariaDB.
     * @return Connection objeto de conexión a la base de datos
     * @throws SQLException si hay un error de conexión
     */
    private Connection getConnection() throws SQLException {
        try {
            // Cargar el driver de forma explícita
            Class.forName("org.mariadb.jdbc.Driver");
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("Error al cargar el driver de MariaDB: " + e.getMessage());
            throw new SQLException("Error al cargar el driver: " + e.getMessage(), e);
        }
    }
    
    /**
     * Obtiene un listado completo de diagnósticos
     * @return Lista observable de diagnósticos
     */
    public ObservableList<ModeloDiagnosticoUMLS> obtenerTodosDiagnosticos() {
        ObservableList<ModeloDiagnosticoUMLS> diagnosticos = FXCollections.observableArrayList();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Diagnosticos LIMIT 1000")) {
            
            while (rs.next()) {
                ModeloDiagnosticoUMLS diagnostico = mapearResultado(rs);
                diagnosticos.add(diagnostico);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener diagnósticos: " + e.getMessage());
            e.printStackTrace();
        }
        
        return diagnosticos;
    }
    
    /**
     * Busca diagnósticos que contengan el texto especificado en el campo STR (descripción)
     * @param texto Texto a buscar
     * @return Lista observable de diagnósticos que coinciden con la búsqueda
     */
    public ObservableList<ModeloDiagnosticoUMLS> buscarDiagnosticosPorTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return FXCollections.observableArrayList();
        }
        
        ObservableList<ModeloDiagnosticoUMLS> diagnosticos = FXCollections.observableArrayList();
        String query = "SELECT * FROM Diagnosticos WHERE STR LIKE ? LIMIT 50";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, "%" + texto + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ModeloDiagnosticoUMLS diagnostico = mapearResultado(rs);
                    diagnosticos.add(diagnostico);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error al buscar diagnósticos: " + e.getMessage());
            e.printStackTrace();
        }
        
        return diagnosticos;
    }
    
    /**
     * Busca un diagnóstico por su CUI (Concept Unique Identifier)
     * @param cui Identificador único del concepto
     * @return ModeloDiagnosticoUMLS encontrado o null si no existe
     */
    public ModeloDiagnosticoUMLS buscarDiagnosticoPorCUI(String cui) {
        if (cui == null || cui.isBlank()) {
            return null;
        }
        
        String query = "SELECT * FROM Diagnosticos WHERE CUI = ? LIMIT 1";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, cui);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultado(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error al buscar diagnóstico por CUI: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Obtiene diagnósticos agrupados por código (elimina duplicados, manteniendo solo la entrada principal)
     * @return Lista de diagnósticos sin duplicados
     */
    public List<ModeloDiagnosticoUMLS> obtenerDiagnosticosUnicos() {
        List<ModeloDiagnosticoUMLS> diagnosticos = new ArrayList<>();
        String query = "SELECT * FROM Diagnosticos GROUP BY CUI LIMIT 1000";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                ModeloDiagnosticoUMLS diagnostico = mapearResultado(rs);
                diagnosticos.add(diagnostico);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener diagnósticos únicos: " + e.getMessage());
            e.printStackTrace();
        }
        
        return diagnosticos;
    }
    
    /**
     * Obtiene un listado limitado de diagnósticos
     * @param limite Número máximo de diagnósticos a devolver
     * @return Lista observable de diagnósticos limitada
     */
    public ObservableList<ModeloDiagnosticoUMLS> obtenerDiagnosticosLimitados(int limite) {
        ObservableList<ModeloDiagnosticoUMLS> diagnosticos = FXCollections.observableArrayList();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Diagnosticos LIMIT ?")) {
            
            stmt.setInt(1, limite);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ModeloDiagnosticoUMLS diagnostico = mapearResultado(rs);
                    diagnosticos.add(diagnostico);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener diagnósticos limitados: " + e.getMessage());
            e.printStackTrace();
        }
        
        return diagnosticos;
    }
    
    /**
     * Mapea un ResultSet a un objeto ModeloDiagnosticoUMLS
     * @param rs ResultSet a mapear
     * @return ModeloDiagnosticoUMLS con datos del ResultSet
     * @throws SQLException si hay un error de acceso a datos
     */
    private ModeloDiagnosticoUMLS mapearResultado(ResultSet rs) throws SQLException {
        return new ModeloDiagnosticoUMLS(
            rs.getString("CUI"),
            rs.getString("LAT"),
            rs.getString("TS"),
            rs.getString("LUI"),
            rs.getString("STT"),
            rs.getString("SUI"),
            rs.getString("ISPREF"),
            rs.getString("AUI"),
            rs.getString("SAUI"),
            rs.getString("SCUI"),
            rs.getString("SDUI"),
            rs.getString("SAB"),
            rs.getString("TTY"),
            rs.getString("CODE"),
            rs.getString("STR"),
            rs.getString("SRL"),
            rs.getString("SUPPRESS"),
            rs.getString("CVF")
        );
    }
    
    /**
     * Comprueba la conexión a la base de datos.
     * @return true si la conexión se estableció correctamente, false en caso contrario
     */
    public boolean comprobarConexion() {
        try {
            System.out.println("Intentando conectar a: " + URL);
            Connection conn = getConnection();
            boolean conectado = conn != null && !conn.isClosed();
            if (conectado) {
                System.out.println("Conexión establecida correctamente!");
            }
            if (conn != null) {
                conn.close();
            }
            return conectado;
        } catch (SQLException e) {
            System.err.println("Error al comprobar conexión: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
} 