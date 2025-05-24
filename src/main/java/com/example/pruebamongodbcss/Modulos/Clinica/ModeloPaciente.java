package com.example.pruebamongodbcss.Modulos.Clinica;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Modelo que representa a un paciente (animal) en la clínica veterinaria.
 */
public class ModeloPaciente implements Serializable{
    private static final long serialVersionUID = 1L;
    
    private ObjectId id;
    private String nombre;
    private String especie;
    private String raza;
    private Date fechaNacimiento;
    private double peso;
    private String sexo;
    private boolean esterilizado;
    private String color;
    private String microchip;
    private ObjectId propietarioId;
    private String nombrePropietario;
    private List<String> alergias;
    private List<String> vacunas;
    private String observaciones;
    private Date ultimaVisita;
    private String estadoPaciente;
    
    // Propiedad para control de edición en la UI (transient)
    private transient boolean editando;
    
    public ModeloPaciente() {
        this.alergias = new ArrayList<>();
        this.vacunas = new ArrayList<>();
        this.estadoPaciente = "Activo";
        this.editando = false;
    }
    
    // Constructor a partir de un documento de MongoDB
    public ModeloPaciente(Document doc) {
        this.id = doc.getObjectId("_id");
        this.nombre = doc.getString("nombre");
        this.especie = doc.getString("especie");
        this.raza = doc.getString("raza");
        this.fechaNacimiento = doc.getDate("fechaNacimiento");
        this.peso = doc.getDouble("peso");
        this.sexo = doc.getString("sexo");
        this.esterilizado = doc.getBoolean("esterilizado", false);
        this.color = doc.getString("color");
        this.microchip = doc.getString("microchip");
        this.propietarioId = doc.getObjectId("propietarioId");
        this.nombrePropietario = doc.getString("nombrePropietario");
        
        List<String> alergiasDoc = doc.getList("alergias", String.class);
        this.alergias = alergiasDoc != null ? alergiasDoc : new ArrayList<>();
        
        List<String> vacunasDoc = doc.getList("vacunas", String.class);
        this.vacunas = vacunasDoc != null ? vacunasDoc : new ArrayList<>();
        
        this.observaciones = doc.getString("observaciones");
        this.ultimaVisita = doc.getDate("ultimaVisita");
        this.estadoPaciente = doc.getString("estadoPaciente");
        
        // Si no hay estado, asignar "Activo" por defecto
        if (this.estadoPaciente == null) {
            this.estadoPaciente = "Activo";
        }
        
        // La propiedad editando es transient, no se carga de la BD
        this.editando = false;
    }
    
    // Convertir a documento para MongoDB
    public Document toDocument() {
        Document doc = new Document();
        if (id != null) {
            doc.append("_id", id);
        }
        doc.append("nombre", nombre)
           .append("especie", especie)
           .append("raza", raza)
           .append("fechaNacimiento", fechaNacimiento)
           .append("peso", peso)
           .append("sexo", sexo)
           .append("esterilizado", esterilizado)
           .append("color", color)
           .append("microchip", microchip)
           .append("propietarioId", propietarioId)
           .append("nombrePropietario", nombrePropietario)
           .append("alergias", alergias)
           .append("vacunas", vacunas)
           .append("observaciones", observaciones)
           .append("ultimaVisita", ultimaVisita)
           .append("estadoPaciente", estadoPaciente);
        return doc;
    }

    // Getters y setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public boolean isEsterilizado() {
        return esterilizado;
    }

    public void setEsterilizado(boolean esterilizado) {
        this.esterilizado = esterilizado;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getMicrochip() {
        return microchip;
    }

    public void setMicrochip(String microchip) {
        this.microchip = microchip;
    }

    public ObjectId getPropietarioId() {
        return propietarioId;
    }

    public void setPropietarioId(ObjectId propietarioId) {
        this.propietarioId = propietarioId;
    }

    public String getNombrePropietario() {
        return nombrePropietario;
    }

    public void setNombrePropietario(String nombrePropietario) {
        this.nombrePropietario = nombrePropietario;
    }

    public List<String> getAlergias() {
        return alergias;
    }

    public void setAlergias(List<String> alergias) {
        this.alergias = alergias;
    }

    public List<String> getVacunas() {
        return vacunas;
    }

    public void setVacunas(List<String> vacunas) {
        this.vacunas = vacunas;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Date getUltimaVisita() {
        return ultimaVisita;
    }

    public void setUltimaVisita(Date ultimaVisita) {
        this.ultimaVisita = ultimaVisita;
    }
    
    public String getEstadoPaciente() {
        return estadoPaciente;
    }
    
    public void setEstadoPaciente(String estadoPaciente) {
        this.estadoPaciente = estadoPaciente;
    }
    
    // Getters y setters para la propiedad editando
    public boolean isEditando() {
        return editando;
    }
    
    public void setEditando(boolean editando) {
        this.editando = editando;
    }
} 