package com.example.pruebamongodbcss.LaboratorioJorge;

import java.io.IOException;

import com.mongodb.client.MongoDatabase;

import Utilidades1.GestorConexion;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Ejecutador extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Inicializar la conexión a MongoDB
        MongoDatabase baseMongo = GestorConexion.conectarEmpresa();
        try {
            baseMongo.createCollection("Prueba");
            System.out.println("Colección creada exitosamente.");
        } catch (Exception ex) {
            System.out.println("Error al crear la colección: " + ex.getMessage());
        }

        // Cargar la interfaz gráfica
        //FXMLLoader fxmlLoader = new FXMLLoader(Ejecutador.class.getResource("/com/example/pruebamongodbcss/Clinica/Diagnostico/diagnostico-view.fxml"));
        FXMLLoader fxmlLoader = new FXMLLoader(Ejecutador.class.getResource("/com/example/pruebamongodbcss/panelInicio.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 450);
        scene.getStylesheets().add(getClass().getResource("/com/example/pruebamongodbcss/InicioSesion/PanelInicioSesionEstilo.css").toExternalForm());
        stage.setTitle("App1!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}