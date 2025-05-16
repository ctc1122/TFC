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
import com.example.pruebamongodbcss.theme.ThemeManager;

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
            
            // Forzar el estilo predeterminado de CalendarFX
            applyCalendarStyles();
            
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
            refreshButton.getStyleClass().add("modern-button");
            refreshButton.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white;");
            refreshButton.setPrefWidth(120);
            
            Button addAppointmentButton = new Button("Nueva cita");
            addAppointmentButton.getStyleClass().add("modern-button");
            addAppointmentButton.setStyle("-fx-background-color: #4285f4; -fx-text-fill: white;");
            addAppointmentButton.setPrefWidth(120);
            
            Button todayButton = new Button("Hoy");
            todayButton.getStyleClass().add("modern-button");
            todayButton.setStyle("-fx-background-color: #34a853; -fx-text-fill: white;");
            todayButton.setPrefWidth(120);
            
            // Agregar eventos a los botones
            refreshButton.setOnAction(e -> refreshCalendarFromDatabase());
            addAppointmentButton.setOnAction(e -> showNewAppointmentDialog());
            todayButton.setOnAction(e -> calendarView.setDate(LocalDate.now()));
            
            // Crear contenedor para los botones
            HBox toolBar = new HBox(10, todayButton, refreshButton, addAppointmentButton);
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
        try {
            // Asegurarse de que estamos usando los estilos por defecto de CalendarFX
            if (!calendarView.getStylesheets().contains(CALENDAR_DEFAULT_CSS)) {
                calendarView.getStylesheets().add(CALENDAR_DEFAULT_CSS);
            }
            
            // Aplicar estilos modernos generales
            calendarView.setStyle("-fx-background-color: white;");
            this.setStyle("-fx-background-color: white;");
            
            // Aplicar tema claro/oscuro según configuración
            applyTheme();
            
            // Aproximación directa: buscar el botón de impresión después de un breve delay
            Platform.runLater(() -> {
                new Thread(() -> {
                    try {
                        // Esperar a que se inicialice la UI completamente
                        Thread.sleep(500);
                        
                        // Volver al hilo de la UI para modificar el botón
                        Platform.runLater(() -> {
                            stylePrintButton(calendarView);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            });
        } catch (Exception e) {
            System.err.println("Error al aplicar estilos: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Método simplificado para buscar y estilizar el botón de impresión
     */
    private void stylePrintButton(Node parent) {
        try {
            // Recorrer todos los nodos hijos recursivamente
            if (parent instanceof Parent) {
                for (Node child : ((Parent) parent).getChildrenUnmodifiable()) {
                    // Verificar si es un botón
                    if (child instanceof Button) {
                        Button button = (Button) child;
                        
                        // Si tiene el texto "Print" o "Imprimir", o tiene la clase "print-button"
                        if ((button.getText() != null && 
                             (button.getText().equals("Print") || button.getText().equals("Imprimir"))) || 
                            button.getStyleClass().contains("print-button")) {
                            
                            // Aplicar estilo directamente
                            button.setStyle("-fx-background-color: #EA4335; -fx-text-fill: white;");
                            System.out.println("Botón de impresión estilizado correctamente");
                        }
                    }
                    
                    // Buscar recursivamente en los hijos
                    stylePrintButton(child);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al estilizar botón de impresión: " + e.getMessage());
        }
    }
    
    /**
     * Aplica el tema actual (claro/oscuro)
     */
    private void applyTheme() {
        boolean isDarkTheme = ThemeManager.getInstance().isDarkTheme();
        
        if (isDarkTheme) {
            setStyle("-fx-background-color: #202124;");
            calendarView.setStyle("-fx-background-color: #202124;");
        } else {
            setStyle("-fx-background-color: white;");
            calendarView.setStyle("-fx-background-color: white;");
        }
    }
    
    /**
     * Configura aspectos visuales adicionales del calendario
     */
    private void configureVisualSettings() {
        // Deshabilitar botones predeterminados que reemplazamos con los nuestros
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowPrintButton(true); // Habilitar botón de impresión nativo
        
        // Mantener controles útiles
        calendarView.setShowPageToolBarControls(true);
        calendarView.setShowSourceTrayButton(true);
        
        // Configurar colores personalizados para algunas vistas
        calendarView.getWeekPage().setStyle("-fx-background-color: #f8f9fa;");
        calendarView.getDayPage().setStyle("-fx-background-color: #f8f9fa;");
    }
    
    /**
     * Muestra un diálogo para editar los detalles de una entrada
     */
    private void showEntryDetailsDialog(Entry<?> entry) {
        try {
            // Crear un diálogo para editar la cita
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Detalles de Cita");
            
            // Verificar si es edición o creación
            boolean isNewEntry = entry.getCalendar() == null;
            dialog.setHeaderText(isNewEntry ? "Nueva cita" : "Editar cita");
            
            // Configurar botones
            ButtonType saveButtonType = new ButtonType("Guardar", ButtonType.OK.getButtonData());
            ButtonType cancelButtonType = new ButtonType("Cancelar", ButtonType.CANCEL.getButtonData());
            ButtonType deleteButtonType = new ButtonType("Eliminar", ButtonType.CANCEL.getButtonData());
            
            if (isNewEntry) {
                dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
            } else {
                dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType, deleteButtonType);
            }
            
            // Crear formulario
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 20, 10, 20));
            
            // Campos para el formulario
            TextField titleField = new TextField(entry.getTitle());
            titleField.setPromptText("Título");
            
            TextField locationField = new TextField(entry.getLocation());
            locationField.setPromptText("Ubicación");
            
            DatePicker startDatePicker = new DatePicker(entry.getStartDate());
            DatePicker endDatePicker = new DatePicker(entry.getEndDate());
            
            Spinner<Integer> startHourSpinner = new Spinner<>(0, 23, entry.getStartTime().getHour());
            Spinner<Integer> startMinuteSpinner = new Spinner<>(0, 59, entry.getStartTime().getMinute());
            
            Spinner<Integer> endHourSpinner = new Spinner<>(0, 23, entry.getEndTime().getHour());
            Spinner<Integer> endMinuteSpinner = new Spinner<>(0, 59, entry.getEndTime().getMinute());
            
            TextField descriptionField = new TextField();
            if (entry.getId() != null && entryDescriptions.containsKey(entry.getId())) {
                descriptionField.setText(entryDescriptions.get(entry.getId()));
            }
            descriptionField.setPromptText("Descripción");
            
            ComboBox<Calendar> calendarComboBox = new ComboBox<>();
            calendarComboBox.getItems().addAll(calendars);
            calendarComboBox.setValue(entry.getCalendar() != null ? entry.getCalendar() : calendars.get(0));
            
            // Configuración visual de los spinners
            startHourSpinner.setMaxWidth(70);
            startMinuteSpinner.setMaxWidth(70);
            endHourSpinner.setMaxWidth(70);
            endMinuteSpinner.setMaxWidth(70);
            
            // Layouts para hora de inicio/fin
            HBox startTimeBox = new HBox(5, new Label("Hora:"), startHourSpinner, new Label(":"), startMinuteSpinner);
            HBox endTimeBox = new HBox(5, new Label("Hora:"), endHourSpinner, new Label(":"), endMinuteSpinner);
            
            // Añadir campos al grid
            int row = 0;
            grid.add(new Label("Título:"), 0, row);
            grid.add(titleField, 1, row, 2, 1);
            
            row++;
            grid.add(new Label("Ubicación:"), 0, row);
            grid.add(locationField, 1, row, 2, 1);
            
            row++;
            grid.add(new Label("Fecha inicio:"), 0, row);
            grid.add(startDatePicker, 1, row);
            grid.add(startTimeBox, 2, row);
            
            row++;
            grid.add(new Label("Fecha fin:"), 0, row);
            grid.add(endDatePicker, 1, row);
            grid.add(endTimeBox, 2, row);
            
            row++;
            grid.add(new Label("Descripción:"), 0, row);
            grid.add(descriptionField, 1, row, 2, 1);
            
            row++;
            grid.add(new Label("Calendario:"), 0, row);
            grid.add(calendarComboBox, 1, row, 2, 1);
            
            // Hacer los campos más anchos
            titleField.setPrefWidth(300);
            locationField.setPrefWidth(300);
            descriptionField.setPrefWidth(300);
            
            dialog.getDialogPane().setContent(grid);
            
            // Estilos visuales para el diálogo
            dialog.getDialogPane().getStyleClass().add("modern-dialog");
            dialog.getDialogPane().setPrefSize(550, 400);
            
            // Focus al campo de título
            Platform.runLater(titleField::requestFocus);
            
            // Establecer manejadores para los botones
            final Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
            final Button deleteButton = (Button) dialog.getDialogPane().lookupButton(deleteButtonType);
            
            // Aplicar estilos a los botones
            if (saveButton != null) {
                saveButton.getStyleClass().add("modern-button");
                saveButton.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white;");
            }
            
            if (deleteButton != null) {
                deleteButton.getStyleClass().add("modern-button");
                deleteButton.setStyle("-fx-background-color: #ea4335; -fx-text-fill: white;");
            }
            
            // Manejar resultado del diálogo
            Optional<ButtonType> result = dialog.showAndWait();
            
            if (result.isPresent()) {
                if (result.get() == saveButtonType) {
                    // Actualizar la entrada con los valores del formulario
                    entry.setTitle(titleField.getText());
                    entry.setLocation(locationField.getText());
                    
                    // Guardar la descripción
                    String description = descriptionField.getText();
                    if (entry.getId() != null && description != null && !description.isEmpty()) {
                        entryDescriptions.put(entry.getId(), description);
                    }
                    
                    LocalTime startTime = LocalTime.of(
                            startHourSpinner.getValue(), 
                            startMinuteSpinner.getValue());
                    
                    LocalTime endTime = LocalTime.of(
                            endHourSpinner.getValue(), 
                            endMinuteSpinner.getValue());
                    
                    entry.changeStartDate(startDatePicker.getValue());
                    entry.changeStartTime(startTime);
                    entry.changeEndDate(endDatePicker.getValue());
                    entry.changeEndTime(endTime);
                    
                    // Cambiar el calendario si es necesario
                    Calendar selectedCalendar = calendarComboBox.getValue();
                    if (entry.getCalendar() != selectedCalendar) {
                        if (entry.getCalendar() != null) {
                            entry.getCalendar().removeEntry(entry);
                        }
                        entry.setCalendar(selectedCalendar);
                        selectedCalendar.addEntry(entry);
                    }
                    
                    // Guardar en la base de datos
                    saveEntryToDatabase(entry);
                } else if (result.get() == deleteButtonType) {
                    // Eliminar la entrada
                    if (entry.getCalendar() != null) {
                        entry.getCalendar().removeEntry(entry);
                        
                        // Eliminar de la base de datos
                        deleteEntryFromDatabase(entry);
                    }
                }
            }
            
            // Refrescar el calendario
            refreshCalendar();
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error", "No se pudo procesar la cita: " + e.getMessage());
        }
    }
    
    /**
     * Guarda una entrada en la base de datos
     */
    private void saveEntryToDatabase(Entry<?> entry) {
        try {
            // Convertir la entrada a un objeto CalendarEvent
            CalendarEvent event = new CalendarEvent();
            
            // Si ya tiene un ID, usarlo (para actualizaciones)
            if (entry.getId() != null && !entry.getId().isEmpty()) {
                event.setId(entry.getId());
            }
            
            event.setTitle(entry.getTitle());
            event.setLocation(entry.getLocation());
            
            // Convertir LocalDateTime a String ISO
            LocalDateTime startDateTime = LocalDateTime.of(entry.getStartDate(), entry.getStartTime());
            LocalDateTime endDateTime = LocalDateTime.of(entry.getEndDate(), entry.getEndTime());
            
            event.setStart(startDateTime.toString());
            event.setEnd(endDateTime.toString());
            
            // Descripción
            if (entry.getId() != null && entryDescriptions.containsKey(entry.getId())) {
                event.setDescription(entryDescriptions.get(entry.getId()));
            }
            
            // Establecer estado y tipo según el calendario
            if (entry.getCalendar() == calendars.get(0)) {
                event.setEstado("PENDIENTE");
                event.setType("default");
            } else if (entry.getCalendar() == calendars.get(1)) {
                event.setEstado("EN_CURSO");
                event.setType("urgent");
            } else if (entry.getCalendar() == calendars.get(2)) {
                event.setEstado("COMPLETADA");
                event.setType("completed");
            } else if (entry.getCalendar() == calendars.get(3)) {
                event.setEstado("CANCELADA");
                event.setType("cancelled");
            } else if (entry.getCalendar() == calendars.get(4)) {
                event.setEventType("meeting");
            } else if (entry.getCalendar() == calendars.get(5)) {
                event.setEventType("reminder");
            }
            
            // Asignar usuario actual si está disponible
            if (usuarioActual != null) {
                event.setUsuario(usuarioActual.getUsuario());
            }
            
            // Guardar en la base de datos
            if (event.getId() != null && !event.getId().isEmpty()) {
                // Actualizar
                calendarService.updateAppointment(event);
            } else {
                // Crear nuevo
                CalendarEvent savedEvent = calendarService.saveAppointment(event);
                // Actualizar ID en la entrada
                if (savedEvent != null && savedEvent.getId() != null) {
                    entry.setId(savedEvent.getId());
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error al guardar", "No se pudo guardar la cita: " + e.getMessage());
        }
    }
    
    /**
     * Elimina una entrada de la base de datos
     */
    private void deleteEntryFromDatabase(Entry<?> entry) {
        try {
            if (entry.getId() != null && !entry.getId().isEmpty()) {
                calendarService.deleteAppointment(entry.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error al eliminar", "No se pudo eliminar la cita: " + e.getMessage());
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
     * Agrega un nuevo evento al calendario y a la base de datos
     * 
     * @param title Título del evento
     * @param location Ubicación
     * @param startDateTime Fecha y hora de inicio
     * @param endDateTime Fecha y hora de fin
     * @param isUrgent Si es una cita urgente
     */
    public void addEvent(String title, String location, ZonedDateTime startDateTime, 
                         ZonedDateTime endDateTime, boolean isUrgent) {
        
        Calendar targetCalendar = isUrgent ? 
                calendars.get(1) : calendars.get(0);
                
        Entry<String> entry = new Entry<>(title);
        entry.setLocation(location);
        
        entry.changeStartDate(startDateTime.toLocalDate());
        entry.changeStartTime(startDateTime.toLocalTime());
        entry.changeEndDate(endDateTime.toLocalDate());
        entry.changeEndTime(endDateTime.toLocalTime());
        
        targetCalendar.addEntry(entry);
        
        // Guardar en la base de datos
        saveEntryToDatabase(entry);
        
        refreshCalendar();
    }
    
    /**
     * Obtiene la vista principal del calendario
     * 
     * @return La vista del calendario
     */
    public CalendarView getCalendarView() {
        return calendarView;
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
        
        // Agregar menú contextual para clic derecho
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
     * Crea una nueva entrada para el calendario
     */
    private Entry<?> createNewEntry(DateControl.CreateEntryParameter param) {
        Entry<String> entry = new Entry<>("Nueva cita");
        
        // Configurar la fecha según el parámetro
        entry.setInterval(param.getZonedDateTime(), param.getZonedDateTime().plusHours(1));
        
        // Configurar el calendario por defecto (citas normales)
        entry.setCalendar(calendars.get(0));
        
        // Mostrar el diálogo de detalles
        Platform.runLater(() -> showEntryDetailsDialog(entry));
        
        return entry;
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
}