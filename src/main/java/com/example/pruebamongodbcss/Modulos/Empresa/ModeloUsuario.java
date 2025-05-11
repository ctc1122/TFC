package com.example.pruebamongodbcss.Modulos.Empresa;

import java.util.Date;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.example.pruebamongodbcss.Data.Usuario;

/**
 * Clase envoltorio para Usuario para facilitar su uso en la interfaz gráfica
 */
public class ModeloUsuario {
    
    public enum RolUsuario {
        ADMIN("Administrador"),
        VETERINARIO("Veterinario"),
        RECEPCIONISTA("Recepcionista"),
        AUXILIAR("Auxiliar"),
        NORMAL("Usuario Normal");
        
        private final String descripcion;
        
        RolUsuario(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
        
        public static RolUsuario fromUsuarioRol(Usuario.Rol rol) {
            switch (rol) {
                case ADMINISTRADOR: return ADMIN;
                case VETERINARIO: return VETERINARIO;
                case RECEPCIONISTA: return RECEPCIONISTA;
                case AUXILIAR: return AUXILIAR;
                case NORMAL:
                default: return NORMAL;
            }
        }
    }
    
    private Usuario usuario;
    
    /**
     * Crea un nuevo ModeloUsuario a partir de un Usuario
     */
    public ModeloUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    /**
     * Crea un nuevo ModeloUsuario a partir de un documento MongoDB
     */
    public ModeloUsuario(Document doc) {
        try {
            this.usuario = new Usuario(doc);
        } catch (Exception e) {
            System.err.println("Error al crear ModeloUsuario desde documento: " + e.getMessage());
            this.usuario = new Usuario();
        }
    }
    
    /**
     * Crea un nuevo ModeloUsuario vacío
     */
    public ModeloUsuario() {
        this.usuario = new Usuario();
    }
    
    /**
     * Obtiene el usuario interno
     */
    public Usuario getUsuario() {
        return usuario;
    }
    
    /**
     * Obtiene el ID del usuario
     */
    public ObjectId getId() {
        return usuario.getId();
    }
    
    /**
     * Obtiene el nombre de usuario
     */
    public String getUsername() {
        return usuario.getUsuario();
    }
    
    /**
     * Obtiene la contraseña
     */
    public String getPassword() {
        return usuario.getPassword();
    }
    
    /**
     * Obtiene el nombre completo
     */
    public String getNombreCompleto() {
        return usuario.getNombreCompleto();
    }
    
    /**
     * Obtiene el nombre
     */
    public String getNombre() {
        return usuario.getNombre();
    }
    
    /**
     * Obtiene el apellido
     */
    public String getApellido() {
        return usuario.getApellido();
    }
    
    /**
     * Obtiene el email
     */
    public String getEmail() {
        return usuario.getEmail();
    }
    
    /**
     * Obtiene el teléfono
     */
    public String getTelefono() {
        return usuario.getTelefono();
    }
    
    /**
     * Obtiene el rol en formato RolUsuario
     */
    public RolUsuario getRolUsuario() {
        return RolUsuario.fromUsuarioRol(usuario.getRol());
    }
    
    /**
     * Obtiene el rol en formato Usuario.Rol
     */
    public Usuario.Rol getRol() {
        return usuario.getRol();
    }
    
    /**
     * Verifica si el usuario está activo
     */
    public boolean isActivo() {
        return usuario.isActivo();
    }
    
    /**
     * Obtiene la fecha de creación
     */
    public Date getFechaCreacion() {
        return usuario.getFechaCreacion();
    }
    
    /**
     * Guarda los cambios en el usuario subyacente
     */
    public void guardar() {
        // Aquí puedes agregar lógica adicional antes de guardar
    }
    
    /**
     * Convierte el ModeloUsuario a un documento MongoDB
     */
    public Document toDocument() {
        return usuario.toDocument();
    }
    
    @Override
    public String toString() {
        return getNombreCompleto();
    }
    
    // Métodos de establecimiento y acceso adicionales si son necesarios
} 