package com.example.pruebamongodbcss.Modulos.Clinica;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Modelo que representa a un propietario de pacientes en la clínica veterinaria.
 */
public class ModeloPropietario implements Serializable{
    private static final long serialVersionUID = 1L;
    
    private ObjectId id;
    private String nombre;
    private String apellidos;
    private String dni;
    private String direccion;
    private String telefono;
    private String email;
    private List<ObjectId> mascotas;
    private Date fechaAlta;
    private Date fechaRegistro; // Fecha específica para estadísticas
    private String observaciones;
    
    // Propiedad para control de edición en la UI (transient)
    private transient boolean editando;
    
    public ModeloPropietario() {
        this.mascotas = new ArrayList<>();
        this.editando = false;
        this.fechaRegistro = new Date(); // Asignar fecha actual automáticamente
        if (this.fechaAlta == null) {
            this.fechaAlta = new Date(); // Por compatibilidad
        }
    }
    
    // Constructor a partir de un documento de MongoDB
    public ModeloPropietario(Document doc) {
        this.id = doc.getObjectId("_id");
        this.nombre = doc.getString("nombre");
        this.apellidos = doc.getString("apellidos");
        this.dni = doc.getString("dni");
        this.direccion = doc.getString("direccion");
        this.telefono = doc.getString("telefono");
        this.email = doc.getString("email");
        
        List<ObjectId> mascotasDoc = doc.getList("mascotas", ObjectId.class);
        this.mascotas = mascotasDoc != null ? mascotasDoc : new ArrayList<>();
        
        this.fechaAlta = doc.getDate("fechaAlta");
        this.fechaRegistro = doc.getDate("fechaRegistro");
        // Si no existe fechaRegistro, usar fechaAlta como fallback
        if (this.fechaRegistro == null && this.fechaAlta != null) {
            this.fechaRegistro = this.fechaAlta;
        } else if (this.fechaRegistro == null) {
            this.fechaRegistro = new Date();
        }
        this.observaciones = doc.getString("observaciones");
        
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
           .append("apellidos", apellidos)
           .append("dni", dni)
           .append("direccion", direccion)
           .append("telefono", telefono)
           .append("email", email)
           .append("mascotas", mascotas)
           .append("fechaAlta", fechaAlta)
           .append("fechaRegistro", fechaRegistro)
           .append("observaciones", observaciones);
        return doc;
    }
    
    // Método para añadir una mascota al propietario
    public void addMascota(ObjectId mascotaId) {
        if (!mascotas.contains(mascotaId)) {
            mascotas.add(mascotaId);
        }
    }
    
    // Método para eliminar una mascota del propietario
    public void removeMascota(ObjectId mascotaId) {
        mascotas.remove(mascotaId);
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

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<ObjectId> getMascotas() {
        return mascotas;
    }

    public void setMascotas(List<ObjectId> mascotas) {
        this.mascotas = mascotas;
    }

    public Date getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(Date fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    // Método para obtener el nombre completo
    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }
    
    // Getters y setters para la propiedad editando
    public boolean isEditando() {
        return editando;
    }
    
    public void setEditando(boolean editando) {
        this.editando = editando;
    }
} 