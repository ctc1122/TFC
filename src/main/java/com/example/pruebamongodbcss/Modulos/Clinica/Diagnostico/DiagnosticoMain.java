package com.example.pruebamongodbcss.Modulos.Clinica.Diagnostico;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Clase principal para iniciar el módulo de diagnósticos de forma independiente.
 * Útil para desarrollo y pruebas.
 */
public class DiagnosticoMain extends Application {
    
    @Override
    public void start(Stage stage) throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(DiagnosticoMain.class.getResource("/com/example/pruebamongodbcss/Clinica/Diagnostico/diagnostico-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            
            // Obtener el controlador para poder configurarlo si es necesario
            DiagnosticoController controller = fxmlLoader.getController();
            
            // Configurar la ventana
            stage.setTitle("Sistema de Diagnósticos Médicos");
            stage.setScene(scene);
            
            // Intentar cargar un ícono
            try {
                stage.getIcons().add(new Image(DiagnosticoMain.class.getResourceAsStream("/Iconos/icono-clinica.png")));
            } catch (Exception e) {
                System.err.println("No se pudo cargar el ícono: " + e.getMessage());
            }
            
            // Configurar cierre de recursos al cerrar la ventana
            stage.setOnCloseRequest(event -> {
                controller.onClose();
            });
            
            // Mostrar ventana
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 