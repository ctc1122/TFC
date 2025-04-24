package com.example.pruebamongodbcss.Modulos.UMLSSearch;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class UMLSSearchMain extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/UMLSSearch-view.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 600, 500);
        primaryStage.setTitle("Búsqueda de Términos Médicos");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 