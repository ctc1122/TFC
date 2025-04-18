package com.example.pruebamongodbcss;

import java.io.IOException;

import com.mongodb.client.MongoDatabase;

import Utilidades.GestorConexion;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppChatMain extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(AppChatMain.class.getResource("/com/example/pruebamongodbcss/panelInicio.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        scene.getStylesheets().add(getClass().getResource("/com/example/pruebamongodbcss/panelInicio.fxml").toExternalForm());
        stage.setTitle("App1!");


        stage.setScene(scene);



        stage.show();
    }

    public static void main(String[] args) {
        launch();
        MongoDatabase baseMongo=GestorConexion.conectarBD();
        try{
            baseMongo.createCollection("Prueba");
            System.out.println("Colección creada exitosamente.");
        }catch (Exception ex){
            System.out.println("Error");
        }


    }
}