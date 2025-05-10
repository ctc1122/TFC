package com.example.pruebamongodbcss.Data;

/**
 * Enumerado que define los posibles roles de un usuario en el sistema.
 */
public enum Rol {
    NORMAL("Usuario Normal"),
    ADMINISTRADOR("Administrador"),
    VETERINARIO("Veterinario"),
    RECEPCIONISTA("Recepcionista"),
    AUXILIAR("Auxiliar");
    
    private final String descripcion;
    
    Rol(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    /**
     * Convierte un texto en un valor del enumerado Rol.
     * Busca tanto por el nombre del enum como por la descripción.
     * 
     * @param texto El texto a convertir
     * @return El valor del enum, o NORMAL si no se encuentra una coincidencia
     */
    public static Rol fromString(String texto) {
        if (texto == null || texto.isEmpty()) {
            return NORMAL;
        }
        
        for (Rol rol : Rol.values()) {
            if (rol.name().equalsIgnoreCase(texto) || rol.getDescripcion().equalsIgnoreCase(texto)) {
                return rol;
            }
        }
        return NORMAL; // Valor por defecto
    }
} 