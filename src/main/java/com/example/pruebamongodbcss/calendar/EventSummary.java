package com.example.pruebamongodbcss.calendar;

import java.io.Serializable;

/**
 * Clase que representa un resumen de eventos serializable para envío por red
 */
public class EventSummary implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int meetings = 0;      // Reuniones
    private int reminders = 0;     // Recordatorios
    private int appointments = 0;  // Citas médicas
    private int total = 0;         // Total de eventos
    
    public EventSummary() {
    }
    
    public EventSummary(int meetings, int reminders, int appointments) {
        this.meetings = meetings;
        this.reminders = reminders;
        this.appointments = appointments;
        this.total = meetings + reminders + appointments;
    }
    
    public int getMeetings() {
        return meetings;
    }
    
    public void setMeetings(int meetings) {
        this.meetings = meetings;
    }
    
    public int getReminders() {
        return reminders;
    }
    
    public void setReminders(int reminders) {
        this.reminders = reminders;
    }
    
    public int getAppointments() {
        return appointments;
    }
    
    public void setAppointments(int appointments) {
        this.appointments = appointments;
    }
    
    public int getTotal() {
        return total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
    
    @Override
    public String toString() {
        return String.format("Eventos: %d total\n- %d reuniones\n- %d recordatorios\n- %d citas", 
            total, meetings, reminders, appointments);
    }
} 