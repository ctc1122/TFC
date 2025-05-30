package com.example.pruebamongodbcss.Modulos.Facturacion;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;

import Utilidades1.GestorConexion;

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
            
            // Generar número de factura único con formato ChichaVet-AAAAMMNNNN
            if (factura.getNumeroFactura() == null || factura.getNumeroFactura().isEmpty() || 
                factura.getNumeroFactura().contains("XXXX")) {
                String numeroUnico = generarNumeroFacturaUnico();
                factura.setNumeroFacturaCompleto(numeroUnico);
                System.out.println("Número de factura asignado: " + numeroUnico);
            }
            
            // Calcular importes antes de guardar
            factura.calcularImportes();
            
            Document doc = factura.toDocument();
            coleccionFacturas.insertOne(doc);
            
            // Si la factura no es borrador y tiene cita asociada, cambiar estado de cita a COMPLETADA
            if (!factura.isEsBorrador() && factura.getCitaId() != null) {
                cambiarEstadoCitaACompletada(factura.getCitaId());
            }
            
            System.out.println("Factura guardada con ID: " + factura.getId() + " y número: " + factura.getNumeroFactura());
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
            
            // Si la factura no es borrador y tiene cita asociada, cambiar estado de cita a COMPLETADA
            if (resultado > 0 && !factura.isEsBorrador() && factura.getCitaId() != null) {
                cambiarEstadoCitaACompletada(factura.getCitaId());
            }
            
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
     * Cambia el estado de una cita a COMPLETADA cuando se finaliza una factura
     */
    public boolean cambiarEstadoCitaACompletada(ObjectId citaId) {
        try {
            System.out.println("🏥 Cambiando estado de cita " + citaId + " a COMPLETADA automáticamente...");
            
            MongoCollection<Document> coleccionCitas = GestorConexion.conectarClinica().getCollection("citas");
            
            // Actualizar en la colección de citas
            long resultado = coleccionCitas.updateOne(
                Filters.eq("_id", citaId),
                Updates.set("estado", "COMPLETADA")
            ).getModifiedCount();
            
            if (resultado > 0) {
                System.out.println("✅ Estado de cita cambiado a COMPLETADA en colección de citas");
                
                // También actualizar en la colección de appointments si existe
                try {
                    MongoCollection<Document> coleccionAppointments = GestorConexion.conectarClinica().getCollection("appointments");
                    long resultadoAppointment = coleccionAppointments.updateOne(
                        Filters.eq("_id", citaId),
                        Updates.combine(
                            Updates.set("estado", "COMPLETADA"),
                            Updates.set("type", "completed")
                        )
                    ).getModifiedCount();
                    
                    if (resultadoAppointment > 0) {
                        System.out.println("✅ También actualizado en colección de appointments");
                    } else {
                        System.out.println("ℹ️ No se encontró en colección de appointments (normal para citas nuevas)");
                    }
                } catch (Exception appointmentException) {
                    System.out.println("⚠️ Cita actualizada en colección principal, pero error en appointments: " + appointmentException.getMessage());
                }
                
                return true;
            } else {
                System.err.println("❌ No se pudo actualizar el estado de la cita");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al cambiar estado de cita a COMPLETADA: " + e.getMessage());
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
    
    /**
     * Obtiene facturas por estado específico
     */
    public List<ModeloFactura> obtenerFacturasPorEstado(ModeloFactura.EstadoFactura estado) {
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
            
            System.out.println("Facturas encontradas con estado " + estado + ": " + facturas.size());
            
        } catch (Exception e) {
            System.err.println("Error al obtener facturas por estado: " + e.getMessage());
            e.printStackTrace();
        }
        return facturas;
    }
    
    /**
     * Obtiene todas las facturas que NO son borradores (facturas finalizadas)
     */
    public List<ModeloFactura> obtenerFacturasFinalizadas() {
        List<ModeloFactura> facturas = new ArrayList<>();
        try {
            MongoCursor<Document> cursor = coleccionFacturas.find(
                Filters.eq("esBorrador", false)
            ).sort(Sorts.descending("fechaEmision")).iterator();
            
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                facturas.add(new ModeloFactura(doc));
            }
            cursor.close();
            
            System.out.println("Facturas finalizadas encontradas: " + facturas.size());
            
        } catch (Exception e) {
            System.err.println("Error al obtener facturas finalizadas: " + e.getMessage());
            e.printStackTrace();
        }
        return facturas;
    }
    
    /**
     * Cambia el estado de una factura
     */
    public boolean cambiarEstadoFactura(ObjectId facturaId, ModeloFactura.EstadoFactura nuevoEstado) {
        try {
            // Obtener la factura actual
            ModeloFactura factura = obtenerFacturaPorId(facturaId);
            if (factura == null) {
                System.err.println("Factura no encontrada: " + facturaId);
                return false;
            }
            
            // Actualizar el estado
            factura.setEstado(nuevoEstado);
            factura.setEsBorrador(false); // Ya no es borrador
            factura.setFechaModificacion(new Date());
            
            // Si se marca como emitida, generar número de factura si no lo tiene
            if (nuevoEstado == ModeloFactura.EstadoFactura.EMITIDA && 
                (factura.getNumeroFactura() == null || factura.getNumeroFactura().isEmpty() || 
                 factura.getNumeroFactura().contains("XXXX"))) {
                String numeroUnico = generarNumeroFacturaUnico();
                factura.setNumeroFacturaCompleto(numeroUnico);
                System.out.println("Número de factura asignado al finalizar: " + numeroUnico);
            }
            
            // Guardar en la base de datos
            boolean actualizado = actualizarFactura(factura);
            
            if (actualizado) {
                System.out.println("Estado de factura " + facturaId + " cambiado a: " + nuevoEstado);
            }
            
            return actualizado;
            
        } catch (Exception e) {
            System.err.println("Error al cambiar estado de factura: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Genera un número de factura único con formato ChichaVet-AAAAMMNNNN
     */
    public String generarNumeroFacturaUnico() {
        try {
            LocalDate now = LocalDate.now();
            String prefijo = String.format("ChichaVet-%04d%02d", 
                now.getYear(), 
                now.getMonthValue());
            
            // Buscar el último número de factura del mes actual
            String patron = "^" + prefijo + "\\d{4}$";
            
            MongoCursor<Document> cursor = coleccionFacturas.find(
                Filters.regex("numeroFactura", patron)
            ).sort(Sorts.descending("numeroFactura")).limit(1).iterator();
            
            int siguienteNumero = 1;
            
            if (cursor.hasNext()) {
                Document doc = cursor.next();
                String ultimoNumero = doc.getString("numeroFactura");
                if (ultimoNumero != null && ultimoNumero.length() >= 4) {
                    // Extraer los últimos 4 dígitos
                    String ultimoIncremental = ultimoNumero.substring(ultimoNumero.length() - 4);
                    try {
                        siguienteNumero = Integer.parseInt(ultimoIncremental) + 1;
                    } catch (NumberFormatException e) {
                        System.err.println("Error al parsear número incremental: " + ultimoIncremental);
                        siguienteNumero = 1;
                    }
                }
            }
            cursor.close();
            
            // Formatear el número completo
            String numeroCompleto = String.format("%s%04d", prefijo, siguienteNumero);
            
            System.out.println("Número de factura generado: " + numeroCompleto);
            return numeroCompleto;
            
        } catch (Exception e) {
            System.err.println("Error al generar número de factura único: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback: usar timestamp
            LocalDate now = LocalDate.now();
            String prefijo = String.format("ChichaVet-%04d%02d", 
                now.getYear(), 
                now.getMonthValue());
            long timestamp = System.currentTimeMillis() % 10000;
            return String.format("%s%04d", prefijo, timestamp);
        }
    }
    
    /**
     * Método de prueba para verificar el formato de numeración
     */
    public void probarGeneracionNumeros() {
        System.out.println("=== PRUEBA DE GENERACIÓN DE NÚMEROS DE FACTURA ===");
        for (int i = 0; i < 5; i++) {
            String numero = generarNumeroFacturaUnico();
            System.out.println("Número generado " + (i + 1) + ": " + numero);
        }
        System.out.println("=== FIN DE PRUEBA ===");
    }
} 