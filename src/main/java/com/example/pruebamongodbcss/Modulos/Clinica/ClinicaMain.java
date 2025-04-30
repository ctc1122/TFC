package com.example.pruebamongodbcss.Modulos.Clinica;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Clase principal para iniciar la aplicación de gestión clínica veterinaria.
 */
public class ClinicaMain extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/clinica-view.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        
        stage.setTitle("Gestión Clínica Veterinaria");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 