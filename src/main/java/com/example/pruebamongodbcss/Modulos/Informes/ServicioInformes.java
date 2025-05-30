package com.example.pruebamongodbcss.Modulos.Informes;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import Utilidades.GestorConexion;

public class ServicioInformes {
    
    private MongoCollection<Document> facturasCollection;
    private MongoCollection<Document> propietariosCollection;
    private MongoCollection<Document> fichajesCollection;
    private MongoCollection<Document> citasCollection;
    private MongoCollection<Document> pacientesCollection;
    private MongoCollection<Document> usuariosCollection;
    
    public ServicioInformes() {
        this.facturasCollection = GestorConexion.conectarClinica().getCollection("facturas");
        this.propietariosCollection = GestorConexion.conectarClinica().getCollection("propietarios");
        this.fichajesCollection = GestorConexion.conectarEmpresa().getCollection("fichajes");
        this.citasCollection = GestorConexion.conectarClinica().getCollection("citas");
        this.pacientesCollection = GestorConexion.conectarClinica().getCollection("pacientes");
        this.usuariosCollection = GestorConexion.conectarEmpresa().getCollection("usuarios");
    }
    
    /**
     * Obtiene las métricas principales del dashboard
     */
    public DashboardMetricas obtenerMetricasDashboard() {
        DashboardMetricas metricas = new DashboardMetricas();
        
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate inicioMesAnterior = inicioMes.minusMonths(1);
        LocalDate finMesAnterior = inicioMes.minusDays(1);
        LocalDate inicioAno = hoy.withDayOfYear(1);
        LocalDate inicioAnoAnterior = inicioAno.minusYears(1);
        
        // Ventas del día
        metricas.setVentasHoy(calcularVentasPorFecha(hoy, hoy));
        
        // Ventas del mes actual vs mes anterior
        double ventasMesActual = calcularVentasPorFecha(inicioMes, hoy);
        double ventasMesAnterior = calcularVentasPorFecha(inicioMesAnterior, finMesAnterior);
        metricas.setVentasMesActual(ventasMesActual);
        metricas.setVentasMesAnterior(ventasMesAnterior);
        metricas.setPorcentajeCambioVentas(calcularPorcentajeCambio(ventasMesActual, ventasMesAnterior));
        
        // Número de facturas del mes
        metricas.setNumeroFacturasMes(contarFacturasPorFecha(inicioMes, hoy));
        metricas.setNumeroFacturasMesAnterior(contarFacturasPorFecha(inicioMesAnterior, finMesAnterior));
        
        // Clientes del año vs año anterior
        int clientesAnoActual = contarClientesPorAno(inicioAno, hoy);
        int clientesAnoAnterior = contarClientesPorAno(inicioAnoAnterior, inicioAno.minusDays(1));
        metricas.setClientesAnoActual(clientesAnoActual);
        metricas.setClientesAnoAnterior(clientesAnoAnterior);
        metricas.setPorcentajeCambioClientes(calcularPorcentajeCambio(clientesAnoActual, clientesAnoAnterior));
        
        // Citas del día
        metricas.setCitasHoy(contarCitasPorFecha(hoy, hoy));
        
        // Promedio de ventas por día del mes
        long diasTranscurridos = java.time.temporal.ChronoUnit.DAYS.between(inicioMes, hoy) + 1;
        metricas.setPromedioVentasDiarias(ventasMesActual / diasTranscurridos);
        
        return metricas;
    }
    
    /**
     * Obtiene datos para el gráfico de ventas mensuales
     */
    public List<DatoGrafico> obtenerVentasMensuales(int meses) {
        List<DatoGrafico> datos = new ArrayList<>();
        LocalDate fechaActual = LocalDate.now();
        
        for (int i = meses - 1; i >= 0; i--) {
            LocalDate inicioMes = fechaActual.minusMonths(i).withDayOfMonth(1);
            LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
            
            double ventas = calcularVentasPorFecha(inicioMes, finMes);
            String etiqueta = inicioMes.format(DateTimeFormatter.ofPattern("MMM yyyy"));
            
            datos.add(new DatoGrafico(etiqueta, ventas));
        }
        
        return datos;
    }
    
    /**
     * Obtiene datos para el gráfico de clientes por mes
     */
    public List<DatoGrafico> obtenerClientesPorMes(int meses) {
        List<DatoGrafico> datos = new ArrayList<>();
        LocalDate fechaActual = LocalDate.now();
        
        for (int i = meses - 1; i >= 0; i--) {
            LocalDate inicioMes = fechaActual.minusMonths(i).withDayOfMonth(1);
            LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
            
            int clientes = contarClientesPorFecha(inicioMes, finMes);
            String etiqueta = inicioMes.format(DateTimeFormatter.ofPattern("MMM yyyy"));
            
            datos.add(new DatoGrafico(etiqueta, clientes));
        }
        
        return datos;
    }
    
    /**
     * Obtiene estadísticas de fichajes
     */
    public EstadisticasFichajes obtenerEstadisticasFichajes() {
        EstadisticasFichajes stats = new EstadisticasFichajes();
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        
        // Empleados fichados hoy
        stats.setEmpleadosFichadosHoy(contarEmpleadosFichadosHoy());
        
        // Total de horas trabajadas este mes
        stats.setHorasTrabajadasMes(calcularHorasTrabajadasMes(inicioMes, hoy));
        
        // Promedio de horas por empleado
        stats.setPromedioHorasPorEmpleado(calcularPromedioHorasPorEmpleado(inicioMes, hoy));
        
        // Días con más fichajes
        stats.setDiaConMasFichajes(obtenerDiaConMasFichajes(inicioMes, hoy));
        
        return stats;
    }
    
    /**
     * Obtiene el top de servicios más vendidos
     */
    public List<ServicioVendido> obtenerTopServicios(int limite) {
        List<ServicioVendido> servicios = new ArrayList<>();
        
        try {
            // Agregación para contar servicios por tipo
            List<Bson> pipeline = Arrays.asList(
                Aggregates.unwind("$items"),
                Aggregates.group("$items.descripcion", 
                    Accumulators.sum("cantidad", "$items.cantidad"),
                    Accumulators.sum("total", new Document("$multiply", 
                        Arrays.asList("$items.cantidad", "$items.precio")))),
                Aggregates.sort(Sorts.descending("cantidad")),
                Aggregates.limit(limite)
            );
            
            MongoCursor<Document> cursor = facturasCollection.aggregate(pipeline).iterator();
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                ServicioVendido servicio = new ServicioVendido();
                servicio.setNombre(doc.getString("_id"));
                servicio.setCantidad(doc.getInteger("cantidad", 0));
                servicio.setTotal(doc.getDouble("total"));
                servicios.add(servicio);
            }
            cursor.close();
        } catch (Exception e) {
            System.err.println("Error al obtener top servicios: " + e.getMessage());
        }
        
        return servicios;
    }
    
    /**
     * Obtiene datos para gráfico de citas por estado
     */
    public List<DatoGrafico> obtenerCitasPorEstado() {
        List<DatoGrafico> datos = new ArrayList<>();
        
        try {
            List<Bson> pipeline = Arrays.asList(
                Aggregates.group("$estado", Accumulators.sum("count", 1)),
                Aggregates.sort(Sorts.descending("count"))
            );
            
            MongoCursor<Document> cursor = citasCollection.aggregate(pipeline).iterator();
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                String estado = doc.getString("_id");
                int count = doc.getInteger("count", 0);
                datos.add(new DatoGrafico(estado != null ? estado : "Sin estado", count));
            }
            cursor.close();
        } catch (Exception e) {
            System.err.println("Error al obtener citas por estado: " + e.getMessage());
        }
        
        return datos;
    }
    
    /**
     * Obtiene estadísticas de usuarios por rol
     */
    public List<DatoGrafico> obtenerUsuariosPorRol() {
        List<DatoGrafico> datos = new ArrayList<>();
        
        try {
            List<Bson> pipeline = Arrays.asList(
                Aggregates.group("$rol", Accumulators.sum("count", 1)),
                Aggregates.sort(Sorts.descending("count"))
            );
            
            MongoCursor<Document> cursor = usuariosCollection.aggregate(pipeline).iterator();
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                String rol = doc.getString("_id");
                int count = doc.getInteger("count", 0);
                datos.add(new DatoGrafico(rol != null ? rol : "Sin rol", count));
            }
            cursor.close();
        } catch (Exception e) {
            System.err.println("Error al obtener usuarios por rol: " + e.getMessage());
        }
        
        return datos;
    }
    
    /**
     * Obtiene comparativa anual de ventas
     */
    public ComparativaAnual obtenerComparativaAnual() {
        ComparativaAnual comparativa = new ComparativaAnual();
        LocalDate hoy = LocalDate.now();
        int anoActual = hoy.getYear();
        int anoAnterior = anoActual - 1;
        
        // Ventas por mes del año actual
        List<DatoGrafico> ventasAnoActual = obtenerVentasPorMeses(anoActual);
        List<DatoGrafico> ventasAnoAnterior = obtenerVentasPorMeses(anoAnterior);
        
        comparativa.setVentasAnoActual(ventasAnoActual);
        comparativa.setVentasAnoAnterior(ventasAnoAnterior);
        
        // Calcular totales
        double totalActual = ventasAnoActual.stream().mapToDouble(DatoGrafico::getValor).sum();
        double totalAnterior = ventasAnoAnterior.stream().mapToDouble(DatoGrafico::getValor).sum();
        
        comparativa.setTotalAnoActual(totalActual);
        comparativa.setTotalAnoAnterior(totalAnterior);
        comparativa.setPorcentajeCrecimiento(calcularPorcentajeCambio(totalActual, totalAnterior));
        
        return comparativa;
    }
    
    /**
     * Obtiene estadísticas básicas de empleados
     */
    public EstadisticasEmpleados obtenerEstadisticasEmpleados() {
        EstadisticasEmpleados stats = new EstadisticasEmpleados();
        
        try {
            // Total de empleados activos en la colección usuarios
            long totalEmpleados = usuariosCollection.countDocuments(new Document("activo", true));
            stats.setTotalEmpleados((int) totalEmpleados);
            
            // Empleados activos que han fichado en los últimos 7 días
            LocalDate hace7Dias = LocalDate.now().minusDays(7);
            String fechaStr = hace7Dias.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            long empleadosActivos = fichajesCollection.distinct("empleadoId", 
                Filters.gte("fecha", fechaStr), ObjectId.class).into(new ArrayList<>()).size();
            stats.setEmpleadosActivos((int) empleadosActivos);
            
            // Distribución de empleados por rol desde la colección usuarios
            List<DatoGrafico> empleadosPorRol = new ArrayList<>();
            String[] roles = {"VETERINARIO", "AUXILIAR", "RECEPCIONISTA", "ADMINISTRADOR"};
            
            for (String rol : roles) {
                long count = usuariosCollection.countDocuments(
                    new Document("rol", rol).append("activo", true));
                if (count > 0) {
                    empleadosPorRol.add(new DatoGrafico(rol, count));
                }
            }
            stats.setEmpleadosPorRol(empleadosPorRol);
            
            // Calcular promedio real de horas trabajadas este mes desde fichajes
            LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
            LocalDate finMes = LocalDate.now();
            double promedioHoras = calcularPromedioHorasReales(inicioMes, finMes);
            stats.setPromedioHorasMes(promedioHoras);
            
        } catch (Exception e) {
            System.err.println("Error al obtener estadísticas de empleados: " + e.getMessage());
            e.printStackTrace();
            // Valores por defecto en caso de error
            stats.setTotalEmpleados(0);
            stats.setEmpleadosActivos(0);
            stats.setEmpleadosPorRol(new ArrayList<>());
            stats.setPromedioHorasMes(0.0);
        }
        
        return stats;
    }
    
    /**
     * Obtiene análisis de productividad por empleado
     */
    public List<ProductividadEmpleado> obtenerProductividadEmpleados(int limite) {
        List<ProductividadEmpleado> productividad = new ArrayList<>();
        
        try {
            MongoCollection<Document> usuariosCollection = GestorConexion.conectarEmpresa().getCollection("usuarios");
            
            // Obtener empleados con rol VETERINARIO
            MongoCursor<Document> empleados = usuariosCollection.find(
                Filters.eq("rol", "VETERINARIO")).iterator();
            
            while (empleados.hasNext()) {
                Document empleado = empleados.next();
                String nombreEmpleado = empleado.getString("nombre");
                String idEmpleado = empleado.getObjectId("_id").toString();
                
                ProductividadEmpleado prod = new ProductividadEmpleado();
                prod.setNombreEmpleado(nombreEmpleado);
                
                // Contar citas atendidas este mes
                LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
                Date fechaInicio = Date.from(inicioMes.atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date fechaFin = Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
                
                long citasAtendidas = citasCollection.countDocuments(Filters.and(
                    Filters.eq("veterinario", nombreEmpleado),
                    Filters.gte("fechaHora", fechaInicio),
                    Filters.lt("fechaHora", fechaFin),
                    Filters.eq("estado", "EMITIDA")
                ));
                
                prod.setCitasAtendidas((int) citasAtendidas);
                
                // Calcular horas trabajadas este mes
                double horasTrabajadas = calcularHorasTrabajadasEmpleado(idEmpleado, inicioMes, LocalDate.now());
                prod.setHorasTrabajadas(horasTrabajadas);
                
                // Calcular eficiencia (citas por hora)
                if (horasTrabajadas > 0) {
                    prod.setEficiencia(citasAtendidas / horasTrabajadas);
                }
                
                productividad.add(prod);
            }
            empleados.close();
            
            // Ordenar por eficiencia descendente
            productividad.sort((a, b) -> Double.compare(b.getEficiencia(), a.getEficiencia()));
            
            // Limitar resultados
            if (productividad.size() > limite) {
                productividad = productividad.subList(0, limite);
            }
            
        } catch (Exception e) {
            System.err.println("Error al obtener productividad de empleados: " + e.getMessage());
        }
        
        return productividad;
    }
    
    /**
     * Obtiene análisis de clientes
     */
    public AnalisisClientes obtenerAnalisisClientes() {
        AnalisisClientes analisis = new AnalisisClientes();
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate inicioAno = hoy.withDayOfYear(1);
        
        try {
            // Total de clientes
            long totalClientes = propietariosCollection.countDocuments();
            analisis.setTotalClientes((int) totalClientes);
            
            // Clientes nuevos este mes
            Date fechaInicioMes = Date.from(inicioMes.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date fechaFin = Date.from(hoy.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            long clientesNuevosMes = propietariosCollection.countDocuments(Filters.and(
                Filters.gte("fechaRegistro", fechaInicioMes),
                Filters.lt("fechaRegistro", fechaFin)
            ));
            analisis.setClientesNuevosMes((int) clientesNuevosMes);
            
            // Clientes nuevos este año
            Date fechaInicioAno = Date.from(inicioAno.atStartOfDay(ZoneId.systemDefault()).toInstant());
            long clientesNuevosAno = propietariosCollection.countDocuments(Filters.and(
                Filters.gte("fechaRegistro", fechaInicioAno),
                Filters.lt("fechaRegistro", fechaFin)
            ));
            analisis.setClientesNuevosAno((int) clientesNuevosAno);
            
            // Clientes activos (con citas en los últimos 3 meses)
            LocalDate hace3Meses = hoy.minusMonths(3);
            Date fecha3Meses = Date.from(hace3Meses.atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            List<String> clientesActivos = citasCollection.distinct("propietario", 
                Filters.gte("fechaHora", fecha3Meses), String.class).into(new ArrayList<>());
            analisis.setClientesActivos(clientesActivos.size());
            
            // Promedio de mascotas por cliente
            long totalMascotas = pacientesCollection.countDocuments();
            if (totalClientes > 0) {
                analisis.setPromedioMascotasPorCliente((double) totalMascotas / totalClientes);
            }
            
        } catch (Exception e) {
            System.err.println("Error al obtener análisis de clientes: " + e.getMessage());
        }
        
        return analisis;
    }
    
    /**
     * Obtiene top de clientes por facturación
     */
    public List<ClienteTop> obtenerTopClientes(int limite) {
        List<ClienteTop> topClientes = new ArrayList<>();
        
        try {
            // Primero agrupamos por propietarioId y usamos nombreCliente que ya está en la factura
            List<Bson> pipeline = Arrays.asList(
                Aggregates.match(Filters.eq("estado", "EMITIDA")),
                Aggregates.group("$propietarioId", 
                    Accumulators.sum("totalFacturado", "$total"),
                    Accumulators.sum("numeroFacturas", 1),
                    Accumulators.first("nombreCliente", "$nombreCliente")),
                Aggregates.sort(Sorts.descending("totalFacturado")),
                Aggregates.limit(limite)
            );
            
            MongoCursor<Document> cursor = facturasCollection.aggregate(pipeline).iterator();
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                ClienteTop cliente = new ClienteTop();
                
                String nombreCliente = doc.getString("nombreCliente");
                // Si no hay nombre en la factura, intentar obtenerlo de propietarios
                if (nombreCliente == null || nombreCliente.trim().isEmpty()) {
                    ObjectId propietarioId = doc.getObjectId("_id");
                    if (propietarioId != null) {
                        Document propietario = propietariosCollection.find(Filters.eq("_id", propietarioId)).first();
                        if (propietario != null) {
                            String nombre = propietario.getString("nombre");
                            String apellidos = propietario.getString("apellidos");
                            nombreCliente = (nombre != null ? nombre : "") + 
                                           (apellidos != null ? " " + apellidos : "");
                        }
                    }
                }
                
                // Si aún no tenemos nombre, usar un placeholder
                if (nombreCliente == null || nombreCliente.trim().isEmpty()) {
                    nombreCliente = "Cliente #" + doc.getObjectId("_id");
                }
                
                cliente.setNombre(nombreCliente.trim());
                cliente.setTotalFacturado(doc.getDouble("totalFacturado") != null ? doc.getDouble("totalFacturado") : 0.0);
                cliente.setNumeroFacturas(doc.getInteger("numeroFacturas", 0));
                
                if (cliente.getNumeroFacturas() > 0) {
                    cliente.setPromedioFactura(cliente.getTotalFacturado() / cliente.getNumeroFacturas());
                } else {
                    cliente.setPromedioFactura(0.0);
                }
                
                topClientes.add(cliente);
            }
            cursor.close();
            
            System.out.println("DEBUG: Encontrados " + topClientes.size() + " clientes top");
            for (ClienteTop cliente : topClientes) {
                System.out.println("DEBUG: Cliente: " + cliente.getNombre() + 
                                 ", Total: " + cliente.getTotalFacturado() + 
                                 ", Facturas: " + cliente.getNumeroFacturas());
            }
            
        } catch (Exception e) {
            System.err.println("Error al obtener top clientes: " + e.getMessage());
            e.printStackTrace();
        }
        
        return topClientes;
    }
    
    /**
     * Calcula las ventas del mes actual basado en facturas emitidas
     */
    public double calcularVentasMesActual() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        
        try {
            Date fechaInicio = Date.from(inicioMes.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date fechaFin = Date.from(hoy.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            List<Bson> pipeline = Arrays.asList(
                Aggregates.match(Filters.and(
                    Filters.gte("fechaEmision", fechaInicio),
                    Filters.lt("fechaEmision", fechaFin),
                    Filters.eq("estado", "EMITIDA")
                )),
                Aggregates.group(null, Accumulators.sum("total", "$total"))
            );
            
            MongoCursor<Document> cursor = facturasCollection.aggregate(pipeline).iterator();
            if (cursor.hasNext()) {
                Document result = cursor.next();
                cursor.close();
                return result.getDouble("total") != null ? result.getDouble("total") : 0.0;
            }
            cursor.close();
        } catch (Exception e) {
            System.err.println("Error al calcular ventas del mes actual: " + e.getMessage());
        }
        return 0.0;
    }
    
    /**
     * Calcula las ventas por año
     */
    public double calcularVentasPorAno(int ano) {
        LocalDate inicioAno = LocalDate.of(ano, 1, 1);
        LocalDate finAno = LocalDate.of(ano, 12, 31);
        
        try {
            Date fechaInicio = Date.from(inicioAno.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date fechaFin = Date.from(finAno.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            System.out.println("DEBUG: Calculando ventas por año " + ano + 
                             ", desde " + fechaInicio + " hasta " + fechaFin);
            
            List<Bson> pipeline = Arrays.asList(
                Aggregates.match(Filters.and(
                    Filters.gte("fechaEmision", fechaInicio),
                    Filters.lt("fechaEmision", fechaFin),
                    Filters.eq("estado", "EMITIDA") // Revertido a EMITIDA
                )),
                Aggregates.group(null, Accumulators.sum("total", "$total"))
            );
            
            MongoCursor<Document> cursor = facturasCollection.aggregate(pipeline).iterator();
            if (cursor.hasNext()) {
                Document result = cursor.next();
                cursor.close();
                double total = result.getDouble("total") != null ? result.getDouble("total") : 0.0;
                System.out.println("DEBUG: Ventas año " + ano + " = " + total);
                return total;
            }
            cursor.close();
        } catch (Exception e) {
            System.err.println("Error al calcular ventas por año: " + e.getMessage());
        }
        System.out.println("DEBUG: Ventas año " + ano + " = 0.0 (sin resultados)");
        return 0.0;
    }
    
    /**
     * Calcula las ventas por mes y año específico
     */
    public double calcularVentasPorMesAno(int mes, int ano) {
        LocalDate inicioMes = LocalDate.of(ano, mes, 1);
        LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
        
        try {
            Date fechaInicio = Date.from(inicioMes.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date fechaFin = Date.from(finMes.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            System.out.println("DEBUG: Calculando ventas mes " + mes + "/" + ano + 
                             ", desde " + fechaInicio + " hasta " + fechaFin);
            
            List<Bson> pipeline = Arrays.asList(
                Aggregates.match(Filters.and(
                    Filters.gte("fechaEmision", fechaInicio),
                    Filters.lt("fechaEmision", fechaFin),
                    Filters.eq("estado", "EMITIDA") // Revertido a EMITIDA
                )),
                Aggregates.group(null, Accumulators.sum("total", "$total"))
            );
            
            MongoCursor<Document> cursor = facturasCollection.aggregate(pipeline).iterator();
            if (cursor.hasNext()) {
                Document result = cursor.next();
                cursor.close();
                double total = result.getDouble("total") != null ? result.getDouble("total") : 0.0;
                System.out.println("DEBUG: Ventas mes " + mes + "/" + ano + " = " + total);
                return total;
            }
            cursor.close();
        } catch (Exception e) {
            System.err.println("Error al calcular ventas por mes y año: " + e.getMessage());
        }
        System.out.println("DEBUG: Ventas mes " + mes + "/" + ano + " = 0.0 (sin resultados)");
        return 0.0;
    }
    
    /**
     * Cuenta pacientes registrados en un año específico
     */
    public int contarPacientesPorAno(int ano) {
        LocalDate inicioAno = LocalDate.of(ano, 1, 1);
        LocalDate finAno = LocalDate.of(ano, 12, 31);
        
        try {
            Date fechaInicio = Date.from(inicioAno.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date fechaFin = Date.from(finAno.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            long count = pacientesCollection.countDocuments(Filters.and(
                Filters.gte("fechaCreacion", fechaInicio),
                Filters.lt("fechaCreacion", fechaFin)
            ));
            
            return (int) count;
        } catch (Exception e) {
            System.err.println("Error al contar pacientes por año: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Cuenta pacientes registrados en un mes y año específico
     */
    public int contarPacientesPorMesAno(int mes, int ano) {
        LocalDate inicioMes = LocalDate.of(ano, mes, 1);
        LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
        
        try {
            Date fechaInicio = Date.from(inicioMes.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date fechaFin = Date.from(finMes.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            long count = pacientesCollection.countDocuments(Filters.and(
                Filters.gte("fechaCreacion", fechaInicio),
                Filters.lt("fechaCreacion", fechaFin)
            ));
            
            return (int) count;
        } catch (Exception e) {
            System.err.println("Error al contar pacientes por mes y año: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Cuenta fichajes totales
     */
    public int contarFichajesTotales() {
        try {
            long count = fichajesCollection.countDocuments();
            return (int) count;
        } catch (Exception e) {
            System.err.println("Error al contar fichajes totales: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Cuenta fichajes por año
     */
    public int contarFichajesPorAno(int ano) {
        try {
            // Crear patrones regex para buscar fechas que comiencen con el año específico
            String patronAno = "^" + ano + "-.*";
            
            long count = fichajesCollection.countDocuments(
                Filters.regex("fechaHoraEntrada", patronAno)
            );
            
            return (int) count;
        } catch (Exception e) {
            System.err.println("Error al contar fichajes por año: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Cuenta fichajes por mes y año
     */
    public int contarFichajesPorMesAno(int mes, int ano) {
        try {
            // Crear patrón regex para buscar fechas que comiencen con YYYY-MM
            String patronMesAno = "^" + ano + "-" + String.format("%02d", mes) + "-.*";
            
            long count = fichajesCollection.countDocuments(
                Filters.regex("fechaHoraEntrada", patronMesAno)
            );
            
            return (int) count;
        } catch (Exception e) {
            System.err.println("Error al contar fichajes por mes y año: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Obtiene datos para gráfico de evolución de ventas según filtro
     */
    public List<DatoGrafico> obtenerEvolucionVentasConFiltro(String tipoFiltro, int ano, Integer mes) {
        List<DatoGrafico> datos = new ArrayList<>();
        
        if ("ANUAL".equals(tipoFiltro)) {
            // Mostrar los últimos 12 meses hasta el año seleccionado
            for (int i = 11; i >= 0; i--) {
                LocalDate fechaMes = LocalDate.of(ano, 12, 1).minusMonths(i);
                if (fechaMes.getYear() <= ano) {
                    double ventas = calcularVentasPorMesAno(fechaMes.getMonthValue(), fechaMes.getYear());
                    String etiqueta = fechaMes.format(DateTimeFormatter.ofPattern("MMM yyyy"));
                    datos.add(new DatoGrafico(etiqueta, ventas));
                }
            }
        } else if ("MENSUAL".equals(tipoFiltro) && mes != null) {
            // Mostrar los días del mes seleccionado
            LocalDate inicioMes = LocalDate.of(ano, mes, 1);
            LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
            
            for (LocalDate fecha = inicioMes; !fecha.isAfter(finMes); fecha = fecha.plusDays(1)) {
                double ventas = calcularVentasPorFecha(fecha, fecha);
                String etiqueta = String.valueOf(fecha.getDayOfMonth());
                datos.add(new DatoGrafico(etiqueta, ventas));
            }
        }
        
        return datos;
    }
    
    /**
     * Cuenta propietarios registrados este mes
     */
    public int contarPropietariosMesActual() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());
        
        try {
            Date fechaInicio = Date.from(inicioMes.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date fechaFin = Date.from(finMes.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            long count = propietariosCollection.countDocuments(Filters.and(
                Filters.gte("fechaRegistro", fechaInicio),
                Filters.lt("fechaRegistro", fechaFin)
            ));
            
            return (int) count;
        } catch (Exception e) {
            System.err.println("Error al contar propietarios del mes actual: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Obtiene datos de propietarios registrados por mes para gráficos
     */
    public List<DatoGrafico> obtenerPropietariosPorMes(int meses) {
        List<DatoGrafico> datos = new ArrayList<>();
        LocalDate fechaActual = LocalDate.now();
        
        for (int i = meses - 1; i >= 0; i--) {
            LocalDate fechaMes = fechaActual.minusMonths(i);
            LocalDate inicioMes = fechaMes.withDayOfMonth(1);
            LocalDate finMes = fechaMes.withDayOfMonth(fechaMes.lengthOfMonth());
            
            try {
                Date fechaInicio = Date.from(inicioMes.atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date fechaFin = Date.from(finMes.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
                
                long count = propietariosCollection.countDocuments(Filters.and(
                    Filters.gte("fechaRegistro", fechaInicio),
                    Filters.lt("fechaRegistro", fechaFin)
                ));
                
                String etiqueta = fechaMes.format(DateTimeFormatter.ofPattern("MMM"));
                datos.add(new DatoGrafico(etiqueta, count));
            } catch (Exception e) {
                System.err.println("Error al obtener propietarios del mes: " + e.getMessage());
                datos.add(new DatoGrafico(fechaMes.format(DateTimeFormatter.ofPattern("MMM")), 0));
            }
        }
        
        return datos;
    }
    
    // Métodos auxiliares privados adicionales
    
    private List<DatoGrafico> obtenerVentasPorMeses(int ano) {
        List<DatoGrafico> datos = new ArrayList<>();
        
        for (int mes = 1; mes <= 12; mes++) {
            LocalDate inicioMes = LocalDate.of(ano, mes, 1);
            LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
            
            double ventas = calcularVentasPorFecha(inicioMes, finMes);
            String etiqueta = inicioMes.format(DateTimeFormatter.ofPattern("MMM"));
            
            datos.add(new DatoGrafico(etiqueta, ventas));
        }
        
        return datos;
    }
    
    private double calcularHorasTrabajadasEmpleado(String empleadoId, LocalDate inicio, LocalDate fin) {
        try {
            // Buscar fichajes del empleado en el período usando ObjectId
            ObjectId empId = new ObjectId(empleadoId);
            Document filtro = new Document("empleadoId", empId)
                .append("fecha", new Document("$gte", inicio.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .append("$lte", fin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
            
            List<Document> fichajes = fichajesCollection.find(filtro).into(new ArrayList<>());
            
            if (fichajes.isEmpty()) {
                // Si no hay fichajes, estimar 8 horas por día laborable
                long diasLaborables = inicio.datesUntil(fin.plusDays(1))
                    .filter(fecha -> fecha.getDayOfWeek().getValue() <= 5) // Lunes a viernes
                    .count();
                return diasLaborables * 8.0;
            }
            
            // Calcular horas reales usando minutosTrabajoTotal
            double totalHoras = 0.0;
            int fichajesValidos = 0;
            
            for (Document fichaje : fichajes) {
                Long minutosTrabajoTotal = fichaje.getLong("minutosTrabajoTotal");
                if (minutosTrabajoTotal != null && minutosTrabajoTotal > 0) {
                    double horas = minutosTrabajoTotal / 60.0;
                    if (horas >= 0.5 && horas <= 12.0) { // Validar rango razonable
                        totalHoras += horas;
                        fichajesValidos++;
                    }
                }
            }
            
            // Si no hay fichajes válidos, usar promedio de 8 horas por fichaje
            if (fichajesValidos == 0 && !fichajes.isEmpty()) {
                return fichajes.size() * 8.0;
            }
            
            return totalHoras;
            
        } catch (Exception e) {
            System.err.println("Error al calcular horas trabajadas del empleado: " + e.getMessage());
            return 0.0;
        }
    }
    
    // Clases internas adicionales para los nuevos reportes
    
    public static class ComparativaAnual {
        private List<DatoGrafico> ventasAnoActual;
        private List<DatoGrafico> ventasAnoAnterior;
        private double totalAnoActual;
        private double totalAnoAnterior;
        private double porcentajeCrecimiento;
        
        // Getters y setters
        public List<DatoGrafico> getVentasAnoActual() { return ventasAnoActual; }
        public void setVentasAnoActual(List<DatoGrafico> ventasAnoActual) { this.ventasAnoActual = ventasAnoActual; }
        
        public List<DatoGrafico> getVentasAnoAnterior() { return ventasAnoAnterior; }
        public void setVentasAnoAnterior(List<DatoGrafico> ventasAnoAnterior) { this.ventasAnoAnterior = ventasAnoAnterior; }
        
        public double getTotalAnoActual() { return totalAnoActual; }
        public void setTotalAnoActual(double totalAnoActual) { this.totalAnoActual = totalAnoActual; }
        
        public double getTotalAnoAnterior() { return totalAnoAnterior; }
        public void setTotalAnoAnterior(double totalAnoAnterior) { this.totalAnoAnterior = totalAnoAnterior; }
        
        public double getPorcentajeCrecimiento() { return porcentajeCrecimiento; }
        public void setPorcentajeCrecimiento(double porcentajeCrecimiento) { this.porcentajeCrecimiento = porcentajeCrecimiento; }
    }
    
    public static class EstadisticasEmpleados {
        private int totalEmpleados;
        private int empleadosActivos;
        private List<DatoGrafico> empleadosPorRol;
        private double promedioHorasMes;
        
        // Getters y setters
        public int getTotalEmpleados() { return totalEmpleados; }
        public void setTotalEmpleados(int totalEmpleados) { this.totalEmpleados = totalEmpleados; }
        
        public int getEmpleadosActivos() { return empleadosActivos; }
        public void setEmpleadosActivos(int empleadosActivos) { this.empleadosActivos = empleadosActivos; }
        
        public List<DatoGrafico> getEmpleadosPorRol() { return empleadosPorRol; }
        public void setEmpleadosPorRol(List<DatoGrafico> empleadosPorRol) { this.empleadosPorRol = empleadosPorRol; }
        
        public double getPromedioHorasMes() { return promedioHorasMes; }
        public void setPromedioHorasMes(double promedioHorasMes) { this.promedioHorasMes = promedioHorasMes; }
    }
    
    public static class ProductividadEmpleado {
        private String nombreEmpleado;
        private int citasAtendidas;
        private double horasTrabajadas;
        private double eficiencia;
        
        // Getters y setters
        public String getNombreEmpleado() { return nombreEmpleado; }
        public void setNombreEmpleado(String nombreEmpleado) { this.nombreEmpleado = nombreEmpleado; }
        
        public int getCitasAtendidas() { return citasAtendidas; }
        public void setCitasAtendidas(int citasAtendidas) { this.citasAtendidas = citasAtendidas; }
        
        public double getHorasTrabajadas() { return horasTrabajadas; }
        public void setHorasTrabajadas(double horasTrabajadas) { this.horasTrabajadas = horasTrabajadas; }
        
        public double getEficiencia() { return eficiencia; }
        public void setEficiencia(double eficiencia) { this.eficiencia = eficiencia; }
    }
    
    public static class AnalisisClientes {
        private int totalClientes;
        private int clientesNuevosMes;
        private int clientesNuevosAno;
        private int clientesActivos;
        private double promedioMascotasPorCliente;
        
        // Getters y setters
        public int getTotalClientes() { return totalClientes; }
        public void setTotalClientes(int totalClientes) { this.totalClientes = totalClientes; }
        
        public int getClientesNuevosMes() { return clientesNuevosMes; }
        public void setClientesNuevosMes(int clientesNuevosMes) { this.clientesNuevosMes = clientesNuevosMes; }
        
        public int getClientesNuevosAno() { return clientesNuevosAno; }
        public void setClientesNuevosAno(int clientesNuevosAno) { this.clientesNuevosAno = clientesNuevosAno; }
        
        public int getClientesActivos() { return clientesActivos; }
        public void setClientesActivos(int clientesActivos) { this.clientesActivos = clientesActivos; }
        
        public double getPromedioMascotasPorCliente() { return promedioMascotasPorCliente; }
        public void setPromedioMascotasPorCliente(double promedioMascotasPorCliente) { this.promedioMascotasPorCliente = promedioMascotasPorCliente; }
    }
    
    public static class ClienteTop {
        private String nombre;
        private double totalFacturado;
        private int numeroFacturas;
        private double promedioFactura;
        
        // Getters y setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public double getTotalFacturado() { return totalFacturado; }
        public void setTotalFacturado(double totalFacturado) { this.totalFacturado = totalFacturado; }
        
        public int getNumeroFacturas() { return numeroFacturas; }
        public void setNumeroFacturas(int numeroFacturas) { this.numeroFacturas = numeroFacturas; }
        
        public double getPromedioFactura() { return promedioFactura; }
        public void setPromedioFactura(double promedioFactura) { this.promedioFactura = promedioFactura; }
    }
    
    // Métodos auxiliares privados
    
    private double calcularVentasPorFecha(LocalDate inicio, LocalDate fin) {
        try {
            Date fechaInicio = Date.from(inicio.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date fechaFin = Date.from(fin.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            List<Bson> pipeline = Arrays.asList(
                Aggregates.match(Filters.and(
                    Filters.gte("fechaEmision", fechaInicio),
                    Filters.lt("fechaEmision", fechaFin),
                    Filters.eq("estado", "EMITIDA")
                )),
                Aggregates.group(null, Accumulators.sum("total", "$total"))
            );
            
            MongoCursor<Document> cursor = facturasCollection.aggregate(pipeline).iterator();
            if (cursor.hasNext()) {
                Document result = cursor.next();
                cursor.close();
                return result.getDouble("total");
            }
            cursor.close();
        } catch (Exception e) {
            System.err.println("Error al calcular ventas: " + e.getMessage());
        }
        return 0.0;
    }
    
    private int contarFacturasPorFecha(LocalDate inicio, LocalDate fin) {
        try {
            Date fechaInicio = Date.from(inicio.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date fechaFin = Date.from(fin.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            long count = facturasCollection.countDocuments(Filters.and(
                Filters.gte("fechaEmision", fechaInicio),
                Filters.lt("fechaEmision", fechaFin),
                Filters.eq("estado", "EMITIDA")
            ));
            
            return (int) count;
        } catch (Exception e) {
            System.err.println("Error al contar facturas: " + e.getMessage());
            return 0;
        }
    }
    
    private int contarClientesPorAno(LocalDate inicio, LocalDate fin) {
        try {
            Date fechaInicio = Date.from(inicio.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date fechaFin = Date.from(fin.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            long count = propietariosCollection.countDocuments(Filters.and(
                Filters.gte("fechaRegistro", fechaInicio),
                Filters.lt("fechaRegistro", fechaFin)
            ));
            
            return (int) count;
        } catch (Exception e) {
            System.err.println("Error al contar clientes: " + e.getMessage());
            return 0;
        }
    }
    
    private int contarClientesPorFecha(LocalDate inicio, LocalDate fin) {
        return contarClientesPorAno(inicio, fin);
    }
    
    public int contarCitasPorFecha(LocalDate inicio, LocalDate fin) {
        try {
            Date fechaInicio = Date.from(inicio.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date fechaFin = Date.from(fin.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            long count = citasCollection.countDocuments(Filters.and(
                Filters.gte("fechaHora", fechaInicio),
                Filters.lt("fechaHora", fechaFin)
            ));
            
            return (int) count;
        } catch (Exception e) {
            System.err.println("Error al contar citas: " + e.getMessage());
            return 0;
        }
    }
    
    private double calcularPorcentajeCambio(double actual, double anterior) {
        if (anterior == 0) return actual > 0 ? 100.0 : 0.0;
        return ((actual - anterior) / anterior) * 100.0;
    }
    
    private int contarEmpleadosFichadosHoy() {
        try {
            LocalDate hoy = LocalDate.now();
            Date fechaInicio = Date.from(hoy.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date fechaFin = Date.from(hoy.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            List<Bson> pipeline = Arrays.asList(
                Aggregates.match(Filters.and(
                    Filters.gte("fechaHoraEntrada", fechaInicio),
                    Filters.lt("fechaHoraEntrada", fechaFin),
                    Filters.exists("fechaHoraSalida", false)
                )),
                Aggregates.group("$empleadoId"),
                Aggregates.count()
            );
            
            MongoCursor<Document> cursor = fichajesCollection.aggregate(pipeline).iterator();
            if (cursor.hasNext()) {
                Document result = cursor.next();
                cursor.close();
                return result.getInteger("count", 0);
            }
            cursor.close();
        } catch (Exception e) {
            System.err.println("Error al contar empleados fichados: " + e.getMessage());
        }
        return 0;
    }
    
    private double calcularHorasTrabajadasMes(LocalDate inicio, LocalDate fin) {
        // Implementación simplificada - en un caso real calcularías las horas exactas
        return contarFacturasPorFecha(inicio, fin) * 8.0; // Estimación
    }
    
    private double calcularPromedioHorasPorEmpleado(LocalDate inicio, LocalDate fin) {
        double totalHoras = calcularHorasTrabajadasMes(inicio, fin);
        int empleados = Math.max(1, contarEmpleadosFichadosHoy());
        return totalHoras / empleados;
    }
    
    private String obtenerDiaConMasFichajes(LocalDate inicio, LocalDate fin) {
        // Implementación simplificada
        return "Lunes";
    }
    
    // Clases internas para los datos
    
    public static class DashboardMetricas {
        private double ventasHoy;
        private double ventasMesActual;
        private double ventasMesAnterior;
        private double porcentajeCambioVentas;
        private int numeroFacturasMes;
        private int numeroFacturasMesAnterior;
        private int clientesAnoActual;
        private int clientesAnoAnterior;
        private double porcentajeCambioClientes;
        private int citasHoy;
        private double promedioVentasDiarias;
        
        // Getters y setters
        public double getVentasHoy() { return ventasHoy; }
        public void setVentasHoy(double ventasHoy) { this.ventasHoy = ventasHoy; }
        
        public double getVentasMesActual() { return ventasMesActual; }
        public void setVentasMesActual(double ventasMesActual) { this.ventasMesActual = ventasMesActual; }
        
        public double getVentasMesAnterior() { return ventasMesAnterior; }
        public void setVentasMesAnterior(double ventasMesAnterior) { this.ventasMesAnterior = ventasMesAnterior; }
        
        public double getPorcentajeCambioVentas() { return porcentajeCambioVentas; }
        public void setPorcentajeCambioVentas(double porcentajeCambioVentas) { this.porcentajeCambioVentas = porcentajeCambioVentas; }
        
        public int getNumeroFacturasMes() { return numeroFacturasMes; }
        public void setNumeroFacturasMes(int numeroFacturasMes) { this.numeroFacturasMes = numeroFacturasMes; }
        
        public int getNumeroFacturasMesAnterior() { return numeroFacturasMesAnterior; }
        public void setNumeroFacturasMesAnterior(int numeroFacturasMesAnterior) { this.numeroFacturasMesAnterior = numeroFacturasMesAnterior; }
        
        public int getClientesAnoActual() { return clientesAnoActual; }
        public void setClientesAnoActual(int clientesAnoActual) { this.clientesAnoActual = clientesAnoActual; }
        
        public int getClientesAnoAnterior() { return clientesAnoAnterior; }
        public void setClientesAnoAnterior(int clientesAnoAnterior) { this.clientesAnoAnterior = clientesAnoAnterior; }
        
        public double getPorcentajeCambioClientes() { return porcentajeCambioClientes; }
        public void setPorcentajeCambioClientes(double porcentajeCambioClientes) { this.porcentajeCambioClientes = porcentajeCambioClientes; }
        
        public int getCitasHoy() { return citasHoy; }
        public void setCitasHoy(int citasHoy) { this.citasHoy = citasHoy; }
        
        public double getPromedioVentasDiarias() { return promedioVentasDiarias; }
        public void setPromedioVentasDiarias(double promedioVentasDiarias) { this.promedioVentasDiarias = promedioVentasDiarias; }
    }
    
    public static class DatoGrafico implements Serializable {
        private static final long serialVersionUID = 1L;
        private String etiqueta;
        private double valor;
        
        public DatoGrafico(String etiqueta, double valor) {
            this.etiqueta = etiqueta;
            this.valor = valor;
        }
        
        public String getEtiqueta() { return etiqueta; }
        public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }
        
        public double getValor() { return valor; }
        public void setValor(double valor) { this.valor = valor; }
    }
    
    public static class EstadisticasFichajes {
        private int empleadosFichadosHoy;
        private double horasTrabajadasMes;
        private double promedioHorasPorEmpleado;
        private String diaConMasFichajes;
        
        // Getters y setters
        public int getEmpleadosFichadosHoy() { return empleadosFichadosHoy; }
        public void setEmpleadosFichadosHoy(int empleadosFichadosHoy) { this.empleadosFichadosHoy = empleadosFichadosHoy; }
        
        public double getHorasTrabajadasMes() { return horasTrabajadasMes; }
        public void setHorasTrabajadasMes(double horasTrabajadasMes) { this.horasTrabajadasMes = horasTrabajadasMes; }
        
        public double getPromedioHorasPorEmpleado() { return promedioHorasPorEmpleado; }
        public void setPromedioHorasPorEmpleado(double promedioHorasPorEmpleado) { this.promedioHorasPorEmpleado = promedioHorasPorEmpleado; }
        
        public String getDiaConMasFichajes() { return diaConMasFichajes; }
        public void setDiaConMasFichajes(String diaConMasFichajes) { this.diaConMasFichajes = diaConMasFichajes; }
    }
    
    public static class ServicioVendido {
        private String nombre;
        private int cantidad;
        private double total;
        
        // Getters y setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
        
        public double getTotal() { return total; }
        public void setTotal(double total) { this.total = total; }
    }
    
    // ================= MÉTODOS PARA REPORTE DE VENTAS =================
    
    /**
     * Obtiene estadísticas de ventas para el reporte específico
     */
    public AnalisisVentas obtenerAnalisisVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        AnalisisVentas analisis = new AnalisisVentas();
        
        try {
            // Convertir LocalDate a Date para MongoDB
            Date fechaInicioDate = Date.from(fechaInicio.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date fechaFinDate = Date.from(fechaFin.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            // Filtro por rango de fechas y estado EMITIDA
            Bson filtroFechas = Filters.and(
                Filters.gte("fechaCreacion", fechaInicioDate),
                Filters.lt("fechaCreacion", fechaFinDate),
                Filters.eq("estado", "EMITIDA")
            );
            
            // Total de ventas en el período
            List<Document> facturas = facturasCollection.find(filtroFechas)
                .into(new ArrayList<>());
            
            double totalVentas = 0.0;
            int numeroFacturas = facturas.size();
            
            for (Document factura : facturas) {
                Double total = factura.getDouble("total");
                if (total != null) {
                    totalVentas += total;
                }
            }
            
            // Promedio por venta
            double promedioVenta = numeroFacturas > 0 ? totalVentas / numeroFacturas : 0.0;
            
            analisis.setTotalVentas(totalVentas);
            analisis.setNumeroFacturas(numeroFacturas);
            analisis.setPromedioVenta(promedioVenta);
            analisis.setFechaInicio(fechaInicio);
            analisis.setFechaFin(fechaFin);
            
        } catch (Exception e) {
            System.err.println("Error al obtener análisis de ventas: " + e.getMessage());
            e.printStackTrace();
        }
        
        return analisis;
    }
    
    /**
     * Obtiene la evolución de ventas mensual para el gráfico
     */
    public List<DatoGrafico> obtenerEvolucionVentasPorPeriodo(int meses) {
        List<DatoGrafico> evolucion = new ArrayList<>();
        
        try {
            LocalDate fechaFin = LocalDate.now();
            LocalDate fechaInicio = fechaFin.minusMonths(meses - 1).withDayOfMonth(1);
            
            // Obtener datos mes por mes
            LocalDate fechaActual = fechaInicio;
            String[] nombresMeses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", 
                                   "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
            
            while (!fechaActual.isAfter(fechaFin.withDayOfMonth(1))) {
                LocalDate inicioMes = fechaActual.withDayOfMonth(1);
                LocalDate finMes = fechaActual.withDayOfMonth(fechaActual.lengthOfMonth());
                
                // Convertir a Date para MongoDB
                Date inicioMesDate = Date.from(inicioMes.atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date finMesDate = Date.from(finMes.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
                
                // Filtro para facturas del mes
                Bson filtroMes = Filters.and(
                    Filters.gte("fechaCreacion", inicioMesDate),
                    Filters.lt("fechaCreacion", finMesDate),
                    Filters.eq("estado", "EMITIDA")
                );
                
                // Calcular total del mes
                double totalMes = 0.0;
                List<Document> facturasMes = facturasCollection.find(filtroMes)
                    .into(new ArrayList<>());
                
                for (Document factura : facturasMes) {
                    Double total = factura.getDouble("total");
                    if (total != null) {
                        totalMes += total;
                    }
                }
                
                // Agregar punto al gráfico
                String etiqueta = nombresMeses[fechaActual.getMonthValue() - 1] + " " + fechaActual.getYear();
                evolucion.add(new DatoGrafico(etiqueta, totalMes));
                
                fechaActual = fechaActual.plusMonths(1);
            }
            
        } catch (Exception e) {
            System.err.println("Error al obtener evolución de ventas: " + e.getMessage());
            e.printStackTrace();
        }
        
        return evolucion;
    }
    
    /**
     * Obtiene los servicios más vendidos en un período
     */
    public List<ServicioVendido> obtenerTopServiciosVendidos(int limite, LocalDate fechaInicio, LocalDate fechaFin) {
        List<ServicioVendido> topServicios = new ArrayList<>();
        
        try {
            // Convertir LocalDate a Date para MongoDB
            Date fechaInicioDate = Date.from(fechaInicio.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date fechaFinDate = Date.from(fechaFin.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            // Filtro por rango de fechas y estado EMITIDA
            Bson filtroFechas = Filters.and(
                Filters.gte("fechaCreacion", fechaInicioDate),
                Filters.lt("fechaCreacion", fechaFinDate),
                Filters.eq("estado", "EMITIDA")
            );
            
            // Obtener todas las facturas del período
            List<Document> facturas = facturasCollection.find(filtroFechas)
                .into(new ArrayList<>());
            
            // Map para acumular servicios (nombre -> [cantidad, total])
            Map<String, double[]> serviciosMap = new HashMap<>();
            
            for (Document factura : facturas) {
                List<Document> servicios = factura.getList("servicios", Document.class);
                if (servicios != null) {
                    for (Document servicio : servicios) {
                        String nombre = servicio.getString("nombre");
                        Integer cantidad = servicio.getInteger("cantidad");
                        Double precio = servicio.getDouble("precio");
                        
                        if (nombre != null && cantidad != null && precio != null) {
                            double totalServicio = cantidad * precio;
                            
                            serviciosMap.compute(nombre, (k, v) -> {
                                if (v == null) {
                                    return new double[]{cantidad, totalServicio};
                                } else {
                                    return new double[]{v[0] + cantidad, v[1] + totalServicio};
                                }
                            });
                        }
                    }
                }
            }
            
            // Convertir a lista y ordenar por total facturado
            for (Map.Entry<String, double[]> entry : serviciosMap.entrySet()) {
                ServicioVendido servicio = new ServicioVendido();
                servicio.setNombre(entry.getKey());
                servicio.setCantidad((int) entry.getValue()[0]);
                servicio.setTotal(entry.getValue()[1]);
                topServicios.add(servicio);
            }
            
            // Ordenar por total descendente y limitar
            topServicios.sort((a, b) -> Double.compare(b.getTotal(), a.getTotal()));
            
            if (topServicios.size() > limite) {
                topServicios = topServicios.subList(0, limite);
            }
            
        } catch (Exception e) {
            System.err.println("Error al obtener top servicios vendidos: " + e.getMessage());
            e.printStackTrace();
        }
        
        return topServicios;
    }
    
    /**
     * Obtiene estadísticas de ventas por rango de fechas
     */
    public DashboardVentas obtenerDashboardVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        DashboardVentas dashboard = new DashboardVentas();
        
        try {
            // Convertir LocalDate a Date para MongoDB
            Date fechaInicioDate = Date.from(fechaInicio.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date fechaFinDate = Date.from(fechaFin.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            // Filtro por rango de fechas y estado EMITIDA
            Bson filtroFechas = Filters.and(
                Filters.gte("fechaCreacion", fechaInicioDate),
                Filters.lt("fechaCreacion", fechaFinDate),
                Filters.eq("estado", "EMITIDA")
            );
            
            List<Document> facturas = facturasCollection.find(filtroFechas)
                .into(new ArrayList<>());
            
            double totalVentas = 0.0;
            int numeroFacturas = facturas.size();
            
            for (Document factura : facturas) {
                Double total = factura.getDouble("total");
                if (total != null) {
                    totalVentas += total;
                }
            }
            
            // Calcular días en el período
            long diasPeriodo = ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1;
            double promedioVentasDiarias = diasPeriodo > 0 ? totalVentas / diasPeriodo : 0.0;
            double promedioVenta = numeroFacturas > 0 ? totalVentas / numeroFacturas : 0.0;
            
            dashboard.setTotalVentas(totalVentas);
            dashboard.setNumeroFacturas(numeroFacturas);
            dashboard.setPromedioVentasDiarias(promedioVentasDiarias);
            dashboard.setPromedioVenta(promedioVenta);
            dashboard.setFechaInicio(fechaInicio);
            dashboard.setFechaFin(fechaFin);
            
        } catch (Exception e) {
            System.err.println("Error al obtener dashboard de ventas: " + e.getMessage());
            e.printStackTrace();
        }
        
        return dashboard;
    }
    
    // ================= CLASES DE DATOS PARA VENTAS =================
    
    public static class AnalisisVentas implements Serializable {
        private static final long serialVersionUID = 1L;
        private double totalVentas;
        private int numeroFacturas;
        private double promedioVenta;
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        
        // Getters y setters
        public double getTotalVentas() { return totalVentas; }
        public void setTotalVentas(double totalVentas) { this.totalVentas = totalVentas; }
        
        public int getNumeroFacturas() { return numeroFacturas; }
        public void setNumeroFacturas(int numeroFacturas) { this.numeroFacturas = numeroFacturas; }
        
        public double getPromedioVenta() { return promedioVenta; }
        public void setPromedioVenta(double promedioVenta) { this.promedioVenta = promedioVenta; }
        
        public LocalDate getFechaInicio() { return fechaInicio; }
        public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
        
        public LocalDate getFechaFin() { return fechaFin; }
        public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    }
    
    public static class DashboardVentas implements Serializable {
        private static final long serialVersionUID = 1L;
        private double totalVentas;
        private int numeroFacturas;
        private double promedioVentasDiarias;
        private double promedioVenta;
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        
        // Getters y setters
        public double getTotalVentas() { return totalVentas; }
        public void setTotalVentas(double totalVentas) { this.totalVentas = totalVentas; }
        
        public int getNumeroFacturas() { return numeroFacturas; }
        public void setNumeroFacturas(int numeroFacturas) { this.numeroFacturas = numeroFacturas; }
        
        public double getPromedioVentasDiarias() { return promedioVentasDiarias; }
        public void setPromedioVentasDiarias(double promedioVentasDiarias) { this.promedioVentasDiarias = promedioVentasDiarias; }
        
        public double getPromedioVenta() { return promedioVenta; }
        public void setPromedioVenta(double promedioVenta) { this.promedioVenta = promedioVenta; }
        
        public LocalDate getFechaInicio() { return fechaInicio; }
        public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
        
        public LocalDate getFechaFin() { return fechaFin; }
        public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    }
    
    public static class FacturaTop implements Serializable {
        private static final long serialVersionUID = 1L;
        private String numeroFactura;
        private String nombreCliente;
        private double total;
        private LocalDate fechaCreacion;
        private int numeroServicios;
        
        // Getters y setters
        public String getNumeroFactura() { return numeroFactura; }
        public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }
        
        public String getNombreCliente() { return nombreCliente; }
        public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
        
        public double getTotal() { return total; }
        public void setTotal(double total) { this.total = total; }
        
        public LocalDate getFechaCreacion() { return fechaCreacion; }
        public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }
        
        public int getNumeroServicios() { return numeroServicios; }
        public void setNumeroServicios(int numeroServicios) { this.numeroServicios = numeroServicios; }
    }
    
    /**
     * Obtiene las top facturas por importe en un período
     */
    public List<FacturaTop> obtenerTopFacturasPorImporte(int limite, LocalDate fechaInicio, LocalDate fechaFin) {
        List<FacturaTop> topFacturas = new ArrayList<>();
        
        try {
            // Convertir LocalDate a Date para MongoDB
            Date fechaInicioDate = Date.from(fechaInicio.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date fechaFinDate = Date.from(fechaFin.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            // Filtro por rango de fechas y estado EMITIDA
            Bson filtroFechas = Filters.and(
                Filters.gte("fechaCreacion", fechaInicioDate),
                Filters.lt("fechaCreacion", fechaFinDate),
                Filters.eq("estado", "EMITIDA")
            );
            
            // Obtener facturas ordenadas por total descendente
            List<Document> facturas = facturasCollection.find(filtroFechas)
                .sort(Sorts.descending("total"))
                .limit(limite)
                .into(new ArrayList<>());
            
            for (Document factura : facturas) {
                FacturaTop facturaTop = new FacturaTop();
                
                // Datos básicos de la factura
                facturaTop.setNumeroFactura(factura.getString("numeroFactura"));
                facturaTop.setNombreCliente(factura.getString("nombreCliente"));
                facturaTop.setTotal(factura.getDouble("total"));
                
                // Fecha de creación
                Date fechaCreacion = factura.getDate("fechaCreacion");
                if (fechaCreacion != null) {
                    facturaTop.setFechaCreacion(fechaCreacion.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate());
                }
                
                // Contar servicios
                List<Document> servicios = factura.getList("servicios", Document.class);
                facturaTop.setNumeroServicios(servicios != null ? servicios.size() : 0);
                
                topFacturas.add(facturaTop);
            }
            
        } catch (Exception e) {
            System.err.println("Error al obtener top facturas por importe: " + e.getMessage());
            e.printStackTrace();
        }
        
        return topFacturas;
    }
    
    /**
     * Calcula el promedio real de horas trabajadas por todos los empleados en un período
     */
    private double calcularPromedioHorasReales(LocalDate inicio, LocalDate fin) {
        try {
            // Buscar todos los fichajes en el período especificado
            Document filtroFechas = new Document("fecha", 
                new Document("$gte", inicio.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .append("$lte", fin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
            
            List<Document> fichajes = fichajesCollection.find(filtroFechas).into(new ArrayList<>());
            
            if (fichajes.isEmpty()) {
                return 0.0;
            }
            
            // Calcular horas totales trabajadas usando minutosTrabajoTotal del modelo
            double horasTotales = 0.0;
            int fichajesValidos = 0;
            
            for (Document fichaje : fichajes) {
                // Usar el campo minutosTrabajoTotal que ya está calculado en ModeloFichaje
                Long minutosTrabajoTotal = fichaje.getLong("minutosTrabajoTotal");
                
                if (minutosTrabajoTotal != null && minutosTrabajoTotal > 0) {
                    double horasTrabajadas = minutosTrabajoTotal / 60.0;
                    
                    // Validar que las horas sean razonables (entre 0.5 y 12 horas)
                    if (horasTrabajadas >= 0.5 && horasTrabajadas <= 12.0) {
                        horasTotales += horasTrabajadas;
                        fichajesValidos++;
                    }
                }
            }
            
            if (fichajesValidos == 0) {
                return 0.0;
            }
            
            // Obtener número de empleados únicos que ficharon
            List<ObjectId> empleadosUnicos = fichajesCollection.distinct("empleadoId", filtroFechas, ObjectId.class)
                .into(new ArrayList<>());
            
            if (empleadosUnicos.isEmpty()) {
                return 0.0;
            }
            
            // Calcular promedio de horas por empleado
            return horasTotales / empleadosUnicos.size();
            
        } catch (Exception e) {
            System.err.println("Error al calcular promedio de horas reales: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }
} 