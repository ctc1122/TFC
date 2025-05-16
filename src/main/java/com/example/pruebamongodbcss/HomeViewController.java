package com.example.pruebamongodbcss;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.example.pruebamongodbcss.calendar.CalendarFXComponent;
import com.example.pruebamongodbcss.calendar.CalendarPreview;
import com.example.pruebamongodbcss.theme.ThemeManager;
import com.jfoenix.controls.JFXButton;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

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
    private JFXButton btnBack;
   
    private boolean isPage1Visible = true;
    private boolean isAnimating = false;
    private ScrollPane scrollWrapper;
    private boolean isScrolling = false;
    private double lastScrollY = 0;
    private static final double SCROLL_THRESHOLD = 30.0;
    
    // Componentes del calendario
    private CalendarFXComponent calendarComponent;
    private CalendarPreview calendarPreview;
    
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
        
        // Apply initial styles
        applyStyles();
        
        // Set button action handlers
        setButtonActions();
        
        // Configure initial page state
        configurePageTransitions();
        
        // Inicializar la vista previa del calendario en la página 1
        initializeCalendarPreview();
        
        // Inicializar el calendario completo en la página 2
        initializeCalendar();
    }
    
    /**
     * Initialize the calendar component and add it to page2
     */
    private void initializeCalendar() {
        try {
            // Crear nuestro componente de calendario personalizado basado en CalendarFX
            calendarComponent = new CalendarFXComponent();
            
            // Replace content in page2 with the calendar
            if (page2 != null) {
                // Find a suitable container in page2 to place the calendar
                Node container = page2.lookup(".calendar-container");
                if (container != null && container instanceof BorderPane) {
                    BorderPane calendarContainer = (BorderPane) container;
                    calendarContainer.setCenter(calendarComponent);
                } else {
                    // If no specific container is found, just add it to page2
                    page2.getChildren().clear();
                    page2.getChildren().add(calendarComponent);
                    VBox.setVgrow(calendarComponent, javafx.scene.layout.Priority.ALWAYS);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        
        // Add back button handler
        btnBack.setOnAction(e -> navigateToPage1());
        
        // Add hover effects to all buttons
        homeContainer.lookupAll(".btn-card, .btn-footer, .btn-back").forEach(node -> {
            if (node instanceof JFXButton) {
                JFXButton button = (JFXButton) node;
                
                // Add hover effect
                button.setOnMouseEntered(e -> button.setStyle("-fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
                button.setOnMouseExited(e -> button.setStyle("-fx-scale-x: 1.0; -fx-scale-y: 1.0;"));
                
                // Add click handler for non-navigation buttons
                if (button != btnLearnMore && button != btnBack) {
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
        
        // Find all feature rows
        page2.lookupAll(".feature-row").forEach(node -> {
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
            
            // Add delay based on index
            int index = 0;
            Node parent = node.getParent();
            if (parent instanceof VBox) {
                index = ((VBox) parent).getChildren().indexOf(node);
            }
            // Increment index to account for the title animation
            animation.setDelay(Duration.millis(150 * (index + 1)));
            
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
            // Crear el componente de vista previa
            calendarPreview = new CalendarPreview();
            
            // Añadir a la página 1
            if (page1 != null) {
                // Buscar el contenedor del calendario en la página 1
                BorderPane calendarContainer = null;
                
                // Buscar en el GridPane
                if (page1Grid != null) {
                    for (Node node : page1Grid.getChildren()) {
                        if (node instanceof BorderPane && 
                            node.getStyleClass().contains("card-main")) {
                            calendarContainer = (BorderPane) node;
                            break;
                        }
                    }
                }
                
                // Si encontramos el contenedor, añadir el calendario
                if (calendarContainer != null) {
                    // Buscar el contenedor específico dentro del BorderPane
                    Node innerContainer = null;
                    for (Node node : calendarContainer.getChildren()) {
                        if (node.getId() != null && node.getId().equals("calendarContainer")) {
                            innerContainer = node;
                            break;
                        }
                    }
                    
                    if (innerContainer instanceof BorderPane) {
                        ((BorderPane) innerContainer).setCenter(calendarPreview);
                    } else {
                        // Si no encuentra el contenedor específico, usar el centro del BorderPane principal
                        calendarContainer.setCenter(calendarPreview);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 