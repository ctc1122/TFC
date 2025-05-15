package com.example.pruebamongodbcss.calendar;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.DateControl;

/**
 * Componente de calendario basado en CalendarFX.
 * Esta clase envuelve la funcionalidad de CalendarFX para su uso en la aplicación.
 */
public class CalendarFXComponent extends BorderPane {
    
    // Variables para el calendario
    private CalendarView calendarView;
    private List<Calendar> calendars = new ArrayList<>();
    private CalendarSource calendarSource;
    
    /**
     * Constructor que inicializa el componente del calendario.
     */
    public CalendarFXComponent() {
        initialize();
    }
    
    /**
     * Inicializa el componente del calendario.
     */
    private void initialize() {
        try {
            // Crear el componente principal de la vista
            calendarView = new CalendarView();
            
            // Crear los calendarios por tipo de cita
            Calendar citasNormales = new Calendar("Citas normales");
            citasNormales.setStyle(Calendar.Style.STYLE1);
            
            Calendar citasUrgentes = new Calendar("Citas urgentes");
            citasUrgentes.setStyle(Calendar.Style.STYLE2);
            
            Calendar citasCompletadas = new Calendar("Citas completadas");
            citasCompletadas.setStyle(Calendar.Style.STYLE3);
            
            Calendar citasCanceladas = new Calendar("Citas canceladas");
            citasCanceladas.setStyle(Calendar.Style.STYLE7);
            
            // Habilitar la edición de los calendarios
            citasNormales.setReadOnly(false);
            citasUrgentes.setReadOnly(false);
            citasCompletadas.setReadOnly(false);
            citasCanceladas.setReadOnly(false);
            
            // Agregar a la lista de calendarios
            calendars.add(citasNormales);
            calendars.add(citasUrgentes);
            calendars.add(citasCompletadas);
            calendars.add(citasCanceladas);
            
            // Agregar los calendarios a una fuente
            calendarSource = new CalendarSource("Veterinaria");
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
            
            // Agregar eventos de ejemplo
            createSampleEntries(citasNormales, citasUrgentes, citasCompletadas, citasCanceladas);
            
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
            
            // Agregar el calendario a este BorderPane
            setCenter(calendarView);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
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
     * Muestra un diálogo para editar los detalles de una entrada
     */
    private void showEntryDetailsDialog(Entry<?> entry) {
        try {
            // Crear un diálogo para editar la cita
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Detalles de Cita");
            dialog.setHeaderText("Información de la cita");
            
            // Configurar botones
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            // Crear formulario
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
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
            grid.add(new Label("Título:"), 0, 0);
            grid.add(titleField, 1, 0);
            grid.add(new Label("Ubicación:"), 0, 1);
            grid.add(locationField, 1, 1);
            grid.add(new Label("Fecha inicio:"), 0, 2);
            grid.add(startDatePicker, 1, 2);
            grid.add(startTimeBox, 2, 2);
            grid.add(new Label("Fecha fin:"), 0, 3);
            grid.add(endDatePicker, 1, 3);
            grid.add(endTimeBox, 2, 3);
            grid.add(new Label("Calendario:"), 0, 4);
            grid.add(calendarComboBox, 1, 4);
            
            dialog.getDialogPane().setContent(grid);
            
            // Focus al campo de título
            Platform.runLater(titleField::requestFocus);
            
            // Manejar resultado del diálogo
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    // Actualizar la entrada con los valores del formulario
                    entry.setTitle(titleField.getText());
                    entry.setLocation(locationField.getText());
                    
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
                    if (entry.getCalendar() != calendarComboBox.getValue()) {
                        if (entry.getCalendar() != null) {
                            entry.getCalendar().removeEntry(entry);
                        }
                        entry.setCalendar(calendarComboBox.getValue());
                        calendarComboBox.getValue().addEntry(entry);
                    }
                    
                    return ButtonType.OK;
                }
                return ButtonType.CANCEL;
            });
            
            dialog.showAndWait();
            
            // Refrescar el calendario
            refreshCalendar();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Crear eventos de ejemplo para el calendario
     */
    private void createSampleEntries(Calendar citasNormales, 
                                  Calendar citasUrgentes, 
                                  Calendar citasCompletadas, 
                                  Calendar citasCanceladas) {
        
        Random random = new Random();
        
        // Títulos de ejemplo para citas
        String[] titles = {
            "Consulta veterinaria", 
            "Vacunación canina", 
            "Control de salud",
            "Revisión felina", 
            "Cirugía menor", 
            "Desparasitación",
            "Análisis de sangre", 
            "Consulta de seguimiento", 
            "Limpieza dental",
            "Tratamiento de heridas"
        };
        
        // Ubicaciones de ejemplo
        String[] locations = {
            "Sala de consulta 1", 
            "Sala de consulta 2", 
            "Sala de tratamientos",
            "Quirófano", 
            "Laboratorio", 
            "Área de rehabilitación"
        };
        
        LocalDate today = LocalDate.now();
        
        // Crear citas normales
        for (int i = 0; i < 10; i++) {
            Entry<String> entry = new Entry<>(titles[random.nextInt(titles.length)]);
            entry.setLocation(locations[random.nextInt(locations.length)]);
            
            LocalDate date = today.plusDays(random.nextInt(14)); // Próximos 14 días
            LocalTime startTime = LocalTime.of(9 + random.nextInt(8), 15 * random.nextInt(4));
            LocalTime endTime = startTime.plusMinutes(30 + random.nextInt(4) * 15);
            
            entry.changeStartDate(date);
            entry.changeStartTime(startTime);
            entry.changeEndDate(date);
            entry.changeEndTime(endTime);
            
            citasNormales.addEntry(entry);
        }
        
        // Crear citas urgentes
        for (int i = 0; i < 3; i++) {
            Entry<String> entry = new Entry<>("URGENTE: " + titles[random.nextInt(titles.length)]);
            entry.setLocation(locations[random.nextInt(locations.length)]);
            
            LocalDate date = today.plusDays(random.nextInt(7)); // Próximos 7 días
            LocalTime startTime = LocalTime.of(9 + random.nextInt(8), 15 * random.nextInt(4));
            LocalTime endTime = startTime.plusMinutes(30 + random.nextInt(4) * 15);
            
            entry.changeStartDate(date);
            entry.changeStartTime(startTime);
            entry.changeEndDate(date);
            entry.changeEndTime(endTime);
            
            citasUrgentes.addEntry(entry);
        }
        
        // Crear citas completadas
        for (int i = 0; i < 5; i++) {
            Entry<String> entry = new Entry<>(titles[random.nextInt(titles.length)] + " (Completada)");
            entry.setLocation(locations[random.nextInt(locations.length)]);
            
            LocalDate date = today.minusDays(random.nextInt(7)); // Últimos 7 días
            LocalTime startTime = LocalTime.of(9 + random.nextInt(8), 15 * random.nextInt(4));
            LocalTime endTime = startTime.plusMinutes(30 + random.nextInt(4) * 15);
            
            entry.changeStartDate(date);
            entry.changeStartTime(startTime);
            entry.changeEndDate(date);
            entry.changeEndTime(endTime);
            
            citasCompletadas.addEntry(entry);
        }
        
        // Crear citas canceladas
        for (int i = 0; i < 2; i++) {
            Entry<String> entry = new Entry<>(titles[random.nextInt(titles.length)] + " (Cancelada)");
            entry.setLocation(locations[random.nextInt(locations.length)]);
            
            LocalDate date = today.plusDays(random.nextInt(10) - 5); // Entre -5 y +5 días
            LocalTime startTime = LocalTime.of(9 + random.nextInt(8), 15 * random.nextInt(4));
            LocalTime endTime = startTime.plusMinutes(30 + random.nextInt(4) * 15);
            
            entry.changeStartDate(date);
            entry.changeStartTime(startTime);
            entry.changeEndDate(date);
            entry.changeEndTime(endTime);
            
            citasCanceladas.addEntry(entry);
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
     * Agrega un nuevo evento al calendario
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
} 