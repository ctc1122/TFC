package com.example.pruebamongodbcss.Modulos.Clinica;

import com.example.pruebamongodbcss.Data.EstadoCita;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

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
    private String motivo;
    private EstadoCita estado;
    private String observaciones;

    /**
     * Constructor vacío
     */
    public ModeloCita() {
        this.fechaHora = LocalDateTime.now();
        this.estado = EstadoCita.PENDIENTE;
        this.observaciones = "";
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
        
        this.motivo = document.getString("motivo");
        this.estado = EstadoCita.valueOf(document.getString("estado"));
        this.observaciones = document.getString("observaciones");
    }

    /**
     * Convierte el modelo a un documento para MongoDB
     */
    public Document toDocument() {
        Document doc = new Document();
        
        if (id != null) {
            doc.append("_id", id);
        }
        
        doc.append("pacienteId", pacienteId)
           .append("nombrePaciente", nombrePaciente)
           .append("tipoAnimal", tipoAnimal)
           .append("razaAnimal", razaAnimal)
           .append("veterinarioId", veterinarioId)
           .append("nombreVeterinario", nombreVeterinario)
           .append("fechaHora", Date.from(fechaHora.atZone(ZoneId.systemDefault()).toInstant()))
           .append("motivo", motivo)
           .append("estado", estado.name())
           .append("observaciones", observaciones);
        
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
} 