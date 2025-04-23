package com.example.pruebamongodbcss.Modulos.Videollamada;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainVideollamada extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(MainVideollamada.class.getResource("videollamada-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 300);
        stage.setTitle("MedConnect - Iniciar Videollamada");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // Inicializar JCEF antes de lanzar la aplicación JavaFX
        try {
            // Asegurarse de que JCEF esté disponible
            Class.forName("org.cef.CefApp");
            launch(args);
        } catch (ClassNotFoundException e) {
            System.err.println("Error: JCEF no está disponible en el classpath.");
            System.err.println("Por favor, asegúrese de que la dependencia jcefmaven está correctamente configurada.");
            e.printStackTrace();
        }
    }
} 