package com.example.pruebamongodbcss;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PanelInicioMainDiseñoJorge extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(PanelInicioMain.class.getResource("/com/example/pruebamongodbcss/panelInicio.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 450);
        scene.getStylesheets().add(getClass().getResource("/com/example/pruebamongodbcss/app.css").toExternalForm());
        stage.setTitle("App1!");


        stage.setScene(scene);

        // Cierre seguro de la aplicación
        stage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch();
        //MongoDatabase baseMongo=GestorConexion.conectarEmpresa();
        //try{
        //    baseMongo.createCollection("Prueba");
        //    System.out.println("Colección creada exitosamente.");
        //}catch (Exception ex){
        //    System.out.println("Error");
        //}


    }
}