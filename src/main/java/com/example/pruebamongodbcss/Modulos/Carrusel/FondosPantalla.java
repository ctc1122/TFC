package com.example.pruebamongodbcss.Modulos.Carrusel;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class FondosPantalla extends Application {
    private int currentIndex = 0;
    private final String[] images = {
            getClass().getResource("/Imagenes/img1.png").toExternalForm(),
            getClass().getResource("/Imagenes/img2.jpg").toExternalForm(),
            getClass().getResource("/Imagenes/img3.jpg").toExternalForm()
    };

    private final String[] textos = {
            "Cabaña nevada en el bosque",
            "Camino de otoño al atardecer",
            "Tecnología y creatividad en acción"
    };

    private ImageView imageView;
    private Circle[] dots;

    Label textoLabel = new Label(textos[0]);

    @Override
    public void start(Stage primaryStage) {
        // Cargar imagen inicial
        imageView = new ImageView(new Image(images[0]));
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);

        // Usamos AnchorPane como contenedor principal
        AnchorPane root = new AnchorPane();

        // Vinculamos el tamaño de la imagen al del contenedor
        imageView.fitWidthProperty().bind(root.widthProperty());
        imageView.fitHeightProperty().bind(root.heightProperty());
        root.getChildren().add(imageView);


        //----------
        //  TEXTOS
        //----------

        textoLabel.setTextFill(Color.WHITE);
        textoLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-background-color: rgba(0, 0, 0, 0.4); -fx-padding: 10px;");
        textoLabel.setMaxWidth(Double.MAX_VALUE);
        textoLabel.setAlignment(Pos.CENTER);




        AnchorPane.setTopAnchor(textoLabel, 30.0);
        AnchorPane.setLeftAnchor(textoLabel, 0.0);
        AnchorPane.setRightAnchor(textoLabel, 0.0);
        root.getChildren().add(textoLabel);

        // Crear el HBox para los puntos
        HBox dotBox = new HBox(10);
        dotBox.setAlignment(Pos.CENTER);
        // Creamos los puntos
        dots = new Circle[images.length];
        for (int i = 0; i < images.length; i++) {
            Circle c = new Circle(6);
            c.setFill(i == 0 ? Color.web("#1c277a") : Color.web("#7d81a3"));
            dots[i] = c;
            dotBox.getChildren().add(c);
        }
        // Anclamos el dotBox al fondo del AnchorPane con márgenes
        AnchorPane.setLeftAnchor(dotBox, 0.0);
        AnchorPane.setRightAnchor(dotBox, 0.0);
        AnchorPane.setBottomAnchor(dotBox, 40.0); // 40px desde el fondo

        root.getChildren().add(dotBox);

        Scene scene = new Scene(root, 900, 650);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Carrusel Material Design");
        primaryStage.show();

        // Aquí iría el resto del código (animaciones, etc.)
        iniciarCarrusel();
    }

    private void iniciarCarrusel() {
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), imageView);
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), imageView);

        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        fadeOut.setOnFinished(e -> {
            currentIndex = (currentIndex + 1) % images.length;
            imageView.setImage(new Image(images[currentIndex]));
            actualizarPuntos();
            fadeIn.play();

            imageView.setImage(new Image(images[currentIndex]));
            textoLabel.setText(textos[currentIndex]);

        });

        fadeIn.setOnFinished(e -> {
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
                Platform.runLater(fadeOut::play);
            }).start();
        });

        fadeOut.play();
    }

    private void actualizarPuntos() {
        for (int i = 0; i < dots.length; i++) {
            dots[i].setFill(i == currentIndex ? Color.web("#1c277a") : Color.web("#7d81a3"));
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}
