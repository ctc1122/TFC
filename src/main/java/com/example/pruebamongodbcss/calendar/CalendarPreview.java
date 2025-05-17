package com.example.pruebamongodbcss.calendar;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Random;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;

import javafx.application.Platform;
import javafx.scene.layout.BorderPane;

/**
 * Clase para mostrar una vista previa del calendario en la página principal.
 * Esta es una versión simplificada del componente completo.
 */
public class CalendarPreview extends BorderPane {
    
    private CalendarView calendarView;
    private CalendarSource calendarSource;
    
    /**
     * Constructor que inicializa el componente del calendario para vista previa.
     */
    public CalendarPreview() {
        initialize();
    }
    
    /**
     * Inicializa el componente del calendario.
     */
    private void initialize() {
        try {
            // Crear el componente principal de la vista
            calendarView = new CalendarView();
            
            // Simplificar la interfaz
            calendarView.setShowAddCalendarButton(false);
            calendarView.setShowPrintButton(false);
            calendarView.setShowSourceTray(false);
            calendarView.setShowToolBar(false);
            calendarView.setShowSearchField(false);
            
            // Crear los calendarios con sus estilos
            Calendar citasNormales = new Calendar("Citas normales");
            citasNormales.setStyle(Calendar.Style.STYLE1);
            
            Calendar citasUrgentes = new Calendar("Citas urgentes");
            citasUrgentes.setStyle(Calendar.Style.STYLE2);
            
            // Agregar los calendarios a una fuente
            calendarSource = new CalendarSource("Vista Previa");
            calendarSource.getCalendars().addAll(citasNormales, citasUrgentes);
            
            // Registrar la fuente del calendario
            calendarView.getCalendarSources().add(calendarSource);
            
            // Configurar fecha y hora actual
            calendarView.setToday(LocalDate.now());
            calendarView.setTime(LocalTime.now());
            
            // Solo mostrar la vista semanal
            calendarView.showWeekPage();
            calendarView.setRequestedTime(LocalTime.of(9, 0));
            
            // Desactivar la edición para la vista previa
            calendarView.setEntryEditPolicy(param -> false);
            calendarView.setEntryDetailsCallback(param -> null);
            
            // Agregar eventos de ejemplo
            createSampleEntries(citasNormales, citasUrgentes);
            
            // Agregar el calendario a este BorderPane
            setCenter(calendarView);
            
            // Actualizar la hora en segundo plano
            Thread updateTimeThread = new Thread("Calendar Preview Time Thread") {
                @Override
                public void run() {
                    while (true) {
                        Platform.runLater(() -> {
                            calendarView.setToday(LocalDate.now());
                            calendarView.setTime(LocalTime.now());
                        });
                        try {
                            sleep(60000); // Actualiza cada minuto
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            
            updateTimeThread.setDaemon(true);
            updateTimeThread.start();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Crear eventos de ejemplo para la vista previa
     */
    private void createSampleEntries(Calendar citasNormales, Calendar citasUrgentes) {
        Random random = new Random();
        
        // Títulos de ejemplo para citas
        String[] titles = {
            "Consulta veterinaria", 
            "Vacunación canina", 
            "Control de salud",
            "Revisión felina"
        };
        
        // Ubicaciones de ejemplo
        String[] locations = {
            "Sala de consulta 1", 
            "Sala de consulta 2"
        };
        
        LocalDate today = LocalDate.now();
        
        // Crear algunas citas normales
        for (int i = 0; i < 3; i++) {
            Entry<String> entry = new Entry<>(titles[random.nextInt(titles.length)]);
            entry.setLocation(locations[random.nextInt(locations.length)]);
            
            // Mostrar eventos en la semana actual
            LocalDate date = today.plusDays(random.nextInt(7) - 3);
            LocalTime startTime = LocalTime.of(9 + random.nextInt(8), 15 * random.nextInt(4));
            LocalTime endTime = startTime.plusMinutes(30 + random.nextInt(4) * 15);
            
            entry.changeStartDate(date);
            entry.changeStartTime(startTime);
            entry.changeEndDate(date);
            entry.changeEndTime(endTime);
            
            citasNormales.addEntry(entry);
        }
        
        // Crear una cita urgente
        Entry<String> urgentEntry = new Entry<>("URGENTE: " + titles[random.nextInt(titles.length)]);
        urgentEntry.setLocation(locations[random.nextInt(locations.length)]);
        
        LocalDate date = today.plusDays(random.nextInt(2));
        LocalTime startTime = LocalTime.of(10 + random.nextInt(5), 0);
        LocalTime endTime = startTime.plusMinutes(45);
        
        urgentEntry.changeStartDate(date);
        urgentEntry.changeStartTime(startTime);
        urgentEntry.changeEndDate(date);
        urgentEntry.changeEndTime(endTime);
        
        citasUrgentes.addEntry(urgentEntry);
    }
} 