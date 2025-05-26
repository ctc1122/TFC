package com.example.pruebamongodbcss.Modulos.Inventario;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Modelo para representar un medicamento del inventario
 * Incluye información de CIMA y stock disponible
 */
public class ModeloMedicamentoInventario {
    
    // Propiedades básicas del medicamento
    private final StringProperty codigo = new SimpleStringProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty laboratorio = new SimpleStringProperty();
    private final StringProperty principioActivo = new SimpleStringProperty();
    private final StringProperty formaFarmaceutica = new SimpleStringProperty();
    private final StringProperty dosis = new SimpleStringProperty();
    private final StringProperty via = new SimpleStringProperty();
    private final StringProperty presentacion = new SimpleStringProperty();
    
    // Información de stock
    private final IntegerProperty unidadesDisponibles = new SimpleIntegerProperty();
    private final DoubleProperty precioUnitario = new SimpleDoubleProperty();
    private final StringProperty ubicacion = new SimpleStringProperty();
    private final StringProperty lote = new SimpleStringProperty();
    private final StringProperty fechaCaducidad = new SimpleStringProperty();
    
    // Información adicional
    private final StringProperty categoria = new SimpleStringProperty();
    private final StringProperty descripcion = new SimpleStringProperty();
    private final BooleanProperty requiereReceta = new SimpleBooleanProperty();
    private final StringProperty observaciones = new SimpleStringProperty();
    
    // Constructores
    public ModeloMedicamentoInventario() {
        // Constructor vacío
    }
    
    public ModeloMedicamentoInventario(String codigo, String nombre, int unidadesDisponibles) {
        setCodigo(codigo);
        setNombre(nombre);
        setUnidadesDisponibles(unidadesDisponibles);
    }
    
    // Getters y Setters para código
    public String getCodigo() {
        return codigo.get();
    }
    
    public void setCodigo(String codigo) {
        this.codigo.set(codigo);
    }
    
    public StringProperty codigoProperty() {
        return codigo;
    }
    
    // Getters y Setters para nombre
    public String getNombre() {
        return nombre.get();
    }
    
    public void setNombre(String nombre) {
        this.nombre.set(nombre);
    }
    
    public StringProperty nombreProperty() {
        return nombre;
    }
    
    // Getters y Setters para laboratorio
    public String getLaboratorio() {
        return laboratorio.get();
    }
    
    public void setLaboratorio(String laboratorio) {
        this.laboratorio.set(laboratorio);
    }
    
    public StringProperty laboratorioProperty() {
        return laboratorio;
    }
    
    // Getters y Setters para principio activo
    public String getPrincipioActivo() {
        return principioActivo.get();
    }
    
    public void setPrincipioActivo(String principioActivo) {
        this.principioActivo.set(principioActivo);
    }
    
    public StringProperty principioActivoProperty() {
        return principioActivo;
    }
    
    // Getters y Setters para forma farmacéutica
    public String getFormaFarmaceutica() {
        return formaFarmaceutica.get();
    }
    
    public void setFormaFarmaceutica(String formaFarmaceutica) {
        this.formaFarmaceutica.set(formaFarmaceutica);
    }
    
    public StringProperty formaFarmaceuticaProperty() {
        return formaFarmaceutica;
    }
    
    // Getters y Setters para dosis
    public String getDosis() {
        return dosis.get();
    }
    
    public void setDosis(String dosis) {
        this.dosis.set(dosis);
    }
    
    public StringProperty dosisProperty() {
        return dosis;
    }
    
    // Getters y Setters para vía
    public String getVia() {
        return via.get();
    }
    
    public void setVia(String via) {
        this.via.set(via);
    }
    
    public StringProperty viaProperty() {
        return via;
    }
    
    // Getters y Setters para presentación
    public String getPresentacion() {
        return presentacion.get();
    }
    
    public void setPresentacion(String presentacion) {
        this.presentacion.set(presentacion);
    }
    
    public StringProperty presentacionProperty() {
        return presentacion;
    }
    
    // Getters y Setters para unidades disponibles
    public int getUnidadesDisponibles() {
        return unidadesDisponibles.get();
    }
    
    public void setUnidadesDisponibles(int unidadesDisponibles) {
        this.unidadesDisponibles.set(unidadesDisponibles);
    }
    
    public IntegerProperty unidadesDisponiblesProperty() {
        return unidadesDisponibles;
    }
    
    // Getters y Setters para precio unitario
    public double getPrecioUnitario() {
        return precioUnitario.get();
    }
    
    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario.set(precioUnitario);
    }
    
    public DoubleProperty precioUnitarioProperty() {
        return precioUnitario;
    }
    
    // Getters y Setters para ubicación
    public String getUbicacion() {
        return ubicacion.get();
    }
    
    public void setUbicacion(String ubicacion) {
        this.ubicacion.set(ubicacion);
    }
    
    public StringProperty ubicacionProperty() {
        return ubicacion;
    }
    
    // Getters y Setters para lote
    public String getLote() {
        return lote.get();
    }
    
    public void setLote(String lote) {
        this.lote.set(lote);
    }
    
    public StringProperty loteProperty() {
        return lote;
    }
    
    // Getters y Setters para fecha de caducidad
    public String getFechaCaducidad() {
        return fechaCaducidad.get();
    }
    
    public void setFechaCaducidad(String fechaCaducidad) {
        this.fechaCaducidad.set(fechaCaducidad);
    }
    
    public StringProperty fechaCaducidadProperty() {
        return fechaCaducidad;
    }
    
    // Getters y Setters para categoría
    public String getCategoria() {
        return categoria.get();
    }
    
    public void setCategoria(String categoria) {
        this.categoria.set(categoria);
    }
    
    public StringProperty categoriaProperty() {
        return categoria;
    }
    
    // Getters y Setters para descripción
    public String getDescripcion() {
        return descripcion.get();
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion.set(descripcion);
    }
    
    public StringProperty descripcionProperty() {
        return descripcion;
    }
    
    // Getters y Setters para requiere receta
    public boolean isRequiereReceta() {
        return requiereReceta.get();
    }
    
    public void setRequiereReceta(boolean requiereReceta) {
        this.requiereReceta.set(requiereReceta);
    }
    
    public BooleanProperty requiereRecetaProperty() {
        return requiereReceta;
    }
    
    // Getters y Setters para observaciones
    public String getObservaciones() {
        return observaciones.get();
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones.set(observaciones);
    }
    
    public StringProperty observacionesProperty() {
        return observaciones;
    }
    
    // Métodos de utilidad
    
    /**
     * Verifica si el medicamento está disponible (tiene stock)
     */
    public boolean estaDisponible() {
        return getUnidadesDisponibles() > 0;
    }
    
    /**
     * Verifica si hay suficiente stock para una cantidad específica
     */
    public boolean haySuficienteStock(int cantidadRequerida) {
        return getUnidadesDisponibles() >= cantidadRequerida;
    }
    
    /**
     * Obtiene el nombre completo del medicamento (nombre + presentación)
     */
    public String getNombreCompleto() {
        String nombreCompleto = getNombre() != null ? getNombre() : "";
        if (getPresentacion() != null && !getPresentacion().isEmpty()) {
            nombreCompleto += " - " + getPresentacion();
        }
        return nombreCompleto;
    }
    
    /**
     * Obtiene información resumida del medicamento
     */
    public String getResumen() {
        StringBuilder resumen = new StringBuilder();
        resumen.append(getNombreCompleto());
        
        if (getLaboratorio() != null && !getLaboratorio().isEmpty()) {
            resumen.append(" (").append(getLaboratorio()).append(")");
        }
        
        if (getPrincipioActivo() != null && !getPrincipioActivo().isEmpty()) {
            resumen.append(" - ").append(getPrincipioActivo());
        }
        
        return resumen.toString();
    }
    
    /**
     * Verifica si el medicamento coincide con un término de búsqueda
     */
    public boolean coincideConBusqueda(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return true;
        }
        
        String terminoLower = termino.toLowerCase();
        
        return (getNombre() != null && getNombre().toLowerCase().contains(terminoLower)) ||
               (getCodigo() != null && getCodigo().toLowerCase().contains(terminoLower)) ||
               (getLaboratorio() != null && getLaboratorio().toLowerCase().contains(terminoLower)) ||
               (getPrincipioActivo() != null && getPrincipioActivo().toLowerCase().contains(terminoLower)) ||
               (getFormaFarmaceutica() != null && getFormaFarmaceutica().toLowerCase().contains(terminoLower)) ||
               (getCategoria() != null && getCategoria().toLowerCase().contains(terminoLower));
    }
    
    @Override
    public String toString() {
        return getNombreCompleto() + " (Stock: " + getUnidadesDisponibles() + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ModeloMedicamentoInventario that = (ModeloMedicamentoInventario) obj;
        return getCodigo() != null ? getCodigo().equals(that.getCodigo()) : that.getCodigo() == null;
    }
    
    @Override
    public int hashCode() {
        return getCodigo() != null ? getCodigo().hashCode() : 0;
    }
} 