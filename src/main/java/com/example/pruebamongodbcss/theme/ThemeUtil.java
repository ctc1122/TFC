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
 * Utilidad para ayudar con la aplicación de temas en la aplicación
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
        // EXCLUSIÓN TOTAL DEL CALENDARIO: No registrar escenas del calendario nativo
        if (isCalendarComponent(node)) {
            System.out.println("🚫 ThemeUtil: Excluyendo componente de calendario del registro de escenas");
            return; // Salir inmediatamente sin procesar este nodo ni sus hijos
        }
        
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
        
        // Procesar recursivamente todos los nodos hijos (solo si no es componente de calendario)
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
        // EXCLUSIÓN TOTAL DEL CALENDARIO: No agregar clases de estilo al calendario nativo
        if (isCalendarComponent(node)) {
            System.out.println("🚫 ThemeUtil: Excluyendo componente de calendario de la adición de clases de estilo");
            return; // Salir inmediatamente sin procesar este nodo ni sus hijos
        }
        
        // Aplicar clase específica para paneles principales
        if (node.getId() != null && node.getId().endsWith("mainPane") && node instanceof Region) {
            node.getStyleClass().add("clinic-main-panel");
        }
        
        // Aplicar estilos a headers de tablas
        if (node.getStyleClass().contains("column-header-background")) {
            node.getStyleClass().add("clinic-header");
        }
        
        // Procesar recursivamente todos los nodos hijos (solo si no es componente de calendario)
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
    
    /**
     * Aplica el tema actual a un nodo específico y todos sus hijos
     * @param node Nodo al que aplicar el tema
     */
    public static void applyThemeToNode(Node node) {
        // EXCLUSIÓN TOTAL DEL CALENDARIO: No aplicar ningún tema al calendario nativo
        if (isCalendarComponent(node)) {
            System.out.println("🚫 ThemeUtil: Excluyendo componente de calendario de la aplicación de temas");
            return; // Salir inmediatamente sin procesar este nodo ni sus hijos
        }
        
        ThemeManager themeManager = ThemeManager.getInstance();
        
        // Aplicar clases específicas según el tipo de nodo
        applyThemeClassesToNode(node);
        
        // Si el nodo tiene hijos, aplicar recursivamente
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                applyThemeToNode(child);
            }
        }
    }
    
    /**
     * Elimina estilos hardcodeados problemáticos de un nodo
     * @param node Nodo del que eliminar estilos problemáticos
     */
    public static void removeHardcodedStyles(Node node) {
        // EXCLUSIÓN TOTAL DEL CALENDARIO: No modificar estilos del calendario nativo
        if (isCalendarComponent(node)) {
            System.out.println("🚫 ThemeUtil: Excluyendo componente de calendario de la eliminación de estilos");
            return; // Salir inmediatamente sin procesar este nodo ni sus hijos
        }
        
        String style = node.getStyle();
        if (style != null && !style.isEmpty()) {
            // Eliminar colores de fondo hardcodeados problemáticos
            style = style.replaceAll("-fx-background-color\\s*:\\s*#f8f9fa\\s*;?", "");
            style = style.replaceAll("-fx-background-color\\s*:\\s*white\\s*;?", "");
            style = style.replaceAll("-fx-background-color\\s*:\\s*#ffffff\\s*;?", "");
            
            // Limpiar estilos vacíos
            style = style.trim();
            if (style.endsWith(";")) {
                style = style.substring(0, style.length() - 1);
            }
            
            node.setStyle(style.isEmpty() ? null : style);
        }
        
        // Aplicar recursivamente a los hijos
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                removeHardcodedStyles(child);
            }
        }
    }
    
    /**
     * Aplica clases CSS específicas a un nodo basándose en su ID y estilo
     * @param node Nodo al que aplicar las clases
     */
    private static void applyThemeClassesToNode(Node node) {
        // EXCLUSIÓN TOTAL DEL CALENDARIO: No aplicar clases CSS al calendario nativo
        if (isCalendarComponent(node)) {
            return; // Salir inmediatamente sin procesar este nodo
        }
        
        // Aplicar clases específicas según el tipo de nodo y su ID
        if (node.getId() != null) {
            String nodeId = node.getId().toLowerCase();
            
            // Identificar contenedores principales de módulos
            if (nodeId.contains("maincontainer") || nodeId.contains("contentcontainer") || 
                nodeId.contains("scrollpane") || nodeId.contains("informes") ||
                (nodeId.contains("main") && nodeId.contains("pane"))) {
                
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
    }
    
    /**
     * Verifica si un nodo es parte del componente de calendario y debe ser excluido del tema
     * @param node Nodo a verificar
     * @return true si es parte del calendario y debe ser excluido
     */
    private static boolean isCalendarComponent(Node node) {
        if (node == null) return false;
        
        // Verificar por ID específico del calendario
        if (node.getId() != null) {
            String nodeId = node.getId().toLowerCase();
            if (nodeId.contains("calendar-fx-isolated-component") || 
                nodeId.contains("calendar-view-isolated") ||
                nodeId.contains("calendar-day-page") ||
                nodeId.contains("calendar-week-page") ||
                nodeId.contains("calendar-month-page") ||
                nodeId.contains("calendar-year-page")) {
                return true;
            }
        }
        
        // Verificar por clase del nodo
        String className = node.getClass().getName().toLowerCase();
        if (className.contains("calendarfx") || 
            className.contains("calendar") && className.contains("view") ||
            className.contains("com.calendarfx")) {
            return true;
        }
        
        // Verificar por clases CSS
        if (node.getStyleClass() != null) {
            for (String styleClass : node.getStyleClass()) {
                if (styleClass.toLowerCase().contains("calendar") ||
                    styleClass.toLowerCase().contains("calendarfx")) {
                    return true;
                }
            }
        }
        
        // Verificar si es instancia de CalendarFXComponent
        if (node.getClass().getName().contains("CalendarFXComponent")) {
            return true;
        }
        
        // Verificar si algún padre es un componente de calendario
        Node parent = node.getParent();
        while (parent != null) {
            if (parent.getId() != null && 
                (parent.getId().contains("calendar-fx-isolated-component") ||
                 parent.getId().contains("calendar-view-isolated"))) {
                return true;
            }
            
            if (parent.getClass().getName().contains("CalendarFXComponent") ||
                parent.getClass().getName().toLowerCase().contains("calendarfx")) {
                return true;
            }
            
            parent = parent.getParent();
        }
        
        return false;
    }
    
    /**
     * Fuerza la actualización del tema en la escena actual
     * @param scene Escena a actualizar
     */
    public static void forceThemeUpdate(Scene scene) {
        ThemeManager.getInstance().forceUpdateScene(scene);
    }
    
    /**
     * Aplica el tema correcto a un contenedor de módulo específico
     * @param container Contenedor del módulo
     */
    public static void applyModuleTheme(Node container) {
        // EXCLUSIÓN TOTAL DEL CALENDARIO: No aplicar tema de módulo al calendario nativo
        if (isCalendarComponent(container)) {
            System.out.println("🚫 ThemeUtil: Excluyendo componente de calendario del tema de módulo");
            return; // Salir inmediatamente sin procesar
        }
        
        // Eliminar estilos hardcodeados problemáticos
        removeHardcodedStyles(container);
        
        // Aplicar clases de tema
        applyThemeToNode(container);
        
        // Agregar clase específica de módulo si no la tiene
        if (!container.getStyleClass().contains("module-main-container")) {
            container.getStyleClass().add("module-main-container");
        }
    }
    
    /**
     * Aplica el tema correcto a una tarjeta o panel de contenido
     * @param card Tarjeta o panel de contenido
     */
    public static void applyCardTheme(Node card) {
        // EXCLUSIÓN TOTAL DEL CALENDARIO: No aplicar tema de tarjeta al calendario nativo
        if (isCalendarComponent(card)) {
            System.out.println("🚫 ThemeUtil: Excluyendo componente de calendario del tema de tarjeta");
            return; // Salir inmediatamente sin procesar
        }
        
        // Eliminar estilos hardcodeados problemáticos
        removeHardcodedStyles(card);
        
        // Aplicar clases de tema
        applyThemeToNode(card);
        
        // Agregar clase específica de tarjeta si no la tiene
        if (!card.getStyleClass().contains("module-card")) {
            card.getStyleClass().add("module-card");
        }
    }
} 