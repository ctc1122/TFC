package com.example.pruebamongodbcss.Modulos.Fichaje;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.bson.Document;
import org.bson.types.ObjectId;

public class ModeloFichaje implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private ObjectId id;
    private ObjectId empleadoId;
    private String nombreEmpleado;
    private String usuarioEmpleado;
    private LocalDate fecha;
    private LocalDateTime fechaHoraEntrada;
    private LocalDateTime fechaHoraSalida;
    private TipoFichaje tipoEntrada;
    private TipoFichaje tipoSalida;
    private String motivoIncidenciaEntrada;
    private String motivoIncidenciaSalida;
    private EstadoFichaje estado;
    private long minutosTrabajoTotal;
    private boolean esIncidencia;
    
    // Enums para tipos y estados
    public enum TipoFichaje {
        NORMAL("Normal"),
        INCIDENCIA_MEDICO("Visita al médico"),
        INCIDENCIA_BAJA("Baja médica"),
        INCIDENCIA_PERSONAL("Asunto personal"),
        INCIDENCIA_TRANSPORTE("Problema de transporte"),
        INCIDENCIA_OTROS("Otros");
        
        private final String descripcion;
        
        TipoFichaje(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
    
    public enum EstadoFichaje {
        ABIERTO("Abierto"),
        CERRADO("Cerrado"),
        INCIDENCIA_AUTO("Incidencia automática"),
        INCOMPLETO("Incompleto");
        
        private final String descripcion;
        
        EstadoFichaje(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
    
    // Constructores
    public ModeloFichaje() {
        this.estado = EstadoFichaje.ABIERTO;
        this.tipoEntrada = TipoFichaje.NORMAL;
        this.tipoSalida = TipoFichaje.NORMAL;
        this.esIncidencia = false;
        this.minutosTrabajoTotal = 0;
    }
    
    public ModeloFichaje(ObjectId empleadoId, String nombreEmpleado, String usuarioEmpleado) {
        this();
        this.empleadoId = empleadoId;
        this.nombreEmpleado = nombreEmpleado;
        this.usuarioEmpleado = usuarioEmpleado;
        this.fecha = LocalDate.now();
    }
    
    // Constructor desde Document de MongoDB
    public ModeloFichaje(Document doc) {
        this.id = doc.getObjectId("_id");
        this.empleadoId = doc.getObjectId("empleadoId");
        this.nombreEmpleado = doc.getString("nombreEmpleado");
        this.usuarioEmpleado = doc.getString("usuarioEmpleado");
        
        // Manejo de fechas
        if (doc.getString("fecha") != null) {
            this.fecha = LocalDate.parse(doc.getString("fecha"));
        }
        if (doc.getString("fechaHoraEntrada") != null) {
            this.fechaHoraEntrada = LocalDateTime.parse(doc.getString("fechaHoraEntrada"));
        }
        if (doc.getString("fechaHoraSalida") != null) {
            this.fechaHoraSalida = LocalDateTime.parse(doc.getString("fechaHoraSalida"));
        }
        
        // Enums
        this.tipoEntrada = doc.getString("tipoEntrada") != null ? 
            TipoFichaje.valueOf(doc.getString("tipoEntrada")) : TipoFichaje.NORMAL;
        this.tipoSalida = doc.getString("tipoSalida") != null ? 
            TipoFichaje.valueOf(doc.getString("tipoSalida")) : TipoFichaje.NORMAL;
        this.estado = doc.getString("estado") != null ? 
            EstadoFichaje.valueOf(doc.getString("estado")) : EstadoFichaje.ABIERTO;
            
        this.motivoIncidenciaEntrada = doc.getString("motivoIncidenciaEntrada");
        this.motivoIncidenciaSalida = doc.getString("motivoIncidenciaSalida");
        this.minutosTrabajoTotal = doc.getLong("minutosTrabajoTotal") != null ? 
            doc.getLong("minutosTrabajoTotal") : 0L;
        this.esIncidencia = doc.getBoolean("esIncidencia", false);
    }
    
    // Método para convertir a Document de MongoDB
    public Document toDocument() {
        Document doc = new Document();
        if (id != null) doc.put("_id", id);
        doc.put("empleadoId", empleadoId);
        doc.put("nombreEmpleado", nombreEmpleado);
        doc.put("usuarioEmpleado", usuarioEmpleado);
        doc.put("fecha", fecha != null ? fecha.toString() : null);
        doc.put("fechaHoraEntrada", fechaHoraEntrada != null ? fechaHoraEntrada.toString() : null);
        doc.put("fechaHoraSalida", fechaHoraSalida != null ? fechaHoraSalida.toString() : null);
        doc.put("tipoEntrada", tipoEntrada.name());
        doc.put("tipoSalida", tipoSalida.name());
        doc.put("estado", estado.name());
        doc.put("motivoIncidenciaEntrada", motivoIncidenciaEntrada);
        doc.put("motivoIncidenciaSalida", motivoIncidenciaSalida);
        doc.put("minutosTrabajoTotal", minutosTrabajoTotal);
        doc.put("esIncidencia", esIncidencia);
        return doc;
    }
    
    // Métodos de utilidad
    public void ficharEntrada(LocalDateTime fechaHora, TipoFichaje tipo, String motivoIncidencia) {
        this.fechaHoraEntrada = fechaHora;
        this.tipoEntrada = tipo;
        this.motivoIncidenciaEntrada = motivoIncidencia;
        this.fecha = fechaHora.toLocalDate();
        this.esIncidencia = (tipo != TipoFichaje.NORMAL);
    }
    
    public void ficharSalida(LocalDateTime fechaHora, TipoFichaje tipo, String motivoIncidencia) {
        this.fechaHoraSalida = fechaHora;
        this.tipoSalida = tipo;
        this.motivoIncidenciaSalida = motivoIncidencia;
        this.estado = EstadoFichaje.CERRADO;
        
        if (tipo != TipoFichaje.NORMAL) {
            this.esIncidencia = true;
        }
        
        // Calcular minutos trabajados
        if (fechaHoraEntrada != null && fechaHoraSalida != null) {
            this.minutosTrabajoTotal = java.time.Duration.between(fechaHoraEntrada, fechaHoraSalida).toMinutes();
        }
    }
    
    public void marcarComoIncidenciaAutomatica() {
        this.estado = EstadoFichaje.INCIDENCIA_AUTO;
        this.esIncidencia = true;
        this.minutosTrabajoTotal = 0; // No se computan horas en incidencias automáticas
    }
    
    public String getHorasTrabajadasFormateadas() {
        if (minutosTrabajoTotal <= 0) return "0h 0m";
        long horas = minutosTrabajoTotal / 60;
        long minutos = minutosTrabajoTotal % 60;
        return String.format("%dh %dm", horas, minutos);
    }
    
    public String getHoraEntradaFormateada() {
        if (fechaHoraEntrada == null) return "-";
        return fechaHoraEntrada.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    
    public String getHoraSalidaFormateada() {
        if (fechaHoraSalida == null) return "-";
        return fechaHoraSalida.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    
    public String getFechaFormateada() {
        if (fecha == null) return "-";
        return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
    
    public boolean puedeSerCerradoAutomaticamente() {
        if (fechaHoraEntrada == null || estado != EstadoFichaje.ABIERTO) {
            return false;
        }
        
        LocalDateTime limite = fechaHoraEntrada.plusHours(12);
        return LocalDateTime.now().isAfter(limite);
    }
    
    // Getters y Setters
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }
    
    public ObjectId getEmpleadoId() { return empleadoId; }
    public void setEmpleadoId(ObjectId empleadoId) { this.empleadoId = empleadoId; }
    
    public String getNombreEmpleado() { return nombreEmpleado; }
    public void setNombreEmpleado(String nombreEmpleado) { this.nombreEmpleado = nombreEmpleado; }
    
    public String getUsuarioEmpleado() { return usuarioEmpleado; }
    public void setUsuarioEmpleado(String usuarioEmpleado) { this.usuarioEmpleado = usuarioEmpleado; }
    
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    
    public LocalDateTime getFechaHoraEntrada() { return fechaHoraEntrada; }
    public void setFechaHoraEntrada(LocalDateTime fechaHoraEntrada) { this.fechaHoraEntrada = fechaHoraEntrada; }
    
    public LocalDateTime getFechaHoraSalida() { return fechaHoraSalida; }
    public void setFechaHoraSalida(LocalDateTime fechaHoraSalida) { this.fechaHoraSalida = fechaHoraSalida; }
    
    public TipoFichaje getTipoEntrada() { return tipoEntrada; }
    public void setTipoEntrada(TipoFichaje tipoEntrada) { this.tipoEntrada = tipoEntrada; }
    
    public TipoFichaje getTipoSalida() { return tipoSalida; }
    public void setTipoSalida(TipoFichaje tipoSalida) { this.tipoSalida = tipoSalida; }
    
    public String getMotivoIncidenciaEntrada() { return motivoIncidenciaEntrada; }
    public void setMotivoIncidenciaEntrada(String motivoIncidenciaEntrada) { this.motivoIncidenciaEntrada = motivoIncidenciaEntrada; }
    
    public String getMotivoIncidenciaSalida() { return motivoIncidenciaSalida; }
    public void setMotivoIncidenciaSalida(String motivoIncidenciaSalida) { this.motivoIncidenciaSalida = motivoIncidenciaSalida; }
    
    public EstadoFichaje getEstado() { return estado; }
    public void setEstado(EstadoFichaje estado) { this.estado = estado; }
    
    public long getMinutosTrabajoTotal() { return minutosTrabajoTotal; }
    public void setMinutosTrabajoTotal(long minutosTrabajoTotal) { this.minutosTrabajoTotal = minutosTrabajoTotal; }
    
    public boolean isEsIncidencia() { return esIncidencia; }
    public void setEsIncidencia(boolean esIncidencia) { this.esIncidencia = esIncidencia; }
    
    @Override
    public String toString() {
        return String.format("Fichaje[%s - %s - %s - %s]", 
            getFechaFormateada(), nombreEmpleado, getHoraEntradaFormateada(), estado.getDescripcion());
    }
} 