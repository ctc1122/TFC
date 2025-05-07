package com.example.pruebamongodbcss;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.Modulos.Clinica.ClinicaController;
import com.example.pruebamongodbcss.Modulos.Empresa.ModeloUsuario;
import com.example.pruebamongodbcss.Modulos.Empresa.ServicioEmpresa;
import com.jfoenix.controls.JFXButton;

import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
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
    private JFXButton btnMenuPrincipal, btnAnimales, btnFichaje, btnSalir, btnToggleSidebar, but_clientes, btnEmpresa, btnChicha;

    @FXML
    private Label lblClinica;

    private boolean isCollapsed = false;
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private BorderPane sidebarContainer;
    
    // Usuario actual de la sesión
    private ModeloUsuario usuarioActual;
    private ServicioEmpresa servicioEmpresa;

    private boolean menuVisible = false;

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
        // sidebar.setMinWidth(0);

        // Configurar iconos y tooltips
        setButtonIcon(btnMenuPrincipal, "/Iconos/iconInicio4.png", 32, 32);
        setButtonIcon(btnAnimales, "/Iconos/iconPet2.png", 32, 32);
        setButtonIcon(but_clientes, "/Iconos/IconPruebaClientes.png", 32, 32);
        setButtonIcon(btnFichaje, "/Iconos/iconClock2.png", 32, 32);
        setButtonIcon(btnSalir, "/Iconos/iconSalir.png", 32, 32);

        // Tooltips para los botones
        btnMenuPrincipal.setTooltip(new Tooltip("Menú Principal"));
        btnAnimales.setTooltip(new Tooltip("Animales"));
        but_clientes.setTooltip(new Tooltip("Clientes"));
        btnFichaje.setTooltip(new Tooltip("Fichaje"));
        btnSalir.setTooltip(new Tooltip("Cerrar sesión"));

        // Configurar eventos
        btnChicha.setOnAction(e -> toggleMenuRadial());
        btnAnimales.setOnAction(event -> abrirModuloClinica());
        but_clientes.setOnAction(event -> abrirModuloClinicaConCitas());
        btnMenuPrincipal.setOnAction(event -> restaurarVistaPrincipal());
        btnSalir.setOnAction(event -> cerrarSesion());

        // Inicializar servicio
        servicioEmpresa = new ServicioEmpresa();
    }
    
    /**
     * Establece el usuario actual de la sesión y configura la interfaz según sus permisos
     */
    public void setUsuarioActual(ModeloUsuario usuario) {
        this.usuarioActual = usuario;
        
        // Si existe el botón de empresa, configurar su visibilidad según el rol
        if (btnEmpresa != null && usuario != null) {
            boolean esAdmin = usuario.esAdmin();
            System.out.println("Usuario: " + usuario.getNombre() + ", Es Admin: " + esAdmin + ", Rol: " + usuario.getRol());
            
            btnEmpresa.setVisible(esAdmin);
            btnEmpresa.setManaged(esAdmin);
            
            // Corregir icono del botón Empresa
            try {
                ImageView empresaIcon = new ImageView(new javafx.scene.image.Image(
                    getClass().getResourceAsStream("/Iconos/iconEmpresa.png")));
                empresaIcon.setFitHeight(22.0);
                empresaIcon.setFitWidth(20.0);
                empresaIcon.setPreserveRatio(true);
                btnEmpresa.setGraphic(empresaIcon);
            } catch (Exception e) {
                System.err.println("Error al cargar icono de empresa: " + e.getMessage());
            }
        } else {
            System.out.println("Botón empresa no encontrado o usuario nulo: " + 
                (btnEmpresa == null ? "btnEmpresa es null" : "btnEmpresa existe") + ", " +
                (usuario == null ? "usuario es null" : "usuario existe"));
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
            System.out.println("Abriendo módulo de empresa como administrador...");
            
            
            try {
                // Cargar la vista sin controlador
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Empresa/empresa-view.fxml"));
                
                // Cargar el FXML
                Parent contenido = loader.load();
                System.out.println("FXML cargado correctamente.");
                
                // Obtener el BorderPane central y reemplazar su contenido
                BorderPane centerPane = (BorderPane) root.getCenter();
                centerPane.setCenter(contenido);
                
                // Actualizar el título
                lblClinica.setText("Gestión de Empresa");
                
            } catch (Exception e) {
                System.err.println("Error al cargar el FXML: " + e.getMessage());
                e.printStackTrace();
                throw new IOException("Error al procesar el FXML: " + e.getMessage(), e);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            String mensajeError = "Error al cargar el módulo de empresa: " + e.getMessage();
            System.err.println(mensajeError);
            mostrarError("Error", mensajeError);
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