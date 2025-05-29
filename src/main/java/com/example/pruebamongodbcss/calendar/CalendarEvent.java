package com.example.pruebamongodbcss.calendar;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clase que representa un evento en el calendario
 */
public class CalendarEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Enumerado para los tipos de eventos
     */
    public enum EventoTipo {
        CITA_MEDICA("Cita médica"),
        REUNION("Reunión"),
        RECORDATORIO("Recordatorio"),
        OTRO("Otro");
        
        private final String descripcion;
        
        EventoTipo(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
        
        public static EventoTipo fromString(String text) {
            if (text == null) {
                return OTRO;
            }
            
            text = text.toLowerCase();
            if (text.contains("meeting") || text.contains("reunion") || text.contains("reunión")) {
                return REUNION;
            } else if (text.contains("reminder") || text.contains("recordatorio")) {
                return RECORDATORIO;
            } else if (text.contains("cita") || text.contains("medical") || text.contains("médica")) {
                return CITA_MEDICA;
            } else {
                return OTRO;
            }
        }
    }
    
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
    private String estado; // PENDIENTE, EN_CURSO, COMPLETADA, CANCELADA, REPROGRAMADA
    private String eventType; // Para compatibilidad con código existente
    private EventoTipo tipoEvento; // Nuevo campo para utilizar el enumerado
    private String pacienteId; // ID del paciente asociado a la cita

    // NUEVOS CAMPOS: Contadores para control de asociaciones
    private int contadorDiagnosticos = 0; // Contador de diagnósticos asociados
    private int contadorFacturas = 0; // Contador de facturas asociadas (incluye borradores)

    /**
     * Constructor por defecto
     */
    public CalendarEvent() {
        this.tipoEvento = EventoTipo.CITA_MEDICA; // Por defecto será cita médica
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
        this.tipoEvento = EventoTipo.CITA_MEDICA; // Por defecto será cita médica
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
     * @param estado Estado de la cita (PENDIENTE, EN_CURSO, COMPLETADA, CANCELADA, REPROGRAMADA)
     * @param eventType Tipo de evento (meeting, reminder, other)
     */
    public CalendarEvent(String id, String title, String start, String end, 
                         String location, String description, String color, String textColor, 
                         boolean allDay, String type, String estado, String eventType) {
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
        this.estado = estado;
        this.eventType = eventType;
        this.tipoEvento = EventoTipo.fromString(eventType); // Convertir eventType a enumerado
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
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    /**
     * @return String representando el tipo de evento (legacy)
     */
    public String getEventType() {
        // Si no hay eventType pero sí hay tipoEvento, convertir tipoEvento a string para compatibilidad
        if ((eventType == null || eventType.isEmpty()) && tipoEvento != null) {
            return tipoEvento == EventoTipo.REUNION ? "meeting" : 
                   tipoEvento == EventoTipo.RECORDATORIO ? "reminder" : 
                   tipoEvento == EventoTipo.CITA_MEDICA ? "medical" : "other";
        }
        return eventType;
    }
    
    /**
     * Establece el tipo de evento (legacy)
     * También actualiza el tipoEvento usando el enumerado
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
        this.tipoEvento = EventoTipo.fromString(eventType);
    }
    
    /**
     * @return EventoTipo del evento utilizando el enumerado
     */
    public EventoTipo getTipoEvento() {
        return tipoEvento;
    }
    
    /**
     * Establece el tipo de evento utilizando el enumerado
     * También actualiza eventType para mantener compatibilidad
     */
    public void setTipoEvento(EventoTipo tipoEvento) {
        this.tipoEvento = tipoEvento;
        
        // Actualizar eventType para mantener compatibilidad
        if (tipoEvento == EventoTipo.REUNION) {
            this.eventType = "meeting";
        } else if (tipoEvento == EventoTipo.RECORDATORIO) {
            this.eventType = "reminder";
        } else if (tipoEvento == EventoTipo.CITA_MEDICA) {
            this.eventType = "medical";
        } else {
            this.eventType = "other";
        }
    }

    public String getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(String pacienteId) {
        this.pacienteId = pacienteId;
    }

    /**
     * Obtiene el contador de diagnósticos asociados a esta cita
     * @return número de diagnósticos asociados
     */
    public int getContadorDiagnosticos() {
        return contadorDiagnosticos;
    }

    /**
     * Establece el contador de diagnósticos asociados a esta cita
     * @param contadorDiagnosticos número de diagnósticos asociados
     */
    public void setContadorDiagnosticos(int contadorDiagnosticos) {
        this.contadorDiagnosticos = Math.max(0, contadorDiagnosticos); // No permitir valores negativos
    }

    /**
     * Incrementa el contador de diagnósticos en 1
     */
    public void incrementarContadorDiagnosticos() {
        this.contadorDiagnosticos++;
    }

    /**
     * Decrementa el contador de diagnósticos en 1 (mínimo 0)
     */
    public void decrementarContadorDiagnosticos() {
        this.contadorDiagnosticos = Math.max(0, this.contadorDiagnosticos - 1);
    }

    /**
     * Obtiene el contador de facturas asociadas a esta cita
     * @return número de facturas asociadas (incluye borradores)
     */
    public int getContadorFacturas() {
        return contadorFacturas;
    }

    /**
     * Establece el contador de facturas asociadas a esta cita
     * @param contadorFacturas número de facturas asociadas
     */
    public void setContadorFacturas(int contadorFacturas) {
        this.contadorFacturas = Math.max(0, contadorFacturas); // No permitir valores negativos
    }

    /**
     * Incrementa el contador de facturas en 1
     */
    public void incrementarContadorFacturas() {
        this.contadorFacturas++;
    }

    /**
     * Decrementa el contador de facturas en 1 (mínimo 0)
     */
    public void decrementarContadorFacturas() {
        this.contadorFacturas = Math.max(0, this.contadorFacturas - 1);
    }

    /**
     * Verifica si la cita puede tener más facturas asociadas
     * @return true si puede tener más facturas (contador < 1), false si ya tiene el máximo
     */
    public boolean puedeAgregarFactura() {
        return this.contadorFacturas < 1;
    }

    /**
     * Verifica si la cita ya tiene facturas asociadas
     * @return true si tiene al menos una factura asociada
     */
    public boolean tieneFacturasAsociadas() {
        return this.contadorFacturas > 0;
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
                ", estado='" + estado + '\'' +
                ", eventType='" + eventType + '\'' +
                ", tipoEvento=" + tipoEvento +
                ", pacienteId='" + pacienteId + '\'' +
                ", contadorDiagnosticos=" + contadorDiagnosticos +
                ", contadorFacturas=" + contadorFacturas +
                '}';
    }
} 