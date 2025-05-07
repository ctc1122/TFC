package com.example.pruebamongodbcss;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.Modulos.Clinica.ClinicaMain;
import com.example.pruebamongodbcss.Modulos.Clinica.ClinicaController;
import com.example.pruebamongodbcss.Modulos.Empresa.ModeloUsuario;
import com.example.pruebamongodbcss.Modulos.Empresa.ServicioEmpresa;
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
    private JFXButton btnMenuPrincipal, btnAnimales, btnFichaje, btnSalir, btnToggleSidebar, but_clientes, btnEmpresa;

    @FXML
    private Label lblClinica;

    private boolean isCollapsed = false;

    @FXML
    private BorderPane sidebarContainer;
    
    // Usuario actual de la sesión
    private ModeloUsuario usuarioActual;
    private ServicioEmpresa servicioEmpresa;

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
                if (btnEmpresa != null) {
                    btnEmpresa.setText("Empresa");
                }

                // Quitar tooltips
                btnMenuPrincipal.setTooltip(null);
                btnAnimales.setTooltip(null);
                btnFichaje.setTooltip(null);
                btnSalir.setTooltip(null);
                but_clientes.setTooltip(null);
                if (btnEmpresa != null) {
                    btnEmpresa.setTooltip(null);
                }

                // 🔧 LIMPIAR clase "collapsed" si existe
                sidebar.getStyleClass().removeIf(style -> style.equals("collapsed"));

            } else {
                btnMenuPrincipal.setText("");
                btnAnimales.setText("");
                btnFichaje.setText("");
                btnSalir.setText("");
                but_clientes.setText("");
                if (btnEmpresa != null) {
                    btnEmpresa.setText("");
                }

                // Añadir tooltips
                btnMenuPrincipal.setTooltip(new Tooltip("Menú Principal"));
                btnAnimales.setTooltip(new Tooltip("Animales"));
                btnFichaje.setTooltip(new Tooltip("Fichaje"));
                btnSalir.setTooltip(new Tooltip("Salir"));
                but_clientes.setTooltip(new Tooltip("Clientes"));
                if (btnEmpresa != null) {
                    btnEmpresa.setTooltip(new Tooltip("Empresa"));
                }


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

        
        // Inicializar servicio para gestionar usuarios
        servicioEmpresa = new ServicioEmpresa();
        
        // Configurar evento para el botón de animales (acceso al módulo de clínica)
        btnAnimales.setOnAction(event -> abrirModuloClinica());
        
        // Configurar evento para el botón de clientes (accede al módulo de clínica con la pestaña de citas)
        but_clientes.setOnAction(event -> abrirModuloClinicaConCitas());
        
        // Configurar evento para el botón de menú principal
        btnMenuPrincipal.setOnAction(event -> restaurarVistaPrincipal());
        
        // Configurar evento para el botón de empresa (solo visible para administradores)
        if (btnEmpresa != null) {
            btnEmpresa.setOnAction(event -> abrirModuloEmpresa());
            
            // Ocultar el botón de empresa hasta que sepamos si el usuario es administrador
            btnEmpresa.setVisible(false);
            btnEmpresa.setManaged(false);
        }
    }
    
    /**
     * Establece el usuario actual de la sesión y configura la interfaz según sus permisos
     */
    public void setUsuarioActual(ModeloUsuario usuario) {
        this.usuarioActual = usuario;
        
        // Si existe el botón de empresa, configurar su visibilidad según el rol
        if (btnEmpresa != null && usuario != null) {
            boolean esAdmin = usuario.esAdmin();
            btnEmpresa.setVisible(esAdmin);
            btnEmpresa.setManaged(esAdmin);
        }
        
        // Actualizar el nombre del usuario en la interfaz si se requiere
        if (lblClinica != null) {
            if (usuario != null) {
                lblClinica.setText("Bienvenido, " + usuario.getNombre());
            } else {
                lblClinica.setText("Clínica Veterinaria");
            }
        }
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
    
    /**
     * Abre el módulo de gestión clínica veterinaria y selecciona la pestaña de citas
     */
    private void abrirModuloClinicaConCitas() {
        try {
            // Cargar la vista de la clínica
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/clinica-view.fxml"));
            Parent contenido = loader.load();
            
            // Obtener el controlador para poder acceder a los componentes
            ClinicaController controller = loader.getController();
            
            // Agregar opciones de JVM necesarias en tiempo de ejecución
            System.setProperty("javafx.controls.behaviour", "com.sun.javafx.scene.control.behavior");
            
            // Obtener el BorderPane central y reemplazar su contenido
            BorderPane centerPane = (BorderPane) root.getCenter();
            centerPane.setCenter(contenido);
            
            // Seleccionar la pestaña de citas (índice 3 en el TabPane)
            controller.seleccionarTabCitas();
            
            // Actualizar el título
            lblClinica.setText("Gestión de Citas");
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar el módulo de clínica con citas: " + e.getMessage());
        }
    }
    
    /**
     * Abre el módulo de gestión de empresa (usuarios y veterinarios)
     * Solo accesible para administradores
     */
    private void abrirModuloEmpresa() {
        // Verificar si el usuario es administrador
        if (usuarioActual == null || !usuarioActual.esAdmin()) {
            mostrarError("Acceso denegado", "Solo los administradores pueden acceder a esta funcionalidad.");
            return;
        }
        
        try {
            // Cargar la vista de empresa
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Empresa/empresa-view.fxml"));
            Parent contenido = loader.load();
            
            // Obtener el BorderPane central y reemplazar su contenido
            BorderPane centerPane = (BorderPane) root.getCenter();
            centerPane.setCenter(contenido);
            
            // Actualizar el título
            lblClinica.setText("Gestión de Empresa");
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar el módulo de empresa: " + e.getMessage());
            mostrarError("Error", "Error al cargar el módulo de empresa: " + e.getMessage());
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
    
    /**
     * Muestra un mensaje de error
     */
    private void mostrarError(String titulo, String mensaje) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}