package com.example.pruebamongodbcss.Utils;

import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SplashUtils {
    public static Stage mostrarSplashDiagnostico() {
        StackPane loadingPane = new StackPane();
        loadingPane.setMinSize(400, 300);

        // Imagen de fondo
        ImageView backgroundImage = new ImageView();
        try {
            Image image = new Image(SplashUtils.class.getResourceAsStream("/ImagenCarga/carga1.png"));
            backgroundImage.setImage(image);
            backgroundImage.setFitWidth(400);
            backgroundImage.setFitHeight(300);
            backgroundImage.setPreserveRatio(false);
            backgroundImage.setSmooth(true);
            StackPane.setAlignment(backgroundImage, Pos.CENTER);
        } catch (Exception e) {
            System.err.println("No se pudo cargar la imagen de fondo: " + e.getMessage());
        }

        // Spinner
        MFXProgressSpinner spinner = new MFXProgressSpinner();
        spinner.setPrefSize(60, 60);
        spinner.setProgress(-1);

        // Texto
        Label cargandoLabel = new Label("Cargando...");
        cargandoLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #0F9D58;");

        VBox spinnerBox = new VBox(spinner, cargandoLabel);
        spinnerBox.setAlignment(Pos.CENTER);
        spinnerBox.setSpacing(20);

        loadingPane.getChildren().addAll(backgroundImage, spinnerBox);

        Scene scene = new Scene(loadingPane, 400, 300);
        Stage splashStage = new Stage();
        splashStage.setScene(scene);
        splashStage.initStyle(StageStyle.UNDECORATED);
        splashStage.setAlwaysOnTop(true);
        splashStage.centerOnScreen();
        splashStage.show();

        return splashStage;
    }
} 