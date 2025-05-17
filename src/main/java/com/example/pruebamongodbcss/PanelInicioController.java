package com.example.pruebamongodbcss;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.Data.ServicioUsuarios;
import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.calendar.CalendarScreen;
import com.example.pruebamongodbcss.theme.ThemeManager;
import com.example.pruebamongodbcss.theme.ThemeToggleSwitch;
import com.example.pruebamongodbcss.theme.ThemeUtil;
import com.jfoenix.controls.JFXButton;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
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
    private JFXButton btnMenuPrincipal, btnAnimales, btnFichaje, btnSalir, btnToggleSidebar, but_clientes, btnEmpresa, btnChicha, btnGoogleCalendar;

    @FXML
    private JFXButton btnMenuPrincipalCarousel, btnAnimalesCarousel, btnFichajeCarousel, btnSalirCarousel, but_clientesCarousel, btnEmpresaCarousel, btnGoogleCalendarCarousel;

    @FXML
    private Label lblClinica;

    @FXML
    private Separator separator;
    @FXML
    private ThemeToggleSwitch themeToggle;

    private boolean isCollapsed = false;
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private BorderPane sidebarContainer;
    
    // Usuario actual de la sesión
    private Usuario usuarioActual;
    private ServicioUsuarios servicioUsuarios;

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
        
        // Configurar iconos personalizados para los ThemeToggleSwitch
        configurarThemeToggles();
        
        // Inicializar el gestor de temas y registrar la escena actual
        // Se hace de forma postergada ya que la escena aún no está disponible
        javafx.application.Platform.runLater(() -> {
            if (root.getScene() != null) {
                // Registrar la escena en el ThemeManager para aplicar el tema
                ThemeManager.getInstance().registerScene(root.getScene());
                
                // Aplicar el tema a todas las ventanas abiertas
                ThemeUtil.applyThemeToAllOpenWindows();
                
                // Cargar automáticamente la vista home
                restaurarVistaPrincipal();
            }
        });
        
        // Agregar un listener al toggle de tema para mantener actualizadas todas las ventanas
        if (themeToggle != null) {
            themeToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
                javafx.application.Platform.runLater(() -> {
                    ThemeUtil.applyThemeToAllOpenWindows();
                });
            });
        }
        
        // Configurar iconos y tooltips para el menú lateral
        setButtonIcon(btnMenuPrincipal, "/Iconos/iconInicio4.png", 32, 32);
        setButtonIcon(btnAnimales, "/Iconos/iconPet2.png", 32, 32);
        setButtonIcon(but_clientes, "/Iconos/IconPruebaClientes.png", 32, 32);
        setButtonIcon(btnFichaje, "/Iconos/iconClock2.png", 32, 32);
        setButtonIcon(btnEmpresa, "/Iconos/iconAdministrador2.png", 35, 38);
        setButtonIcon(btnSalir, "/Iconos/iconSalir.png", 32, 32);
        setButtonIcon(btnGoogleCalendar, "/Iconos/iconClock2.png", 32, 32);

        // Configurar iconos y tooltips para los botones del carrusel
        setButtonIcon(btnMenuPrincipalCarousel, "/Iconos/iconInicio4.png", 32, 32);
        setButtonIcon(btnAnimalesCarousel, "/Iconos/iconPet2.png", 32, 32);
        setButtonIcon(but_clientesCarousel, "/Iconos/IconPruebaClientes.png", 32, 32);
        setButtonIcon(btnFichajeCarousel, "/Iconos/iconClock2.png", 32, 32);
        setButtonIcon(btnEmpresaCarousel, "/Iconos/iconAdministrador2.png", 35, 38);
        setButtonIcon(btnSalirCarousel, "/Iconos/iconSalir.png", 32, 32);
        setButtonIcon(btnGoogleCalendarCarousel, "/Iconos/iconClock2.png", 32, 32);

        // Tooltips para los botones del carrusel
        btnMenuPrincipalCarousel.setTooltip(new Tooltip("Menú Principal"));
        btnAnimalesCarousel.setTooltip(new Tooltip("Animales"));
        but_clientesCarousel.setTooltip(new Tooltip("Clientes"));
        btnFichajeCarousel.setTooltip(new Tooltip("Fichaje"));
        btnSalirCarousel.setTooltip(new Tooltip("Cerrar sesión"));
        btnGoogleCalendarCarousel.setTooltip(new Tooltip("Calendario de Citas"));

        // Configurar eventos del carrusel
        btnChicha.setOnAction(e -> toggleMenuRadial());
        btnMenuPrincipalCarousel.setOnAction(event -> {
            restaurarVistaPrincipal();
            mantenerCarruselVisible(); // Mantener el carrusel visible después de la acción
        });
        btnAnimalesCarousel.setOnAction(event -> {
            abrirModuloClinica();
            mantenerCarruselVisible(); // Mantener el carrusel visible después de la acción
        });
        but_clientesCarousel.setOnAction(event -> {
            abrirModuloClinicaConCitas();
            mantenerCarruselVisible(); // Mantener el carrusel visible después de la acción
        });
        btnFichajeCarousel.setOnAction(event -> {
            abrirModuloFichaje();
            mantenerCarruselVisible(); // Mantener el carrusel visible después de la acción
        });
        btnSalirCarousel.setOnAction(event -> cerrarSesion());
        btnGoogleCalendarCarousel.setOnAction(event -> {
            abrirModuloGoogleCalendar();
            mantenerCarruselVisible(); // Mantener el carrusel visible después de la acción
        });

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
        if (btnGoogleCalendar != null) {
            btnGoogleCalendar.setOnAction(event -> abrirModuloGoogleCalendar());
        }

        // Inicializar servicio
        servicioUsuarios = new ServicioUsuarios();
        
        // Configurar arrastre del botón
        configurarArrastreBoton(btnChicha);
        
        // Configurar z-order y estilo circular para los botones del carrusel
        JFXButton[] botonesCarousel = {btnMenuPrincipalCarousel, btnAnimalesCarousel, but_clientesCarousel, btnFichajeCarousel, btnSalirCarousel, btnGoogleCalendarCarousel};
        for (JFXButton boton : botonesCarousel) {
            boton.getStyleClass().removeAll("itemMenu");
            if (!boton.getStyleClass().contains("circleMenuButton")) {
                boton.getStyleClass().add("circleMenuButton");
            }
            boton.setViewOrder(-1.0); // Valor negativo para estar más adelante en el orden de visualización
        }

        // Mostrar sidebar por defecto
        mostrarSidebar();

        // Asegurarse de que el botón del carrusel siempre esté en primer plano
        btnChicha.setViewOrder(-1.0); // Valor negativo para estar más adelante en el orden de visualización
        
        // Marcar visualmente el botón de Menú Principal como seleccionado
        if (btnMenuPrincipal != null) {
            btnMenuPrincipal.getStyleClass().add("menu-button-selected");
        }
    }
    
    /**
     * Establece el usuario actual de la sesión y configura la interfaz según sus permisos
     */
    public void setUsuarioActual(Usuario usuario) {
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
                    getClass().getResourceAsStream("/Iconos/iconAdministrador2.png")));
                empresaIcon.setFitHeight(38.0);
                empresaIcon.setFitWidth(35.0);
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
            // Desmarcar otros botones y marcar el seleccionado
            desmarcaTodosLosBotones();
            btnAnimales.getStyleClass().add("menu-button-selected");
            
            // Cargar la vista de citas (nueva ubicación)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/clinica-view.fxml"));
            Parent contenido = ThemeUtil.loadWithTheme(loader);
            
            // Agregar opciones de JVM necesarias en tiempo de ejecución
            System.setProperty("javafx.controls.behaviour", "com.sun.javafx.scene.control.behavior");
            
            // Obtener el BorderPane central y reemplazar su contenido
            BorderPane centerPane = (BorderPane) root.getCenter();
            centerPane.setCenter(contenido);
            
            // Asegurarse de que todas las ventanas tengan el tema aplicado
            javafx.application.Platform.runLater(ThemeUtil::applyThemeToAllOpenWindows);
            
            // Actualizar el título (opcional)
            lblClinica.setText("Gestión de Citas");
            
            // Mantener visible el carrusel
            mantenerCarruselVisible();
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar el módulo de clínica: " + e.getMessage());
        }
    }
    
    /**
     * Abre el módulo de clínica veterinaria con la sección de citas
     */
    private void abrirModuloClinicaConCitas() {
        try {
            // Desmarcar otros botones y marcar el seleccionado
            desmarcaTodosLosBotones();
            but_clientes.getStyleClass().add("menu-button-selected");
            
            // Cargar la vista de citas (nueva ubicación)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/citas-standalone.fxml"));
            Parent contenido = ThemeUtil.loadWithTheme(loader);
            
            // Obtener el BorderPane central y reemplazar su contenido
            BorderPane centerPane = (BorderPane) root.getCenter();
            centerPane.setCenter(contenido);
            
            // Actualizar el título
            lblClinica.setText("Gestión de Citas");
            
            // Mantener visible el carrusel
            mantenerCarruselVisible();
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar el módulo de clínica con citas: " + e.getMessage());
        }
    }
    
    /**
     * Abre el módulo de gestión de empresa
     */
    private void abrirModuloEmpresa() {
        try {
            // Desmarcar otros botones y marcar el seleccionado
            desmarcaTodosLosBotones();
            if (btnEmpresa != null) {
                btnEmpresa.getStyleClass().add("menu-button-selected");
            }
            
            // Verificar que el usuario tenga permisos de administrador
            if (usuarioActual == null || !usuarioActual.esAdmin()) {
                mostrarError("Acceso denegado", "Solo los administradores pueden acceder al módulo de Empresa.");
                return;
            }
            
            // Establecer el usuario actual en el servicio
            servicioUsuarios.setUsuarioActual(usuarioActual);
            
            // Inicializar el módulo de Empresa usando la nueva clase simplificada
            com.example.pruebamongodbcss.Modulos.Empresa.EmpresaMain empresaMain = 
                new com.example.pruebamongodbcss.Modulos.Empresa.EmpresaMain(servicioUsuarios);
            
            // Obtener el BorderPane central para integrar el módulo
            BorderPane centerPane = (BorderPane) root.getCenter();
            
            // Iniciar el módulo integrado en el panel central
            empresaMain.iniciarIntegrado(centerPane);
            
            // Actualizar el título
            lblClinica.setText("Gestión de Empresa");
            
            // Mantener visible el carrusel si está activo
            mantenerCarruselVisible();
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error", "Error al iniciar el módulo de Empresa: " + e.getMessage());
        }
    }

    /**
     * Cierra la sesión actual y vuelve a la pantalla de login
     */
    @FXML
    public void cerrarSesion(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/InicioSesion/PruebaDoblePanel.fxml"));
            Parent root = loader.load();
            Scene scene = ThemeUtil.createScene(root, 900, 450);
            Stage stage = (Stage) btnSalir.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cerrar sesión: " + e.getMessage());
        }
    }

    /**
     * Restaura la vista principal por defecto
     */
    private void restaurarVistaPrincipal() {
        try {
            // Desmarcar otros botones y marcar el botón de menú principal
            desmarcaTodosLosBotones();
            btnMenuPrincipal.getStyleClass().add("menu-button-selected");
            
            // Cargar nueva vista home
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/home-view.fxml"));
            Parent homeView = ThemeUtil.loadWithTheme(loader);
            
            // Obtener el BorderPane central y reemplazar su contenido
            BorderPane centerPane = (BorderPane) root.getCenter();
            centerPane.setCenter(homeView);
            
            // Actualizar el título
            lblClinica.setText("Menú Principal");
            
            // Mantener visible el carrusel si está activo
            mantenerCarruselVisible();
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista principal: " + e.getMessage());
        }
    }

    /**
     * Desmarca todos los botones del menú lateral
     */
    private void desmarcaTodosLosBotones() {
        JFXButton[] botones = {btnMenuPrincipal, btnAnimales, but_clientes, btnFichaje, btnEmpresa, btnSalir, btnGoogleCalendar};
        for (JFXButton boton : botones) {
            if (boton != null) {
                boton.getStyleClass().remove("menu-button-selected");
            }
        }
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
            }
            
            // Actualizar posición del botón
            boton.setLayoutX(event.getSceneX() - xOffset);
            boton.setLayoutY(event.getSceneY() - yOffset);
            
            // Despliega el menú radial si no está visible
            if (!menuVisible) {
                toggleMenuRadial();
            }
            
            // Crear o actualizar la zona de anclaje (siempre al arrastrar)
            crearZonaAnclaje();
            
            // Verificar si el botón está sobre la zona de anclaje (100px desde el borde izquierdo)
            if (zonaAnclaje != null && event.getSceneX() < 100) {
                // Iluminar la zona de anclaje con azul intenso
                zonaAnclaje.setStyle("-fx-background-color: rgba(0, 191, 255, 0.6); -fx-border-width: 0 4px 0 0; -fx-border-color: rgba(30, 144, 255, 1.0); -fx-effect: dropshadow(gaussian, rgba(30, 144, 255, 0.8), 15, 0, 5, 0);");
                zonaAnclaje.toFront();
                btnChicha.toFront();  // Mantener el botón por encima
                
                // Traer botones al frente si están visibles
                if (menuVisible) {
                    JFXButton[] botones = {btnMenuPrincipalCarousel, btnAnimalesCarousel, but_clientesCarousel, btnFichajeCarousel, btnSalirCarousel, btnGoogleCalendarCarousel};
                    for (JFXButton btn : botones) {
                        btn.toFront();
                    }
                }
            } else if (zonaAnclaje != null) {
                // Volver al estilo más sutil cuando está fuera de la zona
                zonaAnclaje.setStyle("-fx-background-color: rgba(0, 191, 255, 0.2); -fx-border-width: 0 2px 0 0; -fx-border-color: rgba(30, 144, 255, 0.5);");
            }
        });
        
        boton.setOnMouseReleased(event -> {
            // Si el botón se suelta en la zona de anclaje, volver al modo barra lateral
            if (isCarouselMode && event.getSceneX() < 100) {
                salirModoCarrusel();
            }
            
            // Ocultar la zona de anclaje al soltar
            if (zonaAnclaje != null) {
                zonaAnclaje.setVisible(false);
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

    //Metodo que contrae expande el sidebar
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
        
        // Animar el separador
        if (separator != null) {
            FadeTransition fadeSeparator = new FadeTransition(Duration.seconds(0.3), separator);
            fadeSeparator.setToValue(isCollapsed ? 1 : 0);
            fadeSeparator.play();
        }

        // Animar el botón de toggle
        TranslateTransition slideBtnToggle = new TranslateTransition(Duration.seconds(0.3), btnToggleSidebar);
        slideBtnToggle.setToX(isCollapsed ? 0 : -1);
        slideBtnToggle.play();

        // Animar el ThemeToggleSwitch
        if (themeToggle != null) {
            TranslateTransition slideThemeToggle = new TranslateTransition(Duration.seconds(0.3), themeToggle);
            slideThemeToggle.setToX(isCollapsed ? 0 : -20);
            slideThemeToggle.play();
        }

        timeline.setOnFinished(event -> {
            if (isCollapsed) {
                btnMenuPrincipal.setText("Menú Principal");
                btnAnimales.setText("Animales");
                btnFichaje.setText("Fichaje");
                btnSalir.setText("SALIR");
                but_clientes.setText("Clientes");
                if (separator != null) {
                    separator.setVisible(true);
                }
                if (btnEmpresa != null) {
                    btnEmpresa.setText("Empresa");
                }
            } else {
                btnMenuPrincipal.setText("");
                btnAnimales.setText("");
                btnFichaje.setText("");
                btnSalir.setText("");
                but_clientes.setText("");
                if (separator != null) {
                    separator.setVisible(false);
                }
                if (btnEmpresa != null) {
                    btnEmpresa.setText("");
                }
            }
            isCollapsed = !isCollapsed;
        });
    }

    private void toggleMenuRadial() {
        JFXButton[] botones = {btnMenuPrincipalCarousel, btnAnimalesCarousel, but_clientesCarousel, btnFichajeCarousel,btnEmpresaCarousel ,btnSalirCarousel, btnGoogleCalendarCarousel};
        double centerX = btnChicha.getLayoutX() + btnChicha.getWidth() / 2;
        double centerY = btnChicha.getLayoutY() + btnChicha.getHeight() / 2;
        double radio = 120; // Distancia desde el centro
        int n = botones.length;
        
        // Asegurarnos de que todos los botones estén en la escena
        BorderPane centerPane = (BorderPane) root.getCenter();
        Pane mainPane = (Pane) centerPane.getCenter();
        
        if (!menuVisible) {
            // Asegurar que todos los botones estén en la escena antes de animarlos
            for (JFXButton boton : botones) {
                if (!mainPane.getChildren().contains(boton)) {
                    mainPane.getChildren().add(boton);
                }
                boton.setViewOrder(-1.0); // Mismo z-order que btnChicha
            }
            
            // Animar los botones desde el centro hacia afuera
            for (int i = 0; i < n; i++) {
                double angle = 2 * Math.PI * i / n;
                double x = centerX + radio * Math.cos(angle) - botones[i].getWidth() / 2;
                double y = centerY + radio * Math.sin(angle) - botones[i].getHeight() / 2;
                
                // Posicionar inicialmente en el centro
                botones[i].setLayoutX(centerX - botones[i].getWidth() / 2);
                botones[i].setLayoutY(centerY - botones[i].getHeight() / 2);
                botones[i].setVisible(true);
                
                // Animar hacia la posición final
                animateButtonTo(botones[i], x, y, true);
                botones[i].toFront();
            }
        } else {
            // Animar los botones hacia el centro para ocultarlos
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
        JFXButton[] botonesCarousel = {btnMenuPrincipalCarousel, btnAnimalesCarousel, but_clientesCarousel, btnFichajeCarousel, btnSalirCarousel, btnGoogleCalendarCarousel};
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
        menuVisible = false;
        
        // Remover el panel lateral para que el contenido central ocupe toda la pantalla
        if (root.getLeft() != null) {
            root.setLeft(null);
        }
        
        // Obtener el panel principal donde se mostrarán los elementos
        BorderPane centerPane = (BorderPane) root.getCenter();
        Pane mainPane = (Pane) centerPane.getCenter();
        
        // Asegurar que btnChicha está en la escena
        if (!mainPane.getChildren().contains(btnChicha)) {
            mainPane.getChildren().add(btnChicha);
        }
        btnChicha.setVisible(true);
        btnChicha.toFront();
        
        // Asegurar que los botones del carrusel existen pero están inicialmente ocultos
        JFXButton[] botonesCarousel = {btnMenuPrincipalCarousel, btnAnimalesCarousel, but_clientesCarousel, btnFichajeCarousel,btnEmpresaCarousel , btnSalirCarousel, btnGoogleCalendarCarousel};
        for (JFXButton boton : botonesCarousel) {
            if (!mainPane.getChildren().contains(boton)) {
                mainPane.getChildren().add(boton);
            }
            boton.setVisible(false);  // Inicialmente ocultos
            boton.setViewOrder(-1.0); // Z-order alto
        }
        
        // Crear zona de anclaje
        crearZonaAnclaje();
    }

    // Método placeholder para Fichaje
    private void abrirModuloFichaje() {
        // Desmarcar otros botones y marcar el seleccionado
        desmarcaTodosLosBotones();
        btnFichaje.getStyleClass().add("menu-button-selected");
        
        System.out.println("Abrir módulo de fichaje (implementa la lógica aquí)");
        
        // Mantener visible el carrusel
        mantenerCarruselVisible();
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
        BorderPane centerPane = (BorderPane) root.getCenter();
        Pane mainPane = (Pane) centerPane.getCenter();
        
        // Si ya existe, solo asegurar que esté visible
        if (zonaAnclaje != null) {
            if (!mainPane.getChildren().contains(zonaAnclaje)) {
                mainPane.getChildren().add(zonaAnclaje);
            }
            zonaAnclaje.setPrefHeight(mainPane.getHeight());
            zonaAnclaje.setVisible(true);
            zonaAnclaje.toFront();
            return;
        }
        
        // Crear la zona de anclaje (rectángulo semitransparente en el lado izquierdo)
        zonaAnclaje = new Pane();
        zonaAnclaje.setPrefWidth(100);
        zonaAnclaje.setPrefHeight(mainPane.getHeight());
        // Color intenso para la zona de anclaje
        zonaAnclaje.setStyle("-fx-background-color: rgba(0, 191, 255, 0.2); -fx-border-width: 0 2px 0 0; -fx-border-color: rgba(30, 144, 255, 0.5);");
        zonaAnclaje.setLayoutX(0);
        zonaAnclaje.setLayoutY(0);
        zonaAnclaje.setViewOrder(0.0); // Asegurar que esté en un plano visible
        
        mainPane.getChildren().add(zonaAnclaje);
        zonaAnclajeCreada = true;
        
        // Asegurar que está visible
        zonaAnclaje.toFront();
    }

    private void salirModoCarrusel() {
        isCarouselMode = false;
        menuVisible = false;
        
        // Restaurar la barra lateral
        if (root.getLeft() == null) {
            root.setLeft(sidebarContainer);
        }
        sidebar.setVisible(true);
        sidebarContainer.setVisible(true);
        
        // Ocultar todos los elementos del carrusel
        BorderPane centerPane = (BorderPane) root.getCenter();
        Pane mainPane = (Pane) centerPane.getCenter();
        
        // Ocultar el botón principal del carrusel
        btnChicha.setVisible(false);
        
        // Ocultar los botones del carrusel
        JFXButton[] botones = {btnMenuPrincipalCarousel, btnAnimalesCarousel, but_clientesCarousel, btnFichajeCarousel, btnSalirCarousel, btnGoogleCalendarCarousel};
        for (JFXButton boton : botones) {
            boton.setVisible(false);
        }
        
        // Mantener la zona de anclaje visible y resaltada en azul
        if (zonaAnclaje != null) {
            // Asegurar que sigue visible
            zonaAnclaje.setVisible(true);
            // Establecer estilo destacado para la zona de anclaje
            zonaAnclaje.setStyle("-fx-background-color: rgba(0, 191, 255, 0.6); -fx-border-width: 0 4px 0 0; -fx-border-color: rgba(30, 144, 255, 1.0); -fx-effect: dropshadow(gaussian, rgba(30, 144, 255, 0.8), 15, 0, 5, 0);");
            zonaAnclaje.toFront();
            
            // Configurar un temporizador para ocultar la zona de anclaje después de un breve periodo
            PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
            delay.setOnFinished(event -> {
                // Desvanecer gradualmente la zona de anclaje
                FadeTransition fade = new FadeTransition(Duration.seconds(0.5), zonaAnclaje);
                fade.setFromValue(1.0);
                fade.setToValue(0.0);
                fade.setOnFinished(e -> zonaAnclaje.setVisible(false));
                fade.play();
            });
            delay.play();
        }
    }

    // Método centralizado para mantener el carrusel visible
    private void mantenerCarruselVisible() {
        if (isCarouselMode) {
            // Poner el botón en primer plano
            BorderPane centerPane = (BorderPane) root.getCenter();
            Pane mainPane = (Pane) centerPane.getCenter();
            
            // Asegurarse de que el carrusel esté visible
            if (!mainPane.getChildren().contains(btnChicha)) {
                mainPane.getChildren().add(btnChicha);
            }
            btnChicha.setVisible(true);
            btnChicha.toFront();
            
            // Si el menú está visible, asegurarse de que los botones del menú también estén visibles
            if (menuVisible) {
                JFXButton[] botones = {btnMenuPrincipalCarousel, btnAnimalesCarousel, but_clientesCarousel, btnFichajeCarousel,btnEmpresaCarousel , btnSalirCarousel, btnGoogleCalendarCarousel};
                for (JFXButton boton : botones) {
                    if (!mainPane.getChildren().contains(boton)) {
                        mainPane.getChildren().add(boton);
                    }
                    boton.setVisible(true);
                    boton.toFront();
                    boton.setViewOrder(-1.0); // Mismo z-order que btnChicha
                }
            }
            
            // Ocultar el panel lateral
            sidebar.setVisible(false);
            sidebarContainer.setVisible(false);
            
            // Asegurar que el contenido ocupe toda la pantalla
            if (root.getLeft() != null) {
                root.setLeft(null);
            }
            
            // Recrear la zona de anclaje si es necesario
            crearZonaAnclaje();
            zonaAnclaje.toFront(); // Asegurar que esté en un plano visible
            
            // Traer todos los elementos del carrusel al frente
            btnChicha.toFront();   // La uva debe estar por encima
            
            // Si el menú está desplegado, traer también los botones al frente
            if (menuVisible) {
                JFXButton[] botones = {btnMenuPrincipalCarousel, btnAnimalesCarousel, but_clientesCarousel, btnFichajeCarousel,btnEmpresaCarousel , btnSalirCarousel, btnGoogleCalendarCarousel};
                for (JFXButton boton : botones) {
                    boton.toFront();
                }
            }
        }
    }

    /**
     * Configura los interruptores de tema con iconos personalizados
     */
    private void configurarThemeToggles() {
        // Cargar los iconos para los modos claro y oscuro
        String iconoClaro = "/Iconos/iconClaro.png";
        String iconoOscuro = "/Iconos/iconDark.png";
        
        // Configurar el toggle de tema en la interfaz
        if (themeToggle != null) {
            themeToggle.setLightModeIcon(iconoClaro);
            themeToggle.setDarkModeIcon(iconoOscuro);
        }
    }

    /**
     * Abre el módulo de calendario de citas
     */
    private void abrirModuloGoogleCalendar() {
        try {
            desmarcaTodosLosBotones();
            btnGoogleCalendar.getStyleClass().add("menu-button-selected");
            
            // Verificar que el usuario esté logueado
            if (usuarioActual == null) {
                mostrarError("Error", "No hay usuario logueado. Por favor, inicie sesión nuevamente.");
                return;
            }
            
            // Crear una instancia del componente de calendario personalizado CON EL USUARIO ACTUAL
            CalendarScreen calendarScreen = new CalendarScreen(usuarioActual);
            
            // Asegurar que el tema se aplique correctamente
            if (ThemeManager.getInstance().isDarkTheme()) {
                calendarScreen.getStyleClass().add("dark-theme");
            }
            
            // Reemplazar el contenido central con el componente de calendario
            BorderPane centerPane = (BorderPane) root.getCenter();
            centerPane.setCenter(calendarScreen);
            
            // Actualizar el título
            lblClinica.setText("Calendario de Citas: " + usuarioActual.getUsuario());
            
            // Mantener visible el carrusel si está activo
            mantenerCarruselVisible();
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo cargar el módulo de calendario: " + e.getMessage());
        }
    }
}