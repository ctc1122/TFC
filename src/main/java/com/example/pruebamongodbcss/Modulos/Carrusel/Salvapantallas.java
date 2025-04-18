package com.example.pruebamongodbcss.Modulos.Carrusel;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Salvapantallas extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/pruebamongodbcss/Carrusel.fxml"));
        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/Estilos/Carrusel.css").toExternalForm());


        scene.setFill(Color.TRANSPARENT);

        // Configuramos el stage como TRANSPARENT para que no haya borde ni barra
        primaryStage.initStyle(StageStyle.TRANSPARENT);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Carrusel estilo TikTok + FXML");
        primaryStage.show();

        // Opción: permitir arrastrar la ventana haciendo click y drag en cualquier parte del root
        makeDraggable(primaryStage, root);
    }

    /**
     * Permite arrastrar una ventana sin decoración al hacer drag en el nodo root.
     * (Opcional, pero casi siempre deseable).
     */
    private void makeDraggable(Stage stage, Parent root) {
        final double[] offsetX = {0};
        final double[] offsetY = {0};

        root.setOnMousePressed(event -> {
            offsetX[0] = event.getSceneX();
            offsetY[0] = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - offsetX[0]);
            stage.setY(event.getScreenY() - offsetY[0]);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
