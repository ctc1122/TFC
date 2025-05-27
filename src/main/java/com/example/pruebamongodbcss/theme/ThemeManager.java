package com.example.pruebamongodbcss.theme;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
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
    private final String MODULE_STYLES_PATH = "/com/example/pruebamongodbcss/theme/module-styles.css";
    
    // Lista de estilos que serán eliminados al aplicar nuestros temas
    private final List<String> OVERRIDE_STYLES = List.of(
        "PanelInicioSesionEstilo.css",
        "app.css",
        "Carrusel.css",
        "chatOscuro.css",
        "estilos.css",
        "clinica-styles.css",
        "informes-styles.css",
        "facturacion-styles.css"
    );
    
    // Propiedad observable para el tema oscuro
    private final BooleanProperty darkThemeProperty = new SimpleBooleanProperty(false);
    
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
            toggle.setSelected(darkThemeProperty.get());
            
            // Establecer listener para cambio de tema
            toggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != darkThemeProperty.get()) {
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
        if (darkThemeProperty.get() != dark) {
            darkThemeProperty.set(dark);
            
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
        return darkThemeProperty.get();
    }
    
    /**
     * Obtiene la propiedad observable del tema oscuro
     * @return Propiedad observable del tema oscuro
     */
    public BooleanProperty darkThemeProperty() {
        return darkThemeProperty;
    }
    
    /**
     * Alternar entre tema claro y oscuro
     */
    public void toggleTheme() {
        setDarkTheme(!darkThemeProperty.get());
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
        String themePath = darkThemeProperty.get() ? DARK_THEME_PATH : LIGHT_THEME_PATH;
        if (!scene.getStylesheets().contains(getClass().getResource(themePath).toExternalForm())) {
            scene.getStylesheets().add(0, getClass().getResource(themePath).toExternalForm());
        }
        
        // Agregar los estilos específicos para botones de la clínica (si no están ya)
        String clinicButtonsPath = getClass().getResource(CLINIC_BUTTONS_PATH).toExternalForm();
        if (!scene.getStylesheets().contains(clinicButtonsPath)) {
            scene.getStylesheets().add(clinicButtonsPath);
        }
        
        // Agregar los estilos de módulos específicos
        String moduleStylesPath = getClass().getResource(MODULE_STYLES_PATH).toExternalForm();
        if (!scene.getStylesheets().contains(moduleStylesPath)) {
            scene.getStylesheets().add(moduleStylesPath);
        }
        
        // Aplicar o quitar la clase dark-theme para estilos específicos de tema
        if (darkThemeProperty.get()) {
            if (!scene.getRoot().getStyleClass().contains("dark-theme")) {
                scene.getRoot().getStyleClass().add("dark-theme");
            }
            
            // Asegurarse de que no tiene la clase light-theme
            scene.getRoot().getStyleClass().remove("light-theme");
        } else {
            // Quitar la clase dark-theme y agregar light-theme
            scene.getRoot().getStyleClass().remove("dark-theme");
            
            // Agregar light-theme si no la tiene
            if (!scene.getRoot().getStyleClass().contains("light-theme")) {
                scene.getRoot().getStyleClass().add("light-theme");
            }
        }
        
        // Aplicar clases específicas a nodos para sobrescribir estilos hardcodeados
        applyThemeClassesToNodes(scene.getRoot());
    }
    
    /**
     * Aplica clases CSS específicas a nodos para sobrescribir estilos hardcodeados
     * @param node Nodo raíz para aplicar las clases
     */
    private void applyThemeClassesToNodes(Node node) {
        // Aplicar clases específicas según el tipo de nodo y su ID
        if (node.getId() != null) {
            String nodeId = node.getId().toLowerCase();
            
            // Identificar contenedores principales de módulos
            if (nodeId.contains("maincontainer") || nodeId.contains("contentcontainer") || 
                nodeId.contains("scrollpane") || nodeId.contains("informes") ||
                nodeId.contains("main") && nodeId.contains("pane")) {
                
                if (!node.getStyleClass().contains("module-main-container")) {
                    node.getStyleClass().add("module-main-container");
                }
            }
            
            // Identificar tarjetas y paneles de contenido
            if (nodeId.contains("card") || nodeId.contains("metric") || 
                nodeId.contains("chart") || nodeId.contains("report")) {
                
                if (!node.getStyleClass().contains("module-card")) {
                    node.getStyleClass().add("module-card");
                }
            }
        }
        
        // Aplicar clases basadas en el estilo inline del nodo
        String style = node.getStyle();
        if (style != null && !style.isEmpty()) {
            // Identificar nodos con fondos hardcodeados problemáticos
            if (style.contains("#f8f9fa") || style.contains("f8f9fa")) {
                if (!node.getStyleClass().contains("hardcoded-bg-fix")) {
                    node.getStyleClass().add("hardcoded-bg-fix");
                }
            }
            
            if (style.contains("white") && style.contains("background")) {
                if (!node.getStyleClass().contains("hardcoded-white-bg")) {
                    node.getStyleClass().add("hardcoded-white-bg");
                }
            }
        }
        
        // Procesar recursivamente todos los nodos hijos
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                applyThemeClassesToNodes(child);
            }
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
        return darkThemeProperty.get() ? DARK_THEME_PATH : LIGHT_THEME_PATH;
    }
    
    /**
     * Fuerza la actualización del tema en una escena específica
     * Útil cuando se cargan nuevos contenidos dinámicamente
     * @param scene Escena a actualizar
     */
    public void forceUpdateScene(Scene scene) {
        if (registeredScenes.contains(scene)) {
            applyThemeToScene(scene);
        }
    }
    
    /**
     * Fuerza la actualización del tema en todas las escenas registradas
     * Útil para aplicar cambios después de cargar contenido dinámico
     */
    public void forceUpdateAllScenes() {
        updateAllScenes();
    }
} 