package com.example.pruebamongodbcss.Modulos.Empresa;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Clase principal para ejecutar el módulo de Empresa de forma independiente
 * para pruebas y desarrollo.
 */
public class EmpresaMain extends Application {
    
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(EmpresaMain.class.getResource("/com/example/pruebamongodbcss/Empresa/empresa-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1024, 700);
        stage.setTitle("Gestión de Empresa");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // Primero, cargar los datos de prueba si no existen
        try {
            ServicioEmpresa servicio = new ServicioEmpresa();
            servicio.cargarDatosPrueba();
            System.out.println("Datos de prueba cargados correctamente.");
        } catch (Exception e) {
            System.err.println("Error al cargar datos de prueba: " + e.getMessage());
        }
        
        // Luego, iniciar la aplicación
        launch();
    }
} 