package Utilidades;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RRFImporter {

    // Cambia estos datos según tu configuración
    private static final String DB_URL = "jdbc:mysql://localhost:3306/umls"; 
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "miclave";

    public static void main(String[] args) {
        String filePath = "./MRCONSO.RRF"; // Ruta local al archivo RRF

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            conn.setAutoCommit(false);

            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                int batchSize = 1000;
                int count = 0;

                String sql = "INSERT INTO MRCONSO (CUI, LAT, TS, LUI, STT, SUI, ISPREF, AUI, SAUI, SCUI, SDUI, SAB, TTY, CODE, STR, SRL, SUPPRESS, CVF) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    while ((line = reader.readLine()) != null) {
                        String[] fields = line.split("\\|", -1); // -1 to keep empty trailing fields

                        for (int i = 0; i < fields.length && i < 18; i++) {
                            stmt.setString(i + 1, fields[i]);
                        }

                        stmt.addBatch();

                        if (++count % batchSize == 0) {
                            stmt.executeBatch();
                        }
                    }

                    stmt.executeBatch(); // final
                    conn.commit();
                    System.out.println("Importación completada. Filas insertadas: " + count);
                }
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}
