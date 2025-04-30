package com.example.pruebamongodbcss;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.Modulos.Clinica.ClinicaMain;
import com.jfoenix.controls.JFXButton;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.layout.Pane;
import io.github.palexdev.materialfx.controls.MFXProgressSpinner;


public class PanelInicioController implements Initializable {



    @FXML
    private BorderPane root; // o el layout raíz que contiene todo


    @FXML
    private VBox sidebar;

    @FXML
    private JFXButton btnMenuPrincipal, btnAnimales, btnFichaje, btnSalir, btnToggleSidebar, but_clientes;

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
                but_clientes.setText("Clientes");

                // Quitar tooltips
                btnMenuPrincipal.setTooltip(null);
                btnAnimales.setTooltip(null);
                btnFichaje.setTooltip(null);
                btnSalir.setTooltip(null);
                but_clientes.setTooltip(null);

                // 🔧 LIMPIAR clase "collapsed" si existe
                sidebar.getStyleClass().removeIf(style -> style.equals("collapsed"));

            } else {
                btnMenuPrincipal.setText("");
                btnAnimales.setText("");
                btnFichaje.setText("");
                btnSalir.setText("");
                but_clientes.setText("");

                // Añadir tooltips
                btnMenuPrincipal.setTooltip(new Tooltip("Menú Principal"));
                btnAnimales.setTooltip(new Tooltip("Animales"));
                btnFichaje.setTooltip(new Tooltip("Fichaje"));
                btnSalir.setTooltip(new Tooltip("Salir"));
                but_clientes.setTooltip(new Tooltip("Clientes"));


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
       //TODO: Implementar la lógica para cerrar sesión
       System.out.println("Cerrando sesión...");
       Stage stage = (Stage) root.getScene().getWindow();
       stage.close();
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
}