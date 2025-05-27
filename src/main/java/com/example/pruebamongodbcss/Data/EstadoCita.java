package com.example.pruebamongodbcss.Data;

/**
 * Enumerado que representa los posibles estados de una cita médica
 */
public enum EstadoCita {
    PENDIENTE("Pendiente", "#FFA500"), // naranja
    EN_SALA_DE_ESPERA("En sala de espera", "#FFFF00"), // amarillo
    EN_CURSO("En curso", "#2196F3"), // azul
    PENDIENTE_DE_FACTURAR("Pendiente de facturar", "#4CAF50"), // verde
    COMPLETADA("Completada", "#FFFFFF"), // blanco
    CANCELADA("Cancelada", "#E57373"), // rojo claro
    ABSENTISMO("Absentismo", "#FF5722"), // rojo oscuro
    REPROGRAMADA("Reprogramada", "#BDBDBD"); // gris

    private final String descripcion;
    private final String colorHex;

    EstadoCita(String descripcion, String colorHex) {
        this.descripcion = descripcion;
        this.colorHex = colorHex;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getColorHex() {
        return colorHex;
    }

    @Override
    public String toString() {
        return descripcion;
    }
} 