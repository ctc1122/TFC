package com.example.pruebamongodbcss.Modulos.Clinica;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.example.pruebamongodbcss.Data.EstadoCita;

/**
 * Modelo para representar una cita en la interfaz de usuario
 */
public class ModeloCita {
    private ObjectId id;
    private ObjectId pacienteId;
    private String nombrePaciente;
    private String tipoAnimal;
    private String razaAnimal;
    private ObjectId veterinarioId;
    private String nombreVeterinario;
    private LocalDateTime fechaHora;
    private LocalDateTime fechaHoraFin;
    private int duracionMinutos;
    private String motivo;
    private EstadoCita estado;
    private String observaciones;
    private String usuarioAsignado; // ID del usuario asignado a la cita
    private boolean recordatorio;
    private boolean confirmada;

    /**
     * Constructor vacío
     */
    public ModeloCita() {
        this.fechaHora = LocalDateTime.now();
        this.estado = EstadoCita.PENDIENTE;
        this.observaciones = "";
        this.duracionMinutos = 30; // Duración predeterminada de 30 minutos
        this.fechaHoraFin = this.fechaHora.plusMinutes(this.duracionMinutos);
        this.recordatorio = false;
        this.confirmada = false;
    }

    /**
     * Constructor desde un documento de MongoDB
     */
    public ModeloCita(Document document) {
        this.id = document.getObjectId("_id");
        this.pacienteId = document.getObjectId("pacienteId");
        this.nombrePaciente = document.getString("nombrePaciente");
        this.tipoAnimal = document.getString("tipoAnimal");
        this.razaAnimal = document.getString("razaAnimal");
        this.veterinarioId = document.getObjectId("veterinarioId");
        this.nombreVeterinario = document.getString("nombreVeterinario");
        
        Date fecha = document.getDate("fechaHora");
        this.fechaHora = fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        
        // Leer fechaHoraFin si existe, o calcularla basada en duracionMinutos
        if (document.containsKey("fechaHoraFin")) {
            Date fechaFin = document.getDate("fechaHoraFin");
            this.fechaHoraFin = fechaFin.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        
        // Leer duracionMinutos o calcularlo basado en fechaHora y fechaHoraFin
        if (document.containsKey("duracionMinutos")) {
            this.duracionMinutos = document.getInteger("duracionMinutos");
        } else if (this.fechaHoraFin != null) {
            // Calcular la duración en minutos entre fechaHora y fechaHoraFin
            this.duracionMinutos = (int) java.time.Duration.between(this.fechaHora, this.fechaHoraFin).toMinutes();
        } else {
            // Valor predeterminado
            this.duracionMinutos = 30;
            this.fechaHoraFin = this.fechaHora.plusMinutes(this.duracionMinutos);
        }
        
        this.motivo = document.getString("motivo");
        this.estado = EstadoCita.valueOf(document.getString("estado"));
        this.observaciones = document.getString("observaciones");
        
        // Compatibilidad con documentos antiguos (migración)
        if (document.containsKey("usuarioAsignado")) {
            this.usuarioAsignado = document.getString("usuarioAsignado");
        } else if (document.containsKey("usuarioId")) {
            // Si no tiene usuarioAsignado pero sí usuarioId, migrarlo
            this.usuarioAsignado = document.getString("usuarioId");
        } else if (document.containsKey("usuario")) {
            // Si no tiene ninguno de los anteriores pero sí usuario, migrarlo
            this.usuarioAsignado = document.getString("usuario");
        }
        
        // Campos adicionales
        this.recordatorio = document.getBoolean("recordatorio", false);
        this.confirmada = document.getBoolean("confirmada", false);
    }

    /**
     * Convierte el modelo a un documento para MongoDB
     */
    public Document toDocument() {
        Document doc = new Document();
        
        if (id != null) {
            doc.append("_id", id);
        }
        
        // Asegurar que fechaHoraFin siempre está calculada correctamente
        if (fechaHoraFin == null || duracionMinutos <= 0) {
            this.duracionMinutos = this.duracionMinutos <= 0 ? 30 : this.duracionMinutos;
            this.fechaHoraFin = this.fechaHora.plusMinutes(this.duracionMinutos);
        }
        
        doc.append("pacienteId", pacienteId)
           .append("nombrePaciente", nombrePaciente)
           .append("tipoAnimal", tipoAnimal)
           .append("razaAnimal", razaAnimal)
           .append("veterinarioId", veterinarioId)
           .append("nombreVeterinario", nombreVeterinario)
           .append("fechaHora", Date.from(fechaHora.atZone(ZoneId.systemDefault()).toInstant()))
           .append("fechaHoraFin", Date.from(fechaHoraFin.atZone(ZoneId.systemDefault()).toInstant()))
           .append("duracionMinutos", duracionMinutos)
           .append("motivo", motivo)
           .append("estado", estado.name())
           .append("observaciones", observaciones)
           .append("recordatorio", recordatorio)
           .append("confirmada", confirmada);
        
        // Añadir el campo usuarioAsignado si existe
        if (usuarioAsignado != null && !usuarioAsignado.isEmpty()) {
            doc.append("usuarioAsignado", usuarioAsignado);
        }
        
        return doc;
    }

    // Getters y Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(ObjectId pacienteId) {
        this.pacienteId = pacienteId;
    }

    public String getNombrePaciente() {
        return nombrePaciente;
    }

    public void setNombrePaciente(String nombrePaciente) {
        this.nombrePaciente = nombrePaciente;
    }

    public String getTipoAnimal() {
        return tipoAnimal;
    }

    public void setTipoAnimal(String tipoAnimal) {
        this.tipoAnimal = tipoAnimal;
    }

    public String getRazaAnimal() {
        return razaAnimal;
    }

    public void setRazaAnimal(String razaAnimal) {
        this.razaAnimal = razaAnimal;
    }

    public ObjectId getVeterinarioId() {
        return veterinarioId;
    }

    public void setVeterinarioId(ObjectId veterinarioId) {
        this.veterinarioId = veterinarioId;
    }

    public String getNombreVeterinario() {
        return nombreVeterinario;
    }

    public void setNombreVeterinario(String nombreVeterinario) {
        this.nombreVeterinario = nombreVeterinario;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
        // Actualizar fechaHoraFin basado en la nueva fechaHora
        if (this.duracionMinutos > 0) {
            this.fechaHoraFin = fechaHora.plusMinutes(this.duracionMinutos);
        }
    }
    
    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
        // Actualizar duracionMinutos basado en la diferencia entre fechaHora y fechaHoraFin
        if (this.fechaHora != null) {
            this.duracionMinutos = (int) java.time.Duration.between(this.fechaHora, fechaHoraFin).toMinutes();
        }
    }
    
    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(int duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
        // Actualizar fechaHoraFin basado en fechaHora y la nueva duración
        if (this.fechaHora != null) {
            this.fechaHoraFin = this.fechaHora.plusMinutes(duracionMinutos);
        }
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
    
    public String getEstadoAsString() {
        return estado.getDescripcion();
    }
    
    public String getFechaHoraFormateada() {
        return fechaHora.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    
    public String getFechaHoraFinFormateada() {
        return fechaHoraFin.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getUsuarioAsignado() {
        return usuarioAsignado;
    }

    public void setUsuarioAsignado(String usuarioAsignado) {
        this.usuarioAsignado = usuarioAsignado;
    }
    
    // Métodos de compatibilidad con código antiguo
    @Deprecated
    public String getUsuario() {
        return this.usuarioAsignado;
    }

    @Deprecated
    public void setUsuario(String usuario) {
        this.usuarioAsignado = usuario;
    }

    @Deprecated
    public String getUsuarioId() {
        return this.usuarioAsignado;
    }

    @Deprecated
    public void setUsuarioId(String usuarioId) {
        this.usuarioAsignado = usuarioId;
    }
    
    public boolean isRecordatorio() {
        return recordatorio;
    }

    public void setRecordatorio(boolean recordatorio) {
        this.recordatorio = recordatorio;
    }

    public boolean isConfirmada() {
        return confirmada;
    }

    public void setConfirmada(boolean confirmada) {
        this.confirmada = confirmada;
    }
} 