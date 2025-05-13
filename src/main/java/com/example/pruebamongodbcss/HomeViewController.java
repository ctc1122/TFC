package com.example.pruebamongodbcss;

import java.net.URL;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.theme.ThemeManager;
import com.jfoenix.controls.JFXButton;

import javafx.animation.FadeTransition;
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
        // Calculate page height
        double pageHeight = page1.getPrefHeight() > 0 ? page1.getPrefHeight() : 520;
        
        // Set the initial scroll position
        scrollPane.setVvalue(0);
        
        // Initialize page states
        page1.setOpacity(1.0);
        page2.setOpacity(0.0);
        
        // Configure the scroll behavior
        scrollListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (isAnimating) return; // Skip if already animating
            
            double vvalue = newValue.doubleValue();
            double delta = Math.abs(vvalue - lastScrollPosition);
            
            // Crossfade between pages based on scroll position
            if (vvalue < 0.5) {
                // Fading from page 1 to page 2
                double opacity = 1.0 - (vvalue * 2);
                page1.setOpacity(Math.max(0, opacity));
                page2.setOpacity(Math.min(1, 1 - opacity));
                
                // Update indicators
                updateIndicators(true);
                isPage1Visible = true;
            } else {
                // Page 2 fully visible
                page1.setOpacity(0.0);
                page2.setOpacity(1.0);
                
                // Update indicators
                updateIndicators(false);
                isPage1Visible = false;
            }
            
            // Handle scroll release and snap effect
            if (delta < 0.01 && isScrolling) {
                isScrolling = false;
                
                // If released near transition point, snap to closest page
                if (vvalue > scrollThreshold && vvalue < (1.0 - scrollThreshold)) {
                    snapToPage(vvalue > 0.5);
                }
            } else if (delta >= 0.01) {
                isScrolling = true;
            }
            
            lastScrollPosition = vvalue;
        };
        
        // Add the scroll listener
        scrollPane.vvalueProperty().addListener(scrollListener);
        
        // Mouse wheel event for smoother scrolling
        scrollPane.setOnScroll(e -> {
            if (isAnimating) return;
            
            // Get current scroll position
            double vvalue = scrollPane.getVvalue();
            
            // Determine direction and apply smoother scroll
            if (e.getDeltaY() < 0) {
                // Scrolling down - move to page 2
                if (isPage1Visible) {
                    snapToPage(false);
                }
            } else {
                // Scrolling up - move to page 1
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
        
        indicator1Copy.getStyleClass().remove(isPage1 ? "indicator-inactive" : "indicator-active");
        indicator1Copy.getStyleClass().add(isPage1 ? "indicator-active" : "indicator-inactive");
        
        indicator2Copy.getStyleClass().remove(isPage1 ? "indicator-active" : "indicator-inactive");
        indicator2Copy.getStyleClass().add(isPage1 ? "indicator-inactive" : "indicator-active");
    }
    
    /**
     * Snap to a specific page with animation
     * @param toPage1 true to snap to page 1, false to snap to page 2
     */
    private void snapToPage(boolean toPage1) {
        isAnimating = true;
        
        // Create smooth animation to the target page
        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(scrollPane.vvalueProperty(), toPage1 ? 0.0 : 1.0, Interpolator.EASE_BOTH);
        KeyFrame kf = new KeyFrame(Duration.millis(500), kv);
        timeline.getKeyFrames().add(kf);
        
        // Add fade transitions for visual effect
        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), toPage1 ? page2 : page1);
        fadeOut.setFromValue(toPage1 ? 1.0 : 1.0);
        fadeOut.setToValue(0.0);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), toPage1 ? page1 : page2);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        
        // Update indicators immediately
        updateIndicators(toPage1);
        
        // Play animations
        timeline.play();
        fadeOut.play();
        fadeIn.play();
        
        // Update state when animation completes
        timeline.setOnFinished(e -> {
            isAnimating = false;
            isPage1Visible = toPage1;
            
            // Set final opacities
            page1.setOpacity(toPage1 ? 1.0 : 0.0);
            page2.setOpacity(toPage1 ? 0.0 : 1.0);
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