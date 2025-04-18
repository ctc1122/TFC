package com.example.pruebamongodbcss.Modulos.AppChat;

import java.io.IOException;

import com.mongodb.client.MongoDatabase;

import Utilidades.GestorConexion;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class AppChatMain extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(AppChatMain.class.getResource("/com/example/pruebamongodbcss/chatPanel.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        scene.getStylesheets().add(getClass().getResource("/Estilos/chatOscuro.css").toExternalForm());
        stage.setTitle("App1!");
        // Quitar barra del sistema
        stage.initStyle(StageStyle.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);

        stage.setScene(scene);

        // Redondear bordes con clipping
        Rectangle clip = new Rectangle();
        clip.setArcWidth(30);
        clip.setArcHeight(30);

        // Ajusta al tamaño inicial
        clip.setWidth(scene.getWidth());
        clip.setHeight(scene.getHeight());

        // Escucha cambios dinámicos de tamaño
        scene.widthProperty().addListener((obs, oldVal, newVal) -> clip.setWidth(newVal.doubleValue()));
        scene.heightProperty().addListener((obs, oldVal, newVal) -> clip.setHeight(newVal.doubleValue()));

        scene.getRoot().setClip(clip);


        VentanaChat controlador = fxmlLoader.getController();
        controlador.habilitarRedimension(stage, scene);

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