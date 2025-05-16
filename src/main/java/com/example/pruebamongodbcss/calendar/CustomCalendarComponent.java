package com.example.pruebamongodbcss.calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.example.pruebamongodbcss.theme.ThemeManager;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Componente de calendario personalizado que emula la interfaz de Google Calendar
 * pero conectado a una base de datos local
 */
public class CustomCalendarComponent extends BorderPane {
    
    private CalendarView calendarView;
    private Calendar appointmentsCalendar;
    private CalendarSource calendarSource;
    
    // Ruta al archivo CSS del calendario
    private static final String CALENDAR_STYLES_PATH = "/com/example/pruebamongodbcss/theme/calendar-styles.css";
    
    // Listener para cambios de tema
    private ChangeListener<Boolean> themeChangeListener;
    
    /**
     * Constructor
     */
    public CustomCalendarComponent() {
        initialize();
    }
    
    /**
     * Inicializa el componente
     */
    private void initialize() {
        // Configurar el contenedor principal
        setPadding(new Insets(10));
        getStyleClass().add("calendar-container");
        
        // Crear componente de vista de calendario
        calendarView = new CalendarView();
        
        // Aplicar estilos
        String calendarStylesheet = getClass().getResource(CALENDAR_STYLES_PATH).toExternalForm();
        if (calendarStylesheet != null && !calendarView.getStylesheets().contains(calendarStylesheet)) {
            calendarView.getStylesheets().add(calendarStylesheet);
        }
        
        // Configurar el tema actual
        ThemeManager themeManager = ThemeManager.getInstance();
        if (themeManager.isDarkTheme()) {
            calendarView.getStyleClass().add("dark-theme");
            getStyleClass().add("dark-theme");
        } else {
            calendarView.getStyleClass().add("light-theme");
            getStyleClass().add("light-theme");
        }
        
        // Crear calendario para citas
        appointmentsCalendar = new Calendar("Citas");
        appointmentsCalendar.setStyle(Calendar.Style.STYLE1);
        appointmentsCalendar.setReadOnly(false);
        
        // Agregar a la fuente de calendario
        calendarSource = new CalendarSource("Veterinaria");
        calendarSource.getCalendars().add(appointmentsCalendar);
        
        // Agregar la fuente al calendario
        calendarView.getCalendarSources().add(calendarSource);
        
        // Configurar fecha y hora actuales
        calendarView.setToday(LocalDate.now());
        calendarView.setTime(LocalTime.now());
        
        // Permitir la creación y edición de entradas
        calendarView.setEntryEditPolicy(param -> true);
        
        // Personalizar colores y estilos
        personalizarColores();
        
        // Configurar vistas disponibles
        configurarVistas();
        
        // Establecer manejadores de eventos para crear/editar/eliminar citas
        configureEventHandlers();
        
        // Interfaz de usuario con botones para gestión
        VBox controlPanel = createControlPanel();
        
        // Agregar componentes al layout
        setTop(controlPanel);
        setCenter(calendarView);
        
        // Cargar citas de ejemplo (reemplazar con carga desde BD)
        cargarCitasDeEjemplo();
        
        // Iniciar hilo para actualizar el tiempo
        startTimeThread();
        
        // Escuchar cambios de tema
        setupThemeListener();
    }
    
    /**
     * Crea el panel de control con botones
     */
    private VBox createControlPanel() {
        VBox controlPanel = new VBox(10);
        controlPanel.setPadding(new Insets(10));
        
        Label titleLabel = new Label("Calendario de Citas");
        titleLabel.getStyleClass().add("title-label");
        
        Button newAppointmentButton = new Button("Nueva Cita");
        newAppointmentButton.getStyleClass().add("action-button");
        newAppointmentButton.setOnAction(e -> crearNuevaCita());
        
        Button refreshButton = new Button("Actualizar");
        refreshButton.getStyleClass().add("action-button");
        refreshButton.setOnAction(e -> cargarCitasDesdeBaseDeDatos());
        
        Button importCitasButton = new Button("Importar Citas");
        importCitasButton.getStyleClass().add("action-button");
        importCitasButton.setOnAction(e -> importarCitasDesdeBaseDeDatos());
        
        HBox statusBar = new HBox(10);
        Label statusLabel = new Label("Conectado a base de datos local");
        statusLabel.getStyleClass().add("status-label");
        HBox.setHgrow(statusLabel, Priority.ALWAYS);
        statusBar.getChildren().add(statusLabel);
        
        HBox buttonBar = new HBox(10);
        buttonBar.getChildren().addAll(newAppointmentButton, refreshButton, importCitasButton);
        
        controlPanel.getChildren().addAll(titleLabel, buttonBar, statusBar);
        
        return controlPanel;
    }
    
    /**
     * Configura los manejadores de eventos para el calendario
     */
    private void configureEventHandlers() {
        // Manejar creación/edición de citas
        calendarView.setEntryDetailsCallback(param -> {
            Entry<?> entry = param.getEntry();
            
            // Permitir que se abra el diálogo predeterminado para editar
            // pero procesar la entrada después de cerrado
            editarCita(entry);
            
            return null; // Usar nuestro propio diálogo en lugar del predeterminado
        });
        
        // Configurar menú contextual para citas
        calendarView.setEntryContextMenuCallback(param -> {
            Entry<?> entry = param.getEntry();
            editarCita(entry);
            return null;
        });
    }
    
    /**
     * Inicia un hilo para mantener actualizada la hora en el calendario
     */
    private void startTimeThread() {
        Thread updateTimeThread = new Thread(() -> {
            while (true) {
                Platform.runLater(() -> {
                    calendarView.setToday(LocalDate.now());
                    calendarView.setTime(LocalTime.now());
                });
                
                try {
                    // Actualizar cada minuto
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        updateTimeThread.setDaemon(true);
        updateTimeThread.start();
    }
    
    /**
     * Configura el listener para cambios de tema
     */
    private void setupThemeListener() {
        themeChangeListener = (obs, oldVal, newVal) -> applyTheme();
        ThemeManager.getInstance().darkThemeProperty().addListener(themeChangeListener);
    }
    
    /**
     * Aplica el tema actual
     */
    private void applyTheme() {
        ThemeManager themeManager = ThemeManager.getInstance();
        if (themeManager.isDarkTheme()) {
            calendarView.getStyleClass().add("dark-theme");
            getStyleClass().add("dark-theme");
            getStyleClass().remove("light-theme");
        } else {
            calendarView.getStyleClass().remove("dark-theme");
            calendarView.getStyleClass().add("light-theme");
            getStyleClass().remove("dark-theme");
            getStyleClass().add("light-theme");
        }
    }
    
    /**
     * Carga citas de ejemplo (reemplazar con carga real desde BD)
     */
    private void cargarCitasDeEjemplo() {
        // Agregar algunas citas de ejemplo
        LocalDate today = LocalDate.now();
        
        // Ejemplo 1: Cita veterinaria
        Entry<String> entry1 = new Entry<>("Revisión de Max (María García)");
        entry1.setLocation("Clínica Veterinaria");
        entry1.changeStartDate(today);
        entry1.changeStartTime(LocalTime.of(10, 0));
        entry1.changeEndDate(today);
        entry1.changeEndTime(LocalTime.of(10, 30));
        appointmentsCalendar.addEntry(entry1);
        
        // Ejemplo 2: Vacunación
        Entry<String> entry2 = new Entry<>("Vacunación de Luna (Carlos Pérez)");
        entry2.setLocation("Clínica Veterinaria");
        entry2.changeStartDate(today.plusDays(1));
        entry2.changeStartTime(LocalTime.of(11, 30));
        entry2.changeEndDate(today.plusDays(1));
        entry2.changeEndTime(LocalTime.of(12, 0));
        appointmentsCalendar.addEntry(entry2);
        
        // Ejemplo 3: Cirugía
        Entry<String> entry3 = new Entry<>("Cirugía de Toby (Ana Martínez)");
        entry3.setLocation("Clínica Veterinaria - Quirófano");
        entry3.changeStartDate(today.plusDays(2));
        entry3.changeStartTime(LocalTime.of(9, 0));
        entry3.changeEndDate(today.plusDays(2));
        entry3.changeEndTime(LocalTime.of(11, 0));
        appointmentsCalendar.addEntry(entry3);
        
        // Ejemplo 4: Control postoperatorio
        Entry<String> entry4 = new Entry<>("Control de Rocky (Juan López)");
        entry4.setLocation("Clínica Veterinaria");
        entry4.changeStartDate(today.plusDays(3));
        entry4.changeStartTime(LocalTime.of(16, 30));
        entry4.changeEndDate(today.plusDays(3));
        entry4.changeEndTime(LocalTime.of(17, 0));
        appointmentsCalendar.addEntry(entry4);
    }
    
    /**
     * Crea una nueva cita en el calendario
     */
    private void crearNuevaCita() {
        // Crear nueva entrada vacía para configurar
        Entry<String> newEntry = new Entry<>("Nueva Cita");
        
        // Establecer fecha/hora por defecto (próxima hora completa)
        LocalTime nextHour = LocalTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0);
        LocalDate today = LocalDate.now();
        
        newEntry.changeStartDate(today);
        newEntry.changeStartTime(nextHour);
        newEntry.changeEndDate(today);
        newEntry.changeEndTime(nextHour.plusMinutes(30));
        
        // Añadir al calendario (temporal)
        appointmentsCalendar.addEntry(newEntry);
        
        // Abrir editor para configurar
        editarCita(newEntry);
    }
    
    /**
     * Edita una cita existente o guarda una nueva
     */
    private void editarCita(Entry<?> entry) {
        // Determinar si es una entrada nueva basado en un ID válido
        boolean isNewEntry = (entry.getId() == null || entry.getId().isEmpty());
        
        // Aquí iría un diálogo personalizado para editar la cita
        // Por ahora usamos una alerta básica
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(isNewEntry ? "Nueva Cita" : "Editar Cita");
        alert.setHeaderText("Detalles de la cita");
        alert.setContentText("Título: " + entry.getTitle() + "\n" +
                "Fecha: " + entry.getStartDate() + "\n" +
                "Hora: " + entry.getStartTime() + " - " + entry.getEndTime() + "\n" +
                "Ubicación: " + entry.getLocation());
        
        ButtonType buttonDelete = new ButtonType("Eliminar");
        ButtonType buttonCancel = ButtonType.CANCEL;
        ButtonType buttonOk = ButtonType.OK;
        
        if (isNewEntry) {
            // Para citas nuevas no mostramos el botón de eliminar
            alert.getButtonTypes().setAll(buttonOk, buttonCancel);
        } else {
            alert.getButtonTypes().setAll(buttonOk, buttonDelete, buttonCancel);
        }
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == buttonDelete) {
                // Eliminar cita
                entry.removeFromCalendar();
                eliminarCitaDeBaseDeDatos(entry);
            } else if (result.get() == buttonOk) {
                // Guardar cambios
                if (isNewEntry) {
                    guardarCitaEnBaseDeDatos(entry);
                } else {
                    actualizarCitaEnBaseDeDatos(entry);
                }
            } else {
                // Si es una cita nueva y se cancela, eliminarla del calendario
                if (isNewEntry) {
                    entry.removeFromCalendar();
                }
            }
        }
    }
    
    /**
     * Guarda una nueva cita en la base de datos
     */
    private void guardarCitaEnBaseDeDatos(Entry<?> entry) {
        // TODO: Implementar guardado en base de datos real
        System.out.println("Guardando cita en BD: " + entry.getTitle());
        
        // Mostrar información
        mostrarInformacion("Cita Guardada", 
                "Se ha guardado la cita: " + entry.getTitle() + "\n" +
                "Fecha: " + entry.getStartDate() + "\n" +
                "Hora: " + entry.getStartTime() + " - " + entry.getEndTime());
    }
    
    /**
     * Actualiza una cita existente en la base de datos
     */
    private void actualizarCitaEnBaseDeDatos(Entry<?> entry) {
        // TODO: Implementar actualización en base de datos real
        System.out.println("Actualizando cita en BD: " + entry.getTitle());
        
        // Mostrar información
        mostrarInformacion("Cita Actualizada", 
                "Se ha actualizado la cita: " + entry.getTitle() + "\n" +
                "Fecha: " + entry.getStartDate() + "\n" +
                "Hora: " + entry.getStartTime() + " - " + entry.getEndTime());
    }
    
    /**
     * Elimina una cita de la base de datos
     */
    private void eliminarCitaDeBaseDeDatos(Entry<?> entry) {
        // TODO: Implementar eliminación en base de datos real
        System.out.println("Eliminando cita de BD: " + entry.getTitle());
        
        // Mostrar información
        mostrarInformacion("Cita Eliminada", 
                "Se ha eliminado la cita: " + entry.getTitle());
    }
    
    /**
     * Carga las citas desde la base de datos
     */
    private void cargarCitasDesdeBaseDeDatos() {
        // TODO: Implementar carga desde base de datos real
        System.out.println("Cargando citas desde BD");
        
        // Ejemplo: Limpiar citas actuales y recargar
        appointmentsCalendar.clear();
        cargarCitasDeEjemplo(); // Reemplazar con carga real
        
        // Mostrar información
        mostrarInformacion("Calendario Actualizado", 
                "Se han cargado las citas desde la base de datos");
    }
    
    /**
     * Importa citas desde la base de datos
     */
    private void importarCitasDesdeBaseDeDatos() {
        // TODO: Implementar importación real desde la base de datos
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Importar Citas");
        confirmacion.setHeaderText("¿Desea importar las citas desde la base de datos?");
        confirmacion.setContentText("Esto reemplazará todas las citas actuales en el calendario.");
        
        Optional<ButtonType> result = confirmacion.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Limpiar el calendario
            appointmentsCalendar.clear();
            
            // Simular carga de citas (reemplazar con carga real)
            cargarCitasDeEjemplo();
            
            mostrarInformacion("Importación Completada", 
                    "Se han importado todas las citas desde la base de datos.");
        }
    }
    
    /**
     * Muestra un diálogo de información
     */
    private void mostrarInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    /**
     * Convierte LocalDateTime a Entry para el calendario
     */
    private Entry<String> localDateTimeToEntry(String title, String location, 
                                             LocalDateTime start, LocalDateTime end) {
        Entry<String> entry = new Entry<>(title);
        entry.setLocation(location);
        
        entry.changeStartDate(start.toLocalDate());
        entry.changeStartTime(start.toLocalTime());
        entry.changeEndDate(end.toLocalDate());
        entry.changeEndTime(end.toLocalTime());
        
        return entry;
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
     * Personaliza los colores del calendario
     */
    public void personalizarColores() {
        // Personalizar colores y estilos para cada tipo de calendario
        appointmentsCalendar.setStyle(Calendar.Style.STYLE3);
        
        // Agregar otros calendarios con diferentes colores si es necesario
        Calendar urgentCalendar = new Calendar("Urgencias");
        urgentCalendar.setStyle(Calendar.Style.STYLE2);
        
        Calendar surgeryCalendar = new Calendar("Cirugías");
        surgeryCalendar.setStyle(Calendar.Style.STYLE1);
        
        // Añadir al origen de calendarios
        calendarSource.getCalendars().addAll(urgentCalendar, surgeryCalendar);
    }
    
    /**
     * Configura las páginas que se mostrarán en el calendario
     */
    public void configurarVistas() {
        // Ocultar vistas que no necesitamos
        calendarView.showDayPage();
        
        // Ocultar los campos de año, mes, agenda si no se necesitan
        calendarView.getYearPage().setVisible(false);
        calendarView.getMonthPage().setVisible(true);
        calendarView.getWeekPage().setVisible(true);
        calendarView.getDayPage().setVisible(true);
        
        // Establecer una vista por defecto (por ejemplo, la vista de semana)
        calendarView.showWeekPage();
        
        // Otros ajustes visuales
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowPrintButton(false);
        calendarView.setShowPageToolBarControls(true);
    }
} 