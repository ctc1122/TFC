package com.example.pruebamongodbcss.Modulos.Videollamada;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Usuario {
    private final StringProperty nombre;
    private final StringProperty estado;
    private String id;
    private String salaActual;

    public Usuario(String id, String nombre) {
        this.id = id;
        this.nombre = new SimpleStringProperty(nombre);
        this.estado = new SimpleStringProperty("Disponible");
        this.salaActual = null;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre.get();
    }

    public StringProperty nombreProperty() {
        return nombre;
    }

    public String getEstado() {
        return estado.get();
    }

    public void setEstado(String estado) {
        this.estado.set(estado);
    }

    public StringProperty estadoProperty() {
        return estado;
    }

    public String getSalaActual() {
        return salaActual;
    }

    public void setSalaActual(String salaActual) {
        this.salaActual = salaActual;
    }

    @Override
    public String toString() {
        return nombre.get();
    }
} 