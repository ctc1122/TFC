package com.example.pruebamongodbcss.Modulos.Carrusel;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class CarruselController {

    @FXML
    private AnchorPane root;        // El root transparente
    @FXML
    private AnchorPane cardPane;    // El contenedor blanco con bordes redondeados
    @FXML
    private ImageView mainImageView;
    @FXML
    private Label titleLabel;
    @FXML
    private Label closeLabel;

    private final List<SlideData> slides = new ArrayList<>();
    private int currentIndex = 0;
    private Timeline timeline;

    // Clase interna para agrupar la URL de imagen + título
    private static class SlideData {
        URL imageUrl;
        String title;
        SlideData(URL url, String title) {
            this.imageUrl = url;
            this.title = title;
        }
    }

    public void initialize() {
        // 1) Cargar tus imágenes y títulos
        slides.add(new SlideData(getClass().getResource("/Imagenes/img1.png"), "Netherland"));
        slides.add(new SlideData(getClass().getResource("/Imagenes/img2.jpg"), "Alemania"));
        slides.add(new SlideData(getClass().getResource("/Imagenes/img3.jpg"), "España"));

        // 2) Mostrar primer slide
        showSlide(currentIndex);

        // 3) Cambiar cada 3s
        timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> nextSlide()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        // 4) Escuchar cambios de tamaño de la escena para recolocar / recalcular
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener((o, oldW, newW) -> onResize(newW.doubleValue(), newScene.getHeight()));
                newScene.heightProperty().addListener((o, oldH, newH) -> onResize(newScene.getWidth(), newH.doubleValue()));
            }
        });
    }

    /**
     * Muestra el slide de 'currentIndex' con un fade.
     */
    private void showSlide(int index) {
        if (slides.isEmpty()) return;
        SlideData data = slides.get(index);
        Image newImg = new Image(data.imageUrl.toExternalForm());

        FadeTransition ftOut = new FadeTransition(Duration.seconds(0.5), mainImageView);
        ftOut.setToValue(0.0);
        ftOut.setOnFinished(ev -> {
            mainImageView.setImage(newImg);
            titleLabel.setText(data.title);

            FadeTransition ftIn = new FadeTransition(Duration.seconds(0.5), mainImageView);
            ftIn.setToValue(1.0);
            ftIn.play();
        });
        ftOut.play();
    }

    private void nextSlide() {
        currentIndex = (currentIndex + 1) % slides.size();
        showSlide(currentIndex);
    }

    /**
     * Ajusta cosas si se redimensiona la ventana.
     * En este ejemplo:
     * - Centra el "X" si quieres, o recalcula su LayoutX si quieres que se mantenga a 20 px del borde derecho.
     */
    private void onResize(double newWidth, double newHeight) {
        // cardPane tiene prefWidth=900, prefHeight=500, pero se "escala" dentro del StackPane.
        // Si quieres reposicionar la X Label, hazlo así:
        double cardCurrentWidth = cardPane.getWidth();
        // Layout X = cardCurrentWidth - 40 => deja 40 px de margen al borde
        closeLabel.setLayoutX(cardCurrentWidth - 40);
        // Ajusta la Y si quieres.
    }

    /**
     * Cierra la ventana al hacer click en X.
     */
    @FXML
    private void closeWindow() {
        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }
}
