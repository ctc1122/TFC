package com.example.pruebamongodbcss.Data;

import java.util.Date;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Usuario {

    public enum Rol {
        ADMINISTRADOR("Administrador"),
        VETERINARIO("Veterinario"),
        RECEPCIONISTA("Recepcionista"),
        AUXILIAR("Auxiliar"),
        NORMAL("Normal");
        
        private final String descripcion;
        
        Rol(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
        
        public static Rol fromString(String texto) {
            for (Rol rol : Rol.values()) {
                if (rol.name().equalsIgnoreCase(texto) || rol.getDescripcion().equalsIgnoreCase(texto)) {
                    return rol;
                }
            }
            return NORMAL; // Valor por defecto
        }
    }

    private ObjectId _id;
    private String nombre;
    private String apellido;
    private String usuario;
    private String password;
    private Rol rol;
    private Date fechaCreacion;
    private String email;
    private String telefono;
    private boolean activo;
    private ObjectId veterinarioId; // Referencia al ModeloVeterinario si el rol es VETERINARIO
    
    // Campos adicionales para veterinarios
    private String especialidad;
    private String numeroColegiado;
    private String horaInicio;
    private String horaFin;
    private boolean disponible;

    /* Constructores */
    
    /**
     * Constructor para crear un usuario normal
     */
    public Usuario(String nombre, String apellido, String usuario, String password, String email, String telefono) throws PatronExcepcion {
        this.setNombre(nombre);
        this.setApellido(apellido);
        this.setUsuario(usuario);
        this.setPassword(password);
        this.setEmail(email);
        this.setTelefono(telefono);
        this.rol = Rol.NORMAL;
        this.fechaCreacion = new Date();
        this.activo = true;
    }

    /**
     * Constructor para crear un nuevo usuario administrador
     * (ahora necesita que la solicitud venga de un usuario con privilegios de administrador)
     */
    public Usuario(String nombre, String apellido, String usuario, String password, String email, String telefono, String contrasenaAdmin) throws Exception {
        this.setNombre(nombre);
        this.setApellido(apellido);
        this.setUsuario(usuario);
        this.setPassword(password);
        this.setEmail(email);
        this.setTelefono(telefono);
        this.fechaCreacion = new Date();
        this.activo = true;
        
        // Ya no verificamos una contraseña hardcodeada
        // La verificación de privilegios de administrador se hace en el controlador
        this.rol = Rol.ADMINISTRADOR;
    }
    
    /**
     * Constructor completo para cualquier tipo de usuario
     */
    public Usuario(String nombre, String apellido, String usuario, String password, 
                  String email, String telefono, Rol rol, boolean activo,
                  String especialidad, String numeroColegiado, String horaInicio, String horaFin) throws PatronExcepcion {
        this.setNombre(nombre);
        this.setApellido(apellido);
        this.setUsuario(usuario);
        this.setPassword(password);
        this.setEmail(email);
        this.setTelefono(telefono);
        this.rol = rol;
        this.fechaCreacion = new Date();
        this.activo = activo;
        this.especialidad = especialidad;
        this.numeroColegiado = numeroColegiado;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.disponible = true;
    }
    
    /**
     * Constructor desde un documento MongoDB
     */
    public Usuario(Document doc) {
        try {
            if (doc.containsKey("_id")) {
                this._id = doc.getObjectId("_id");
            }
            
            // Campos obligatorios
            this.usuario = doc.getString("usuario");
            this.password = doc.getString("password");
            
            // Intentar obtener nombre y apellido, pero usar valores por defecto si hay error
            try {
                this.nombre = doc.getString("nombre");
            } catch (Exception e) {
                this.nombre = this.usuario; // Usar username como fallback
            }
            
            try {
                this.apellido = doc.getString("apellido");
            } catch (Exception e) {
                this.apellido = ""; // Apellido vacío como fallback
            }
            
            // Intentar obtener email, pero usar un valor predeterminado si hay error
            try {
                this.email = doc.getString("email");
                if (this.email == null || this.email.isEmpty()) {
                    this.email = this.usuario + "@clinica.com";
                }
            } catch (Exception e) {
                this.email = this.usuario + "@clinica.com"; // Email predeterminado
            }
            
            // Intentar obtener teléfono, pero usar un valor predeterminado si hay error
            try {
                this.telefono = doc.getString("telefono");
                if (this.telefono == null || this.telefono.isEmpty()) {
                    this.telefono = "000000000";
                }
            } catch (Exception e) {
                this.telefono = "000000000"; // Teléfono predeterminado
            }
            
            // Obtener rol o asignar NORMAL si no existe
            try {
                this.rol = Rol.valueOf(doc.getString("rol"));
            } catch (Exception e) {
                this.rol = Rol.NORMAL;
            }
            
            // Obtener fecha de creación o usar la actual
            try {
                this.fechaCreacion = doc.getDate("fechaCreacion");
            } catch (Exception e) {
                this.fechaCreacion = new Date();
            }
            
            // Campos opcionales
            this.activo = doc.getBoolean("activo", true);
            
            if (doc.containsKey("veterinarioId")) {
                this.veterinarioId = doc.getObjectId("veterinarioId");
            }
            
            // Campos específicos de veterinario
            if (this.rol == Rol.VETERINARIO) {
                this.especialidad = doc.containsKey("especialidad") ? doc.getString("especialidad") : "";
                this.numeroColegiado = doc.containsKey("numeroColegiado") ? doc.getString("numeroColegiado") : "";
                this.horaInicio = doc.containsKey("horaInicio") ? doc.getString("horaInicio") : "09:00";
                this.horaFin = doc.containsKey("horaFin") ? doc.getString("horaFin") : "17:00";
                this.disponible = doc.getBoolean("disponible", true);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al crear Usuario desde Document: " + e.getMessage(), e);
        }
    }
    
    /**
     * Constructor vacío para serialización
     */
    public Usuario() {
        this.fechaCreacion = new Date();
        this.rol = Rol.NORMAL;
        this.activo = true;
        this.disponible = true;
    }

    /**
     * Convierte el usuario a un documento MongoDB
     */
    public Document toDocument() {
        Document doc = new Document();
        
        if (_id != null) {
            doc.append("_id", _id);
        }
        
        doc.append("nombre", nombre)
           .append("apellido", apellido)
           .append("usuario", usuario)
           .append("password", password)
           .append("email", email)
           .append("telefono", telefono)
           .append("rol", rol.name())
           .append("fechaCreacion", fechaCreacion)
           .append("activo", activo);
        
        // Añadir campos específicos de veterinario si es necesario
        if (rol == Rol.VETERINARIO) {
            if (especialidad != null && !especialidad.isEmpty()) {
                doc.append("especialidad", especialidad);
            }
            
            if (numeroColegiado != null && !numeroColegiado.isEmpty()) {
                doc.append("numeroColegiado", numeroColegiado);
            }
            
            if (horaInicio != null && !horaInicio.isEmpty()) {
                doc.append("horaInicio", horaInicio);
            }
            
            if (horaFin != null && !horaFin.isEmpty()) {
                doc.append("horaFin", horaFin);
            }
            
            doc.append("disponible", disponible);
        }
        
        if (veterinarioId != null) {
            doc.append("veterinarioId", veterinarioId);
        }
        
        return doc;
    }

    /* Métodos */
    @Override
    public String toString() {
        return "Usuario{" +
                "nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", usuario='" + usuario + '\'' +
                ", email='" + email + '\'' +
                ", telefono='" + telefono + '\'' +
                ", rol='" + rol + '\'' +
                ", fechaCreacion='" + fechaCreacion + '\'' +
                '}';
    }
    
    /**
     * Devuelve el nombre completo del usuario
     */
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
    
    /**
     * Verifica si el usuario es administrador
     */
    public boolean esAdmin() {
        return rol == Rol.ADMINISTRADOR;
    }

    /* Setters y Getters */
    public void setNombre(String nombre) throws PatronExcepcion {
        if (nombre == null || nombre.isEmpty() || !nombre.matches("^[A-Za-zÁÉÍÓÚáéíóúñÑ ]{2,50}$")) {
            throw new PatronExcepcion("Nombre no válido");
        }
        this.nombre = nombre;
    }

    public void setApellido(String apellido) throws PatronExcepcion {
        if (apellido == null || apellido.isEmpty() || !apellido.matches("^[A-Za-zÁÉÍÓÚáéíóúñÑ ]{2,50}$")) {
            throw new PatronExcepcion("Apellido no válido");
        }
        this.apellido = apellido;
    }

    public void setUsuario(String usuario) throws PatronExcepcion {
        if (usuario == null || usuario.isEmpty() || !usuario.matches("^[a-zA-Z0-9]{4,16}$")) {
            throw new PatronExcepcion("Usuario no válido. Debe tener entre 4 y 16 caracteres alfanuméricos");
        }
        this.usuario = usuario;
    }

    public void setPassword(String password) throws PatronExcepcion {
        // Permitir contraseñas más cortas para fines de prueba y desarrollo
        if (password == null || password.isEmpty()) {
            throw new PatronExcepcion("Contraseña no válida. No puede estar vacía");
        }
        
        // Para contraseñas cortas, mostrar una advertencia pero permitirlas (para propósitos de prueba)
        if (password.length() < 8) {
            System.out.println("ADVERTENCIA: Contraseña corta (" + password.length() + 
                " caracteres). En producción, se recomienda mínimo 8 caracteres.");
        }
        
        this.password = password;
    }

    public void setEmail(String email) throws PatronExcepcion {
        if (email == null || email.isEmpty() || !email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new PatronExcepcion("Email no válido");
        }
        this.email = email;
    }

    public void setTelefono(String telefono) throws PatronExcepcion {
        if (telefono == null || telefono.isEmpty() || !telefono.matches("^[0-9]{9}$")) {
            throw new PatronExcepcion("Teléfono no válido. Debe tener 9 dígitos");
        }
        this.telefono = telefono;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }

    public Rol getRol() {
        return rol;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId id) {
        this._id = id;
    }
    
    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getNumeroColegiado() {
        return numeroColegiado;
    }

    public void setNumeroColegiado(String numeroColegiado) {
        this.numeroColegiado = numeroColegiado;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public ObjectId getVeterinarioId() {
        return veterinarioId;
    }

    public void setVeterinarioId(ObjectId veterinarioId) {
        this.veterinarioId = veterinarioId;
    }
}