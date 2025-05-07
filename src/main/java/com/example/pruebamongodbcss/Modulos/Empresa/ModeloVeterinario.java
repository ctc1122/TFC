package com.example.pruebamongodbcss.Modulos.Empresa;

import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.Date;

/**
 * Modelo de datos para representar a un veterinario en el sistema.
 */
public class ModeloVeterinario {
    private ObjectId id;
    private String nombre;
    private String apellidos;
    private String dni;
    private String numeroTitulo;
    private String especialidad;
    private String email;
    private String telefono;
    private Date fechaContratacion;
    private boolean activo;
    
    /**
     * Constructor con todos los campos.
     */
    public ModeloVeterinario(ObjectId id, String nombre, String apellidos, String dni, 
                            String numeroTitulo, String especialidad, String email, 
                            String telefono, Date fechaContratacion, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.dni = dni;
        this.numeroTitulo = numeroTitulo;
        this.especialidad = especialidad;
        this.email = email;
        this.telefono = telefono;
        this.fechaContratacion = fechaContratacion;
        this.activo = activo;
    }
    
    /**
     * Constructor desde un documento MongoDB.
     */
    public ModeloVeterinario(Document doc) {
        this.id = doc.getObjectId("_id");
        this.nombre = doc.getString("nombre");
        this.apellidos = doc.getString("apellidos");
        this.dni = doc.getString("dni");
        this.numeroTitulo = doc.getString("numeroTitulo");
        this.especialidad = doc.getString("especialidad");
        this.email = doc.getString("email");
        this.telefono = doc.getString("telefono");
        this.fechaContratacion = doc.getDate("fechaContratacion");
        this.activo = doc.getBoolean("activo", true);
    }
    
    /**
     * Constructor vacío.
     */
    public ModeloVeterinario() {
        this.activo = true;
        this.fechaContratacion = new Date();
    }
    
    /**
     * Convierte el modelo a un documento MongoDB.
     */
    public Document toDocument() {
        Document doc = new Document();
        
        if (id != null) {
            doc.append("_id", id);
        }
        
        doc.append("nombre", nombre)
           .append("apellidos", apellidos)
           .append("dni", dni)
           .append("numeroTitulo", numeroTitulo)
           .append("especialidad", especialidad)
           .append("email", email)
           .append("telefono", telefono)
           .append("fechaContratacion", fechaContratacion)
           .append("activo", activo);
        
        return doc;
    }
    
    /**
     * Devuelve el nombre completo del veterinario.
     */
    public String getNombreCompleto() {
        return nombre + " " + apellidos;
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

    public String getNumeroTitulo() {
        return numeroTitulo;
    }

    public void setNumeroTitulo(String numeroTitulo) {
        this.numeroTitulo = numeroTitulo;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Date getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(Date fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
} 