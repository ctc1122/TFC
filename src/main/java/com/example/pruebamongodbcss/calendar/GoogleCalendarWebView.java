package com.example.pruebamongodbcss.calendar;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import com.example.pruebamongodbcss.theme.ThemeManager;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

/**
 * Componente que muestra la interfaz web de calendario personalizada
 * con interacción bidireccional entre Java y JavaScript
 */
public class GoogleCalendarWebView extends BorderPane {
    
    private WebView webView;
    private WebEngine webEngine;
    private StackPane browserContainer;
    
    // Ruta al archivo HTML del calendario - Usando versión para pruebas
    private static final String CALENDAR_HTML_PATH = "/com/example/pruebamongodbcss/calendar/test-calendar.html";
    
    // Listener para cambios de tema
    private ChangeListener<Boolean> themeChangeListener;
    
    // Lista de eventos en memoria
    private List<CalendarEvent> events = new ArrayList<>();
    
    // Servicio de calendario
    private CalendarService calendarService;
    
    private com.example.pruebamongodbcss.Data.Usuario usuarioActual;
    
    /**
     * Constructor que recibe el usuario actual
     */
    public GoogleCalendarWebView(com.example.pruebamongodbcss.Data.Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        initialize();
    }
    
    /**
     * Constructor por defecto (para compatibilidad, pero no filtra por usuario)
     */
    public GoogleCalendarWebView() {
        this(null);
    }
    
    /**
     * Inicializa el componente
     */
    private void initialize() {
        // Inicializar el servicio de calendario
        calendarService = new CalendarService();
        
        // Actualizar todas las citas para asignar usuario correcto
        actualizarCitasConUsuarioCorrecto();
        
        // Configurar el contenedor principal
        setPadding(new Insets(10));
        getStyleClass().add("calendar-container");
        
        // Inicializar el navegador WebView
        initBrowser();
        
        // Aplicar tema
        applyTheme();
        
        // Escuchar cambios de tema usando el BooleanProperty del ThemeManager
        themeChangeListener = (obs, oldVal, newVal) -> applyTheme();
        ThemeManager.getInstance().darkThemeProperty().addListener(themeChangeListener);
    }
    
    /**
     * Actualiza todas las citas existentes para asignarles el usuario correcto
     */
    private void actualizarCitasConUsuarioCorrecto() {
        try {
            int citasActualizadas = calendarService.actualizarCitasParaAgregarUsuario();
            if (citasActualizadas > 0) {
                System.out.println("Se actualizaron " + citasActualizadas + " citas para asignarles el usuario correcto");
            }
        } catch (Exception e) {
            System.err.println("Error al actualizar citas con usuario correcto: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Inicializa el navegador web con WebView
     */
    private void initBrowser() {
        try {
            System.out.println("Inicializando navegador web para calendario...");
            
            // Crear el contenedor para el navegador
            browserContainer = new StackPane();
            browserContainer.setPrefSize(800, 600);
            
            // Crear el WebView
            webView = new WebView();
            webEngine = webView.getEngine();
            
            // Configurar JavaScript
            webEngine.setJavaScriptEnabled(true);
            
            // Habilitar la consola de JavaScript en la salida estándar
            webEngine.setOnAlert(event -> System.out.println("JS Alert: " + event.getData()));
            
            // Interceptar errores de JavaScript
            webEngine.getLoadWorker().exceptionProperty().addListener(
                (obs, old, error) -> {
                    if (error != null) {
                        System.err.println("Error de JavaScript: " + error.getMessage());
                        error.printStackTrace();
                    }
                }
            );
            
            // Escuchar cuando la página termine de cargar
            webEngine.getLoadWorker().stateProperty().addListener(
                (observable, oldValue, newValue) -> {
                    System.out.println("Estado de carga: " + newValue);
                    
                    if (newValue == Worker.State.SUCCEEDED) {
                        System.out.println("Página cargada correctamente, configurando puente JS-Java");
                        
                        try {
                            // Esperar un momento para asegurar que JavaScript ha inicializado completamente
                            Thread.sleep(500);
                            
                            // Crear puente entre Java y JavaScript
                            JSObject window = (JSObject) webEngine.executeScript("window");
                            window.setMember("javaConnector", new JavaConnector(this));
                            
                            // Verificar si la API de calendario está disponible
                            boolean apiAvailable = (boolean) webEngine.executeScript(
                                "(() => { return (typeof window.calendarApi !== 'undefined'); })();"
                            );
                            
                            System.out.println("API de calendario disponible: " + apiAvailable);
                            
                            if (apiAvailable) {
                                // PRIMERO: Cargar eventos desde la base de datos
                                // Esto debe hacerse antes de aplicar el tema para evitar recargas
                                System.out.println("Cargando eventos ANTES de configurar tema...");
                                loadEventsFromDatabase();
                                
                                // DESPUÉS: Establecer el tema actual
                                boolean isDarkTheme = ThemeManager.getInstance().isDarkTheme();
                                webEngine.executeScript(
                                    "(() => { "
                                    + "console.log('Configurando tema inicial: " + (isDarkTheme ? "oscuro" : "claro") + "'); "
                                    + "if(window.calendarApi) { "
                                    + "    window.calendarApi.toggleDarkMode(" + isDarkTheme + "); "
                                    + "} else { "
                                    + "    console.error('calendarApi no está disponible'); "
                                    + "}"
                                    + "})();"
                                );
                                System.out.println("Tema configurado: " + (isDarkTheme ? "oscuro" : "claro"));
                            } else {
                                System.err.println("ERROR: La API del calendario no está disponible en la página");
                            }
                        } catch (Exception e) {
                            System.err.println("Error al configurar puente JS-Java: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else if (newValue == Worker.State.FAILED) {
                        System.err.println("Error al cargar la página del calendario");
                    }
                }
            );
            
            // Obtener URL del archivo HTML
            URL url = getClass().getResource(CALENDAR_HTML_PATH);
            if (url != null) {
                System.out.println("Cargando React desde: " + url.toExternalForm());
                webEngine.load(url.toExternalForm());
            } else {
                throw new Exception("No se pudo encontrar el archivo HTML del calendario React");
            }
            
            // Añadir el WebView al contenedor
            browserContainer.getChildren().add(webView);
            
            // Mostrar el navegador
            setCenter(browserContainer);
            
        } catch (Exception e) {
            System.err.println("Error al inicializar el navegador web: " + e.getMessage());
            e.printStackTrace();
            
            // Mostrar mensaje de error
            Label errorLabel = new Label("Error al cargar el calendario: " + e.getMessage());
            errorLabel.getStyleClass().add("error-label");
            
            VBox errorContainer = new VBox(10);
            errorContainer.setAlignment(Pos.CENTER);
            errorContainer.getChildren().add(errorLabel);
            
            setCenter(errorContainer);
        }
    }
    
    /**
     * Carga eventos desde la base de datos
     */
    private void loadEventsFromDatabase() {
        try {
            System.out.println("Cargando eventos del calendario...");
            
            // Cargar eventos según el rol del usuario
            this.events = loadEventsByUserRole();
            
            if (events != null && !events.isEmpty()) {
                System.out.println("Se cargaron " + events.size() + " eventos");
                updateCalendarEvents();
            } else {
                System.out.println("No se encontraron eventos para mostrar");
            }
        } catch (Exception e) {
            System.err.println("Error al cargar eventos: " + e.getMessage());
            e.printStackTrace();
            events = new ArrayList<>();
        }
    }
    
    /**
     * Carga eventos según el rol del usuario actual
     * - Administradores: ven todas las citas
     * - Auxiliares: ven todas las citas
     * - Usuarios normales: solo ven sus propias citas
     * 
     * @return Lista de eventos según el rol del usuario
     */
    private List<CalendarEvent> loadEventsByUserRole() {
        List<CalendarEvent> result = new ArrayList<>();
        
        try {
            // Verificar si hay usuario logueado
            if (usuarioActual == null) {
                System.out.println("ERROR CRÍTICO: No hay usuario logueado. Revisar cómo se inicializa GoogleCalendarWebView");
                System.out.println("El usuario debe ser pasado en el constructor GoogleCalendarWebView(usuario)");
                throw new IllegalStateException("No hay usuario logueado para cargar citas");
            }
            
            String username = usuarioActual.getUsuario();
            com.example.pruebamongodbcss.Data.Usuario.Rol rol = usuarioActual.getRol();
            System.out.println("Cargando citas para: " + username + " (Rol: " + rol + ")");
            
            // Determinar qué citas mostrar según el rol
            if (rol != null && (rol == com.example.pruebamongodbcss.Data.Usuario.Rol.ADMINISTRADOR || 
                                rol == com.example.pruebamongodbcss.Data.Usuario.Rol.AUXILIAR)) {
                // Administradores y auxiliares ven todas las citas
                System.out.println("Usuario con rol " + rol + ": cargando TODAS las citas");
                result = calendarService.getAllAppointments();
            } else {
                // Usuarios normales solo ven sus propias citas
                System.out.println("Usuario normal: cargando solo sus citas");
                result = calendarService.getAppointmentsByUser(username);
                
                // Si no se encontraron citas, intentar buscar por nombre
                if (result.isEmpty() && usuarioActual.getNombre() != null && usuarioActual.getApellido() != null) {
                    String nombreCompleto = usuarioActual.getNombre() + " " + usuarioActual.getApellido();
                    System.out.println("No se encontraron citas por usuario, buscando por nombre: " + nombreCompleto);
                    result = calendarService.searchAppointments(nombreCompleto);
                }
            }
            
            System.out.println("Total de citas encontradas: " + result.size());
            return result;
        } catch (Exception e) {
            System.err.println("Error al cargar citas según rol: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error cargando citas del usuario: " + e.getMessage(), e);
        }
    }
    
    /**
     * Actualiza los eventos en el calendario web
     */
    private void updateCalendarEvents() {
        if (webEngine != null && webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            try {
                // Convertir la lista de eventos a un JSONArray
                JSONArray eventsJsonArray = new JSONArray();
                for (CalendarEvent event : events) {
                    JSONObject jsonEvent = convertEventToJsonObject(event);
                    eventsJsonArray.put(jsonEvent);
                    // Depuración de cada evento
                    System.out.println("Evento procesado: " + event.getId() + 
                                      " - " + event.getTitle() + 
                                      " [" + event.getStart() + " a " + event.getEnd() + "]");
                }
                
                // Depuración
                System.out.println("Enviando " + events.size() + " eventos al calendario");
                
                // Escapar las comillas en el JSON para evitar problemas con JavaScript
                String jsonString = eventsJsonArray.toString()
                    .replace("\\", "\\\\")
                    .replace("'", "\\'");
                
                // Imprimir JSON para depuración
                System.out.println("JSON eventos: " + jsonString);
                
                // Enviar los eventos al calendario web con método alternativo
                // que evita problemas de escape
                String script = 
                    "(() => { " +
                    "  console.log('Actualizando eventos desde Java...'); " +
                    "  if (typeof window.calendarApi !== 'undefined' && window.calendarApi) { " +
                    "    try { " +
                    "      window.calendarApi.updateEvents(" + jsonString + "); " +
                    "      console.log('Eventos actualizados correctamente'); " +
                    "      return true; " +
                    "    } catch (e) { " +
                    "      console.error('Error al actualizar eventos:', e); " +
                    "      return false; " +
                    "    } " +
                    "  } else { " +
                    "    console.error('calendarApi no disponible'); " +
                    "    return false; " +
                    "  } " +
                    "})();";
                
                System.out.println("Ejecutando script para actualizar eventos...");
                boolean resultado = (boolean)webEngine.executeScript(script);
                if (resultado) {
                    System.out.println("Eventos enviados al calendario web correctamente");
                } else {
                    System.err.println("⚠️ No se pudieron enviar los eventos al calendario");
                }
                
            } catch (Exception e) {
                System.err.println("Error al actualizar eventos en el calendario: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("No se pueden actualizar eventos: el motor web no está listo");
            if (webEngine != null) {
                System.err.println("Estado del motor web: " + webEngine.getLoadWorker().getState());
            }
        }
    }
    
    /**
     * Convierte un evento a formato JSONObject
     */
    private JSONObject convertEventToJsonObject(CalendarEvent event) {
        JSONObject jsonEvent = new JSONObject();
        
        // Campos obligatorios con valores por defecto si son null
        jsonEvent.put("id", event.getId() != null ? event.getId() : generateTempId());
        jsonEvent.put("title", event.getTitle() != null ? event.getTitle() : "Sin título");
        
        // Asegurarnos que las fechas están en formato ISO
        String startDate = formatDateIfNeeded(event.getStart());
        String endDate = formatDateIfNeeded(event.getEnd());
        
        jsonEvent.put("start", startDate);
        jsonEvent.put("end", endDate);
        
        // Campos opcionales solo si tienen valor
        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            jsonEvent.put("description", event.getDescription());
        }
        
        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
            jsonEvent.put("location", event.getLocation());
        }
        
        if (event.getColor() != null && !event.getColor().isEmpty()) {
            jsonEvent.put("color", event.getColor());
        }
        
        if (event.getTextColor() != null && !event.getTextColor().isEmpty()) {
            jsonEvent.put("textColor", event.getTextColor());
        }
        
        jsonEvent.put("allDay", event.isAllDay());
        
        // Añadir tipo de evento para clasificar en React
        String eventType = "default";
        if (event.getType() != null) {
            eventType = event.getType();
        } else if (event.getTitle() != null) {
            String title = event.getTitle().toLowerCase();
            if (title.contains("urgente") || title.contains("urgencia")) {
                eventType = "urgent";
            } else if (title.contains("completada") || title.contains("realizada")) {
                eventType = "completed";
            } else if (title.contains("cancelada")) {
                eventType = "cancelled";
            }
        }
        jsonEvent.put("type", eventType);
        
        // Añadir eventType para filtros de eventos vs citas
        if (event.getEventType() != null) {
            jsonEvent.put("eventType", event.getEventType());
        } else {
            // Si no tiene eventType pero tiene estado, es una cita, no un evento
            if (event.getEstado() == null || event.getEstado().isEmpty()) {
                // Determinar eventType según el título si parece un evento y no una cita
                String title = event.getTitle().toLowerCase();
                if (title.contains("reunión") || title.contains("reunion") || title.contains("meeting")) {
                    jsonEvent.put("eventType", "meeting");
                } else if (title.contains("recordatorio") || title.contains("reminder")) {
                    jsonEvent.put("eventType", "reminder");
                } else {
                    jsonEvent.put("eventType", "other");
                }
            }
        }
        
        // Añadir estado para filtros adicionales
        if (event.getEstado() != null) {
            jsonEvent.put("estado", event.getEstado());
        } else {
            // Determinar estado según el tipo si no hay estado asignado
            switch (eventType) {
                case "urgent":
                    jsonEvent.put("estado", "EN_CURSO");
                    break;
                case "completed":
                    jsonEvent.put("estado", "COMPLETADA");
                    break;
                case "cancelled":
                    jsonEvent.put("estado", "CANCELADA");
                    break;
                default:
                    jsonEvent.put("estado", "PENDIENTE");
                    break;
            }
        }
        
        return jsonEvent;
    }
    
    /**
     * Genera un ID temporal para eventos sin ID
     */
    private String generateTempId() {
        return "temp_" + System.currentTimeMillis();
    }
    
    /**
     * Asegura que la fecha está en formato ISO
     */
    private String formatDateIfNeeded(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            // Si no hay fecha, usar la actual
            return LocalDateTime.now().toString();
        }
        
        System.out.println("Formateando fecha: " + dateStr);
        
        try {
            // Si ya tiene formato ISO con la T, verificar que sea válido
            if (dateStr.contains("T")) {
                // Intentar parsear para validar el formato
                LocalDateTime parsed = LocalDateTime.parse(dateStr);
                System.out.println("  ✓ Fecha ya en formato ISO: " + parsed);
                return dateStr;
            }
            
            // Intentar darle formato si es un LocalDateTime sin formato
            LocalDateTime dateTime = LocalDateTime.parse(dateStr);
            System.out.println("  ✓ Fecha convertida a formato ISO: " + dateTime);
            return dateTime.toString();
        } catch (Exception e) {
            System.err.println("  ✗ Error al formatear fecha: " + dateStr + " - " + e.getMessage());
            
            try {
                // Intentar interpretar la fecha como LocalDateTime actual
                LocalDateTime now = LocalDateTime.now();
                System.out.println("  ! Usando fecha actual como fallback: " + now);
                return now.toString();
            } catch (Exception e2) {
                // Si todo falla, devolver la fecha actual
                System.err.println("  ✗ Error grave en formateo de fecha, usando hora del sistema");
                return LocalDateTime.now().toString();
            }
        }
    }
    
    /**
     * Aplica el tema actual
     */
    private void applyTheme() {
        ThemeManager themeManager = ThemeManager.getInstance();
        if (themeManager.isDarkTheme()) {
            getStyleClass().add("dark-theme");
            getStyleClass().remove("light-theme");
            
            // Actualizar tema en la página web
            if (webEngine != null && webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
                try {
                    webEngine.executeScript(
                        "(() => { "
                        + "if(window.calendarApi) { "
                        + "   window.calendarApi.toggleDarkMode(true); "
                        + "} "
                        + "})();"
                    );
                } catch (Exception e) {
                    System.err.println("Error al aplicar tema oscuro: " + e.getMessage());
                }
            }
        } else {
            getStyleClass().add("light-theme");
            getStyleClass().remove("dark-theme");
            
            // Actualizar tema en la página web
            if (webEngine != null && webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
                try {
                    webEngine.executeScript(
                        "(() => { "
                        + "if(window.calendarApi) { "
                        + "   window.calendarApi.toggleDarkMode(false); "
                        + "} "
                        + "})();"
                    );
                } catch (Exception e) {
                    System.err.println("Error al aplicar tema claro: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Procesa eventos recibidos desde JavaScript
     */
    public void processEvent(String action, String eventData) {
        try {
            System.out.println("Evento recibido desde JavaScript: " + action + ", datos: " + eventData);
            
            // Acción de refresco especial para cargar citas del usuario
            if ("refresh".equals(action)) {
                JSONObject requestData = new JSONObject(eventData);
                String requestType = requestData.optString("requestType", "");
                
                if ("userAppointments".equals(requestType)) {
                    System.out.println("Solicitando citas del usuario actual...");
                    loadUserAppointments();
                    return;
                }
            }
            
            // Acción para abrir el formulario de cita clínica
            if ("openClinicaForm".equals(action)) {
                try {
                    JSONObject requestData = new JSONObject(eventData);
                    String dateStr = requestData.optString("date", "");
                    System.out.println("Solicitando apertura de formulario de cita para fecha: " + dateStr);
                    
                    // Informar al usuario que esta funcionalidad requiere integración adicional
                    Platform.runLater(() -> {
                        try {
                            // Intentar notificar a la escena actual
                            Scene scene = getScene();
                            if (scene != null) {
                                Node root = scene.getRoot();
                                if (root != null) {
                                    // Verificar si la raíz tiene un método para abrir el formulario de citas
                                    boolean success = triggerFormOpeningInParent(dateStr);
                                    if (!success) {
                                        System.out.println("No se pudo abrir el formulario de citas automáticamente");
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error al intentar abrir formulario de citas: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                    return;
                } catch (Exception e) {
                    System.err.println("Error al procesar solicitud de apertura de formulario clínico: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Para otras acciones, convertir JSON a objeto CalendarEvent
            CalendarEvent event = parseEventFromJson(eventData);
            
            // Procesar según la acción
            switch (action) {
                case "save":
                    saveEvent(event);
                    break;
                case "update":
                    updateEvent(event);
                    break;
                case "delete":
                    deleteEvent(event);
                    break;
                case "click":
                    // Solo registrar el clic para depuración
                    System.out.println("Clic en evento: " + event.getId() + " - " + event.getTitle());
                    break;
                default:
                    System.out.println("Acción desconocida: " + action);
            }
        } catch (Exception e) {
            System.err.println("Error al procesar evento: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Intenta activar la apertura del formulario de citas en el componente padre
     * Este método usa reflexión para intentar encontrar métodos relevantes
     * @param dateStr Fecha para la cita
     * @return true si se pudo abrir el formulario
     */
    private boolean triggerFormOpeningInParent(String dateStr) {
        try {
            // Convertir la fecha si está disponible
            LocalDateTime dateTime;
            
            if (dateStr != null && !dateStr.isEmpty()) {
                try {
                    dateTime = LocalDateTime.parse(dateStr.replace("Z", ""));
                } catch (Exception e) {
                    System.err.println("Error al parsear fecha: " + e.getMessage());
                    dateTime = LocalDateTime.now(); // Usar fecha actual como respaldo
                }
            } else {
                dateTime = LocalDateTime.now(); // Usar fecha actual como respaldo
            }
            
            // Crear una copia final para usarla en lambdas
            final LocalDateTime date = dateTime;
            
            // Método DIRECTO: Intentar con clase conocida primero
            try {
                // Intentar abrir directamente el formulario clínico
                openClinicaFormDirectly(date);
                return true;
            } catch (Exception e) {
                System.err.println("Error al abrir formulario directamente: " + e.getMessage());
            }
            
            // ALTERNATIVA: Intentar encontrar el controlador principal
            Scene scene = getScene();
            if (scene == null) return false;
            
            // Obtener la ventana (Stage)
            Stage stage = (Stage) scene.getWindow();
            if (stage == null) return false;
            
            // Buscar objetos útiles en las propiedades de la escena y ventana
            Object mainController = scene.getUserData();
            if (mainController == null) {
                mainController = stage.getUserData();
            }
            
            if (mainController != null) {
                // Intentar varios métodos comunes para abrir un formulario de citas
                final LocalDateTime finalDate = date;
                Class<?> controllerClass = mainController.getClass();
                
                // Método 1: openClinicaForm o similar
                try {
                    java.lang.reflect.Method method = findSuitableMethod(controllerClass, 
                            "openClinicaForm", "openAppointmentForm", "showClinicaForm", "mostrarFormularioCita");
                    
                    if (method != null) {
                        System.out.println("Encontrado método para abrir formulario: " + method.getName());
                        
                        // Determinar los parámetros correctos
                        Class<?>[] paramTypes = method.getParameterTypes();
                        Object[] params = new Object[paramTypes.length];
                        
                        // Intentar llenar los parámetros con valores adecuados
                        for (int i = 0; i < paramTypes.length; i++) {
                            if (paramTypes[i].isAssignableFrom(LocalDateTime.class)) {
                                params[i] = finalDate;
                            } else if (paramTypes[i].isAssignableFrom(com.example.pruebamongodbcss.Data.Usuario.class)) {
                                params[i] = usuarioActual;
                            } else {
                                params[i] = null;
                            }
                        }
                        
                        // Invocar el método
                        method.invoke(mainController, params);
                        return true;
                    }
                } catch (Exception e) {
                    System.err.println("Error intentando método 1: " + e.getMessage());
                }
                
                // Método 2: navegar a una vista de citas
                try {
                    java.lang.reflect.Method method = findSuitableMethod(controllerClass,
                            "navigate", "navigateTo", "showView", "mostrarVista");
                    
                    if (method != null) {
                        System.out.println("Encontrado método de navegación: " + method.getName());
                        
                        // Intentar navegar a la vista de citas
                        Class<?>[] paramTypes = method.getParameterTypes();
                        if (paramTypes.length == 1 && paramTypes[0] == String.class) {
                            // Probar diferentes nombres para la vista de citas
                            String[] possibleViews = {"clinica", "citas", "appointments", "nuevaCita"};
                            
                            for (String viewName : possibleViews) {
                                try {
                                    method.invoke(mainController, viewName);
                                    return true;
                                } catch (Exception e) {
                                    // Intentar con el siguiente nombre
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error intentando método 2: " + e.getMessage());
                }
            }
            
            // Si no funcionó nada, intentar con el método directo nuevamente
            try {
                openClinicaFormDirectly(date);
                return true;
            } catch (Exception e) {
                System.err.println("Error al intentar abrir formulario directamente (segundo intento): " + e.getMessage());
                
                // Último recurso: Mostrar mensaje informativo
                final LocalDateTime finalDate = date;
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Información");
                    alert.setHeaderText("Formulario de Citas");
                    alert.setContentText("Para agendar una cita médica, por favor utilice la sección de Clínica " +
                                         "desde el menú principal. La fecha seleccionada es: " + 
                                         (finalDate != null ? finalDate.toLocalDate().toString() : "hoy"));
                    alert.showAndWait();
                });
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("Error en triggerFormOpeningInParent: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Intenta abrir directamente el formulario de citas clínicas
     * @param date Fecha de la cita
     */
    private void openClinicaFormDirectly(LocalDateTime date) throws Exception {
        System.out.println("Intentando abrir formulario de citas directamente...");
        
        // Buscar la clase del controlador de clínica
        Class<?> clinicaControllerClass = null;
        try {
            // Intentar diferentes nombres de paquete
            String[] possiblePackages = {
                "com.example.pruebamongodbcss.controllers",
                "com.example.pruebamongodbcss.controller",
                "com.example.pruebamongodbcss"
            };
            
            for (String pkg : possiblePackages) {
                try {
                    clinicaControllerClass = Class.forName(pkg + ".ClinicaController");
                    if (clinicaControllerClass != null) {
                        System.out.println("Encontrada clase ClinicaController en paquete: " + pkg);
                        break;
                    }
                } catch (ClassNotFoundException e) {
                    // Intentar con el siguiente paquete
                }
            }
            
            if (clinicaControllerClass == null) {
                throw new ClassNotFoundException("No se pudo encontrar la clase ClinicaController");
            }
            
            // Intentar cargar el FXML
            final Class<?> finalClinicaControllerClass = clinicaControllerClass;
            
            Platform.runLater(() -> {
                try {
                    // Intentar diferentes rutas para el FXML
                    String[] possibleFxmlPaths = {
                        "/com/example/pruebamongodbcss/clinica-view.fxml",
                        "/com/example/pruebamongodbcss/views/clinica-view.fxml",
                        "/com/example/pruebamongodbcss/fxml/clinica-view.fxml",
                        "/fxml/clinica-view.fxml",
                        "/clinica-view.fxml"
                    };
                    
                    URL fxmlUrl = null;
                    for (String path : possibleFxmlPaths) {
                        fxmlUrl = getClass().getResource(path);
                        if (fxmlUrl != null) {
                            System.out.println("Encontrado FXML en ruta: " + path);
                            break;
                        }
                    }
                    
                    if (fxmlUrl == null) {
                        throw new Exception("No se pudo encontrar el archivo FXML para Clinica");
                    }
                    
                    // Cargar el FXML
                    FXMLLoader loader = new FXMLLoader(fxmlUrl);
                    Parent root = loader.load();
                    
                    // Obtener el controlador
                    Object controller = loader.getController();
                    if (controller == null) {
                        throw new Exception("No se pudo obtener el controlador del FXML");
                    }
                    
                    // Configurar el controlador
                    if (controller.getClass() == finalClinicaControllerClass) {
                        // Usar reflexión para invocar métodos en el controlador
                        try {
                            // Intentar establecer el usuario
                            if (usuarioActual != null) {
                                java.lang.reflect.Method setUsuarioMethod = findSuitableMethod(
                                    finalClinicaControllerClass, "setUsuarioActual", "setUsuario");
                                
                                if (setUsuarioMethod != null) {
                                    setUsuarioMethod.invoke(controller, usuarioActual);
                                }
                            }
                            
                            // Intentar establecer la fecha
                            java.lang.reflect.Method setFechaMethod = findSuitableMethod(
                                finalClinicaControllerClass, "setFechaCitaDesdeCalendario", "setFechaCita");
                            
                            if (setFechaMethod != null && date != null) {
                                setFechaMethod.invoke(controller, date);
                            }
                            
                            // Intentar inicializar el formulario como nueva cita
                            java.lang.reflect.Method prepararMethod = findSuitableMethod(
                                finalClinicaControllerClass, "prepararNuevaCita", "nuevaCita", "initialize");
                            
                            if (prepararMethod != null) {
                                prepararMethod.invoke(controller);
                            }
                        } catch (Exception e) {
                            System.err.println("Error al configurar el controlador: " + e.getMessage());
                        }
                    }
                    
                    // Crear y mostrar una nueva ventana
                    Stage stage = new Stage();
                    stage.setTitle("Nueva Cita Médica");
                    stage.setScene(new Scene(root));
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.show();
                    
                } catch (Exception e) {
                    System.err.println("Error al abrir formulario directamente: " + e.getMessage());
                    e.printStackTrace();
                    
                    // Mostrar diálogo de error
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("No se pudo abrir el formulario de citas");
                    alert.setContentText("Detalles: " + e.getMessage());
                    alert.showAndWait();
                }
            });
        } catch (Exception e) {
            System.err.println("Error buscando el controlador: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Busca un método adecuado por nombres posibles
     * @param clazz Clase donde buscar
     * @param possibleNames Nombres posibles del método
     * @return El método encontrado o null
     */
    private java.lang.reflect.Method findSuitableMethod(Class<?> clazz, String... possibleNames) {
        for (String name : possibleNames) {
            java.lang.reflect.Method[] methods = clazz.getMethods();
            for (java.lang.reflect.Method method : methods) {
                if (method.getName().equals(name)) {
                    return method;
                }
            }
        }
        return null;
    }
    
    /**
     * Carga las citas del usuario actual desde el módulo de citas
     */
    private void loadUserAppointments() {
        try {
            System.out.println("Actualizando citas desde botón de refresco...");
            
            // Usar el mismo método basado en roles
            List<CalendarEvent> userAppointments = loadEventsByUserRole();
            
            if (userAppointments != null && !userAppointments.isEmpty()) {
                System.out.println("Se cargaron " + userAppointments.size() + " citas");
                this.events = userAppointments;
            } else {
                System.out.println("No se encontraron citas para mostrar");
                this.events = new ArrayList<>();
            }
            
            updateCalendarEvents();
        } catch (Exception e) {
            System.err.println("Error al cargar citas del usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Parsea un evento desde un JSON string
     */
    private CalendarEvent parseEventFromJson(String eventData) {
        try {
            JSONObject jsonEvent = new JSONObject(eventData);
            CalendarEvent event = new CalendarEvent();
            
            if (jsonEvent.has("id")) {
                event.setId(jsonEvent.getString("id"));
            }
            
            if (jsonEvent.has("title")) {
                event.setTitle(jsonEvent.getString("title"));
            }
            
            if (jsonEvent.has("start")) {
                event.setStart(jsonEvent.getString("start"));
            }
            
            if (jsonEvent.has("end")) {
                event.setEnd(jsonEvent.getString("end"));
            }
            
            if (jsonEvent.has("description")) {
                event.setDescription(jsonEvent.getString("description"));
            }
            
            if (jsonEvent.has("location")) {
                event.setLocation(jsonEvent.getString("location"));
            }
            
            if (jsonEvent.has("color")) {
                event.setColor(jsonEvent.getString("color"));
            }
            
            if (jsonEvent.has("textColor")) {
                event.setTextColor(jsonEvent.getString("textColor"));
            }
            
            if (jsonEvent.has("allDay")) {
                event.setAllDay(jsonEvent.getBoolean("allDay"));
            }
            
            // Manejar el tipo de evento
            if (jsonEvent.has("type")) {
                String type = jsonEvent.getString("type");
                event.setType(type);
                
                // Asignar colores según el tipo si no se especificaron
                if (!jsonEvent.has("color")) {
                    switch (type) {
                        case "urgent":
                            event.setColor("#ff9800");
                            break;
                        case "completed":
                            event.setColor("#4caf50");
                            break;
                        case "cancelled":
                            event.setColor("#f44336");
                            break;
                        default:
                            event.setColor("#1a73e8");
                    }
                }
            }
            
            // Manejar eventType (meeting, reminder, other)
            if (jsonEvent.has("eventType")) {
                event.setEventType(jsonEvent.getString("eventType"));
            }
            
            // Estado de la cita
            if (jsonEvent.has("estado")) {
                event.setEstado(jsonEvent.getString("estado"));
            } else if (jsonEvent.has("type")) {
                // Determinar estado según el tipo si no hay estado asignado
                String tipo = jsonEvent.getString("type");
                switch (tipo) {
                    case "urgent":
                        event.setEstado("EN_CURSO");
                        break;
                    case "completed":
                        event.setEstado("COMPLETADA");
                        break;
                    case "cancelled":
                        event.setEstado("CANCELADA");
                        break;
                    default:
                        event.setEstado("PENDIENTE");
                        break;
                }
            } else {
                event.setEstado("PENDIENTE");
            }
            
            return event;
        } catch (Exception e) {
            e.printStackTrace();
            return new CalendarEvent();
        }
    }
    
    /**
     * Guarda un evento en la base de datos
     */
    private void saveEvent(CalendarEvent event) {
        try {
            // Asignar el usuario actual al evento si está disponible
            if (usuarioActual != null) {
                event.setUsuario(usuarioActual.getUsuario()); 
            } else {
                // Si no hay usuario logueado, no asignar usuario
                System.out.println("ADVERTENCIA: No hay usuario logueado al guardar la cita");
            }
            
            // Verificar si ya existe
            if (event.getId() != null && !event.getId().isEmpty()) {
                Optional<CalendarEvent> existingEvent = events.stream()
                    .filter(e -> e.getId().equals(event.getId()))
                    .findFirst();
                if (existingEvent.isPresent()) {
                    calendarService.updateAppointment(event);
                } else {
                    CalendarEvent savedEvent = calendarService.saveAppointment(event);
                    if (savedEvent != null) {
                        events.add(savedEvent);
                    }
                }
            } else {
                CalendarEvent savedEvent = calendarService.saveAppointment(event);
                if (savedEvent != null) {
                    events.add(savedEvent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Actualiza un evento en la base de datos
     */
    private void updateEvent(CalendarEvent event) {
        try {
            if (event.getId() != null && !event.getId().isEmpty()) {
                // Actualizar en la base de datos
                calendarService.updateAppointment(event);
                
                // Actualizar en la lista local
                for (int i = 0; i < events.size(); i++) {
                    if (events.get(i).getId().equals(event.getId())) {
                        events.set(i, event);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Elimina un evento de la base de datos
     */
    private void deleteEvent(CalendarEvent event) {
        try {
            if (event.getId() != null && !event.getId().isEmpty()) {
                // Eliminar de la base de datos
                calendarService.deleteAppointment(event.getId());
                
                // Eliminar de la lista local
                events.removeIf(e -> e.getId().equals(event.getId()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Libera recursos al cerrar
     */
    public void dispose() {
        // Eliminar el listener de tema
        if (themeChangeListener != null) {
            ThemeManager.getInstance().darkThemeProperty().removeListener(themeChangeListener);
        }
    }
    
    /**
     * Conector entre Java y JavaScript
     */
    public class JavaConnector {
        private GoogleCalendarWebView parent;
        
        public JavaConnector(GoogleCalendarWebView parent) {
            this.parent = parent;
        }
        
        // Método para recibir eventos desde JavaScript
        public void processEvent(String action, String eventData) {
            System.out.println("JavaConnector recibió evento: " + action);
            parent.processEvent(action, eventData);
        }
    }
} 