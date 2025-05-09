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
    private JFXButton btnMenuPrincipalCarousel, btnAnimalesCarousel, btnFichajeCarousel, btnSalirCarousel, but_clientesCarousel;

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
    private boolean isCarouselMode = false;
    private Pane carouselContainer;

    private javafx.animation.PauseTransition holdTimer;
    private boolean isHoldingToggle = false;

    private Pane zonaAnclaje;
    private boolean zonaAnclajeCreada = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configurar el contenedor del carrusel
        carouselContainer = new Pane();
        carouselContainer.setPrefWidth(200);
        carouselContainer.setStyle("-fx-background-color: transparent;");
        
        // Configurar iconos y tooltips para el menú lateral
        setButtonIcon(btnMenuPrincipal, "/Iconos/iconInicio4.png", 32, 32);
        setButtonIcon(btnAnimales, "/Iconos/iconPet2.png", 32, 32);
        setButtonIcon(but_clientes, "/Iconos/IconPruebaClientes.png", 32, 32);
        setButtonIcon(btnFichaje, "/Iconos/iconClock2.png", 32, 32);
        setButtonIcon(btnSalir, "/Iconos/iconSalir.png", 32, 32);

        // Configurar iconos y tooltips para los botones del carrusel
        setButtonIcon(btnMenuPrincipalCarousel, "/Iconos/iconInicio4.png", 32, 32);
        setButtonIcon(btnAnimalesCarousel, "/Iconos/iconPet2.png", 32, 32);
        setButtonIcon(but_clientesCarousel, "/Iconos/IconPruebaClientes.png", 32, 32);
        setButtonIcon(btnFichajeCarousel, "/Iconos/iconClock2.png", 32, 32);
        setButtonIcon(btnSalirCarousel, "/Iconos/iconSalir.png", 32, 32);

        // Tooltips para los botones del carrusel
        btnMenuPrincipalCarousel.setTooltip(new Tooltip("Menú Principal"));
        btnAnimalesCarousel.setTooltip(new Tooltip("Animales"));
        but_clientesCarousel.setTooltip(new Tooltip("Clientes"));
        btnFichajeCarousel.setTooltip(new Tooltip("Fichaje"));
        btnSalirCarousel.setTooltip(new Tooltip("Cerrar sesión"));

        // Configurar eventos del carrusel
        btnChicha.setOnAction(e -> toggleMenuRadial());
        btnMenuPrincipalCarousel.setOnAction(event -> restaurarVistaPrincipal());
        btnAnimalesCarousel.setOnAction(event -> abrirModuloClinica());
        but_clientesCarousel.setOnAction(event -> abrirModuloClinicaConCitas());
        btnSalirCarousel.setOnAction(event -> cerrarSesion());

        // Configurar eventos del menú lateral
        btnToggleSidebar.setOnMousePressed(event -> {
            isHoldingToggle = true;
            holdTimer = new javafx.animation.PauseTransition(Duration.seconds(0.5));
            holdTimer.setOnFinished(e -> {
                if (isHoldingToggle) {
                    activarModoCarrusel();
                }
            });
            holdTimer.play();
        });
        btnToggleSidebar.setOnMouseReleased(event -> {
            isHoldingToggle = false;
            if (holdTimer != null) holdTimer.stop();
        });
        btnToggleSidebar.setOnMouseDragged(event -> {
            if (isHoldingToggle && holdTimer != null && holdTimer.getCurrentTime().greaterThanOrEqualTo(Duration.seconds(0.5))) {
                activarModoCarrusel();
                isHoldingToggle = false;
                holdTimer.stop();
            }
        });
        btnToggleSidebar.setOnAction(event -> {
            if (!isCarouselMode) {
                toggleSidebar();
            }
        });

        // Vincular botones del menú lateral a sus métodos
        btnMenuPrincipal.setOnAction(event -> restaurarVistaPrincipal());
        btnAnimales.setOnAction(event -> abrirModuloClinica());
        but_clientes.setOnAction(event -> abrirModuloClinicaConCitas());
        btnFichaje.setOnAction(event -> abrirModuloFichaje());
        btnSalir.setOnAction(event -> cerrarSesion());
        if (btnEmpresa != null) {
            btnEmpresa.setOnAction(event -> abrirModuloEmpresa());
        }

        // Inicializar servicio
        servicioEmpresa = new ServicioEmpresa();
        
        // Configurar arrastre del botón
        configurarArrastreBoton(btnChicha);
        
        // Configurar botones del menú radial
        JFXButton[] botonesCarousel = {btnMenuPrincipalCarousel, btnAnimalesCarousel, but_clientesCarousel, btnFichajeCarousel, btnSalirCarousel};
        for (JFXButton boton : botonesCarousel) {
            boton.getStyleClass().removeAll("itemMenu");
            if (!boton.getStyleClass().contains("circleMenuButton")) {
                boton.getStyleClass().add("circleMenuButton");
            }
            boton.setText(""); // Solo icono, sin texto
        }

        // Mostrar sidebar por defecto
        mostrarSidebar();

        // Al hacer clic en la uva, mostrar/ocultar el menú radial
        btnChicha.setOnAction(event -> {
            if (isCarouselMode) {
                toggleMenuRadial();
            }
        });
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
        defaultPane.setPrefHeight(700.0);
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
            // Al arrastrar, si no estamos en modo carrusel, lo activamos
            if (!isCarouselMode) {
                activarModoCarrusel();
                crearZonaAnclaje(); // Crear la zona de anclaje al activar el modo carrusel
            }
            
            // Actualizar posición del botón
            boton.setLayoutX(event.getSceneX() - xOffset);
            boton.setLayoutY(event.getSceneY() - yOffset);
            
            // Despliega el menú radial si no está visible
            if (!menuVisible) {
                toggleMenuRadial();
            }
            
            // Verificar si el botón está sobre la zona de anclaje (100px desde el borde izquierdo)
            if (event.getSceneX() < 100) {
                // Iluminar la zona de anclaje
                zonaAnclaje.setStyle("-fx-background-color: rgba(0, 120, 215, 0.3);");
            } else {
                // Ocultar el resaltado si está fuera de la zona
                zonaAnclaje.setStyle("-fx-background-color: rgba(0, 0, 255, 0.0);");
            }
        });
        
        boton.setOnMouseReleased(event -> {
            // Si el botón se suelta en la zona de anclaje, volver al modo barra lateral
            if (isCarouselMode && event.getSceneX() < 100) {
                salirModoCarrusel();
            }
            
            // Ocultar la iluminación de la zona de anclaje
            if (zonaAnclaje != null) {
                zonaAnclaje.setStyle("-fx-background-color: rgba(0, 0, 255, 0.0);");
            }
        });
    }

    private void setButtonIcon(JFXButton button, String iconPath, double width, double height) {
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
        icon.setFitWidth(width);
        icon.setFitHeight(height);
        icon.setPreserveRatio(true);
        button.setGraphic(icon);
    }

    private void toggleSidebar() {
        if (isCarouselMode) {
            return; // No permitir toggle del sidebar en modo carrusel
        }

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
            } else {
                btnMenuPrincipal.setText("");
                btnAnimales.setText("");
                btnFichaje.setText("");
                btnSalir.setText("");
                but_clientes.setText("");
                if (btnEmpresa != null) {
                    btnEmpresa.setText("");
                }
            }
            isCollapsed = !isCollapsed;
        });
    }

    private void toggleMenuRadial() {
        JFXButton[] botones = {btnMenuPrincipalCarousel, btnAnimalesCarousel, but_clientesCarousel, btnFichajeCarousel, btnSalirCarousel};
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

    private void mostrarSidebar() {
        isCarouselMode = false;
        menuVisible = false;
        sidebar.setVisible(true);
        sidebarContainer.setVisible(true);
        btnChicha.setVisible(false);
        
        // Ocultar botones del carrusel
        JFXButton[] botonesCarousel = {btnMenuPrincipalCarousel, btnAnimalesCarousel, but_clientesCarousel, btnFichajeCarousel, btnSalirCarousel};
        for (JFXButton boton : botonesCarousel) {
            boton.setVisible(false);
        }
    }

    private void mostrarCarousel() {
        isCarouselMode = true;
        if (sidebar != null) {
            sidebar.setVisible(false);
        }
        if (sidebarContainer != null) {
            sidebarContainer.setVisible(false);
        }
        if (carouselContainer != null) {
            carouselContainer.getChildren().clear();
            carouselContainer.getChildren().add(btnChicha);
        }
        if (btnChicha != null) {
            btnChicha.setVisible(true);
        }
    }

    private void activarModoCarrusel() {
        isCarouselMode = true;
        sidebar.setVisible(false);
        sidebarContainer.setVisible(false);
        btnChicha.setVisible(true);
        menuVisible = false;
        
        // Asegúrate de que los botones del carrusel están inicialmente ocultos
        JFXButton[] botonesCarousel = {btnMenuPrincipalCarousel, btnAnimalesCarousel, but_clientesCarousel, btnFichajeCarousel, btnSalirCarousel};
        for (JFXButton boton : botonesCarousel) {
            boton.setVisible(false);
        }
    }

    // Método placeholder para Fichaje
    private void abrirModuloFichaje() {
        // Aquí puedes implementar la lógica para abrir el módulo de fichaje
        System.out.println("Abrir módulo de fichaje (implementa la lógica aquí)");
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

    private void crearZonaAnclaje() {
        if (zonaAnclajeCreada) return;
        
        // Crear la zona de anclaje (rectángulo semitransparente en el lado izquierdo)
        zonaAnclaje = new Pane();
        zonaAnclaje.setPrefWidth(100);
        zonaAnclaje.setPrefHeight(root.getHeight());
        zonaAnclaje.setStyle("-fx-background-color: rgba(0, 0, 255, 0.0);"); // Inicialmente invisible
        zonaAnclaje.setLayoutX(0);
        zonaAnclaje.setLayoutY(0);
        
        // Añadir al centro del BorderPane (el contenedor principal)
        BorderPane centerPane = (BorderPane) root.getCenter();
        Pane mainPane = (Pane) centerPane.getCenter();
        mainPane.getChildren().add(zonaAnclaje);
        
        zonaAnclajeCreada = true;
    }

    private void salirModoCarrusel() {
        isCarouselMode = false;
        menuVisible = false;
        sidebar.setVisible(true);
        sidebarContainer.setVisible(true);
        btnChicha.setVisible(false);
        
        // Ocultar los botones del carrusel
        JFXButton[] botones = {btnMenuPrincipalCarousel, btnAnimalesCarousel, but_clientesCarousel, btnFichajeCarousel, btnSalirCarousel};
        for (JFXButton boton : botones) {
            boton.setVisible(false);
        }
    }
}