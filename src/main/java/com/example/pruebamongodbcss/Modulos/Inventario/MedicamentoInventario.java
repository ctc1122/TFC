package com.example.pruebamongodbcss.Modulos.Inventario;

import org.bson.Document;

/**
 * Modelo simplificado para medicamentos del inventario
 * Trabaja directamente con Document de MongoDB
 */
public class MedicamentoInventario {
    
    private Document documento;
    
    public MedicamentoInventario(Document documento) {
        this.documento = documento != null ? documento : new Document();
    }
    
    // Getters que extraen datos del Document
    public String getCodigo() {
        return documento.getString("codigo");
    }
    
    public String getNombre() {
        return documento.getString("nombre");
    }
    
    public String getLaboratorio() {
        return documento.getString("laboratorio");
    }
    
    public String getDimension() {
        return documento.getString("dimension");
    }
    
    public String getViaAdmin() {
        return documento.getString("ViaAdmin");
    }
    
    public int getUnidades() {
        return documento.getInteger("unidades", 0);
    }
    
    public double getPrecio() {
        return documento.getDouble("precio") != null ? documento.getDouble("precio") : 0.0;
    }
    
    // Métodos de utilidad
    public boolean tieneStock() {
        return getUnidades() > 0;
    }
    
    public boolean haySuficienteStock(int cantidadRequerida) {
        return getUnidades() >= cantidadRequerida;
    }
    
    public String getNombreCompleto() {
        String nombre = getNombre();
        String dimension = getDimension();
        
        if (nombre == null) nombre = "";
        if (dimension != null && !dimension.isEmpty()) {
            nombre += " - " + dimension;
        }
        
        return nombre;
    }
    
    public String getResumen() {
        StringBuilder resumen = new StringBuilder();
        resumen.append(getNombreCompleto());
        
        String laboratorio = getLaboratorio();
        if (laboratorio != null && !laboratorio.isEmpty()) {
            resumen.append(" (").append(laboratorio).append(")");
        }
        
        String via = getViaAdmin();
        if (via != null && !via.isEmpty()) {
            resumen.append(" - ").append(via);
        }
        
        return resumen.toString();
    }
    
    public boolean coincideConBusqueda(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return true;
        }
        
        String terminoLower = termino.toLowerCase();
        
        return (getNombre() != null && getNombre().toLowerCase().contains(terminoLower)) ||
               (getCodigo() != null && getCodigo().toLowerCase().contains(terminoLower)) ||
               (getLaboratorio() != null && getLaboratorio().toLowerCase().contains(terminoLower)) ||
               (getDimension() != null && getDimension().toLowerCase().contains(terminoLower)) ||
               (getViaAdmin() != null && getViaAdmin().toLowerCase().contains(terminoLower));
    }
    
    // Getter para el documento original
    public Document getDocumento() {
        return documento;
    }
    
    @Override
    public String toString() {
        return getNombreCompleto() + " (Stock: " + getUnidades() + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        MedicamentoInventario that = (MedicamentoInventario) obj;
        String codigo = getCodigo();
        String otherCodigo = that.getCodigo();
        
        return codigo != null ? codigo.equals(otherCodigo) : otherCodigo == null;
    }
    
    @Override
    public int hashCode() {
        String codigo = getCodigo();
        return codigo != null ? codigo.hashCode() : 0;
    }
    
    // Setters para permitir edición
    public void setLaboratorio(String laboratorio) {
        documento.put("laboratorio", laboratorio);
    }
    
    public void setPrecio(double precio) {
        documento.put("precio", precio);
    }
    
    public void setDimension(String dimension) {
        documento.put("dimension", dimension);
    }
    
    public void setViaAdmin(String viaAdmin) {
        documento.put("ViaAdmin", viaAdmin);
    }
    
    public void setUnidades(int unidades) {
        documento.put("unidades", unidades);
    }
    
    // Método para actualizar el documento completo
    public void actualizarDocumento(Document nuevoDocumento) {
        if (nuevoDocumento != null) {
            this.documento = nuevoDocumento;
        }
    }
} 