package com.example.pruebamongodbcss.Data;

/**
 * Enumerado que representa los posibles estados de una cita médica
 */
public enum EstadoCita {
    PENDIENTE("Pendiente"),
    EN_CURSO("En curso"),
    COMPLETADA("Completada"),
    CANCELADA("Cancelada"),
    REPROGRAMADA("Reprogramada");

    private final String descripcion;

    EstadoCita(String descripcion) {
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