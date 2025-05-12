package com.example.pruebamongodbcss.theme;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;

/**
 * Clase que gestiona el tema de la aplicación (claro u oscuro)
 * Implementa el patrón Singleton para garantizar una única instancia
 */
public class ThemeManager {
    private static ThemeManager instance;
    private final String LIGHT_THEME_PATH = "/com/example/pruebamongodbcss/theme/light-theme.css";
    private final String DARK_THEME_PATH = "/com/example/pruebamongodbcss/theme/dark-theme.css";
    private final String CLINIC_BUTTONS_PATH = "/com/example/pruebamongodbcss/theme/clinic-buttons.css";
    
    // Lista de estilos que serán eliminados al aplicar nuestros temas
    private final List<String> OVERRIDE_STYLES = List.of(
        "PanelInicioSesionEstilo.css",
        "app.css",
        "Carrusel.css",
        "chatOscuro.css",
        "estilos.css"
    );
    
    private boolean isDarkTheme = false;
    private List<Scene> registeredScenes = new ArrayList<>();
    private List<ToggleButton> themeToggles = new ArrayList<>();
    
    // Constructor privado para Singleton
    private ThemeManager() {}
    
    /**
     * Obtiene la instancia única de ThemeManager
     * @return Instancia de ThemeManager
     */
    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    
    /**
     * Registra una escena para aplicar el tema
     * @param scene Escena a registrar
     */
    public void registerScene(Scene scene) {
        if (!registeredScenes.contains(scene)) {
            registeredScenes.add(scene);
            // Aplicar el tema actual a la nueva escena
            applyThemeToScene(scene);
        }
    }
    
    /**
     * Registra un ToggleButton para mantener sincronizado el estado del tema
     * @param toggle ToggleButton a registrar
     */
    public void registerToggle(ToggleButton toggle) {
        if (!themeToggles.contains(toggle)) {
            themeToggles.add(toggle);
            toggle.setSelected(isDarkTheme);
            
            // Establecer listener para cambio de tema
            toggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != isDarkTheme) {
                    setDarkTheme(newVal);
                }
            });
        }
    }
    
    /**
     * Cambia el tema a oscuro o claro
     * @param dark true para tema oscuro, false para tema claro
     */
    public void setDarkTheme(boolean dark) {
        if (isDarkTheme != dark) {
            isDarkTheme = dark;
            
            // Actualizar todas las escenas registradas
            updateAllScenes();
            
            // Sincronizar todos los toggles registrados
            for (ToggleButton toggle : themeToggles) {
                if (toggle.isSelected() != dark) {
                    toggle.setSelected(dark);
                }
            }
        }
    }
    
    /**
     * Obtiene si el tema actual es oscuro
     * @return true si el tema es oscuro, false si es claro
     */
    public boolean isDarkTheme() {
        return isDarkTheme;
    }
    
    /**
     * Alternar entre tema claro y oscuro
     */
    public void toggleTheme() {
        setDarkTheme(!isDarkTheme);
    }
    
    /**
     * Actualiza el tema en todas las escenas registradas
     */
    private void updateAllScenes() {
        for (Scene scene : registeredScenes) {
            applyThemeToScene(scene);
        }
    }
    
    /**
     * Aplica el tema a una escena específica
     * @param scene Escena a la que aplicar el tema
     */
    private void applyThemeToScene(Scene scene) {
        // Eliminar cualquier estilo específico para asegurar consistencia
        removeOverridingStyles(scene);
        
        // Remover específicamente los estilos de temas anteriores
        scene.getStylesheets().removeIf(style -> 
            style.contains("light-theme.css") || style.contains("dark-theme.css"));
        
        // Aplicar el tema actual siempre como primer stylesheet para prioridad máxima
        String themePath = isDarkTheme ? DARK_THEME_PATH : LIGHT_THEME_PATH;
        if (!scene.getStylesheets().contains(getClass().getResource(themePath).toExternalForm())) {
            scene.getStylesheets().add(0, getClass().getResource(themePath).toExternalForm());
        }
        
        // Agregar los estilos específicos para botones de la clínica (si no están ya)
        String clinicButtonsPath = getClass().getResource(CLINIC_BUTTONS_PATH).toExternalForm();
        if (!scene.getStylesheets().contains(clinicButtonsPath)) {
            scene.getStylesheets().add(clinicButtonsPath);
        }
        
        // Aplicar o quitar la clase dark-theme para estilos específicos de tema
        if (isDarkTheme) {
            if (!scene.getRoot().getStyleClass().contains("dark-theme")) {
                scene.getRoot().getStyleClass().add("dark-theme");
            }
        } else {
            scene.getRoot().getStyleClass().remove("dark-theme");
        }
    }
    
    /**
     * Elimina estilos que podrían interferir con nuestros temas
     * @param scene Escena de la que eliminar estilos conflictivos
     */
    private void removeOverridingStyles(Scene scene) {
        scene.getStylesheets().removeIf(stylesheet -> 
            OVERRIDE_STYLES.stream().anyMatch(stylesheet::contains));
    }
    
    /**
     * Obtiene la ruta del CSS del tema actual
     * @return Ruta del CSS del tema actual
     */
    public String getCurrentThemePath() {
        return isDarkTheme ? DARK_THEME_PATH : LIGHT_THEME_PATH;
    }
} 