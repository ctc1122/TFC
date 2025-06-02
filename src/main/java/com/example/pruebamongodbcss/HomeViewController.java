package com.example.pruebamongodbcss;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.example.pruebamongodbcss.calendar.CalendarFXComponent;
import com.example.pruebamongodbcss.calendar.CalendarPreview;
import com.example.pruebamongodbcss.calendar.CalendarScreen;
import com.example.pruebamongodbcss.theme.ThemeManager;
import com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes;
import com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.DashboardMetricas;
import com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.EstadisticasFichajes;
import com.jfoenix.controls.JFXButton;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Controller for the home-view.fxml which displays the modern energy-themed home page
 */
public class HomeViewController implements Initializable {

    @FXML
    private BorderPane homeContainer;
    
    @FXML
    private StackPane pagesContainer;
    
    @FXML
    private VBox page1;
    
    @FXML
    private VBox page2;
    
    @FXML
    private GridPane page1Grid;
    
    @FXML
    private Rectangle indicator1;
    
    @FXML
    private Rectangle indicator2;
    
    @FXML
    private HBox pageIndicatorContainer;
    
    @FXML
    private JFXButton btnLearnMore;
    
    @FXML
    private JFXButton btnViewCalendar;
    
    // Nuevos elementos para métricas
    @FXML
    private BorderPane ventasCard;
    
    @FXML
    private BorderPane fichajeCard;
    
    @FXML
    private Label lblVentasMes;
    
    @FXML
    private Label lblCambioVentas;
    
    @FXML
    private Label lblFacturasMes;
    
    @FXML
    private Label lblPromedioVenta;
    
    @FXML
    private JFXButton btnDescargarVentas;
    
    @FXML
    private Label lblHorasMes;
    
    @FXML
    private Label lblEstadoFichaje;
    
    @FXML
    private Label lblHorasHoy;
    
    @FXML
    private Label lblPromedioHoras;
    
    @FXML
    private ProgressBar progressHoras;
    
    // Botones de reportes
    @FXML
    private GridPane reportsGrid;
    
    @FXML
    private JFXButton btnReporteVentas;
    
    @FXML
    private JFXButton btnReporteClientes;
    
    @FXML
    private JFXButton btnReporteEmpleados;
    
    @FXML
    private JFXButton btnReporteServicios;
   
    private boolean isPage1Visible = true;
    private boolean isAnimating = false;
    private ScrollPane scrollWrapper;
    private boolean isScrolling = false;
    private double lastScrollY = 0;
    private static final double SCROLL_THRESHOLD = 30.0;
    
    // Componentes del calendario
    private CalendarFXComponent calendarComponent;
    private CalendarPreview calendarPreview;
    
    // Servicio de informes
    private ServicioInformes servicioInformes;
    
    // Datos actuales
    private DashboardMetricas dashboardMetricas;
    private EstadisticasFichajes estadisticasFichajes;
    private ServicioInformes.EstadisticasFichajeUsuario estadisticasFichajeUsuario;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Register the scene with the ThemeManager when it's available
        homeContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                ThemeManager.getInstance().registerScene(newScene);
                // Configurar el scroll una vez que la escena esté disponible
                setupScrollFunctionality(newScene);
            }
        });
        
        // Initialize services
        servicioInformes = new ServicioInformes();
        
        // Apply initial styles
        applyStyles();
        
        // Set button action handlers
        setButtonActions();
        
        // Configure initial page state
        configurePageTransitions();
        
        // Inicializar la vista previa del calendario en la página 1
        initializeCalendarPreview();
        
        // Cargar métricas
        cargarMetricas();
        
        // Inicializar timer para actualizar métricas de fichaje
        inicializarTimerFichaje();
    }
    
    /**
     * Cargar métricas de ventas y fichajes
     */
    private void cargarMetricas() {
        // Ejecutar en hilo separado para no bloquear la UI
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Cargar métricas de dashboard
                    dashboardMetricas = servicioInformes.obtenerMetricasDashboard();
                    estadisticasFichajes = servicioInformes.obtenerEstadisticasFichajes();
                    
                    // Cargar estadísticas específicas del usuario actual
                    com.example.pruebamongodbcss.Data.Usuario usuarioActual = PanelInicioController.getUsuarioSesion();
                    if (usuarioActual != null) {
                        estadisticasFichajeUsuario = servicioInformes.obtenerEstadisticasFichajeUsuario(usuarioActual.getUsuario());
                    }
                    
                    // Actualizar UI en el hilo de JavaFX
                    Platform.runLater(() -> {
                        actualizarMetricasVentas();
                        actualizarMetricasFichaje();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        System.err.println("Error al cargar métricas: " + e.getMessage());
                        // Mostrar valores por defecto
                        mostrarMetricasPorDefecto();
                    });
                }
                return null;
            }
        };
        
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    /**
     * Inicializar timer para actualizar métricas de fichaje cada minuto
     */
    private void inicializarTimerFichaje() {
        // Timer para actualizar las métricas de fichaje cada minuto (solo si el usuario está fichado)
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(Duration.seconds(60), e -> {
                // Solo actualizar si hay un usuario y si está fichado
                com.example.pruebamongodbcss.Data.Usuario usuarioActual = PanelInicioController.getUsuarioSesion();
                if (usuarioActual != null && estadisticasFichajeUsuario != null && estadisticasFichajeUsuario.isHaFichadoHoy()) {
                    // Cargar métricas de fichaje actualizadas en hilo separado
                    Task<Void> taskActualizacion = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            try {
                                estadisticasFichajeUsuario = servicioInformes.obtenerEstadisticasFichajeUsuario(usuarioActual.getUsuario());
                                Platform.runLater(() -> actualizarMetricasFichaje());
                            } catch (Exception ex) {
                                System.err.println("Error al actualizar métricas de fichaje: " + ex.getMessage());
                            }
                            return null;
                        }
                    };
                    Thread thread = new Thread(taskActualizacion);
                    thread.setDaemon(true);
                    thread.start();
                }
            })
        );
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();
    }
    
    /**
     * Actualizar las métricas de fichaje en la UI usando datos específicos del usuario
     */
    private void actualizarMetricasFichaje() {
        if (estadisticasFichajeUsuario != null) {
            // Formatear horas del mes
            String horasMes = estadisticasFichajeUsuario.getHorasTrabajadasFormateadas();
            String estadoFichaje = estadisticasFichajeUsuario.getEstadoFormateado();
            String horasHoy = estadisticasFichajeUsuario.getTiempoHoyFormateado();
            String promedioHoras = String.format("%.1fh diarias", estadisticasFichajeUsuario.getPromedioHorasDiarias());
            
            lblHorasMes.setText(horasMes.replace(" ", "").replace("h", "h ").replace("m", "m"));
            lblEstadoFichaje.setText(estadoFichaje);
            lblHorasHoy.setText(horasHoy.replace(" ", "").replace("h", "h ").replace("m", "m"));
            lblPromedioHoras.setText(String.format("%.1fh", estadisticasFichajeUsuario.getPromedioHorasDiarias()));
            
            // Calcular progreso de la barra (meta: 160h mensuales)
            double metaMensual = 160.0;
            double progreso = Math.min(estadisticasFichajeUsuario.getHorasTrabajadasMes() / metaMensual, 1.0);
            progressHoras.setProgress(progreso);
            
            // Cambiar estilo del estado según si está fichado o no
            lblEstadoFichaje.getStyleClass().removeAll("metric-change-positive", "metric-change-negative");
            if (estadisticasFichajeUsuario.isHaFichadoHoy()) {
                if ("ABIERTO".equals(estadisticasFichajeUsuario.getEstadoFichaje())) {
                    lblEstadoFichaje.getStyleClass().add("metric-change-positive");
                } else {
                    lblEstadoFichaje.getStyleClass().add("metric-change");
                }
            } else {
                lblEstadoFichaje.getStyleClass().add("metric-change-negative");
            }
        } else {
            // Valores por defecto si no hay datos del usuario
            lblHorasMes.setText("0h");
            lblEstadoFichaje.setText("No fichado");
            lblHorasHoy.setText("0h");
            lblPromedioHoras.setText("0h");
            progressHoras.setProgress(0.0);
        }
    }
    
    /**
     * Actualizar las métricas de ventas en la UI (versión mejorada)
     */
    private void actualizarMetricasVentas() {
        if (dashboardMetricas != null) {
            // Formatear valores monetarios
            String ventasMes = String.format("€%.0f", dashboardMetricas.getVentasMesActual());
            String cambioVentas = String.format("%.1f%% vs anterior", dashboardMetricas.getPorcentajeCambioVentas());
            String facturas = String.valueOf(dashboardMetricas.getNumeroFacturasMes());
            String promedio = String.format("€%.0f", dashboardMetricas.getPromedioVentasDiarias());
            
            lblVentasMes.setText(ventasMes);
            lblCambioVentas.setText(cambioVentas);
            lblFacturasMes.setText(facturas);
            lblPromedioVenta.setText(promedio);
            
            // Cambiar color según el cambio porcentual
            lblCambioVentas.getStyleClass().removeAll("metric-change-positive", "metric-change-negative");
            if (dashboardMetricas.getPorcentajeCambioVentas() > 0) {
                lblCambioVentas.getStyleClass().add("metric-change-positive");
            } else if (dashboardMetricas.getPorcentajeCambioVentas() < 0) {
                lblCambioVentas.getStyleClass().add("metric-change-negative");
            } else {
                lblCambioVentas.getStyleClass().add("metric-change");
            }
        }
    }
    
    /**
     * Mostrar métricas por defecto cuando hay error
     */
    private void mostrarMetricasPorDefecto() {
        lblVentasMes.setText("€0");
        lblCambioVentas.setText("0% vs anterior");
        lblFacturasMes.setText("0");
        lblPromedioVenta.setText("€0");
        
        lblHorasMes.setText("0h");
        lblEstadoFichaje.setText("No fichado");
        lblHorasHoy.setText("0h");
        lblPromedioHoras.setText("0h");
        progressHoras.setProgress(0.0);
    }
    
    /**
     * Descargar reporte de ventas en formato CSV
     */
    private void descargarReporteVentas() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte de Ventas");
        fileChooser.setInitialFileName("reporte_ventas_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        
        Stage stage = (Stage) homeContainer.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    generarReporteCSV(file);
                    return null;
                }
                
                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Reporte Generado");
                        alert.setHeaderText(null);
                        alert.setContentText("El reporte de ventas se ha generado correctamente.");
                        alert.showAndWait();
                    });
                }
                
                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Error al generar reporte");
                        alert.setContentText("No se pudo generar el reporte CSV.");
                        alert.showAndWait();
                    });
                }
            };
            
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }
    
    /**
     * Generar reporte CSV
     */
    private void generarReporteCSV(File file) throws Exception {
        try (FileWriter writer = new FileWriter(file)) {
            // Escribir cabecera CSV
            writer.append("REPORTE DE VENTAS MENSUAL\n");
            writer.append("Fecha:," + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n\n");
            
            writer.append("Métrica,Valor\n");
            
            if (dashboardMetricas != null) {
                writer.append("Ventas del mes actual,€" + String.format("%.2f", dashboardMetricas.getVentasMesActual()) + "\n");
                writer.append("Ventas del mes anterior,€" + String.format("%.2f", dashboardMetricas.getVentasMesAnterior()) + "\n");
                writer.append("Cambio porcentual," + String.format("%.1f%%", dashboardMetricas.getPorcentajeCambioVentas()) + "\n");
                writer.append("Número de facturas," + dashboardMetricas.getNumeroFacturasMes() + "\n");
                writer.append("Promedio de ventas diarias,€" + String.format("%.2f", dashboardMetricas.getPromedioVentasDiarias()) + "\n");
                writer.append("Ventas de hoy,€" + String.format("%.2f", dashboardMetricas.getVentasHoy()) + "\n");
                writer.append("Citas de hoy," + dashboardMetricas.getCitasHoy() + "\n");
            }
            
            writer.append("\nReporte generado automáticamente por el Sistema de Gestión Veterinaria\n");
        }
    }
    
    /**
     * Navegar a una vista específica de reporte
     */
    private void navegarAReporte(String tipoReporte) {
        try {
            // Usar siempre el módulo de informes genérico con título específico
            String fxmlPath = "/com/example/pruebamongodbcss/Modulos/Informes/informes-view.fxml";
            String titulo = "";
            
            // Determinar el título según el tipo de reporte
            switch (tipoReporte) {
                case "Ventas":
                    titulo = "Informes y Análisis: Ventas";
                    break;
                case "Clientes":
                    titulo = "Informes y Análisis: Clientes";
                    break;
                case "Empleados":
                    titulo = "Informes y Análisis: Empleados";
                    break;
                case "Servicios":
                    titulo = "Informes y Análisis: Servicios";
                    break;
                case "Fichajes":
                    titulo = "Informes y Análisis: Fichajes";
                    break;
                default:
                    titulo = "Informes y Análisis";
                    break;
            }
            
            // Buscar el BorderPane principal del PanelInicioController
            javafx.scene.Scene currentScene = homeContainer.getScene();
            if (currentScene != null && currentScene.getRoot() instanceof BorderPane) {
                BorderPane mainRoot = (BorderPane) currentScene.getRoot();
                
                // Cargar la vista de informes
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Node informesView = loader.load();
                
                // Reemplazar el contenido central
                BorderPane centerPane = (BorderPane) mainRoot.getCenter();
                if (centerPane != null) {
                    centerPane.setCenter(informesView);
                } else {
                    mainRoot.setCenter(informesView);
                }
                
                // Actualizar el título si existe
                javafx.scene.control.Label lblClinica = (javafx.scene.control.Label) mainRoot.lookup("#lblClinica");
                if (lblClinica != null) {
                    lblClinica.setText(titulo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al cargar el reporte de " + tipoReporte + ": " + e.getMessage());
        }
    }
    
    /**
     * Initialize the calendar component and add it to page2
     */
    private void initializeCalendar() {
        // Ya no se usa para page2, pero se mantiene para compatibilidad
    }
    
    /**
     * Create sample calendar entries (appointments)
     */
    private void createSampleEntries(Calendar citasNormales, Calendar citasUrgentes, 
                                  Calendar citasCompletadas, Calendar citasCanceladas) {
        // Crear eventos para el mes actual
        LocalDate today = LocalDate.now();
        Random random = new Random();
        
        // Listas de títulos y ubicaciones de ejemplo
        List<String> eventTitles = Arrays.asList(
            "Consulta veterinaria", 
            "Vacunación canina", 
            "Control de salud",
            "Revisión felina", 
            "Cirugía menor", 
            "Desparasitación",
            "Análisis de sangre", 
            "Consulta de seguimiento", 
            "Limpieza dental",
            "Tratamiento de heridas"
        );
        
        List<String> eventLocations = Arrays.asList(
            "Sala de consulta 1", 
            "Sala de consulta 2", 
            "Sala de tratamientos",
            "Quirófano", 
            "Laboratorio", 
            "Área de rehabilitación"
        );
        
        // Crear citas normales
        for (int i = 0; i < 10; i++) {
            Entry<String> entry = new Entry<>();
            entry.setTitle(eventTitles.get(random.nextInt(eventTitles.size())));
            entry.setLocation(eventLocations.get(random.nextInt(eventLocations.size())));
            
            LocalDate date = today.plusDays(random.nextInt(14)); // Próximos 14 días
            LocalTime startTime = LocalTime.of(9 + random.nextInt(8), 15 * random.nextInt(4));
            LocalTime endTime = startTime.plusMinutes(30 + random.nextInt(4) * 15);
            
            entry.setInterval(date, startTime, date, endTime);
            
            citasNormales.addEntry(entry);
        }
        
        // Crear citas urgentes
        for (int i = 0; i < 3; i++) {
            Entry<String> entry = new Entry<>();
            entry.setTitle("URGENTE: " + eventTitles.get(random.nextInt(eventTitles.size())));
            entry.setLocation(eventLocations.get(random.nextInt(eventLocations.size())));
            
            LocalDate date = today.plusDays(random.nextInt(7)); // Próximos 7 días
            LocalTime startTime = LocalTime.of(9 + random.nextInt(8), 15 * random.nextInt(4));
            LocalTime endTime = startTime.plusMinutes(30 + random.nextInt(4) * 15);
            
            entry.setInterval(date, startTime, date, endTime);
            
            citasUrgentes.addEntry(entry);
        }
        
        // Crear citas completadas
        for (int i = 0; i < 5; i++) {
            Entry<String> entry = new Entry<>();
            entry.setTitle(eventTitles.get(random.nextInt(eventTitles.size())) + " (Completada)");
            entry.setLocation(eventLocations.get(random.nextInt(eventLocations.size())));
            
            LocalDate date = today.minusDays(random.nextInt(7)); // Últimos 7 días
            LocalTime startTime = LocalTime.of(9 + random.nextInt(8), 15 * random.nextInt(4));
            LocalTime endTime = startTime.plusMinutes(30 + random.nextInt(4) * 15);
            
            entry.setInterval(date, startTime, date, endTime);
            
            citasCompletadas.addEntry(entry);
        }
        
        // Crear citas canceladas
        for (int i = 0; i < 2; i++) {
            Entry<String> entry = new Entry<>();
            entry.setTitle(eventTitles.get(random.nextInt(eventTitles.size())) + " (Cancelada)");
            entry.setLocation(eventLocations.get(random.nextInt(eventLocations.size())));
            
            LocalDate date = today.plusDays(random.nextInt(10) - 5); // Entre -5 y +5 días
            LocalTime startTime = LocalTime.of(9 + random.nextInt(8), 15 * random.nextInt(4));
            LocalTime endTime = startTime.plusMinutes(30 + random.nextInt(4) * 15);
            
            entry.setInterval(date, startTime, date, endTime);
            
            citasCanceladas.addEntry(entry);
        }
    }
    
    /**
     * Configurar la funcionalidad de scroll para navegar entre páginas
     */
    private void setupScrollFunctionality(javafx.scene.Scene scene) {
        // Crear un wrapper de ScrollPane en código (sin afectar el FXML)
        if (scrollWrapper == null) {
            // Añadir eventos de scroll a la escena
            scene.addEventFilter(ScrollEvent.SCROLL, this::handleScroll);
            
            // Configurar las propiedades del contenedor para detectar swipes en pantallas táctiles
            pagesContainer.setOnSwipeUp(e -> {
                if (isPage1Visible && !isAnimating) {
                    navigateToPage2();
                }
            });
            
            pagesContainer.setOnSwipeDown(e -> {
                if (!isPage1Visible && !isAnimating) {
                    navigateToPage1();
                }
            });
        }
    }
    
    /**
     * Manejar eventos de scroll
     */
    private void handleScroll(ScrollEvent event) {
        if (isAnimating) return;
        
        // Calcular la dirección y cantidad de scroll
        double deltaY = event.getDeltaY();
        lastScrollY += deltaY;
        
        // Determinar si el scroll es suficiente para cambiar de página
        if (Math.abs(lastScrollY) > SCROLL_THRESHOLD) {
            if (lastScrollY < 0 && isPage1Visible) {
                // Scroll hacia abajo - navegar a página 2
                navigateToPage2();
            } else if (lastScrollY > 0 && !isPage1Visible) {
                // Scroll hacia arriba - navegar a página 1
                navigateToPage1();
            }
            
            // Reiniciar el contador de scroll
            lastScrollY = 0;
        }
        
        // Prevenir propagación del evento para evitar comportamiento no deseado
        event.consume();
    }
    
    /**
     * Apply styles based on current theme
     */
    private void applyStyles() {
        // Add custom stylesheets for home view
        String homeStylesheet = getClass().getResource("/com/example/pruebamongodbcss/theme/home-styles.css").toExternalForm();
        String calendarStylesheet = getClass().getResource("/com/example/pruebamongodbcss/theme/calendar-styles.css").toExternalForm();
        String jfxCalendarStylesheet = getClass().getResource("/com/example/pruebamongodbcss/theme/jfx-calendar-styles.css").toExternalForm();
        
        // Apply stylesheets if not already present
        if (homeContainer.getScene() != null) {
            if (!homeContainer.getScene().getStylesheets().contains(homeStylesheet)) {
                homeContainer.getScene().getStylesheets().add(homeStylesheet);
            }
            if (!homeContainer.getScene().getStylesheets().contains(calendarStylesheet)) {
                homeContainer.getScene().getStylesheets().add(calendarStylesheet);
            }
            if (!homeContainer.getScene().getStylesheets().contains(jfxCalendarStylesheet)) {
                homeContainer.getScene().getStylesheets().add(jfxCalendarStylesheet);
            }
        }
    }
    
    /**
     * Set action handlers for all buttons
     */
    private void setButtonActions() {
        // Add learn more button handler
        btnLearnMore.setOnAction(e -> navigateToPage2());
        
        // Botón VER CALENDARIO
        btnViewCalendar.setOnAction(e -> {
            try {
                // Verificar que el usuario esté logueado
                com.example.pruebamongodbcss.Data.Usuario usuarioActual = PanelInicioController.getUsuarioSesion();
                if (usuarioActual == null) {
                    System.err.println("Error: No hay usuario logueado");
                    return;
                }
                
                // Crear una instancia del componente de calendario personalizado CON EL USUARIO ACTUAL
                CalendarScreen calendarScreen = new CalendarScreen(usuarioActual);
        
                // Asegurar que el tema se aplique correctamente
                if (ThemeManager.getInstance().isDarkTheme()) {
                    calendarScreen.getStyleClass().add("dark-theme");
                }
                
                // Buscar el BorderPane principal del PanelInicioController
                // Necesitamos acceder al root del PanelInicioController
                javafx.scene.Scene currentScene = homeContainer.getScene();
                if (currentScene != null && currentScene.getRoot() instanceof BorderPane) {
                    BorderPane mainRoot = (BorderPane) currentScene.getRoot();
                    
                    // Reemplazar el contenido central con el componente de calendario
                    BorderPane centerPane = (BorderPane) mainRoot.getCenter();
                    if (centerPane != null) {
                        centerPane.setCenter(calendarScreen);
                    } else {
                        mainRoot.setCenter(calendarScreen);
                    }
                    
                    // Buscar y actualizar el título si existe
                    javafx.scene.control.Label lblClinica = (javafx.scene.control.Label) mainRoot.lookup("#lblClinica");
                    if (lblClinica != null) {
                        lblClinica.setText("Calendario de Citas: " + usuarioActual.getUsuario());
                    }
                }
                
            } catch (Exception ex) {
                ex.printStackTrace();
                System.err.println("Error al cargar el módulo de calendario: " + ex.getMessage());
            }
        });
        
        // Botón de descarga de CSV de ventas
        btnDescargarVentas.setOnAction(e -> descargarReporteVentas());
        
        // Botones de reportes en página 2
        btnReporteVentas.setOnAction(e -> navegarAReporte("Ventas"));
        btnReporteClientes.setOnAction(e -> navegarAReporte("Clientes"));
        btnReporteEmpleados.setOnAction(e -> navegarAReporte("Empleados"));
        btnReporteServicios.setOnAction(e -> navegarAReporte("Servicios"));
        
        // Add hover effects to all buttons
        homeContainer.lookupAll(".btn-card, .btn-footer, .btn-back").forEach(node -> {
            if (node instanceof JFXButton) {
                JFXButton button = (JFXButton) node;
                
                // Add hover effect
                button.setOnMouseEntered(e -> button.setStyle("-fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
                button.setOnMouseExited(e -> button.setStyle("-fx-scale-x: 1.0; -fx-scale-y: 1.0;"));
                
                // Add click handler para botones que no sean navegación
                if (button != btnLearnMore && button != btnViewCalendar && 
                    button != btnDescargarVentas && button != btnReporteVentas &&
                    button != btnReporteClientes && button != btnReporteEmpleados &&
                    button != btnReporteServicios) {
                    button.setOnAction(e -> handleButtonClick(button));
                }
            }
        });
        
        // Make indicators clickable for navigation
        indicator1.setOnMouseClicked(e -> navigateToPage1());
        indicator2.setOnMouseClicked(e -> navigateToPage2());
    }
    
    /**
     * Configure page transitions
     */
    private void configurePageTransitions() {
        // Set initial page state
        page1.setVisible(true);
        page2.setVisible(false);
        page1.setOpacity(1.0);
        page2.setOpacity(0.0);
        
        // Ensure indicators are showing correct state
        updateIndicators(true);
    }
    
    /**
     * Update page indicators based on current page
     * @param isPage1 true if page 1 is active, false if page 2 is active
     */
    private void updateIndicators(boolean isPage1) {
        indicator1.getStyleClass().remove(isPage1 ? "indicator-inactive" : "indicator-active");
        indicator1.getStyleClass().add(isPage1 ? "indicator-active" : "indicator-inactive");
        
        indicator2.getStyleClass().remove(isPage1 ? "indicator-active" : "indicator-inactive");
        indicator2.getStyleClass().add(isPage1 ? "indicator-inactive" : "indicator-active");
    }
    
    /**
     * Navigate to page 1 with animation
     */
    public void navigateToPage1() {
        if (isAnimating || isPage1Visible) return;
        isAnimating = true;
        
        // Update indicators
        updateIndicators(true);
        
        // Make both pages visible for the transition
        page1.setVisible(true);
        page2.getStyleClass().add("home-page-hiding");
        
        // Create animation
        FadeTransition fadeOutPage2 = new FadeTransition(Duration.millis(300), page2);
        fadeOutPage2.setFromValue(1.0);
        fadeOutPage2.setToValue(0.0);
        
        FadeTransition fadeInPage1 = new FadeTransition(Duration.millis(300), page1);
        fadeInPage1.setFromValue(0.0);
        fadeInPage1.setToValue(1.0);
        
        // Add slide effect
        TranslateTransition slidePage1 = new TranslateTransition(Duration.millis(300), page1);
        slidePage1.setFromY(50);
        slidePage1.setToY(0);
        slidePage1.setInterpolator(Interpolator.EASE_OUT);
        
        // Run animations in parallel
        ParallelTransition transition = new ParallelTransition(fadeOutPage2, fadeInPage1, slidePage1);
        
        transition.setOnFinished(e -> {
            // Update state after animation
            page2.setVisible(false);
            page2.getStyleClass().remove("home-page-hiding");
            isPage1Visible = true;
            isAnimating = false;
        });
        
        transition.play();
    }
    
    /**
     * Navigate to page 2 with animation
     */
    public void navigateToPage2() {
        if (isAnimating || !isPage1Visible) return;
        isAnimating = true;
        
        // Update indicators
        updateIndicators(false);
        
        // Make both pages visible for the transition
        page2.setVisible(true);
        page1.getStyleClass().add("home-page-hiding");
        
        // Ensure the page title starts hidden for animation
        Label titleLabel = (Label) page2.lookup(".page-title");
        if (titleLabel != null) {
            titleLabel.setOpacity(0);
            titleLabel.setTranslateY(20);
        }
        
        // Hide feature rows initially
        page2.lookupAll(".feature-row").forEach(node -> {
            node.setOpacity(0);
            node.setTranslateY(20);
        });
        
        // Create animation
        FadeTransition fadeOutPage1 = new FadeTransition(Duration.millis(300), page1);
        fadeOutPage1.setFromValue(1.0);
        fadeOutPage1.setToValue(0.0);
        
        FadeTransition fadeInPage2 = new FadeTransition(Duration.millis(300), page2);
        fadeInPage2.setFromValue(0.0);
        fadeInPage2.setToValue(1.0);
        
        // Add slide effect
        TranslateTransition slidePage2 = new TranslateTransition(Duration.millis(300), page2);
        slidePage2.setFromY(-50);
        slidePage2.setToY(0);
        slidePage2.setInterpolator(Interpolator.EASE_OUT);
        
        // Run animations in parallel
        ParallelTransition transition = new ParallelTransition(fadeOutPage1, fadeInPage2, slidePage2);
        
        transition.setOnFinished(e -> {
            // Update state after animation
            page1.setVisible(false);
            page1.getStyleClass().remove("home-page-hiding");
            isPage1Visible = false;
            isAnimating = false;
            
            // Add staggered animation for feature rows
            animateFeatureRows();
        });
        
        transition.play();
    }
    
    /**
     * Animate feature rows with a staggered effect
     */
    private void animateFeatureRows() {
        // Animate the title first
        Label titleLabel = (Label) page2.lookup(".page-title");
        if (titleLabel != null) {
            // Set initial state
            titleLabel.setOpacity(0);
            titleLabel.setTranslateY(20);
            
            // Create animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), titleLabel);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            
            TranslateTransition slideUp = new TranslateTransition(Duration.millis(300), titleLabel);
            slideUp.setFromY(20);
            slideUp.setToY(0);
            
            ParallelTransition animation = new ParallelTransition(fadeIn, slideUp);
            animation.play();
        }
        
        // Find all report cards
        page2.lookupAll(".report-card").forEach(node -> {
            // Set initial state
            node.setOpacity(0);
            node.setTranslateY(20);
            
            // Create animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), node);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            
            TranslateTransition slideUp = new TranslateTransition(Duration.millis(300), node);
            slideUp.setFromY(20);
            slideUp.setToY(0);
            
            ParallelTransition animation = new ParallelTransition(fadeIn, slideUp);
            
            // Add delay based on position in grid
            Node parent = node.getParent();
            if (parent instanceof GridPane) {
                Integer columnIndex = GridPane.getColumnIndex(node);
                Integer rowIndex = GridPane.getRowIndex(node);
                if (columnIndex == null) columnIndex = 0;
                if (rowIndex == null) rowIndex = 0;
                
                int delay = (rowIndex * 2 + columnIndex) * 150;
                animation.setDelay(Duration.millis(delay));
            }
            
            // Play animation
            animation.play();
        });
    }
    
    /**
     * Handle button clicks based on button text
     * @param button The button that was clicked
     */
    private void handleButtonClick(JFXButton button) {
        switch (button.getText()) {
            case "SOLUTIONS":
                System.out.println("Solutions clicked");
                // Add functionality as needed
                break;
            case "ABOUT US":
                System.out.println("About Us clicked");
                // Add functionality as needed
                break;
            case "PRICING":
                System.out.println("Pricing clicked");
                // Add functionality as needed
                break;
            case "DEMO CALENDAR":
                // Mostrar la demostración del calendario en una nueva ventana
                showCalendarDemo();
                break;
            default:
                System.out.println("Button clicked: " + button.getText());
                break;
        }
    }
    
    /**
     * Mostrar la demostración del calendario en una ventana nueva
     */
    private void showCalendarDemo() {
        try {
            // Crear un nuevo stage para la demostración del calendario
            Stage demoStage = new Stage();
            
            // Crear nuestro componente de calendario personalizado
            CalendarFXComponent calendarDemo = new CalendarFXComponent();
            
            // Crear el diseño raíz
            BorderPane root = new BorderPane();
            root.setCenter(calendarDemo);
            
            // Crear la escena
            Scene scene = new Scene(root, 1200, 800);
            
            // Configurar el stage
            demoStage.setTitle("Demostración Completa del Calendario");
            demoStage.setScene(scene);
            demoStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Añadir entradas de muestra para la demo
     */
    private void addSampleEntries(Calendar family, Calendar work, Calendar holiday) {
        // Entry for family calendar
        Entry<String> familyBirthday = new Entry<>("Cumpleaños de mamá");
        familyBirthday.setLocation("Casa");
        LocalDate birthdayDate = LocalDate.now().plusDays(2);
        familyBirthday.setInterval(birthdayDate, LocalTime.of(12, 0), birthdayDate, LocalTime.of(14, 0));
        family.addEntry(familyBirthday);
        
        Entry<String> familyDinner = new Entry<>("Cena familiar");
        familyDinner.setLocation("Restaurante El Rincón");
        LocalDate dinnerDate = LocalDate.now().plusDays(7);
        familyDinner.setInterval(dinnerDate, LocalTime.of(20, 0), dinnerDate, LocalTime.of(22, 30));
        family.addEntry(familyDinner);
        
        // Entry for work calendar
        Entry<String> workMeeting = new Entry<>("Reunión de equipo");
        workMeeting.setLocation("Sala de conferencias");
        LocalDate meetingDate = LocalDate.now().plusDays(1);
        workMeeting.setInterval(meetingDate, LocalTime.of(10, 0), meetingDate, LocalTime.of(11, 0));
        work.addEntry(workMeeting);
        
        Entry<String> presentation = new Entry<>("Presentación de proyecto");
        presentation.setLocation("Auditorio");
        LocalDate presDate = LocalDate.now().plusDays(5);
        presentation.setInterval(presDate, LocalTime.of(14, 0), presDate, LocalTime.of(15, 30));
        work.addEntry(presentation);
        
        // Entry for holiday calendar
        Entry<String> holiday1 = new Entry<>("Vacaciones de verano");
        LocalDate holidayStart = LocalDate.now().plusDays(30);
        LocalDate holidayEnd = holidayStart.plusDays(14);
        holiday1.setInterval(holidayStart, holidayEnd);
        holiday.addEntry(holiday1);
    }
    
    /**
     * Inicializar la vista previa del calendario en la página 1
     */
    private void initializeCalendarPreview() {
        try {
            calendarPreview = new CalendarPreview();
            if (page1 != null) {
                // Buscar el HBox dentro de page1
                for (Node node : page1.getChildren()) {
                    if (node instanceof HBox) {
                        HBox hbox = (HBox) node;
                        for (Node child : hbox.getChildren()) {
                            if (child instanceof BorderPane && child.getStyleClass().contains("card-main")) {
                                ((BorderPane) child).setCenter(calendarPreview);
                                return;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 