package com.example.pruebamongodbcss.Data;

/**
 * Enumerado que representa los posibles estados de un paciente en la clínica veterinaria
 */
public enum EstadoPaciente {
    ACTIVO("Activo"),
    INACTIVO("Inactivo"),
    FALLECIDO("Fallecido");

    private final String descripcion;

    EstadoPaciente(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
} 