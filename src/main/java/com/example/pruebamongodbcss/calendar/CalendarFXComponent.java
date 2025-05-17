package com.example.pruebamongodbcss.calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.DateControl;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.control.Control;
import javafx.scene.control.ToolBar;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita;
import com.example.pruebamongodbcss.calendar.EventoFormularioController;
import java.time.format.DateTimeFormatter;
import org.bson.types.ObjectId;

/**
 * Componente de calendario basado en CalendarFX.
 * Esta clase envuelve la funcionalidad de CalendarFX para su uso en la aplicación.
 */
public class CalendarFXComponent extends BorderPane {
    
    // Variables para el calendario
    private CalendarView calendarView;
    private List<Calendar> calendars = new ArrayList<>();
    private CalendarSource calendarSource;
    
    // Servicio de calendario para cargar citas reales
    private CalendarService calendarService;
    
    // Usuario actual
    private com.example.pruebamongodbcss.Data.Usuario usuarioActual;
    
    // Ruta al archivo CSS del calendario
    private static final String CALENDAR_DEFAULT_CSS = CalendarFXComponent.class.getResource("/com/example/pruebamongodbcss/theme/jfx-calendar-styles.css").toExternalForm();
    
    // CSS personalizado para forzar texto negro en todo el calendario
    private static final String BLACK_TEXT_CSS = 
        ".calendar-view * { -fx-text-fill: black !important; }" +
        ".date-time-label { -fx-text-fill: black !important; }" +
        ".date-time-header { -fx-text-fill: black !important; }" +
        ".weekday-label { -fx-text-fill: black !important; }" +
        ".date-label { -fx-text-fill: black !important; }" +
        ".month-year-label { -fx-text-fill: black !important; }" +
        ".time-label { -fx-text-fill: black !important; }" +
        ".detail-label { -fx-text-fill: black !important; }" +
        ".calendar-header { -fx-text-fill: black !important; }" +
        ".entry-title { -fx-text-fill: black !important; }" +
        ".agenda-view .time-label { -fx-text-fill: black !important; }" +
        ".day-of-week-label { -fx-text-fill: black !important; }" +
        ".day-of-month-label { -fx-text-fill: black !important; }";
    
    private Map<String, String> entryDescriptions = new java.util.HashMap<>();
    
    /**
     * Constructor que inicializa el componente del calendario con el usuario actual.
     * @param usuario El usuario actual logueado en el sistema
     */
    public CalendarFXComponent(com.example.pruebamongodbcss.Data.Usuario usuario) {
        this.usuarioActual = usuario;
        initialize();
    }
    
    /**
     * Constructor que inicializa el componente del calendario sin usuario.
     */
    public CalendarFXComponent() {
        this(null);
    }
    
    /**
     * Inicializa el componente del calendario.
     */
    private void initialize() {
        try {
            // Inicializar el servicio de calendario
            calendarService = new CalendarService();
            
            // Crear el componente principal de la vista
            calendarView = new CalendarView();
            
            // DESACTIVAR COMPLETAMENTE LOS ESTILOS DEL THEME MANAGER
            // Esto es una técnica radical pero efectiva
            calendarView.getStylesheets().clear();
            
            // Establecer un ID único para este componente y sus hijos
            // para evitar que los selectores CSS del ThemeManager lo afecten
            this.setId("calendar-fx-isolated-component");
            calendarView.setId("calendar-view-isolated");
            
            // Forzar el estilo predeterminado de CalendarFX SOLAMENTE
            if (!calendarView.getStylesheets().contains(CALENDAR_DEFAULT_CSS)) {
                calendarView.getStylesheets().add(CALENDAR_DEFAULT_CSS);
            }
            
            // Agregar el CSS para forzar texto negro
            calendarView.getStylesheets().add("data:text/css," + BLACK_TEXT_CSS.replace(" ", "%20"));
            
            // Aplicar tema básico
            applyTheme();
            
            // Desconectar este componente del ThemeManager
            desconectarDelThemeManager(this);
            
            // Crear los calendarios por tipo de cita con colores predeterminados
            Calendar citasPendientes = new Calendar("Citas pendientes");
            citasPendientes.setStyle(Calendar.Style.STYLE1); // Azul
            
            Calendar citasEnCurso = new Calendar("Citas en curso");
            citasEnCurso.setStyle(Calendar.Style.STYLE2); // Naranja
            
            Calendar citasCompletadas = new Calendar("Citas completadas");
            citasCompletadas.setStyle(Calendar.Style.STYLE3); // Verde
            
            Calendar citasCanceladas = new Calendar("Citas canceladas");
            citasCanceladas.setStyle(Calendar.Style.STYLE7); // Rojo
            
            Calendar eventosReuniones = new Calendar("Reuniones");
            eventosReuniones.setStyle(Calendar.Style.STYLE5); // Morado
            
            Calendar eventosRecordatorios = new Calendar("Recordatorios");
            eventosRecordatorios.setStyle(Calendar.Style.STYLE4); // Naranja claro
            
            // Configurar nombres cortos
            citasPendientes.setShortName("CP");
            citasEnCurso.setShortName("CEC");
            citasCompletadas.setShortName("CC");
            citasCanceladas.setShortName("CX");
            eventosReuniones.setShortName("REU");
            eventosRecordatorios.setShortName("REC");
            
            // Habilitar la edición de los calendarios
            citasPendientes.setReadOnly(false);
            citasEnCurso.setReadOnly(false);
            citasCompletadas.setReadOnly(false);
            citasCanceladas.setReadOnly(false);
            eventosReuniones.setReadOnly(false);
            eventosRecordatorios.setReadOnly(false);
            
            // Agregar a la lista de calendarios
            calendars.add(citasPendientes);
            calendars.add(citasEnCurso);
            calendars.add(citasCompletadas);
            calendars.add(citasCanceladas);
            calendars.add(eventosReuniones);
            calendars.add(eventosRecordatorios);
            
            // Agregar los calendarios a una fuente
            calendarSource = new CalendarSource("Clínica Veterinaria");
            calendarSource.getCalendars().addAll(calendars);
            
            // Registrar la fuente del calendario
            calendarView.getCalendarSources().add(calendarSource);
            
            // Configurar fecha y hora actual
            calendarView.setToday(LocalDate.now());
            calendarView.setTime(LocalTime.now());
            
            // Permitir la creación y edición de entradas
            calendarView.setEntryEditPolicy(param -> true);
            calendarView.setEntryFactory(param -> createNewEntry(param));
            
            // Configurar el manejo de eventos del calendario
            configureCalendarHandlers();
            
            // Mostrar páginas relevantes
            calendarView.showDayPage();
            calendarView.showWeekPage();
            calendarView.showMonthPage();
            
            // Configuraciones adicionales de estilo visual
            configureVisualSettings();
            
            // Cargar citas reales desde la base de datos
            loadAppointmentsFromDatabase();
            
            // Hilo para actualizar la hora
            Thread updateTimeThread = new Thread("Calendar: Update Time Thread") {
                @Override
                public void run() {
                    while (true) {
                        Platform.runLater(() -> {
                            calendarView.setToday(LocalDate.now());
                            calendarView.setTime(LocalTime.now());
                        });
                        
                        try {
                            // Actualizar cada 10 segundos
                            sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            
            updateTimeThread.setDaemon(true);
            updateTimeThread.start();
            
            // Crear barra de herramientas personalizada
            createCustomToolbar();
            
            // Añadir el calendario a este BorderPane
            setCenter(calendarView);
            
            // Buscar y modificar el botón de impresión una vez que todo esté configurado
            Platform.runLater(() -> {
                // Programar múltiples intentos para asegurar que capturamos el botón
                for (int i = 0; i < 5; i++) {
                    int delay = i * 1000;
                    new Thread(() -> {
                        try {
                            Thread.sleep(delay);
                            Platform.runLater(() -> {
                                buscarYCambiarColorBoton(calendarView);
                                aplicarTextoNegro(calendarView);
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error al inicializar el calendario", e.getMessage());
        }
    }
    
    /**
     * Crea una barra de herramientas personalizada para el calendario
     */
    private void createCustomToolbar() {
        try {
            // Crear botones con estilos modernos
            Button refreshButton = new Button("Actualizar");
            refreshButton.setId("calendar-refresh-button");
            refreshButton.getStyleClass().add("modern-button");
            refreshButton.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-font-weight: normal;");
            refreshButton.setPrefWidth(120);
            
            Button addAppointmentButton = new Button("Nueva cita");
            addAppointmentButton.setId("calendar-new-appointment-button");
            addAppointmentButton.getStyleClass().add("modern-button");
            addAppointmentButton.setStyle("-fx-background-color: #4285f4; -fx-text-fill: white; -fx-font-weight: normal;");
            addAppointmentButton.setPrefWidth(120);
            
            Button todayButton = new Button("Hoy");
            todayButton.setId("calendar-today-button");
            todayButton.getStyleClass().add("modern-button");
            todayButton.setStyle("-fx-background-color: #34a853; -fx-text-fill: white; -fx-font-weight: normal;");
            todayButton.setPrefWidth(120);
            
            // Agregar eventos a los botones
            refreshButton.setOnAction(e -> refreshCalendarFromDatabase());
            addAppointmentButton.setOnAction(e -> showNewAppointmentDialog());
            todayButton.setOnAction(e -> calendarView.setDate(LocalDate.now()));
            
            // Crear contenedor para los botones con estilo fijo
            HBox toolBar = new HBox(10, todayButton, refreshButton, addAppointmentButton);
            toolBar.setId("calendar-toolbar");
            toolBar.setPadding(new Insets(10));
            toolBar.setStyle("-fx-background-color: #f8f9fa;");
            
            // Agregar espacio flexible para alinear los botones a la izquierda
            HBox.setHgrow(toolBar, Priority.ALWAYS);
            
            // Agregar la barra de herramientas en la parte superior
            setTop(toolBar);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Muestra un diálogo para crear una nueva cita
     */
    private void showNewAppointmentDialog() {
        Entry<String> entry = new Entry<>("Nueva cita");
        
        // Configurar la fecha y hora para la próxima hora completa
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextHour = now.withMinute(0).withSecond(0).plusHours(1);
        entry.setInterval(ZonedDateTime.of(nextHour, ZoneId.systemDefault()), 
                          ZonedDateTime.of(nextHour.plusHours(1), ZoneId.systemDefault()));
        
        // Configurar el calendario por defecto (citas pendientes)
        entry.setCalendar(calendars.get(0));
        
        // Mostrar el diálogo de detalles
        showEntryDetailsDialog(entry);
    }
    
    /**
     * Carga las citas desde la base de datos
     */
    private void loadAppointmentsFromDatabase() {
        try {
            System.out.println("Cargando citas desde la base de datos...");
            
            // Limpiar todas las entradas existentes
            for (Calendar calendar : calendars) {
                calendar.clear();
            }
            
            List<CalendarEvent> events;
            
            // Cargar citas según el rol del usuario
            if (usuarioActual != null) {
                String username = usuarioActual.getUsuario();
                com.example.pruebamongodbcss.Data.Usuario.Rol rol = usuarioActual.getRol();
                
                if (rol != null && (rol == com.example.pruebamongodbcss.Data.Usuario.Rol.ADMINISTRADOR || 
                                    rol == com.example.pruebamongodbcss.Data.Usuario.Rol.AUXILIAR)) {
                    // Administradores y auxiliares ven todas las citas
                    events = calendarService.getAllAppointments();
                } else {
                    // Usuarios normales solo ven sus propias citas
                    events = calendarService.getAppointmentsByUser(username);
                }
            } else {
                // Si no hay usuario, mostrar todas las citas
                events = calendarService.getAllAppointments();
            }
            
            System.out.println("Se encontraron " + events.size() + " citas.");
            
            // Convertir eventos a entradas del calendario
            for (CalendarEvent event : events) {
                // Determinar en qué calendario va según el estado o tipo
                Calendar targetCalendar = getTargetCalendar(event);
                
                Entry<String> entry = new Entry<>(event.getTitle());
                entry.setId(event.getId());
                entry.setLocation(event.getLocation());
                
                // Convertir fechas de String a LocalDateTime
                LocalDateTime startDateTime = parseDateTime(event.getStart());
                LocalDateTime endDateTime = parseDateTime(event.getEnd());
                
                if (startDateTime != null && endDateTime != null) {
                    entry.changeStartDate(startDateTime.toLocalDate());
                    entry.changeStartTime(startDateTime.toLocalTime());
                    entry.changeEndDate(endDateTime.toLocalDate());
                    entry.changeEndTime(endDateTime.toLocalTime());
                    
                    // Agregar notas si hay descripción
                    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                        entryDescriptions.put(entry.getId(), event.getDescription());
                    }
                    
                    // Agregar al calendario correspondiente
                    targetCalendar.addEntry(entry);
                }
            }
            
            System.out.println("Citas cargadas con éxito.");
            refreshCalendar();
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error al cargar citas", "No se pudieron cargar las citas: " + e.getMessage());
        }
    }
    
    /**
     * Determina el calendario destino para un evento según su estado o tipo
     */
    private Calendar getTargetCalendar(CalendarEvent event) {
        // Verificar eventType primero (reuniones y recordatorios)
        if (event.getEventType() != null) {
            String eventType = event.getEventType().toLowerCase();
            if (eventType.contains("meeting") || eventType.contains("reunion")) {
                return calendars.get(4); // Reuniones
            } else if (eventType.contains("reminder") || eventType.contains("recordatorio")) {
                return calendars.get(5); // Recordatorios
            }
        }
        
        // Si no es un evento, usar el estado
        if (event.getEstado() != null) {
            String estado = event.getEstado().toUpperCase();
            switch (estado) {
                case "PENDIENTE":
                    return calendars.get(0); // Citas pendientes
                case "EN_CURSO":
                    return calendars.get(1); // Citas en curso
                case "COMPLETADA":
                    return calendars.get(2); // Citas completadas
                case "CANCELADA":
                    return calendars.get(3); // Citas canceladas
                default:
                    return calendars.get(0); // Por defecto, pendientes
            }
        } else if (event.getType() != null) {
            // Usar el tipo como respaldo
            String type = event.getType().toLowerCase();
            if (type.contains("urgent")) {
                return calendars.get(1); // Citas en curso
            } else if (type.contains("completed")) {
                return calendars.get(2); // Citas completadas
            } else if (type.contains("cancelled")) {
                return calendars.get(3); // Citas canceladas
            }
        }
        
        // Por defecto, pendientes
        return calendars.get(0);
    }
    
    /**
     * Convierte una cadena de fecha ISO a LocalDateTime
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return LocalDateTime.now();
        }
        
        try {
            // Manejar tanto formato ISO completo como sin zona horaria
            String cleanedStr = dateTimeStr.replace("Z", "");
            if (cleanedStr.contains("T")) {
                return LocalDateTime.parse(cleanedStr);
            } else {
                // Si no tiene formato ISO con T, intentar interpretar como fecha local
                return LocalDateTime.parse(cleanedStr);
            }
        } catch (Exception e) {
            System.err.println("Error al parsear fecha: " + dateTimeStr + " - " + e.getMessage());
            return LocalDateTime.now();
        }
    }
    
    /**
     * Refresca los datos del calendario desde la base de datos
     */
    public void refreshCalendarFromDatabase() {
        loadAppointmentsFromDatabase();
    }
    
    /**
     * Aplica los estilos CSS por defecto del calendario
     */
    private void applyCalendarStyles() {
        // Asegurarse de que estamos usando los estilos por defecto de CalendarFX
        if (!calendarView.getStylesheets().contains(CALENDAR_DEFAULT_CSS)) {
            calendarView.getStylesheets().add(CALENDAR_DEFAULT_CSS);
        }
        
        // Aplicar estilos básicos
        this.setStyle("-fx-background-color: white;");
    }
    
    /**
     * Aplica un tema básico independiente del ThemeManager
     */
    private void applyTheme() {
        // Aplicar un tema claro fijo, sin usar el ThemeManager
        setStyle("-fx-background-color: white;");
        calendarView.setStyle("-fx-background-color: white;");
        
        // Aplicar estilo de texto negro directamente a través de CSS inline
        String blackTextStyle = "-fx-text-fill: black !important;";
        calendarView.setStyle(calendarView.getStyle() + "; " + blackTextStyle);
        
        // Programar múltiples intentos para buscar y modificar los componentes
        programarMultiplesIntentos();
    }
    
    /**
     * Programa múltiples intentos para modificar los componentes críticos
     */
    private void programarMultiplesIntentos() {
        // Ejecutar varias veces con diferentes retrasos para asegurar que capturamos todos los componentes
        for (int i = 0; i < 10; i++) {
            final int delay = 500 * (i + 1);
            new Thread(() -> {
                try {
                    Thread.sleep(delay);
                    Platform.runLater(() -> {
                        buscarYModificarComponentes(calendarView);
                        aplicarTextoNegro(calendarView);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    /**
     * Busca y modifica botones y otros componentes 
     */
    private void buscarYModificarComponentes(Node nodo) {
        try {
            // Modificar según el tipo de componente
            if (nodo instanceof Button) {
                Button btn = (Button) nodo;
                
                // Aplicar texto negro a todos los botones
                btn.setTextFill(Color.BLACK);
                
                // Si es botón de impresión, modificar directamente
                if (btn.getText() != null && 
                    (btn.getText().equals("Print") || btn.getText().equals("Imprimir"))) {
                    
                    // Crear un fondo rojo sólido
                    javafx.scene.layout.Background redBackground = new javafx.scene.layout.Background(
                        new javafx.scene.layout.BackgroundFill(
                            javafx.scene.paint.Color.web("#EA4335"), 
                            new javafx.scene.layout.CornerRadii(3), 
                            javafx.geometry.Insets.EMPTY
                        )
                    );
                    
                    // Aplicar el fondo y otros estilos directamente
                    btn.setBackground(redBackground);
                    btn.setTextFill(javafx.scene.paint.Color.WHITE);
                    btn.setBorder(null);
                    btn.setEffect(null);
                    
                    System.out.println("🖨️ Botón de impresión modificado: " + btn.getText());
                }
            } 
            // Si es la barra de búsqueda
            else if (nodo instanceof TextField) {
                TextField searchField = (TextField) nodo;
                searchField.setStyle(searchField.getStyle() + "; -fx-text-fill: black;");
                
                if (searchField.getPromptText() != null && 
                    (searchField.getPromptText().contains("Search") || 
                     searchField.getPromptText().contains("Buscar"))) {
                    
                    searchField.setBackground(new javafx.scene.layout.Background(
                        new javafx.scene.layout.BackgroundFill(
                            javafx.scene.paint.Color.WHITE, 
                            new javafx.scene.layout.CornerRadii(3), 
                            javafx.geometry.Insets.EMPTY
                        )
                    ));
                    searchField.setBorder(new javafx.scene.layout.Border(
                        new javafx.scene.layout.BorderStroke(
                            javafx.scene.paint.Color.LIGHTGRAY,
                            javafx.scene.layout.BorderStrokeStyle.SOLID,
                            new javafx.scene.layout.CornerRadii(3),
                            new javafx.scene.layout.BorderWidths(1)
                        )
                    ));
                    
                    System.out.println("🔍 Barra de búsqueda modificada");
                }
            }
            // Si es cualquier etiqueta, aplicar texto negro
            else if (nodo instanceof Label) {
                ((Label) nodo).setTextFill(Color.BLACK);
            }
            // Si es un texto, aplicar color negro
            else if (nodo instanceof Text) {
                ((Text) nodo).setFill(Color.BLACK);
            }
            
            // Buscar recursivamente en todos los hijos
            if (nodo instanceof Parent) {
                Parent parent = (Parent) nodo;
                for (Node hijo : parent.getChildrenUnmodifiable()) {
                    buscarYModificarComponentes(hijo);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al modificar componentes: " + e.getMessage());
        }
    }
    
    /**
     * Configura aspectos visuales adicionales del calendario
     */
    private void configureVisualSettings() {
        // IDs únicos para cada página
        calendarView.getDayPage().setId("calendar-day-page");
        calendarView.getWeekPage().setId("calendar-week-page");
        calendarView.getMonthPage().setId("calendar-month-page");
        calendarView.getYearPage().setId("calendar-year-page");
        
        // Deshabilitar botones predeterminados que reemplazamos con los nuestros
        calendarView.setShowAddCalendarButton(false);
        
        // Asegurarnos de que el botón de impresión está habilitado y visible
        calendarView.setShowPrintButton(true);
        //calendarView.getCalendarSources().get(0).getCalendars().get(0).getStyle();
        
        // Mantener controles útiles
        calendarView.setShowPageToolBarControls(true);
        calendarView.setShowSourceTrayButton(true);
        
        // Configurar colores personalizados para algunas vistas
        calendarView.getWeekPage().setStyle("-fx-background-color: #f8f9fa;");
        calendarView.getDayPage().setStyle("-fx-background-color: #f8f9fa;");
        
        // Aplicar estilo de texto negro a las páginas principales
        String blackTextStyle = "-fx-text-fill: black !important;";
        calendarView.getDayPage().setStyle(calendarView.getDayPage().getStyle() + "; " + blackTextStyle);
        calendarView.getWeekPage().setStyle(calendarView.getWeekPage().getStyle() + "; " + blackTextStyle);
        calendarView.getMonthPage().setStyle(calendarView.getMonthPage().getStyle() + "; " + blackTextStyle);
        calendarView.getYearPage().setStyle(calendarView.getYearPage().getStyle() + "; " + blackTextStyle);
        
        // Programar búsqueda del botón de impresión y forzar su color
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {  // 10 intentos
                try {
                    Thread.sleep(500 * (i + 1));  // Incrementar el tiempo entre intentos
                    Platform.runLater(() -> {
                        buscarYCambiarColorBoton(calendarView);
                        aplicarTextoNegro(calendarView);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    /**
     * Método simple para cambiar el color del botón de impresión
     */
    private void buscarYCambiarColorBoton(Node node) {
        if (node instanceof Button) {
            Button boton = (Button) node;
            // Verificar si es el botón de impresión
            if (boton.getText() != null && 
                (boton.getText().equals("Print") || boton.getText().equals("Imprimir"))) {
                
                // Cambiar el color directamente (sin CSS)
                try {
                    boton.setBackground(new javafx.scene.layout.Background(
                        new javafx.scene.layout.BackgroundFill(
                            javafx.scene.paint.Color.web("#EA4335"), 
                            new javafx.scene.layout.CornerRadii(3), 
                            javafx.geometry.Insets.EMPTY
                        )
                    ));
                    
                    // Cambiar color del texto
                    boton.setTextFill(javafx.scene.paint.Color.WHITE);
                    
                    System.out.println("Botón de impresión modificado");
                } catch (Exception e) {
                    // Si falla, probar con setStyle
                    boton.setStyle("-fx-background-color: #EA4335; -fx-text-fill: white;");
                }
            }
        }
        
        // Revisar hijos recursivamente
        if (node instanceof Parent) {
            for (Node hijo : ((Parent) node).getChildrenUnmodifiable()) {
                buscarYCambiarColorBoton(hijo);
            }
        }
    }
    
    /**
     * Muestra un diálogo para editar los detalles de una entrada
     */
    private void showEntryDetailsDialog(Entry<?> entry) {
        try {
            // Convertir Entry a CalendarEvent para determinar el tipo
            CalendarEvent event = entryToCalendarEvent(entry);
            
            // Decidir qué formulario mostrar según el tipo de evento
            if (calendarService.esReunion(event) || calendarService.esRecordatorio(event)) {
                // Si es reunión o recordatorio, mostrar el formulario de eventos
                showEventoFormulario(event, entry);
            } else {
                // Si es cita médica, mostrar el formulario de citas
                showCitaFormulario(event, entry);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error", "No se pudo procesar la cita: " + e.getMessage());
        }
    }
    
    /**
     * Convierte un Entry a CalendarEvent
     */
    private CalendarEvent entryToCalendarEvent(Entry<?> entry) {
        CalendarEvent event = new CalendarEvent();
        
        // Asignar ID si lo tiene
        event.setId(entry.getId());
        
        // Datos básicos
        event.setTitle(entry.getTitle());
        event.setLocation(entry.getLocation());
        
        // Fechas
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime startDateTime = LocalDateTime.of(entry.getStartDate(), entry.getStartTime());
        LocalDateTime endDateTime = LocalDateTime.of(entry.getEndDate(), entry.getEndTime());
        event.setStart(startDateTime.format(formatter));
        event.setEnd(endDateTime.format(formatter));
        
        // Descripción
        if (entry.getId() != null && entryDescriptions.containsKey(entry.getId())) {
            event.setDescription(entryDescriptions.get(entry.getId()));
        }
        
        // Determinar tipo de evento según el calendario
        if (entry.getCalendar() == calendars.get(0)) {
            event.setEstado("PENDIENTE");
            event.setType("default");
            event.setTipoEvento(CalendarEvent.EventoTipo.CITA_MEDICA);
        } else if (entry.getCalendar() == calendars.get(1)) {
            event.setEstado("EN_CURSO");
            event.setType("urgent");
            event.setTipoEvento(CalendarEvent.EventoTipo.CITA_MEDICA);
        } else if (entry.getCalendar() == calendars.get(2)) {
            event.setEstado("COMPLETADA");
            event.setType("completed");
            event.setTipoEvento(CalendarEvent.EventoTipo.CITA_MEDICA);
        } else if (entry.getCalendar() == calendars.get(3)) {
            event.setEstado("CANCELADA");
            event.setType("cancelled");
            event.setTipoEvento(CalendarEvent.EventoTipo.CITA_MEDICA);
        } else if (entry.getCalendar() == calendars.get(4)) {
            event.setTipoEvento(CalendarEvent.EventoTipo.REUNION);
        } else if (entry.getCalendar() == calendars.get(5)) {
            event.setTipoEvento(CalendarEvent.EventoTipo.RECORDATORIO);
        }
        
        return event;
    }
    
    /**
     * Muestra el formulario para citas médicas
     */
    private void showCitaFormulario(CalendarEvent event, Entry<?> entry) {
        try {
            // Si la entrada ya está en un calendario, eliminarla temporalmente
            Calendar originalCalendar = entry.getCalendar();
            if (originalCalendar != null) {
                originalCalendar.removeEntry(entry);
            }
            
            // Cargar el formulario de citas desde la ubicación correcta
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/Citas/cita-formulario.fxml"));
            Parent root = loader.load();
            
            // Obtener el controlador
            com.example.pruebamongodbcss.Modulos.Clinica.Citas.CitaFormularioController controller = loader.getController();
            
            // Configurar el controlador con el servicio de clínica
            com.example.pruebamongodbcss.Modulos.Clinica.ServicioClinica servicioClinica = new com.example.pruebamongodbcss.Modulos.Clinica.ServicioClinica();
            controller.setServicio(servicioClinica);
            
            // Configurar callback para refrescar el calendario
            controller.setCitaGuardadaCallback(() -> {
                refreshCalendarFromDatabase();
            });
            
            // Si es edición, convertir el evento a cita y pasarla al controlador
            if (event.getId() != null && !event.getId().isEmpty()) {
                com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita cita = calendarEventToModeloCita(event);
                if (cita != null) {
                    controller.setCita(cita);
                }
            }
            
            // Mostrar el formulario en una ventana modal
            Stage stage = new Stage();
            stage.setTitle("Cita Médica");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Refrescar el calendario después de cerrar, independientemente del resultado
            refreshCalendarFromDatabase();
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error", "No se pudo abrir el formulario de citas: " + e.getMessage());
            // Refrescar de todos modos para asegurar consistencia
            refreshCalendarFromDatabase();
        }
    }
    
    /**
     * Muestra el formulario para reuniones y recordatorios
     */
    private void showEventoFormulario(CalendarEvent event, Entry<?> entry) {
        try {
            // Si la entrada ya está en un calendario, eliminarla temporalmente
            Calendar originalCalendar = entry.getCalendar();
            if (originalCalendar != null) {
                originalCalendar.removeEntry(entry);
            }
            
            // Cargar el formulario de eventos
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/calendar/evento-formulario.fxml"));
            Parent root = loader.load();
            
            // Obtener el controlador
            EventoFormularioController controller = loader.getController();
            
            // Configurar el controlador
            controller.setServicio(calendarService);
            
            // Configurar callback para refrescar el calendario
            controller.setEventoGuardadoCallback(() -> {
                refreshCalendarFromDatabase();
            });
            
            // Si es edición, pasar el evento
            if (event.getId() != null && !event.getId().isEmpty()) {
                controller.setEvento(event);
            }
            
            // Mostrar el formulario en una ventana modal
            Stage stage = new Stage();
            stage.setTitle(event.getTipoEvento() == CalendarEvent.EventoTipo.REUNION ? "Reunión" : "Recordatorio");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Refrescar el calendario después de cerrar, independientemente del resultado
            refreshCalendarFromDatabase();
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error", "No se pudo abrir el formulario de eventos: " + e.getMessage());
            // Refrescar de todos modos para asegurar consistencia
            refreshCalendarFromDatabase();
        }
    }
    
    /**
     * Convierte un CalendarEvent a ModeloCita para el formulario de citas
     */
    private com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita calendarEventToModeloCita(CalendarEvent event) {
        try {
            com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita cita = 
                new com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita();
            
            // ID (quitar el prefijo _ si lo tiene)
            if (event.getId() != null && event.getId().startsWith("_")) {
                cita.setId(new ObjectId(event.getId().substring(1)));
            } else if (event.getId() != null && !event.getId().isEmpty()) {
                cita.setId(new ObjectId(event.getId()));
            }
            
            // Fechas
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            cita.setFechaHora(LocalDateTime.parse(event.getStart(), formatter));
            
            // Datos básicos
            cita.setMotivo(event.getTitle());
            cita.setObservaciones(event.getDescription());
            
            // Estado
            if (event.getEstado() != null) {
                String estadoStr = event.getEstado().toUpperCase();
                try {
                    cita.setEstado(com.example.pruebamongodbcss.Data.EstadoCita.valueOf(estadoStr));
                } catch (Exception e) {
                    // Estado por defecto
                    cita.setEstado(com.example.pruebamongodbcss.Data.EstadoCita.PENDIENTE);
                }
            } else {
                // Estado por defecto
                cita.setEstado(com.example.pruebamongodbcss.Data.EstadoCita.PENDIENTE);
            }
            
            // Veterinario
            cita.setNombreVeterinario(event.getUsuario());
            
            // Para citas médicas, el usuario asignado es el veterinario de la cita
            // IMPORTANTE: Esto solo establece el valor inicial, el formulario lo actualizará
            // cuando el usuario seleccione un veterinario
            if (event.getUsuario() != null && !event.getUsuario().isEmpty()) {
                // Extraer el nombre de usuario del nombre del veterinario
                String nombreVeterinario = event.getUsuario();
                // Intentamos extraer el username del formato "Dr. Nombre Apellido"
                if (nombreVeterinario.startsWith("Dr. ") || nombreVeterinario.startsWith("Dra. ")) {
                    nombreVeterinario = nombreVeterinario.substring(4);
                }
                // Convertir a formato de usuario simple (primera letra nombre + apellido en minúsculas)
                String[] partes = nombreVeterinario.split(" ");
                if (partes.length >= 2) {
                    String posibleUsuario = (partes[0].substring(0, 1) + partes[1]).toLowerCase();
                    cita.setUsuarioAsignado(posibleUsuario);
                } else {
                    cita.setUsuarioAsignado(nombreVeterinario.toLowerCase());
                }
            } 
            // Si no hay veterinario, usamos el usuario actual
            else if (usuarioActual != null) {
                cita.setUsuarioAsignado(usuarioActual.getUsuario());
            }
            // Valor por defecto como último recurso
            else {
                cita.setUsuarioAsignado("sistema");
            }
            
            return cita;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Crea una nueva entrada para el calendario
     */
    private Entry<?> createNewEntry(DateControl.CreateEntryParameter param) {
        Entry<String> entry = new Entry<>("Nueva cita");
        
        // Configurar la fecha según el parámetro
        entry.setInterval(param.getZonedDateTime(), param.getZonedDateTime().plusHours(1));
        
        // Determinar el tipo de evento según dónde se hizo clic
        // Si se hizo clic en los calendarios de reuniones o recordatorios, usar ese tipo
        // De lo contrario, usar cita médica (predeterminado)
        if (param.getCalendar() == calendars.get(4)) {
            // Si se hizo clic en el calendario de reuniones
            entry.setCalendar(calendars.get(4));
        } else if (param.getCalendar() == calendars.get(5)) {
            // Si se hizo clic en el calendario de recordatorios
            entry.setCalendar(calendars.get(5));
        } else {
            // Si se hizo clic en cualquier otro calendario o en un área vacía, cita pendiente
            entry.setCalendar(calendars.get(0));
        }
        
        // Mostrar el diálogo de detalles sin añadir la entrada al calendario permanentemente
        // Se añadirá solo si el usuario guarda correctamente
        
        // Importante: No añadimos la entrada al calendario aquí, lo hacemos si se guarda correctamente
        // Si la entrada ya está en un calendario, la quitamos temporalmente
        Calendar calendar = entry.getCalendar();
        if (calendar != null) {
            calendar.removeEntry(entry);
        }
        
        Platform.runLater(() -> showEntryDetailsDialog(entry));
        
        // Retornamos la entrada creada pero sin agregarla al calendario
        return entry;
    }
    
    /**
     * Configura los manejadores de eventos para el calendario
     */
    private void configureCalendarHandlers() {
        // Manejar evento de creación de nueva entrada
        calendarView.setEntryDetailsCallback(entryDetailsParameter -> {
            Entry<?> entry = entryDetailsParameter.getEntry();
            
            // Asegurarse de que la entrada ya esté agregada a un calendario
            if (entry.getCalendar() == null) {
                // Agregar a calendario por defecto (primer calendario)
                entry.setCalendar(calendars.get(0));
            }
            
            showEntryDetailsDialog(entry);
            return null;
        });
        
        // Menú contextual para clic derecho sobre una entrada (cita)
        calendarView.setEntryContextMenuCallback(param -> {
            Entry<?> entry = param.getEntry();
            ContextMenu contextMenu = new ContextMenu();
            
            MenuItem editItem = new MenuItem("Editar cita");
            MenuItem deleteItem = new MenuItem("Eliminar cita");
            
            // Configurar acción de edición
            editItem.setOnAction(e -> {
                showEntryDetailsDialog(entry);
            });
            
            // Configurar acción de eliminación
            deleteItem.setOnAction(e -> {
                // Confirmar eliminación
                Alert confirmDialog = new Alert(AlertType.CONFIRMATION);
                confirmDialog.setTitle("Eliminar cita");
                confirmDialog.setHeaderText("¿Está seguro que desea eliminar esta cita?");
                confirmDialog.setContentText("Esta acción no se puede deshacer.");
                
                Optional<ButtonType> result = confirmDialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    // Obtener ID de la cita
                    String entryId = entry.getId();
                    
                    // Eliminar de la BD
                    boolean deleted = calendarService.deleteAppointment(entryId);
                    
                    if (deleted) {
                        // Eliminar del calendario visual
                        Calendar calendar = entry.getCalendar();
                        if (calendar != null) {
                            calendar.removeEntry(entry);
                        }
                        
                        // Refrescar la vista
                        refreshCalendarFromDatabase();
                        
                        // Mostrar mensaje de éxito
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Éxito");
                        alert.setHeaderText(null);
                        alert.setContentText("La cita ha sido eliminada correctamente.");
                        alert.showAndWait();
                    } else {
                        // Mostrar error
                        showErrorMessage("Error", "No se pudo eliminar la cita. Por favor, inténtelo de nuevo.");
                        // Refrescar de todos modos para asegurar consistencia
                        refreshCalendarFromDatabase();
                    }
                }
            });
            
            // Agregar opciones al menú
            contextMenu.getItems().addAll(editItem, deleteItem);
            
            return contextMenu;
        });
        
        // Agregar menú contextual para clic derecho en un día
        calendarView.getDayPage().setContextMenuCallback(new Callback<DateControl.ContextMenuParameter, ContextMenu>() {
            @Override
            public ContextMenu call(DateControl.ContextMenuParameter param) {
                ContextMenu contextMenu = new ContextMenu();
                
                MenuItem newAppointmentItem = new MenuItem("Nueva cita médica");
                MenuItem newMeetingItem = new MenuItem("Nueva reunión");
                MenuItem newReminderItem = new MenuItem("Nuevo recordatorio");
                
                // Configurar acciones
                newAppointmentItem.setOnAction(e -> {
                    Entry<String> entry = new Entry<>("Nueva cita médica");
                    entry.setInterval(param.getZonedDateTime(), param.getZonedDateTime().plusHours(1));
                    entry.setCalendar(calendars.get(0)); // Pendientes
                    showEntryDetailsDialog(entry);
                });
                
                newMeetingItem.setOnAction(e -> {
                    Entry<String> entry = new Entry<>("Nueva reunión");
                    entry.setInterval(param.getZonedDateTime(), param.getZonedDateTime().plusHours(1));
                    entry.setCalendar(calendars.get(4)); // Reuniones
                    showEntryDetailsDialog(entry);
                });
                
                newReminderItem.setOnAction(e -> {
                    Entry<String> entry = new Entry<>("Nuevo recordatorio");
                    entry.setInterval(param.getZonedDateTime(), param.getZonedDateTime().plusHours(1));
                    entry.setCalendar(calendars.get(5)); // Recordatorios
                    showEntryDetailsDialog(entry);
                });
                
                // Agregar items al menú
                contextMenu.getItems().addAll(newAppointmentItem, newMeetingItem, newReminderItem);
                
                return contextMenu;
            }
        });
    }
    
    /**
     * Muestra un mensaje de error
     */
    private void showErrorMessage(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    /**
     * Método para desconectar este componente del ThemeManager
     * recorriendo todos los nodos y aplicando estilos directos
     */
    private void desconectarDelThemeManager(Node nodo) {
        try {
            // Desconectar nodo actual
            if (nodo != null) {
                // Aplicar estilos directos según el tipo de nodo
                if (nodo instanceof Button) {
                    Button btn = (Button) nodo;
                    
                    // Aplicar texto negro por defecto a botones
                    btn.setTextFill(Color.BLACK);
                    
                    // Si es botón de impresión, forzar estilo rojo
                    if (btn.getText() != null && 
                        (btn.getText().equals("Print") || btn.getText().equals("Imprimir"))) {
                        
                        btn.setBackground(new javafx.scene.layout.Background(
                            new javafx.scene.layout.BackgroundFill(
                                javafx.scene.paint.Color.web("#EA4335"), 
                                new javafx.scene.layout.CornerRadii(3), 
                                javafx.geometry.Insets.EMPTY
                            )
                        ));
                        btn.setTextFill(javafx.scene.paint.Color.WHITE);
                    }
                } 
                // Si es barra de búsqueda
                else if (nodo instanceof TextField) {
                    TextField searchField = (TextField) nodo;
                    searchField.setStyle(searchField.getStyle() + "; -fx-text-fill: black;");
                    
                    if (searchField.getPromptText() != null && 
                        (searchField.getPromptText().contains("Search") || 
                         searchField.getPromptText().contains("Buscar"))) {
                        
                        searchField.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-prompt-text-fill: #999; -fx-text-fill: black;");
                    }
                }
                // Si es una etiqueta o texto, forzar color negro
                else if (nodo instanceof Label) {
                    ((Label) nodo).setTextFill(Color.BLACK);
                }
                else if (nodo instanceof Text) {
                    ((Text) nodo).setFill(Color.BLACK);
                }
            }
            
            // Recursivamente desconectar todos los hijos
            if (nodo instanceof Parent) {
                Parent parent = (Parent) nodo;
                for (Node hijo : parent.getChildrenUnmodifiable()) {
                    desconectarDelThemeManager(hijo);
                }
            }
        } catch (Exception e) {
            System.err.println("Error en desconectarDelThemeManager: " + e.getMessage());
        }
    }
    
    /**
     * Aplica color negro a todos los textos del calendario
     */
    private void aplicarTextoNegro(Node nodo) {
        try {
            // Aplicar estilo de texto negro a todos los nodos de texto
            if (nodo instanceof Text) {
                ((Text) nodo).setFill(Color.BLACK);
            } else if (nodo instanceof Label) {
                ((Label) nodo).setTextFill(Color.BLACK);
            } else if (nodo instanceof Button) {
                ((Button) nodo).setTextFill(Color.BLACK);
            } else if (nodo instanceof TextField) {
                ((TextField) nodo).setStyle(((TextField) nodo).getStyle() + "; -fx-text-fill: black;");
            } else if (nodo instanceof Control) {
                ((Control) nodo).setStyle(((Control) nodo).getStyle() + "; -fx-text-fill: black;");
            }
            
            // Recorrer recursivamente todos los nodos hijos
            if (nodo instanceof Parent) {
                for (Node hijo : ((Parent) nodo).getChildrenUnmodifiable()) {
                    aplicarTextoNegro(hijo);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al aplicar texto negro: " + e.getMessage());
        }
    }
    
    /**
     * Refresca el calendario
     */
    public void refreshCalendar() {
        if (calendarView != null) {
            calendarView.refreshData();
        }
    }
    
    /**
     * Obtiene la vista principal del calendario
     * 
     * @return La vista del calendario
     */
    public CalendarView getCalendarView() {
        return calendarView;
    }
}