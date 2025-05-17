package com.example.pruebamongodbcss.Modulos.Farmacia;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Modelo que representa un medicamento de la base de datos CIMA.
 */
public class ModeloMedicamentoCIMA {
    private final StringProperty codigo;
    private final StringProperty nombre;
    
    /**
     * Constructor con valores por defecto.
     */
    public ModeloMedicamentoCIMA() {
        this.codigo = new SimpleStringProperty();
        this.nombre = new SimpleStringProperty();
    }
    
    /**
     * Constructor con parámetros básicos.
     */
    public ModeloMedicamentoCIMA(String codigo, String nombre) {
        this();
        this.codigo.set(codigo);
        this.nombre.set(nombre);
    }
    
    // Getters y setters como propiedades para JavaFX
    
    public String getCodigo() {
        return codigo.get();
    }
    
    public StringProperty codigoProperty() {
        return codigo;
    }
    
    public void setCodigo(String codigo) {
        this.codigo.set(codigo);
    }
    
    public String getNombre() {
        return nombre.get();
    }
    
    public StringProperty nombreProperty() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre.set(nombre);
    }
    
    @Override
    public String toString() {
        return nombre.get() + " (" + codigo.get() + ")";
    }
} 