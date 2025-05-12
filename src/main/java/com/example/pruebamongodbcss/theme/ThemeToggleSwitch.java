package com.example.pruebamongodbcss.theme;

import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Control personalizado para un Toggle Switch de tema claro/oscuro
 */
public class ThemeToggleSwitch extends ToggleButton {
    
    private final BooleanProperty darkMode = new SimpleBooleanProperty(false);
    private final Duration ANIMATION_DURATION = Duration.millis(200);
    
    private Rectangle background;
    private Circle thumb;
    private Pane container;
    
    // Iconos para representar sol/luna
    private Circle sunIcon;
    private Circle moonIcon;
    
    public ThemeToggleSwitch() {
        initialize();
        setupListeners();
        
        // Registrarse en el ThemeManager
        ThemeManager.getInstance().registerToggle(this);
    }
    
    private void initialize() {
        // Crear contenedor
        container = new Pane();
        container.setPrefSize(50, 24);
        
        // Crear fondo del switch
        background = new Rectangle(50, 24);
        background.setArcWidth(24);
        background.setArcHeight(24);
        background.setFill(Color.LIGHTGRAY);
        
        // Crear "thumb" (círculo que se mueve)
        thumb = new Circle(12);
        thumb.setCenterX(12);
        thumb.setCenterY(12);
        thumb.setFill(Color.WHITE);
        thumb.setStroke(Color.LIGHTGRAY);
        thumb.setStrokeWidth(1);
        
        // Crear iconos para indicar día/noche
        sunIcon = new Circle(8);
        sunIcon.setCenterX(12);
        sunIcon.setCenterY(12);
        sunIcon.setFill(Color.YELLOW);
        sunIcon.setOpacity(0.8);
        
        moonIcon = new Circle(8);
        moonIcon.setCenterX(38);
        moonIcon.setCenterY(12);
        moonIcon.setFill(Color.DEEPSKYBLUE);
        moonIcon.setOpacity(0.0); // Inicialmente invisible
        
        // Añadir elementos al contenedor
        container.getChildren().addAll(background, sunIcon, moonIcon, thumb);
        
        // Agregar estilos CSS
        getStyleClass().add("theme-toggle-switch");
        
        // Configurar estilos del botón
        setBackground(null);
        setPrefSize(50, 24);
        setGraphic(container);
    }
    
    private void setupListeners() {
        // Vincular propiedad selected con darkMode
        darkMode.bindBidirectional(selectedProperty());
        
        // Agregar listener para cambio de tema
        selectedProperty().addListener((obs, oldVal, newVal) -> {
            updateVisuals(newVal);
        });
        
        // Configuración inicial
        updateVisuals(isSelected());
    }
    
    private void updateVisuals(boolean isDark) {
        TranslateTransition transition = new TranslateTransition(ANIMATION_DURATION, thumb);
        
        if (isDark) {
            // Cambiar a modo oscuro
            transition.setToX(26);
            background.setFill(Color.rgb(30, 30, 40));
            sunIcon.setOpacity(0.0);
            moonIcon.setOpacity(0.8);
        } else {
            // Cambiar a modo claro
            transition.setToX(0);
            background.setFill(Color.LIGHTBLUE);
            sunIcon.setOpacity(0.8);
            moonIcon.setOpacity(0.0);
        }
        
        transition.play();
    }
    
    public BooleanProperty darkModeProperty() {
        return darkMode;
    }
    
    public boolean isDarkMode() {
        return darkMode.get();
    }
    
    public void setDarkMode(boolean dark) {
        darkMode.set(dark);
    }
} 