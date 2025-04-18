package com.example.pruebamongodbcss;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class PanelInicioController implements Initializable {


    @FXML
    private VBox topBox;

    @FXML
    private BorderPane root; // o el layout raíz que contiene todo


    @FXML
    private VBox sidebar;

    @FXML
    private JFXButton btnMenuPrincipal, btnAnimales, btnFichaje, btnSalir, btnToggleSidebar;

    @FXML
    private Label lblClinica;

    private boolean isCollapsed = false;

    @FXML
    private BorderPane sidebarContainer;


    @FXML
    private void toggleSidebar() {
        double startWidth = isCollapsed ? 45 : 200;
        double endWidth = isCollapsed ? 200 : 45;

        Timeline timeline = new Timeline();

        // Animar tanto el VBox como el BorderPane contenedor
        KeyValue kvSidebar = new KeyValue(sidebar.prefWidthProperty(), endWidth, Interpolator.EASE_BOTH);
        KeyValue kvContainer = new KeyValue(sidebarContainer.prefWidthProperty(), endWidth, Interpolator.EASE_BOTH);

        KeyFrame kf = new KeyFrame(Duration.seconds(0.3), kvSidebar, kvContainer);
        timeline.getKeyFrames().add(kf);
        timeline.play();

        // Animaciones adicionales
        FadeTransition fade = new FadeTransition(Duration.seconds(0.3), lblClinica);
        fade.setToValue(isCollapsed ? 1 : 0);
        fade.play();

        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.3), btnToggleSidebar);
        slide.setToX(isCollapsed ? 0 : -85);
        slide.play();

        timeline.setOnFinished(event -> {
            if (isCollapsed) {
                btnMenuPrincipal.setText("Menú Principal");
                btnAnimales.setText("Animales");
                btnFichaje.setText("Fichaje");
                btnSalir.setText("SALIR");

                // Quitar tooltips
                btnMenuPrincipal.setTooltip(null);
                btnAnimales.setTooltip(null);
                btnFichaje.setTooltip(null);
                btnSalir.setTooltip(null);

                // 🔧 LIMPIAR clase "collapsed" si existe
                sidebar.getStyleClass().removeIf(style -> style.equals("collapsed"));

            } else {
                btnMenuPrincipal.setText("");
                btnAnimales.setText("");
                btnFichaje.setText("");
                btnSalir.setText("");

                // Añadir tooltips
                btnMenuPrincipal.setTooltip(new Tooltip("Menú Principal"));
                btnAnimales.setTooltip(new Tooltip("Animales"));
                btnFichaje.setTooltip(new Tooltip("Fichaje"));
                btnSalir.setTooltip(new Tooltip("Salir"));


                // Añadir clase CSS
                if (!sidebar.getStyleClass().contains("collapsed")) {
                    sidebar.getStyleClass().add("collapsed");
                }

            }
            isCollapsed = !isCollapsed;
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Eliminar restricciones de tamaño mínimo si quieres permitir colapsar completamente
        sidebar.setMinWidth(0);
        sidebarContainer.setMinWidth(0);

        root.heightProperty().addListener((obs, oldVal, newVal) -> {
            topBox.setPrefHeight(newVal.doubleValue() / 2);
        });
        //AJUSTAMOS ANCHOS
        topBox.prefWidthProperty().bind(sidebarContainer.widthProperty().multiply(0.8));
    }
}