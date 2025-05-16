package com.example.pruebamongodbcss.calendar;

import java.util.Objects;

/**
 * Clase que representa un evento en el calendario
 */
public class CalendarEvent {
    private String id;
    private String title;
    private String start;
    private String end;
    private String location;
    private String description;
    private String color;
    private String textColor;
    private boolean allDay;
    private String type; // default, urgent, completed, cancelled
    private String usuario; // usuario propietario de la cita

    /**
     * Constructor por defecto
     */
    public CalendarEvent() {
    }

    /**
     * Constructor con parámetros básicos
     * @param id ID del evento
     * @param title Título
     * @param start Fecha y hora de inicio
     * @param end Fecha y hora de fin
     */
    public CalendarEvent(String id, String title, String start, String end) {
        this.id = id;
        this.title = title;
        this.start = start;
        this.end = end;
    }

    /**
     * Constructor con todos los parámetros
     * @param id ID del evento
     * @param title Título
     * @param start Fecha y hora de inicio
     * @param end Fecha y hora de fin
     * @param location Ubicación
     * @param description Descripción
     * @param color Color (opcional)
     * @param textColor Color del texto (opcional)
     * @param allDay Si es un evento de todo el día
     * @param type Tipo de evento (default, urgent, completed, cancelled)
     */
    public CalendarEvent(String id, String title, String start, String end, 
                         String location, String description, String color, String textColor, boolean allDay, String type) {
        this.id = id;
        this.title = title;
        this.start = start;
        this.end = end;
        this.location = location;
        this.description = description;
        this.color = color;
        this.textColor = textColor;
        this.allDay = allDay;
        this.type = type;
    }

    // Getters y setters
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public String getTextColor() {
        return textColor;
    }
    
    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }
    
    public boolean isAllDay() {
        return allDay;
    }
    
    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalendarEvent that = (CalendarEvent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CalendarEvent{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", start='" + start + '\'' +
                ", end='" + end + '\'' +
                ", location='" + location + '\'' +
                ", description='" + description + '\'' +
                ", color='" + color + '\'' +
                ", textColor='" + textColor + '\'' +
                ", allDay=" + allDay +
                ", type='" + type + '\'' +
                ", usuario='" + usuario + '\'' +
                '}';
    }
} 