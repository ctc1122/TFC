package com.example.pruebamongodbcss.Modulos.Facturacion;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Utilidades.GestorConexion;

/**
 * Servicio para gestionar las operaciones de facturación en la base de datos
 */
public class ServicioFacturacion {
    
    private MongoCollection<Document> coleccionFacturas;
    
    public ServicioFacturacion() {
        try {
            this.coleccionFacturas = GestorConexion.conectarClinica().getCollection("facturas");
        } catch (Exception e) {
            System.err.println("Error al conectar con la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Guarda una nueva factura en la base de datos
     */
    public ObjectId guardarFactura(ModeloFactura factura) {
        try {
            if (factura.getId() == null) {
                factura.setId(new ObjectId());
            }
            
            // Generar número de factura si no existe
            if (factura.getNumeroFactura() == null || factura.getNumeroFactura().isEmpty()) {
                factura.generarNumeroFactura();
            }
            
            // Calcular importes antes de guardar
            factura.calcularImportes();
            
            Document doc = factura.toDocument();
            coleccionFacturas.insertOne(doc);
            
            System.out.println("Factura guardada con ID: " + factura.getId());
            return factura.getId();
            
        } catch (Exception e) {
            System.err.println("Error al guardar factura: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Actualiza una factura existente
     */
    public boolean actualizarFactura(ModeloFactura factura) {
        try {
            if (factura.getId() == null) {
                System.err.println("No se puede actualizar una factura sin ID");
                return false;
            }
            
            // Calcular importes antes de actualizar
            factura.calcularImportes();
            factura.setFechaModificacion(new Date());
            
            Document doc = factura.toDocument();
            doc.remove("_id"); // No actualizar el ID
            
            long resultado = coleccionFacturas.replaceOne(
                Filters.eq("_id", factura.getId()), 
                doc
            ).getModifiedCount();
            
            System.out.println("Factura actualizada: " + (resultado > 0));
            return resultado > 0;
            
        } catch (Exception e) {
            System.err.println("Error al actualizar factura: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Obtiene una factura por su ID
     */
    public ModeloFactura obtenerFacturaPorId(ObjectId id) {
        try {
            Document doc = coleccionFacturas.find(Filters.eq("_id", id)).first();
            if (doc != null) {
                return new ModeloFactura(doc);
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error al obtener factura por ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Obtiene todas las facturas ordenadas por fecha de emisión descendente
     */
    public List<ModeloFactura> obtenerTodasFacturas() {
        List<ModeloFactura> facturas = new ArrayList<>();
        try {
            MongoCursor<Document> cursor = coleccionFacturas.find()
                .sort(Sorts.descending("fechaEmision"))
                .iterator();
            
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                facturas.add(new ModeloFactura(doc));
            }
            cursor.close();
            
        } catch (Exception e) {
            System.err.println("Error al obtener todas las facturas: " + e.getMessage());
            e.printStackTrace();
        }
        return facturas;
    }
    
    /**
     * Busca facturas por cliente (propietario)
     */
    public List<ModeloFactura> buscarFacturasPorCliente(ObjectId propietarioId) {
        List<ModeloFactura> facturas = new ArrayList<>();
        try {
            MongoCursor<Document> cursor = coleccionFacturas.find(
                Filters.eq("propietarioId", propietarioId)
            ).sort(Sorts.descending("fechaEmision")).iterator();
            
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                facturas.add(new ModeloFactura(doc));
            }
            cursor.close();
            
        } catch (Exception e) {
            System.err.println("Error al buscar facturas por cliente: " + e.getMessage());
            e.printStackTrace();
        }
        return facturas;
    }
    
    /**
     * Busca facturas por rango de fechas
     */
    public List<ModeloFactura> buscarFacturasPorFecha(Date fechaInicio, Date fechaFin) {
        List<ModeloFactura> facturas = new ArrayList<>();
        try {
            MongoCursor<Document> cursor = coleccionFacturas.find(
                Filters.and(
                    Filters.gte("fechaEmision", fechaInicio),
                    Filters.lte("fechaEmision", fechaFin)
                )
            ).sort(Sorts.descending("fechaEmision")).iterator();
            
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                facturas.add(new ModeloFactura(doc));
            }
            cursor.close();
            
        } catch (Exception e) {
            System.err.println("Error al buscar facturas por fecha: " + e.getMessage());
            e.printStackTrace();
        }
        return facturas;
    }
    
    /**
     * Busca facturas por estado
     */
    public List<ModeloFactura> buscarFacturasPorEstado(ModeloFactura.EstadoFactura estado) {
        List<ModeloFactura> facturas = new ArrayList<>();
        try {
            MongoCursor<Document> cursor = coleccionFacturas.find(
                Filters.eq("estado", estado.name())
            ).sort(Sorts.descending("fechaEmision")).iterator();
            
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                facturas.add(new ModeloFactura(doc));
            }
            cursor.close();
            
        } catch (Exception e) {
            System.err.println("Error al buscar facturas por estado: " + e.getMessage());
            e.printStackTrace();
        }
        return facturas;
    }
    
    /**
     * Obtiene todas las facturas en estado borrador
     */
    public List<ModeloFactura> obtenerFacturasBorrador() {
        List<ModeloFactura> facturas = new ArrayList<>();
        try {
            MongoCursor<Document> cursor = coleccionFacturas.find(
                Filters.eq("esBorrador", true)
            ).sort(Sorts.descending("fechaCreacion")).iterator();
            
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                facturas.add(new ModeloFactura(doc));
            }
            cursor.close();
            
        } catch (Exception e) {
            System.err.println("Error al obtener facturas borrador: " + e.getMessage());
            e.printStackTrace();
        }
        return facturas;
    }
    
    /**
     * Finaliza una factura (la convierte de borrador a emitida)
     */
    public boolean finalizarFactura(ObjectId facturaId) {
        try {
            ModeloFactura factura = obtenerFacturaPorId(facturaId);
            if (factura == null) {
                System.err.println("Factura no encontrada");
                return false;
            }
            
            factura.finalizar();
            return actualizarFactura(factura);
            
        } catch (Exception e) {
            System.err.println("Error al finalizar factura: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Elimina una factura (solo si está en borrador)
     */
    public boolean eliminarFactura(ObjectId facturaId) {
        try {
            // Verificar que la factura esté en borrador
            ModeloFactura factura = obtenerFacturaPorId(facturaId);
            if (factura == null) {
                System.err.println("Factura no encontrada");
                return false;
            }
            
            if (!factura.isEsBorrador()) {
                System.err.println("No se puede eliminar una factura que no está en borrador");
                return false;
            }
            
            long resultado = coleccionFacturas.deleteOne(Filters.eq("_id", facturaId)).getDeletedCount();
            System.out.println("Factura eliminada: " + (resultado > 0));
            return resultado > 0;
            
        } catch (Exception e) {
            System.err.println("Error al eliminar factura: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Busca facturas por número de factura
     */
    public ModeloFactura buscarFacturaPorNumero(String numeroFactura) {
        try {
            Document doc = coleccionFacturas.find(
                Filters.eq("numeroFactura", numeroFactura)
            ).first();
            
            if (doc != null) {
                return new ModeloFactura(doc);
            }
            return null;
            
        } catch (Exception e) {
            System.err.println("Error al buscar factura por número: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Busca facturas por paciente
     */
    public List<ModeloFactura> buscarFacturasPorPaciente(ObjectId pacienteId) {
        List<ModeloFactura> facturas = new ArrayList<>();
        try {
            MongoCursor<Document> cursor = coleccionFacturas.find(
                Filters.eq("pacienteId", pacienteId)
            ).sort(Sorts.descending("fechaEmision")).iterator();
            
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                facturas.add(new ModeloFactura(doc));
            }
            cursor.close();
            
        } catch (Exception e) {
            System.err.println("Error al buscar facturas por paciente: " + e.getMessage());
            e.printStackTrace();
        }
        return facturas;
    }
    
    /**
     * Busca facturas por cita
     */
    public ModeloFactura buscarFacturaPorCita(ObjectId citaId) {
        try {
            Document doc = coleccionFacturas.find(
                Filters.eq("citaId", citaId)
            ).first();
            
            if (doc != null) {
                return new ModeloFactura(doc);
            }
            return null;
            
        } catch (Exception e) {
            System.err.println("Error al buscar factura por cita: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Obtiene estadísticas de facturación por mes
     */
    public List<EstadisticaFacturacion> obtenerEstadisticasPorMes(int año) {
        List<EstadisticaFacturacion> estadisticas = new ArrayList<>();
        try {
            for (int mes = 1; mes <= 12; mes++) {
                LocalDate inicioMes = LocalDate.of(año, mes, 1);
                LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
                
                Date fechaInicio = Date.from(inicioMes.atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date fechaFin = Date.from(finMes.atStartOfDay(ZoneId.systemDefault()).toInstant());
                
                List<ModeloFactura> facturasMes = buscarFacturasPorFecha(fechaInicio, fechaFin);
                
                double totalMes = facturasMes.stream()
                    .filter(f -> !f.isEsBorrador())
                    .mapToDouble(ModeloFactura::getTotal)
                    .sum();
                
                int cantidadMes = (int) facturasMes.stream()
                    .filter(f -> !f.isEsBorrador())
                    .count();
                
                estadisticas.add(new EstadisticaFacturacion(mes, año, cantidadMes, totalMes));
            }
        } catch (Exception e) {
            System.err.println("Error al obtener estadísticas: " + e.getMessage());
            e.printStackTrace();
        }
        return estadisticas;
    }
    
    /**
     * Clase para estadísticas de facturación
     */
    public static class EstadisticaFacturacion {
        private int mes;
        private int año;
        private int cantidadFacturas;
        private double totalFacturado;
        
        public EstadisticaFacturacion(int mes, int año, int cantidadFacturas, double totalFacturado) {
            this.mes = mes;
            this.año = año;
            this.cantidadFacturas = cantidadFacturas;
            this.totalFacturado = totalFacturado;
        }
        
        // Getters
        public int getMes() { return mes; }
        public int getAño() { return año; }
        public int getCantidadFacturas() { return cantidadFacturas; }
        public double getTotalFacturado() { return totalFacturado; }
    }
    
    /**
     * Actualiza el estado de una cita a "Pendiente de Facturar"
     */
    public boolean cambiarEstadoCitaPendienteFacturar(ObjectId citaId) {
        try {
            MongoCollection<Document> coleccionCitas = GestorConexion.conectarClinica().getCollection("citas");
            
            long resultado = coleccionCitas.updateOne(
                Filters.eq("_id", citaId),
                Updates.set("estado", "Pendiente de Facturar")
            ).getModifiedCount();
            
            System.out.println("Estado de cita actualizado: " + (resultado > 0));
            return resultado > 0;
            
        } catch (Exception e) {
            System.err.println("Error al cambiar estado de cita: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Busca facturas con filtros múltiples
     */
    public List<ModeloFactura> buscarFacturasConFiltros(String numeroFactura, ObjectId propietarioId, 
                                                        Date fechaInicio, Date fechaFin, 
                                                        ModeloFactura.EstadoFactura estado) {
        List<ModeloFactura> facturas = new ArrayList<>();
        try {
            List<org.bson.conversions.Bson> filtros = new ArrayList<>();
            
            if (numeroFactura != null && !numeroFactura.trim().isEmpty()) {
                filtros.add(Filters.regex("numeroFactura", ".*" + numeroFactura + ".*", "i"));
            }
            
            if (propietarioId != null) {
                filtros.add(Filters.eq("propietarioId", propietarioId));
            }
            
            if (fechaInicio != null && fechaFin != null) {
                filtros.add(Filters.and(
                    Filters.gte("fechaEmision", fechaInicio),
                    Filters.lte("fechaEmision", fechaFin)
                ));
            }
            
            if (estado != null) {
                filtros.add(Filters.eq("estado", estado.name()));
            }
            
            org.bson.conversions.Bson filtroFinal = filtros.isEmpty() ? 
                new Document() : Filters.and(filtros);
            
            MongoCursor<Document> cursor = coleccionFacturas.find(filtroFinal)
                .sort(Sorts.descending("fechaEmision"))
                .iterator();
            
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                facturas.add(new ModeloFactura(doc));
            }
            cursor.close();
            
        } catch (Exception e) {
            System.err.println("Error al buscar facturas con filtros: " + e.getMessage());
            e.printStackTrace();
        }
        return facturas;
    }
} 