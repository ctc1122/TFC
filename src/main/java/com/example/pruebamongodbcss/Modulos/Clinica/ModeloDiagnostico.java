package com.example.pruebamongodbcss.Modulos.Clinica;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Modelo que representa un diagnóstico médico para un paciente.
 */
public class ModeloDiagnostico implements Serializable{
    private static final long serialVersionUID = 1L;
    private ObjectId id;
    private ObjectId pacienteId;
    private String nombrePaciente;
    private Date fecha;
    private String motivo;
    private String anamnesis;
    private String examenFisico;
    private List<String> pruebas;
    private String diagnostico;
    private String tratamiento;
    private List<String> medicamentos;
    private String observaciones;
    private String veterinario;
    private Date proximaVisita;
    private List<String> imagenes;  // URLs o referencias a imágenes
    private ObjectId citaId;  // ID de la cita asociada
    
    public ModeloDiagnostico() {
        this.pruebas = new ArrayList<>();
        this.medicamentos = new ArrayList<>();
        this.imagenes = new ArrayList<>();
    }
    
    // Constructor a partir de un documento de MongoDB
    public ModeloDiagnostico(Document doc) {
        this.id = doc.getObjectId("_id");
        this.pacienteId = doc.getObjectId("pacienteId");
        this.nombrePaciente = doc.getString("nombrePaciente");
        this.fecha = doc.getDate("fecha");
        this.motivo = doc.getString("motivo");
        this.anamnesis = doc.getString("anamnesis");
        this.examenFisico = doc.getString("examenFisico");
        
        List<String> pruebasDoc = doc.getList("pruebas", String.class);
        this.pruebas = pruebasDoc != null ? pruebasDoc : new ArrayList<>();
        
        this.diagnostico = doc.getString("diagnostico");
        this.tratamiento = doc.getString("tratamiento");
        
        List<String> medicamentosDoc = doc.getList("medicamentos", String.class);
        this.medicamentos = medicamentosDoc != null ? medicamentosDoc : new ArrayList<>();
        
        this.observaciones = doc.getString("observaciones");
        this.veterinario = doc.getString("veterinario");
        this.proximaVisita = doc.getDate("proximaVisita");
        
        List<String> imagenesDoc = doc.getList("imagenes", String.class);
        this.imagenes = imagenesDoc != null ? imagenesDoc : new ArrayList<>();
    }
    
    // Convertir a documento para MongoDB
    public Document toDocument() {
        Document doc = new Document();
        if (id != null) {
            doc.append("_id", id);
        }
        doc.append("pacienteId", pacienteId)
           .append("nombrePaciente", nombrePaciente)
           .append("fecha", fecha)
           .append("motivo", motivo)
           .append("anamnesis", anamnesis)
           .append("examenFisico", examenFisico)
           .append("pruebas", pruebas)
           .append("diagnostico", diagnostico)
           .append("tratamiento", tratamiento)
           .append("medicamentos", medicamentos)
           .append("observaciones", observaciones)
           .append("veterinario", veterinario)
           .append("proximaVisita", proximaVisita)
           .append("imagenes", imagenes)
           .append("citaId", citaId);
        return doc;
    }

    // Getters y setters
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

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getAnamnesis() {
        return anamnesis;
    }

    public void setAnamnesis(String anamnesis) {
        this.anamnesis = anamnesis;
    }

    public String getExamenFisico() {
        return examenFisico;
    }

    public void setExamenFisico(String examenFisico) {
        this.examenFisico = examenFisico;
    }

    public List<String> getPruebas() {
        return pruebas;
    }

    public void setPruebas(List<String> pruebas) {
        this.pruebas = pruebas;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(String tratamiento) {
        this.tratamiento = tratamiento;
    }

    public List<String> getMedicamentos() {
        return medicamentos;
    }

    public void setMedicamentos(List<String> medicamentos) {
        this.medicamentos = medicamentos;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getVeterinario() {
        return veterinario;
    }

    public void setVeterinario(String veterinario) {
        this.veterinario = veterinario;
    }

    public Date getProximaVisita() {
        return proximaVisita;
    }

    public void setProximaVisita(Date proximaVisita) {
        this.proximaVisita = proximaVisita;
    }

    public List<String> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<String> imagenes) {
        this.imagenes = imagenes;
    }

    public ObjectId getCitaId() {
        return citaId;
    }
    
    public void setCitaId(ObjectId citaId) {
        this.citaId = citaId;
    }
} 