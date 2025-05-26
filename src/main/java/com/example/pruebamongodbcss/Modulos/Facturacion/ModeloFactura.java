package com.example.pruebamongodbcss.Modulos.Facturacion;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Modelo de factura para el sistema de facturación veterinaria
 * Cumple con los requisitos legales españoles para facturación
 */
public class ModeloFactura implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Identificadores
    private ObjectId id;
    private String numeroFactura;
    private ObjectId citaId; // Referencia a la cita asociada
    
    // Datos del cliente
    private ObjectId propietarioId;
    private String nombreCliente;
    private String dniCliente;
    private String direccionCliente;
    private String telefonoCliente;
    private String emailCliente;
    
    // Datos del paciente
    private ObjectId pacienteId;
    private String nombrePaciente;
    private String especiePaciente;
    private String razaPaciente;
    
    // Datos de la factura
    private Date fechaEmision;
    private Date fechaVencimiento;
    private EstadoFactura estado;
    private boolean esBorrador;
    
    // Datos del veterinario
    private String veterinarioNombre;
    private String veterinarioId;
    
    // Servicios y conceptos
    private List<ConceptoFactura> servicios;
    private List<ConceptoFactura> medicamentos;
    
    // Importes
    private double subtotal;
    private double ivaGeneral; // 21% en España
    private double ivaMedicamentos; // 10% para medicamentos veterinarios
    private double total;
    
    // Observaciones
    private String observaciones;
    private String metodoPago;
    
    // Metadatos
    private Date fechaCreacion;
    private Date fechaModificacion;
    private String usuarioCreacion;
    
    public enum EstadoFactura {
        BORRADOR("Borrador"),
        EMITIDA("Emitida"),
        PAGADA("Pagada"),
        VENCIDA("Vencida"),
        ANULADA("Anulada");
        
        private final String descripcion;
        
        EstadoFactura(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
    
    /**
     * Clase interna para representar conceptos de facturación
     */
    public static class ConceptoFactura implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String descripcion;
        private int cantidad;
        private double precioUnitario;
        private double descuento; // Porcentaje de descuento
        private double tipoIva; // Porcentaje de IVA
        private double subtotal;
        private double importeIva;
        private double total;
        
        public ConceptoFactura() {
            this.cantidad = 1;
            this.descuento = 0.0;
            this.tipoIva = 21.0; // IVA general por defecto
        }
        
        public ConceptoFactura(String descripcion, int cantidad, double precioUnitario, double tipoIva) {
            this();
            this.descripcion = descripcion;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.tipoIva = tipoIva;
            calcularImportes();
        }
        
        public void calcularImportes() {
            this.subtotal = cantidad * precioUnitario * (1 - descuento / 100);
            this.importeIva = subtotal * (tipoIva / 100);
            this.total = subtotal + importeIva;
        }
        
        // Getters y setters
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        
        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { 
            this.cantidad = cantidad; 
            calcularImportes();
        }
        
        public double getPrecioUnitario() { return precioUnitario; }
        public void setPrecioUnitario(double precioUnitario) { 
            this.precioUnitario = precioUnitario; 
            calcularImportes();
        }
        
        public double getDescuento() { return descuento; }
        public void setDescuento(double descuento) { 
            this.descuento = descuento; 
            calcularImportes();
        }
        
        public double getTipoIva() { return tipoIva; }
        public void setTipoIva(double tipoIva) { 
            this.tipoIva = tipoIva; 
            calcularImportes();
        }
        
        public double getSubtotal() { return subtotal; }
        public double getImporteIva() { return importeIva; }
        public double getTotal() { return total; }
    }
    
    // Constructores
    public ModeloFactura() {
        this.servicios = new ArrayList<>();
        this.medicamentos = new ArrayList<>();
        this.estado = EstadoFactura.BORRADOR;
        this.esBorrador = true;
        this.fechaCreacion = new Date();
        this.fechaEmision = new Date();
        this.fechaVencimiento = Date.from(LocalDate.now().plusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
    
    public ModeloFactura(Document doc) {
        this();
        if (doc != null) {
            this.id = doc.getObjectId("_id");
            this.numeroFactura = doc.getString("numeroFactura");
            this.citaId = doc.getObjectId("citaId");
            
            // Datos del cliente
            this.propietarioId = doc.getObjectId("propietarioId");
            this.nombreCliente = doc.getString("nombreCliente");
            this.dniCliente = doc.getString("dniCliente");
            this.direccionCliente = doc.getString("direccionCliente");
            this.telefonoCliente = doc.getString("telefonoCliente");
            this.emailCliente = doc.getString("emailCliente");
            
            // Datos del paciente
            this.pacienteId = doc.getObjectId("pacienteId");
            this.nombrePaciente = doc.getString("nombrePaciente");
            this.especiePaciente = doc.getString("especiePaciente");
            this.razaPaciente = doc.getString("razaPaciente");
            
            // Fechas
            this.fechaEmision = doc.getDate("fechaEmision");
            this.fechaVencimiento = doc.getDate("fechaVencimiento");
            this.fechaCreacion = doc.getDate("fechaCreacion");
            this.fechaModificacion = doc.getDate("fechaModificacion");
            
            // Estado
            String estadoStr = doc.getString("estado");
            if (estadoStr != null) {
                this.estado = EstadoFactura.valueOf(estadoStr);
            }
            this.esBorrador = doc.getBoolean("esBorrador", true);
            
            // Veterinario
            this.veterinarioNombre = doc.getString("veterinarioNombre");
            this.veterinarioId = doc.getString("veterinarioId");
            
            // Importes
            this.subtotal = doc.getDouble("subtotal") != null ? doc.getDouble("subtotal") : 0.0;
            this.ivaGeneral = doc.getDouble("ivaGeneral") != null ? doc.getDouble("ivaGeneral") : 0.0;
            this.ivaMedicamentos = doc.getDouble("ivaMedicamentos") != null ? doc.getDouble("ivaMedicamentos") : 0.0;
            this.total = doc.getDouble("total") != null ? doc.getDouble("total") : 0.0;
            
            // Otros datos
            this.observaciones = doc.getString("observaciones");
            this.metodoPago = doc.getString("metodoPago");
            this.usuarioCreacion = doc.getString("usuarioCreacion");
            
            // Cargar servicios y medicamentos
            cargarConceptos(doc);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void cargarConceptos(Document doc) {
        // Cargar servicios
        List<Document> serviciosDoc = (List<Document>) doc.get("servicios");
        if (serviciosDoc != null) {
            for (Document servicioDoc : serviciosDoc) {
                ConceptoFactura concepto = new ConceptoFactura();
                concepto.setDescripcion(servicioDoc.getString("descripcion"));
                concepto.setCantidad(servicioDoc.getInteger("cantidad", 1));
                concepto.setPrecioUnitario(servicioDoc.getDouble("precioUnitario") != null ? servicioDoc.getDouble("precioUnitario") : 0.0);
                concepto.setDescuento(servicioDoc.getDouble("descuento") != null ? servicioDoc.getDouble("descuento") : 0.0);
                concepto.setTipoIva(servicioDoc.getDouble("tipoIva") != null ? servicioDoc.getDouble("tipoIva") : 21.0);
                this.servicios.add(concepto);
            }
        }
        
        // Cargar medicamentos
        List<Document> medicamentosDoc = (List<Document>) doc.get("medicamentos");
        if (medicamentosDoc != null) {
            for (Document medicamentoDoc : medicamentosDoc) {
                ConceptoFactura concepto = new ConceptoFactura();
                concepto.setDescripcion(medicamentoDoc.getString("descripcion"));
                concepto.setCantidad(medicamentoDoc.getInteger("cantidad", 1));
                concepto.setPrecioUnitario(medicamentoDoc.getDouble("precioUnitario") != null ? medicamentoDoc.getDouble("precioUnitario") : 0.0);
                concepto.setDescuento(medicamentoDoc.getDouble("descuento") != null ? medicamentoDoc.getDouble("descuento") : 0.0);
                concepto.setTipoIva(medicamentoDoc.getDouble("tipoIva") != null ? medicamentoDoc.getDouble("tipoIva") : 10.0);
                this.medicamentos.add(concepto);
            }
        }
    }
    
    /**
     * Convierte el modelo a Document de MongoDB
     */
    public Document toDocument() {
        Document doc = new Document();
        
        if (id != null) doc.put("_id", id);
        if (numeroFactura != null) doc.put("numeroFactura", numeroFactura);
        if (citaId != null) doc.put("citaId", citaId);
        
        // Datos del cliente
        if (propietarioId != null) doc.put("propietarioId", propietarioId);
        if (nombreCliente != null) doc.put("nombreCliente", nombreCliente);
        if (dniCliente != null) doc.put("dniCliente", dniCliente);
        if (direccionCliente != null) doc.put("direccionCliente", direccionCliente);
        if (telefonoCliente != null) doc.put("telefonoCliente", telefonoCliente);
        if (emailCliente != null) doc.put("emailCliente", emailCliente);
        
        // Datos del paciente
        if (pacienteId != null) doc.put("pacienteId", pacienteId);
        if (nombrePaciente != null) doc.put("nombrePaciente", nombrePaciente);
        if (especiePaciente != null) doc.put("especiePaciente", especiePaciente);
        if (razaPaciente != null) doc.put("razaPaciente", razaPaciente);
        
        // Fechas
        if (fechaEmision != null) doc.put("fechaEmision", fechaEmision);
        if (fechaVencimiento != null) doc.put("fechaVencimiento", fechaVencimiento);
        if (fechaCreacion != null) doc.put("fechaCreacion", fechaCreacion);
        doc.put("fechaModificacion", new Date());
        
        // Estado
        if (estado != null) doc.put("estado", estado.name());
        doc.put("esBorrador", esBorrador);
        
        // Veterinario
        if (veterinarioNombre != null) doc.put("veterinarioNombre", veterinarioNombre);
        if (veterinarioId != null) doc.put("veterinarioId", veterinarioId);
        
        // Importes
        doc.put("subtotal", subtotal);
        doc.put("ivaGeneral", ivaGeneral);
        doc.put("ivaMedicamentos", ivaMedicamentos);
        doc.put("total", total);
        
        // Otros datos
        if (observaciones != null) doc.put("observaciones", observaciones);
        if (metodoPago != null) doc.put("metodoPago", metodoPago);
        if (usuarioCreacion != null) doc.put("usuarioCreacion", usuarioCreacion);
        
        // Convertir servicios y medicamentos
        doc.put("servicios", convertirConceptosADocuments(servicios));
        doc.put("medicamentos", convertirConceptosADocuments(medicamentos));
        
        return doc;
    }
    
    private List<Document> convertirConceptosADocuments(List<ConceptoFactura> conceptos) {
        List<Document> docs = new ArrayList<>();
        for (ConceptoFactura concepto : conceptos) {
            Document conceptoDoc = new Document()
                .append("descripcion", concepto.getDescripcion())
                .append("cantidad", concepto.getCantidad())
                .append("precioUnitario", concepto.getPrecioUnitario())
                .append("descuento", concepto.getDescuento())
                .append("tipoIva", concepto.getTipoIva())
                .append("subtotal", concepto.getSubtotal())
                .append("importeIva", concepto.getImporteIva())
                .append("total", concepto.getTotal());
            docs.add(conceptoDoc);
        }
        return docs;
    }
    
    /**
     * Calcula todos los importes de la factura
     */
    public void calcularImportes() {
        this.subtotal = 0.0;
        this.ivaGeneral = 0.0;
        this.ivaMedicamentos = 0.0;
        
        // Calcular servicios
        for (ConceptoFactura servicio : servicios) {
            servicio.calcularImportes();
            this.subtotal += servicio.getSubtotal();
            this.ivaGeneral += servicio.getImporteIva();
        }
        
        // Calcular medicamentos
        for (ConceptoFactura medicamento : medicamentos) {
            medicamento.calcularImportes();
            this.subtotal += medicamento.getSubtotal();
            this.ivaMedicamentos += medicamento.getImporteIva();
        }
        
        this.total = this.subtotal + this.ivaGeneral + this.ivaMedicamentos;
    }
    
    /**
     * Genera el número de factura automáticamente
     */
    public void generarNumeroFactura() {
        if (this.numeroFactura == null || this.numeroFactura.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            this.numeroFactura = String.format("FAC-%04d-%02d-%06d", 
                now.getYear(), 
                now.getMonthValue(), 
                System.currentTimeMillis() % 1000000);
        }
    }
    
    /**
     * Finaliza la factura (la convierte de borrador a emitida)
     */
    public void finalizar() {
        this.esBorrador = false;
        this.estado = EstadoFactura.EMITIDA;
        this.fechaModificacion = new Date();
        if (this.numeroFactura == null || this.numeroFactura.isEmpty()) {
            generarNumeroFactura();
        }
    }
    
    // Getters y setters
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }
    
    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }
    
    public ObjectId getCitaId() { return citaId; }
    public void setCitaId(ObjectId citaId) { this.citaId = citaId; }
    
    public ObjectId getPropietarioId() { return propietarioId; }
    public void setPropietarioId(ObjectId propietarioId) { this.propietarioId = propietarioId; }
    
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
    
    public String getDniCliente() { return dniCliente; }
    public void setDniCliente(String dniCliente) { this.dniCliente = dniCliente; }
    
    public String getDireccionCliente() { return direccionCliente; }
    public void setDireccionCliente(String direccionCliente) { this.direccionCliente = direccionCliente; }
    
    public String getTelefonoCliente() { return telefonoCliente; }
    public void setTelefonoCliente(String telefonoCliente) { this.telefonoCliente = telefonoCliente; }
    
    public String getEmailCliente() { return emailCliente; }
    public void setEmailCliente(String emailCliente) { this.emailCliente = emailCliente; }
    
    public ObjectId getPacienteId() { return pacienteId; }
    public void setPacienteId(ObjectId pacienteId) { this.pacienteId = pacienteId; }
    
    public String getNombrePaciente() { return nombrePaciente; }
    public void setNombrePaciente(String nombrePaciente) { this.nombrePaciente = nombrePaciente; }
    
    public String getEspeciePaciente() { return especiePaciente; }
    public void setEspeciePaciente(String especiePaciente) { this.especiePaciente = especiePaciente; }
    
    public String getRazaPaciente() { return razaPaciente; }
    public void setRazaPaciente(String razaPaciente) { this.razaPaciente = razaPaciente; }
    
    public Date getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(Date fechaEmision) { this.fechaEmision = fechaEmision; }
    
    public Date getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(Date fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    
    public EstadoFactura getEstado() { return estado; }
    public void setEstado(EstadoFactura estado) { this.estado = estado; }
    
    public boolean isEsBorrador() { return esBorrador; }
    public void setEsBorrador(boolean esBorrador) { this.esBorrador = esBorrador; }
    
    public String getVeterinarioNombre() { return veterinarioNombre; }
    public void setVeterinarioNombre(String veterinarioNombre) { this.veterinarioNombre = veterinarioNombre; }
    
    public String getVeterinarioId() { return veterinarioId; }
    public void setVeterinarioId(String veterinarioId) { this.veterinarioId = veterinarioId; }
    
    public List<ConceptoFactura> getServicios() { return servicios; }
    public void setServicios(List<ConceptoFactura> servicios) { this.servicios = servicios; }
    
    public List<ConceptoFactura> getMedicamentos() { return medicamentos; }
    public void setMedicamentos(List<ConceptoFactura> medicamentos) { this.medicamentos = medicamentos; }
    
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    
    public double getIvaGeneral() { return ivaGeneral; }
    public void setIvaGeneral(double ivaGeneral) { this.ivaGeneral = ivaGeneral; }
    
    public double getIvaMedicamentos() { return ivaMedicamentos; }
    public void setIvaMedicamentos(double ivaMedicamentos) { this.ivaMedicamentos = ivaMedicamentos; }
    
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    
    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public Date getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(Date fechaModificacion) { this.fechaModificacion = fechaModificacion; }
    
    public String getUsuarioCreacion() { return usuarioCreacion; }
    public void setUsuarioCreacion(String usuarioCreacion) { this.usuarioCreacion = usuarioCreacion; }
} 