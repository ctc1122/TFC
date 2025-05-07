package com.example.pruebamongodbcss.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Clase que representa una cita médica en la clínica veterinaria
 */
public class Cita {
    private String id;
    private Paciente paciente;
    private Usuario veterinario;
    private LocalDateTime fechaHora;
    private String motivo;
    private EstadoCita estado;
    private String observaciones;
    
    // Constantes para el formato de fecha
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    /**
     * Constructor para crear una nueva cita
     */
    public Cita(Paciente paciente, Usuario veterinario, LocalDateTime fechaHora, String motivo) {
        this.id = UUID.randomUUID().toString();
        this.paciente = paciente;
        this.veterinario = veterinario;
        this.fechaHora = fechaHora;
        this.motivo = motivo;
        this.estado = EstadoCita.PENDIENTE;
        this.observaciones = "";
    }
    
    /**
     * Constructor completo para cargar citas desde la base de datos
     */
    public Cita(String id, Paciente paciente, Usuario veterinario, LocalDateTime fechaHora, 
               String motivo, EstadoCita estado, String observaciones) {
        this.id = id;
        this.paciente = paciente;
        this.veterinario = veterinario;
        this.fechaHora = fechaHora;
        this.motivo = motivo;
        this.estado = estado;
        this.observaciones = observaciones;
    }
    
    // Métodos para cambiar el estado de la cita
    public void marcarEnCurso() {
        this.estado = EstadoCita.EN_CURSO;
    }
    
    public void marcarCompletada() {
        this.estado = EstadoCita.COMPLETADA;
    }
    
    public void cancelar() {
        this.estado = EstadoCita.CANCELADA;
    }
    
    public void reprogramar(LocalDateTime nuevaFechaHora) {
        this.fechaHora = nuevaFechaHora;
        this.estado = EstadoCita.REPROGRAMADA;
    }
    
    public void agregarObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    // Método para comprobar si hay conflicto con otra cita
    public boolean hayConflictoCon(Cita otraCita) {
        // Si las citas son para el mismo día y hora, hay conflicto
        return this.fechaHora.equals(otraCita.getFechaHora());
    }
    
    // Método para comprobar si la cita está en un rango de fechas
    public boolean estaEnRango(LocalDateTime inicio, LocalDateTime fin) {
        return (this.fechaHora.isAfter(inicio) || this.fechaHora.isEqual(inicio)) && 
               (this.fechaHora.isBefore(fin) || this.fechaHora.isEqual(fin));
    }
    
    // ToString para mostrar información de la cita
    @Override
    public String toString() {
        return "Cita{" +
                "id='" + id + '\'' +
                ", paciente=" + paciente.toString() +
                ", veterinario=" + veterinario.getNombre() +
                ", fechaHora=" + fechaHora.format(FORMATTER) +
                ", motivo='" + motivo + '\'' +
                ", estado=" + estado +
                '}';
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public Paciente getPaciente() {
        return paciente;
    }
    
    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }
    
    public Usuario getVeterinario() {
        return veterinario;
    }
    
    public void setVeterinario(Usuario veterinario) {
        this.veterinario = veterinario;
    }
    
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
    
    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
    
    public String getMotivo() {
        return motivo;
    }
    
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
    
    public EstadoCita getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    // Métodos para formatear la fecha y hora
    public String getFechaHoraFormateada() {
        return fechaHora.format(FORMATTER);
    }
} 