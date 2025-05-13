package com.example.pruebamongodbcss;

import java.net.URL;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.theme.ThemeManager;
import com.jfoenix.controls.JFXButton;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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
        
        // Configure initial page state
        configurePageTransitions();
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
            int index = ((VBox)page2).getChildren().indexOf(node.getParent());
            animation.setDelay(Duration.millis(150 * index));
            
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
            default:
                System.out.println("Button clicked: " + button.getText());
                break;
        }
    }
} 