package com.example.pruebamongodbcss;

import java.net.URL;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.theme.ThemeManager;
import com.jfoenix.controls.JFXButton;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Controller for the home-view.fxml which displays the modern energy-themed home page
 */
public class HomeViewController implements Initializable {

    @FXML
    private BorderPane homeContainer;
    
    @FXML
    private ScrollPane scrollPane;
    
    @FXML
    private VBox contentContainer;
    
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
    private Rectangle indicator1Copy;
    
    @FXML
    private Rectangle indicator2Copy;
    
    @FXML
    private HBox homeHeader;
    
    @FXML
    private Label homeTitle;
    
    @FXML
    private HBox homeFooter;
    
    @FXML
    private JFXButton btnSolutions;
    
    @FXML
    private JFXButton btnAboutUs;
    
    @FXML
    private JFXButton btnPricing;
    
    @FXML
    private HBox pageIndicatorContainer;
    
    private boolean isScrolling = false;
    private boolean isPage1Visible = true;
    private boolean isAnimating = false;
    private double scrollThreshold = 0.3; // Porcentaje de la página para activar cambio
    private double lastScrollPosition = 0;
    private ChangeListener<Number> scrollListener;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Register the scene with the ThemeManager when it's available
        homeContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                ThemeManager.getInstance().registerScene(newScene);
            }
        });
        
        // Apply initial styles
        applyStyles();
        
        // Set button action handlers
        setButtonActions();
        
        // Configure scroll behavior
        configureScrollAnimation();
    }
    
    /**
     * Apply styles based on current theme
     */
    private void applyStyles() {
        // Add custom stylesheet for home view
        String homeStylesheet = getClass().getResource("/com/example/pruebamongodbcss/theme/home-styles.css").toExternalForm();
        
        // Apply stylesheet if not already present
        if (homeContainer.getScene() != null && 
            !homeContainer.getScene().getStylesheets().contains(homeStylesheet)) {
            homeContainer.getScene().getStylesheets().add(homeStylesheet);
        }
    }
    
    /**
     * Set action handlers for all buttons
     */
    private void setButtonActions() {
        // Find all buttons using CSS selectors and add handlers
        homeContainer.lookupAll(".btn-card, .btn-footer").forEach(node -> {
            if (node instanceof JFXButton) {
                JFXButton button = (JFXButton) node;
                
                // Add hover effect
                button.setOnMouseEntered(e -> button.setStyle("-fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
                button.setOnMouseExited(e -> button.setStyle("-fx-scale-x: 1.0; -fx-scale-y: 1.0;"));
                
                // Add click handler based on button type
                button.setOnAction(e -> handleButtonClick(button));
            }
        });
    }
    
    /**
     * Configure scroll animation and snap behavior
     */
    private void configureScrollAnimation() {
        // Asegurar que el contenedor no tenga padding y el scrollPane tome todo el espacio
        pageIndicatorContainer.toFront();
        contentContainer.setStyle("-fx-padding: 0; -fx-spacing: 0;");
        
        // Aplicar estilo específico al ScrollPane
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        
        // Establecer posición inicial
        scrollPane.setVvalue(0);
        page1.setOpacity(1.0);
        page2.setOpacity(0.0);
        
        // Configurar padding para que la página 2 esté siempre al principio cuando está activa
        page1.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            if (page2 != null) {
                // Ajustar el padding-top de page2 para que siempre aparezca arriba del todo cuando está activa
                page2.setPadding(new javafx.geometry.Insets(0, 20, 20, 20));
            }
        });
        
        // Haciendo los indicadores clicables para navegar entre páginas
        indicator1.setOnMouseClicked(e -> snapToPage(true));
        indicator2.setOnMouseClicked(e -> snapToPage(false));
        
        // Configure the scroll behavior
        scrollListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (isAnimating) return; // Skip if already animating
            
            double vvalue = newValue.doubleValue();
            double delta = Math.abs(vvalue - lastScrollPosition);
            
            // Crossfade between pages - más abrupto para que se vea más claramente
            if (vvalue < 0.4) {
                // Primer página completamente visible
                page1.setOpacity(1.0);
                page2.setOpacity(0.0);
                updateIndicators(true);
                isPage1Visible = true;
            } else if (vvalue >= 0.4 && vvalue < 0.6) {
                // Zona de transición
                double progress = (vvalue - 0.4) / 0.2; // 0 a 1 en el rango 0.4-0.6
                page1.setOpacity(1.0 - progress);
                page2.setOpacity(progress);
            } else {
                // Segunda página completamente visible
                page1.setOpacity(0.0);
                page2.setOpacity(1.0);
                updateIndicators(false);
                isPage1Visible = false;
                
                // Asegurar que la página 2 esté en la parte superior cuando se muestra
                if (vvalue > 0.9) {
                    page2.toFront();
                }
            }
            
            // Snap más agresivo
            if (delta < 0.01 && isScrolling) {
                isScrolling = false;
                
                // Si está en la zona de transición, ir a la página más cercana
                if (vvalue > 0.3 && vvalue < 0.7) {
                    snapToPage(vvalue < 0.5);
                }
            } else if (delta >= 0.01) {
                isScrolling = true;
            }
            
            lastScrollPosition = vvalue;
        };
        
        // Añadir el listener de scroll
        scrollPane.vvalueProperty().addListener(scrollListener);
        
        // Mouse wheel event para un scrolling más suave
        scrollPane.setOnScroll(e -> {
            if (isAnimating) return;
            
            // Determinar dirección y aplicar scroll más suave
            if (e.getDeltaY() < 0) {
                // Scrolling hacia abajo - ir a página 2
                if (isPage1Visible) {
                    snapToPage(false);
                }
            } else {
                // Scrolling hacia arriba - ir a página 1
                if (!isPage1Visible) {
                    snapToPage(true);
                }
            }
        });
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
     * Snap to a specific page with animation
     * @param toPage1 true to snap to page 1, false to snap to page 2
     */
    private void snapToPage(boolean toPage1) {
        isAnimating = true;
        
        // Establecer la opacidad inmediatamente para una respuesta más rápida
        page1.setOpacity(toPage1 ? 1.0 : 0.0);
        page2.setOpacity(toPage1 ? 0.0 : 1.0);
        
        // Actualizar indicadores inmediatamente
        updateIndicators(toPage1);
        
        // Si vamos a la página 2, asegurar que está en la parte superior
        if (!toPage1) {
            // Asegurar que la página 2 esté en la parte superior
            scrollPane.setVvalue(0.6); // Iniciar un poco por encima para que la animación sea visible
            
            // Mover la página 2 al frente
            page2.toFront();
            
            // Asegurar que el padding sea correcto
            page2.setPadding(new javafx.geometry.Insets(0, 20, 20, 20));
        }
        
        // Crear animación más rápida para el scroll
        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(scrollPane.vvalueProperty(), toPage1 ? 0.0 : 1.0, Interpolator.EASE_OUT);
        KeyFrame kf = new KeyFrame(Duration.millis(300), kv); // Más rápido: 300ms
        timeline.getKeyFrames().add(kf);
        
        // Reproducir animación
        timeline.play();
        
        // Actualizar estado cuando finaliza la animación
        timeline.setOnFinished(e -> {
            isAnimating = false;
            isPage1Visible = toPage1;
            
            // Si vamos a la página 2, asegurar que esté completamente en la parte superior
            if (!toPage1) {
                // Forzar posición correcta después de terminar la animación
                scrollPane.setVvalue(1.0);
                page2.toFront();
                
                // Aplicar un pequeño delay y volver a posicionar para asegurar que se ajuste correctamente
                javafx.application.Platform.runLater(() -> {
                    // Forzar reposicionamiento
                    scrollPane.setVvalue(1.0);
                });
            }
        });
    }
    
    /**
     * Handle button clicks based on button text
     * @param button The button that was clicked
     */
    private void handleButtonClick(JFXButton button) {
        switch (button.getText()) {
            case "LEARN MORE":
                // Scroll to page 2 when Learn More is clicked
                snapToPage(false);
                break;
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
            default:
                System.out.println("Button clicked: " + button.getText());
                break;
        }
    }
} 