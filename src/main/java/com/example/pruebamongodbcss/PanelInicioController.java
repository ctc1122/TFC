package com.example.pruebamongodbcss;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.Data.ServicioUsuarios;
import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.Modulos.AppChat.ServidorAppChat;
import com.example.pruebamongodbcss.Modulos.AppChat.VentanaChat;
import com.example.pruebamongodbcss.Protocolo.Protocolo;
import com.example.pruebamongodbcss.calendar.CalendarScreen;
import com.example.pruebamongodbcss.calendar.CalendarService;
import com.example.pruebamongodbcss.theme.ThemeManager;
import com.example.pruebamongodbcss.theme.ThemeToggleSwitch;
import com.example.pruebamongodbcss.theme.ThemeUtil;
import com.jfoenix.controls.JFXButton;

import Utilidades1.GestorSocket;
import Utilidades1.ScreensaverManager;
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
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class PanelInicioController implements Initializable {

    @FXML
    private BorderPane root; // o el layout raíz que contiene todo

    @FXML
    private VBox sidebar;

    @FXML
    private JFXButton btnMenuPrincipal, btnAnimales, btnFichaje, btnSalir, btnToggleSidebar, but_clientes, btnEmpresa, btnChicha, btnEventCounter, btnChat, btnFacturacion, btnInformes;

    @FXML
    private JFXButton btnMenuPrincipalCarousel, btnAnimalesCarousel, btnFichajeCarousel, btnSalirCarousel, but_clientesCarousel, btnEmpresaCarousel, btnChatCarousel, btnFacturacionCarousel, btnInformesCarousel;

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
    private static Usuario usuarioActual;
    private ServicioUsuarios servicioUsuarios;
    private CalendarService calendarService;

    private boolean menuVisible = false;
    private boolean isCarouselMode = false;
    private Pane carouselContainer;

    private javafx.animation.PauseTransition holdTimer;
    private boolean isHoldingToggle = false;

    private Pane zonaAnclaje;
    private boolean zonaAnclajeCreada = false;
    
    // Variables para controlar arrastre vs doble clic
    private boolean isDragging = false;
    private long lastPressTime = 0;

    private GestorSocket gestorSocket;
    private ScreensaverManager screensaverManager;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Inicializar el gestor de socket
            gestorSocket = GestorSocket.getInstance();
            
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
                    
                    // Establecer el icono de la ventana principal
                    Stage currentStage = (Stage) root.getScene().getWindow();
                    try {
                        Image icon = new Image(getClass().getResourceAsStream("/logo.png"));
                        currentStage.getIcons().clear(); // Limpiar iconos existentes
                        currentStage.getIcons().add(icon);
                    } catch (Exception e) {
                        System.err.println("No se pudo cargar el icono de la ventana: " + e.getMessage());
                    }
                    
                    // Crear e inicializar el salvapantallas para esta ventana
                    screensaverManager = new ScreensaverManager(currentStage);
                    screensaverManager.startInactivityMonitoring();
                    System.out.println("ScreensaverManager creado e iniciado en PanelInicioController");
                    
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
            setButtonIcon(btnChat, "/Iconos/iconChat.png", 32, 32);
            setButtonIcon(btnFacturacion, "/Iconos/iconInvoice1.png", 32, 32);
            setButtonIcon(btnInformes, "/Iconos/iconChart.png", 32, 32);

            // Configurar iconos y tooltips para los botones del carrusel
            setButtonIcon(btnMenuPrincipalCarousel, "/Iconos/iconInicio4.png", 32, 32);
            setButtonIcon(btnAnimalesCarousel, "/Iconos/iconPet2.png", 32, 32);
            setButtonIcon(but_clientesCarousel, "/Iconos/IconPruebaClientes.png", 32, 32);
            setButtonIcon(btnFichajeCarousel, "/Iconos/iconClock2.png", 32, 32);
            setButtonIcon(btnEmpresaCarousel, "/Iconos/iconAdministrador2.png", 35, 38);
            setButtonIcon(btnSalirCarousel, "/Iconos/iconSalir.png", 32, 32);
            setButtonIcon(btnChatCarousel, "/Iconos/iconChat.png", 32, 32);
            setButtonIcon(btnFacturacionCarousel, "/Iconos/iconInvoice1.png", 32, 32);
            setButtonIcon(btnInformesCarousel, "/Iconos/iconChart.png", 32, 32);

            // Tooltips para los botones del carrusel
            btnMenuPrincipalCarousel.setTooltip(new Tooltip("Menú Principal"));
            btnAnimalesCarousel.setTooltip(new Tooltip("Animales"));
            but_clientesCarousel.setTooltip(new Tooltip("Clientes"));
            btnFichajeCarousel.setTooltip(new Tooltip("Fichaje"));
            btnSalirCarousel.setTooltip(new Tooltip("Cerrar sesión"));
            btnFacturacionCarousel.setTooltip(new Tooltip("Facturación"));
            btnInformesCarousel.setTooltip(new Tooltip("Informes"));

            // Configurar eventos del carrusel
            // Doble clic para desplegar menú (solo si no se está arrastrando)
            btnChicha.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !isDragging) {
                    toggleMenuRadial();
                }
            });
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
            btnFacturacionCarousel.setOnAction(event -> {
                abrirModuloFacturacion();
                mantenerCarruselVisible(); // Mantener el carrusel visible después de la acción
            });
            btnInformesCarousel.setOnAction(event -> {
                abrirModuloInformes();
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
            btnChat.setOnAction(event -> abrirChat());
            btnFacturacion.setOnAction(event -> abrirModuloFacturacion());
            btnInformes.setOnAction(event -> abrirModuloInformes());
            btnSalir.setOnAction(event -> cerrarSesion());
            if (btnEmpresa != null) {
                btnEmpresa.setOnAction(event -> abrirModuloEmpresa());
            }


            // Inicializar servicio
            servicioUsuarios = new ServicioUsuarios();
            calendarService = new CalendarService();
            
            // Configurar arrastre del botón
            configurarArrastreBoton(btnChicha);
            
            // Configurar z-order y estilo circular para los botones del carrusel
            JFXButton[] botonesCarousel = {btnMenuPrincipalCarousel, btnAnimalesCarousel, but_clientesCarousel, btnFichajeCarousel, btnSalirCarousel, btnChatCarousel, btnFacturacionCarousel, btnInformesCarousel};
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

            // Configurar el contador de eventos
            if (btnEventCounter != null) {
                btnEventCounter.setTooltip(new Tooltip("Cargando eventos..."));
                btnEventCounter.setOnAction(event -> abrirModuloGoogleCalendar());
                
                // Estilizar el botón como un badge circular
                btnEventCounter.getStyleClass().add("event-counter-badge");
                
                // Configurar actualización periódica del contador (cada 5 minutos)
                configurarActualizacionPeriodicaEventos();
            }
            
            // Configurar posicionamiento dinámico del carrusel para responsive design
            javafx.application.Platform.runLater(() -> {
                if (root.getScene() != null && btnChicha != null) {
                    // Obtener el panel central
                    BorderPane centerPane = (BorderPane) root.getCenter();
                    Pane mainPane = (Pane) centerPane.getCenter();
                    
                    // Configurar listeners para centrar el botón automáticamente
                    mainPane.widthProperty().addListener((obs, oldVal, newVal) -> {
                        centrarBotonCarrusel();
                    });
                    
                    mainPane.heightProperty().addListener((obs, oldVal, newVal) -> {
                        centrarBotonCarrusel();
                    });
                    
                    // Centrar inicialmente
                    centrarBotonCarrusel();
                }
            });
            
        } catch (Exception e) {
            System.err.println("Error al inicializar el controlador: " + e.getMessage());
            mostrarError("Error de Inicialización", "No se pudo inicializar correctamente la aplicación.");
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

        // Actualizar el contador de eventos si el usuario existe
        if (usuario != null) {
            actualizarContadorEventos();
        }
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }
    
    /**
     * Método estático para obtener el usuario actual de la sesión
     * @return El usuario actual
     */
    public static Usuario getUsuarioSesion() {
        return usuarioActual;
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
     * Abre el módulo de facturación
     */
    private void abrirModuloFacturacion() {
        try {
            // Desmarcar otros botones y marcar el seleccionado
            desmarcaTodosLosBotones();
            if (btnFacturacion != null) {
                btnFacturacion.getStyleClass().add("menu-button-selected");
            }
            
            // Cargar la vista de facturación
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Modulos/Facturacion/facturacion-view.fxml"));
            Parent contenido = ThemeUtil.loadWithTheme(loader);
            
            // Obtener el controlador y configurar el usuario actual
            com.example.pruebamongodbcss.Modulos.Facturacion.FacturacionController facturacionController = loader.getController();
            if (facturacionController != null && usuarioActual != null) {
                facturacionController.setUsuarioActual(usuarioActual);
            }
            
            // Obtener el BorderPane central y reemplazar su contenido
            BorderPane centerPane = (BorderPane) root.getCenter();
            centerPane.setCenter(contenido);
            
            // Asegurarse de que todas las ventanas tengan el tema aplicado
            javafx.application.Platform.runLater(ThemeUtil::applyThemeToAllOpenWindows);
            
            // Actualizar el título
            lblClinica.setText("Facturación");
            
            // Mantener visible el carrusel si está activo
            mantenerCarruselVisible();
            
        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error", "Error al cargar el módulo de facturación: " + e.getMessage());
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
            mostrarError("Error", "Error al iniciar el módulo de Empresa: " + e.getMessage());
        }
    }

    /**
     * Cierra la sesión actual y vuelve a la pantalla de login
     */
    @FXML
    public void cerrarSesion(){
        // Detener el salvapantallas si está activo
        if (screensaverManager != null) {
            screensaverManager.stop();
        }
        
        if (gestorSocket != null) {
            gestorSocket.cerrarConexion();
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/InicioSesion/PruebaDoblePanel.fxml"));
            Parent root = loader.load();
            Scene scene = ThemeUtil.createScene(root, 900, 450);
            Stage stage = (Stage) btnSalir.getScene().getWindow();
            
            // Establecer el icono de la ventana
            try {
                Image icon = new Image(getClass().getResourceAsStream("/logo.png"));
                stage.getIcons().clear(); // Limpiar iconos existentes
                stage.getIcons().add(icon);
            } catch (Exception e) {
                System.err.println("No se pudo cargar el icono de la ventana: " + e.getMessage());
            }
            
            stage.setScene(scene);
            stage.centerOnScreen();
            
        } catch (IOException e) {
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
            System.err.println("Error al cargar la vista principal: " + e.getMessage());
        }
    }

    /**
     * Desmarca todos los botones del menú lateral
     */
    private void desmarcaTodosLosBotones() {
        JFXButton[] botones = {btnMenuPrincipal, btnAnimales, but_clientes, btnFichaje, btnEmpresa, btnSalir, btnChat, btnFacturacion, btnInformes};
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
            isDragging = false;
            lastPressTime = System.currentTimeMillis();
            xOffset = event.getSceneX() - boton.getLayoutX();
            yOffset = event.getSceneY() - boton.getLayoutY();
        });

        boton.setOnMouseDragged(event -> {
            // Marcar que se está arrastrando
            isDragging = true;
            
            // Al arrastrar, si no estamos en modo carrusel, lo activamos
            if (!isCarouselMode) {
                activarModoCarrusel();
            }
            
            // Actualizar posición del botón
            boton.setLayoutX(event.getSceneX() - xOffset);
            boton.setLayoutY(event.getSceneY() - yOffset);
            
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
                    JFXButton[] botones = {btnMenuPrincipalCarousel, btnAnimalesCarousel, but_clientesCarousel, btnFichajeCarousel, btnSalirCarousel, btnChatCarousel, btnFacturacionCarousel, btnInformesCarousel};
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
                btnChat.setText("Chat");
                btnFacturacion.setText("Facturacion");
                btnInformes.setText("Informes");
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
                btnChat.setText("");
                btnFacturacion.setText("");
                btnInformes.setText("");
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
        JFXButton[] botones = {btnMenuPrincipalCarousel, btnAnimalesCarousel, but_clientesCarousel, btnFichajeCarousel, btnEmpresaCarousel, btnSalirCarousel, btnChatCarousel, btnFacturacionCarousel, btnInformesCarousel};
        double baseRadio = 100;
        double radio = baseRadio + (botones.length - 6) * 30; // Aumenta el radio si hay más de 6 botones
        double centerX = btnChicha.getLayoutX() + btnChicha.getWidth() / 2;
        double centerY = btnChicha.getLayoutY() + btnChicha.getHeight() / 2;
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
        JFXButton[] botonesCarousel = {btnMenuPrincipalCarousel, btnAnimalesCarousel, but_clientesCarousel, btnFichajeCarousel, btnSalirCarousel, btnFacturacionCarousel, btnInformesCarousel};
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
        JFXButton[] botonesCarousel = {btnMenuPrincipalCarousel, btnAnimalesCarousel, but_clientesCarousel, btnFichajeCarousel, btnEmpresaCarousel, btnSalirCarousel, btnChatCarousel, btnFacturacionCarousel, btnInformesCarousel};
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
        try {
            // Desmarcar otros botones y marcar el seleccionado
            desmarcaTodosLosBotones();
            btnFichaje.getStyleClass().add("menu-button-selected");
            
            // Cargar la vista de fichaje
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Modulos/Fichaje/fichaje-view.fxml"));
            Parent contenido = ThemeUtil.loadWithTheme(loader);
            
            // Aplicar estilos CSS específicos del módulo de fichaje
            contenido.getStylesheets().add(getClass().getResource("/Estilos/fichaje-styles.css").toExternalForm());
            
            // Obtener el BorderPane central y reemplazar su contenido
            BorderPane centerPane = (BorderPane) root.getCenter();
            centerPane.setCenter(contenido);
            
            // Asegurarse de que todas las ventanas tengan el tema aplicado
            javafx.application.Platform.runLater(ThemeUtil::applyThemeToAllOpenWindows);
            
            // Actualizar el título
            lblClinica.setText("Sistema de Fichaje");
            
            // Mantener visible el carrusel
            mantenerCarruselVisible();
            
        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error", "Error al cargar el módulo de fichaje: " + e.getMessage());
        }
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
        JFXButton[] botones = {btnMenuPrincipalCarousel, btnAnimalesCarousel, but_clientesCarousel, btnFichajeCarousel, btnSalirCarousel, btnFacturacionCarousel, btnInformesCarousel};
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
                JFXButton[] botones = {btnMenuPrincipalCarousel, btnAnimalesCarousel, but_clientesCarousel, btnFichajeCarousel, btnEmpresaCarousel, btnSalirCarousel, btnChatCarousel, btnFacturacionCarousel, btnInformesCarousel};
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
                JFXButton[] botones = {btnMenuPrincipalCarousel, btnAnimalesCarousel, but_clientesCarousel, btnFichajeCarousel, btnEmpresaCarousel, btnSalirCarousel, btnChatCarousel, btnFacturacionCarousel, btnInformesCarousel};
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

    /**
     * Actualiza el contador de eventos para el usuario actual usando petición al servidor
     */
    private void actualizarContadorEventos() {
        if (usuarioActual == null || btnEventCounter == null) {
            return;
        }
        
        // Usar un hilo secundario para no bloquear la UI
        new Thread(() -> {
            try {
                // Hacer petición al servidor para obtener el resumen de eventos
                String peticion = Protocolo.OBTENER_RESUMEN_EVENTOS_USUARIO + Protocolo.SEPARADOR_CODIGO + usuarioActual.getUsuario();
                gestorSocket.enviarPeticion(peticion);
                
                // Leer la respuesta del servidor
                ObjectInputStream entrada = gestorSocket.getEntrada();
                int codigoRespuesta = entrada.readInt();
                
                if (codigoRespuesta == Protocolo.OBTENER_RESUMEN_EVENTOS_USUARIO_RESPONSE) {
                    // Leer el objeto EventSummary del servidor
                    final com.example.pruebamongodbcss.calendar.EventSummary summary = 
                        (com.example.pruebamongodbcss.calendar.EventSummary) entrada.readObject();
                    
                    // Actualizar el botón en el hilo de la UI
                    javafx.application.Platform.runLater(() -> {
                        try {
                            // Actualizar el texto con el total
                            btnEventCounter.setText(String.valueOf(summary.getTotal()));
                            
                            // Crear el contenido HTML para el tooltip con el estilo moderno
                            String tooltipContent = String.format(
                                "<html><head><style>" +
                                "body { margin: 0; padding: 0; overflow: hidden; }" +
                                ".radio-container {" +
                                "  --main-color: #f7e479;" +
                                "  --main-color-opacity: #f7e4791c;" +
                                "  --total-radio: 3;" +
                                "  background: #1a1a1a;" +
                                "  padding: 12px;" +
                                "  border-radius: 8px;" +
                                "  width: 200px;" +
                                "  box-sizing: border-box;" +
                                "}" +
                                ".event-type {" +
                                "  color: #ffffff;" +
                                "  padding: 6px 10px;" +
                                "  margin: 3px 0;" +
                                "  position: relative;" +
                                "  display: flex;" +
                                "  justify-content: space-between;" +
                                "  align-items: center;" +
                                "  border-left: 2px solid transparent;" +
                                "  transition: all 0.3s ease;" +
                                "  font-size: 13px;" +
                                "}" +
                                ".event-type:hover {" +
                                "  background: rgba(247, 228, 121, 0.1);" +
                                "  border-left-color: #f7e479;" +
                                "  padding-left: 15px;" +
                                "}" +
                                ".event-count {" +
                                "  color: #f7e479;" +
                                "  font-weight: bold;" +
                                "  margin-left: 10px;" +
                                "}" +
                                ".total-events {" +
                                "  color: #f7e479;" +
                                "  font-size: 14px;" +
                                "  font-weight: bold;" +
                                "  text-align: center;" +
                                "  padding: 5px 0;" +
                                "  margin-bottom: 8px;" +
                                "  border-bottom: 1px solid rgba(247, 228, 121, 0.2);" +
                                "}" +
                                "</style></head><body>" +
                                "<div class='radio-container'>" +
                                "<div class='total-events'>Total de Eventos: %d</div>" +
                                "<div class='event-type'>Reuniones<span class='event-count'>%d</span></div>" +
                                "<div class='event-type'>Recordatorios<span class='event-count'>%d</span></div>" +
                                "<div class='event-type'>Citas Médicas<span class='event-count'>%d</span></div>" +
                                "</div></body></html>",
                                summary.getTotal(),
                                summary.getMeetings(),
                                summary.getReminders(),
                                summary.getAppointments()
                            );
                            
                            // Crear un nuevo tooltip con WebView para soportar HTML
                            Tooltip tooltip = new Tooltip();
                            tooltip.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                            
                            // Usar WebView para mostrar el contenido HTML
                            javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
                            webView.setPrefSize(200, 140); // Ajustado para el contenido exacto
                            webView.setMaxSize(200, 140);
                            webView.setMinSize(200, 140);
                            webView.setContextMenuEnabled(false);
                            
                            // Deshabilitar scroll
                            webView.getEngine().setJavaScriptEnabled(true);
                            webView.getEngine().loadContent(tooltipContent);
                            
                            // Hacer el fondo del WebView transparente
                            webView.setStyle("-fx-background-color: transparent;");
                            
                            tooltip.setGraphic(webView);
                            tooltip.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
                            
                            // Configurar los tiempos del tooltip
                            tooltip.setShowDelay(javafx.util.Duration.millis(100));
                            tooltip.setShowDuration(javafx.util.Duration.seconds(20));
                            tooltip.setHideDelay(javafx.util.Duration.millis(200));
                            
                            btnEventCounter.setTooltip(tooltip);
                            
                            // Hacer visible el botón
                            btnEventCounter.setVisible(true);
                            
                            // Aplicar estilo adicional según la cantidad
                            if (summary.getTotal() > 0) {
                                if (!btnEventCounter.getStyleClass().contains("has-events")) {
                                    btnEventCounter.getStyleClass().add("has-events");
                                }
                            } else {
                                btnEventCounter.getStyleClass().remove("has-events");
                            }
                        } catch (Exception e) {
                            System.err.println("Error al actualizar UI del contador: " + e.getMessage());
                        }
                    });
                } else if (codigoRespuesta == Protocolo.ERROR_OBTENER_RESUMEN_EVENTOS_USUARIO) {
                    // Error del servidor
                    javafx.application.Platform.runLater(() -> {
                        btnEventCounter.setText("?");
                        Tooltip errorTooltip = new Tooltip("Error al cargar eventos desde el servidor");
                        errorTooltip.setStyle(
                            "-fx-background-color: #1a1a1a;" +
                            "-fx-text-fill: #ff4444;" +
                            "-fx-font-size: 12px;" +
                            "-fx-padding: 10px;" +
                            "-fx-border-radius: 5px;" +
                            "-fx-background-radius: 5px;"
                        );
                        btnEventCounter.setTooltip(errorTooltip);
                        btnEventCounter.setVisible(true);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                // En caso de error de comunicación, mostrar "?" en el contador
                javafx.application.Platform.runLater(() -> {
                    btnEventCounter.setText("?");
                    Tooltip errorTooltip = new Tooltip("Error de comunicación con el servidor");
                    errorTooltip.setStyle(
                        "-fx-background-color: #1a1a1a;" +
                        "-fx-text-fill: #ff4444;" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 10px;" +
                        "-fx-border-radius: 5px;" +
                        "-fx-background-radius: 5px;"
                    );
                    btnEventCounter.setTooltip(errorTooltip);
                    btnEventCounter.setVisible(true);
                });
            }
        }).start();
    }

    /**
     * Configura una actualización periódica del contador de eventos
     */
    private void configurarActualizacionPeriodicaEventos() {
        // Crear un Timeline que se ejecute cada 5 minutos
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(
                javafx.util.Duration.minutes(5),
                event -> {
                    // Solo actualizar si hay un usuario activo
                    if (usuarioActual != null) {
                        actualizarContadorEventos();
                    }
                }
            )
        );
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * Centra dinámicamente el botón del carrusel en el panel central
     */
    private void centrarBotonCarrusel() {
        if (btnChicha != null && btnChicha.isVisible()) {
            BorderPane centerPane = (BorderPane) root.getCenter();
            if (centerPane != null && centerPane.getCenter() instanceof Pane) {
                Pane mainPane = (Pane) centerPane.getCenter();
                
                // Calcular posición central
                double x = (mainPane.getWidth() - btnChicha.getWidth()) / 2;
                double y = (mainPane.getHeight() - btnChicha.getHeight()) / 2;
                
                // Aplicar posición solo si son valores válidos
                if (x >= 0 && y >= 0) {
                    btnChicha.setLayoutX(x);
                    btnChicha.setLayoutY(y);
                }
            }
        }
    }

    private void abrirModuloChat() {
        try {
            // Desmarcar otros botones y marcar el seleccionado
            desmarcaTodosLosBotones();
            btnChat.getStyleClass().add("menu-button-selected");
            
            // Iniciar el servidor de chat si no está corriendo
            ServidorAppChat servidor = ServidorAppChat.getInstance();
            servidor.iniciar();
            
            // Cargar la ventana de chat
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/chatPanel.fxml"));
            Parent root = loader.load();
            
            VentanaChat controller = loader.getController();
            controller.setUsuarioActual(usuarioActual.getId().toString(), usuarioActual.getNombre());
            
            Scene scene = new Scene(root, 620, 450);
            scene.getStylesheets().add(getClass().getResource("/Estilos/chatOscuro.css").toExternalForm());
            
            Stage stage = new Stage();
            stage.setTitle("Chat - " + usuarioActual.getNombre());
            
            // Establecer el icono de la ventana
            try {
                Image icon = new Image(getClass().getResourceAsStream("/logo.png"));
                stage.getIcons().add(icon);
            } catch (Exception e) {
                System.err.println("No se pudo cargar el icono de la ventana de chat: " + e.getMessage());
            }
            
            stage.setScene(scene);
            
            // Habilitar redimensión
            controller.habilitarRedimension(stage, scene);
            
            // Mostrar la ventana
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo abrir el módulo de chat: " + e.getMessage());
        }
    }

    @FXML
    private void abrirChat() {
        try {
            Usuario usuario = getUsuarioActual();
            if (usuario != null) {
                // Iniciar el servidor primero y esperar un momento
                ServidorAppChat servidor = ServidorAppChat.getInstance();
                servidor.iniciar();
                
                // Pequeña pausa para asegurar que el servidor está listo
                Thread.sleep(500);
                
                // Configurar el estilo de la ventana antes de cargar el FXML
                Stage stage = new Stage();
                stage.initStyle(StageStyle.TRANSPARENT);
                
                // Establecer el icono de la ventana
                try {
                    Image icon = new Image(getClass().getResourceAsStream("/logo.png"));
                    stage.getIcons().add(icon);
                } catch (Exception e) {
                    System.err.println("No se pudo cargar el icono de la ventana de chat: " + e.getMessage());
                }
                
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/chatPanel.fxml"));
                Scene scene = new Scene(fxmlLoader.load());
                scene.setFill(null); // Hacer el fondo transparente
                
                stage.setScene(scene);
                
                VentanaChat ventanaChat = fxmlLoader.getController();
                ventanaChat.setUsuarioActual(usuario.getId().toString(), usuario.getNombre());
                ventanaChat.habilitarRedimension(stage, scene);
                
                stage.show();
            } else {
                mostrarError("Error", "No hay usuario activo en la sesión");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            mostrarError("Error", "Error al abrir el chat: " + e.getMessage());
        }
    }

    private void abrirModuloInformes() {
        try {
            // Desmarcar otros botones y marcar el seleccionado
            desmarcaTodosLosBotones();
            btnInformes.getStyleClass().add("menu-button-selected");
            
            // Cargar la vista de informes
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Modulos/Informes/informes-view.fxml"));
            Parent contenido = ThemeUtil.loadWithTheme(loader);
            
            // Obtener el controlador y configurar el usuario actual
            com.example.pruebamongodbcss.Modulos.Informes.InformesController informesController = loader.getController();
            if (informesController != null && usuarioActual != null) {
                informesController.setUsuarioActual(usuarioActual);
            }
            
            // Obtener el BorderPane central y reemplazar su contenido
            BorderPane centerPane = (BorderPane) root.getCenter();
            centerPane.setCenter(contenido);
            
            // Asegurarse de que todas las ventanas tengan el tema aplicado
            javafx.application.Platform.runLater(ThemeUtil::applyThemeToAllOpenWindows);
            
            // Actualizar el título
            lblClinica.setText("Informes");
            
            // Mantener visible el carrusel si está activo
            mantenerCarruselVisible();
            
        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error", "Error al cargar el módulo de informes: " + e.getMessage());
        }
    }
}