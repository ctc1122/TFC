package com.example.pruebamongodbcss.theme;

import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private ImageView lightIcon;
    private ImageView darkIcon;
    
    // Rutas de las imágenes por defecto
    private static final String DEFAULT_LIGHT_ICON = "/Iconos/iconClaro.png";
    private static final String DEFAULT_DARK_ICON = "/Iconos/iconDark.png";
    
    private String lightIconPath = DEFAULT_LIGHT_ICON;
    private String darkIconPath = DEFAULT_DARK_ICON;
    
    public ThemeToggleSwitch() {
        initialize();
        setupListeners();
        
        // Registrarse en el ThemeManager
        ThemeManager.getInstance().registerToggle(this);
    }
    
    private void initialize() {
        // Crear contenedor
        container = new Pane();
        container.setPrefSize(60, 30);
        
        // Crear fondo del switch
        background = new Rectangle(60, 30);
        background.setArcWidth(30);
        background.setArcHeight(30);
        background.setFill(Color.LIGHTGRAY);
        
        // Crear "thumb" (círculo que se mueve)
        thumb = new Circle(15);
        thumb.setCenterX(15);
        thumb.setCenterY(15);
        thumb.setFill(Color.rgb(255, 255, 255, 0.75)); // Blanco semi-transparente
        thumb.setStroke(Color.LIGHTGRAY);
        thumb.setStrokeWidth(1);
        
        // Crear iconos de imágenes para indicar día/noche
        try {
            lightIcon = new ImageView(new Image(getClass().getResourceAsStream(lightIconPath)));
            lightIcon.setFitWidth(20);
            lightIcon.setFitHeight(20);
            lightIcon.setLayoutX(5);
            lightIcon.setLayoutY(5);
            lightIcon.setPreserveRatio(true);
            lightIcon.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 0, 0);");
            
            darkIcon = new ImageView(new Image(getClass().getResourceAsStream(darkIconPath)));
            darkIcon.setFitWidth(20);
            darkIcon.setFitHeight(20);
            darkIcon.setLayoutX(37);
            darkIcon.setLayoutY(5);
            darkIcon.setPreserveRatio(true);
            darkIcon.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 0, 0);");
            darkIcon.setOpacity(0.0); // Inicialmente invisible
            
            // Añadir elementos en el orden correcto
            container.getChildren().clear();
            container.getChildren().add(background);   // Fondo en la capa inferior
            container.getChildren().add(thumb);        // Thumb en la capa media
            container.getChildren().add(lightIcon);    // Iconos en la capa superior
            container.getChildren().add(darkIcon);     // Iconos en la capa superior
        } catch (Exception e) {
            // Si hay error cargando las imágenes, volver al comportamiento original
            System.err.println("Error loading theme icons: " + e.getMessage());
            
            // Crear círculos como fallback para los iconos
            Circle sunCircle = new Circle(8);
            sunCircle.setCenterX(5);
            sunCircle.setCenterY(15);
            sunCircle.setFill(Color.YELLOW);
            sunCircle.setOpacity(0.8);
            
            Circle moonCircle = new Circle(8);
            moonCircle.setCenterX(55);
            moonCircle.setCenterY(15);
            moonCircle.setFill(Color.DEEPSKYBLUE);
            moonCircle.setOpacity(0.0);
            
            // Usar los círculos en lugar de las imágenes
            lightIcon = new ImageView();  // Placeholder vacío
            darkIcon = new ImageView();   // Placeholder vacío
            
            // Añadir elementos en el orden correcto
            container.getChildren().clear();
            container.getChildren().add(background);   // Fondo en la capa inferior
            container.getChildren().add(thumb);        // Thumb en la capa media
            container.getChildren().add(sunCircle);    // Círculos en la capa superior
            container.getChildren().add(moonCircle);   // Círculos en la capa superior
        }
        
        // Agregar estilos CSS
        getStyleClass().add("theme-toggle-switch");
        
        // Configurar estilos del botón
        setBackground(null);
        setPrefSize(60, 30);
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
            transition.setToX(30);
            background.setFill(Color.rgb(40, 40, 65));
            lightIcon.setOpacity(0.0);
            darkIcon.setOpacity(1.0);
            
            // Asegurar que los iconos estén en la capa superior
            darkIcon.toFront();
        } else {
            // Cambiar a modo claro
            transition.setToX(0);
            background.setFill(Color.rgb(173, 216, 230)); // Celeste más suave
            lightIcon.setOpacity(1.0);
            darkIcon.setOpacity(0.0);
            
            // Asegurar que los iconos estén en la capa superior
            lightIcon.toFront();
        }
        
        transition.play();
    }
    
    /**
     * Establece la imagen para el modo claro
     * @param path Ruta de la imagen en los recursos
     */
    public void setLightModeIcon(String path) {
        this.lightIconPath = path;
        try {
            Image image = new Image(getClass().getResourceAsStream(path));
            lightIcon.setImage(image);
        } catch (Exception e) {
            System.err.println("Error loading light mode icon: " + e.getMessage());
        }
    }
    
    /**
     * Establece la imagen para el modo oscuro
     * @param path Ruta de la imagen en los recursos
     */
    public void setDarkModeIcon(String path) {
        this.darkIconPath = path;
        try {
            Image image = new Image(getClass().getResourceAsStream(path));
            darkIcon.setImage(image);
        } catch (Exception e) {
            System.err.println("Error loading dark mode icon: " + e.getMessage());
        }
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