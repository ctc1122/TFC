package com.example.pruebamongodbcss.theme;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Utilidad para facilitar la aplicación de temas en cualquier controlador o ventana
 */
public class ThemeUtil {
    
    /**
     * Método estático para registrar una escena en el ThemeManager
     * Se puede llamar desde cualquier controlador para asegurar que
     * el tema se aplique correctamente.
     * 
     * @param scene Escena a registrar
     */
    public static void applyTheme(Scene scene) {
        if (scene != null) {
            ThemeManager.getInstance().registerScene(scene);
        }
    }
    
    /**
     * Asegura que el tema se aplique a la escena una vez que esté disponible
     * Útil cuando la escena aún no está inicializada
     * 
     * @param parent Nodo padre que eventualmente tendrá una escena
     */
    public static void applyThemeWhenAvailable(Parent parent) {
        if (parent.getScene() != null) {
            ThemeManager.getInstance().registerScene(parent.getScene());
        } else {
            // Esperar a que la escena esté disponible
            Platform.runLater(() -> {
                if (parent.getScene() != null) {
                    ThemeManager.getInstance().registerScene(parent.getScene());
                }
            });
        }
    }
    
    /**
     * Aplica el tema actual al cargar una nueva vista FXML
     * 
     * @param loader FXMLLoader usado para cargar la vista
     * @return El nodo raíz cargado con el tema aplicado
     * @throws java.io.IOException Si ocurre un error al cargar el FXML
     */
    public static Parent loadWithTheme(FXMLLoader loader) throws java.io.IOException {
        Parent root = loader.load();
        
        // Asegurarse de que todos los nodos hijos tengan las clases CSS necesarias
        addStylingClasses(root);
        
        // Buscar y agregar estilo a todas las escenas dentro del FXML (por ejemplo, en pestañas)
        findAndRegisterAllScenes(root);
        
        // Aplicar el tema cuando la escena esté disponible
        applyThemeWhenAvailable(root);
        
        return root;
    }
    
    /**
     * Encuentra recursivamente todas las escenas dentro de un nodo y sus hijos
     * y las registra en el ThemeManager.
     * 
     * @param node Nodo raíz para buscar escenas
     */
    private static void findAndRegisterAllScenes(Node node) {
        // Registrar la escena si está disponible
        if (node.getScene() != null) {
            ThemeManager.getInstance().registerScene(node.getScene());
        }
        
        // Buscar en TabPanes para registrar contenido de pestañas
        if (node instanceof TabPane) {
            TabPane tabPane = (TabPane) node;
            for (Tab tab : tabPane.getTabs()) {
                if (tab.getContent() != null) {
                    findAndRegisterAllScenes(tab.getContent());
                }
            }
        }
        
        // Procesar recursivamente todos los nodos hijos
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                findAndRegisterAllScenes(child);
            }
        }
    }
    
    /**
     * Agrega clases de estilo apropiadas a todos los nodos según su tipo.
     * 
     * @param node Nodo al que agregar las clases de estilo
     */
    private static void addStylingClasses(Node node) {
        // Aplicar clase específica para paneles principales
        if (node.getId() != null && node.getId().endsWith("mainPane") && node instanceof Region) {
            node.getStyleClass().add("clinic-main-panel");
        }
        
        // Aplicar estilos a headers de tablas
        if (node.getStyleClass().contains("column-header-background")) {
            node.getStyleClass().add("clinic-header");
        }
        
        // Procesar recursivamente todos los nodos hijos
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                addStylingClasses(child);
            }
        }
    }
    
    /**
     * Registra todas las escenas en todas las ventanas activas
     * Útil para aplicar el tema a toda la aplicación de una vez
     */
    public static void applyThemeToAllOpenWindows() {
        for (Window window : Stage.getWindows()) {
            if (window instanceof Stage) {
                Stage stage = (Stage) window;
                if (stage.getScene() != null) {
                    ThemeManager.getInstance().registerScene(stage.getScene());
                }
            }
        }
    }
    
    /**
     * Cambia entre tema claro y oscuro
     */
    public static void toggleTheme() {
        ThemeManager.getInstance().toggleTheme();
    }
    
    /**
     * Establece un tema específico
     * 
     * @param dark true para tema oscuro, false para claro
     */
    public static void setDarkTheme(boolean dark) {
        ThemeManager.getInstance().setDarkTheme(dark);
    }
    
    /**
     * Verifica si el tema actual es oscuro
     * 
     * @return true si el tema es oscuro, false si es claro
     */
    public static boolean isDarkTheme() {
        return ThemeManager.getInstance().isDarkTheme();
    }

    /**
     * Crea una nueva escena con el tema aplicado
     * 
     * @param root Nodo raíz para la nueva escena
     * @param width Ancho de la escena
     * @param height Alto de la escena
     * @return Nueva escena con el tema actual aplicado
     */
    public static Scene createScene(Parent root, double width, double height) {
        // Primero agregar clases de estilo apropiadas a los nodos
        addStylingClasses(root);
        
        // Crear la escena
        Scene scene = new Scene(root, width, height);
        
        // Registrarla en el ThemeManager
        ThemeManager.getInstance().registerScene(scene);
        
        return scene;
    }
} 