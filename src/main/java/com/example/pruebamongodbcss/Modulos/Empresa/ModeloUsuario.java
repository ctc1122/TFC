package com.example.pruebamongodbcss.Modulos.Empresa;

import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.Date;

/**
 * Modelo de datos para representar a un usuario del sistema.
 */
public class ModeloUsuario {
    
    public enum RolUsuario {
        ADMIN("Administrador"),
        VETERINARIO("Veterinario"),
        RECEPCIONISTA("Recepcionista"),
        AUXILIAR("Auxiliar");
        
        private final String descripcion;
        
        RolUsuario(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
        
        public static RolUsuario fromString(String texto) {
            for (RolUsuario rol : RolUsuario.values()) {
                if (rol.name().equalsIgnoreCase(texto) || rol.getDescripcion().equalsIgnoreCase(texto)) {
                    return rol;
                }
            }
            return AUXILIAR; // Valor por defecto
        }
    }
    
    private ObjectId id;
    private String usuario;
    private String password;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private RolUsuario rol;
    private Date fechaCreacion;
    private boolean activo;
    private ObjectId veterinarioId; // Si es veterinario, referencia al modelo de veterinario
    
    /**
     * Constructor con todos los campos.
     */
    public ModeloUsuario(ObjectId id, String usuario, String password, String nombre, 
                        String apellido, String email, String telefono, RolUsuario rol, 
                        Date fechaCreacion, boolean activo, ObjectId veterinarioId) {
        this.id = id;
        this.usuario = usuario;
        this.password = password;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.telefono = telefono;
        this.rol = rol;
        this.fechaCreacion = fechaCreacion;
        this.activo = activo;
        this.veterinarioId = veterinarioId;
    }
    
    /**
     * Constructor desde un documento MongoDB.
     */
    public ModeloUsuario(Document doc) {
        this.id = doc.getObjectId("_id");
        this.usuario = doc.getString("usuario");
        this.password = doc.getString("password");
        this.nombre = doc.getString("nombre");
        this.apellido = doc.getString("apellido");
        this.email = doc.getString("email");
        this.telefono = doc.getString("telefono");
        this.rol = RolUsuario.fromString(doc.getString("rol"));
        this.fechaCreacion = doc.getDate("fechaCreacion");
        this.activo = doc.getBoolean("activo", true);
        
        if (doc.containsKey("veterinarioId")) {
            Object vetId = doc.get("veterinarioId");
            if (vetId instanceof ObjectId) {
                this.veterinarioId = (ObjectId) vetId;
            }
        }
    }
    
    /**
     * Constructor vacío.
     */
    public ModeloUsuario() {
        this.activo = true;
        this.fechaCreacion = new Date();
        this.rol = RolUsuario.AUXILIAR;
    }
    
    /**
     * Convierte el modelo a un documento MongoDB.
     */
    public Document toDocument() {
        Document doc = new Document();
        
        if (id != null) {
            doc.append("_id", id);
        }
        
        doc.append("usuario", usuario)
           .append("password", password)
           .append("nombre", nombre)
           .append("apellido", apellido)
           .append("email", email)
           .append("telefono", telefono)
           .append("rol", rol.name())
           .append("fechaCreacion", fechaCreacion)
           .append("activo", activo);
        
        if (veterinarioId != null) {
            doc.append("veterinarioId", veterinarioId);
        }
        
        return doc;
    }
    
    /**
     * Verifica si el usuario es administrador.
     */
    public boolean esAdmin() {
        return rol == RolUsuario.ADMIN;
    }
    
    /**
     * Devuelve el nombre completo del usuario.
     */
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
    
    // Getters y setters
    
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
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

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public ObjectId getVeterinarioId() {
        return veterinarioId;
    }

    public void setVeterinarioId(ObjectId veterinarioId) {
        this.veterinarioId = veterinarioId;
    }
} 