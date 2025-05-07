package com.example.pruebamongodbcss;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;

import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;


public class PanelInicioController implements Initializable {



    @FXML
    private BorderPane root; // o el layout raíz que contiene todo


    @FXML
    private VBox sidebar;

    @FXML
    private JFXButton btnMenuPrincipal, btnAnimales, btnFichaje, btnSalir, but_clientes, btnChicha;

    @FXML
    private Label lblClinica;

    private boolean isCollapsed = false;
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private Pane mainPane;

    private boolean menuVisible = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // sidebar.setMinWidth(0);

        // Asignar iconos a los botones
        setButtonIcon(btnMenuPrincipal, "/Iconos/iconInicio4.png", 32, 32);
        setButtonIcon(btnAnimales, "/Iconos/iconPet2.png", 32, 32);
        setButtonIcon(but_clientes, "/Iconos/IconPruebaClientes.png", 32, 32);
        setButtonIcon(btnFichaje, "/Iconos/iconClock2.png", 32, 32);
        setButtonIcon(btnSalir, "/Iconos/iconSalir.png", 32, 32);

        // Tooltips para los botones circulares
        btnMenuPrincipal.setTooltip(new Tooltip("Menú Principal"));
        btnAnimales.setTooltip(new Tooltip("Animales"));
        but_clientes.setTooltip(new Tooltip("Clientes"));
        btnFichaje.setTooltip(new Tooltip("Fichaje"));
        btnSalir.setTooltip(new Tooltip("Cerrar sesión"));

        // Aplicar la clase CSS circular a los botones del menú radial
        JFXButton[] botones = {btnMenuPrincipal, btnAnimales, but_clientes, btnFichaje, btnSalir};
        for (JFXButton boton : botones) {
            boton.getStyleClass().removeAll("itemMenu");
            if (!boton.getStyleClass().contains("circleMenuButton")) {
                boton.getStyleClass().add("circleMenuButton");
            }
            boton.setText("");
        }

        // Configurar la funcionalidad de arrastre para el botón Chicha
        configurarArrastreBoton(btnChicha);

        // Configurar menú radial
        btnChicha.setOnAction(e -> toggleMenuRadial());

        // Configurar evento para el botón de animales (acceso al módulo de clínica)
        btnAnimales.setOnAction(event -> abrirModuloClinica());
        
        // Configurar evento para el botón de clientes (también accede al módulo de clínica)
        but_clientes.setOnAction(event -> abrirModuloClinica());
        
        // Configurar evento para el botón de menú principal
        btnMenuPrincipal.setOnAction(event -> restaurarVistaPrincipal());
    }
    
    /**
     * Abre el módulo de gestión clínica veterinaria
     */
    private void abrirModuloClinica() {
        try {
            // Cargar la vista de la clínica
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/clinica-view.fxml"));
            Parent contenido = loader.load();
            
            // Agregar opciones de JVM necesarias en tiempo de ejecución
            System.setProperty("javafx.controls.behaviour", "com.sun.javafx.scene.control.behavior");
            
            // Obtener el BorderPane central y reemplazar su contenido
            BorderPane centerPane = (BorderPane) root.getCenter();
            centerPane.setCenter(contenido);
            
            // Actualizar el título (opcional)
            lblClinica.setText("Gestión Clínica");
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar el módulo de clínica: " + e.getMessage());
        }
    }

    @FXML
    public void cerrarSesion(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/InicioSesion/PruebaDoblePanel.fxml"));
            Parent loginRoot = loader.load();
            Stage stage = (Stage) root.getScene().getWindow();
            stage.getScene().setRoot(loginRoot);
            stage.setWidth(900);
            stage.setHeight(450);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar el panel de inicio de sesión: " + e.getMessage());
        }
    }

    /**
     * Restaura la vista principal por defecto
     */
    private void restaurarVistaPrincipal() {
        // Obtener el BorderPane central
        BorderPane centerPane = (BorderPane) root.getCenter();
        
        // Restaurar contenido por defecto (un Pane con el spinner)
        Pane defaultPane = new Pane();
        defaultPane.setPrefHeight(400.0);
        defaultPane.setPrefWidth(622.0);
        defaultPane.getStylesheets().add("@app.css");
        
        // Añadir el spinner
        try {
            MFXProgressSpinner spinner = new MFXProgressSpinner();
            spinner.setLayoutX(188.0);
            spinner.setLayoutY(225.0);
            spinner.setProgress(0.0);
            spinner.getStyleClass().add("mfx-progress-spinner");
            spinner.getStylesheets().add("@MFXProgressSpinner.css");
            defaultPane.getChildren().add(spinner);
        } catch (Exception e) {
            System.err.println("Error al crear el spinner: " + e.getMessage());
        }
        
        // Reemplazar el contenido central
        centerPane.setCenter(defaultPane);
        
        // Restaurar título
        lblClinica.setText("Clínica Veterinaria");
    }

    /**
     * Configura la funcionalidad de arrastre para un botón
     */
    private void configurarArrastreBoton(JFXButton boton) {
        boton.setOnMousePressed(event -> {
            xOffset = event.getSceneX() - boton.getLayoutX();
            yOffset = event.getSceneY() - boton.getLayoutY();
        });

        boton.setOnMouseDragged(event -> {
            boton.setLayoutX(event.getSceneX() - xOffset);
            boton.setLayoutY(event.getSceneY() - yOffset);
        });
    }

    private void setButtonIcon(JFXButton button, String iconPath, double width, double height) {
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
        icon.setFitWidth(width);
        icon.setFitHeight(height);
        icon.setPreserveRatio(true);
        button.setGraphic(icon);
    }

    private void toggleMenuRadial() {
        JFXButton[] botones = {btnMenuPrincipal, btnAnimales, but_clientes, btnFichaje, btnSalir};
        double centerX = btnChicha.getLayoutX() + btnChicha.getWidth() / 2;
        double centerY = btnChicha.getLayoutY() + btnChicha.getHeight() / 2;
        double radio = 120; // Distancia desde el centro
        int n = botones.length;
        if (!menuVisible) {
            for (int i = 0; i < n; i++) {
                double angle = 2 * Math.PI * i / n;
                double x = centerX + radio * Math.cos(angle) - botones[i].getWidth() / 2;
                double y = centerY + radio * Math.sin(angle) - botones[i].getHeight() / 2;
                botones[i].setVisible(true);
                animateButtonTo(botones[i], x, y, true);
            }
        } else {
            for (JFXButton boton : botones) {
                animateButtonTo(boton, centerX - boton.getWidth() / 2, centerY - boton.getHeight() / 2, false);
            }
        }
        menuVisible = !menuVisible;
    }

    private void animateButtonTo(JFXButton boton, double x, double y, boolean show) {
        Timeline timeline = new Timeline();
        KeyValue kvX = new KeyValue(boton.layoutXProperty(), x, Interpolator.EASE_BOTH);
        KeyValue kvY = new KeyValue(boton.layoutYProperty(), y, Interpolator.EASE_BOTH);
        KeyFrame kf = new KeyFrame(Duration.seconds(0.3), kvX, kvY);
        timeline.getKeyFrames().add(kf);
        if (!show) {
            timeline.setOnFinished(e -> boton.setVisible(false));
        }
        timeline.play();
    }
}