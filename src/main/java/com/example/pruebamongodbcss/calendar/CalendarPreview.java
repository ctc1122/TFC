package com.example.pruebamongodbcss.calendar;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

/**
 * Clase para mostrar una vista previa del calendario en la página principal.
 * Esta es una versión simplificada del componente completo.
 */
public class CalendarPreview extends BorderPane {
    
    private CalendarFXComponent calendarFXComponent;
    
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
            calendarFXComponent = new CalendarFXComponent();
            calendarFXComponent.getCalendarView().showDayPage();
            calendarFXComponent.getCalendarView().setShowSearchField(false);
            calendarFXComponent.getCalendarView().setShowPrintButton(false);
            setCenter(calendarFXComponent);
            
            // Ocultar los otros botones de la barra de vista de día, dejando solo el de la izquierda (agenda)
            Platform.runLater(() -> {
                Node toolBar = calendarFXComponent.getCalendarView().getDayPage().lookup(".tool-bar");
                if (toolBar != null) {
                    List<Node> buttons = toolBar.lookupAll(".toggle-button").stream().toList();
                    for (int i = 0; i < buttons.size(); i++) {
                        if (i != 0) {
                            buttons.get(i).setVisible(false);
                            buttons.get(i).setManaged(false);
                        }
                    }
                }
            });
            
            // Actualizar la hora en segundo plano
            Thread updateTimeThread = new Thread("Calendar Preview Time Thread") {
                @Override
                public void run() {
                    while (true) {
                        Platform.runLater(() -> {
                            LocalDate today = LocalDate.now();
                            LocalTime now = LocalTime.now();
                            
                            // OPTIMIZACIÓN: Solo actualizar si realmente ha cambiado
                            if (!today.equals(calendarFXComponent.getCalendarView().getToday()) || 
                                Math.abs(now.getMinute() - calendarFXComponent.getCalendarView().getTime().getMinute()) >= 1) {
                                calendarFXComponent.getCalendarView().setToday(today);
                                calendarFXComponent.getCalendarView().setTime(now);
                            }
                        });
                        try {
                            // OPTIMIZACIÓN: Aumentar intervalo de 1 min a 5 min para preview
                            sleep(300000); // Actualiza cada 5 minutos en lugar de cada minuto
                        } catch (InterruptedException e) {
                            System.out.println("Hilo de actualización de tiempo preview interrumpido");
                            break;
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